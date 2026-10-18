package com.example.juki

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int) = when (position) {
        0 -> GameFragment()
        1 -> RegisterFragment()
        2 -> RecordsFragment()
        3 -> RulesFragment()
        4 -> AuthorsFragment()
        5 -> SettingsFragment()
        else -> GameFragment()
    }
}