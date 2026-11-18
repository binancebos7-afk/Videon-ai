package com.example.data.engine

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

class LocalAudioEngine(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null

    suspend fun synthesizeSpeechToWav(
        text: String,
        languageTag: String,
        pitch: Float = 1.0f,
        speechRate: Float = 1.0f
    ): Result<File> = withContext(Dispatchers.IO) {
        val cleanText = text.trim()
        if (cleanText.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Text cannot be empty"))
        }
        val outDir = File(context.filesDir, "videon_exports").apply { mkdirs() }
        val outFile = File(outDir, "tts_${System.currentTimeMillis()}.wav")

        return@withContext suspendCancellableCoroutine { cont ->
            var ttsRef: TextToSpeech? = null
            ttsRef = TextToSpeech(context.applicationContext) { status ->
                val tts = ttsRef
                if (status != TextToSpeech.SUCCESS || tts == null) {
                    tts?.shutdown()
                    if (cont.isActive) {
                        cont.resume(Result.failure(IllegalStateException("Android TextToSpeech engine unavailable on this device.")))
                    }
                    return@TextToSpeech
                }
                val locale = when (languageTag) {
                    "id" -> Locale("id", "ID")
                    "ja" -> Locale.JAPAN
                    "es" -> Locale("es", "ES")
                    "fr" -> Locale.FRANCE
                    else -> Locale.US
                }
                tts.language = locale
                tts.setPitch(pitch.coerceIn(0.6f, 1.5f))
                tts.setSpeechRate(speechRate.coerceIn(0.6f, 1.6f))

                val utteranceId = UUID.randomUUID().toString()
                tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(id: String?) {}
                    override fun onDone(id: String?) {
                        tts.shutdown()
                        if (cont.isActive) cont.resume(Result.success(outFile))
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(id: String?) {
                        tts.shutdown()
                        if (cont.isActive) {
                            cont.resume(Result.failure(IllegalStateException("TTS synthesis failed")))
                        }
                    }

                    override fun onError(id: String?, errorCode: Int) {
                        tts.shutdown()
                        if (cont.isActive) {
                            cont.resume(Result.failure(IllegalStateException("TTS error code: $errorCode")))
                        }
                    }
                })

                val res = tts.synthesizeToFile(cleanText, Bundle(), outFile, utteranceId)
                if (res != TextToSpeech.SUCCESS) {
                    tts.shutdown()
                    if (cont.isActive) {
                        cont.resume(Result.failure(IllegalStateException("Failed to enqueue TTS synthesis")))
                    }
                }
            }

            cont.invokeOnCancellation {
                ttsRef?.stop()
                ttsRef?.shutdown()
            }
        }
    }

    suspend fun synthesizeProceduralAudio(
        preset: String,
        durationSec: Int = 5
    ): File = withContext(Dispatchers.Default) {
        val sampleRate = 22050
        val clampedSec = durationSec.coerceIn(2, 15)
        val totalSamples = sampleRate * clampedSec
        val pcmBuffer = ByteArrayOutputStream(totalSamples * 2)
        val rng = Random(preset.hashCode())

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val env = when {
                t < 0.25 -> t / 0.25
                t > clampedSec - 0.35 -> ((clampedSec - t) / 0.35).coerceAtLeast(0.0)
                else -> 1.0
            }
            val sample = when (preset) {
                "cinematic_riser" -> {
                    val freq = 55.0 + (t / clampedSec) * 260.0
                    val sub = sin(2 * PI * freq * t) * 0.55
                    val shimmer = sin(2 * PI * freq * 2.0 * t) * 0.25
                    (sub + shimmer) * env * (t / clampedSec).coerceAtLeast(0.2)
                }
                "cyberpunk_synth" -> {
                    val arpFreqs = doubleArrayOf(110.0, 164.81, 220.0, 261.63, 329.63)
                    val step = ((t * 4).toInt()) % arpFreqs.size
                    val f = arpFreqs[step]
                    val pulse = sin(2 * PI * f * t) * 0.4 + sin(2 * PI * (f * 0.5) * t) * 0.35
                    pulse * env
                }
                "rain_ambience" -> {
                    val noise = (rng.nextDouble() * 2.0 - 1.0) * 0.28
                    val lowHum = sin(2 * PI * 72.0 * t) * 0.12
                    (noise + lowHum) * env
                }
                "scifi_whoosh" -> {
                    val peak = exp(-((t - clampedSec * 0.5) * (t - clampedSec * 0.5)) * 2.2)
                    val sweep = sin(2 * PI * (120.0 + 480.0 * peak) * t) * 0.4
                    val air = (rng.nextDouble() * 2.0 - 1.0) * 0.35 * peak
                    (sweep + air) * env
                }
                else -> { // lofi_pad
                    val c1 = sin(2 * PI * 130.81 * t)
                    val c2 = sin(2 * PI * 196.00 * t)
                    val c3 = sin(2 * PI * 246.94 * t)
                    ((c1 + c2 * 0.8 + c3 * 0.6) / 2.6) * env
                }
            }
            val pcm16 = (sample.coerceIn(-0.95, 0.95) * Short.MAX_VALUE).toInt().toShort()
            pcmBuffer.write(pcm16.toInt() and 0xFF)
            pcmBuffer.write((pcm16.toInt() shr 8) and 0xFF)
        }

        val pcmBytes = pcmBuffer.toByteArray()
        val outDir = File(context.filesDir, "videon_exports").apply { mkdirs() }
        val outFile = File(outDir, "${preset}_${System.currentTimeMillis()}.wav")
        writeWavFile(outFile, pcmBytes, sampleRate, 1, 16)
        outFile
    }

    private fun writeWavFile(
        file: File,
        pcmData: ByteArray,
        sampleRate: Int,
        channels: Int,
        bitsPerSample: Int
    ) {
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".toByteArray())
            putInt(36 + pcmData.size)
            put("WAVE".toByteArray())
            put("fmt ".toByteArray())
            putInt(16)
            putShort(1.toShort()) // PCM
            putShort(channels.toShort())
            putInt(sampleRate)
            putInt(byteRate)
            putShort((channels * bitsPerSample / 8).toShort())
            putShort(bitsPerSample.toShort())
            put("data".toByteArray())
            putInt(pcmData.size)
        }.array()

        FileOutputStream(file).use { fos ->
            fos.write(header)
            fos.write(pcmData)
        }
    }

    fun playAudioFile(path: String, onCompletion: () -> Unit = {}) {
        stopPlayback()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(path)
            setOnCompletionListener {
                onCompletion()
            }
            prepare()
            start()
        }
    }

    fun stopPlayback() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {
        }
        mediaPlayer = null
    }
}
