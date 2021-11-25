package com.msp.board_service.domain

import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("boards")
data class Board(
    var postId: String? = null,
    var nickName: String? = null,
    var title: ArrayList<MultiLang>? = null,
    var contents: String? = null,
    var category: String? = null,//enum 으로 설정할지 고민해볼것
    var useYn: String? = null,
    var exposureDate: Long? = null,
    var exposureDateRes: String? = null,
    var createdDate: Long? = null,
    var createdDateRes: String? = null,
    var updateDate: Long? = null,
    var updateDateRes: String? = null,
    var deletedDate: Long? = null,
    var deletedDateRes: String? = null,
//    var History: ArrayList<History>? = null

)



data class Comment(
    var postId: String? = null,
    var nickName: String? = null,
    var contents: String? = null,
    var createdDate: Long? = null,
    var createdDateRes: String? = null,
    var updateDate: Long? = null,
    var updateDateRes: String? = null
)

