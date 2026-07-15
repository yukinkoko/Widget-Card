package jp.co.tsuqrea.designer_kmp_template.data.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import jp.co.tsuqrea.wordwidget.db.WordWidgetDatabase

actual fun createDatabaseDriver(): SqlDriver =
    NativeSqliteDriver(WordWidgetDatabase.Schema, "wordwidget.db")
