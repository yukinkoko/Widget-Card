package jp.co.tsuqrea.designer_kmp_template.data.db

import app.cash.sqldelight.db.SqlDriver
import jp.co.tsuqrea.wordwidget.db.WordWidgetDatabase

/** プラットフォーム別の SQLDelight ドライバ生成。 */
expect fun createDatabaseDriver(): SqlDriver

fun createDatabase(driver: SqlDriver): WordWidgetDatabase = WordWidgetDatabase(driver)
