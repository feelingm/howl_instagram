package com.feelingm.instagram.model

data class ContentDto(var explain: String? = null,
                      var imageUrl: String? = null,
                      var uid: String? = null,
                      var userId: String? = null,
                      var timestamp: Long? = null,
                      var favoriteCount: Int = 0,
                      var favorites: Map<String, Boolean> = mutableMapOf()) {

    data class Comment(var uid: String? = null,
                       var userId: String? = null,
                       var comment: String? = null,
                       var timestamp: Long? = null)

}
