package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.JobStatus
import com.example.domain.model.MediaType

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val mode: String,
    val modelId: String,
    val prompt: String,
    val aspectRatio: String = "16:9",
    val resolution: String = "720p",
    val durationSec: Int = 5,
    val outputPath: String? = null,
    val thumbnailResName: String? = null,
    val isFavorite: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "generation_jobs")
data class GenerationJobEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long? = null,
    val title: String,
    val mediaType: MediaType,
    val mode: String,
    val modelId: String,
    val modelName: String,
    val prompt: String,
    val status: JobStatus,
    val progress: Float = 0f,
    val durationSec: Int = 5,
    val resolution: String = "720p",
    val aspectRatio: String = "16:9",
    val fps: Int = 24,
    val seed: Long = 420789L,
    val outputFilePath: String? = null,
    val fileSizeBytes: Long = 0L,
    val errorMessage: String? = null,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val role: String,
    val faceDescription: String,
    val outfitDescription: String,
    val poseReference: String,
    val styleTag: String,
    val consistencySeed: Long,
    val imageUriString: String? = null,
    val drawableResName: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "prompt_templates")
data class PromptTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String,
    val style: String,
    val promptEn: String,
    val promptId: String,
    val cameraHint: String,
    val isFavorite: Boolean = false
)

@Entity(tableName = "download_history")
data class DownloadRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val assetTitle: String,
    val mediaType: MediaType,
    val filePath: String,
    val exportDestination: String,
    val fileSizeBytes: Long,
    val downloadedAt: Long = System.currentTimeMillis()
)
