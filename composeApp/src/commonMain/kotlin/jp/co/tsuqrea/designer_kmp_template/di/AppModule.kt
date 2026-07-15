package jp.co.tsuqrea.designer_kmp_template.di

import jp.co.tsuqrea.designer_kmp_template.ui.screen.daily.DailyViewModel
import jp.co.tsuqrea.designer_kmp_template.ui.screen.worddetail.WordDetailViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * composeApp モジュールの Koin DI 定義。
 * ViewModel を登録する。
 */
val appModule =
    module {
        viewModelOf(::DailyViewModel)
        viewModelOf(::WordDetailViewModel)
    }
