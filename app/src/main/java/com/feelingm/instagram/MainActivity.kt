package com.feelingm.instagram

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.feelingm.instagram.navigation.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progress_bar.visibility = View.VISIBLE

        bottom_navigation.setOnNavigationItemSelectedListener(this)
        bottom_navigation.selectedItemId = R.id.action_home

        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun setToolbarDefault() {
        toolbar_title_image.visibility = View.VISIBLE
        toolbar_btn_back.visibility = View.GONE
        toolbar_username.visibility = View.GONE
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        setToolbarDefault()

        when (item.itemId) {

            R.id.action_home -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.main_content, DetailViewFragment())
                        .commit()
                return true
            }

            R.id.action_search -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.main_content, GridFragment())
                        .commit()
                return true
            }

            R.id.action_add_photo -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    startActivity(Intent(this, AddPhotoActivity::class.java))
                } else {
                    Toast.makeText(this, "no permission", Toast.LENGTH_SHORT).show()
                }
                return true
            }

            R.id.action_favorite_alarm -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.main_content, AlarmFragment())
                        .commit()
                return true
            }

            R.id.action_account -> {
                val uid = FirebaseAuth.getInstance().currentUser!!.uid
                val bundle = Bundle().apply {
                    putString("destinationUid", uid)
                }
                supportFragmentManager.beginTransaction()
                        .replace(R.id.main_content, UserFragment().apply { arguments = bundle })
                        .commit()
                return true
            }
        }

        return false
    }
}