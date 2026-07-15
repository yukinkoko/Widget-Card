package jp.co.tsuqrea.designer_kmp_template.di

import jp.co.tsuqrea.designer_kmp_template.ui.screen.daily.DailyViewModel
import jp.co.tsuqrea.designer_kmp_template.ui.screen.foldercreate.FolderCreateViewModel
import jp.co.tsuqrea.designer_kmp_template.ui.screen.folders.FoldersViewModel
import jp.co.tsuqrea.designer_kmp_template.ui.screen.worddetail.WordDetailViewModel
import jp.co.tsuqrea.designer_kmp_template.ui.screen.wordentry.WordEntryViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * composeApp モジュールの Koin DI 定義。
 * ViewModel を登録する。
 */
val appModule =
    module {
        viewModelOf(::DailyViewModel)
        viewModelOf(::FoldersViewModel)
        viewModelOf(::FolderCreateViewModel)
        viewModelOf(::WordEntryViewModel)
        viewModelOf(::WordDetailViewModel)
    }
