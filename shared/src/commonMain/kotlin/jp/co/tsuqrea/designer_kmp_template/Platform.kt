package jp.co.tsuqrea.designer_kmp_template

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
