package jp.co.tsuqrea.designer_kmp_template.di

import jp.co.tsuqrea.designer_kmp_template.data.api.HealthApiClient
import jp.co.tsuqrea.designer_kmp_template.data.api.createHttpClient
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * shared モジュールの Koin DI 定義。
 * HTTP クライアントと API クライアントを登録する。
 */
val sharedModule =
    module {
        single { createHttpClient() }
        singleOf(::HealthApiClient)
    }
