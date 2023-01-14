package com.lu.wxmask.ui

import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lu.magic.ui.BindingFragment
import com.lu.magic.util.SizeUtil
import com.lu.magic.util.ToastUtil
import com.lu.magic.util.log.LogUtil
import com.lu.magic.util.ripple.RectangleRippleBuilder
import com.lu.magic.util.ripple.RippleApplyUtil
import com.lu.mask.donate.DonatePresenter
import com.lu.wxmask.BuildConfig
import com.lu.wxmask.Constrant
import com.lu.wxmask.R
import com.lu.wxmask.SelfHook
import com.lu.wxmask.databinding.FragmentMainBinding


class MainFragment : BindingFragment<FragmentMainBinding>() {
    private val donatePresenter by lazy { DonatePresenter.create() }

    override fun onViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentMainBinding {
        return FragmentMainBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rippleRadius = SizeUtil.dp2px(resources, 8f).toInt()
        binding.clModuleState.let {
            if (SelfHook.getInstance().isModuleEnable) {
                binding.ivModuleState.setImageResource(R.drawable.ic_icon_check)
                binding.tvModuleStateTitle.setText(R.string.module_have_active)
                RippleApplyUtil.apply(
                    it,
                    RectangleRippleBuilder(requireContext().getColor(R.color.purple_200), Color.GRAY, rippleRadius)
                )
            } else {
                binding.ivModuleState.setImageResource(R.drawable.ic_icon_warning)
                binding.tvModuleStateTitle.setText(R.string.module_not_active)
                RippleApplyUtil.apply(it, RectangleRippleBuilder(0xFFFF6027.toInt(), Color.GRAY, rippleRadius))
            }
        }
        RectangleRippleBuilder(Color.TRANSPARENT, Color.GRAY, rippleRadius).let {
            RippleApplyUtil.apply(binding.clManagerConfig, it)
            RippleApplyUtil.apply(binding.clModuleDonate, it)
            RippleApplyUtil.apply(binding.clAddConfig, it)
        }
        val moduleVersionText = getString(R.string.module_version)
        val versionDesText = if (BuildConfig.DEBUG) {
            "v${BuildConfig.VERSION_NAME}-debug"
        } else {
            "v${BuildConfig.VERSION_NAME}-release"
        }
        binding.tvModuleStateSub.text = "$moduleVersionText：$versionDesText"

        binding.clManagerConfig.setOnClickListener {
            jumpWxManagerConfigUI(Constrant.VALUE_INTENT_PLUGIN_MODE_MANAGER)
        }

        //模块需要优化，先屏蔽入口
//        binding.clModuleDonate.visibility = View.GONE
        binding.clModuleDonate.setOnClickListener {
            donatePresenter.lecturing(it.context)
        }
        binding.clAddConfig.setOnClickListener {
            jumpWxManagerConfigUI(Constrant.VALUE_INTENT_PLUGIN_MODE_ADD)
        }
        binding.clModuleState.setOnClickListener {

        }

    }

    /**
     * 跳转到微信进行配置
     */
    private fun jumpWxManagerConfigUI(mode: Int = Constrant.VALUE_INTENT_PLUGIN_MODE_MANAGER) {
        try {
            val intent = Intent()
            intent.action = "com.tencent.mm.action.BIZSHORTCUT"
//                intent.action = Intent.ACTION_MAIN
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(Constrant.KEY_INTENT_FROM_MASK, true)
            intent.putExtra(Constrant.KEY_INTENT_PLUGIN_MODE, mode)

            intent.component = ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI")
            startActivity(intent)
        } catch (e: Exception) {
            ToastUtil.show("跳转WeChat配置页面失败")
            LogUtil.e(e)
        }
    }

}