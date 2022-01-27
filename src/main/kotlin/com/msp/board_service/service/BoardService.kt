package com.msp.board_service.service

import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult
import com.msp.board_service.common.customValidation.TitleValueValidator
import com.msp.board_service.config.RedisKey
import com.msp.board_service.domain.Board
import com.msp.board_service.domain.DeleteBoardHistory
import com.msp.board_service.domain.ModifyBoardHistory
import com.msp.board_service.domain.request.InsertBoardRequest
import com.msp.board_service.domain.request.ModBoardRequest
import com.msp.board_service.domain.response.BoardListResponse
import com.msp.board_service.domain.response.BoardResponse
import com.msp.board_service.exception.CustomException
import com.msp.board_service.repository.BoardRepository
import com.msp.board_service.repository.CommentRepository
import com.msp.board_service.repository.HistoryRepository
import com.msp.board_service.repository.SequenceRepository
import com.msp.board_service.util.CommonService
import com.msp.board_service.util.MakeWhereCriteria
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrDefault
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.aggregation.*
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.util.StopWatch
import reactor.core.publisher.Mono
import java.util.concurrent.TimeUnit


interface BoardServiceIn {
    fun getOneBoard(postId: String):Mono<BoardResponse>
    fun getBoardList(
        postId: String, category: String, nickName: String, title: String,
        contents: String, q: String, page: Long, size: Long, orderBy: String, lang: String
    ):Mono<HashMap<String,*>>
    fun insertBoard(param: InsertBoardRequest): Mono<BoardListResponse>
    fun deleteBoard(postId: String):Mono<DeleteResult>
    fun modifyBoard(postId: String, modBoardDTO: ModBoardRequest):Mono<UpdateResult>
    fun getBoardFromRedis(key: String, page: Long, size: Long):Mono<HashMap<String,*>>
    fun insertBoardToRedis(Key: String, page: Long, size: Long):ArrayList<BoardListResponse>
    fun updateRedisStatus(postId: String?, boardResponse: BoardResponse?, boardListResponse: BoardListResponse?, flag: String)
}

@Service
class BoardService:BoardServiceIn {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Autowired
    lateinit var boardRepository: BoardRepository
    @Autowired
    lateinit var seqRepository: SequenceRepository
    @Autowired
    lateinit var historyRepository: HistoryRepository
    @Autowired
    lateinit var commentRepository: CommentRepository
    @Autowired
    lateinit var redisTemplate: RedisTemplate<String,Any>
    @Autowired
    lateinit var commonService: CommonService

    /**
     * 게시판 글 단건 조회
     * @param postId 조회하고자 하는 게시글의 아이디
     *
     * @suppress 1. 게시글이 존재하는지 확인
     * 2. redis 캐시 데이터 확인
     * 3. 응답 전용 시간 변환 (epoch time -> yyyy-MM-dd'T'HH:mm:ss)
     * 4. 요청 게시글 반환
     *
     * @exception CustomException.invalidPostId 유효하지 않은 postId 인 경우
     */
    override fun getOneBoard(postId: String): Mono<BoardResponse> {
        return runBlocking {
            val stopWatch = StopWatch("getOneBoard")
            logger.info("getOneBoard Param : $postId")
            stopWatch.start("[getOneBoard]Find Board From Redis")

            /**
             * 단건조회시 redis 확인 후 있으면 return
             */
            val opsForValue = redisTemplate.opsForValue()

            opsForValue.get(postId)?.let {
                stopWatch.stop()
                logger.info("[getOneBoard]Return from redis : $postId")
                return@runBlocking Mono.just(it as BoardResponse)
            }

            stopWatch.stop()
            stopWatch.start("[getOneBoard]Find Board From Collection")

            val regex = Regex("^post_[0-9]*")
            val matches = postId.matches(regex)
            if (!matches){
                throw CustomException.invalidPostId(postId)
            }

            val query = Query(where("postId").`is`(postId))

            val board = withContext(Dispatchers.Default) {
                boardRepository.findOneBoard(query).awaitFirstOrDefault(null)
            } ?: throw CustomException.invalidPostId(postId)

            val createdDate = CommonService.epochToString(board.createdDate!!)

            val lastUpdatedDate = if(board.lastUpdatedDate == null || board.lastUpdatedDate!! <= 0){
                createdDate
            }else{
                CommonService.epochToString(board.lastUpdatedDate!!)
            }

            val boardResponse = BoardResponse(
                postId = board.postId,
                nickName = board.nickName,
                category = board.category,
                title = board.title,
                contents = board.contents,
                createdDate = createdDate,
                lastUpdatedDate = lastUpdatedDate
            )
            opsForValue.set(postId,boardResponse,86400L,TimeUnit.SECONDS)

            stopWatch.stop()
            logger.debug("[getOneBoard]${stopWatch.prettyPrint()}")
            logger.info("getOneBoard time : ${stopWatch.totalTimeMillis}ms.")
            Mono.just(boardResponse)
        }
    }

