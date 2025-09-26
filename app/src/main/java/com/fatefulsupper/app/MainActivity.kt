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
            setOf(R.id.connoisseurCheckFragment),
            drawerLayout
        )

        // 必須保留：設置 Toolbar 標題和圖標的初始狀態
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

        // ⭐ 關鍵修正：手動設置 Toolbar 導航點擊監聽器以繞過事件衝突
        binding.appBarMain.toolbar.setNavigationOnClickListener {
            // 檢查 toggle 的狀態來判斷當前顯示的是什麼圖標
            if (toggle.isDrawerIndicatorEnabled) {
                // 如果是漢堡圖標，手動開/關抽屜
                if (drawerLayout.isDrawerOpen(navView)) {
                    drawerLayout.closeDrawer(navView)
                } else {
                    drawerLayout.openDrawer(navView)
                }
                Log.d(TAG, "Toolbar Navigation Click: Drawer Toggle (Hamburger) activated.")
            } else {
                // 如果是返回箭頭，執行向上導航
                onSupportNavigateUp()
                Log.d(TAG, "Toolbar Navigation Click: Up Arrow (Back) activated.")
            }
        }

        // 負責切換圖標的邏輯
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isTopLevelDestination = appBarConfiguration.topLevelDestinations.contains(destination.id)

            if (isTopLevelDestination) {
                // 主頁面：顯示漢堡圖標 (≡)
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                toggle.isDrawerIndicatorEnabled = true // 設置為漢堡狀態
            } else {
                // 非主頁面：顯示返回箭頭 (←)
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                toggle.isDrawerIndicatorEnabled = false // 設置為返回狀態
            }

            // 這會觸發圖標的實際切換（無動畫），但確保狀態正確
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

    // 由於我們使用 setNavigationOnClickListener 處理了導航圖標的點擊，
    // onOptionsItemSelected 只需要處理 Toolbar 上的其他選單項目。
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // 忽略導航圖標的點擊，避免干擾手動設置的 setNavigationOnClickListener
        if (item.itemId == android.R.id.home) {
            return false
        }
        return super.onOptionsItemSelected(item)
    }


    // 處理導航返回邏輯
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        Log.d(TAG, "onSupportNavigateUp called. Attempting NavController.navigateUp.")

        // 確保使用帶有 appBarConfiguration 的 navigateUp
        val navigated = navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()

        Log.d(TAG, "navController.navigateUp result: $navigated")

        return navigated
    }

    // --- 輔助方法定義 ---

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

    // --- Interface 實作 ---

    override fun onNotificationSettingsSaved() {
        Log.d(TAG, "Notification settings saved. Rescheduling with scheduleNotifications and proceeding to blacklist.")
        NotificationScheduler.scheduleNotifications(this)
        if (!::sharedPreferences.isInitialized) {
            sharedPreferences = getSharedPreferences(SetupConstants.PREFS_NAME, MODE_PRIVATE)
        }
        sharedPreferences.edit {
            putBoolean(SetupConstants.KEY_FIRST_TIME_SETUP_COMPLETED, true)
        }
        Log.d(TAG, "Marked KEY_FIRST_TIME_SETUP_COMPLETED as true.")
        showSupperBlacklistDialog()
    }

    override fun onNotificationSetupSkipped() {
        Log.d(TAG, "Notification setup skipped. Ensuring notifications reflect this (likely cancelled or default) and proceeding to blacklist.")
        NotificationScheduler.scheduleNotifications(this)
        if (!::sharedPreferences.isInitialized) {
            sharedPreferences = getSharedPreferences(SetupConstants.PREFS_NAME, MODE_PRIVATE)
        }
        sharedPreferences.edit {
            putBoolean(SetupConstants.KEY_FIRST_TIME_SETUP_COMPLETED, true)
        }
        Log.d(TAG, "Marked KEY_FIRST_TIME_SETUP_COMPLETED as true.")
        showSupperBlacklistDialog()
    }

    override fun onBlacklistSettingsSaved() {
        Log.d(TAG, "Supper blacklist settings saved.")
    }

    override fun onBlacklistSetupSkipped() {
        Log.d(TAG, "Supper blacklist setup skipped.")
    }
}
