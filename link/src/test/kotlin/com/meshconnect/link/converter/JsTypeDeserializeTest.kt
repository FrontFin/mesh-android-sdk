package com.meshconnect.link.converter

import com.meshconnect.link.entity.JsType
import com.meshconnect.link.entity.Type
import org.junit.Test

class JsTypeDeserializeTest {

    private val jsonConverter = JsonConverter

    private fun toJsType(json: String) = jsonConverter.fromJson(json, JsType::class.java)

    @Test
    fun `test JsType done`() {
        val obj = toJsType("{type:'done'}")
        assert(obj == JsType(Type.done))
    }

    @Test
    fun `test JsType close`() {
        val obj = toJsType("{type:'close'}")
        assert(obj == JsType(Type.close))
    }

    @Test
    fun `test JsType showClose`() {
        val obj = toJsType("{type:'showClose'}")
        assert(obj == JsType(Type.showClose))
    }

    @Test
    fun `test JsType error`() {
        val obj = toJsType("{type:'error'}")
        assert(obj == JsType(Type.error))
    }

    @Test
    fun `test JsType brokerageAccountAccessToken`() {
        val obj = toJsType("{type:'brokerageAccountAccessToken'}")
        assert(obj == JsType(Type.brokerageAccountAccessToken))
    }

    @Test
    fun `test JsType transferFinished`() {
        val obj = toJsType("{type:'transferFinished'}")
        assert(obj == JsType(Type.transferFinished))
    }
}