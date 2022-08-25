package com.waveproject.tools.encrypt

import android.util.Base64
import java.nio.charset.Charset

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object EncryptUtils {

        /** 加密key  */
        private var PASSWORD_ENC_SECRET = "test"

        //private static final String CipherMode = "AES/ECB/PKCS5Padding";使用ECB加密，不需要设置IV，但是不安全
        private const val CipherMode = "AES/CFB/NoPadding"//使用CFB加密，需要设置IV

        /**
         * 对字符串加密
         * @param data  源字符串
         * @return  加密后的字符串
         */
        fun encrypt(data: String): String {
                checkKeyLength()
                val cipher = Cipher.getInstance(CipherMode)
                val keySpec = SecretKeySpec(PASSWORD_ENC_SECRET.toByteArray(), "AES")
                cipher.init(
                        Cipher.ENCRYPT_MODE, keySpec, IvParameterSpec(
                                ByteArray(cipher.blockSize)
                        )
                )
                val encrypted = cipher.doFinal(data.toByteArray())
                return Base64.encodeToString(encrypted, Base64.DEFAULT)
        }

        /**
         * 对字符串解密
         * @param data  已被加密的字符串
         * @return  解密得到的字符串
         */
        fun decrypt(data: String): String {
                checkKeyLength()
                val encrypted1 = Base64.decode(data.toByteArray(), Base64.DEFAULT)
                val cipher = Cipher.getInstance(CipherMode)
                val keySpec = SecretKeySpec(PASSWORD_ENC_SECRET.toByteArray(), "AES")
                cipher.init(
                        Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(
                                ByteArray(cipher.blockSize)
                        )
                )
                val original = cipher.doFinal(encrypted1)
                return String(original, Charset.forName("UTF-8"))
        }

        /**
         * 检查设定的key值长度
         */
        private fun checkKeyLength() {
                if (PASSWORD_ENC_SECRET.length < 32) {
                        val a = 32 - PASSWORD_ENC_SECRET.length
                        val builder = StringBuilder(PASSWORD_ENC_SECRET)
                        for (i in 0 until a) {
                                builder.append("A")
                        }
                        PASSWORD_ENC_SECRET = builder.toString()
                }
        }
}
