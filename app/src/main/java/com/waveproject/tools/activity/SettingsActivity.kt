package com.waveproject.tools.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import cn.fkj233.ui.activity.MIUIActivity
import cn.fkj233.ui.activity.view.SpinnerV
import cn.fkj233.ui.activity.view.SwitchV
import cn.fkj233.ui.activity.view.TextSummaryV
import cn.fkj233.ui.dialog.MIUIDialog
import com.jaredrummler.ktsh.Shell
import com.waveproject.tools.BuildConfig
import com.waveproject.tools.R
import com.waveproject.tools.data.DataConst.GET_BATTERY_HEALTH_STATUS
import com.waveproject.tools.data.DataConst.GET_CACHED_APPS_FREEZER_KERNEL_STATUS
import com.waveproject.tools.data.DataConst.GET_CACHED_APPS_FREEZER_STATUS
import com.waveproject.tools.data.DataConst.GET_MAX_MODE_STATUS
import com.waveproject.tools.data.DataConst.GET_SELINUX_STATUS
import com.waveproject.tools.data.DataConst.GET_YC_STATUS
import com.waveproject.tools.data.DataConst.REBOOT
import com.waveproject.tools.data.DataConst.SET_BATTERY_HEALTH_OFF
import com.waveproject.tools.data.DataConst.SET_BATTERY_HEALTH_ON
import com.waveproject.tools.data.DataConst.SET_CACHED_APPS_FREEZER_KERNEL_DEFAULT
import com.waveproject.tools.data.DataConst.SET_CACHED_APPS_FREEZER_KERNEL_DISABLE
import com.waveproject.tools.data.DataConst.SET_CACHED_APPS_FREEZER_KERNEL_ENABLE
import com.waveproject.tools.data.DataConst.SET_CACHED_APPS_FREEZER_KERNEL_OFF
import com.waveproject.tools.data.DataConst.SET_CACHED_APPS_FREEZER_KERNEL_ON
import com.waveproject.tools.data.DataConst.SET_MAX_MODE_OFF
import com.waveproject.tools.data.DataConst.SET_MAX_MODE_ON
import com.waveproject.tools.data.DataConst.SET_SELINUX_OFF
import com.waveproject.tools.data.DataConst.SET_SELINUX_ON
import com.waveproject.tools.data.DataConst.SET_YC_AUTO
import com.waveproject.tools.data.DataConst.SET_YC_BALANCE
import com.waveproject.tools.data.DataConst.SET_YC_FAST
import com.waveproject.tools.data.DataConst.SET_YC_PERFORMANCE
import com.waveproject.tools.data.DataConst.SET_YC_POWERSAVE
import com.waveproject.tools.utils.ShellUtils

