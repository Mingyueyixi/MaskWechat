package com.lu.wxmask.util.http

import com.lu.magic.util.thread.AppExecutor
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

//懒得引入各种库，直接基于HttpConnection封装一个简易http工具
class HttpConnectUtil {
    class Response(
        var code: Int,
        var header: Map<String, List<String>>,
        var body: ByteArray,
        var error: Throwable? = null
    )

    class Request(
        var url: String,
        var method: String,
        var header: Map<String, String> = mutableMapOf(),
        var body: ByteArray? = null,
        var connectTimeOut: Int = 5000,
        var readTimeOut: Int = 5000
    ) {
        fun fetch(): Response {
            var connection: HttpURLConnection? = null
            var iStream: InputStream? = null
            var outStream: OutputStream? = null

            val resp = Response(-1, mutableMapOf(), byteArrayOf())
            try {
                connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = method
                connection.connectTimeout = connectTimeOut
                connection.readTimeout = readTimeOut
                header.forEach {
                    connection.setRequestProperty(it.key, it.value)
                }
                body?.let {
                    connection.doOutput = true
                    val out = connection.outputStream
                    outStream = out
                    out.write(it)
                    out.flush()
                }
                resp.code = connection.responseCode
                resp.header = connection.headerFields
                iStream = connection.getInputStream()
                resp.body = iStream.readBytes()
            } catch (e: Throwable) {
                resp.error = e
            } finally {
                try {
                    connection?.disconnect()
                    outStream?.close()
                    iStream?.close()
                } catch (e: Exception) {
                    resp.error = e
                }
            }
            return resp
        }
    }

    companion object {
        @JvmField
        val httpExecutor = ThreadPoolExecutor(8, 16, 0L, TimeUnit.MILLISECONDS, LinkedBlockingQueue());

        @JvmStatic
        fun get(url: String, callback: (resp: Response) -> Unit) {
            httpExecutor.submit {
                Request(url, "GET").fetch().let(callback)
            }
        }

        @JvmStatic
        fun postJson(url: String, json: String, callback: (resp: Response) -> Unit) {
            httpExecutor.submit {
                Request(
                    url,
                    "POST",
                    mutableMapOf("Content-Type" to "application/json; charset=utf-8"),
                    json.toByteArray(Charsets.UTF_8)
                ).fetch().let(callback)
            }
        }
    }
}