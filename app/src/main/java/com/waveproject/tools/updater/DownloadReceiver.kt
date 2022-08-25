package com.waveproject.tools.updater

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.TextUtils

class DownloadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val bundle = intent.extras ?: return
        val downId = bundle.getLong(DownloadManager.EXTRA_DOWNLOAD_ID, 0)
        //下载完成或点击通知栏
        if (TextUtils.equals(intent.action, DownloadManager.ACTION_DOWNLOAD_COMPLETE) ||
            TextUtils.equals(intent.action, DownloadManager.ACTION_NOTIFICATION_CLICKED)
        ) {
            queryFileUri(context, downId)
        }
    }

    private fun queryFileUri(context: Context, downloadId: Long) {
        val dManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query()
        query.setFilterById(downloadId)
        val c = dManager.query(query)
        if (c != null && c.moveToFirst()) {
            when (c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))) {
                DownloadManager.STATUS_PENDING -> {}
                DownloadManager.STATUS_PAUSED -> {}
                DownloadManager.STATUS_RUNNING -> {}
                DownloadManager.STATUS_SUCCESSFUL -> {}
                DownloadManager.STATUS_FAILED -> {}
            }
            c.close()
        }
    }
}