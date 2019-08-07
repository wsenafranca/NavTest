package washington.franca.com.navtest.fragment.login

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import washington.franca.com.navtest.R
import washington.franca.com.navtest.fragment.BaseFragment
import washington.franca.com.navtest.viewmodel.UserViewModel

open class BaseLoginFragment : BaseFragment() {
    open lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userViewModel = UserViewModel.create(requireActivity())
        userViewModel.error.observe(viewLifecycleOwner, Observer {
            showProgress(false)
        })
    }

    open fun openPrivacyPolicy() {
        val url = Uri.parse(getString(R.string.login_policy_privacy_link))
        val intent = Intent(Intent.ACTION_VIEW, url)
        context?.packageManager?.let {
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
    }

    override fun isBottomNavigationVisible():Boolean {
        return false
    }

    override fun getToolBar(): Toolbar? {
        return view?.findViewById<View>(R.id.appbar)?.findViewById(R.id.toolbar)
    }
}