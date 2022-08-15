package com.waveproject.tools.utils

import java.io.IOException

object RootDetector {
    fun getRootStatus(): Int {
        val process: Process = Runtime.getRuntime().exec("su")
        return try {
            process.waitFor()
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        } finally {
            process.destroy()
        }
    }
}