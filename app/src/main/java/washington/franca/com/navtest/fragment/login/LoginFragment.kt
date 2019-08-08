package washington.franca.com.navtest.fragment.login

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import kotlinx.android.synthetic.main.fragment_login.*
import washington.franca.com.navtest.R
import washington.franca.com.navtest.databinding.FragmentLoginBinding

class LoginFragment : BaseLoginFragment() {
    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.userViewModel = userViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        sign_in_with_facebook_button.setOnClickListener {
            userViewModel.signInWithFacebook(this@LoginFragment)
        }

        sign_in_with_google_button.setOnClickListener {
            userViewModel.signInWithGoogle(this@LoginFragment)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        userViewModel.onActivityResult(requestCode, resultCode, data)
    }
}
