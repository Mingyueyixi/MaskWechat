package com.lu.mask.wx

import org.json.JSONML
import org.json.JSONObject
import org.json.JSONString
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
        val k = JSONObject("{}").optString("key")
        println(k)
    }
}