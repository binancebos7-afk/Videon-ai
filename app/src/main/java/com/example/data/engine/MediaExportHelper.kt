package com.example.data.engine

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.domain.model.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object MediaExportHelper {

    private data class ExportTarget(
        val collection: android.net.Uri,
        val mimeType: String,
        val relativePath: String,
        val ext: String
    )

    suspend fun exportToDeviceGallery(
        context: Context,
        sourceFilePath: String,
        title: String,
        mediaType: MediaType
    ): Result<String> = withContext(Dispatchers.IO) {
        val srcFile = File(sourceFilePath)
        if (!srcFile.exists()) {
            return@withContext Result.failure(IllegalStateException("Source file does not exist"))
        }
        return@withContext try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val (collection, mimeType, relativePath, ext) = when (mediaType) {
                    MediaType.VIDEO -> ExportTarget(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        "video/mp4",
                        "${Environment.DIRECTORY_MOVIES}/VideonAIStudio",
                        "mp4"
                    )
                    MediaType.IMAGE -> ExportTarget(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        "image/png",
                        "${Environment.DIRECTORY_PICTURES}/VideonAIStudio",
                        "png"
                    )
                    MediaType.AUDIO -> ExportTarget(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        "audio/wav",
                        "${Environment.DIRECTORY_MUSIC}/VideonAIStudio",
                        "wav"
                    )
                }
                val safeTitle = title.replace(Regex("[^A-Za-z0-9_\\-]"), "_").take(32)
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "${safeTitle}_${System.currentTimeMillis()}.$ext")
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(collection, values)
                    ?: return@withContext Result.failure(IllegalStateException("MediaStore insert failed"))
                resolver.openOutputStream(uri)?.use { out ->
                    srcFile.inputStream().use { input -> input.copyTo(out) }
                }
                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                Result.success(relativePath)
            } else {
                // Zero-permission app-scoped external storage fallback on API < 29
                val destDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.filesDir
                val dest = File(destDir, srcFile.name)
                srcFile.copyTo(dest, overwrite = true)
                Result.success(dest.absolutePath)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun shareFile(context: Context, filePath: String, mediaType: MediaType, title: String) {
        val file = File(filePath)
        if (!file.exists()) return
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val mime = when (mediaType) {
            MediaType.VIDEO -> "video/mp4"
            MediaType.IMAGE -> "image/png"
            MediaType.AUDIO -> "audio/wav"
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
