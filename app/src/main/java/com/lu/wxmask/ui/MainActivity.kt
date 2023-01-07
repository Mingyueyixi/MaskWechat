package com.lu.wxmask.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lu.magic.ui.FragmentNavigation
import com.lu.wxmask.databinding.LayoutMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var fragmentNavigation: FragmentNavigation
    private lateinit var binding: LayoutMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fragmentNavigation = FragmentNavigation(this, binding.mainContainer)
        fragmentNavigation.navigate(MainFragment::class.java)
    }


    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (!fragmentNavigation.navigateBack()) {
            super.onBackPressed()
        }
        applicationContext
    }


    override fun onResume() {
        super.onResume()
    }

}