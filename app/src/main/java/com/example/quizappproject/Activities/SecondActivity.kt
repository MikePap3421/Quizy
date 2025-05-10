package com.example.quizappproject.Activities

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import android.content.res.Configuration
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.quizappproject.AppDatabase
import com.example.quizappproject.R
import com.example.quizappproject.fragments.*
import com.google.android.material.color.DynamicColors
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch

class SecondActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var userIconTextView: TextView
    private lateinit var userPointsTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        DynamicColors.applyToActivityIfAvailable(this)
        setContentView(R.layout.activity_second)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        drawerLayout = findViewById(R.id.mainDrawer)
        navView = findViewById(R.id.top_nav_view)

        navView.setNavigationItemSelectedListener { menuItem ->
            navigateToMenuItem(menuItem.itemId)
            true
        }

        drawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        val drawerIcon = toolbar.navigationIcon
        drawerIcon?.let {
            toolbar.navigationIcon = it
            toolbar.post {
                val navIconView = toolbar.findViewById<View>(androidx.appcompat.R.id.action_bar)
                    ?: toolbar.getChildAt(1)
                val orientation = resources.configuration.orientation
                navIconView?.y = if (orientation == Configuration.ORIENTATION_LANDSCAPE) 90f else 140f
                navIconView?.scaleX = 1.8f
                navIconView?.scaleY = 1.8f
            }
        }

        if (savedInstanceState != null) {
            val currentFragment = supportFragmentManager.getFragment(savedInstanceState, "current_fragment")
            currentFragment?.let { loadFragment(it) }
        } else {
            loadFragment(HomeFragment())
        }

        userPointsTextView = findViewById(R.id.userPoints)
        userIconTextView = findViewById(R.id.userIcon)

        userIconTextView.setOnClickListener {
            navigateToMenuItem(R.id.Profileview)
        }

        updateUserInfo()
    }

    private fun navigateToMenuItem(itemId: Int) {
        when (itemId) {
            R.id.HomeView -> loadFragment(HomeFragment())
            R.id.Profileview -> loadFragment(ProfileFragment())
            R.id.statsView -> loadFragment(LeaderboardFragment())
            R.id.chaseView -> {
                val sharedPrefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
                val email = sharedPrefs.getString("USER_EMAIL", null)

                if (email != null) {
                    lifecycleScope.launch {
                        val userDao = AppDatabase.getDatabase(applicationContext).userDao()
                        val user = userDao.getUserByEmail(email)

                        if (user != null && user.points > 0) {
                            supportFragmentManager.beginTransaction()
                                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left)
                                .replace(R.id.fragment_container, ChaseFragment())
                                .addToBackStack(null)
                                .commit()
                        } else {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                    requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
                                } else {
                                    sendChaseLockedNotification()
                                }
                            } else {
                                sendChaseLockedNotification()
                            }

                        }
                    }
                }
            }
            R.id.Aboutview -> loadFragment(AboutFragment())
            R.id.LogOutview -> logoutUser()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun logoutUser() {
        val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear()
            apply()
        }
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    fun updateUserInfo() {
        val sharedPrefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val email = sharedPrefs.getString("USER_EMAIL", null)

        lifecycleScope.launch {
            if (email != null) {
                val userDao = AppDatabase.getDatabase(applicationContext).userDao()
                val user = userDao.getUserByEmail(email)

                user?.let {
                    userIconTextView.text = it.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                    userPointsTextView.text = "Points: ${it.points}"
                }
            } else {
                Toast.makeText(this@SecondActivity, "No email found in session", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment != null) {
            supportFragmentManager.putFragment(outState, "current_fragment", currentFragment)
        }
    }

    fun updateDrawerHeaderName(name: String) {
        val headerView = navView.getHeaderView(0)
        val tvUserName = headerView.findViewById<TextView>(R.id.textView5)
        tvUserName.text = name
    }
    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val name = "ChaseNotifications"
            val descriptionText = "Notifications for chase access"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("CHASE_CHANNEL_ID", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: android.app.NotificationManager =
                getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun sendChaseLockedNotification() {
        val builder = NotificationCompat.Builder(this, "CHASE_CHANNEL_ID")
            .setSmallIcon(R.drawable.baseline_info_24)
            .setContentTitle("Chase Locked")
            .setContentText("Get at least one point to unlock the Chase feature.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE) // Add this
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(2001, builder.build())
    }
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            sendChaseLockedNotification()
        } else {
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }



}