    /**
     * 게시글 검색 포함 목록
     *
     * @param postId 검색 대상 게시글의 아이디
     * @param category 카테고리 검색
     * @param nickName 작성자 검색
     * @param title 게시글 제목 검색
     * @param contents 게시글 내용 검색
     * @param q 다중 조건 검색용 q
     * @param page offset
     * @param size interval
     * @param orderBy 정렬 기준
     * @param lang 다국어 조회(제목)
     *
     * @suppress 1. parameter & paging 점검 후 Redis 캐시 확인
     * 2. 조회 할 필드 project 추가(lang 입력시 해당 언어 추가 default all)
     * 3. Criteria 생성. 노출예약을 고려해서 현재시각 > exposureDate 조건 추가
     * 4. 검색 parameter 값 존재하면 해당 Criteria 추가
     * 5. paging 처리 후 게시글 count
     * 6. 검색 조건에 따른 결과 반환
     */
    override fun getBoardList(
        postId: String,
        category: String,
        nickName: String,
        title: String,
        contents: String,
        q: String,
        page: Long,
        size: Long,
        orderBy: String,
        lang: String
    ): Mono<HashMap<String,*>> {
        logger.info(
            "getBoardList Param : postId= $postId, category= $category, nickName= $nickName, title= $title," +
                    " contents= $contents, q= $q, page= $page, size= $size, orderBy= $orderBy, lang= $lang")
        val stopWatch = StopWatch("getBoardList")
        stopWatch.start("[getOneBoard]Find BoardList From Redis")

        /**
         * 게시글 리스트를 조회하기 앞서 파라미터를 모두 점검하고 빈값일 경우 Redis를 통해 return 한다
         */
        val params = (postId+category+nickName+title+contents+q+orderBy+lang)
        val paging = if (page<=0) {
            1 * size
        } else {
            page * size
        }

        if (params.isEmpty() && paging <= 100) {
            val key = RedisKey.LATEST_BOARD
            return getBoardFromRedis(key, page, size).doFinally {
                stopWatch.stop()
                logger.debug("[BoardService]${stopWatch.prettyPrint()}")
                logger.info("getBoardList time: ${stopWatch.totalTimeMillis}ms.")
            }
        }

        stopWatch.stop()
        stopWatch.start("[getBoardList]Make Aggregation")

        val listAggOps = ArrayList<AggregationOperation>()
        val countAggOps = ArrayList<AggregationOperation>()

        /**
         * Document 에서 필요한 field 정의
         * 다국어 처리도 같이 실행
         */
        val project: ProjectionOperation = if(lang.isNotEmpty()){
            Aggregation.project(
                "postId","nickName","title","category","createdDate","exposureDate","useYn","contents"
            ).and(
                ArrayOperators.Filter.filter("title").`as`("title").by(
                    BooleanOperators.Or.or(
                        ComparisonOperators.Eq.valueOf("title.lang").equalToValue(lang.toLowerCase())
//                        ComparisonOperators.Eq.valueOf("title.lang").equalToValue(DefaultCode.FALLBACK_LANG)
                    )
                )
            ).`as`("title")
        }else{
            Aggregation.project(
                "postId","nickName","title","category","createdDate","exposureDate","useYn","contents"
            )
        }
        listAggOps.add(project)
        countAggOps.add(project)


        /**
         * Criteria 설정
         */
        val now = CommonService.getNowEpochTime()
        val criteria = setMatchCriteria(
                postId, category, nickName, title, contents, q, now
            )
        val match: AggregationOperation = Aggregation.match(criteria)
        listAggOps.add(match)
        countAggOps.add(match)
        val countAgg : AggregationOperation = Aggregation.group("useYn").count().`as`("TotalCount")
        countAggOps.add(countAgg)


        /**
         * 정렬 조건
         * default : createdDate DESC
         */
        val orderProject = setOf(
            "postId", "nickName", "title", "category", "createdDate", "exposureDate", "contents"
        )

        var orderArr = arrayOf("createdDate:-1")
        if(orderBy != ""){
            orderArr = orderBy.split(",").toTypedArray()
        }
        val orders = ArrayList<Sort.Order>()
        for (order in orderArr) {
            val fieldName = order.split(":")[0]

            if (!orderProject.contains(fieldName))
                throw CustomException.invalidSortField(fieldName)


            val sortOrder = order.split(":")[1].toInt()
            if(sortOrder ==1 ){
                orders.add(Sort.Order(Sort.Direction.ASC,fieldName))
            }else{
                orders.add(Sort.Order(Sort.Direction.DESC,fieldName))
            }
        }
        listAggOps.add(Aggregation.sort(Sort.by(orders)))

        /**
         * paging 처리
         * default : page 0, size 10(limit 200)
         */
        if (size < 0L || size > 200L){
            throw CustomException.invalidSizeRange()
        }else if (page < 0L){
            throw CustomException.invalidPageRange()
        }

        val limitValue = if (size in 1..200) {
            size
        } else {
            10L
        }
        val skipValue = if (page > 0L) {
            (page - 1) * limitValue
        } else {
            0L
        }
        val skip: AggregationOperation = Aggregation.skip(skipValue)
        val limit: AggregationOperation = Aggregation.limit(limitValue)
        listAggOps.add(skip)
        listAggOps.add(limit)

        val listAgg = Aggregation.newAggregation(listAggOps)

        stopWatch.stop()
        stopWatch.start("[getBoardList]Count Board")
        logger.info("[getBoardList]query = $listAgg")
        return boardRepository.findBoardMapList(Aggregation.newAggregation(countAggOps)).collectList().flatMap { countMap ->
            stopWatch.stop()
            stopWatch.start("[getBoardList]Find Board List")

            val cnt = if (countMap.isNotEmpty()){
                countMap.first().toMutableMap()["TotalCount"].toString().toLong()
            }else{
                0L
            }
            val resultMap = HashMap<String, Any>()
            resultMap["page"] = page
            resultMap["size"] = limitValue
            resultMap["total"] = cnt
            if (cnt > 0L){
                boardRepository.findBoardList(listAgg).collectList().flatMap {  resultBoardList ->
                    val boardList = ArrayList<BoardListResponse>()

                    stopWatch.stop()
                    stopWatch.start("[getBoardList]Convert Board To DTO")

                    resultBoardList.forEach{ board ->
                        val createdDate = CommonService.epochToString(board.createdDate!!)
                        boardList.add(
                            BoardListResponse(
                                postId = board.postId,
                                nickName = board.nickName,
                                title = board.title,
                                contents = board.contents,
                                category = board.category,
                                createdDate = createdDate
                            )
                        )
                    }
                    resultMap["data"] = boardList

                    stopWatch.stop()
                    logger.debug("[getBoardList]${stopWatch.prettyPrint()}")
                    logger.info("getBoardList time: ${stopWatch.totalTimeMillis}ms.")
                    Mono.just(resultMap)
                }
            }else{
                stopWatch.stop()
                logger.debug("[getBoardList]${stopWatch.prettyPrint()}")
                logger.info("getBoardList time: ${stopWatch.totalTimeMillis}ms.")
                resultMap["data"] = ArrayList<BoardListResponse>()
                Mono.just(resultMap)
            }
        }

    }


