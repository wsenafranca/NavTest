package washington.franca.com.navtest.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil

import washington.franca.com.navtest.R
import washington.franca.com.navtest.databinding.FragmentLaunchBinding
import washington.franca.com.navtest.viewmodel.UserViewModel

class LaunchFragment : BaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val accountViewModel = UserViewModel.create(requireActivity())

        val binding:FragmentLaunchBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_launch, container, false)
        binding.userViewModel = accountViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        accountViewModel.verifyCurrentAccount()
        return binding.root
    }

    override fun isBottomNavigationVisible(): Boolean = false

}
