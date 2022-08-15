package com.waveproject.tools.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import cn.fkj233.ui.activity.MIUIActivity
import com.waveproject.tools.BuildConfig
import com.waveproject.tools.R
import com.waveproject.tools.utils.CopyAssetsUtils
import com.waveproject.tools.utils.ShellUtils

class MainActivity: MIUIActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exractAssets()
        rootDetector()
    }

    private fun exractAssets() {
        CopyAssetsUtils.copyAssetsDir2Phone(this,"core")
        ShellUtils.execCommand("chmod -R 777 /data/data/com.waveproject.tools/files/core", false)
    }

    @SuppressLint("SdCardPath")
    private fun rootDetector() {
        val rootStatus = ShellUtils.execCommand("/data/data/com.waveproject.tools/files/core/check_root.sh", true)
        if (rootStatus.successMsg == "success") {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            this.finish()
        } else {
            Toast.makeText(this, "未能获取Root权限！", Toast.LENGTH_LONG).show()
        }
    }

    init {
        initView {
            registerMain(getString(R.string.app_name), false)
            {
                Author(
                    authorHead = getDrawable(R.mipmap.ic_launcher)!!,
                    authorName = getString(R.string.cannot_get_root),
                    authorTips = getString(R.string.download_magisk),
                    onClickListener =
                    {
                        try {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("coolmarket://apk/com.topjohnwu.magisk")
                                )
                            )
                            Toast.makeText(this@MainActivity, "请通过Magisk获取Root！", Toast.LENGTH_SHORT)
                                .show()
                        } catch (e: Exception) {
                            Toast.makeText(this@MainActivity, "本机未安装酷安应用！", Toast.LENGTH_SHORT)
                                .show()
                            val uri = Uri.parse("https://magiskcn.com/magisk-download")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            startActivity(intent)
                        }
                    }
                )
            }
        }
    }
}