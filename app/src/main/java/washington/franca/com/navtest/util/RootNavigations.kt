package washington.franca.com.navtest.util

import androidx.navigation.ui.AppBarConfiguration
import washington.franca.com.navtest.R

object RootNavigation {
    fun appBarConfiguration(): AppBarConfiguration {
        return AppBarConfiguration.Builder(setOf(
            R.id.dest_login,
            R.id.dest_home,
            R.id.dest_favorites,
            R.id.dest_notifications,
            R.id.dest_settings)).build()
    }
}