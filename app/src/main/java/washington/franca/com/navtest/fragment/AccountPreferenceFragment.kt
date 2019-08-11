package washington.franca.com.navtest.fragment

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.View
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import com.facebook.CallbackManager
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import washington.franca.com.navtest.R
import washington.franca.com.navtest.viewmodel.UserViewModel

class AccountPreferenceFragment : PreferenceFragmentCompat() {
    private lateinit var userViewModel: UserViewModel
    private lateinit var facebookLoginManger: LoginManager
    private lateinit var facebookCallback: CallbackManager
    private lateinit var googleCallback: GoogleCallback

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.app_preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        facebookCallback = CallbackManager.Factory.create()
        facebookLoginManger = LoginManager.getInstance()
        googleCallback = GoogleCallback()

        userViewModel = ViewModelProviders.of(requireActivity()).get(UserViewModel::class.java)
        userViewModel.user.observe(viewLifecycleOwner, Observer {user->
            if(user == null) return@Observer

            findPreference<Preference>("delete_account")?.apply {
                setOnPreferenceClickListener {
                    userViewModel.delete(this@AccountPreferenceFragment)
                    return@setOnPreferenceClickListener true
                }
            }

            val passwordPref = findPreference<Preference>("change_password")?.apply {
                setSummary(R.string.settings_preference_account_link_state_disabled)
                setIcon(R.drawable.ic_menu_password_disabled)
                setOnPreferenceClickListener {
                    userViewModel.updatePassword(this@AccountPreferenceFragment)
                    return@setOnPreferenceClickListener true
                }
            }
            val googlePref = findPreference<Preference>("link_google")?.apply {
                setSummary(R.string.settings_preference_account_link_state_disconnected)
                setIcon(R.drawable.ic_menu_google_disconnected)
                setOnPreferenceClickListener {
                    summary = "Connecting..."
                    userViewModel.linkWithGoogle(this@AccountPreferenceFragment)
                    return@setOnPreferenceClickListener true
                }
            }
            val facebookPref = findPreference<Preference>("link_facebook")?.apply {
                setSummary(R.string.settings_preference_account_link_state_disconnected)
                setIcon(R.drawable.ic_menu_facebook_disconnected)
                setOnPreferenceClickListener {
                    summary = "Connecting..."
                    userViewModel.linkWithFacebook(this@AccountPreferenceFragment)
                    return@setOnPreferenceClickListener true
                }
            }
            for(profile in user.providerData) {
                when(profile.providerId) {
                    GoogleAuthProvider.PROVIDER_ID-> {
                        googlePref?.apply {
                            setSummary(R.string.settings_preference_account_link_state_connected)
                            setIcon(R.drawable.ic_menu_google_connected)
                            setOnPreferenceClickListener {
                                summary = "Disconnecting..."
                                userViewModel.unlinkWithGoogle()
                                return@setOnPreferenceClickListener true
                            }
                        }
                    }
                    FacebookAuthProvider.PROVIDER_ID-> {
                        facebookPref?.apply {
                            setSummary(R.string.settings_preference_account_link_state_connected)
                            setIcon(R.drawable.ic_menu_facebook_connected)
                            setOnPreferenceClickListener {
                                summary = "Disconnecting..."
                                userViewModel.unlinkWithFacebook()
                                return@setOnPreferenceClickListener true
                            }
                        }
                    }
                    EmailAuthProvider.PROVIDER_ID-> {
                        passwordPref?.apply {
                            setSummary(R.string.settings_preference_account_link_state_enabled)
                            setIcon(R.drawable.ic_menu_password_enabled)
                            val typedValue = TypedValue()
                            context.theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)
                            val color = typedValue.data
                            DrawableCompat.setTint(icon, color)

                            setOnPreferenceClickListener {
                                userViewModel.updatePassword(this@AccountPreferenceFragment)
                                return@setOnPreferenceClickListener true
                            }
                        }
                    }
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        userViewModel.onActivityResult(requestCode, resultCode, data)
    }

    private fun setupIconColor() {
        val typedValue = TypedValue()
        context?.let {context->
            context.theme.resolveAttribute(R.attr.colorOnSurface, typedValue, true)
            val color = typedValue.data
            tintIcons(preferenceScreen, color)
        }
    }

    private fun tintIcons(preference:Preference, color:Int) {
        preference.icon?.let { icon->
            DrawableCompat.setTint(icon, color)
        }
        if(preference is PreferenceGroup) {
            for(i in 0 until preference.preferenceCount) {
                tintIcons(preference.getPreference(i), color)
            }
        }
    }

    class GoogleCallback {
        var callback:((GoogleSignInAccount)->Unit)? = null
    }
}