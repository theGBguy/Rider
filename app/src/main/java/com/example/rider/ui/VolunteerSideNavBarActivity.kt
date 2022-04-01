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
import com.example.rider.databinding.ActivityVolunteerSideNavBarBinding
import com.example.rider.ui.nav_fragments.DonateFragment
import com.example.rider.ui.nav_fragments.ProfileFragment
import com.example.rider.ui.nav_fragments.VolunteerHomeFragment
import com.example.rider.ui.nav_fragments.VolunteerRequestFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class VolunteerSideNavBarActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener {
    private var binding: ActivityVolunteerSideNavBarBinding? = null

    override fun onBackPressed() {
        if (Firebase.auth.currentUser != null) {
            showLogoutDialog()
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVolunteerSideNavBarBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        setSupportActionBar(binding?.toolbar)

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
                .replace(R.id.fragment_container, VolunteerHomeFragment()).commit()
            binding?.navView?.setCheckedItem(R.id.nav_home)
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
                .replace(R.id.fragment_container, VolunteerHomeFragment()).commit()
            R.id.nav_profile -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment()).commit()
            R.id.nav_request -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, VolunteerRequestFragment()).commit()
            R.id.nav_logout -> showLogoutDialog()
            R.id.nav_donate -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DonateFragment()).commit()
        }
        binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        return true
    }

    fun showLogoutDialog() {
        if (Firebase.auth.currentUser != null) {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.app_name)
                .setIcon(R.mipmap.ic_launcher)
                .setMessage("Do you want to Log Out?")
                .setCancelable(false)
                .setPositiveButton("Yes") { _: DialogInterface?, _: Int ->
                    Firebase.auth.signOut()
                    finish()
                }
                .setNegativeButton("No") { dialog: DialogInterface, _: Int -> dialog.cancel() }
                .show()
        }
    }
}