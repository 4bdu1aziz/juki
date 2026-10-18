package com.example.juki

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class MenuPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> GameFragment()
        1 -> RecordsFragment()
        2 -> SettingsFragment()
        3 -> RulesFragment()
        4 -> AuthorsFragment()
        else -> GameFragment()
    }
}