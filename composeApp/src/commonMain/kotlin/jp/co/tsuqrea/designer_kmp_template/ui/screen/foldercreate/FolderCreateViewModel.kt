package jp.co.tsuqrea.designer_kmp_template.ui.screen.foldercreate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.co.tsuqrea.designer_kmp_template.domain.model.DeadlineTarget
import jp.co.tsuqrea.designer_kmp_template.domain.model.Folder
import jp.co.tsuqrea.designer_kmp_template.domain.model.FolderIcon
import jp.co.tsuqrea.designer_kmp_template.domain.repository.FolderRepository
import jp.co.tsuqrea.designer_kmp_template.platform.todayEpochDay
import kotlinx.coroutines.launch

/** 単語の追加方法。 */
enum class AddMethod { Ai, Manual }

class FolderCreateViewModel(
    private val folderRepository: FolderRepository,
) : ViewModel() {

    /**
     * フォルダを作成して表示中にする。作成後の遷移先は [method]（AI or 手動）で分岐予定だが、
     * 該当画面が未実装のうちは onCreated に id を返して呼び出し側で戻る。
     */
    fun create(
        name: String,
        deadline: DeadlineTarget?,
        icon: FolderIcon,
        onCreated: (folderId: String, method: AddMethod) -> Unit,
        method: AddMethod,
    ) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val folder = folderRepository.create(
                Folder(
                    id = "",
                    name = trimmed,
                    icon = icon,
                    deadline = deadline,
                    isActive = false,
                    createdEpochDay = todayEpochDay(),
                ),
            )
            folderRepository.setActive(folder.id)
            onCreated(folder.id, method)
        }
    }
}
