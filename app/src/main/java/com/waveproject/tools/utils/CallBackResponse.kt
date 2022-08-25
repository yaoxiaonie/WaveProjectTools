package com.waveproject.tools.utils

class CallBackResponse {
    fun handler(callback: CallBack, request: String, var1: String?, var2: String?, var3: String?) {
        callback.processingCallback(request, var1, var2, var3)
    }
}