package com.example.data.ai

import android.graphics.Bitmap
import com.example.domain.model.AppLanguage
import com.example.domain.model.CameraMovement
import com.example.domain.model.DialogueLine
import com.example.domain.model.ImageGenerationMode
import com.example.domain.model.StoryboardScene
import com.example.domain.model.VisualStyle

data class AiOutput<T>(
    val data: T,
    val isSamplePlaceholder: Boolean = true,
    val noticeMessage: String
)

data class GeneratedImageResult(
    val filePath: String?,
    val summary: String,
    val width: Int,
    val height: Int,
    val isSamplePlaceholder: Boolean = true
)

interface AiRepository {
    suspend fun enhancePrompt(
        rawPrompt: String,
        style: VisualStyle,
        language: AppLanguage
    ): AiOutput<String>

    suspend fun translateIndonesianToEnglish(indonesianText: String): AiOutput<String>

    suspend fun createCinematicPrompt(
        concept: String,
        camera: CameraMovement,
        style: VisualStyle
    ): AiOutput<String>

    suspend fun generateDialogue(
        sceneContext: String,
        speakers: List<String>,
        language: AppLanguage
    ): AiOutput<List<DialogueLine>>

    suspend fun promptFromImage(
        bitmap: Bitmap,
        userHint: String,
        language: AppLanguage
    ): AiOutput<String>

    suspend fun generateStoryboard(
        concept: String,
        sceneCount: Int,
        language: AppLanguage
    ): AiOutput<List<StoryboardScene>>

    suspend fun generateConsistentScenePrompts(
        characterName: String,
        characterTraits: String,
        sceneCount: Int
    ): AiOutput<List<String>>

    suspend fun generateStudioImage(
        prompt: String,
        mode: ImageGenerationMode,
        style: VisualStyle,
        aspectRatio: String,
        referenceBitmap: Bitmap?
    ): AiOutput<GeneratedImageResult>
}
