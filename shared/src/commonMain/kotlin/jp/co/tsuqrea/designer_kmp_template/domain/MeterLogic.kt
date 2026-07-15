package jp.co.tsuqrea.designer_kmp_template.domain

import jp.co.tsuqrea.designer_kmp_template.domain.model.Word

/**
 * 受け身の表示回数メーター（n / 10 → Learned）のロジック。純関数。
 * 仕様: docs/PRODUCT_SPEC.md §7。
 */
object MeterLogic {
    const val LEARN_THRESHOLD = Word.LEARN_THRESHOLD

    /** 単語が画面に出た（出会った）: 回数を1増やし、閾値到達で Learned 化。 */
    fun onEncounter(word: Word): Word {
        if (word.isLearned) return word
        val next = (word.encounterCount + 1).coerceAtMost(LEARN_THRESHOLD)
        return word.copy(encounterCount = next, isLearned = next >= LEARN_THRESHOLD)
    }

    /** 手動「覚えた」: 即 Learned 化（メーター満タン）。 */
    fun markLearned(word: Word): Word =
        word.copy(encounterCount = LEARN_THRESHOLD, isLearned = true)

    /** 「覚え中」に戻す（詳細でのトグル用・任意）。 */
    fun markLearning(word: Word): Word =
        word.copy(
            isLearned = false,
            encounterCount = word.encounterCount.coerceAtMost(LEARN_THRESHOLD - 1),
        )

    /**
     * ローテーション対象の単語を返す。
     * Learned は [hideLearned] が true のとき除外。表示順で並べる。
     */
    fun rotationCandidates(words: List<Word>, hideLearned: Boolean): List<Word> =
        words
            .filter { !hideLearned || !it.isLearned }
            .sortedBy { it.order }
}
