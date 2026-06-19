package jp.co.tsuqrea.designer_kmp_template.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import jp.co.tsuqrea.designer_kmp_template.data.model.HealthResponse

/**
 * ヘルスチェック用 API クライアント。
 * httpbin.org を使ったサンプル実装。
 */
class HealthApiClient(
    private val httpClient: HttpClient,
) {
    /**
     * IP アドレスを取得するサンプル API 呼び出し。
     * 実際のプロジェクトでは自前の API エンドポイントに差し替える。
     */
    suspend fun checkHealth(): HealthResponse = httpClient.get("https://httpbin.org/ip").body()
}
