package io.samagra.odk.collect.extension.utilities

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


class ZipCreator{

    fun zipFolders(inputFolderPath: String, outputFile: File, folders: List<String>) {
        ZipOutputStream(FileOutputStream(outputFile)).use { zipOutputStream ->
            for (folder in folders) {
                val folderToZip = File(inputFolderPath, folder)
                zipFolder(folderToZip, folderToZip.name, zipOutputStream)
            }
        }
    }

    private fun zipFolder(folderToZip: File, baseName: String, zipOutputStream: ZipOutputStream) {
        val fileList = folderToZip.listFiles()
        for (file in fileList) {
            if (file.isDirectory) {
                zipFolder(file, "$baseName/${file.name}", zipOutputStream)
            } else {
                if (baseName != "metadata" || file.name == "forms.db") {
                    val fileInputStream = FileInputStream(file)
                    val zipEntry = ZipEntry("$baseName/${file.name}")
                    zipOutputStream.putNextEntry(zipEntry)

                    val bytes = ByteArray(1024)
                    var length: Int
                    while (fileInputStream.read(bytes).also { length = it } >= 0) {
                        zipOutputStream.write(bytes, 0, length)
                    }
                    fileInputStream.close()
                }
            }
        }
    }
}