package washington.franca.com.navtest.repository

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*
import washington.franca.com.navtest.R

class UserRepository(private val context: Context) {
    private val auth = FirebaseAuth.getInstance()
    //private val database = FirebaseDatabase.getInstance().reference
    val googleSignInClient: GoogleSignInClient

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestProfile()
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    fun currentUser(callback:((FirebaseUser)->Unit)?, errorCallback:((Throwable?)->Unit)?=null) {
        try {
            callback?.invoke(auth.currentUser!!)
        }catch (e:Exception) {
            errorCallback?.invoke(e)
        }
    }

    fun signInWithEmail(email:String?, password:String?, callback:((FirebaseUser)->Unit)?=null, errorCallback:((Throwable?)->Unit)?=null) {
        try {
            auth.signInWithEmailAndPassword(email!!, password!!).addOnCompleteListener {
                try {
                    if(it.isSuccessful) {
                        callback?.invoke(auth.currentUser!!)
                    }else {
                        errorCallback?.invoke(it.exception)
                    }
                } catch (e:Exception) {

                    errorCallback?.invoke(e)
                }
            }
        }
        catch (e:Exception) {
            errorCallback?.invoke(e)
        }
    }

    fun signInWithGoogle(account: GoogleSignInAccount?, callback:((FirebaseUser)->Unit)?=null, errorCallback:((Throwable?)->Unit)?=null) {
        try {
            val credential = GoogleAuthProvider.getCredential(account!!.idToken, null)
            auth.signInWithCredential(credential).addOnCompleteListener {
                try {
                    if(it.isSuccessful) {
                        callback?.invoke(auth.currentUser!!)
                    }else {
                        errorCallback?.invoke(it.exception)
                    }
                } catch (e:Exception) {

                    errorCallback?.invoke(e)
                }
            }
        }catch (e:Exception) {
            errorCallback?.invoke(e)
        }
    }

    fun signInWithFacebook(loginResult: LoginResult?, callback: ((FirebaseUser) -> Unit)?=null, errorCallback: ((Throwable?) -> Unit)?) {
        try {
            val token = loginResult!!.accessToken.token
            val credential = FacebookAuthProvider.getCredential(token)
            auth.signInWithCredential(credential).addOnCompleteListener {
                try {
                    if(it.isSuccessful) {
                        callback?.invoke(auth.currentUser!!)
                    }else {
                        errorCallback?.invoke(it.exception)
                    }
                } catch (e:Exception) {

                    errorCallback?.invoke(e)
                }
            }
        }catch (e:Exception) {
            errorCallback?.invoke(e)
        }
    }

    fun signUp(email:String?, password:String?, name:String?, callback:((FirebaseUser)->Unit)?=null, errorCallback:((Throwable?)->Unit)?=null) {
        try {
            auth.createUserWithEmailAndPassword(email!!, password!!).addOnCompleteListener {
                if(it.isSuccessful) {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()
                    updateProfile(profileUpdates, {user->
                        if(user.email == null) {
                            updateEmail(email, callback, errorCallback)
                        } else {
                            callback?.invoke(user)
                        }
                    }, errorCallback)
                } else {
                    errorCallback?.invoke(it.exception)
                }
            }
        }
        catch (e:Exception) {
            errorCallback?.invoke(e)
        }
    }

    fun updateEmail(email: String?, callback: ((FirebaseUser) -> Unit)?=null, errorCallback: ((Throwable?) -> Unit)?=null) {
        try {
            val user = auth.currentUser!!
            if(user.email!! == email) {
                callback?.invoke(user)
                return
            }
            user.updateEmail(email!!).addOnCompleteListener {
                if(it.isSuccessful) {
                    try {
                        callback?.invoke(auth.currentUser!!)
                    }catch (e:Exception) {
                        errorCallback?.invoke(e)
                    }
                }else {
                    errorCallback?.invoke(it.exception)
                }
            }
        }catch (e:Exception) {
            errorCallback?.invoke(e)
        }
    }

    fun updateProfile(profile: UserProfileChangeRequest?, callback: ((FirebaseUser) -> Unit)?=null, errorCallback: ((Throwable?) -> Unit)?=null) {
        try {
            auth.currentUser!!.updateProfile(profile!!).addOnCompleteListener {
                try {
                    callback?.invoke(auth.currentUser!!)
                } catch (e:Exception) {
                    errorCallback?.invoke(e)
                }
            }
        }
        catch (e:Exception) {
            errorCallback?.invoke(e)
        }
    }

    fun updatePassword(password: String?, callback:(()->Unit)?=null, errorCallback:((Throwable?)->Unit)?=null) {
        try {
            auth.currentUser!!.updatePassword(password!!).addOnCompleteListener {
                if(it.isSuccessful) {
                    callback?.invoke()
                }else {
                    errorCallback?.invoke(it.exception)
                }
            }
        }catch (e:Exception) {
            errorCallback?.invoke(e)
        }
    }

