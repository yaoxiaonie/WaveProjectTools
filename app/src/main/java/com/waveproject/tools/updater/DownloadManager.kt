package com.waveproject.tools.updater

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.text.TextUtils
import com.waveproject.tools.updater.ApkUtils.getFileNameForUrl
import java.io.File

class DownloadManager private constructor(context: Activity) {
    private var fileName: String? = null
    private var filePath: String? = null
    private var dirName: String? = null
    private var title: String? = null
    private var downloadUrl: String? = null
    private val context: Activity?
    private var downloadManager: DownloadManager? = null
    private var mTaskId: Long = 0
    private var hideNotification = false
    private var allowedOverRoaming = false
    private var downloadReceiver: DownloadReceiver? = null
    private var downloadObserver: DownloadObserver? = null
    private var claerCache = false
    private fun download() {
        if (downloadManager == null) {
            downloadManager = context!!.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        }
        val request = DownloadManager.Request(Uri.parse(downloadUrl))
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
        request.setAllowedOverRoaming(allowedOverRoaming)
        request.setTitle(if (TextUtils.isEmpty(title)) fileName else title)
        request.setNotificationVisibility(if (hideNotification) DownloadManager.Request.VISIBILITY_HIDDEN else DownloadManager.Request.VISIBILITY_VISIBLE)
        if (TextUtils.isEmpty(fileName)) {
            fileName = getFileNameForUrl(downloadUrl!!)
        }

        //设置下载路径
        if (TextUtils.isEmpty(filePath) && TextUtils.isEmpty(dirName)) {
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        } else if (!TextUtils.isEmpty(dirName)) {
            request.setDestinationInExternalPublicDir(dirName, fileName)
        } else {
            val fileAbsPath = filePath + File.separator + fileName
            request.setDestinationUri(Uri.fromFile(File(fileAbsPath)))
        }

        //将下载请求加入下载队列
        //加入下载队列后会给该任务返回一个long型的id，
        //通过该id可以取消任务，重启任务等等
        mTaskId = downloadManager!!.enqueue(request)
    }

    private var listeners: ArrayList<ProgressListener>? = null

    /**
     * 添加下载进度回调
     */
    fun addProgressListener(progressListener: ProgressListener) {
        if (listeners == null) {
            listeners = ArrayList()
        }
        if (!listeners!!.contains(progressListener)) {
            listeners!!.add(progressListener)
        }
        if (downloadObserver == null && handler != null && downloadManager != null) {
            downloadObserver = DownloadObserver(handler, downloadManager!!, mTaskId)
            context!!.contentResolver.registerContentObserver(
                Uri.parse("content://downloads/"),
                true, downloadObserver!!
            )
        }
    }

    /**
     * 移除下载进度回调
     */
    fun removeProgressListener(progressListener: ProgressListener) {
        if (!listeners!!.contains(progressListener)) {
            throw NullPointerException("this progressListener not attch Updater")
        }
        if (listeners != null && listeners!!.isNotEmpty()) {
            listeners!!.remove(progressListener)
            if (listeners!!.isEmpty() && downloadObserver != null) context!!.contentResolver.unregisterContentObserver(
                downloadObserver!!
            )
        }
    }

    private val handler: Handler = Handler { msg ->
        val data = msg.data
        val cutBytes = data.getLong(DownloadObserver.CURBYTES)
        val totalBytes = data.getLong(DownloadObserver.TOTALBYTES)
        val progress = data.getInt(DownloadObserver.PROGRESS)
        if (listeners != null && listeners!!.isNotEmpty()) {
            for (listener in listeners!!) {
                listener.onProgressChange(totalBytes, cutBytes, progress)
            }
        }
        false
    }

    class Builder(context: Activity) {
        private var mDownloadManager: com.waveproject.tools.updater.DownloadManager? = null

        /**
         * 设置下载下来的文件名
         *
         * @param fileName 文件的名字
         * @return
         */
        fun setfileName(fileName: String?): Builder {
            mDownloadManager!!.fileName = fileName
            return this
        }

        /**
         * 设置下载的路径
         *
         * @param filePath 自定义的全路径
         * @return
         */
        fun setFilePath(filePath: String?): Builder {
            mDownloadManager!!.filePath = filePath
            return this
        }

        /**
         * 设置下载的文件目录
         *
         * @param dirName sd卡的文件夹名字
         * @return
         */
        fun setFileDir(dirName: String?): Builder {
            mDownloadManager!!.dirName = dirName
            return this
        }

        /**
         * 设置下载的链接地址
         *
         * @param downloadUrl 文件的下载链接
         * @return
         */
        fun setDownloadUrl(downloadUrl: String?): Builder {
            mDownloadManager!!.downloadUrl = downloadUrl
            return this
        }

        /**
         * 通知栏显示的标题
         *
         * @param title 标题
         * @return
         */
        fun setNotificationTitle(title: String?): Builder {
            mDownloadManager!!.title = title
            return this
        }

        /**
         * 隐藏通知栏
         *
         * @return
         */
        fun hideNotification(): Builder {
            mDownloadManager!!.hideNotification = true
            return this
        }

        /**
         * 允许漫游网络可下载
         *
         * @return
         */
        fun allowedOverRoaming(): Builder {
            mDownloadManager!!.allowedOverRoaming = true
            return this
        }

        fun clearCache(): Builder {
            mDownloadManager!!.claerCache = true
            return this
        }

        /**
         * 开始下载
         *
         * @return
         */
        fun start(): com.waveproject.tools.updater.DownloadManager? {
            mDownloadManager!!.download()
            return mDownloadManager
        }

        init {
            synchronized(DownloadManager::class.java) {
                if (mDownloadManager == null) {
                    synchronized(DownloadManager::class.java) { mDownloadManager =
                        DownloadManager(context)
                    }
                }
            }
        }
    }

    init {
        this.context = context
    }
}