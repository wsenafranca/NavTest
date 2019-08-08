package washington.franca.com.navtest.fragment


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.preference.*
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*

import washington.franca.com.navtest.R
import washington.franca.com.navtest.viewmodel.UserViewModel

private const val RC_GOOGLE_SIGN_IN = 9002

class SettingsFragment : BaseFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val root = arguments?.getString("root")
        val fragment = when(root) {
            "account"->{
                AccountPreferenceFragment()
            }else -> {
                PreferenceFragment()
            }
        }
        fragment.arguments = Bundle().apply {
            putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, root)
        }

        childFragmentManager.beginTransaction().replace(R.id.fragment_layout, fragment).commit()
    }

    override fun getToolBar(): Toolbar? {
        return view?.findViewById<View>(R.id.appbar)?.findViewById(R.id.toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
    }

    class AccountPreferenceFragment : PreferenceFragmentCompat() {
        private lateinit var userViewModel: UserViewModel
        private lateinit var facebookLoginManger: LoginManager
        private lateinit var facebookCallback: CallbackManager
        private lateinit var googleCallback: GoogleCallback

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.app_preferences, rootKey)
            setupIconColor()
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            facebookCallback = CallbackManager.Factory.create()
            facebookLoginManger = LoginManager.getInstance()
            googleCallback = GoogleCallback()

            userViewModel = ViewModelProviders.of(requireActivity()).get(UserViewModel::class.java)
            userViewModel.user.observe(viewLifecycleOwner, Observer {user->
                findPreference<Preference>("delete_account")?.apply {
                    setOnPreferenceClickListener {
                        userViewModel.delete {
                            handleReauthenticate(user) {
                                userViewModel.delete {  }
                            }
                        }
                        return@setOnPreferenceClickListener true
                    }
                }

                val passwordPref = findPreference<Preference>("change_password")?.apply {
                    summary = getString(R.string.settings_preference_account_link_state_disabled)
                    setIcon(R.drawable.ic_menu_password_disabled)
                    setOnPreferenceClickListener {
                        handleLink(user, "update.email")
                        return@setOnPreferenceClickListener true
                    }
                }
                val googlePref = findPreference<Preference>("link_google")?.apply {
                    summary = getString(R.string.settings_preference_account_link_state_disconnected)
                    setIcon(R.drawable.ic_menu_google_disconnected)
                    setOnPreferenceClickListener {
                        summary = "Connecting..."
                        handleLink(user, GoogleAuthProvider.PROVIDER_ID)
                        return@setOnPreferenceClickListener true
                    }
                }
                val facebookPref = findPreference<Preference>("link_facebook")?.apply {
                    summary = getString(R.string.settings_preference_account_link_state_disconnected)
                    setIcon(R.drawable.ic_menu_facebook_disconnected)
                    setOnPreferenceClickListener {
                        summary = "Connecting..."
                        handleLink(user, FacebookAuthProvider.PROVIDER_ID)
                        return@setOnPreferenceClickListener true
                    }
                }
                for(profile in user.providerData) {
                    when(profile.providerId) {
                        GoogleAuthProvider.PROVIDER_ID-> {
                            googlePref?.apply {
                                summary = getString(R.string.settings_preference_account_link_state_connected)
                                setIcon(R.drawable.ic_menu_google_connected)
                                setOnPreferenceClickListener {
                                    summary = "Disconnecting..."
                                    userViewModel.unlinkProvider(GoogleAuthProvider.PROVIDER_ID)
                                    return@setOnPreferenceClickListener true
                                }
                            }
                        }
                        FacebookAuthProvider.PROVIDER_ID-> {
                            facebookPref?.apply {
                                summary = getString(R.string.settings_preference_account_link_state_connected)
                                setIcon(R.drawable.ic_menu_facebook_connected)
                                setOnPreferenceClickListener {
                                    summary = "Disconnecting..."
                                    userViewModel.unlinkProvider(FacebookAuthProvider.PROVIDER_ID)
                                    return@setOnPreferenceClickListener true
                                }
                            }
                        }
                        EmailAuthProvider.PROVIDER_ID-> {
                            passwordPref?.apply {
                                summary = getString(R.string.settings_preference_account_link_state_enabled)
                                setIcon(R.drawable.ic_menu_password_enabled)
                                val typedValue = TypedValue()
                                context.theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)
                                val color = typedValue.data
                                DrawableCompat.setTint(icon, color)

                                setOnPreferenceClickListener {
                                    handleLink(user, "update.email")
                                    return@setOnPreferenceClickListener true
                                }
                            }
                        }
                    }
                }
            })
        }

        private fun handleLink(user:FirebaseUser, providerId:String) {
            when(providerId) {
                GoogleAuthProvider.PROVIDER_ID->{
                    val signInIntent = userViewModel.googleSignInClient().signInIntent
                    googleCallback.callback = {
                        userViewModel.linkWithCredential(GoogleAuthProvider.getCredential(it.idToken, null)) {
                            handleReauthenticate(user) {currentUser->
                                handleLink(currentUser, providerId)
                            }
                        }
                    }
                    startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
                }
                "update.email"->{
                    val editText = EditText(context)
                    AlertDialog.Builder(context!!)
                        .setTitle("Insert new password")
                        .setView(editText)
                        .setPositiveButton(android.R.string.ok) {d, _ ->
                            d.dismiss()
                            //val email = user.email
                            val password = editText.editableText.toString()
                            userViewModel.updatePassword(password) {
                                handleReauthenticate(user) {currentUser->
                                    handleLink(currentUser, providerId)
                                }
                            }
                        }
                        .setNeutralButton(android.R.string.cancel) {d, _ ->
                            d.dismiss()
                        }
                        .show()
                }
                FacebookAuthProvider.PROVIDER_ID->{
                    facebookLoginManger.registerCallback(facebookCallback, object: FacebookCallback<LoginResult> {
                        override fun onSuccess(result: LoginResult?) {
                            try {
                                userViewModel.linkWithCredential(FacebookAuthProvider.getCredential(result!!.accessToken.token)) {
                                    handleReauthenticate(user) {currentUser->
                                        handleLink(currentUser, providerId)
                                    }
                                }
                            }catch (e:Exception) {
                                userViewModel.showErrorMessage(e)
                            }
                        }

                        override fun onCancel() {}

                        override fun onError(error: FacebookException?) {
                            userViewModel.showErrorMessage(error)
                        }
                    })
                    facebookLoginManger.logIn(this@AccountPreferenceFragment, ArrayList<String>().apply{
                        add("email")
                        add("public_profile")
                    })
                }
            }
        }

        private fun handleReauthenticate(user:FirebaseUser, callback:(user:FirebaseUser)->Unit) {
            for(profile in user.providerData) {
                when(profile.providerId) {
                    GoogleAuthProvider.PROVIDER_ID->{
                        val signInIntent = userViewModel.googleSignInClient().signInIntent
                        googleCallback.callback = {
                            userViewModel.reauthenticate(GoogleAuthProvider.getCredential(it.idToken, null)) {currentUser->
                                callback(currentUser)
                            }
                        }
                        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
                        return
                    }
                    EmailAuthProvider.PROVIDER_ID->{

                    }
                    FacebookAuthProvider.PROVIDER_ID->{
                        facebookLoginManger.registerCallback(facebookCallback, object: FacebookCallback<LoginResult> {
                            override fun onSuccess(result: LoginResult?) {
                                try {
                                    userViewModel.reauthenticate(FacebookAuthProvider.getCredential(result!!.accessToken.token)) {currentUser->
                                        callback(currentUser)
                                    }
                                }catch (e:Exception) {
                                    userViewModel.showErrorMessage(e)
                                }
                            }

                            override fun onCancel() {}

                            override fun onError(error: FacebookException?) {
                                userViewModel.showErrorMessage(error)
                            }
                        })
                        facebookLoginManger.logIn(this@AccountPreferenceFragment, ArrayList<String>().apply{
                            add("email")
                            add("public_profile")
                        })
                        return
                    }
                }
            }
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            when(requestCode) {
                RC_GOOGLE_SIGN_IN-> {
                    if(resultCode == Activity.RESULT_OK) {
                        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                        try {
                            googleCallback.callback?.invoke(task.getResult(ApiException::class.java)!!)
                            googleCallback.callback = null

                        } catch (e: ApiException) {
                            // Google Sign In failed, update UI appropriately
                            userViewModel.showErrorMessage(e)
                        }
                    }
                }
                else->{
                    facebookCallback.onActivityResult(requestCode, resultCode, data)
                }
            }
        }

        class GoogleCallback {
            var callback:((GoogleSignInAccount)->Unit)? = null
        }
    }

    class PreferenceFragment : PreferenceFragmentCompat() {
        private lateinit var userViewModel: UserViewModel
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.app_preferences, rootKey)
            setupIconColor()

            findPreference<SwitchPreferenceCompat>("dark_theme")?.setOnPreferenceChangeListener { _, newValue ->
                when (newValue) {
                    true -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    false -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
                return@setOnPreferenceChangeListener true
            }
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            userViewModel = ViewModelProviders.of(requireActivity()).get(UserViewModel::class.java)
            userViewModel.user.observe(viewLifecycleOwner, Observer {user->
                findPreference<Preference>("user")?.apply {
                    title = user.displayName
                    summary = user.email
                }
            })
            userViewModel.profilePhoto.observe(viewLifecycleOwner, Observer {
                findPreference<Preference>("user")?.apply {
                    icon = RoundedBitmapDrawableFactory.create(resources, it)
                }
            })
        }

        override fun onNavigateToScreen(preferenceScreen: PreferenceScreen?) {
            super.onNavigateToScreen(preferenceScreen)

            findNavController().navigate(SettingsFragmentDirections.actionGlobalToDestSubSettings(
                preferenceScreen?.title.toString(),
                preferenceScreen?.key))
        }
    }
}

fun PreferenceFragmentCompat.setupIconColor() {
    val typedValue = TypedValue()
    context?.let {context->
        context.theme.resolveAttribute(R.attr.colorOnSurface, typedValue, true)
        val color = typedValue.data
        tintIcons(preferenceScreen, color)
    }
}

fun PreferenceFragmentCompat.tintIcons(preference:Preference, color:Int) {
    preference.icon?.let { icon->
        DrawableCompat.setTint(icon, color)
    }
    if(preference is PreferenceGroup) {
        for(i in 0 until preference.preferenceCount) {
            tintIcons(preference.getPreference(i), color)
        }
    }
}