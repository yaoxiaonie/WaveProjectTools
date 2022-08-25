package com.waveproject.tools.utils

class CallBackResponse {
    fun handler(callback: CallBack, request: String, var1: String?) {
        callback.processingCallback(request, var1)
    }
}