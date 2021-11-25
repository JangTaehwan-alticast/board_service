package com.msp.board_service.domain

import org.springframework.data.mongodb.core.mapping.Document

@Document("history")
data class ModifyBoardHistory(
    var historyId: String? = null,
    var postId: String? = null,
    var type: String? = null,
    var board: Board? = null,
    var modifier: String? = null,
    var updateDate: Long? = 0,
    var updateDateRes: String? = null,
    var version: String? = null
)
@Document("history")
data class DeleteBoardHistory(
    var historyId: String? = null,
    var postId: String? = null,
    var type: String? = null,
    var board: Board? = null,
    var deletedDate: Long? = 0,
    var deletedDateRes: String? = null
)
@Document("history")
data class CommentHistory(
    var historyId: String? = null,
    var postId: String? = null,
    var comment: Any? = null
)