    /**
     * 게시글 입력
     * @param param 게시글 입력 필드
     * @suppress 1.board seq 생성 및 boardId 부여
     * 2. 노출시각 예약이 존재할 경우 반영 없으면 현재시각으로 설정
     * 3. board collection 게시글 document 삽입
     * 4. Redis 게시글 리스트 update
     */
    override fun insertBoard(param: InsertBoardRequest): Mono<BoardListResponse> {
        return runBlocking {
            logger.info("insertBoard Param : $param")
            val stopWatch = StopWatch("insertBoard")
            stopWatch.start("[insertBoard]Insert Board")

            /**
             * title language lowercase 변환
             */
            param.title!!.forEach { multiLang ->
                multiLang.lang = multiLang.lang.toLowerCase()
            }

            /**
             * sequence 조회 및 update
             */
            val seq = async {
                seqRepository.getNextSeqIdUpdateInc("board").awaitFirstOrDefault(null)
            }

            val now = CommonService.getNowEpochTime()
            val expDate = if (param.exposureDate != null && param.exposureDate!! >= now) {
                param.exposureDate
            } else {
                now
            }
            val board = Board(
                nickName = param.nickName,
                contents = param.contents,
                title = param.title,
                category = param.category ?: "all",
                useYn = "11",
                createdDate = now,
                exposureDate = expDate
            )

            board.postId = "post_${seq.await().seq}"
            boardRepository.insertBoard(board).awaitFirst()

            val createdDate = CommonService.epochToString(board.createdDate!!)
            val resBoard = BoardListResponse(
                postId = board.postId,
                nickName = board.nickName,
                category = board.category,
                title = board.title,
                contents = board.contents,
                createdDate = createdDate
            )
            stopWatch.stop()
            /**
             * update redis status
             */
            stopWatch.start("[insertBoard]Update Redis Status")
            updateRedisStatus(null,null,resBoard,"create")
            stopWatch.stop()

            logger.debug("[BoardService]${stopWatch.prettyPrint()}")
            logger.info("insertBoard time : ${stopWatch.totalTimeMillis}ms.")
            Mono.just(resBoard)
        }
    }


