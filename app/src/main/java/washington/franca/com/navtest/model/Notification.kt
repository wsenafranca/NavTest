package washington.franca.com.navtest.model

import android.text.format.DateUtils
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Notification(var id:String?=null,
                        val type:Int=0,
                        val title:String?=null,
                        val body:String?=null,
                        @ServerTimestamp
                        val date:Date=Date(),
                        var status:Boolean=false)
{
    companion object {
        fun create(title: String?, body: String?):Notification {
            val time = Date()
            return Notification(null, 0, title, body, time, false)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Notification

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    fun relativeTime():CharSequence {
        return DateUtils.getRelativeTimeSpanString(date.time, System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS)
    }
}