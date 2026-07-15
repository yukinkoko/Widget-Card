package jp.co.tsuqrea.designer_kmp_template.di

import jp.co.tsuqrea.designer_kmp_template.data.api.HealthApiClient
import jp.co.tsuqrea.designer_kmp_template.data.api.createHttpClient
import jp.co.tsuqrea.designer_kmp_template.data.repository.InMemoryFolderRepository
import jp.co.tsuqrea.designer_kmp_template.data.repository.InMemorySettingsRepository
import jp.co.tsuqrea.designer_kmp_template.data.repository.InMemoryStatsRepository
import jp.co.tsuqrea.designer_kmp_template.data.repository.InMemoryWordRepository
import jp.co.tsuqrea.designer_kmp_template.domain.repository.FolderRepository
import jp.co.tsuqrea.designer_kmp_template.domain.repository.SettingsRepository
import jp.co.tsuqrea.designer_kmp_template.domain.repository.StatsRepository
import jp.co.tsuqrea.designer_kmp_template.domain.repository.WordRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * shared モジュールの Koin DI 定義。
 * リポジトリ（M1a: インメモリ実装）と、既存の API サンプルを登録する。
 * M1b で SQLDelight 実装に差し替える（バインドするインターフェイスは不変）。
 */
val sharedModule =
    module {
        // ── API サンプル（M2 で削除予定） ──
        single { createHttpClient() }
        singleOf(::HealthApiClient)

        // ── リポジトリ（M1a: インメモリ） ──
        single<StatsRepository> { InMemoryStatsRepository() }
        single<FolderRepository> { InMemoryFolderRepository() }
        single<WordRepository> { InMemoryWordRepository(get()) }
        single<SettingsRepository> { InMemorySettingsRepository() }
    }
