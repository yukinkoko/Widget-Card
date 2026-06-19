package jp.co.tsuqrea.designer_kmp_template.data.model

import kotlinx.serialization.Serializable

/**
 * httpbin.org/ip のレスポンスモデル。
 * サンプル API 呼び出しのデモ用。
 */
@Serializable
data class HealthResponse(
    val origin: String,
)