    /**
     * 게시글 삭제
     * @param postId 대상 게시글의 아이디
     *
     * @suppress 1. 게시글이 존재하는지 판단
     * 2. history 의 seq 생성 및 historyId 부여
     * 3. 해당 게시글의 postId 를 가지고 있는 댓글과 게시글 history collection 이동
     * 4. 기존 댓글, 게시글 삭제
     * 5. Redis status 변경
     * @exception CustomException.invalidPostId 유효하지 않은 postId 입력시
     */
    override fun deleteBoard(postId:String):Mono<DeleteResult> {
        return runBlocking {
            logger.info("deleteBoard param : $postId")
            val stopWatch = StopWatch("deleteBoard")
            stopWatch.start("[deleteBoard]Start Delete Board")

            val regex = Regex("^post_[0-9]*")
            val matches = postId.matches(regex)
            if (!matches)
                throw CustomException.invalidPostId(postId)

            val query = Query(where("postId").`is`(postId))
            val deleteBoardHistory = DeleteBoardHistory()

            val board = async {
                boardRepository.findOneBoard(query).awaitFirstOrElse {
                    throw CustomException.invalidPostId(postId)
                }
            }

            val seq = async {
                seqRepository.getNextSeqIdUpdateInc("history").awaitFirstOrElse {
                    throw IllegalStateException("ServerError")
                }
            }

            val commentList = async {
                commentRepository.findAllComment(query).collectList().awaitFirstOrDefault(null)
            }
            val insertedBoard = board.await()

            stopWatch.stop()
            /**
             * update redis status
             */
            stopWatch.start("[deleteBoard]Update Redis Status")
            updateRedisStatus(insertedBoard.postId,null,null,"delete")
            stopWatch.stop()

            stopWatch.start("[deleteBoard]Update boardHistory & Delete Board")
            deleteBoardHistory.type = "DELETE"
            deleteBoardHistory.board = insertedBoard
            deleteBoardHistory.postId = insertedBoard.postId
            deleteBoardHistory.deletedDate = CommonService.getNowEpochTime()
            deleteBoardHistory.historyId = "history_${seq.await().seq}"
            deleteBoardHistory.comment = commentList.await().toCollection(ArrayList())

            withContext(Dispatchers.IO) {
                commentRepository.deleteComment(query)
                historyRepository.insertBoardHistory(deleteBoardHistory)
                boardRepository.deleteBoard(query)
            }.flatMap {
                Mono.just(it)
            }.doFinally {
                stopWatch.stop()
                logger.debug("[deleteBoard]${stopWatch.prettyPrint()}")
                logger.info("deleteBoard time : ${stopWatch.totalTimeMillis}ms.")
            }
        }
    }

