package com.example.juki

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> GameFragment()         // игра — первой вкладкой
        1 -> RegisterFragment()
        2 -> RulesFragment()
        3 -> AuthorsFragment()
        4 -> SettingsFragment()
        else -> GameFragment()
    }
}