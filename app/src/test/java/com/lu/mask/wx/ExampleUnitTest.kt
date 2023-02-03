package com.lu.mask.wx

import com.lu.magic.util.GsonUtil
import com.lu.magic.util.log.LogUtil
import com.lu.wxmask.bean.MaskItemBean
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
//        assertEquals(4, 2 + 2)
//        var jsonText =
//            """[{"maskId":"24594827650@chatroom","tagName":"虫洞 • 技术栈(β区)","temporary":{"clickCount":5,"duration":150},"temporaryMode":0,"tipData":{"mess":"该用户对您私密哦"},"tipMode":0},{"maskId":"9059133959@chatroom","tagName":"相亲相爱学习交流群","temporary":null,"temporaryMode":0,"tipData":{"mess":"该用户对您私密哦"},"tipMode":10086}]"""
        var jsonText =
            """[{"maskId":"9059133959@chatroom","tagName":"相亲相爱学习交流群","tipData":{"mess":"该用户对您私密哦"},"tipMode":10086}]"""
        val beanList = try {
            val typ = GsonUtil.getType(ArrayList::class.java, MaskItemBean::class.java)
            GsonUtil.fromJson<ArrayList<MaskItemBean>>(jsonText, typ)
        } catch (e: Throwable) {
            LogUtil.e(jsonText, e)
            // 不清除配置
            // sp.edit().remove(KEY_MASK_LIST).apply()
            arrayListOf()
        }
        println(beanList)

    }
}