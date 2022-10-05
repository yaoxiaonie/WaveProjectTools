package com.waveproject.tools.activity

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.waveproject.tools.R

class HistoryAnnouncement: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.history_announcement)
        val intent = intent
        val bundle = intent.extras
        val deviceVersion = bundle!!.getString("deviceVersion")
        val historyAnnouncement = bundle!!.getString("historyAnnouncement")
        // 设定MIUI版本号
        val tvMiuiVersion: TextView = findViewById(R.id.miui_version)
        val miuiVersion = deviceVersion
        tvMiuiVersion.text = "$miuiVersion WaveProject"
        // 设定历史版本更新日志内容
        val tvUpdateContent: TextView = findViewById(R.id.update_content)
        tvUpdateContent.text = historyAnnouncement
    }
}