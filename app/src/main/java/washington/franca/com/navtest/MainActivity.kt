package washington.franca.com.navtest

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.pd.chocobar.ChocoBar

import kotlinx.android.synthetic.main.activity_main.*
import washington.franca.com.navtest.databinding.ActivityMainBinding
import washington.franca.com.navtest.fragment.login.SignInFragmentDirections
import washington.franca.com.navtest.util.EventObserver
import washington.franca.com.navtest.util.RootNavigation
import washington.franca.com.navtest.viewmodel.UserViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding:ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        navController = findNavController(R.id.nav_host_fragment)
        bottom_nav.setupWithNavController(navController)

        userViewModel = UserViewModel.create(this)
        userViewModel.authState.observe(this, EventObserver {
            when(it) {
                UserViewModel.AuthState.AUTHENTICATED -> navController.navigate(MainNavGraphDirections.actionGlobalToDestHome())
                UserViewModel.AuthState.SIGNING_IN_INPUT_EMAIL -> navController.navigate(LoginNavGraphDirections.actionGlobalToDestSignIn())
                UserViewModel.AuthState.SIGNING_IN_INPUT_PASSWORD -> navController.navigate(SignInFragmentDirections.actionDestSignInToDestSignInPassword())
                UserViewModel.AuthState.SIGNING_IN_FORGOT_PASSWORD -> navController.navigate(LoginNavGraphDirections.actionGlobalToDestForgotPassword())
                UserViewModel.AuthState.SIGNING_UP -> navController.navigate(SignInFragmentDirections.actionDestSignInToDestSignUp())
                UserViewModel.AuthState.UNAUTHENTICATED -> navController.navigate(MainNavGraphDirections.actionGlobalToLoginNavGraph())
                else -> {}
            }
        })

        binding.userViewModel = userViewModel
        binding.lifecycleOwner = this
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when(item?.itemId) {
            R.id.action_logout-> {
                userViewModel.signOut()
                return true
            }
            else->super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, RootNavigation.appBarConfiguration()) || super.onNavigateUp()
    }

    private fun showErrorMessage(e:Throwable?) {
        e?.printStackTrace()
        val message = e?.localizedMessage
        message?.let{
            ChocoBar.builder().setView(root_view)
                .setText(message)
                .setDuration(ChocoBar.LENGTH_INDEFINITE)
                .setActionText(android.R.string.ok)
                .red()
                .show()
        }
    }

    private fun showMessage(message: CharSequence?) {
        message?.let{
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}
