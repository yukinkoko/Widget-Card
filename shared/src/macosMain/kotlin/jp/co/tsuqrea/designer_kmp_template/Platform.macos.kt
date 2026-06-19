package jp.co.tsuqrea.designer_kmp_template

import platform.Foundation.NSProcessInfo

class MacOSPlatform : Platform {
    override val name: String =
        "macOS " + NSProcessInfo.processInfo.operatingSystemVersionString
}

actual fun getPlatform(): Platform = MacOSPlatform()
