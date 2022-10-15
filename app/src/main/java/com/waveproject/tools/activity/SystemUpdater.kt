package com.waveproject.tools.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.MenuInflater
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cloud.progressbar.ProgressButton
import com.hjq.permissions.IPermissionInterceptor
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.jaredrummler.ktsh.Shell
import com.waveproject.tools.R
import com.waveproject.tools.data.DataConst.GET_DEVICE_NAME
import com.waveproject.tools.data.DataConst.GET_DEVICE_VERSION
import com.waveproject.tools.data.DataConst.GET_UPDATE_JSON
import com.waveproject.tools.encrypt.Md5Utils
import com.waveproject.tools.updater.DownloadManager
import com.waveproject.tools.updater.ProgressListener
import com.waveproject.tools.utils.CallBack
import com.waveproject.tools.utils.CallBackResponse
import com.waveproject.tools.utils.GetThreadResult
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class SystemUpdater: AppCompatActivity(), CallBack {
    private val suShell = Shell.SU
    private val shShell = Shell.SH
    private val deviceName = shShell.run(GET_DEVICE_NAME).stdout()
    private val deviceVersion = shShell.run(GET_DEVICE_VERSION).stdout()
    private var checkUpdateThreadOnStart: Thread? = null
    private var downloadEntireUpdate: Thread? = null
    private var checkUpdateThread: Thread? = null
    private var checkUpdateMd5Thread: Thread? = null
    // Json读取
    private var jsonContent: Shell.Command.Result? = null
    private var jsonObject: JSONObject? = null
    private var zipName: String? = null
    private var zipVersion: String? = null
    private var zipUrl: String? = null
    private var zipMd5: String? = null
    private var zipSize: String? = null
    private var zipLatestNotice: String? = null
    private var zipHistoryNotice: String? = null
    private var zipFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.system_updater)
        getVersion()
        checkUpdateThreadOnStart = Thread {
            XXPermissions.setInterceptor(object : IPermissionInterceptor {})
            XXPermissions.with(this@SystemUpdater)
                .permission(Permission.MANAGE_EXTERNAL_STORAGE)
                .request(object: OnPermissionCallback {
                    override fun onGranted(permissions: MutableList<String>, all: Boolean) {
                        if (!all) {
                            return
                        }
                    }
                })
            // 设置正在检查更新文字
            setTvCheckingUpdate("visible")
            // Json读取
            jsonContent = shShell.run(GET_UPDATE_JSON + "device=$deviceName&&version=$deviceVersion\"")
            if (jsonContent!!.stdout() == "error") {
                this.runOnUiThread {
                    // 正在检查更新文字渐隐
                    setTvCheckingUpdate("gone")
                    // 设备不支持更新提示
                    setTvDeviceUnsupport()
                }
            } else if (!jsonContent!!.isSuccess) {
                this.runOnUiThread {
                    // 未接入互联网提示
                    setTvNoInternet()
                    // 正在检查更新文字渐隐
                    setTvCheckingUpdate("gone")
                    // 检查更新按钮出现
                    setBtnCheckingUpdate("visible")
                }
            } else {
                jsonObject = JSONObject(jsonContent!!.stdout())
                zipName = jsonObject!!.getString("zipName")
                zipVersion = jsonObject!!.getString("zipVersion")
                zipUrl = getRedirectUrl(jsonObject!!.getString("zipUrl"))
                zipMd5 = jsonObject!!.getString("zipMd5")
                zipSize = jsonObject!!.getString("zipSize")
                zipLatestNotice = jsonObject!!.getString("zipLatestNotice")
                zipHistoryNotice = jsonObject!!.getString("zipHistoryNotice")
                zipFile = File(Environment.getExternalStorageDirectory().path + "/" + Environment.DIRECTORY_DOWNLOADS + "/" + zipName)
                this.runOnUiThread {
                    // 本地版本和云端版本比较
                    if (zipVersion == deviceVersion) {
                        this.runOnUiThread {
                            Toast.makeText(this@SystemUpdater, R.string.already_up_to_date, Toast.LENGTH_LONG).show()
                            // 正在检查更新文字渐隐
                            setTvCheckingUpdate("gone")
                            // 检查更新按钮出现
                            setBtnCheckingUpdate("visible")
                        }
                    } else if (zipVersion !== deviceVersion) {
                        this.runOnUiThread {
                            Toast.makeText(this@SystemUpdater, R.string.has_update, Toast.LENGTH_SHORT).show()
                            // 正在检查更新文字渐隐
                            setTvCheckingUpdate("gone")
                        }
                        // 检测本地是否有已经完成下载的文件，如果有就验证MD5
                        val zipFile = File(Environment.getExternalStorageDirectory().path + "/" + Environment.DIRECTORY_DOWNLOADS + "/" + zipName)
                        if (zipFile.exists()) {
                            // 验证MD5
                            this.runOnUiThread {
                                Toast.makeText(this@SystemUpdater, R.string.local_has_update, Toast.LENGTH_SHORT).show()
                                setBtnCheckingUpdate("md5")
                            }
                            if (Md5Utils.filePath(zipFile.toString()) == zipMd5) {
                                this.runOnUiThread {
                                    // 检查更新按钮隐藏
                                    setBtnCheckingUpdate("gone")
                                    // 重启立即更新按钮出现
                                    setBtnRebootToUpdate("visible")
                                    // 上移动画
                                    upAnimation(zipVersion.toString(), zipSize.toString(), zipLatestNotice.toString())
                                }
                            } else {
                                this.runOnUiThread {
                                    // 校验失败Toast
                                    Toast.makeText(this@SystemUpdater, R.string.verifying_downloaded_updates_failed, Toast.LENGTH_SHORT).show()
                                    // 检查更新按钮隐藏
                                    setBtnCheckingUpdate("gone")
                                    // 下载更新按钮出现
                                    setBtnDownloadUpdate("visible")
                                    // 上移动画
                                    upAnimation(zipVersion.toString(), zipSize.toString(), zipLatestNotice.toString())
                                }
                            }
                        } else {
                            this.runOnUiThread {
                                // 检查更新按钮隐藏
                                setBtnCheckingUpdate("gone")
                                // 下载更新按钮出现
                                setBtnDownloadUpdate("visible")
                                // 上移动画
                                upAnimation(zipVersion.toString(), zipSize.toString(), zipLatestNotice.toString())
                            }
                        }
                    }
                }
            }
        }
        checkUpdateThreadOnStart!!.name = "checkUpdateThreadOnStart"
        checkUpdateThreadOnStart!!.start()
    }

    fun backMain(view : View){
        super.onBackPressed()
    }

    fun enterHistoryAnnouncement (view: View) {
        val intent = Intent(this, HistoryAnnouncement::class.java)
        val bundle = Bundle()
        bundle.putCharSequence("deviceVersion", deviceVersion)
        bundle.putCharSequence("historyAnnouncement", zipHistoryNotice)
        intent.putExtras(bundle)
        startActivity(intent)
    }

    fun showPopup(view : View){
        val popup = PopupMenu(this, view)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.menu, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId){
                R.id.download_entire_update-> {
                    val btnDownloadUpdateProgress: ProgressButton?
                    btnDownloadUpdateProgress = findViewById(R.id.download_update)
                    downloadEntireUpdate = Thread {
                        if (jsonContent!!.stdout() == "error") {
                            this.runOnUiThread {
                                // 设备不支持更新提示
                                setTvDeviceUnsupport()
                            }
                        } else if (!jsonContent!!.isSuccess) {
                            this.runOnUiThread {
                                // 未接入互联网提示
                                setTvNoInternet()
                            }
                        } else {
                            // 检测本地是否有已经完成下载的文件，如果有就验证MD5
                            if (zipFile!!.exists()) {
                                // 验证MD5
                                this.runOnUiThread {
                                    Toast.makeText(this@SystemUpdater, R.string.local_has_update, Toast.LENGTH_SHORT).show()
                                    setBtnCheckingUpdate("md5")
                                }
                                if (Md5Utils.filePath(zipFile.toString()) == zipMd5) {
                                    this.runOnUiThread {
                                        // 检查更新按钮隐藏
                                        setBtnCheckingUpdate("gone")
                                        // 重启立即更新按钮出现
                                        setBtnRebootToUpdate("visible")
                                        // 上移动画
                                        upAnimation(zipVersion!!, zipSize!!, zipLatestNotice!!)
                                    }
                                } else {
                                    this.runOnUiThread {
                                        // 校验失败Toast
                                        Toast.makeText(this@SystemUpdater, R.string.verifying_downloaded_updates_failed, Toast.LENGTH_SHORT).show()
                                        // 检查更新按钮隐藏
                                        setBtnCheckingUpdate("gone")
                                        // 上移动画
                                        upAnimation(zipVersion!!, zipSize!!, zipLatestNotice!!)
                                        // 下载更新按钮出现
                                        setBtnDownloadUpdate("visible")
                                        val zipStatus = DownloadManager.Builder(this@SystemUpdater)
                                            .setDownloadUrl(zipUrl)
                                            .setfileName(zipName)
                                            .setNotificationTitle(zipName)
                                            .start()
                                        zipStatus!!.addProgressListener(object: ProgressListener {
                                            @SuppressLint("SetTextI18n")
                                            override fun onProgressChange(totalBytes: Long, curBytes: Long, progress: Int) {
                                                btnDownloadUpdateProgress.setProgress(progress)
                                                btnDownloadUpdateProgress.text = "$progress%"
                                                if (totalBytes == curBytes) {
                                                    checkUpdateMd5(zipFile.toString(), zipMd5!!)
                                                }
                                            }
                                        })
                                        setBtnDownloadUpdate("cannotClick")
                                    }
                                }
                            } else {
                                this.runOnUiThread {
                                    // 检查更新按钮隐藏
                                    setBtnCheckingUpdate("gone")
                                    // 上移动画
                                    upAnimation(zipVersion!!, zipSize!!, zipLatestNotice!!)
                                    // 下载更新按钮出现
                                    setBtnDownloadUpdate("visible")
                                    val zipStatus = DownloadManager.Builder(this@SystemUpdater)
                                        .setDownloadUrl(zipUrl)
                                        .setfileName(zipName)
                                        .setNotificationTitle(zipName)
                                        .start()
                                    zipStatus!!.addProgressListener(object : ProgressListener {
                                        @SuppressLint("SetTextI18n")
                                        override fun onProgressChange(
                                            totalBytes: Long,
                                            curBytes: Long,
                                            progress: Int
                                        ) {
                                            btnDownloadUpdateProgress.setProgress(progress)
                                            btnDownloadUpdateProgress.text = "$progress%"
                                            if (totalBytes == curBytes) {
                                                checkUpdateMd5(zipFile.toString(), zipMd5!!)
                                            }
                                        }
                                    })
                                    setBtnDownloadUpdate("cannotClick")
                                }
                            }
                        }
                    }
                    downloadEntireUpdate!!.name = "checkUpdateThread"
                    downloadEntireUpdate!!.start()
                }
                R.id.reboot_to_recovery-> {
                    suShell.run("reboot recovery")
                }
                R.id.history_announcement_menu-> {
                    val intent = Intent(this, HistoryAnnouncement::class.java)
                    val bundle = Bundle()
                    bundle.putCharSequence("deviceVersion", deviceVersion)
                    bundle.putCharSequence("historyAnnouncement", zipHistoryNotice)
                    intent.putExtras(bundle)
                    startActivity(intent)
                }
            }
            true
        }
        popup.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (checkUpdateThread != null) {
            checkUpdateThread!!.interrupt()
        }
        if (checkUpdateMd5Thread != null) {
            checkUpdateMd5Thread!!.interrupt()
        }
    }

    // 子线程回调主线程
    override fun processingCallback(result: String?, var1: String?) {
        when (result) {
            // 下载进度条按钮
            "setBtnDownloadUpdateProgress" -> {
                this.runOnUiThread {
                    setBtnDownloadUpdateProgress(var1!!)
                }
            }
            // 重启立即更新按钮
            "setBtnRebootToUpdate" -> {
                this.runOnUiThread {
                    setBtnRebootToUpdate(var1!!)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun getVersion() {
        // 设定MIUI版本号
        val tvMiuiVersion: TextView = findViewById(R.id.miui_version)
        val miuiVersion = deviceVersion
        tvMiuiVersion.text = "$miuiVersion WaveProject"
    }

    @SuppressLint("SetTextI18n")
    fun upAnimation(zipVersion: String, zipSize: String, zipNotice: String) {
        // 正在检查更新立即消失
        val tvCheckingUpdate: TextView = findViewById(R.id.checking_update)
        tvCheckingUpdate.visibility = View.GONE
        // 历史版本公告按钮消失
        val btnHistoryAnnouncement: View = findViewById(R.id.history_announcement)
        btnHistoryAnnouncement.visibility = View.GONE
        // MIUI Version Logo上移
        val phoMiuiVersionLogo: ImageView = findViewById(R.id.miui_version_logo)
        phoMiuiVersionLogo.animate().translationY(-280F)
        // MIUI Text Logo上移
        val phoMiuiTextLogo: ImageView = findViewById(R.id.miui_version_text)
        phoMiuiTextLogo.animate().translationY(-280F)
        // 新的版本号设置
        val tvMiuiVersion: TextView = findViewById(R.id.miui_version)
        tvMiuiVersion.text = "$zipVersion WaveProject $zipSize"
        // MIUI Version Text上移
        tvMiuiVersion.animate().translationY(-470F)
        // 分割线出现并上移
        val textLine: View = findViewById(R.id.text_line)
        textLine.visibility = View.VISIBLE
        textLine.animate().translationY(-1260F)
        // other图标出现
        val phoOtherLogo: ImageView = findViewById(R.id.other_update_logo)
        phoOtherLogo.visibility = View.VISIBLE
        phoOtherLogo.animate().translationY(-150F)
        // '其他'文字出现
        val tvOtherUpdate: TextView = findViewById(R.id.other_update_text)
        tvOtherUpdate.visibility = View.VISIBLE
        // '其他'文字上移
        tvOtherUpdate.animate().translationY(-235F)
        // 更新内容出现
        val tvUpdateContent: TextView = findViewById(R.id.update_content)
        tvUpdateContent.visibility = View.VISIBLE
        // 更新内容上移
        tvUpdateContent.animate().translationY(-260F)
        tvUpdateContent.text = zipNotice
    }

    private fun setTvDeviceUnsupport() {
        val tvNetworkError: TextView = findViewById(R.id.network_error)
        tvNetworkError.text = getString(R.string.device_unsupport)
        tvNetworkError.alpha = 1F
    }

    private fun setTvNoInternet() {
        val tvNetworkError: TextView = findViewById(R.id.network_error)
        tvNetworkError.text = getString(R.string.no_internet)
        tvNetworkError.alpha = 1F
    }

    private fun setTvCheckingUpdate(switch: String) {
        val tvCheckingUpdate: TextView = findViewById(R.id.checking_update)
        when (switch) {
            "visible" -> {
                // 正在检查更新文字出现
                tvCheckingUpdate.alpha = 1F
                tvCheckingUpdate.visibility = View.VISIBLE
            }

            "gone" -> {
                // 正在检查更新文字渐隐
                tvCheckingUpdate.animate().alpha(0F).setDuration(1500).start()
            }
        }
    }

    private fun setBtnCheckingUpdate(switch: String) {
        val btnCheckUpdate: TextView = findViewById(R.id.check_update)
        when (switch) {
            "visible" -> {
                // 检查更新按钮出现
                btnCheckUpdate.visibility = View.VISIBLE
            }

            "gone" -> {
                // 检查更新按钮隐藏
                btnCheckUpdate.visibility = View.GONE
            }

            "md5" -> {
                // 校验更新按钮 设置不可点击
                btnCheckUpdate.text = getString(R.string.verifying_downloaded_updates)
                btnCheckUpdate.isEnabled = false
            }
        }
    }

    private fun setBtnRebootToUpdate(switch: String) {
        val btnRebootToUpdate: View = findViewById(R.id.reboot_to_update)
        when (switch) {
            "visible" -> {
                // 重启立即更新按钮出现
                btnRebootToUpdate.visibility = View.VISIBLE
            }

            "gone" -> {
                // 重启立即更新按钮隐藏
                btnRebootToUpdate.visibility = View.GONE
            }
        }
    }

    private fun setBtnDownloadUpdate(switch: String) {
        val btnDownloadUpdate: TextView = findViewById(R.id.download_update)
        when (switch) {
            "visible" -> {
                // 下载更新按钮出现
                btnDownloadUpdate.visibility = View.VISIBLE
            }

            "gone" -> {
                // 下载更新按钮隐藏
                btnDownloadUpdate.visibility = View.GONE
            }

            "cannotClick" -> {
                // 下载更新按钮设置不可点击
                btnDownloadUpdate.isEnabled = false
            }
        }
    }

    private fun setBtnDownloadUpdateProgress(switch: String) {
        val btnDownloadUpdateProgress: ProgressButton?
        btnDownloadUpdateProgress = findViewById(R.id.download_update)
        when (switch) {
            "visible" -> {
                // 下载进度条出现
                btnDownloadUpdateProgress.visibility = View.VISIBLE
            }

            "gone" -> {
                // 下载进度条隐藏
                btnDownloadUpdateProgress.visibility = View.GONE
            }

            "text" -> {
                // 校验更新文字出现
                btnDownloadUpdateProgress.text = getString(R.string.verifying_downloaded_updates)
            }

            "failed" -> {
                // 校验更新文字出现
                btnDownloadUpdateProgress.text = getString(R.string.verifying_downloaded_updates_failed)
            }
        }
    }

    fun checkUpdateMd5(uFile: String, cMd5: String) {
        checkUpdateMd5Thread = Thread {
            // 校验更新文字出现
            CallBackResponse().handler(this@SystemUpdater, "setBtnDownloadUpdateProgress", "text")
            if (Md5Utils.filePath(uFile) == cMd5) {
                // 进度条消失
                CallBackResponse().handler(this@SystemUpdater, "setBtnDownloadUpdateProgress", "gone")
                // 重启立即更新按钮出现
                CallBackResponse().handler(this@SystemUpdater, "setBtnRebootToUpdate", "visible")
            } else {
                CallBackResponse().handler(this@SystemUpdater, "btnDownloadUpdateProgress", "failed")
            }
        }
        checkUpdateMd5Thread!!.name = "checkUpdateMd5Thread"
        checkUpdateMd5Thread!!.start()
    }

    fun checkUpdate(view: View) {
        // 设置正在检查更新文字
        setTvCheckingUpdate("visible")
        checkUpdateThread = Thread {
            if (jsonContent!!.stdout() == "error") {
                this.runOnUiThread {
                    // 正在检查更新文字渐隐
                    setTvCheckingUpdate("gone")
                    // 设备不支持更新提示
                    setTvDeviceUnsupport()
                }
            } else if (!jsonContent!!.isSuccess) {
                this.runOnUiThread {
                    // 正在检查更新文字渐隐
                    setTvCheckingUpdate("gone")
                    // 未接入互联网提示
                    setTvNoInternet()
                }
            } else {
                // 本地版本和云端版本比较
                if (zipVersion == deviceVersion) {
                    this.runOnUiThread {
                        Toast.makeText(this@SystemUpdater, R.string.already_up_to_date, Toast.LENGTH_LONG).show()
                        // 正在检查更新文字渐隐
                        setTvCheckingUpdate("gone")
                    }
                } else if (zipVersion !== deviceVersion) {
                    this.runOnUiThread {
                        Toast.makeText(this@SystemUpdater, R.string.has_update, Toast.LENGTH_SHORT).show()
                        // 正在检查更新文字渐隐
                        setTvCheckingUpdate("gone")
                    }
                    // 检测本地是否有已经完成下载的文件，如果有就验证MD5
                    val zipFile = File(Environment.getExternalStorageDirectory().path + "/" + Environment.DIRECTORY_DOWNLOADS + "/" + zipName)
                    if (zipFile.exists()) {
                        // 验证MD5
                        this.runOnUiThread {
                            Toast.makeText(this@SystemUpdater, R.string.local_has_update, Toast.LENGTH_SHORT).show()
                            setBtnCheckingUpdate("md5")
                        }
                        if (Md5Utils.filePath(zipFile.toString()) == zipMd5) {
                            this.runOnUiThread {
                                // 检查更新按钮隐藏
                                setBtnCheckingUpdate("gone")
                                // 重启立即更新按钮出现
                                setBtnRebootToUpdate("visible")
                                // 上移动画
                                upAnimation(zipVersion!!, zipSize!!, zipLatestNotice!!)
                            }
                        } else {
                            this.runOnUiThread {
                                // 校验失败Toast
                                Toast.makeText(this@SystemUpdater, R.string.verifying_downloaded_updates_failed, Toast.LENGTH_SHORT).show()
                                // 检查更新按钮隐藏
                                setBtnCheckingUpdate("gone")
                                // 下载更新按钮出现
                                setBtnDownloadUpdate("visible")
                                // 上移动画
                                upAnimation(zipVersion!!, zipSize!!, zipLatestNotice!!)
                            }
                        }
                    } else {
                        this.runOnUiThread {
                            // 检查更新按钮隐藏
                            setBtnCheckingUpdate("gone")
                            // 下载更新按钮出现
                            setBtnDownloadUpdate("visible")
                            // 上移动画
                            upAnimation(zipVersion.toString(), zipSize.toString(), zipLatestNotice.toString())
                        }
                    }
                }
            }
        }
        checkUpdateThread!!.name = "checkUpdateThread"
        checkUpdateThread!!.start()
    }

    @Throws(Exception::class)
    fun getRedirectUrl(urlStr: String): String {
        val realUrl = GetThreadResult {
            try {
                val conn = URL(urlStr)
                    .openConnection() as HttpURLConnection
                conn.instanceFollowRedirects = false
                conn.connectTimeout = 5000
                conn.getHeaderField("Location")
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        val thread = Thread(realUrl, "getRealUrl")
        thread.start()
        return realUrl.get().toString()
    }

    fun downloadUpdate(view: View) {
        val btnDownloadUpdateProgress: ProgressButton?
        btnDownloadUpdateProgress = findViewById(R.id.download_update)
        val zipStatus = DownloadManager.Builder(this@SystemUpdater)
            .setDownloadUrl(zipUrl)
            .setfileName(zipName)
            .setNotificationTitle(zipName)
            .start()
        zipStatus!!.addProgressListener(object: ProgressListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChange(totalBytes: Long, curBytes: Long, progress: Int) {
                btnDownloadUpdateProgress.setProgress(progress)
                btnDownloadUpdateProgress.text = "$progress%"
                if (totalBytes == curBytes) {
                    checkUpdateMd5(zipFile.toString(), zipMd5!!)
                }
            }
        })
        setBtnDownloadUpdate("cannotClick")
    }

    fun rebootToUpdate(view: View) {
        suShell.run("mkdir -p /data/ota_cache/recovery && echo 'install $zipFile' >/data/ota_cache/recovery/openrecoveryscript && reboot recovery")
        suShell.run("mkdir -p /data/ota_cache/recovery && echo '--update_package=$zipFile' >/data/ota_cache/recovery/command && reboot recovery")
    }
}

