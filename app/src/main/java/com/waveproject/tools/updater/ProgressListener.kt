package com.waveproject.tools.updater

interface ProgressListener {
    fun onProgressChange(totalBytes: Long, curBytes: Long, progress: Int)
}