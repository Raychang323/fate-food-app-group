package com.fatefulsupper.app.utils // 或者您的工具程式套件路徑

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.CollapsingToolbarLayout

/**
 * AppCompatActivity 擴充函式，用於設定標準 Toolbar 與 NavController。
 */
fun AppCompatActivity.setupToolbarWithNavigation(
    toolbar: Toolbar,
    navController: NavController,
    appBarConfiguration: AppBarConfiguration
) {
    setSupportActionBar(toolbar)
    toolbar.setupWithNavController(navController, appBarConfiguration)
}

/**
 * Fragment 擴充函式，用於設定帶有 CollapsingToolbarLayout 的場景。
 *
 * @param collapsingToolbar The CollapsingToolbarLayout in the Fragment's layout.
 * @param toolbarWithinCollapsing (Optional) The Toolbar *inside* the CollapsingToolbarLayout,
 *                                if you have one pinned for visual effect when collapsed.
 * @param pageTitle The title to be set on the Activity's Toolbar when this Fragment is active.
 */
fun Fragment.setupCollapsingToolbar(
    collapsingToolbar: CollapsingToolbarLayout,
    toolbarWithinCollapsing: Toolbar? = null, // 對應 XML 中的 dummy_toolbar_in_collapsing_layout
    pageTitle: String?
) {
    collapsingToolbar.isTitleEnabled = false

    (activity as? AppCompatActivity)?.supportActionBar?.title = pageTitle
}

/**
 * Fragment 擴充函式，用於在 Fragment 銷毀其視圖時清除 Activity 的 Toolbar 標題。
 */
fun Fragment.clearActivityToolbarTitleOnDestroy() {
    (activity as? AppCompatActivity)?.supportActionBar?.title = null
}
    