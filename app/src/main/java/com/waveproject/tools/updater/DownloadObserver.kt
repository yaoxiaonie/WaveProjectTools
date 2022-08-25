package com.waveproject.tools.updater

import android.app.DownloadManager
import android.database.ContentObserver
import android.database.Cursor
import android.os.Bundle
import android.os.Handler
import android.os.Message
import java.lang.Exception

class DownloadObserver(
    private val mHandler: Handler,
    private val mDownloadManager: DownloadManager,
    mTaskId: Long
) : ContentObserver(
    mHandler
) {
    private val bundle = Bundle()
    private var message: Message? = null
    private val query: DownloadManager.Query
    private var cursor: Cursor? = null
    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        try {
            cursor = mDownloadManager.query(query)
            if (cursor == null) {
                return
            }
            cursor!!.moveToFirst()
            val curBytes = cursor!!
                .getLong(cursor!!.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
            val totalBytes = cursor!!
                .getLong(cursor!!.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
            val mProgress = (curBytes * 100 / totalBytes).toInt()
            if (totalBytes != 0L) {
                message = mHandler.obtainMessage()
                bundle.putLong(CURBYTES, curBytes)
                bundle.putLong(TOTALBYTES, totalBytes)
                bundle.putInt(PROGRESS, mProgress)
                message!!.data = bundle
                mHandler.sendMessage(message!!)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (cursor != null) {
                cursor!!.close()
            }
        }
    }

    companion object {
        const val CURBYTES = "curBytes"
        const val TOTALBYTES = "totalBytes"
        const val PROGRESS = "progress"
    }

    init {
        query = DownloadManager.Query().setFilterById(mTaskId)
    }
}