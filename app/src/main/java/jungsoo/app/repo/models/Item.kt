package jungsoo.app.repo.models

data class Item (
    var id: String = "",
    var qrUrl: String = "",
    var thumbnail: String = "",
    val name: String = "",
    val price: String = "",
    var quantity: Int = 0
)