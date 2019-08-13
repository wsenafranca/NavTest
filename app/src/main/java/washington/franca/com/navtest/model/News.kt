package washington.franca.com.navtest.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

data class News(
    var title:String?=null,
    var description:String? = null,
    var url:String? = null,
    var urlToImage:String? = null,
    var publishedAt: Date = Date(),
    var content:String? = null,
    var source:Source? = null,
    var author:String?=null) {

    data class Source(var id:String?=null, var name:String?=null)
}

@Entity(tableName = "news_suggestion")
data class NewsSuggestion(@PrimaryKey val text:String, val time:Long=System.currentTimeMillis())
