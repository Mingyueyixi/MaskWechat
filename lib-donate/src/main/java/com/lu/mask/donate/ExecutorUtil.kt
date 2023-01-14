package com.lu.mask.donate

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors

internal class ExecutorUtil {

    companion object {
        val io = Executors.newCachedThreadPool()
        val main = MainThreadExecutor()
        fun runOnIo(block: Runnable) {
            io.execute(block)
        }

        fun runOnMain(block: Runnable) {
            main.execute(block)
        }
    }

    class MainThreadExecutor : Executor {
        private val mainThreadHandler = Handler(Looper.getMainLooper())
        override fun execute(command: Runnable) {
            if (Thread.currentThread() == Looper.getMainLooper().thread) {
                command.run()
            } else {
                mainThreadHandler.post(command)
            }
        }
    }

}