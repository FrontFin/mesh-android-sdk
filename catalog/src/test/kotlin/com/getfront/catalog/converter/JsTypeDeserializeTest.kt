package com.getfront.catalog.converter

import com.getfront.catalog.entity.JsType
import com.getfront.catalog.entity.Type
import org.junit.Test

class JsTypeDeserializeTest {

    private val gson = JsonConverter.get()

    @Test
    fun `test JsType done`() {
        val obj = gson.fromJson<JsType>("{type:'done'}")
        assert(obj == JsType(Type.done))
    }

    @Test
    fun `test JsType close`() {
        val obj = gson.fromJson<JsType>("{type:'close'}")
        assert(obj == JsType(Type.close))
    }

    @Test
    fun `test JsType showClose`() {
        val obj = gson.fromJson<JsType>("{type:'showClose'}")
        assert(obj == JsType(Type.showClose))
    }

    @Test
    fun `test JsType error`() {
        val obj = gson.fromJson<JsType>("{type:'error'}")
        assert(obj == JsType(Type.error))
    }

    @Test
    fun `test JsType brokerageAccountAccessToken`() {
        val obj = gson.fromJson<JsType>("{type:'brokerageAccountAccessToken'}")
        assert(obj == JsType(Type.brokerageAccountAccessToken))
    }

    @Test
    fun `test JsType transferFinished`() {
        val obj = gson.fromJson<JsType>("{type:'transferFinished'}")
        assert(obj == JsType(Type.transferFinished))
    }
}