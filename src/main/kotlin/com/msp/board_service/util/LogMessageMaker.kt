package com.msp.board_service.util

import org.springframework.util.StopWatch

class LogMessageMaker{

    /**
     * 1.정상 응답이 된 경우
     *   - 응답은 되었으나 value가 없는 경우
     * 2.예외가 발생했을 경우 (Internal Error는 우짜지)
     *   - CustomException
     *   - just Exception
     *
     *
     * 1. 정상응답
     * 서비스 이름
     * serviceName = BoardService,
     * serviceName = CommentService,
     * serviceName = HistoryService,
     *
     * 어떤 작업
     * function = insertBoard,
     * function = getOneBoard,
     * function = getBoardList,
     * function = modifyBoard...
     *
     * 성공여부
     * result = SUCCESS
     * - result = FAILURE
     *
     * 값
     * value = model
     *
     * ex)
     * serviceName = BoardService, function = insertBoard, result = SUCCESS, value = {이것저것~}
     * noValuePresent할 경우 value에 errorResponse 넣기
     *
     *
     * 2.예외가 발생했을 경우
     * 서비스 이름
     * serviceName = BoardService,
     * serviceName = CommentService,
     * serviceName = HistoryService,
     *
     * 어떤 작업
     * function = insertBoard,
     * function = getOneBoard,
     * function = getBoardList,
     * function = modifyBoard...
     *
     * 성공여부
     * result = FAILURE
     *
     * 메세지
     * message = postId is required
     *
     * 코드
     * code = 1000002
     *
     * serviceName = BoardService, function = insertBoard, result = FAILURE, message = postId is required, code = 1000002
     * */
    companion object{
        fun getFunctionLog(stopWatch: StopWatch, serviceName: String, function: String) =
            LogMessageMaker()
                .getFunctionLog(
                    stopWatch = stopWatch,
                    serviceName = serviceName,
                    function = function
                )
        fun getSuccessLog(
            stopWatch: StopWatch, serviceName: String, function: String, result: String, value: Any, path:Any, param:Any
        ) =
            LogMessageMaker()
                .getSuccessLogMsg(
                    stopWatch = stopWatch,
                    serviceName = serviceName,
                    function = function,
                    result = result,
                    value = value,
                    path = path,
                    param = param
                )
        fun getFailureLog(
            stopWatch: StopWatch, serviceName: String, function: String, result: String, message: String, code: Int, path:Any, param:Any
        ) =
            LogMessageMaker()
                .getFailureLogMsg(
                    stopWatch = stopWatch,
                    serviceName = serviceName,
                    function = function,
                    result = result,
                    message = message,
                    code = code,
                    path = path,
                    param = param
                )

    }
    fun getFunctionLog(stopWatch: StopWatch, serviceName: String, function: String):String{
        stopWatch.stop()
        val time = stopWatch.totalTimeMillis
        return "serviceName=$serviceName, function=$function, time=$time ms"
    }
    //serviceName = BoardService, function = insertBoard, time = 40ms, result = SUCCESS, value = {이것저것~}
    fun getSuccessLogMsg(
        stopWatch: StopWatch,serviceName: String, function: String, result: String, value:Any, path:Any, param:Any
    ):String{
        stopWatch.stop()
        val time = stopWatch.totalTimeMillis
        return "serviceName=$serviceName, function=$function, time=$time ms, result=$result, value=$value, path=$path, param=$param"
    }
    //serviceName = BoardService, function = insertBoard, time = 40ms, result = FAILURE, message = postId is required, code = 1000002
    fun getFailureLogMsg(
        stopWatch: StopWatch,serviceName: String, function: String, result: String, message: String, code : Int, path:Any, param:Any
    ):String{
        stopWatch.stop()
        val time = stopWatch.totalTimeMillis
        return "serviceName=$serviceName, function=$function, time=$time ms, result=$result, message=$message, code=$code, path=$path, param=$param"
    }
}