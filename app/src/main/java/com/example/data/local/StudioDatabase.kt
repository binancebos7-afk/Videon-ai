package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.domain.model.JobStatus
import com.example.domain.model.MediaType

class StudioTypeConverters {
    @TypeConverter
    fun fromJobStatus(value: JobStatus): String = value.name

    @TypeConverter
    fun toJobStatus(value: String): JobStatus =
        enumValues<JobStatus>().firstOrNull { it.name == value } ?: JobStatus.QUEUED

    @TypeConverter
    fun fromMediaType(value: MediaType): String = value.name

    @TypeConverter
    fun toMediaType(value: String): MediaType =
        enumValues<MediaType>().firstOrNull { it.name == value } ?: MediaType.VIDEO
}

@Database(
    entities = [
        ProjectEntity::class,
        GenerationJobEntity::class,
        CharacterEntity::class,
        PromptTemplateEntity::class,
        DownloadRecordEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(StudioTypeConverters::class)
abstract class StudioDatabase : RoomDatabase() {
    abstract fun studioDao(): StudioDao

    companion object {
        @Volatile
        private var INSTANCE: StudioDatabase? = null

        fun getInstance(context: Context): StudioDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    StudioDatabase::class.java,
                    "videon_ai_studio.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
