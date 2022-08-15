package com.waveproject.tools.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import cn.fkj233.ui.activity.MIUIActivity
import cn.fkj233.ui.activity.view.SpinnerV
import cn.fkj233.ui.activity.view.SwitchV
import cn.fkj233.ui.activity.view.TextSummaryV
import cn.fkj233.ui.dialog.MIUIDialog
import com.waveproject.tools.BuildConfig
import com.waveproject.tools.R
import com.waveproject.tools.utils.ShellUtils

class SettingsActivity: MIUIActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private fun getSelinuxStatus(): Boolean {
        return when (ShellUtils.execCommand("getenforce", true).successMsg) {
            "Enforcing" -> true
            "Permissive" -> false
            else -> false
        }
    }

    private fun getBatteryHealthStatus(): Boolean {
        return when (ShellUtils.execCommand("getprop persist.vendor.battery.health", true).successMsg) {
            "true" -> true
            "false" -> false
            else -> false
        }
    }

    @SuppressLint("SdCardPath")
    private fun getDolbyStatus(): Boolean {
        return when (ShellUtils.execCommand("/data/data/com.waveproject.tools/files/core/loading_dolby_xml.sh get", true).successMsg) {
            "1" -> true
            "0" -> false
            else -> false
        }
    }

    private fun getMaxModeStatus(): Boolean {
        return when (ShellUtils.execCommand("settings get Secure speed_mode_enable", true).successMsg) {
            "1" -> true
            "0" -> false
            else -> false
        }
    }

    private fun getFreezerStatus(): String {
        val freezerStatus = when(ShellUtils.execCommand("settings get global cached_apps_freezer", true).successMsg){
            "disabled" -> getString(R.string.disable_text)
            "default" -> getString(R.string.default_text)
            "enable" -> getString(R.string.enable_text)
            else -> getString(R.string.null_text)
        }
        return freezerStatus
    }

    private fun getKernelFreezerStatus(): String {
        return when (ShellUtils.execCommand("device_config list | grep activity_manager_native_boot/use_freezer | cut -d = -f 2", true).successMsg) {
            "true" -> getString(R.string.enable_text)
            "false" -> getString(R.string.disable_text)
            else -> getString(R.string.null_text)
        }
    }

    private fun getYCStatus(): String {
        return when (ShellUtils.execCommand("cat /sdcard/Android/yc/uperf/cur_powermode.txt", true).successMsg) {
            "auto" -> getString(R.string.yc_auto)
            "powersave" -> getString(R.string.yc_powersave)
            "balance" -> getString(R.string.yc_balance)
            "performance" -> getString(R.string.yc_performance)
            "fast" -> getString(R.string.yc_fast)
            else -> getString(R.string.null_text)
        }
    }
    init {
        initView {
            registerMain(getString(R.string.app_name), false) {
                TextSummaryWithSwitch(
                    TextSummaryV(
                        // 加载杜比配置
                        textId = R.string.loading_dolby_xml
                    ),
                    SwitchV("loading_dolby_xml", getDolbyStatus())
                    {
                        if (getDolbyStatus()) {
                            // 开启时
                            ShellUtils.execCommand("sh /data/data/com.waveproject.tools/files/core/loading_dolby_xml.sh set false", true)
                        } else {
                            // 关闭时
                            ShellUtils.execCommand("sh /data/data/com.waveproject.tools/files/core/loading_dolby_xml.sh set true", true)
                        }
                    }
                )
                TextSummaryWithSwitch(
                    TextSummaryV(
                        // 电池健康
                        textId = R.string.display_battery_health
                    ),
                    SwitchV("display_battery_health", getBatteryHealthStatus())
                    {
                        if (getBatteryHealthStatus()) {
                            // 开启时
                            ShellUtils.execCommand("setprop persist.vendor.battery.health false", true)
                        } else {
                            // 关闭时
                            ShellUtils.execCommand("setprop persist.vendor.battery.health true", true)
                        }
                    }
                )
                TextSummaryWithSwitch(
                    TextSummaryV(
                        // 临时selinux
                        textId = R.string.max_mode
                    ),
                    SwitchV("max_mode", getMaxModeStatus())
                    {
                        if (getMaxModeStatus()) {
                            // 开启时
                            ShellUtils.execCommand("settings put Secure speed_mode_enable 0", true)
                        } else {
                            // 关闭时
                            ShellUtils.execCommand("settings put Secure speed_mode_enable 1", true)
                        }
                    }
                )
                TextSummaryWithSwitch(
                    TextSummaryV(
                        // 临时selinux
                        textId = R.string.temporary_selinux
                    ),
                    SwitchV("temporary_selinux", getSelinuxStatus())
                    {
                        if (getSelinuxStatus()) {
                            // 开启时
                            ShellUtils.execCommand("setenforce 0", true)
                        } else {
                            // 关闭时
                            ShellUtils.execCommand("setenforce 1", true)
                        }
                    }
                )
                val freezerStatus: HashMap<Int, String> = hashMapOf()
                freezerStatus[0] = getString(R.string.disable_text)
                freezerStatus[1] = getString(R.string.default_text)
                freezerStatus[2] = getString(R.string.enable_text)
                TextSummaryWithSpinner(
                    TextSummaryV(
                        // 墓碑模式
                        textId = R.string.cashed_apps_freezer,
                        tipsId = R.string.cashed_apps_freezer_tips
                    ),
                    SpinnerV(getFreezerStatus()) {
                        add(freezerStatus[0].toString())
                        {
                            ShellUtils.execCommand("settings put global cached_apps_freezer disabled", true)
                        }
                        add(freezerStatus[1].toString())
                        {
                            ShellUtils.execCommand("settings put global cached_apps_freezer default", true)
                        }
                        add(freezerStatus[2].toString())
                        {
                            ShellUtils.execCommand("settings put global cached_apps_freezer enable", true)
                        }
                    }
                )
                val kernelFreezerStatus: HashMap<Int, String> = hashMapOf()
                kernelFreezerStatus[0] = getString(R.string.disable_text)
                kernelFreezerStatus[1] = getString(R.string.enable_text)
                TextSummaryWithSpinner(
                    TextSummaryV(
                        // 墓碑模式（内核）
                        textId = R.string.kernel_cashed_apps_freezer,
                        tipsId = R.string.kernel_cashed_apps_freezer_tips
                    ),
                    SpinnerV(getKernelFreezerStatus()) {
                        add(kernelFreezerStatus[0].toString())
                        {
                            ShellUtils.execCommand("device_config put activity_manager_native_boot use_freezer false", true)
                        }
                        add(kernelFreezerStatus[1].toString())
                        {
                            ShellUtils.execCommand("device_config put activity_manager_native_boot use_freezer true", true)
                        }
                    }
                )
                Line()
                TitleText(textId = R.string.yc)
                val ycStatus: HashMap<Int, String> = hashMapOf()
                ycStatus[0] = getString(R.string.yc_auto)
                ycStatus[1] = getString(R.string.yc_powersave)
                ycStatus[2] = getString(R.string.yc_balance)
                ycStatus[3] = getString(R.string.yc_performance)
                ycStatus[4] = getString(R.string.yc_fast)
                TextSummaryWithSpinner(
                    TextSummaryV(
                        // YC调度切换器
                        textId = R.string.yc_current_mode,
                    ),
                    SpinnerV(getYCStatus()) {
                        add(ycStatus[0].toString())
                        {
                            ShellUtils.execCommand("sh /data/adb/modules/uperf/script/powercfg_main.sh auto", true)
                        }
                        add(ycStatus[1].toString())
                        {
                            ShellUtils.execCommand("sh /data/adb/modules/uperf/script/powercfg_main.sh powersave", true)
                        }
                        add(ycStatus[2].toString())
                        {
                            ShellUtils.execCommand("sh /data/adb/modules/uperf/script/powercfg_main.sh balance", true)
                        }
                        add(ycStatus[3].toString())
                        {
                            ShellUtils.execCommand("sh /data/adb/modules/uperf/script/powercfg_main.sh performance", true)
                        }
                        add(ycStatus[4].toString())
                        {
                            ShellUtils.execCommand("sh /data/adb/modules/uperf/script/powercfg_main.sh fast", true)
                        }
                    }
                )
                TextSummaryArrow(
                    TextSummaryV(
                        textId = R.string.yc_get_new_version,
                        onClickListener =
                        {
                            try {
                                val uri = Uri.parse("https://github.com/yc9559/uperf/releases")
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(this@SettingsActivity, "访问失败", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                )
                Line()
                TitleText(textId = R.string.more)
                TextSummaryArrow(
                    TextSummaryV(
                        textId = R.string.about_waveprojecttools,
                        tipsId = R.string.about_waveprojecttools_tips,
                        onClickListener =
                        {
                            showFragment("about_waveprojecttools")
                        }
                    )
                )
                register("about_waveprojecttools", getString(R.string.about_waveprojecttools), true)
                {
                    Author(
                        authorHead = getDrawable(R.mipmap.ic_launcher)!!,
                        authorName = getString(R.string.app_name),
                        authorTips = "${BuildConfig.VERSION_NAME}(${BuildConfig.BUILD_TYPE})",
                        onClickListener =
                        {
                            Toast.makeText(
                                this@SettingsActivity,
                                "你点我干什么♪(･ω･)ﾉ",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                    Line()
                    TitleText(text = getString(R.string.developer))
                    Author(
                        authorHead = getDrawable(R.drawable.author)!!,
                        authorName = "Alibi",
                        authorTips = "LittleWave",
                        onClickListener =
                        {
                            try {
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("coolmarket://u/3498251")))
                                Toast.makeText(this@SettingsActivity, "点个关注吧！", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(this@SettingsActivity, "本机未安装酷安应用！", Toast.LENGTH_SHORT).show()
                                val uri = Uri.parse("https://www.coolapk.com/u/3498251")
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                startActivity(intent)
                            }
                        }
                    )
                    Line()
                    TitleText(text = getString(R.string.thank_list))
                    TextSummaryArrow(
                        TextSummaryV(
                            textId = R.string.third_party_open_source_statement,
                            onClickListener =
                            {
                                try {
                                    val uri = Uri.parse("https://github.com/yaoxiaonie/WaveProjectTools")
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(this@SettingsActivity, "访问失败！", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    )
                    Line()
                    TitleText(text = getString(R.string.discussions))
                    TextSummaryArrow(
                        TextSummaryV(
                            textId = R.string.issues,
                            tipsId = R.string.issues_url,
                            onClickListener =
                            {
                                try {
                                    val uri = Uri.parse("https://github.com/yaoxiaonie/WaveProjectTools/issues")
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(this@SettingsActivity, "访问失败！", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    )
                    Line()
                    TitleText(getString(R.string.other))
                    TextSummaryArrow(TextSummaryV(
                        textId = R.string.opensource,
                        tipsId = R.string.github_url,
                        onClickListener =
                        {
                            try {
                                val uri = Uri.parse("https://github.com/yaoxiaonie/WaveProjectTools")
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(this@SettingsActivity, "访问失败！", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    )
                }
                registerMenu(getString(R.string.menu))
                {
                    TextSummaryArrow(
                        TextSummaryV(
                            textId = R.string.reboot_system,
                            onClickListener =
                            {
                                MIUIDialog(this@SettingsActivity) {
                                    setTitle(R.string.warning)
                                    setMessage(R.string.reboot_tips)
                                    setLButton(R.string.cancel) {
                                        dismiss()
                                    }
                                    setRButton(R.string.done) {
                                        ShellUtils.execCommand("/system/bin/sync;/system/bin/svc power reboot || reboot", true)
                                    }
                                }.show()
                            }
                        )
                    )
                }
            }
        }
    }
}