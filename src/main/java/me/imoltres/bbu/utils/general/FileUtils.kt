package me.imoltres.bbu.utils.general

import java.io.File

object FileUtils {
    @JvmStatic
    fun deleteFolder(folder: File) {
        folder.walkBottomUp().forEach { it.delete() }
    }
}