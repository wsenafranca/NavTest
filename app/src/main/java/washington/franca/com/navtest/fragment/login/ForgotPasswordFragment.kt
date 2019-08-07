package washington.franca.com.navtest.fragment.login


import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_forgot_password.*

import washington.franca.com.navtest.R
import washington.franca.com.navtest.util.SoftKeyboard
import washington.franca.com.navtest.viewmodel.UserViewModel

class ForgotPasswordFragment : BaseLoginFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_forgot_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            email_edit_text.setText(it.getString("email"))
        }

        userViewModel = UserViewModel.create(requireActivity())

        reset_password_button.setOnClickListener {
            SoftKeyboard.hide(activity)
            showProgress(true, "Connecting...")
            userViewModel.sendPasswordResetEmail(email_edit_text.editableText.toString()) {
                context?.let {
                    Toast.makeText(it, R.string.login_forgot_password_check_your_email_message, Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack(R.id.dest_sign_in, false)
                }
            }
        }

        email_edit_text.requestFocus()
    }
}
