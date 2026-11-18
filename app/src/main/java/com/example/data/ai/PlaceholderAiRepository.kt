package com.example.data.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import com.example.domain.model.AppLanguage
import com.example.domain.model.CameraMovement
import com.example.domain.model.DialogueLine
import com.example.domain.model.ImageGenerationMode
import com.example.domain.model.StoryboardScene
import com.example.domain.model.VisualStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Fallback implementation used when Firebase AI Logic is not enabled.
 * All outputs are clearly labeled as sample template responses.
 * For Image-to-Image / Reference styling with a user-picked Bitmap, it performs real
 * on-device color-grading / character-sheet layout processing and labels cloud generation notice.
 */
class PlaceholderAiRepository(private val context: Context) : AiRepository {

    private fun notice(lang: AppLanguage): String = if (lang == AppLanguage.ID) {
        "[Sampel Lokal] Hubungkan Firebase AI Logic untuk generasi Gemini cloud langsung."
    } else {
        "[Local Sample] Enable Firebase AI Logic for live cloud Gemini generation."
    }

    override suspend fun enhancePrompt(
        rawPrompt: String,
        style: VisualStyle,
        language: AppLanguage
    ): AiOutput<String> = withContext(Dispatchers.Default) {
        delay(250)
        val base = rawPrompt.trim().ifBlank { "Futuristic neon city street at dusk" }
        val styleTags = when (style) {
            VisualStyle.CINEMATIC -> "anamorphic 35mm lens, volumetric rim lighting, shallow depth of field, 24fps film grain, ultra-detailed reflections"
            VisualStyle.REALISTIC -> "photorealistic 8K raw cinema still, natural subsurface scattering, physically accurate global illumination"
            VisualStyle.ANIME -> "high-budget modern anime keyframe, crisp cel shading, luminous bloom, dynamic speed lines, Studio Makoto atmosphere"
            VisualStyle.CYBERPUNK -> "rain-slicked obsidian asphalt, cyan and violet holographic reflections, atmospheric neon fog, blade-runner mood"
            VisualStyle.NOIR -> "chiaroscuro high-contrast monochrome lighting, dramatic Venetian blind shadows, 1940s cinema noir grain"
        }
        val result = "[Sample AI Prompt] $base, $styleTags, smooth steady camera motion, balanced composition."
        AiOutput(data = result, isSamplePlaceholder = true, noticeMessage = notice(language))
    }

    override suspend fun translateIndonesianToEnglish(indonesianText: String): AiOutput<String> =
        withContext(Dispatchers.Default) {
            delay(200)
            val cleaned = indonesianText.trim()
            val dictionary = mapOf(
                "kota" to "city",
                "hujan" to "rain",
                "malam" to "night",
                "karakter" to "character",
                "wanita" to "woman",
                "pria" to "man",
                "berjalan" to "walking",
                "berlari" to "running",
                "gunung" to "mountain",
                "laut" to "ocean",
                "sinematik" to "cinematic",
                "cahaya" to "light",
                "biru" to "blue",
                "ungu" to "purple",
                "futuristik" to "futuristic",
                "langit" to "sky",
                "kamera" to "camera"
            )
            var translated = cleaned
            dictionary.forEach { (idWord, enWord) ->
                translated = translated.replace(Regex("\\b$idWord\\b", RegexOption.IGNORE_CASE), enWord)
            }
            val output = "[Sample Translation] Cinematic shot: $translated, volumetric lighting, smooth camera motion."
            AiOutput(
                data = output,
                isSamplePlaceholder = true,
                noticeMessage = notice(AppLanguage.ID)
            )
        }

    override suspend fun createCinematicPrompt(
        concept: String,
        camera: CameraMovement,
        style: VisualStyle
    ): AiOutput<String> = withContext(Dispatchers.Default) {
        delay(220)
        val subject = concept.trim().ifBlank { "A lone cyber-traveler standing on a glass skybridge" }
        val camDesc = when (camera) {
            CameraMovement.STATIC -> "locked tripod establishing frame"
            CameraMovement.DOLLY_IN -> "slow smooth dolly push-in toward subject"
            CameraMovement.PAN_RIGHT -> "sweeping horizontal pan right revealing skyline"
            CameraMovement.ORBIT -> "360-degree orbital gimbal shot around subject"
            CameraMovement.CRANE_UP -> "rising crane ascent unveiling vast horizon"
            CameraMovement.HANDHELD -> "tactile dynamic handheld follow shot"
        }
        val output = "[Sample Cinematic Prompt] $subject, $camDesc, ${style.labelEn.lowercase()} aesthetic, " +
            "golden cyan key light with deep violet ambient shadows, 35mm anamorphic bokeh."
        AiOutput(data = output, isSamplePlaceholder = true, noticeMessage = notice(AppLanguage.EN))
    }

