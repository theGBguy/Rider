package com.example.rider.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.rider.R
import com.example.rider.databinding.ActivityStudentSideNavBarBinding
import com.google.android.material.navigation.NavigationView

class StudentSideNavBarActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener {
    private var binding: ActivityStudentSideNavBarBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStudentSideNavBarBinding.inflate(layoutInflater)
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
                .replace(R.id.fragment_container, StudentHomeFragment()).commit()
            binding?.navView?.setCheckedItem(R.id.nav_home)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
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
                .replace(R.id.fragment_container, StudentHomeFragment()).commit()
            R.id.nav_profile -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment()).commit()
            R.id.nav_request -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, StudentRequestFragment()).commit()
            R.id.nav_logout -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LogoutFragment()).commit()
            R.id.nav_donate -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DonateFragment()).commit()
        }
        binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        return true
    }
}