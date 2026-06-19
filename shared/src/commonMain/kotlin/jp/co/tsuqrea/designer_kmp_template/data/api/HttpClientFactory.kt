package jp.co.tsuqrea.designer_kmp_template.data.api

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Ktor HttpClient を生成する。
 * エンジン（OkHttp / Darwin）はクラスパスから自動検出される。
 */
fun createHttpClient(): HttpClient =
    HttpClient {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    prettyPrint = false
                },
            )
        }
    }
