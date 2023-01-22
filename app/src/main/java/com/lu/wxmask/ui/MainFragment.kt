package com.lu.wxmask.ui

import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lu.magic.ui.BaseFragment
import com.lu.magic.ui.LifecycleAutoViewBinding
import com.lu.magic.util.SizeUtil
import com.lu.magic.util.ToastUtil
import com.lu.magic.util.log.LogUtil
import com.lu.magic.util.ripple.RectangleRippleBuilder
import com.lu.magic.util.ripple.RippleApplyUtil
import com.lu.mask.donate.DonatePresenter
import com.lu.wxmask.BuildConfig
import com.lu.wxmask.ClazzN
import com.lu.wxmask.Constrant
import com.lu.wxmask.R
import com.lu.wxmask.SelfHook
import com.lu.wxmask.adapter.AbsListAdapter
import com.lu.wxmask.adapter.CommonListAdapter
import com.lu.wxmask.databinding.FragmentMainBinding
import com.lu.wxmask.databinding.ItemIconTextBinding


class MainFragment : BaseFragment() {
    private var itemBinding: ItemIconTextBinding by LifecycleAutoViewBinding<MainFragment, ItemIconTextBinding>()
    private var mainBinding: FragmentMainBinding by LifecycleAutoViewBinding<MainFragment, FragmentMainBinding>()
    private val donatePresenter by lazy { DonatePresenter.create() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentMainBinding.inflate(inflater, container, false).let {
            mainBinding = it
            it.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rippleRadius = SizeUtil.dp2px(resources, 8f).toInt()
        mainBinding.listView.adapter = object : CommonListAdapter<Any, ItemBindingViewHolder>() {
            init {
                setData(arrayListOf(1, 2, 3, 4))
            }


            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemBindingViewHolder {
                itemBinding = ItemIconTextBinding.inflate(layoutInflater, parent, false)

                return object : ItemBindingViewHolder(itemBinding) {
                    init {
                        itemBinding.layoutItem.setOnClickListener {
                            when (layoutPosition) {
                                1 -> jumpWxManagerConfigUI(Constrant.VALUE_INTENT_PLUGIN_MODE_ADD)
                                2 -> jumpWxManagerConfigUI(Constrant.VALUE_INTENT_PLUGIN_MODE_MANAGER)
                                3 -> donatePresenter.lecturing(view.context)
                            }
                        }

                    }
                }
            }

            override fun onBindViewHolder(vh: ItemBindingViewHolder, position: Int, parent: ViewGroup) {
                if (position != 0) {
                    applyCommonItemRipple(vh.binding.layoutItem)
                }
                when (position) {
                    0 -> {
                        if (SelfHook.getInstance().isModuleEnable) {
                            vh.binding.ivItemIcon.setImageResource(R.drawable.ic_icon_check)
                            vh.binding.tvItemTitle.setText(R.string.module_have_active)
                            applyModuleStateRipple(vh.binding.layoutItem, true)
                        } else {
                            vh.binding.ivItemIcon.setImageResource(R.drawable.ic_icon_warning)
                            vh.binding.tvItemTitle.setText(R.string.module_not_active)
                            applyModuleStateRipple(vh.binding.layoutItem, false)
                        }
                        vh.binding.tvItemTitleSub.text = (getString(R.string.module_version) + "：" + getVersionText())
                    }

                    1 -> {
                        vh.binding.ivItemIcon.setImageResource(R.drawable.ic_icon_add)
                        vh.binding.tvItemTitle.setText(R.string.config_add)
                        vh.binding.tvItemTitleSub.setText(R.string.click_here_to_add)
                    }

                    2 -> {
                        vh.binding.ivItemIcon.setImageResource(R.drawable.ic_icon_manager)
                        vh.binding.tvItemTitle.setText(R.string.config_manager)
                        vh.binding.tvItemTitleSub.setText(R.string.click_here_to_manager)
                    }

                    3 -> {
                        vh.binding.ivItemIcon.setImageResource(R.drawable.ic_icon_dollar)
                        vh.binding.tvItemTitle.setText(R.string.donate)
                        vh.binding.tvItemTitleSub.setText(R.string.donate_description)
                    }

                }

            }

            private fun applyCommonItemRipple(v: View) {
                RectangleRippleBuilder(Color.TRANSPARENT, Color.GRAY, rippleRadius).let {
                    RippleApplyUtil.apply(v, it)
                }
            }

            private fun applyModuleStateRipple(v: View, enable: Boolean) {
                val contentColor = if (enable) {
                    view.context.getColor(R.color.purple_200)
                } else {
                    0xFFFF6027.toInt()
                }
                RectangleRippleBuilder(contentColor, Color.GRAY, rippleRadius).let {
                    RippleApplyUtil.apply(v, it)
                }
            }

        }


// 设置了ripple， 子view拿走了事件，此处不响应
//        mainBinding.listView.setOnItemClickListener { _, view, position, _ ->
//
//        }
    }

    private fun getVersionText(): String {
        return if (BuildConfig.DEBUG) {
            "v${BuildConfig.VERSION_NAME}-debug"
        } else {
            "v${BuildConfig.VERSION_NAME}-release"
        }
    }

    open class ItemBindingViewHolder(@JvmField var binding: ItemIconTextBinding) :
        AbsListAdapter.ViewHolder(binding.root)

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

            intent.component = ComponentName("com.tencent.mm", ClazzN.LauncherUI)
            startActivity(intent)
        } catch (e: Exception) {
            ToastUtil.show("跳转WeChat配置页面失败")
            LogUtil.e(e)
        }
    }

}