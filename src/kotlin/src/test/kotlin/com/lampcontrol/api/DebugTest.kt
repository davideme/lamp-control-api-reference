package com.lampcontrol.api

import com.lampcontrol.api.models.LampCreate
import com.lampcontrol.module
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test

class DebugTest {
    @Test
    fun debugJsonOutput() = testApplication {
        application {
            module()
        }
        
        // Create a lamp
        val lampCreate = LampCreate(status = true)
        val createResponse = client.post("/v1/lamps") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString<LampCreate>(lampCreate))
        }
        
        println("Create response status: ${createResponse.status}")
        val responseBody = createResponse.bodyAsText()
        println("Create response body: $responseBody")
        
        // Try to extract the ID manually
        if (responseBody.contains("\"id\":\"")) {
            val lampIdStart = responseBody.indexOf("\"id\":\"") + 6
            val lampIdEnd = responseBody.indexOf("\"", lampIdStart)
            val lampId = responseBody.substring(lampIdStart, lampIdEnd)
            println("Extracted lamp ID: $lampId")
            
            // Try to get the lamp
            val getResponse = client.get("/v1/lamps/$lampId")
            println("Get response status: ${getResponse.status}")
            println("Get response body: ${getResponse.bodyAsText()}")
        } else {
            println("No id field found in response")
        }
    }
}
