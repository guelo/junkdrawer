package miguel.junkdrawer.network

import JunkDrawer.composeApp.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

suspend fun vision(base64Image: String): String {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
        install(Logging) {
            level = LogLevel.BODY
        }
    }

    val response: HttpResponse = client.request("https://api.openai.com/v1/chat/completions") {
        method = HttpMethod.Post
        headers {
            append(HttpHeaders.ContentType, "application/json")
            append(HttpHeaders.Authorization, "Bearer ${BuildConfig.OPENAI_API_KEY}")
        }
        val body = """
{
  "model": "gpt-4o",
  "messages": [
    {
      "role": "user",
      "content": [
        {
          "type": "text",
          "text": "What is in this image?"
        },
        {
          "type": "image_url",
          "image_url": {
            "url": "data:image/jpeg;base64,$base64Image"
          }
        }
      ]
    }
  ],
  "max_tokens": 300
}
            """.trimIndent()
        println(body)
        setBody(body)
    }

    val stringBody: String = response.body()
    client.close()
    val content = Json.Default.parseToJsonElement(stringBody)
        .jsonObject["choices"]!!
        .jsonArray[0]
        .jsonObject["message"]!!
        .jsonObject["content"].toString()

    return content
}
