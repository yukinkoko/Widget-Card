package jp.co.tsuqrea.designer_kmp_template.di

import jp.co.tsuqrea.designer_kmp_template.data.api.HealthApiClient
import jp.co.tsuqrea.designer_kmp_template.data.api.createHttpClient
import jp.co.tsuqrea.designer_kmp_template.data.db.SqlFolderRepository
import jp.co.tsuqrea.designer_kmp_template.data.db.SqlSettingsRepository
import jp.co.tsuqrea.designer_kmp_template.data.db.SqlStatsRepository
import jp.co.tsuqrea.designer_kmp_template.data.db.SqlWordRepository
import jp.co.tsuqrea.designer_kmp_template.data.db.createDatabase
import jp.co.tsuqrea.designer_kmp_template.data.db.createDatabaseDriver
import jp.co.tsuqrea.designer_kmp_template.data.db.seedIfEmpty
import jp.co.tsuqrea.wordwidget.db.WordWidgetDatabase
import jp.co.tsuqrea.designer_kmp_template.domain.repository.FolderRepository
import jp.co.tsuqrea.designer_kmp_template.domain.repository.SettingsRepository
import jp.co.tsuqrea.designer_kmp_template.domain.repository.StatsRepository
import jp.co.tsuqrea.designer_kmp_template.domain.repository.WordRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * shared モジュールの Koin DI 定義。
 * SQLDelight 永続化のリポジトリと、既存の API サンプルを登録する。
 */
val sharedModule =
    module {
        // ── API サンプル（M2 で削除予定） ──
        single { createHttpClient() }
        singleOf(::HealthApiClient)

        // ── DB（SQLDelight）──
        single { createDatabaseDriver() }
        single<WordWidgetDatabase> { createDatabase(get()).also { seedIfEmpty(it) } }

        // ── リポジトリ ──
        single<StatsRepository> { SqlStatsRepository(get()) }
        single<FolderRepository> { SqlFolderRepository(get()) }
        single<WordRepository> { SqlWordRepository(get(), get()) }
        single<SettingsRepository> { SqlSettingsRepository(get()) }

        // ── ウィジェット同期 ──
        single { jp.co.tsuqrea.designer_kmp_template.widget.WidgetSyncer(get(), get(), get()) }

        // ── リマインダー通知 ──
        single { jp.co.tsuqrea.designer_kmp_template.notify.ReminderScheduler(get(), get()) }

        // ── iCloud（KVS）バックアップ同期 ──
        single<jp.co.tsuqrea.designer_kmp_template.backup.BackupRepository> {
            jp.co.tsuqrea.designer_kmp_template.data.db.SqlBackupRepository(get())
        }
        single {
            jp.co.tsuqrea.designer_kmp_template.backup.ICloudSyncer(get(), get(), get(), get())
        }
    }