    /**
     * 게시글 수정
     * @param postId 대상 게시글의 아이디
     * @param param 수정필드 DTO
     *
     * @suppress 1. 게시글이 존재하는지 판단
     * 2. history 의 seq 생성 및 historyId 부여
     * 3. history collection 에 현재 존재하는 버전 count 후 version 부여
     * 4. history collection 에 PATCH type 으로 document 생성
     * 5. 기존 게시물 수정
     * @exception CustomException.validation 파라미터 유효성 검사
     * @exception CustomException.invalidPostId 유효하지 않은 postId 입력시
     * @exception IllegalStateException Unexpected Server error
     */
    override fun modifyBoard(postId:String, param: ModBoardRequest):Mono<UpdateResult>{
        return runBlocking {
            logger.info("modifyBoard param : postId= $postId, $param")
            val stopWatch = StopWatch("modifyBoard")
            stopWatch.start("[modifyBoard]Update boardHistory")

            val query = Query(where("postId").`is`(postId))
            val modifyBoardHistory = ModifyBoardHistory()

            if(!param.contents.isNullOrEmpty() && param.contents!!.length > 255){
                throw CustomException.validation(message = "길이가 0에서 255 사이여야 합니다",field = "contents")
            }else if(!param.title.isNullOrEmpty()){
                TitleValueValidator.titleValidation(param.title!!)
                param.title!!.forEach { multiLang ->
                    multiLang.lang = multiLang.lang.toLowerCase()
                }
            }
            val boardAsync = async {
                boardRepository.findOneBoard(query).awaitFirstOrElse {
                    throw CustomException.invalidPostId(postId)
                }
            }
            val seq = async {
                seqRepository.getNextSeqIdUpdateInc("history").awaitFirstOrElse {
                    throw IllegalStateException("ServerError")
                }
            }
            val verCnt = async {
                getCountHistoryBoard(postId,"PATCH").awaitFirstOrElse {
                    throw IllegalStateException("ServerError")
                }
            }

            val board = boardAsync.await()

            val createdDate = CommonService.epochToString(board.createdDate!!)
            val lastUpdatedDate = if(board.lastUpdatedDate != null) {
                CommonService.epochToString(board.lastUpdatedDate!!)
            }else{
                ""
            }
            val boardRes = BoardResponse(
                postId = board.postId,
                nickName = board.nickName,
                category = board.category,
                title = board.title,
                contents = board.contents,
                createdDate = createdDate,
                lastUpdatedDate = lastUpdatedDate
            )
            modifyBoardHistory.board = boardRes
            modifyBoardHistory.postId = board.postId
            modifyBoardHistory.type = "PATCH"
            modifyBoardHistory.updatedDate = CommonService.getNowEpochTime()
            modifyBoardHistory.modifier = param.modifier
            modifyBoardHistory.historyId = "history_${seq.await().seq}"
            modifyBoardHistory.version = "V${verCnt.await()+1}.0"


            withContext(Dispatchers.IO){
                historyRepository.insertBoardHistory(modifyBoardHistory).awaitFirstOrElse {
                    throw IllegalStateException("ServerError")
                }
            }

            val update = Update()
            param.title?.let {
                boardRes.title = param.title
                update.set("title",param.title)
            }
            param.contents?.let {
                boardRes.contents = param.contents
                update.set("contents",param.contents)
            }
            update.set("modifier",param.modifier)
            update.set("lastUpdatedDate",modifyBoardHistory.updatedDate)

            stopWatch.stop()
            /**
             * update redis status
             */
            stopWatch.start("[modifyBoard]Update Redis Status")
            updateRedisStatus(null,boardRes,null,"update")
            stopWatch.stop()
            stopWatch.start("[modifyBoard]Update Board")

            boardRepository.modifyBoard(query, update).flatMap {
                Mono.just(it)
            }.doFinally {
                stopWatch.stop()
                logger.debug("[modifyBoard]${stopWatch.prettyPrint()}")
                logger.info("modifyBoard time : ${stopWatch.totalTimeMillis}ms.")
            }
        }
    }

