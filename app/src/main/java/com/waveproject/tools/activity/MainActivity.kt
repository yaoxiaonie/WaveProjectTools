package com.waveproject.tools.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import cn.fkj233.ui.activity.MIUIActivity
import cn.fkj233.ui.activity.view.TextSummaryV
import com.waveproject.tools.R
import com.waveproject.tools.utils.CopyAssetsUtils
import com.waveproject.tools.utils.ShellUtils


@SuppressLint("UseCompatLoadingForDrawables")
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
            Toast.makeText(this, R.string.cannot_get_root, Toast.LENGTH_LONG).show()
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
                            Toast.makeText(this@MainActivity, R.string.no_root, Toast.LENGTH_SHORT)
                                .show()
                        } catch (e: Exception) {
                            Toast.makeText(this@MainActivity, R.string.no_coolapk, Toast.LENGTH_SHORT)
                                .show()
                            val uri = Uri.parse("https://magiskcn.com/magisk-download")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            startActivity(intent)
                        }
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
                            Toast.makeText(this@MainActivity, R.string.pay_attention, Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(this@MainActivity, R.string.no_coolapk, Toast.LENGTH_SHORT).show()
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
                                Toast.makeText(this@MainActivity, R.string.access_failed, Toast.LENGTH_SHORT).show()
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
                                Toast.makeText(this@MainActivity, R.string.access_failed, Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                )
                Line()
                TitleText(getString(R.string.other))
                TextSummaryArrow(
                    TextSummaryV(
                    textId = R.string.opensource,
                    tipsId = R.string.github_url,
                    onClickListener =
                    {
                        try {
                            val uri = Uri.parse("https://github.com/yaoxiaonie/WaveProjectTools")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(this@MainActivity, R.string.access_failed, Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                )
            }
        }
    }
}