    override suspend fun generateDialogue(
        sceneContext: String,
        speakers: List<String>,
        language: AppLanguage
    ): AiOutput<List<DialogueLine>> = withContext(Dispatchers.Default) {
        delay(250)
        val s1 = speakers.getOrNull(0)?.ifBlank { "Kael" } ?: "Kael"
        val s2 = speakers.getOrNull(1)?.ifBlank { "Lyra" } ?: "Lyra"
        val topic = sceneContext.trim().ifBlank {
            if (language == AppLanguage.ID) "peluncuran stasiun orbit" else "orbital station launch"
        }
        val lines = if (language == AppLanguage.ID) {
            listOf(
                DialogueLine(s1, "Tenang & Fokus", "[Sampel] Semua sensor optik untuk $topic sudah terkunci. Kita siap memulai rekaman."),
                DialogueLine(s2, "Antusias", "[Sampel] Cahaya neon di cakrawala terlihat sempurna—jalankan mesin render sekarang."),
                DialogueLine(s1, "Optimis", "[Sampel] Bingkai pertama sudah bersih. Mari ciptakan adegan yang tak terlupakan.")
            )
        } else {
            listOf(
                DialogueLine(s1, "Calm & Focused", "[Sample] Optical telemetry for $topic is locked in. Ready to roll."),
                DialogueLine(s2, "Inspired", "[Sample] The horizon light looks unreal—initiate the motion sequence."),
                DialogueLine(s1, "Confident", "[Sample] First frame captured cleanly. Let's bring this vision to life.")
            )
        }
        AiOutput(data = lines, isSamplePlaceholder = true, noticeMessage = notice(language))
    }

    override suspend fun promptFromImage(
        bitmap: Bitmap,
        userHint: String,
        language: AppLanguage
    ): AiOutput<String> = withContext(Dispatchers.Default) {
        // Inspect actual average luminance & dominant hue of user-picked bitmap for truthful local analysis
        val scaled = Bitmap.createScaledBitmap(bitmap, 16, 16, true)
        var rSum = 0L
        var gSum = 0L
        var bSum = 0L
        for (y in 0 until 16) {
            for (x in 0 until 16) {
                val p = scaled.getPixel(x, y)
                rSum += Color.red(p)
                gSum += Color.green(p)
                bSum += Color.blue(p)
            }
        }
        val count = 256L
        val avgR = rSum / count
        val avgG = gSum / count
        val avgB = bSum / count
        val tone = when {
            avgB > avgR + 25 -> "cool cyan-blue atmospheric palette"
            avgR > avgB + 25 -> "warm amber-crimson lighting"
            (avgR + avgG + avgB) / 3 < 70 -> "low-key moody obsidian shadows"
            else -> "balanced studio contrast"
        }
        val hintText = userHint.trim().ifBlank { "reference subject" }
        val prompt = "[Sample Image-to-Prompt (${bitmap.width}x${bitmap.height})] " +
            "Animate $hintText with $tone, subtle volumetric depth, smooth slow dolly-in camera movement, cinematic film grain."
        AiOutput(data = prompt, isSamplePlaceholder = true, noticeMessage = notice(language))
    }

    override suspend fun generateStoryboard(
        concept: String,
        sceneCount: Int,
        language: AppLanguage
    ): AiOutput<List<StoryboardScene>> = withContext(Dispatchers.Default) {
        delay(260)
        val topic = concept.trim().ifBlank { "Cyberpunk Horizon" }
        val shots = listOf("Wide Establishing Shot", "Medium Tracking Shot", "Close-Up Reaction", "Crane Pull-Back")
        val scenes = (1..sceneCount.coerceIn(2, 6)).map { idx ->
            val shot = shots[(idx - 1) % shots.size]
            if (language == AppLanguage.ID) {
                StoryboardScene(
                    sceneNumber = idx,
                    shotType = shot,
                    durationSec = 5,
                    visualPrompt = "[Sampel Scene $idx] $topic — $shot dengan pencahayaan cyan-violet dan pergerakan kamera halus.",
                    audioCue = if (idx == 1) "Ambient synth pad masuk perlahan" else "Transisi whoosh + efek atmosfer"
                )
            } else {
                StoryboardScene(
                    sceneNumber = idx,
                    shotType = shot,
                    durationSec = 5,
                    visualPrompt = "[Sample Scene $idx] $topic — $shot with cyan-violet rim lighting and smooth gimbal motion.",
                    audioCue = if (idx == 1) "Deep ambient synth pad fade-in" else "Subtle riser whoosh & atmospheric texture"
                )
            }
        }
        AiOutput(data = scenes, isSamplePlaceholder = true, noticeMessage = notice(language))
    }

