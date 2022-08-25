package com.waveproject.tools.updater

import android.content.Context
import android.text.TextUtils
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.lang.NullPointerException

object ApkUtils {
    @JvmStatic
    fun getFileNameForUrl(url: String): String {
        if (TextUtils.isEmpty(url)) {
            throw NullPointerException("url is null")
        }
        return url.substring(url.lastIndexOf("/") + 1)
    }

    @JvmStatic
    fun installApk(context: Context, uri: Uri) {
        val file = File(uri.path)
        if (!file.exists()) {
            return
        }
        val intent = Intent()
        val packageName = context.packageName
        val providerUri = FileProvider.getUriForFile(context, "$packageName.fileProvider", file)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.setDataAndType(providerUri, "application/vnd.android.package-archive")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.action = Intent.ACTION_VIEW
        context.startActivity(intent)
    }
}