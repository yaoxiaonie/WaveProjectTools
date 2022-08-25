package com.waveproject.tools.utils

import java.util.*
import java.util.concurrent.locks.LockSupport
import java.util.function.Supplier

class GetThreadResult<T>(
    /**
     * 为了少写一个中间类，这里直接用了函数式接口
     */
    private val supplier: Supplier<T>) : Runnable {
    private var state: State

    /**
     * 获取结果的线程
     */
    private var getResultThread: Thread? = null

    /**
     * 线程结果返回值
     */
    private var res: T? = null
    override fun run() {
        res = supplier.get()
        state = State.COMPLETED
        if (Objects.nonNull(getResultThread)) {
            // unpark 获取结果线程
            LockSupport.unpark(getResultThread)
        }
    }

    fun get(): T? {
        // 如果状态是已完成，则直接返回结果
        if (state == State.COMPLETED) {
            return res
        }
        getResultThread = Thread.currentThread()
        // park 获取结果线程
        LockSupport.park()
        return res
    }

    /**
     * 状态
     */
    internal enum class State {
        /**
         * 初始化
         */
        INIT,

        /**
         * 已完成
         */
        COMPLETED
    }

    init {
        state = State.INIT
    }
}