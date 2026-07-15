package jp.co.tsuqrea.designer_kmp_template

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import jp.co.tsuqrea.designer_kmp_template.data.db.AndroidDbContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // SQLDelight の Android ドライバ用に Context を先に渡す。
        AndroidDbContext.appContext = applicationContext
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}