class SettingsActivity: MIUIActivity() {
    private val suShell = Shell.SU
    private val shShell = Shell.SH

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        suShell.shutdown()
        shShell.shutdown()
    }

    private fun getSelinuxStatus(): Boolean {
        return when (suShell.run(GET_SELINUX_STATUS).stdout()) {
            "Enforcing" -> true
            "Permissive" -> false
            else -> false
        }
    }

    private fun getBatteryHealthStatus(): Boolean {
        return when (shShell.run(GET_BATTERY_HEALTH_STATUS).stdout()) {
            "true" -> true
            "false" -> false
            else -> false
        }
    }

    private fun getMaxModeStatus(): Boolean {
        return when (shShell.run(GET_MAX_MODE_STATUS).stdout()) {
            "1" -> true
            "0" -> false
            else -> false
        }
    }

    private fun getFreezerStatus(): String {
        val freezerStatus = when(shShell.run(GET_CACHED_APPS_FREEZER_STATUS).stdout()){
            "disabled" -> getString(R.string.disable_text)
            "default" -> getString(R.string.default_text)
            "enable" -> getString(R.string.enable_text)
            else -> getString(R.string.null_text)
        }
        return freezerStatus
    }

    private fun getKernelFreezerStatus(): String {
        return when (shShell.run(GET_CACHED_APPS_FREEZER_KERNEL_STATUS).stdout()) {
            "true" -> getString(R.string.enable_text)
            "false" -> getString(R.string.disable_text)
            else -> getString(R.string.null_text)
        }
    }

    private fun getYCStatus(): String {
        return when (ShellUtils.execCommand(GET_YC_STATUS, true).successMsg) {
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
                        // 电池健康
                        textId = R.string.display_battery_health
                    ),
                    SwitchV("display_battery_health", getBatteryHealthStatus())
                    {
                        if (getBatteryHealthStatus()) {
                            // 开启时
                            suShell.run(SET_BATTERY_HEALTH_OFF)
                        } else {
                            // 关闭时
                            suShell.run(SET_BATTERY_HEALTH_ON)
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
                            suShell.run(SET_MAX_MODE_OFF)
                        } else {
                            // 关闭时
                            suShell.run(SET_MAX_MODE_ON)
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
                            suShell.run(SET_SELINUX_OFF)
                        } else {
                            // 关闭时
                            suShell.run(SET_SELINUX_ON)
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
                            suShell.run(SET_CACHED_APPS_FREEZER_KERNEL_DISABLE)
                        }
                        add(freezerStatus[1].toString())
                        {
                            suShell.run(SET_CACHED_APPS_FREEZER_KERNEL_DEFAULT)
                        }
                        add(freezerStatus[2].toString())
                        {
                            suShell.run(SET_CACHED_APPS_FREEZER_KERNEL_ENABLE)
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
                            suShell.run(SET_CACHED_APPS_FREEZER_KERNEL_OFF)
                        }
                        add(kernelFreezerStatus[1].toString())
                        {
                            suShell.run(SET_CACHED_APPS_FREEZER_KERNEL_ON)
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
                            ShellUtils.execCommand(SET_YC_AUTO, true)
                        }
                        add(ycStatus[1].toString())
                        {
                            ShellUtils.execCommand(SET_YC_POWERSAVE, true)
                        }
                        add(ycStatus[2].toString())
                        {
                            ShellUtils.execCommand(SET_YC_BALANCE, true)
                        }
                        add(ycStatus[3].toString())
                        {
                            ShellUtils.execCommand(SET_YC_PERFORMANCE, true)
                        }
                        add(ycStatus[4].toString())
                        {
                            ShellUtils.execCommand(SET_YC_FAST, true)
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
                                Toast.makeText(this@SettingsActivity, R.string.access_failed, Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                )
                Line()
                TitleText(textId = R.string.more)
                TextSummaryArrow(
                    TextSummaryV(
                        textId = R.string.system_updater,
                        onClickListener =
                        {
                            val isAppModuleInstalled = ShellUtils.execCommand("[ -d /data/adb/modules/WaveProjectUpdate ]", true).result
                            val isReboot = ShellUtils.execCommand("[ ! -f /data/adb/modules/WaveProjectUpdate/update ]", true).result
                            val isEnable = ShellUtils.execCommand("[ ! -f /data/adb/modules/WaveProjectUpdate/disable ]", true).result
                            if (isAppModuleInstalled == 0) {
                                if (isReboot == 0 && isEnable == 0) {
                                    val intent = Intent(this@SettingsActivity, SystemUpdater::class.java)
                                    startActivity(intent)
                                    suShell.shutdown()
                                    shShell.shutdown()
                                } else if (isReboot != 0) {
                                    Toast.makeText(this@SettingsActivity, R.string.reboot_to_take_effect, Toast.LENGTH_SHORT).show()
                                } else if (isEnable != 0) {
                                    Toast.makeText(this@SettingsActivity, R.string.is_disable, Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                suShell.run("cp -frp /data/data/com.waveproject.tools/files/core/WaveProjectUpdate /data/adb/modules/ && chown -R root.root /data/adb/modules/WaveProjectUpdate")
                                Toast.makeText(this@SettingsActivity, R.string.no_module_installed, Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                )
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
                                Toast.makeText(this@SettingsActivity, R.string.pay_attention, Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(this@SettingsActivity, R.string.no_coolapk, Toast.LENGTH_SHORT).show()
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
                                    Toast.makeText(this@SettingsActivity, R.string.access_failed, Toast.LENGTH_SHORT).show()
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
                                    Toast.makeText(this@SettingsActivity, R.string.access_failed, Toast.LENGTH_SHORT).show()
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
                                Toast.makeText(this@SettingsActivity, R.string.access_failed, Toast.LENGTH_SHORT).show()
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
                                        suShell.run(REBOOT)
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