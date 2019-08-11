package washington.franca.com.navtest.repository

import com.google.firebase.firestore.*
import washington.franca.com.navtest.model.Notification

class NotificationRepository(private val uid:String) {
    private val db = FirebaseFirestore.getInstance()
    private var cursor:List<DocumentSnapshot>? = null

    private fun doc(): CollectionReference {
        return db.collection("users").document(uid).collection("notifications")
    }

    fun load(pageSize:Int, lastId:String?, callback:(ArrayList<Notification>)->Unit, errorCallback:(Throwable?)->Unit) {
        var query = doc().orderBy("date", Query.Direction.DESCENDING).limit(pageSize.toLong())
        if(lastId != null) {
            //cursor?.find { it.id == lastId }.let {
            cursor?.lastOrNull()?.let {
                query = query.startAfter(it)
            }
        }
        query.get().addOnSuccessListener {
            val list = ArrayList<Notification>()
            cursor = it.documents
            for(document in it) {
                val notification = document.toObject(Notification::class.java)
                list.add(notification)
            }
            callback(list)
        }.addOnFailureListener {
            errorCallback(it)
        }
    }

    fun add(notification:Notification, callback:()->Unit, errorCallback:(Throwable?)->Unit) {
        val ref = doc().document()
        notification.id = ref.id
        ref.set(notification).addOnSuccessListener {
            callback()
        }.addOnFailureListener {
            errorCallback(it)
        }
    }

    fun update(notification: Notification, callback:()->Unit, errorCallback:(Throwable?)->Unit) {
        doc().document(notification.id!!).set(notification).addOnSuccessListener {
            callback()
        }.addOnFailureListener {
            errorCallback(it)
        }
    }

    fun remove(notification: Notification, callback:()->Unit, errorCallback:(Throwable?)->Unit) {
        doc().document(notification.id!!).delete().addOnSuccessListener {
            callback()
        }.addOnFailureListener {
            errorCallback(it)
        }
    }
}