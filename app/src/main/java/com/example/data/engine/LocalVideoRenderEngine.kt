package com.example.data.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import android.util.Log
import com.example.R
import com.example.domain.model.AdvancedVideoConfig
import com.example.domain.model.CameraMovement
import com.example.domain.model.EditorTimelineState
import com.example.domain.model.TransitionPreset
import com.example.domain.model.VideoFilterPreset
import com.example.domain.model.VideoGenerationMode
import com.example.domain.model.VisualStyle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.coroutineContext
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class LocalVideoRenderEngine(private val context: Context) {

    companion object {
        private const val TAG = "LocalVideoRenderEngine"
        private const val MIME_TYPE = "video/avc"
    }

    private fun resolveDimensions(resolution: String, aspectRatio: String): Pair<Int, Int> {
        val baseLongEdge = when (resolution) {
            "480p" -> 640
            "1080p", "2K", "4K" -> 1280
            else -> 960 // 720p smooth & crisp
        }
        val (rawW, rawH) = when (aspectRatio) {
            "9:16" -> (baseLongEdge * 9f / 16f).roundToInt() to baseLongEdge
            "1:1" -> (baseLongEdge * 0.75f).roundToInt() to (baseLongEdge * 0.75f).roundToInt()
            "4:3" -> baseLongEdge to (baseLongEdge * 3f / 4f).roundToInt()
            "21:9" -> baseLongEdge to (baseLongEdge * 9f / 21f).roundToInt()
            else -> baseLongEdge to (baseLongEdge * 9f / 16f).roundToInt()
        }
        // Align to multiple of 16 for H.264 encoders
        val w = ((rawW + 8) / 16) * 16
        val h = ((rawH + 8) / 16) * 16
        return w.coerceAtLeast(256) to h.coerceAtLeast(256)
    }

    private fun decodeUriBitmap(uriString: String?): Bitmap? {
        if (uriString.isNullOrBlank()) return null
        return try {
            val uri = Uri.parse(uriString)
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Unable to decode reference bitmap: ${e.message}")
            null
        }
    }

    private fun loadDrawableFallback(resName: String?): Bitmap? {
        val resId = when (resName) {
            "img_char_cyber" -> R.drawable.img_char_cyber
            "img_char_anime" -> R.drawable.img_char_anime
            "img_scene_preset" -> R.drawable.img_scene_preset
            "img_hero_studio" -> R.drawable.img_hero_studio
            else -> null
        } ?: return null
        return try {
            BitmapFactory.decodeResource(context.resources, resId)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun renderGeneratedVideo(
        jobId: Long,
        prompt: String,
        mode: VideoGenerationMode,
        config: AdvancedVideoConfig,
        referenceImageUri: String?,
        secondImageUri: String?,
        characterDrawableName: String?,
        characterImageUri: String?,
        onProgress: suspend (Float) -> Unit
    ): File = withContext(Dispatchers.Default) {
        val outDir = File(context.filesDir, "videon_exports").apply { mkdirs() }
        val outFile = File(outDir, "videon_${jobId}_${System.currentTimeMillis()}.mp4")

        val (width, height) = resolveDimensions(config.resolution, config.aspectRatio)
        val fps = config.fps.coerceIn(15, 30) // Stable real-time software/hardware encoding
        val durationSec = config.durationSec.coerceIn(3, 30)
        val totalFrames = durationSec * fps

        val primaryBitmap = decodeUriBitmap(referenceImageUri)
            ?: decodeUriBitmap(characterImageUri)
            ?: loadDrawableFallback(characterDrawableName)
            ?: loadDrawableFallback("img_scene_preset")
        val secondaryBitmap = decodeUriBitmap(secondImageUri)

        encodeFramesToMp4(
            outFile = outFile,
            width = width,
            height = height,
            fps = fps,
            totalFrames = totalFrames,
            onProgress = onProgress
        ) { canvas, frameIndex, total ->
            val t = frameIndex.toFloat() / (total - 1).coerceAtLeast(1)
            drawGenerativeCinemaFrame(
                canvas = canvas,
                width = width,
                height = height,
                t = t,
                prompt = prompt,
                mode = mode,
                config = config,
                primaryBitmap = primaryBitmap,
                secondaryBitmap = secondaryBitmap
            )
        }
        outFile
    }

    suspend fun exportEditedVideo(
        state: EditorTimelineState,
        onProgress: suspend (Float) -> Unit
    ): File = withContext(Dispatchers.Default) {
        val outDir = File(context.filesDir, "videon_exports").apply { mkdirs() }
        val outFile = File(outDir, "videon_edit_${System.currentTimeMillis()}.mp4")
        val (width, height) = resolveDimensions(state.resizeResolution, state.cropAspectRatio)
        val fps = 24

        val trimSpanMs = (state.trimEndMs - state.trimStartMs).coerceAtLeast(1000L)
        val adjustedDurationSec = ((trimSpanMs / state.playbackSpeed.coerceIn(0.5f, 2.0f)) / 1000f)
            .coerceIn(1.5f, 30f)
        val totalFrames = (adjustedDurationSec * fps).roundToInt().coerceAtLeast(24)

        val retriever = MediaMetadataRetriever()
        var hasVideoSource = false
        try {
            if (!state.sourceVideoPath.isNullOrBlank() && File(state.sourceVideoPath).exists()) {
                retriever.setDataSource(state.sourceVideoPath)
                hasVideoSource = true
            } else if (!state.sourceVideoUriString.isNullOrBlank()) {
                retriever.setDataSource(context, Uri.parse(state.sourceVideoUriString))
                hasVideoSource = true
            }
        } catch (e: Exception) {
            Log.w(TAG, "Source video retriever init failed, using studio canvas fallback: ${e.message}")
            hasVideoSource = false
        }

        val fallbackBitmap = loadDrawableFallback("img_scene_preset")
        var cachedFrame: Bitmap? = null
        var lastSampleBucket = -1

        try {
            encodeFramesToMp4(
                outFile = outFile,
                width = width,
                height = height,
                fps = fps,
                totalFrames = totalFrames,
                onProgress = onProgress
            ) { canvas, frameIndex, total ->
                val t = frameIndex.toFloat() / (total - 1).coerceAtLeast(1)
                val sourceTimeUs = ((state.trimStartMs + (t * trimSpanMs)).toLong()) * 1000L

                if (hasVideoSource) {
                    // Sample every 2nd frame for fast reliable retrieval while maintaining fluid output
                    val bucket = frameIndex / 2
                    if (bucket != lastSampleBucket) {
                        lastSampleBucket = bucket
                        cachedFrame = try {
                            retriever.getFrameAtTime(sourceTimeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                        } catch (_: Exception) {
                            cachedFrame
                        }
                    }
                }

                val frameBitmap = cachedFrame ?: fallbackBitmap
                drawEditorFrame(
                    canvas = canvas,
                    width = width,
                    height = height,
                    t = t,
                    sourceBitmap = frameBitmap,
                    state = state
                )
            }
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {
            }
        }
        outFile
    }

    private suspend fun encodeFramesToMp4(
        outFile: File,
        width: Int,
        height: Int,
        fps: Int,
        totalFrames: Int,
        onProgress: suspend (Float) -> Unit,
        drawFrame: (Canvas, Int, Int) -> Unit
    ) {
        val format = MediaFormat.createVideoFormat(MIME_TYPE, width, height).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            setInteger(MediaFormat.KEY_BIT_RATE, width * height * 4)
            setInteger(MediaFormat.KEY_FRAME_RATE, fps)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        }

        val encoder = MediaCodec.createEncoderByType(MIME_TYPE)
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val inputSurface = encoder.createInputSurface()
        encoder.start()

        val muxer = MediaMuxer(outFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var trackIndex = -1
        var muxerStarted = false
        val bufferInfo = MediaCodec.BufferInfo()

        fun drainEncoder(endOfStream: Boolean) {
            if (endOfStream) {
                encoder.signalEndOfInputStream()
            }
            while (true) {
                val outputIndex = encoder.dequeueOutputBuffer(bufferInfo, 10_000L)
                when {
                    outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER -> {
                        if (!endOfStream) break
                    }
                    outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        if (!muxerStarted) {
                            trackIndex = muxer.addTrack(encoder.outputFormat)
                            muxer.start()
                            muxerStarted = true
                        }
                    }
                    outputIndex >= 0 -> {
                        val encodedData = encoder.getOutputBuffer(outputIndex)
                        if (encodedData != null && bufferInfo.size > 0 && muxerStarted) {
                            if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0) {
                                encodedData.position(bufferInfo.offset)
                                encodedData.limit(bufferInfo.offset + bufferInfo.size)
                                muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                            }
                        }
                        encoder.releaseOutputBuffer(outputIndex, false)
                        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            break
                        }
                    }
                }
            }
        }

        try {
            for (frame in 0 until totalFrames) {
                coroutineContext.ensureActive()
                drainEncoder(false)
                val canvas = inputSurface.lockCanvas(null)
                try {
                    drawFrame(canvas, frame, totalFrames)
                } finally {
                    inputSurface.unlockCanvasAndPost(canvas)
                }
                if (frame % 3 == 0 || frame == totalFrames - 1) {
                    onProgress((frame + 1).toFloat() / totalFrames.toFloat())
                }
            }
            drainEncoder(true)
        } catch (e: CancellationException) {
            outFile.delete()
            throw e
        } finally {
            try {
                encoder.stop()
            } catch (_: Exception) {
            }
            try {
                encoder.release()
            } catch (_: Exception) {
            }
            try {
                inputSurface.release()
            } catch (_: Exception) {
            }
            try {
                if (muxerStarted) muxer.stop()
                muxer.release()
            } catch (_: Exception) {
            }
        }
    }

    private fun drawGenerativeCinemaFrame(
        canvas: Canvas,
        width: Int,
        height: Int,
        t: Float,
        prompt: String,
        mode: VideoGenerationMode,
        config: AdvancedVideoConfig,
        primaryBitmap: Bitmap?,
        secondaryBitmap: Bitmap?
    ) {
        canvas.drawColor(Color.parseColor("#050814"), PorterDuff.Mode.SRC)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val motionAmp = 0.08f + (config.motionStrength * 0.16f)

        // Determine animated Ken Burns transform based on CameraMovement
        val (scale, offsetX, offsetY) = when (config.cameraMovement) {
            CameraMovement.STATIC -> Triple(1.04f, 0f, 0f)
            CameraMovement.DOLLY_IN -> Triple(1.0f + t * motionAmp * 1.4f, 0f, 0f)
            CameraMovement.PAN_RIGHT -> Triple(1.18f, (t - 0.5f) * -width * motionAmp, 0f)
            CameraMovement.ORBIT -> Triple(
                1.14f + 0.05f * sin(t * PI).toFloat(),
                cos(t * 2f * PI).toFloat() * width * 0.04f,
                sin(t * 2f * PI).toFloat() * height * 0.03f
            )
            CameraMovement.CRANE_UP -> Triple(1.16f, 0f, (0.5f - t) * height * motionAmp)
            CameraMovement.HANDHELD -> Triple(
                1.12f,
                sin(t * 14f).toFloat() * width * 0.015f,
                cos(t * 11f).toFloat() * height * 0.015f
            )
        }

        // Apply visual style filter
        paint.colorFilter = styleColorFilter(config.style)

        val activeBitmap = if (secondaryBitmap != null && t > 0.5f) secondaryBitmap else primaryBitmap
        if (activeBitmap != null) {
            val baseScale = maxOf(width.toFloat() / activeBitmap.width, height.toFloat() / activeBitmap.height) * scale
            val dw = activeBitmap.width * baseScale
            val dh = activeBitmap.height * baseScale
            val left = (width - dw) / 2f + offsetX
            val top = (height - dh) / 2f + offsetY
            canvas.drawBitmap(activeBitmap, null, RectF(left, top, left + dw, top + dh), paint)
        } else {
            val bgPaint = Paint().apply {
                shader = LinearGradient(
                    0f,
                    0f,
                    width.toFloat(),
                    height.toFloat(),
                    intArrayOf(
                        Color.parseColor("#0A1024"),
                        Color.parseColor("#1E1B4B"),
                        Color.parseColor("#0E4A6B")
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
        }

        // Volumetric light sweep overlay
        val sweepPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                width * (t - 0.3f),
                0f,
                width * (t + 0.3f),
                height.toFloat(),
                intArrayOf(Color.TRANSPARENT, Color.argb(65, 0, 229, 255), Color.TRANSPARENT),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), sweepPaint)

        // Subtle animated kinetic light particles
        val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        for (i in 0 until 14) {
            val seedAngle = (config.seed + i * 97) % 360
            val px = ((sin(seedAngle + t * 2.5) * 0.45 + 0.5) * width).toFloat()
            val py = ((cos(seedAngle * 1.3 + t * 1.8) * 0.45 + 0.5) * height).toFloat()
            particlePaint.color = if (i % 2 == 0) Color.argb(120, 0, 229, 255) else Color.argb(110, 139, 92, 246)
            canvas.drawCircle(px, py, (2f + (i % 4) * 1.5f), particlePaint)
        }

        // Bottom cinematic vignette & subtle prompt caption bar
        val vignettePaint = Paint().apply {
            shader = LinearGradient(
                0f,
                height * 0.65f,
                0f,
                height.toFloat(),
                Color.TRANSPARENT,
                Color.argb(190, 5, 8, 20),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, height * 0.65f, width.toFloat(), height.toFloat(), vignettePaint)

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(220, 248, 250, 252)
            textSize = (height * 0.032f).coerceIn(14f, 26f)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val tagPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#00E5FF")
            textSize = (height * 0.024f).coerceIn(11f, 18f)
            typeface = Typeface.MONOSPACE
        }
        val snippet = prompt.trim().take(54).ifBlank { "VIDEON AI STUDIO • ${mode.labelEn}" }
        canvas.drawText("VIDEON LOCAL ENGINE • ${config.style.labelEn.uppercase()}", 24f, height - 46f, tagPaint)
        canvas.drawText(snippet, 24f, height - 20f, labelPaint)
    }

    private fun drawEditorFrame(
        canvas: Canvas,
        width: Int,
        height: Int,
        t: Float,
        sourceBitmap: Bitmap?,
        state: EditorTimelineState
    ) {
        canvas.drawColor(Color.BLACK, PorterDuff.Mode.SRC)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        paint.colorFilter = editorColorFilter(state.filter)

        if (sourceBitmap != null) {
            val scale = maxOf(width.toFloat() / sourceBitmap.width, height.toFloat() / sourceBitmap.height)
            val dw = sourceBitmap.width * scale
            val dh = sourceBitmap.height * scale
            val dx = (width - dw) / 2f
            val dy = (height - dh) / 2f
            canvas.drawBitmap(sourceBitmap, null, RectF(dx, dy, dx + dw, dy + dh), paint)
        }

        // Transition overlay
        when (state.transition) {
            TransitionPreset.FADE -> {
                val alpha = when {
                    t < 0.12f -> ((1f - t / 0.12f) * 255).toInt()
                    t > 0.88f -> (((t - 0.88f) / 0.12f) * 255).toInt()
                    else -> 0
                }.coerceIn(0, 255)
                if (alpha > 0) canvas.drawColor(Color.argb(alpha, 0, 0, 0))
            }
            TransitionPreset.WIPE_CYAN -> {
                if (t < 0.15f) {
                    val x = (t / 0.15f) * width
                    val wipePaint = Paint().apply { color = Color.argb(180, 0, 229, 255) }
                    canvas.drawRect(x, 0f, width.toFloat(), height.toFloat(), wipePaint)
                }
            }
            TransitionPreset.CROSS_DISSOLVE -> {
                if (t < 0.14f) {
                    val alpha = ((1f - t / 0.14f) * 160).toInt().coerceIn(0, 255)
                    canvas.drawColor(Color.argb(alpha, 15, 23, 48))
                }
            }
            TransitionPreset.NONE -> Unit
        }

        // Burn-in Text Overlay (Top)
        if (state.textOverlay.isNotBlank()) {
            val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = (height * 0.055f).coerceIn(18f, 44f)
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                setShadowLayer(6f, 2f, 2f, Color.BLACK)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(state.textOverlay.take(40), width / 2f, height * 0.14f, overlayPaint)
        }

        // Burn-in Subtitle (Bottom)
        if (state.subtitleText.isNotBlank()) {
            val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = (height * 0.042f).coerceIn(15f, 34f)
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            val boxPaint = Paint().apply { color = Color.argb(165, 5, 8, 20) }
            val textW = subPaint.measureText(state.subtitleText.take(56))
            val rect = RectF(
                (width - textW) / 2f - 20f,
                height * 0.82f,
                (width + textW) / 2f + 20f,
                height * 0.92f
            )
            canvas.drawRoundRect(rect, 12f, 12f, boxPaint)
            canvas.drawText(state.subtitleText.take(56), width / 2f, height * 0.89f, subPaint)
        }
    }

    private fun styleColorFilter(style: VisualStyle): ColorMatrixColorFilter? {
        val m = ColorMatrix()
        return when (style) {
            VisualStyle.NOIR -> {
                m.setSaturation(0f)
                ColorMatrixColorFilter(m)
            }
            VisualStyle.CYBERPUNK -> {
                m.setSaturation(1.3f)
                m.postConcat(ColorMatrix(floatArrayOf(
                    0.95f, 0f, 0.1f, 0f, -8f,
                    0f, 1.05f, 0.12f, 0f, 6f,
                    0.1f, 0.15f, 1.25f, 0f, 18f,
                    0f, 0f, 0f, 1f, 0f
                )))
                ColorMatrixColorFilter(m)
            }
            VisualStyle.ANIME -> {
                m.setSaturation(1.45f)
                ColorMatrixColorFilter(m)
            }
            VisualStyle.CINEMATIC -> {
                m.setSaturation(1.12f)
                ColorMatrixColorFilter(m)
            }
            VisualStyle.REALISTIC -> null
        }
    }

    private fun editorColorFilter(preset: VideoFilterPreset): ColorMatrixColorFilter? {
        val m = ColorMatrix()
        return when (preset) {
            VideoFilterPreset.NONE -> null
            VideoFilterPreset.NOIR_BW -> {
                m.setSaturation(0f)
                ColorMatrixColorFilter(m)
            }
            VideoFilterPreset.CINEMATIC_TEAL -> {
                m.setSaturation(1.18f)
                m.postConcat(ColorMatrix(floatArrayOf(
                    1.05f, 0f, 0f, 0f, 4f,
                    0f, 1.02f, 0.08f, 0f, 0f,
                    0f, 0.08f, 1.12f, 0f, 10f,
                    0f, 0f, 0f, 1f, 0f
                )))
                ColorMatrixColorFilter(m)
            }
            VideoFilterPreset.CYBER_NEON -> {
                m.setSaturation(1.38f)
                m.postConcat(ColorMatrix(floatArrayOf(
                    1.05f, 0f, 0.15f, 0f, 0f,
                    0f, 0.95f, 0.15f, 0f, 4f,
                    0.15f, 0.15f, 1.25f, 0f, 16f,
                    0f, 0f, 0f, 1f, 0f
                )))
                ColorMatrixColorFilter(m)
            }
            VideoFilterPreset.GOLDEN_HOUR -> {
                m.setSaturation(1.22f)
                m.postConcat(ColorMatrix(floatArrayOf(
                    1.18f, 0f, 0f, 0f, 12f,
                    0f, 1.06f, 0f, 0f, 4f,
                    0f, 0f, 0.85f, 0f, -8f,
                    0f, 0f, 0f, 1f, 0f
                )))
                ColorMatrixColorFilter(m)
            }
            VideoFilterPreset.ANIME_VIVID -> {
                m.setSaturation(1.5f)
                ColorMatrixColorFilter(m)
            }
        }
    }
}
