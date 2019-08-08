package washington.franca.com.navtest.viewmodel

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import com.google.firebase.auth.*
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

    private val repository: UserRepository = UserRepository()

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

    fun signInWithEmail() {
        email = null
        isNewUser = false
        authState.postValue(Event(AuthState.SIGNING_IN_INPUT_EMAIL))
    }

    fun verifyEmail(email:String?) {
        repository.verifyEmail(email, object : UserRepository.Callback<List<String>>(){
            override fun onSuccess(result: List<String>) {
                this@UserViewModel.email = email
                when {
                    result.contains(EmailAuthProvider.PROVIDER_ID) -> {
                        isNewUser = false
                        authState.postValue(Event(AuthState.SIGNING_IN_INPUT_PASSWORD))
                    }
                    result.isNotEmpty() -> {
                        isNewUser = true
                        authState.postValue(Event(AuthState.SIGNING_IN_INPUT_PASSWORD))
                    }
                    else -> {
                        authState.postValue(Event(AuthState.SIGNING_UP))
                    }
                }
            }

            override fun onError(e: Throwable?) {
                showErrorMessage(e)
            }
        })
    }

    fun signInWithEmail(email:String?, password: String?) {
        repository.signInWithEmail(email, password, object: UserRepository.Callback<FirebaseUser>() {
            override fun onSuccess(result: FirebaseUser) {
                postCurrentUser()
            }

            override fun onError(e: Throwable?) {
                showErrorMessage(e)
            }
        })
    }

    fun createUser(email: String?, password: String?, name:String?) {
        repository.createUser(email, password, name, object : UserRepository.Callback<FirebaseUser>() {
            override fun onSuccess(result: FirebaseUser) {
                postCurrentUser()
            }

            override fun onError(e: Throwable?) {
                showErrorMessage(e)
            }
        })
    }

    private fun reauthenticate(fragment: Fragment, providerId: String, onSuccess:()->Unit) {
        when(providerId) {
            EmailAuthProvider.PROVIDER_ID-> {
                repository.reauthenticateWithEmail(fragment, object:UserRepository.Callback<FirebaseUser>(){
                    override fun onSuccess(result: FirebaseUser) {
                        onSuccess()
                    }

                    override fun onError(e: Throwable?) {
                        showErrorMessage(e)
                    }
                })
            }
            GoogleAuthProvider.PROVIDER_ID->{
                 repository.reauthenticateWithGoogle(fragment, object:
                     UserRepository.Callback<FirebaseUser>(){
                     override fun onSuccess(result: FirebaseUser) {
                         onSuccess()
                     }

                     override fun onError(e: Throwable?) {
                         showErrorMessage(e)
                     }
                 })
            }
            FacebookAuthProvider.PROVIDER_ID->{
                repository.reauthenticateWithFacebook(fragment, object:
                    UserRepository.Callback<FirebaseUser>(){
                    override fun onSuccess(result: FirebaseUser) {
                        onSuccess()
                    }

                    override fun onError(e: Throwable?) {
                        showErrorMessage(e)
                    }
                })
            }
        }
    }

    fun linkWithGoogle(fragment:Fragment) {
        repository.linkWithGoogle(fragment, object : UserRepository.Callback<FirebaseUser>() {
            override fun onSuccess(result: FirebaseUser) {
                updateUser()
            }

            override fun onNeedReauthenticate() {
                val provider = repository.getProvider()
                reauthenticate(fragment, provider) {
                    linkWithGoogle(fragment)
                }
            }

            override fun onError(e: Throwable?) {
                e?.printStackTrace()
                showErrorMessage(e)
            }
        })
    }

    fun linkWithFacebook(fragment: Fragment) {
        repository.linkWithFacebook(fragment, object : UserRepository.Callback<FirebaseUser>() {
            override fun onSuccess(result: FirebaseUser) {
                updateUser()
            }

            override fun onNeedReauthenticate() {
                val provider = repository.getProvider()
                reauthenticate(fragment, provider) {
                    linkWithFacebook(fragment)
                }
            }

            override fun onError(e: Throwable?) {
                e?.printStackTrace()
                showErrorMessage(e)
            }
        })
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?):Boolean {
        return repository.onActivityResult(requestCode, resultCode, data)
    }

    private fun postCurrentUser() {
        repository.currentUser(object : UserRepository.Callback<FirebaseUser>(){
            override fun onSuccess(result: FirebaseUser) {
                authState.postValue(Event(AuthState.AUTHENTICATED))
                showMessage("Logged in")
                updateUser()
            }

            override fun onError(e: Throwable?) {
                authState.postValue(Event(AuthState.UNAUTHENTICATED))
                showErrorMessage(e)
            }
        })
    }

    private fun updateUser() {
        repository.currentUser(object : UserRepository.Callback<FirebaseUser>(){
            override fun onSuccess(result: FirebaseUser) {
                _user.postValue(result)
                loadProfilePhoto()
            }

            override fun onError(e: Throwable?) {
                authState.postValue(Event(AuthState.UNAUTHENTICATED))
                showErrorMessage(e)
            }
        })
    }

    fun verifyCurrentAccount() = postCurrentUser()

    fun signInWithGoogle(fragment: Fragment) {
        repository.signInWithGoogle(fragment, object : UserRepository.Callback<FirebaseUser>(){
            override fun onSuccess(result: FirebaseUser) {
                postCurrentUser()
            }

            override fun onError(e: Throwable?) {
                showErrorMessage(e)
            }
        })
    }

    fun signInWithFacebook(fragment: Fragment) {
        repository.signInWithFacebook(fragment, object : UserRepository.Callback<FirebaseUser>(){
            override fun onSuccess(result: FirebaseUser) {
                postCurrentUser()
            }

            override fun onError(e: Throwable?) {
                showErrorMessage(e)
            }
        })
    }

    fun recoverPassword() {
        authState.postValue(Event(AuthState.SIGNING_IN_FORGOT_PASSWORD))
    }

    fun sendPasswordResetEmail(email: String?) {
        repository.sendPasswordResetEmail(email, object : UserRepository.Callback<FirebaseUser>() {
            override fun onSuccess(result: FirebaseUser) {
                authState.postValue(Event(AuthState.SIGNING_IN_INPUT_PASSWORD))
            }

            override fun onError(e: Throwable?) {
                showErrorMessage(e)
            }
        })
    }

    fun updatePassword(fragment: Fragment) {
        repository.updatePassword(fragment, object: UserRepository.Callback<FirebaseUser>() {
            override fun onSuccess(result: FirebaseUser) {
                updateUser()
            }

            override fun onNeedReauthenticate() {
                val provider = repository.getProvider()
                reauthenticate(fragment, provider) {
                    updatePassword(fragment)
                }
            }

            override fun onError(e: Throwable?) {
                e?.printStackTrace()
                showErrorMessage(e)
            }
        })
    }

    fun unlinkWithGoogle() {
        repository.unlinkWithGoogle(object: UserRepository.Callback<FirebaseUser>(){
            override fun onSuccess(result: FirebaseUser) {
                updateUser()
            }

            override fun onError(e: Throwable?) {
                showErrorMessage(e)
            }
        })
    }

    fun unlinkWithFacebook() {
        repository.unlinkWithFacebook(object: UserRepository.Callback<FirebaseUser>(){
            override fun onSuccess(result: FirebaseUser) {
                updateUser()
            }

            override fun onError(e: Throwable?) {
                showErrorMessage(e)
            }
        })
    }

    fun signOut() {
        repository.signOut(object: UserRepository.Callback<Void?>(){
            override fun onSuccess(result: Void?) {
                authState.postValue(Event(AuthState.UNAUTHENTICATED))
                _profilePhoto.postValue(null)
            }

            override fun onError(e: Throwable?) {
                showErrorMessage(e)
            }
        })
    }

    fun delete(fragment: Fragment) {
        repository.deleteUser(object: UserRepository.Callback<Void?>(){
            override fun onSuccess(result: Void?) {
                authState.postValue(Event(AuthState.UNAUTHENTICATED))
                _profilePhoto.postValue(null)
            }

            override fun onNeedReauthenticate() {
                val provider = repository.getProvider()
                reauthenticate(fragment, provider) {
                    delete(fragment)
                }
            }

            override fun onError(e: Throwable?) {
                showErrorMessage(e)
            }
        })
    }

    private fun loadProfilePhoto() {
        repository.loadProfilePhoto(getApplication(), object : UserRepository.Callback<Bitmap?>(){
            override fun onSuccess(result: Bitmap?) {
                _profilePhoto.postValue(result)
            }

            override fun onError(e: Throwable?) {
                showErrorMessage(e)
            }
        })
    }
}