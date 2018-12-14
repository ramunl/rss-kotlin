package ru.rian.dynamics.ui

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.onesignal.OneSignal
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.drawer_menu_item_layout.view.*
import ru.rian.dynamics.BuildConfig
import ru.rian.dynamics.InitApp
import ru.rian.dynamics.R
import ru.rian.dynamics.R.string.*
import ru.rian.dynamics.SchedulerProvider
import ru.rian.dynamics.di.component.DaggerActivityComponent
import ru.rian.dynamics.di.model.ActivityModule
import ru.rian.dynamics.retrofit.model.HSResult
import ru.rian.dynamics.utils.PLAYER_ID
import ru.rian.dynamics.utils.PreferenceHelper
import ru.rian.dynamics.utils.PreferenceHelper.get
import ru.rian.dynamics.utils.PreferenceHelper.prefs
import ru.rian.dynamics.utils.PreferenceHelper.putHStoPrefs
import ru.rian.dynamics.utils.PreferenceHelper.set
import ru.rian.dynamics.utils.TRENDING
import java.util.logging.Logger
import javax.inject.Inject

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    @Inject
    lateinit var mainViewModel: MainViewModel
    private lateinit var compositeDisposable: CompositeDisposable

    companion object {
        val Log = Logger.getLogger(MainActivity::class.java.name)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        compositeDisposable = CompositeDisposable()
        val activityComponent = DaggerActivityComponent
            .builder()
            .appComponent(InitApp.get(this).component())
            .activityModule(ActivityModule(SchedulerProvider()))
            .build()
        activityComponent.inject(this)

        val playerId: String? = prefs()[PLAYER_ID]
        if (TextUtils.isEmpty(playerId)) {
            OneSignal.idsAvailable { userId, _ ->
                prefs()[PLAYER_ID] = userId
                requestHS()
            }
        } else {
            requestHS()
        }


        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)

        window.setBackgroundDrawableResource(R.color.transparent)
    }

    private fun addMenuItems(result: HSResult?) {
        navView.inflateMenu(R.menu.nav_drawer_stub)
        var isToken = mainViewModel.isTokenPresented()
        addMenuItem(
            if (isToken) getString(terminal_title) else result?.application,
            R.drawable.ic_menu_ddn,
            R.id.nav_news
        )
        if (isToken) {
            addMenuItem(stories_tab_title, R.drawable.ic_menu_story, R.id.nav_story)
        } else {
            addMenuItem(terminal_title, R.drawable.ic_menu_terminal, R.id.nav_terminal)
        }
        addMenuItem(tapes_tab_title, R.drawable.ic_menu_my_feeds, R.id.nav_tapes)


        addMenuItem(settings_notification_title, R.drawable.ic_menu_notifications, R.id.nav_notifications)
        addMenuItem(settings_events_title, R.drawable.ic_menu_events, R.id.nav_events)
        addMenuItem(settings_about_title, R.drawable.ic_menu_about, R.id.nav_about)
        if (isToken) {
            addMenuItem(logout, R.drawable.ic_menu_exit, R.id.nav_terminal_logout)
        }
        if (BuildConfig.FLAVOR.equals(TRENDING)) {
            addMenuItem(choose_lang_title, R.drawable.ic_menu_language, R.id.nav_lang)
        }
    }

    private fun addMenuItem(title: Int, iconResId: Int, itemId: Int) {
        addMenuItem(getString(title), iconResId, itemId)
    }

    private fun addMenuItem(title: String?, iconResId: Int, itemId: Int) {
        val menuItem = navView.menu.add(0, itemId, 0, null)
        var menuItemActionView = View.inflate(this, R.layout.drawer_menu_item_layout, null)
        menuItemActionView.drawerMenuItemIcon.setImageResource(iconResId)
        menuItemActionView.drawerMenuItemTitle.text = title
        menuItem.actionView = menuItemActionView
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            /* R.id.nav_share -> {

             }
             R.id.nav_send -> {

             }*/
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun requestFeeds() {
        var disposable = mainViewModel.provideFeeds()
            ?.subscribe({ result ->

            }, { e ->
                showError(e)
            })
        compositeDisposable.add(disposable!!)
    }

    private fun showError(e: Throwable) {
        e.printStackTrace()
        Snackbar.make(rootLayout, getString(R.string.connection_error_title), Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.try_again) { requestHS() }
            .setActionTextColor(resources.getColor(R.color.action_color))
            .show()
    }

    private fun requestHS() {
        var disposable = mainViewModel.provideHS()
            ?.subscribe({ result ->
                putHStoPrefs(result)
                addMenuItems(result)
                requestFeeds()
            }, { e ->
                showError(e)
            })
        compositeDisposable.add(disposable!!)
    }


    override fun onResume() {
        super.onResume()
    }


    override fun onPause() {
        super.onPause()
        if (compositeDisposable.size() > 0) {
            compositeDisposable.clear()
        }
    }
}
