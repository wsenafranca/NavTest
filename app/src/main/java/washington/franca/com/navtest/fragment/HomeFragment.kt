package washington.franca.com.navtest.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_home.*

import washington.franca.com.navtest.R
import washington.franca.com.navtest.databinding.FragmentHomeBinding
import washington.franca.com.navtest.viewmodel.UserViewModel

class HomeFragment : BaseFragment() {
    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        userViewModel = UserViewModel.create(requireActivity())
        userViewModel.profilePhoto.observe(viewLifecycleOwner, Observer {
            profile_photo.setImageDrawable(RoundedBitmapDrawableFactory.create(resources, it))
        })
        val binding:FragmentHomeBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        binding.userViewModel = userViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun getToolBar(): Toolbar? {
        return appbar.findViewById(R.id.toolbar)
    }
}
