package com.waveproject.tools.encrypt

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

object Md5Utils {
    private fun bytesToHexString(src: ByteArray?): String? {
        val result = StringBuilder("")
        if (src?.isEmpty()==true) {
            return null
        }

        src?.forEach {
            val i = it.toInt()
            //这里需要对b与0xff做位与运算，
            //若b为负数，强制转换将高位位扩展，导致错误，
            //故需要高位清零
            val hexStr = Integer.toHexString(i and 0xff)
            //若转换后的十六进制数字只有一位，
            //则在前补"0"
            if (hexStr.length == 1) {
                result.append(0)
            }
            result.append(hexStr)
        }
        return result.toString()
    }

    fun filePath(path: String?): String? {
        if (path.isNullOrEmpty()) {
            return null
        }
        val digest: MessageDigest?
        var fileIS: FileInputStream? = null
        val buffer = ByteArray(1024)
        var len = 0
        try {
            digest = MessageDigest.getInstance("MD5")
            val oldF = File(path)
            fileIS = FileInputStream(oldF)
            while (fileIS.read(buffer).also { len = it } != -1) {
                digest.update(buffer, 0, len)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }finally {
            fileIS?.close()
        }
        return bytesToHexString(digest?.digest())
    }
}