    fun sendPasswordResetEmail(email: String?, callback: (() -> Unit)?, errorCallback: ((Throwable?) -> Unit)?) {
        try {
            auth.sendPasswordResetEmail(email!!).addOnCompleteListener {
                if(it.isSuccessful) {
                    callback?.invoke()
                } else {
                    errorCallback?.invoke(it.exception)
                }
            }
        }catch (e:Exception) {
            errorCallback?.invoke(e)
        }
    }

    fun signOut(callback:(()->Unit)?=null, errorCallback:((Throwable?)->Unit)?=null) {
        try {
            auth.currentUser?.let { user->
                for(profile in user.providerData) {
                    when(profile.providerId) {
                        GoogleAuthProvider.PROVIDER_ID -> {
                            googleSignInClient.signOut()
                        }
                        FacebookAuthProvider.PROVIDER_ID -> {
                            LoginManager.getInstance().logOut()
                        }
                    }
                }
            }
            auth.signOut()
            callback?.invoke()
        }
        catch (e:Exception) {
            errorCallback?.invoke(e)
        }
    }

    fun loadProfilePhoto(callback:((Bitmap?)->Unit)?, errorCallback: ((Throwable?) -> Unit)?) {
        try {
            Glide.with(context)
                .asBitmap()
                .signature(ObjectKey(auth.currentUser!!.uid))
                .load(auth.currentUser!!.photoUrl)
                .apply(RequestOptions.circleCropTransform())
                .addListener(object:RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        errorCallback?.invoke(e)
                        return true
                    }

                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        try {
                            callback?.invoke(resource)
                        }catch (e:Exception) {
                            errorCallback?.invoke(e)
                        }
                        return true
                    }
                })
                .preload(128, 128)
        }
        catch (e:Exception) {
            errorCallback?.invoke(e)
        }
    }

    fun reauthenticate(credential: AuthCredential, callback: ((FirebaseUser) -> Unit)?, errorCallback: ((Throwable?) -> Unit)?) {
        try {
            auth.currentUser!!.reauthenticate(credential).addOnCompleteListener {
                if(it.isSuccessful) {
                    callback?.invoke(auth.currentUser!!)
                } else {
                    errorCallback?.invoke(it.exception)
                }
            }
        }catch (e:Exception) {
            errorCallback?.invoke(e)
        }
    }

    fun linkWithCredential(credential: AuthCredential, callback:(()->Unit)?=null, errorCallback:((Throwable?)->Unit)?=null) {
        try {
            val user = auth.currentUser!!
            val oldName = user.displayName
            val oldPhotoUrl = user.photoUrl
            user.linkWithCredential(credential).addOnCompleteListener {
                try {
                    if(it.isSuccessful) {
                        user.reload().addOnCompleteListener {task->
                            if (task.isSuccessful) {
                                callback?.invoke()
                            } else {
                                errorCallback?.invoke(task.exception)
                            }
                        }

                        /*
                        val newUser = it.result!!.user
                        val builder = UserProfileChangeRequest.Builder()
                        var shouldUpdate = false
                        if(oldName == null) {
                            builder.setDisplayName(newUser.displayName)
                            shouldUpdate = true
                        }
                        if(oldPhotoUrl == null) {
                            builder.setPhotoUri(newUser.photoUrl)
                            shouldUpdate = true
                        }
                        if(shouldUpdate) {
                            updateProfile(builder.build(), {
                                callback?.invoke()
                            }, errorCallback)
                        }else {
                            callback?.invoke()
                        }
                        */
                    }else {
                        errorCallback?.invoke(it.exception)
                    }
                } catch (e:Exception) {
                    errorCallback?.invoke(e)
                }
            }
        }
        catch (e:Exception) {
            errorCallback?.invoke(e)
        }
    }

    fun unlinkProvider(providerId:String, callback: (() -> Unit)?, errorCallback: ((Throwable?) -> Unit)?) {
        try {
            val email = auth.currentUser!!.email!!
            auth.currentUser!!.unlink(providerId).addOnCompleteListener {
                when(providerId) {
                    GoogleAuthProvider.PROVIDER_ID -> {
                        googleSignInClient.signOut()
                    }
                    FacebookAuthProvider.PROVIDER_ID -> {
                        LoginManager.getInstance().logOut()
                    }
                }
                updateEmail(email, {
                   callback?.invoke()
               }, errorCallback)
            }
        }catch (e:Exception) {
            errorCallback?.invoke(e)
        }
    }

    fun delete(callback: (() -> Unit)?, errorCallback: ((Throwable?) -> Unit)?) {
        try {
            val user = auth.currentUser!!
            user.delete().addOnCompleteListener {
                if(it.isSuccessful) {
                    callback?.invoke()
                } else {
                    errorCallback?.invoke(it.exception)
                }
            }
        }catch (e:Exception) {
            errorCallback?.invoke(e)
        }
    }
}