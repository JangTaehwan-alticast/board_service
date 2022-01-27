//package com.msp.board_service.service
//
//import com.msp.board_service.config.RedisKey
//import com.msp.board_service.domain.MultiLang
//import com.msp.board_service.domain.request.InsertBoardRequest
//import com.msp.board_service.domain.response.BoardListResponse
//import kotlinx.coroutines.reactive.awaitFirst
//import kotlinx.coroutines.reactive.awaitFirstOrDefault
//import kotlinx.coroutines.runBlocking
//import org.assertj.core.api.Assertions.assertThat
//import org.junit.jupiter.api.Test
//import org.slf4j.LoggerFactory
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.data.redis.core.ReactiveRedisTemplate
//import org.springframework.data.redis.core.RedisTemplate
//import org.springframework.util.StopWatch
//
//@SpringBootTest
//class BoardServiceTest {
//
//    val logger = LoggerFactory.getLogger(this::class.java)
//
//    @Autowired
//    lateinit var redisTemplate: RedisTemplate<String, Any>
//    @Autowired
//    lateinit var boardService: BoardService
//
//
//    @Test
//    fun 게시글_단건_삽입() {
//        // given
//        val opsForValue = redisTemplate.opsForValue()
//        var key ="singleBoard"
//        var resultMap: HashMap<String, *>?
//        runBlocking {
//          resultMap = boardService
//              .getBoardList("","","","","","",0,0,"","")
//              .awaitFirstOrDefault(null)
//        }
//        var boardList:ArrayList<BoardListResponse> = resultMap!!["data"] as ArrayList<BoardListResponse>
//        var postId = boardList[0].postId
//
//        //when
//        opsForValue.set(key,boardList[0])
//
//        //then
//        var value = opsForValue.get(key) as BoardListResponse
//        logger.info("key : $key, value: $value")
//        assertThat(postId).isEqualTo(value.postId)
//    }
//
//    @Test
//    fun 게시글_리스트통째로_삽입() {
//        val stopWatch = StopWatch("게시글_리스트_삽입")
//        //given
//        val opsForList = redisTemplate.opsForList()
//        val key: String = "multiListBoard"
//        var resultMap: HashMap<String, *>?
//        stopWatch.start("게시글 가져오기")
//        runBlocking {
//            resultMap = boardService
//                .getBoardList("","","","","","",0,0,"","")
//                .awaitFirstOrDefault(null)
//        }
//        stopWatch.stop()
//        var boardList:ArrayList<BoardListResponse> = resultMap!!["data"] as ArrayList<BoardListResponse>
//        var size = boardList.size
//
//
//        //when
//        stopWatch.start("redis에 data 삽입하기")
//        opsForList.leftPush(key,boardList)
//
//        //then
//        stopWatch.stop()
//        stopWatch.start("redis에서 data 꺼내오기")
//        var value = opsForList.index(key,0) as ArrayList<BoardListResponse>
//        stopWatch.stop()
//        for (boardListResponse in value) {
//            logger.info("boardListResponse : $boardListResponse \n")
//        }
//        assertThat(size).isEqualTo(value.size)
//        logger.info("테스트 시간 ${stopWatch.prettyPrint()}")
//
//        /**
//         *  여기서 고민해야 할 점
//         */
//    }
//
//    @Test
//    fun 게시글_하나씩_리스트로_삽입() {
//        //given
//        val opsForList = redisTemplate.opsForList()
//        val key: String = "latestBoard"
//        var resultMap: HashMap<String, *>?
//        runBlocking {
//            resultMap = boardService
//                .getBoardList("","","","","","",0,0,"","")
//                .awaitFirstOrDefault(null)
//        }
//        var boardList:ArrayList<BoardListResponse> = resultMap!!["data"] as ArrayList<BoardListResponse>
//
//        //when
//        for (boardListResponse in boardList) {
//            opsForList.rightPush(key,boardListResponse)
//        }
//
//        //then
//        val end = opsForList.size(key)-1
//        val list = opsForList.range(key, 0, end) as ArrayList<BoardListResponse>
//        for (boardListResponse in list) {
//            logger.info("[from redis]boardListResponse : $boardListResponse \n")
//        }
//        assertThat(boardList.size).isEqualTo(list.size)
//    }
//
//    @Test
//    fun rightPop_테스트() {
//        //given
//        val opsForList = redisTemplate.opsForList()
//        val key = "multiBoard"
//        val end = opsForList.size(key)
//        val boardList = opsForList.range(key, 0, end) as ArrayList<BoardListResponse>
//
//        //when
//        val leftPop = opsForList.rightPop("multiBoard") as BoardListResponse
//        logger.info("remove post : $leftPop")
//        val secondList = opsForList.range(key, 0, end) as ArrayList<BoardListResponse>
//
//        //then
//        assertThat(boardList.size).isNotEqualTo(secondList.size)
//    }
//
//    @Test
//    fun 게시물_등록시_redis_업데이트() {
//        //given
//        게시글_하나씩_리스트로_삽입()
//        val opsForList = redisTemplate.opsForList()
//        val key = "multiBoard"
//        val end = opsForList.size(key)
//        var boardListResponse:BoardListResponse
//
//
//        val title = ArrayList<MultiLang>()
//        title.add(MultiLang(
//            lang="en",
//            value="영어지만 한글로 작성할거야"
//        )
//        )
//        title.add(MultiLang(
//            lang="ko",
//            value="한글이니까 한글로 작성할거야"
//        ))
//
//        //when
//        runBlocking {
//           boardListResponse= boardService.insertBoard(InsertBoardRequest(
//                nickName = "redisMan",
//                title = title,
//                contents = "내용인데 이건 그냥 대충 길이만 맞춰서 쭈욱 쓰면 될거같은데 Redis insert 테스트야"
//            )).awaitFirst()
//        }
//        opsForList.rightPop(key)
//        opsForList.leftPush(key,boardListResponse)
//
//        //then
//        val boardList = opsForList.range(key, 0, end) as ArrayList<BoardListResponse>
//        for (boardListResponse in boardList) {
//            logger.info("boardList : $boardListResponse")
//        }
//
//        val board = opsForList.index(key, 0) as BoardListResponse
//        assertThat(board.nickName).isEqualTo("redisMan")
//    }
//
//
//    @Test
//    fun 단건_Value_입력() {
//        //given
//        val opsForValue = redisTemplate.opsForValue()
//        var resultMap: HashMap<String, *>?
//        runBlocking {
//            resultMap = boardService
//                .getBoardList("","","","","","",1,100,"","")
//                .awaitFirstOrDefault(null)
//        }
//        var boardList:ArrayList<BoardListResponse> = resultMap!!["data"] as ArrayList<BoardListResponse>
//
//        var keyList = ArrayList<String?>()
//        //when
//        for (boardListResponse in boardList) {
//            opsForValue.set(boardListResponse.postId,boardListResponse)
//            keyList.add(boardListResponse.postId)
//        }
//        opsForValue.set("keyList",keyList)
//
//        //then
//        val board = opsForValue.get(boardList[0].postId) as BoardListResponse
//        val keys = opsForValue.get("keyList") as List<*>
//        for (key in keys) {
//            logger.info("key List : $key")
//        }
//        assertThat(board.postId).isEqualTo(boardList[0].postId)
//    }
//
//    @Test
//    fun 게시글_삭제시_redis_업데이트() {
//        //given
//        val targetPostId = "post_151943"
//
//        val opsForList = redisTemplate.opsForList()
//        val key = "latestBoard"
//        boardService.getBoardList("","","","","","",0,0,"","")
//        val size = opsForList.size(key)
//        val boardList = opsForList.range(key, 0, size) as ArrayList<BoardListResponse>
//        var tarBoard: BoardListResponse? = null
//
//        boardList.map {
//            if (it.postId.equals(targetPostId))
//                tarBoard = it
//        }
//
//        val originalBoard = tarBoard
//        val targetBoard = opsForList.index(key, 0) as BoardListResponse
//
//        //when
//        opsForList.remove(key,1,originalBoard)
//
//        //then
//        val afterDelSize = opsForList.size(key)
//        val afterDelBoard = opsForList.index(key, 0) as BoardListResponse
//
//        logger.info("삭제 전 : $size / 삭제 후 : $afterDelSize")
//        assertThat(afterDelSize).isLessThan(size)
//        assertThat(targetBoard.postId).isEqualTo(originalBoard!!.postId)
//        assertThat(targetBoard.postId).isNotEqualTo(afterDelBoard.postId)
//    }
//
//
//    @Test
//    fun trim_method_test() {
//        //given
//        val opsForList = redisTemplate.opsForList()
//        boardService.getBoardList("","","","","","",0,0,"","")
//        val key = RedisKey.LATEST_BOARD
//        var boardList = opsForList.range(key, 0, 30) as ArrayList<BoardListResponse>
//        val bListSize = boardList.size
//
//        //when
//        redisTemplate.delete(key)
//
//        boardList = opsForList.range(key,0,30) as ArrayList<BoardListResponse>
//        val aListSize = boardList.size
//
//        //then
//        assertThat(bListSize).isNotEqualTo(aListSize)
//
//    }
//
//
//
//
//
//}