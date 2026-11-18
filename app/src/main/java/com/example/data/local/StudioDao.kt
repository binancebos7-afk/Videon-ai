package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.domain.model.JobStatus
import com.example.domain.model.MediaType
import kotlinx.coroutines.flow.Flow

@Dao
interface StudioDao {
    // Projects
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun observeProjects(): Flow<List<ProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProject(id: Long)

    // Generation Jobs
    @Query("SELECT * FROM generation_jobs ORDER BY createdAt DESC")
    fun observeJobs(): Flow<List<GenerationJobEntity>>

    @Query("SELECT * FROM generation_jobs WHERE id = :id")
    suspend fun getJobById(id: Long): GenerationJobEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: GenerationJobEntity): Long

    @Query("UPDATE generation_jobs SET status = :status, progress = :progress, outputFilePath = :outputPath, fileSizeBytes = :sizeBytes, errorMessage = :error, completedAt = :completedAt WHERE id = :id")
    suspend fun updateJobProgress(
        id: Long,
        status: JobStatus,
        progress: Float,
        outputPath: String?,
        sizeBytes: Long,
        error: String?,
        completedAt: Long?
    )

    @Query("UPDATE generation_jobs SET isFavorite = :favorite WHERE id = :id")
    suspend fun setJobFavorite(id: Long, favorite: Boolean)

    @Query("DELETE FROM generation_jobs WHERE id = :id")
    suspend fun deleteJob(id: Long)

    @Query("SELECT COUNT(*) FROM generation_jobs WHERE mediaType = :type AND status = 'COMPLETED'")
    fun observeCompletedCount(type: MediaType): Flow<Int>

    @Query("SELECT COALESCE(SUM(fileSizeBytes), 0) FROM generation_jobs WHERE status = 'COMPLETED'")
    fun observeTotalStorageBytes(): Flow<Long>

    // Characters
    @Query("SELECT * FROM characters ORDER BY createdAt DESC")
    fun observeCharacters(): Flow<List<CharacterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: CharacterEntity): Long

    @Query("DELETE FROM characters WHERE id = :id")
    suspend fun deleteCharacter(id: Long)

    @Query("SELECT COUNT(*) FROM characters")
    suspend fun countCharacters(): Int

    // Prompt Templates
    @Query("SELECT * FROM prompt_templates ORDER BY id DESC")
    fun observePrompts(): Flow<List<PromptTemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrompt(prompt: PromptTemplateEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrompts(prompts: List<PromptTemplateEntity>)

    @Query("UPDATE prompt_templates SET isFavorite = :favorite WHERE id = :id")
    suspend fun setPromptFavorite(id: Long, favorite: Boolean)

    @Query("SELECT COUNT(*) FROM prompt_templates")
    suspend fun countPrompts(): Int

    // Downloads
    @Query("SELECT * FROM download_history ORDER BY downloadedAt DESC")
    fun observeDownloads(): Flow<List<DownloadRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(record: DownloadRecordEntity): Long

    @Query("DELETE FROM download_history")
    suspend fun clearDownloads()
}
