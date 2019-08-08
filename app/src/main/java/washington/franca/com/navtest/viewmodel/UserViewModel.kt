package washington.franca.com.navtest.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.databinding.ObservableField
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.*
import washington.franca.com.navtest.api.LoginManager
import washington.franca.com.navtest.repository.UserRepository
import washington.franca.com.navtest.util.Event

class UserViewModel(application: Application) : BaseViewModel(application) {
    companion object {
        fun create(fragmentActivity: FragmentActivity) : UserViewModel {
            return ViewModelProviders.of(fragmentActivity).get(UserViewModel::class.java)
        }
    }

    enum class AuthState {
        UNKNOWN,
        UNAUTHENTICATED,
        SIGNING_IN_INPUT_EMAIL,
        SIGNING_IN_INPUT_PASSWORD,
        SIGNING_IN_FORGOT_PASSWORD,
        SIGNING_UP,
        AUTHENTICATED
    }

    private val repository = UserRepository(application)
    private val manager:LoginManager = LoginManager()

    val authState = MutableLiveData<Event<AuthState>>()

    private val _user = MutableLiveData<FirebaseUser>()
    val user:LiveData<FirebaseUser> = _user

    private val _profilePhoto = MutableLiveData<Bitmap?>()
    val profilePhoto:LiveData<Bitmap?> = _profilePhoto

    var email:String? = null
    var isNewUser = false

    init {
        authState.value = Event(AuthState.UNKNOWN)
        _profilePhoto.value = null
    }

    fun googleSignInClient():GoogleSignInClient = repository.googleSignInClient

    fun signInWithEmail() {
        email = null
        isNewUser = false
        authState.postValue(Event(AuthState.SIGNING_IN_INPUT_EMAIL))
    }

    fun verifyEmail(email:String?) {
        repository.verifyEmail(email, {
            this.email = email
            when {
                it.contains(EmailAuthProvider.PROVIDER_ID) -> {
                    isNewUser = false
                    authState.postValue(Event(AuthState.SIGNING_IN_INPUT_PASSWORD))
                }
                it.isNotEmpty() -> {
                    isNewUser = true
                    authState.postValue(Event(AuthState.SIGNING_IN_INPUT_PASSWORD))
                }
                else -> {
                    authState.postValue(Event(AuthState.SIGNING_UP))
                }
            }
        }, {
            showErrorMessage(it)
        })
    }

    fun signInWithEmail(email:String?, password: String?) {
        manager.signInWithEmail(email, password, object:LoginManager.Callback{
            override fun onSuccess(user: FirebaseUser) {
                postCurrentUser()
            }

            override fun onCancel() {
            }

            override fun onError(e: Throwable?) {
                showErrorMessage(e)
            }

            override fun onNeedReauthenticate() {
            }
        })
    }

    fun createUser(email: String?, password: String?, name:String?) {
        manager.createUser(email, password, name, object :LoginManager.Callback {
            override fun onSuccess(user: FirebaseUser) {
                postCurrentUser()
            }

            override fun onCancel() {
            }

            override fun onError(e: Throwable?) {
                showErrorMessage(e)
            }

            override fun onNeedReauthenticate() {
            }
        })
    }

    private fun postCurrentUser() {
        repository.currentUser({
            authState.postValue(Event(AuthState.AUTHENTICATED))
            showMessage("Logged in")
            updateUser()
        }, {
            authState.postValue(Event(AuthState.UNAUTHENTICATED))
            showErrorMessage(it)
        })
    }

    private fun updateUser() {
        repository.currentUser({
            _user.postValue(it)
            loadProfilePhoto()
        }, {
            authState.postValue(Event(AuthState.UNAUTHENTICATED))
            showErrorMessage(it)
        })
    }

    fun verifyCurrentAccount() = postCurrentUser()

    fun signInWithGoogle(account: GoogleSignInAccount?) {
        repository.signInWithGoogle(account, {
            postCurrentUser()
        }, {
            showErrorMessage(it)
        })
    }

    fun signInWithFacebook(loginResult: LoginResult?) {
        repository.signInWithFacebook(loginResult, {
            postCurrentUser()
        }, {
            showErrorMessage(it)
        })
    }

    fun sendPasswordResetEmail(email: String?, callback:()->Unit) {
        repository.sendPasswordResetEmail(email, callback, {
            showErrorMessage(it)
        })
    }

    fun updatePassword(password: String?, needReauthenticateCallback: (() -> Unit)?) {
        try {
            repository.updatePassword(password, {
                updateUser()
            }, {
                if(it is FirebaseAuthRecentLoginRequiredException) {
                    needReauthenticateCallback?.invoke()
                } else {
                    showErrorMessage(it)
                }
            })
        }catch (e:Exception) {
            showErrorMessage(e)
        }
    }

    fun linkWithCredential(auth: AuthCredential, needReauthenticateCallback: (() -> Unit)?) {
        try {
            repository.linkWithCredential(auth, {
                updateUser()
            }, {
                if(it is FirebaseAuthRecentLoginRequiredException) {
                    needReauthenticateCallback?.invoke()
                } else {
                    showErrorMessage(it)
                }
            })
        }catch (e:Exception) {
            showErrorMessage(e)
        }
    }

    fun reauthenticate(auth:AuthCredential, callback: ((FirebaseUser) -> Unit)?) {
        try {
            repository.reauthenticate(auth, callback, {
                showErrorMessage(it)
            })
        }catch (e:Exception) {
            showErrorMessage(e)
        }
    }

    fun unlinkProvider(providerId:String) {
        repository.unlinkProvider(providerId, {
            updateUser()
        }, {
            showErrorMessage(it)
        })
    }

    fun signOut() {
        repository.signOut( {
            authState.postValue(Event(AuthState.UNAUTHENTICATED))
            _profilePhoto.postValue(null)
        }, {
            showErrorMessage(it)
        })
    }

    fun delete(callback: (() -> Unit)?) {
        repository.delete({
            authState.postValue(Event(AuthState.UNAUTHENTICATED))
            _profilePhoto.postValue(null)
        }, {
            if(it is FirebaseAuthRecentLoginRequiredException) {
                callback?.invoke()
            } else {
                showErrorMessage(it)
            }
        })
    }

    private fun loadProfilePhoto() {
        repository.loadProfilePhoto({
            _profilePhoto.postValue(it)
        }, {
            showErrorMessage(it)
        })
    }
}