package jp.co.tsuqrea.designer_kmp_template.ui.screen.foldercreate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.co.tsuqrea.designer_kmp_template.domain.model.DeadlineTarget
import jp.co.tsuqrea.designer_kmp_template.domain.model.Folder
import jp.co.tsuqrea.designer_kmp_template.domain.model.FolderIcon
import jp.co.tsuqrea.designer_kmp_template.domain.model.WordLanguage
import jp.co.tsuqrea.designer_kmp_template.domain.repository.FolderRepository
import jp.co.tsuqrea.designer_kmp_template.platform.todayEpochDay
import kotlinx.coroutines.launch

/** 単語の追加方法。 */
enum class AddMethod { Ai, Manual }

class FolderCreateViewModel(
    private val folderRepository: FolderRepository,
) : ViewModel() {

    /** 編集モードのプリフィル用。 */
    suspend fun getFolder(id: String): Folder? = folderRepository.getFolder(id)

    /**
     * フォルダを作成して表示中にする。作成後の遷移先は [method]（AI or 手動）で分岐予定だが、
     * 該当画面が未実装のうちは onCreated に id を返して呼び出し側で戻る。
     */
    fun create(
        name: String,
        description: String?,
        deadline: DeadlineTarget?,
        icon: FolderIcon,
        language: WordLanguage,
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
                    description = description?.trim()?.ifBlank { null },
                    icon = icon,
                    deadline = deadline,
                    language = language,
                    isActive = false,
                    createdEpochDay = todayEpochDay(),
                ),
            )
            folderRepository.setActive(folder.id)
            onCreated(folder.id, method)
        }
    }

    /** 既存フォルダの編集を保存する（isActive・作成日は保持）。 */
    fun update(
        folderId: String,
        name: String,
        description: String?,
        deadline: DeadlineTarget?,
        icon: FolderIcon,
        language: WordLanguage,
        onSaved: () -> Unit,
    ) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val existing = folderRepository.getFolder(folderId) ?: return@launch
            folderRepository.update(
                existing.copy(
                    name = trimmed,
                    description = description?.trim()?.ifBlank { null },
                    deadline = deadline,
                    icon = icon,
                    language = language,
                ),
            )
            onSaved()
        }
    }
}