    override suspend fun generateConsistentScenePrompts(
        characterName: String,
        characterTraits: String,
        sceneCount: Int
    ): AiOutput<List<String>> = withContext(Dispatchers.Default) {
        delay(220)
        val actions = listOf(
            "walking through rain-lit glass corridor, calm expression, medium tracking shot",
            "looking over glowing neon skyline from rooftop balcony, wind in hair, wide shot",
            "activating holographic wrist console, focused close-up, cyan screen glow on face",
            "stepping into misty neon alley under twilight sky, dramatic low-angle shot"
        )
        val prompts = (0 until sceneCount.coerceIn(2, 4)).map { i ->
            "[Sample Consistent Scene ${i + 1}] Character $characterName ($characterTraits), ${actions[i % actions.size]}, identical facial structure and outfit continuity."
        }
        AiOutput(data = prompts, isSamplePlaceholder = true, noticeMessage = notice(AppLanguage.EN))
    }

    override suspend fun generateStudioImage(
        prompt: String,
        mode: ImageGenerationMode,
        style: VisualStyle,
        aspectRatio: String,
        referenceBitmap: Bitmap?
    ): AiOutput<GeneratedImageResult> = withContext(Dispatchers.IO) {
        // If user supplied a reference bitmap (Image-to-Image / Character Sheet / Portrait grade),
        // we apply a real color-grading matrix & save a real PNG without pretending cloud AI generated it.
        if (referenceBitmap != null) {
            val outDir = File(context.filesDir, "videon_exports").apply { mkdirs() }
            val outFile = File(outDir, "graded_image_${System.currentTimeMillis()}.png")
            val targetW = 1024
            val targetH = when (aspectRatio) {
                "9:16" -> 1820
                "4:3" -> 768
                "16:9" -> 576
                "21:9" -> 438
                else -> 1024
            }
            val resultBitmap = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(resultBitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            val matrix = ColorMatrix()
            when (style) {
                VisualStyle.NOIR -> matrix.setSaturation(0f)
                VisualStyle.CYBERPUNK -> {
                    matrix.setSaturation(1.35f)
                    matrix.postConcat(ColorMatrix(floatArrayOf(
                        0.95f, 0f, 0.1f, 0f, -10f,
                        0f, 1.05f, 0.15f, 0f, 5f,
                        0.1f, 0.15f, 1.25f, 0f, 18f,
                        0f, 0f, 0f, 1f, 0f
                    )))
                }
                VisualStyle.CINEMATIC -> matrix.setSaturation(1.15f)
                VisualStyle.ANIME -> matrix.setSaturation(1.4f)
                VisualStyle.REALISTIC -> matrix.setSaturation(1.05f)
            }
            paint.colorFilter = ColorMatrixColorFilter(matrix)
            val scale = maxOf(targetW.toFloat() / referenceBitmap.width, targetH.toFloat() / referenceBitmap.height)
            val dw = referenceBitmap.width * scale
            val dh = referenceBitmap.height * scale
            val dx = (targetW - dw) / 2f
            val dy = (targetH - dh) / 2f
            canvas.drawBitmap(
                referenceBitmap,
                null,
                android.graphics.RectF(dx, dy, dx + dw, dy + dh),
                paint
            )
            FileOutputStream(outFile).use { fos ->
                resultBitmap.compress(Bitmap.CompressFormat.PNG, 95, fos)
            }
            return@withContext AiOutput(
                data = GeneratedImageResult(
                    filePath = outFile.absolutePath,
                    summary = "Applied real on-device ${style.labelEn} color grade (${targetW}x${targetH}) to your reference image. Cloud generative synthesis requires Firebase AI Logic.",
                    width = targetW,
                    height = targetH,
                    isSamplePlaceholder = true
                ),
                isSamplePlaceholder = true,
                noticeMessage = notice(AppLanguage.EN)
            )
        }

        // Without Firebase AI Logic, do NOT fabricate procedural Canvas artwork as fake AI images;
        // return a clean notice explaining cloud AI image generation requires Firebase AI Logic.
        AiOutput(
            data = GeneratedImageResult(
                filePath = null,
                summary = "Cloud generative ${mode.labelEn} requires Firebase AI Logic (Gemini Nano Banana). You can pick a reference photo above to apply real on-device studio color grading & aspect-ratio framing, or enable Firebase AI Logic.",
                width = 0,
                height = 0,
                isSamplePlaceholder = true
            ),
            isSamplePlaceholder = true,
            noticeMessage = notice(AppLanguage.EN)
        )
    }
}
