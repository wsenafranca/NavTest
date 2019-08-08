package washington.franca.com.navtest.fragment.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
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
        /*
        sign_up_button.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionDestLoginToDestSignUp(null, null))
        }

        sign_in_with_google_button.setOnClickListener {
            val signInIntent = userViewModel.googleSignInClient().signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        sign_in_with_facebook_button.setOnClickListener {
            facebookLoginManger.logIn(this, ArrayList<String>().apply{
                add("email")
                add("public_profile")
            })
        }

        privacy_policy_button.setOnClickListener {
            openPrivacyPolicy()
        }
        */
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}
