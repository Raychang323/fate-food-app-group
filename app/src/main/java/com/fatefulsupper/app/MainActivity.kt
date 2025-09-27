package com.fatefulsupper.app

import android.Manifest
import android.content.Context
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
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
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
                    Log.d(TAG, "POST_NOTIFICATIONS permission granted by user.")
                    if (sharedPreferences.getBoolean(SetupConstants.KEY_FIRST_TIME_SETUP_COMPLETED, false)) {
                        Log.d(TAG, "POST_NOTIFICATIONS granted, re-evaluating and scheduling notifications.")
                        NotificationScheduler.scheduleNotifications(this)
                    }
                } else {
                    Log.w(TAG, "POST_NOTIFICATIONS permission denied by user.")
                    Toast.makeText(
                        this,
                        getString(R.string.notification_permission_denied_message),
                        Toast.LENGTH_LONG
                    ).show()
                    Log.d(TAG, "POST_NOTIFICATIONS denied, cancelling all scheduled notifications.")
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
            setOf(
                R.id.connoisseurCheckFragment,
            ),
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
                Log.d(TAG, "Toolbar Navigation Click: Drawer Toggle (Hamburger) activated.")
            } else {
                onSupportNavigateUp()
                Log.d(TAG, "Toolbar Navigation Click: Up Arrow (Back) activated.")
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isTopLevelDestination = appBarConfiguration.topLevelDestinations.contains(destination.id)
            if (isTopLevelDestination) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                toggle.isDrawerIndicatorEnabled = true
            } else {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                toggle.isDrawerIndicatorEnabled = false
            }
            toggle.syncState()
            Log.d(TAG, "Destination changed. Drawer indicator enabled: ${toggle.isDrawerIndicatorEnabled}")
        }

        NotificationHelper.createNotificationChannel(this)

        sharedPreferences = getSharedPreferences(SetupConstants.PREFS_NAME, MODE_PRIVATE)
        updateNavHeader()

        if (sharedPreferences.getBoolean(SetupConstants.KEY_FIRST_TIME_SETUP_COMPLETED, false)) {
            Log.d(TAG, "Setup previously completed. Checking permissions for scheduling.")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "POST_NOTIFICATIONS already granted. Scheduling notifications.")
                    NotificationScheduler.scheduleNotifications(this)
                } else {
                    Log.i(TAG, "POST_NOTIFICATIONS not granted. Requesting permission. Notifications will be scheduled if granted.")
                    checkAndRequestNotificationPermission()
                }
            } else {
                Log.d(TAG, "Pre-TIRAMISU. Scheduling notifications.")
                NotificationScheduler.scheduleNotifications(this)
            }
        } else {
            Log.d(TAG, "First time setup not completed. Will launch dialog. Requesting notification permission first.")
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
            Log.d(TAG, "onPostCreate: toggle.syncState() called.")
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
        Log.d(TAG, "onSupportNavigateUp called. Attempting NavController.navigateUp.")
        val navigated = navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
        Log.d(TAG, "navController.navigateUp result: $navigated")
        return navigated
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (::toggle.isInitialized) {
            toggle.onConfigurationChanged(newConfig)
        }
    }

    private fun checkAndRequestNotificationPermission() {
        if (!::sharedPreferences.isInitialized) {
            sharedPreferences = getSharedPreferences(SetupConstants.PREFS_NAME, MODE_PRIVATE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d(TAG, "POST_NOTIFICATIONS permission already granted.")
                    if (sharedPreferences.getBoolean(SetupConstants.KEY_FIRST_TIME_SETUP_COMPLETED, false)) {
                        NotificationScheduler.scheduleNotifications(this)
                    }
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    Log.i(TAG, "Showing rationale for POST_NOTIFICATIONS permission.")
                    Toast.makeText(this, getString(R.string.notification_permission_rationale), Toast.LENGTH_LONG).show()
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    Log.d(TAG, "Requesting POST_NOTIFICATIONS permission for the first time or after previous denial without rationale.")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun checkAndLaunchFirstTimeSetup() {
        if (!::sharedPreferences.isInitialized) {
            sharedPreferences = getSharedPreferences(SetupConstants.PREFS_NAME, MODE_PRIVATE)
        }
        val isSetupCompleted = sharedPreferences.getBoolean(SetupConstants.KEY_FIRST_TIME_SETUP_COMPLETED, false)
        if (!isSetupCompleted) {
            Log.d(TAG, "First time setup not completed. Launching NotificationSettingsDialog.")
            showNotificationSettingsDialog()
        } else {
            Log.d(TAG, "First time setup already completed.")
        }
    }

    private fun updateNavHeader(username: String = "訪客") {
        if (::binding.isInitialized) {
            val headerView = binding.navView.getHeaderView(0)
            val greetingTextView = headerView.findViewById<TextView>(R.id.textView_user_greeting)
            greetingTextView.text = getString(R.string.user_greeting, username)
        } else {
            Log.e(TAG, "updateNavHeader: Binding not initialized!")
        }
    }

    private fun showNotificationSettingsDialog() {
        if (supportFragmentManager.findFragmentByTag(NotificationSettingsDialog.TAG) == null) {
            val dialog = NotificationSettingsDialog.newInstance(isFirstTimeSetup = true)
            dialog.show(supportFragmentManager, NotificationSettingsDialog.TAG)
        }
    }

    private fun showSupperBlacklistDialog() {
        if (supportFragmentManager.findFragmentByTag(SupperBlacklistDialog.TAG) == null) {
            val dialog = SupperBlacklistDialog.newInstance(isFirstTimeSetup = true)
            dialog.show(supportFragmentManager, SupperBlacklistDialog.TAG)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.drawer_menu, menu)
        return true
    }

    // Updated Interface Implementation
    override fun onNotificationSettingsSaved(isFirstTimeSetupContext: Boolean) {
        Log.d(TAG, "Notification settings saved. isFirstTimeSetupContext: $isFirstTimeSetupContext. Rescheduling with scheduleNotifications.")
        NotificationScheduler.scheduleNotifications(this)
        if (isFirstTimeSetupContext) { // Only proceed if it's the first time setup
            if (!::sharedPreferences.isInitialized) {
                sharedPreferences = getSharedPreferences(SetupConstants.PREFS_NAME, MODE_PRIVATE)
            }
            // It seems KEY_FIRST_TIME_SETUP_COMPLETED is marked true in SupperBlacklistDialog
            // when 'Save and Finish' is clicked, or when 'Skip' is clicked there.
            // So, no need to set it here if we are always showing SupperBlacklistDialog next in first-time setup.
            // If SupperBlacklistDialog is skipped, it will handle setting the completed flag.
            Log.d(TAG, "Proceeding to blacklist dialog as part of first-time setup.")
            showSupperBlacklistDialog() // This method already passes isFirstTimeSetup = true
        }
    }

    override fun onNotificationSetupSkipped(isFirstTimeSetupContext: Boolean) {
        Log.d(TAG, "Notification setup skipped. isFirstTimeSetupContext: $isFirstTimeSetupContext. Ensuring notifications reflect this.")
        NotificationScheduler.scheduleNotifications(this) // Ensure scheduler reflects current state (e.g. defaults)
        if (isFirstTimeSetupContext) { // Only proceed if it's the first time setup
             // Similar to onNotificationSettingsSaved, KEY_FIRST_TIME_SETUP_COMPLETED will be handled by SupperBlacklistDialog.
            Log.d(TAG, "Proceeding to blacklist dialog as part of first-time setup (after skipping notifications).")
            showSupperBlacklistDialog() // This method already passes isFirstTimeSetup = true
        }
    }

    override fun onBlacklistSettingsSaved() {
        Log.d(TAG, "Supper blacklist settings saved.")
        // Mark setup as fully completed ONLY if it was part of the first-time flow.
        // SupperBlacklistDialog now handles this internally via its own isFirstTimeSetup state.
    }

    override fun onBlacklistSetupSkipped() {
        Log.d(TAG, "Supper blacklist setup skipped.")
        // Mark setup as fully completed ONLY if it was part of the first-time flow.
        // SupperBlacklistDialog now handles this internally via its own isFirstTimeSetup state.
    }
}
