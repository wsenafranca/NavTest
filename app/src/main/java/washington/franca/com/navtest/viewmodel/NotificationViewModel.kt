package washington.franca.com.navtest.viewmodel

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import washington.franca.com.navtest.model.Notification
import washington.franca.com.navtest.repository.NotificationRepository

class NotificationViewModel : ViewModel() {
    companion object {
        fun create(fragmentActivity: FragmentActivity) : NotificationViewModel {
            return ViewModelProviders.of(fragmentActivity).get(NotificationViewModel::class.java)
        }
    }

    private var repository:NotificationRepository? = null

    val loaded = MutableLiveData<ArrayList<Notification>>()
    val added = MutableLiveData<Notification>()
    val removed = MutableLiveData<Notification>()
    val updated = MutableLiveData<Notification>()
    val error = MutableLiveData<Throwable?>()
    val countUnread = MutableLiveData<Int>()

    init {
        countUnread.value = 0
    }

    fun setUser(user:FirebaseUser?) {
        repository = null
        user?.let {
            repository = NotificationRepository(user.uid)
            countUnread.postValue(0)
        }
    }

    fun load(pageSize:Int, last:Notification?) {
        repository?.load(pageSize, last?.id, {
            var count = 0
            for(not in it) {
                count += if(not.status) 0 else 1
            }
            countUnread.postValue(countUnread.value!! + count)
            loaded.postValue(it)
        }, {
            error.postValue(it)
        })
    }

    fun add(notification:Notification) {
        repository?.add(notification, {
            countUnread.postValue(countUnread.value!! + 1)
            added.postValue(notification)
        }, {
            error.postValue(it)
        })
    }

    fun read(notification:Notification) {
        if(!notification.status) {
            notification.status = true
            repository?.update(notification, {
                updated.postValue(notification)
                countUnread.postValue(countUnread.value!! - 1)
            }, {
                error.postValue(it)
            })
        }
    }

    fun update(notification: Notification) {
        repository?.update(notification, {
            updated.postValue(notification)
        }, {
            error.postValue(it)
        })
    }

    fun remove(notification: Notification) {
        val unread = notification.status
        repository?.remove(notification, {
            if(unread) {
                countUnread.postValue(countUnread.value!! - 1)
            }
            removed.postValue(notification)
        }, {
            error.postValue(it)
        })
    }
}