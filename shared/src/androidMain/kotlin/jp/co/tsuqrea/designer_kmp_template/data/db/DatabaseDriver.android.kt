package jp.co.tsuqrea.designer_kmp_template.data.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import jp.co.tsuqrea.wordwidget.db.WordWidgetDatabase

/** Android では起動時に [appContext] をセットしてからDBを使う（例: Application.onCreate）。 */
object AndroidDbContext {
    lateinit var appContext: Context
}

actual fun createDatabaseDriver(): SqlDriver =
    AndroidSqliteDriver(WordWidgetDatabase.Schema, AndroidDbContext.appContext, "wordwidget.db")
