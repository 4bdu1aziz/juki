package com.example.juki

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.juki.db.AppDatabase
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState != null) return

        val savedId = CurrentPlayer.getSavedId(this)
        if (savedId != -1L) {
            lifecycleScope.launch {
                val db = AppDatabase.getInstance(this@MainActivity)
                val player = db.playerDao().getById(savedId)
                if (player != null) {
                    CurrentPlayer.player = player
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, MenuFragment())
                        .commit()
                } else {
                    showLogin()
                }
            }
        } else {
            showLogin()
        }
    }

    private fun showLogin() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, LoginFragment())
            .commit()
    }
}