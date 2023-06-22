package com.lu.wxmask.util.http

import com.lu.wxmask.util.AppUpdateCheckUtil
import org.junit.Test
import java.util.concurrent.CountDownLatch

class HttpConnectUtilTest {

    @Test
    fun get() {
        val lock = CountDownLatch(1)
        HttpConnectUtil.get(AppUpdateCheckUtil.repoLastReleaseUrl) {
            println(it.body.toString(Charsets.UTF_8))
            lock.countDown()
        }
        lock.await()
    }

    @Test
    fun postJson() {
        val lock = CountDownLatch(1)
        HttpConnectUtil.postJson("http://127.0.0.1:80/app/version", """{"尼玛":"123"}""") {
            println(it.body.toString(Charsets.UTF_8))
            println(it.code)
            println(it.error)
            lock.countDown()
        }
        lock.await()
    }

    @Test
    fun getBaidu() {
        val lock = CountDownLatch(1)
        HttpConnectUtil.get("https://www.baidu.com") {
            println(it.body.toString(Charsets.UTF_8))
            lock.countDown()
        }
        lock.await()
    }
}