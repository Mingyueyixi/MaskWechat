package com.lu.wxmask.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.lu.magic.ui.FragmentNavigation
import com.lu.wxmask.databinding.LayoutMainBinding
import com.lu.wxmask.route.MaskAppRouter
import com.lu.wxmask.ui.vm.AppUpdateViewModel


class MainActivity : AppCompatActivity() {
    private lateinit var fragmentNavigation: FragmentNavigation
    private lateinit var binding: LayoutMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fragmentNavigation = FragmentNavigation(this, binding.mainContainer)
        fragmentNavigation.navigate(MainFragment::class.java)
        ViewModelProvider(this)[AppUpdateViewModel::class.java].checkOnEnter(this)

        handleDeeplinkRoute(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleDeeplinkRoute(intent)
    }

    private fun handleDeeplinkRoute(intent: Intent?) {
        if (intent == null) return
        val from = intent.getStringExtra("from")
        if (DeepLinkActivity::class.java.name != from) {
            return
        }
        intent.data?.let {
            binding.root.post {
                MaskAppRouter.route(this, it.toString())
            }
        }
    }


    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (!fragmentNavigation.navigateBack()) {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu)
        JsonMenuManager.inflate(this, menu)
        return true
    }

    override fun onResume() {
        super.onResume()
        JsonMenuManager.updateMenuListFromRemoteIfNeed(this)
    }

}