    /**
     * history type 별 count 조회
     */
    fun getCountHistoryBoard(postId:String,type:String):Mono<Long>{
        val query = Query(where("postId").`is`(postId).and("type").`is`(type))
        return historyRepository.countHistoryBoard(query)
    }

    /**
     * Field 에 따른 Criteria 생성
     * @param postId    게시글 id
     * @param category  게시글의 카테고리
     * @param nickName  작성자
     * @param title     다국어 제목
     * @param contents  게시글 내용
     * @param q         q 검색
     * @param now       exposureDate 기준 검색을 위한 현재시각
     */
    fun setMatchCriteria(
        postId: String, category: String, nickName: String,
        title: String, contents: String, q: String, now: Long
    ): Criteria {
        val criteria = Criteria()
        val andCriteria = ArrayList<Criteria>()
        andCriteria.add(MakeWhereCriteria.makeWhereCriteria("exposureDate","le",now.toString(),"long"))
        if(postId.isNotEmpty()){
            val paramValue = postId.split("?")
            if(paramValue.size == 2){
                andCriteria.add(MakeWhereCriteria.makeWhereCriteria("postId",paramValue[0],paramValue[1]))
            }
        }
        if(category.isNotEmpty()){
            val paramValue = category.split("?")
            if(paramValue.size == 2){
                andCriteria.add(MakeWhereCriteria.makeWhereCriteria("category",paramValue[0],paramValue[1]))
            }
        }
        if(nickName.isNotEmpty()){
            val paramValue = nickName.split("?")
            if(paramValue.size == 2){
                andCriteria.add(MakeWhereCriteria.makeWhereCriteria("nickName",paramValue[0],paramValue[1]))
            }
        }
        if(title.isNotEmpty()){
            val paramValue = title.split("?")
            if(paramValue.size == 2){
                andCriteria.add(MakeWhereCriteria.makeWhereCriteria("title.value",paramValue[0],paramValue[1]))
            }
        }
        if(contents.isNotEmpty()){
            val paramValue = contents.split("?")
            if(paramValue.size == 2){
                andCriteria.add(MakeWhereCriteria.makeWhereCriteria("contents",paramValue[0],paramValue[1]))
            }
        }
        if(q.isNotEmpty()){
            val qList = StringUtils.deleteWhitespace(q).split(",")
            val paramList = ArrayList<String>()
            var idx = -1
            qList.forEach{
                if(it.indexOf("%")>0){
                    paramList.add(it)
                    idx++
                }else{
                    paramList[idx] = paramList[idx]+","+it
                }
            }

            paramList.forEach {
                val param = it.split("%")
                if(param.size == 2){
                    val paramName = if(param[0] == "title"){
                        "title.value"
                    }else{
                        param[0]
                    }
                    val paramValues = param[1].split("?")
                    if(paramValues.size == 2){
                        var valueType = "string"
                        if(StringUtils.equalsAnyIgnoreCase(paramName,
                                "exposureDate","createdDate","lastUpdatedDate")){
                            valueType = "Long"
                        }else if(StringUtils.equalsAnyIgnoreCase(paramName,
                                "input any Int value")){
                            valueType = "int"
                        }
                        andCriteria
                            .add(MakeWhereCriteria.makeWhereCriteria(paramName, paramValues[0],paramValues[1],valueType))
                    }
                }
            }
        }
        // And Criteria 가 존재하는 경우 Criteria And Operator 추가
        if(andCriteria.size>0){
            criteria.andOperator(*andCriteria.toTypedArray())
        }
        return criteria
    }

