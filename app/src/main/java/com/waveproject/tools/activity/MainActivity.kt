package com.waveproject.tools.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import cn.fkj233.ui.activity.MIUIActivity
import cn.fkj233.ui.activity.view.SpinnerV
import cn.fkj233.ui.activity.view.SwitchV
import cn.fkj233.ui.activity.view.TextSummaryV
import cn.fkj233.ui.dialog.MIUIDialog
import com.waveproject.tools.BuildConfig
import com.waveproject.tools.R
import com.waveproject.tools.utils.CopyAssetsUtils
import com.waveproject.tools.utils.ShellUtils
import java.util.*
import kotlin.collections.HashMap

class MainActivity : MIUIActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CopyAssetsUtils.copyAssetsDir2Phone(this,"core");
        ShellUtils.exec("chmod -R 777 /data/data/com.waveproject.tools/files/core")
    }

    fun getSelinuxStatus(): Boolean {
        val selinuxStatus: String = ShellUtils.exec("getenforce")
        return when (selinuxStatus) {
            "Enforcing\n" -> true
            "Permissive\n" -> false
            else -> false
        }
    }

    fun getBatteryHealthStatus(): Boolean {
        val batteryHealthStatus: String = ShellUtils.exec("getprop persist.vendor.battery.health")
        return when (batteryHealthStatus) {
            "true\n" -> true
            "false\n" -> false
            else -> false
        }
    }

    fun getDolbyStatus(): Boolean {
        val dolbyStatus: String = ShellUtils.exec("/data/data/com.waveproject.tools/files/core/loading_dolby_xml.sh get")
        return when (dolbyStatus) {
            "1\n" -> true
            "0\n" -> false
            else -> false
        }
    }

    fun getMaxModeStatus(): Boolean {
        val maxModeStatus: String = ShellUtils.exec("settings get Secure speed_mode_enable")
        return when (maxModeStatus) {
            "1\n" -> true
            "0\n" -> false
            else -> false
        }
    }

    fun getFreezerStatus(): String {
        val freezerStatus = when(ShellUtils.exec("settings get global cached_apps_freezer")){
            "disabled\n" -> getString(R.string.disable_text)
            "default\n" -> getString(R.string.default_text)
            "enable\n" -> getString(R.string.enable_text)
            else -> getString(R.string.null_text)
        }
        return freezerStatus
    }

    fun getKernelFreezerStatus(): String {
        val kernelFreezerStatus: String = ShellUtils.exec("device_config list | grep activity_manager_native_boot/use_freezer | cut -d = -f 2")
        return when (kernelFreezerStatus) {
            "true\n" -> getString(R.string.enable_text)
            "false\n" -> getString(R.string.disable_text)
            else -> getString(R.string.null_text)
        }
    }

    fun getYCStatus(): String {
        val ycStatus: String = ShellUtils.exec("cat /sdcard/Android/yc/uperf/cur_powermode.txt")
        return when (ycStatus) {
            "auto\n" -> getString(R.string.yc_auto)
            "powersave\n" -> getString(R.string.yc_powersave)
            "balance\n" -> getString(R.string.yc_balance)
            "performance\n" -> getString(R.string.yc_performance)
            "fast\n" -> getString(R.string.yc_fast)
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
                            Log.d("MainActivity", "执行umount /data/vendor/dolby")
                            ShellUtils.exec("sh /data/data/com.waveproject.tools/files/core/loading_dolby_xml.sh set false")
                        } else {
                            // 关闭时
                            Log.d(
                                "MainActivity",
                                "执行mount --bind /vendor/etc/dolby /data/vendor/dolby"
                            )
                            ShellUtils.exec("sh /data/data/com.waveproject.tools/files/core/loading_dolby_xml.sh set true")
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
                            Log.d("MainActivity", "执行setprop persist.vendor.battery.health false")
                            ShellUtils.exec("setprop persist.vendor.battery.health false")
                        } else {
                            // 关闭时
                            Log.d("MainActivity", "setprop persist.vendor.battery.health true")
                            ShellUtils.exec("setprop persist.vendor.battery.health true")
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
                            Log.d("MainActivity", "执行settings put Secure speed_mode_enable 0")
                            ShellUtils.exec("settings put Secure speed_mode_enable 0")
                        } else {
                            // 关闭时
                            Log.d("MainActivity", "执行settings put Secure speed_mode_enable 1")
                            ShellUtils.exec("settings put Secure speed_mode_enable 1")
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
                            Log.d("MainActivity", "执行setenforce 0")
                            ShellUtils.exec("setenforce 0")
                        } else {
                            // 关闭时
                            Log.d("MainActivity", "执行setenforce 1")
                            ShellUtils.exec("setenforce 1")
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
                            ShellUtils.exec("settings put global cached_apps_freezer disabled")
                        }
                        add(freezerStatus[1].toString())
                        {
                            ShellUtils.exec("settings put global cached_apps_freezer default")
                        }
                        add(freezerStatus[2].toString())
                        {
                            ShellUtils.exec("settings put global cached_apps_freezer enable")
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
                            ShellUtils.exec("device_config put activity_manager_native_boot use_freezer false")
                        }
                        add(kernelFreezerStatus[1].toString())
                        {
                            ShellUtils.exec("device_config put activity_manager_native_boot use_freezer true")
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
                            ShellUtils.exec("sh /data/adb/modules/uperf/script/powercfg_main.sh auto")
                        }
                        add(ycStatus[1].toString())
                        {
                            ShellUtils.exec("sh /data/adb/modules/uperf/script/powercfg_main.sh powersave")
                        }
                        add(ycStatus[2].toString())
                        {
                            ShellUtils.exec("sh /data/adb/modules/uperf/script/powercfg_main.sh balance")
                        }
                        add(ycStatus[3].toString())
                        {
                            ShellUtils.exec("sh /data/adb/modules/uperf/script/powercfg_main.sh performance")
                        }
                        add(ycStatus[4].toString())
                        {
                            ShellUtils.exec("sh /data/adb/modules/uperf/script/powercfg_main.sh fast")
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
                                Toast.makeText(this@MainActivity, "访问失败", Toast.LENGTH_SHORT).show()
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
                                this@MainActivity,
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
                            Toast.makeText(this@MainActivity, "靓仔，点个关注吧！", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(this@MainActivity, "本机未安装酷安应用", Toast.LENGTH_SHORT).show()
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
                                Toast.makeText(this@MainActivity, "访问失败", Toast.LENGTH_SHORT).show()
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
                                Toast.makeText(this@MainActivity, "访问失败", Toast.LENGTH_SHORT).show()
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
                            Toast.makeText(this@MainActivity, "访问失败", Toast.LENGTH_SHORT).show()
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
                                MIUIDialog(this@MainActivity) {
                                    setTitle(R.string.warning)
                                    setMessage(R.string.reboot_tips)
                                    setLButton(R.string.cancel) {
                                        dismiss()
                                    }
                                    setRButton(R.string.done) {
                                        ShellUtils.exec("/system/bin/sync;/system/bin/svc power reboot || reboot")
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