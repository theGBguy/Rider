package com.example.rider.ui

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.rider.R
import com.example.rider.databinding.ActivityStudentSideNavBarBinding
import com.example.rider.ui.nav_fragments.DonateFragment
import com.example.rider.ui.nav_fragments.ProfileFragment
import com.example.rider.ui.nav_fragments.StudentHomeFragment
import com.example.rider.ui.nav_fragments.StudentRequestFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class StudentSideNavBarActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener {
    private var binding: ActivityStudentSideNavBarBinding? = null

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount <= 1) {
            if (Firebase.auth.currentUser != null) {
                showLogoutDialog()
            } else {
                super.onBackPressed()
            }
        } else {
            supportFragmentManager.popBackStack()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStudentSideNavBarBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        setSupportActionBar(binding?.toolbar)

        supportFragmentManager.addOnBackStackChangedListener {
            when (supportFragmentManager.findFragmentByTag("visible")) {
                is StudentHomeFragment -> {
                    binding?.navView?.setCheckedItem(R.id.nav_home)
                }
                is StudentRequestFragment -> {
                    binding?.navView?.setCheckedItem(R.id.nav_request)
                }
                is ProfileFragment -> {
                    binding?.navView?.setCheckedItem(R.id.nav_profile)
                }
                is DonateFragment -> {
                    binding?.navView?.setCheckedItem(R.id.nav_donate)
                }
            }
        }

        binding?.navView?.setNavigationItemSelectedListener(this)
        val toggle = ActionBarDrawerToggle(
            this,
            binding?.drawerLayout,
            binding?.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        binding?.drawerLayout?.addDrawerListener(toggle)
        toggle.syncState()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, StudentHomeFragment(), "visible")
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_options, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.about_id) {
            startActivity(Intent(this, AboutOptionMenuActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, StudentHomeFragment(), "visible")
                .addToBackStack(null)
                .commit()
            R.id.nav_profile -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment(), "visible")
                .addToBackStack(null)
                .commit()
            R.id.nav_request -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, StudentRequestFragment(), "visible")
                .addToBackStack(null)
                .commit()
            R.id.nav_donate -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DonateFragment(), "visible")
                .addToBackStack(null)
                .commit()
            R.id.nav_logout -> if (Firebase.auth.currentUser != null) showLogoutDialog()
        }
        binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        return true
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.app_name)
            .setIcon(R.mipmap.ic_launcher)
            .setMessage("Do you want to log out?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _: DialogInterface?, _: Int ->
                Firebase.auth.signOut()
                finish()
            }
            .setNegativeButton("No") { dialog: DialogInterface, _: Int -> dialog.cancel() }
            .show()
    }
}