    override fun getBoardFromRedis(key:String, page: Long, size: Long): Mono<HashMap<String, *>> {
        logger.info("[getBoardFromRedis]Find boardList from redis")

        if (size < 0L || size > 200L){
            throw CustomException.invalidSizeRange()
        }else if (page < 0L){
            throw CustomException.invalidPageRange()
        }

        val opsForList = redisTemplate.opsForList()
        val opsForValue = redisTemplate.opsForValue()
        val resultMap = HashMap<String,Any>()

        val limitValue = if (size in 1..100) {
            size.toInt()
        } else {
            10
        }
        val skipValue = if (page > 0L) {
            ((page - 1) * limitValue).toInt()
        } else {
            0
        }

        resultMap["page"] = page
        resultMap["size"] = limitValue
        val boardSize = opsForList.size(key)

        if (boardSize < 100){
            val boardList = insertBoardToRedis(key, 0, 200)
            resultMap["data"] = boardList.subList(skipValue,skipValue+limitValue)
        } else {
            val boardList = opsForList.range(key,0,boardSize) as ArrayList<*>
            resultMap["data"] = boardList.subList(skipValue,skipValue+limitValue)
        }
        resultMap["total"] = opsForValue.get(RedisKey.TOTAL)

        return Mono.just(resultMap)
    }

    override fun insertBoardToRedis(key: String, page: Long, size: Long): ArrayList<BoardListResponse> {
        logger.info("[insertBoardToRedis]start insert boardList to redis")
        redisTemplate.delete(key)

        val opsForList = redisTemplate.opsForList()
        val opsForValue = redisTemplate.opsForValue()

        var resultMap: HashMap<String, *>

        runBlocking {
            resultMap = getBoardList("","","","","","",page,size,"","")
                .awaitFirst()
        }

        var boardList = resultMap["data"] as ArrayList<BoardListResponse>
        var total = resultMap["total"].toString().toInt()

        opsForValue.set(RedisKey.TOTAL,total)

        for (boardListResponse in boardList) {
            opsForList.rightPush(key,boardListResponse)
        }
        //게시글 리스트 유효시간 60s * 60h * 24h * 1d = 86400s
        redisTemplate.expire(key,86400L, TimeUnit.SECONDS)
        return boardList
    }

    override fun updateRedisStatus(postId: String?, boardResponse: BoardResponse?, boardListResponse: BoardListResponse?, flag: String) {
        logger.info("[updateRedisStatus]reason : $flag")
        val opsForValue = redisTemplate.opsForValue()
        val opsForList = redisTemplate.opsForList()

        when (flag){
            "create" ->{
                var total = opsForValue.get(RedisKey.TOTAL).toString().toInt()
                opsForValue.set(RedisKey.TOTAL,++total)
                opsForList.rightPop(RedisKey.LATEST_BOARD)
                opsForList.leftPush(RedisKey.LATEST_BOARD,boardListResponse)
            }
            "delete" ->{
                var boardListResponse: BoardListResponse? = null
                val size = opsForList.size(RedisKey.LATEST_BOARD)
                val boardList = opsForList.range(RedisKey.LATEST_BOARD, 0, size) as ArrayList<BoardListResponse>
                boardList.map {
                    if (postId.equals(it.postId))
                        boardListResponse = it
                }
                var total = opsForValue.get(RedisKey.TOTAL).toString().toInt()
                opsForValue.set(RedisKey.TOTAL,--total)
                opsForList.remove(RedisKey.LATEST_BOARD,1,boardListResponse)
                redisTemplate.delete(postId)
            }
            "update" ->{
                opsForValue.set(boardResponse!!.postId,boardResponse)

                val modBrdListRes = BoardListResponse(
                    postId = boardResponse.postId,
                    contents = boardResponse.contents,
                    title = boardResponse.title,
                    nickName = boardResponse.nickName,
                    createdDate = boardResponse.createdDate,
                    category = boardResponse.category
                )

                var brdListRes: BoardListResponse? = null
                val size = opsForList.size(RedisKey.LATEST_BOARD)
                val boardList = opsForList.range(RedisKey.LATEST_BOARD, 0, size) as ArrayList<BoardListResponse>

                boardList.map {
                    if (boardResponse.postId.equals(it.postId))
                        brdListRes = it
                }

                if (brdListRes != null){
                    val idx = boardList.indexOf(brdListRes!!).toLong()
                    opsForList.remove(RedisKey.LATEST_BOARD,1,brdListRes)
                    opsForList.set(RedisKey.LATEST_BOARD,idx,modBrdListRes)
                }
            }
        }
    }
}