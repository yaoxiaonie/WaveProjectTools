package com.waveproject.tools.utils

import android.app.Activity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object CopyAssetsUtils {
    /**
     * 从assets目录中复制整个文件夹内容,考贝到 /data/data/包名/files/目录中
     * @param  activity  activity 使用CopyFiles类的Activity
     * @param  filePath  String  文件路径,如：/assets/aa
     */
    fun copyAssetsDir2Phone(activity: Activity, filePath: String) {
        var filePath = filePath
        try {
            val fileList = activity.assets.list(filePath)
            if (fileList!!.isNotEmpty()) { //如果是目录
                val file = File(activity.filesDir.absolutePath + File.separator + filePath)
                file.mkdirs() //如果文件夹不存在，则递归
                for (fileName in fileList) {
                    filePath = filePath + File.separator + fileName
                    copyAssetsDir2Phone(activity, filePath)
                    filePath = filePath.substring(0, filePath.lastIndexOf(File.separator))
                }
            } else { //如果是文件
                val inputStream = activity.assets.open(filePath)
                val file = File(activity.filesDir.absolutePath + File.separator + filePath)
                if (!file.exists() || file.length() == 0L) {
                    val fos = FileOutputStream(file)
                    var len = -1
                    val buffer = ByteArray(1024)
                    while (inputStream.read(buffer).also { len = it } != -1) {
                        fos.write(buffer, 0, len)
                    }
                    fos.flush()
                    inputStream.close()
                    fos.close()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 将文件从assets目录，考贝到 /data/data/包名/files/ 目录中。assets 目录中的文件，会不经压缩打包至APK包中，使用时还应从apk包中导出来
     * @param fileName 文件名,如aaa.txt
     */
    fun copyAssetsFile2Phone(activity: Activity, fileName: String) {
        try {
            val inputStream = activity.assets.open(fileName)
            //getFilesDir() 获得当前APP的安装路径 /data/data/包名/files 目录
            val file = File(activity.filesDir.absolutePath + File.separator + fileName)
            if (!file.exists() || file.length() == 0L) {
                val fos = FileOutputStream(file) //如果文件不存在，FileOutputStream会自动创建文件
                var len = -1
                val buffer = ByteArray(1024)
                while (inputStream.read(buffer).also { len = it } != -1) {
                    fos.write(buffer, 0, len)
                }
                fos.flush() //刷新缓存区
                inputStream.close()
                fos.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}