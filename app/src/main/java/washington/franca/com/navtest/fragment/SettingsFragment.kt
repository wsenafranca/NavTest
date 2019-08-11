package washington.franca.com.navtest.fragment

import android.os.Bundle
import android.util.TypedValue
import android.view.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.preference.*

import washington.franca.com.navtest.R
import washington.franca.com.navtest.viewmodel.UserViewModel

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
                    title = user?.displayName
                    summary = user?.email
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
    }
}
