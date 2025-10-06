package com.fatefulsupper.app

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.fatefulsupper.app.databinding.ActivityMainBinding
import com.fatefulsupper.app.ui.dialog.NotificationSettingsDialog
import com.fatefulsupper.app.ui.dialog.SupperBlacklistDialog
import com.fatefulsupper.app.util.NotificationHelper
import com.fatefulsupper.app.util.NotificationScheduler
import com.fatefulsupper.app.util.SetupConstants
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(),
    NotificationSettingsDialog.NotificationDialogListener,
    SupperBlacklistDialog.SupperBlacklistDialogListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var drawerLayout: DrawerLayout

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (!::sharedPreferences.isInitialized) {
                    sharedPreferences = getSharedPreferences(SetupConstants.PREFS_NAME, MODE_PRIVATE)
                }
                if (isGranted) {
                    if (sharedPreferences.getBoolean(SetupConstants.KEY_FIRST_TIME_SETUP_COMPLETED, false)) {
                        NotificationScheduler.scheduleNotifications(this)
                    }
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.notification_permission_denied_message),
                        Toast.LENGTH_LONG
                    ).show()
                    NotificationScheduler.cancelScheduledNotifications(this)
                }
            }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        drawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.connoisseurCheckFragment, R.id.lazyModeFragment),
            drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            binding.appBarMain.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)

        binding.appBarMain.toolbar.setNavigationOnClickListener {
            if (toggle.isDrawerIndicatorEnabled) {
                if (drawerLayout.isDrawerOpen(navView)) {
                    drawerLayout.closeDrawer(navView)
                } else {
                    drawerLayout.openDrawer(navView)
                }
            } else {
                onSupportNavigateUp()
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isTopLevelDestination = appBarConfiguration.topLevelDestinations.contains(destination.id)
            drawerLayout.setDrawerLockMode(if (isTopLevelDestination) DrawerLayout.LOCK_MODE_UNLOCKED else DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            toggle.isDrawerIndicatorEnabled = isTopLevelDestination
            toggle.syncState()
        }

        NotificationHelper.createNotificationChannel(this)

        sharedPreferences = getSharedPreferences(SetupConstants.PREFS_NAME, MODE_PRIVATE)
        updateNavHeader()

        if (sharedPreferences.getBoolean(SetupConstants.KEY_FIRST_TIME_SETUP_COMPLETED, false)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    NotificationScheduler.scheduleNotifications(this)
                } else {
                    checkAndRequestNotificationPermission()
                }
            } else {
                NotificationScheduler.scheduleNotifications(this)
            }
        } else {
            checkAndRequestNotificationPermission()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED &&
                !shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                Handler(Looper.getMainLooper()).postDelayed({
                    checkAndLaunchFirstTimeSetup()
                }, 1000)
            } else {
                checkAndLaunchFirstTimeSetup()
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (::toggle.isInitialized) {
            toggle.syncState()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            return false
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (::toggle.isInitialized) {
            toggle.onConfigurationChanged(newConfig)
        }
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                    if (sharedPreferences.getBoolean(SetupConstants.KEY_FIRST_TIME_SETUP_COMPLETED, false)) {
                        NotificationScheduler.scheduleNotifications(this)
                    }
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    Toast.makeText(this, getString(R.string.notification_permission_rationale), Toast.LENGTH_LONG).show()
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun checkAndLaunchFirstTimeSetup() {
        if (!sharedPreferences.getBoolean(SetupConstants.KEY_FIRST_TIME_SETUP_COMPLETED, false)) {
            showNotificationSettingsDialog()
        }
    }

    private fun updateNavHeader(username: String = "訪客") {
        val headerView = binding.navView.getHeaderView(0)
        val greetingTextView = headerView.findViewById<TextView>(R.id.textView_user_greeting)
        greetingTextView.text = getString(R.string.user_greeting, username)
    }

    private fun showNotificationSettingsDialog() {
        if (supportFragmentManager.findFragmentByTag(NotificationSettingsDialog.TAG) == null) {
            NotificationSettingsDialog.newInstance(isFirstTimeSetup = true).show(supportFragmentManager, NotificationSettingsDialog.TAG)
        }
    }

    private fun showSupperBlacklistDialog() {
        if (supportFragmentManager.findFragmentByTag(SupperBlacklistDialog.TAG) == null) {
            SupperBlacklistDialog.newInstance(isFirstTimeSetup = true).show(supportFragmentManager, SupperBlacklistDialog.TAG)
        }
    }

    override fun onNotificationSettingsSaved(isFirstTimeSetupContext: Boolean) {
        NotificationScheduler.scheduleNotifications(this)
        if (isFirstTimeSetupContext) {
            showSupperBlacklistDialog()
        }
    }

    override fun onNotificationSetupSkipped(isFirstTimeSetupContext: Boolean) {
        NotificationScheduler.scheduleNotifications(this)
        if (isFirstTimeSetupContext) {
            showSupperBlacklistDialog()
        }
    }

    override fun onBlacklistSettingsSaved(blacklistedTypes: Set<String>) {
        // Handled in SettingsFragment
    }

    override fun onBlacklistSetupSkipped() {
        // Handled in SettingsFragment
    }
}