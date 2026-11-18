package com.example.data.engine

import com.example.domain.model.AdvancedVideoConfig
import com.example.domain.model.AppLanguage
import com.example.domain.model.ModelAvailabilityStatus
import com.example.domain.model.VideoGenerationMode
import com.example.domain.model.VideoModelSpec
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class OpenSourceHealthResponse(
    val status: String? = null,
    val model: String? = null,
    val gpu: String? = null
)

@JsonClass(generateAdapter = true)
data class OpenSourceVideoRequest(
    val model_id: String,
    val mode: String,
    val prompt: String,
    val negative_prompt: String,
    val duration_sec: Int,
    val resolution: String,
    val aspect_ratio: String,
    val fps: Int,
    val seed: Long,
    val motion_strength: Float
)

@JsonClass(generateAdapter = true)
data class OpenSourceVideoResponse(
    val job_id: String? = null,
    val status: String? = null,
    val video_url: String? = null,
    val message: String? = null
)

interface OpenSourceServerApi {
    @GET("health")
    suspend fun checkHealth(): Response<OpenSourceHealthResponse>

    @POST("api/v1/generate/video")
    suspend fun submitVideoJob(@Body request: OpenSourceVideoRequest): Response<OpenSourceVideoResponse>
}

object ModelCatalog {
    val allModels: List<VideoModelSpec> = listOf(
        VideoModelSpec(
            id = "videon_local_motion",
            name = "Videon Local Motion v1",
            provider = "On-Device MediaCodec Engine",
            descriptionId = "Mesin render H.264 MP4 native gratis di perangkat. Mendukung Teks, Gambar Referensi, Karakter, Ken Burns Sinematik, Subtitle & Ekspor MP4.",
            descriptionEn = "Free native on-device H.264 MP4 rendering engine. Supports Text, Reference Photos, Characters, Cinematic Camera Motion, Subtitles & MP4 Export.",
            qualityRating = "HD / 1080p Local",
            maxDurationSec = 30,
            supportedResolutions = listOf("480p", "720p", "1080p"),
            supportedAspectRatios = listOf("16:9", "9:16", "1:1", "4:3", "21:9"),
            supportsAudio = true,
            availability = ModelAvailabilityStatus.ACTIVE_LOCAL,
            integrationStatusId = "Aktif • Gratis di Perangkat",
            integrationStatusEn = "Active • Free On-Device"
        ),
        VideoModelSpec(
            id = "wan_ai_selfhosted",
            name = "Wan AI 2.1 (Open-Source Adapter)",
            provider = "Self-Hosted GPU / ComfyUI",
            descriptionId = "Adapter open-source untuk menjalankan Wan 2.1, CogVideoX, atau HunyuanVideo di server GPU lokal Anda tanpa biaya langganan.",
            descriptionEn = "Open-source adapter for running Wan 2.1, CogVideoX, or HunyuanVideo on your own self-hosted GPU server with zero subscription.",
            qualityRating = "Up to 1080p Open",
            maxDurationSec = 15,
            supportedResolutions = listOf("480p", "720p", "1080p"),
            supportedAspectRatios = listOf("16:9", "9:16", "1:1"),
            supportsAudio = false,
            availability = ModelAvailabilityStatus.SELF_HOSTED_ADAPTER,
            integrationStatusId = "Adapter Siap • Butuh URL Server Lokal",
            integrationStatusEn = "Adapter Ready • Self-Hosted URL"
        ),
        VideoModelSpec(
            id = "seedance_2_5",
            name = "Seedance 2.5",
            provider = "ByteDance Volcengine",
            descriptionId = "Model sintesis video sinematik beresolusi tinggi dengan konsistensi gerakan multi-subjek.",
            descriptionEn = "High-resolution cinematic video synthesis model with multi-subject motion consistency.",
            qualityRating = "1080p Cinema",
            maxDurationSec = 10,
            supportedResolutions = listOf("720p", "1080p"),
            supportedAspectRatios = listOf("16:9", "9:16", "1:1"),
            supportsAudio = true,
            availability = ModelAvailabilityStatus.UNAVAILABLE_NO_PROVIDER,
            integrationStatusId = "Belum tersedia • Tanpa Akses Provider",
            integrationStatusEn = "Not Available • No Provider Access"
        ),
        VideoModelSpec(
            id = "kling_3_0",
            name = "Kling 3.0",
            provider = "Kuaishou AI",
            descriptionId = "Model generasi video fisika realistis dan kontrol kamera 3D generasi terbaru.",
            descriptionEn = "Next-gen realistic physics video generation model with 3D camera choreography.",
            qualityRating = "4K Cinema",
            maxDurationSec = 15,
            supportedResolutions = listOf("720p", "1080p", "2K", "4K"),
            supportedAspectRatios = listOf("16:9", "9:16", "1:1"),
            supportsAudio = true,
            availability = ModelAvailabilityStatus.UNAVAILABLE_NO_PROVIDER,
            integrationStatusId = "Belum tersedia • Tanpa Akses Provider",
            integrationStatusEn = "Not Available • No Provider Access"
        ),
        VideoModelSpec(
            id = "kling_2_6",
            name = "Kling 2.6",
            provider = "Kuaishou AI",
            descriptionId = "Model Video-to-Video dan kontrol Start/End Frame presisi tinggi.",
            descriptionEn = "High-precision Video-to-Video and Start/End Frame interpolation model.",
            qualityRating = "1080p Pro",
            maxDurationSec = 10,
            supportedResolutions = listOf("720p", "1080p"),
            supportedAspectRatios = listOf("16:9", "9:16", "1:1"),
            supportsAudio = false,
            availability = ModelAvailabilityStatus.UNAVAILABLE_NO_PROVIDER,
            integrationStatusId = "Belum tersedia • Tanpa Akses Provider",
            integrationStatusEn = "Not Available • No Provider Access"
        ),
        VideoModelSpec(
            id = "veo_cloud",
            name = "Veo",
            provider = "Google DeepMind",
            descriptionId = "Model video generatif sinematik dengan pemahaman prompt kompleks dan audio tersinkronisasi.",
            descriptionEn = "Cinematic generative video model with deep prompt comprehension and native audio.",
            qualityRating = "1080p / 4K",
            maxDurationSec = 15,
            supportedResolutions = listOf("720p", "1080p", "4K"),
            supportedAspectRatios = listOf("16:9", "9:16"),
            supportsAudio = true,
            availability = ModelAvailabilityStatus.UNAVAILABLE_NO_PROVIDER,
            integrationStatusId = "Belum tersedia • Tanpa Akses Provider",
            integrationStatusEn = "Not Available • No Provider Access"
        ),
        VideoModelSpec(
            id = "sora_cloud",
            name = "Sora",
            provider = "OpenAI",
            descriptionId = "Simulator dunia dan generator video multi-adegan dengan kontinuitas visual panjang.",
            descriptionEn = "World-simulator video model with multi-shot narrative continuity.",
            qualityRating = "1080p",
            maxDurationSec = 20,
            supportedResolutions = listOf("480p", "720p", "1080p"),
            supportedAspectRatios = listOf("16:9", "9:16", "1:1"),
            supportsAudio = false,
            availability = ModelAvailabilityStatus.UNAVAILABLE_NO_PROVIDER,
            integrationStatusId = "Belum tersedia • Tanpa Akses Provider",
            integrationStatusEn = "Not Available • No Provider Access"
        ),
        VideoModelSpec(
            id = "runway_gen4",
            name = "Runway Gen-4",
            provider = "Runway",
            descriptionId = "Generasi video berfokus karakter konsisten dan kontrol kamera profesional.",
            descriptionEn = "Character-consistent cinematic video model with directorial camera control.",
            qualityRating = "1080p / 2K",
            maxDurationSec = 10,
            supportedResolutions = listOf("720p", "1080p", "2K"),
            supportedAspectRatios = listOf("16:9", "9:16", "21:9"),
            supportsAudio = false,
            availability = ModelAvailabilityStatus.UNAVAILABLE_NO_PROVIDER,
            integrationStatusId = "Belum tersedia • Tanpa Akses Provider",
            integrationStatusEn = "Not Available • No Provider Access"
        ),
        VideoModelSpec(
            id = "hailuo_minimax",
            name = "Hailuo",
            provider = "MiniMax",
            descriptionId = "Model animasi ekspresi manusia halus dan dinamika pencahayaan alami.",
            descriptionEn = "Expressive human motion and natural lighting video generation model.",
            qualityRating = "720p / 1080p",
            maxDurationSec = 10,
            supportedResolutions = listOf("720p", "1080p"),
            supportedAspectRatios = listOf("16:9", "9:16", "1:1"),
            supportsAudio = false,
            availability = ModelAvailabilityStatus.UNAVAILABLE_NO_PROVIDER,
            integrationStatusId = "Belum tersedia • Tanpa Akses Provider",
            integrationStatusEn = "Not Available • No Provider Access"
        ),
        VideoModelSpec(
            id = "luma_ray",
            name = "Luma AI",
            provider = "Luma Labs",
            descriptionId = "Dream Machine untuk keyframe start-end dan transisi kamera cepat.",
            descriptionEn = "Dream Machine for fast keyframe bridging and fluid camera arcs.",
            qualityRating = "1080p",
            maxDurationSec = 10,
            supportedResolutions = listOf("720p", "1080p"),
            supportedAspectRatios = listOf("16:9", "9:16", "1:1", "4:3"),
            supportsAudio = false,
            availability = ModelAvailabilityStatus.UNAVAILABLE_NO_PROVIDER,
            integrationStatusId = "Belum tersedia • Tanpa Akses Provider",
            integrationStatusEn = "Not Available • No Provider Access"
        ),
        VideoModelSpec(
            id = "pika_cloud",
            name = "Pika",
            provider = "Pika Labs",
            descriptionId = "Efek kreatif, Pikaffects, dan modifikasi objek video pendek.",
            descriptionEn = "Creative effects, object transforms, and stylized short-form motion.",
            qualityRating = "720p / 1080p",
            maxDurationSec = 10,
            supportedResolutions = listOf("720p", "1080p"),
            supportedAspectRatios = listOf("16:9", "9:16", "1:1"),
            supportsAudio = true,
            availability = ModelAvailabilityStatus.UNAVAILABLE_NO_PROVIDER,
            integrationStatusId = "Belum tersedia • Tanpa Akses Provider",
            integrationStatusEn = "Not Available • No Provider Access"
        ),
        VideoModelSpec(
            id = "mochi_cogvideox",
            name = "Mochi 1 / CogVideoX",
            provider = "Open-Source Self-Hosted",
            descriptionId = "Model open-weights gratis yang dapat dijalankan via endpoint server ComfyUI/FastAPI lokal.",
            descriptionEn = "Free open-weights model runnable via your local ComfyUI/FastAPI backend endpoint.",
            qualityRating = "720p Open",
            maxDurationSec = 10,
            supportedResolutions = listOf("480p", "720p"),
            supportedAspectRatios = listOf("16:9", "1:1"),
            supportsAudio = false,
            availability = ModelAvailabilityStatus.SELF_HOSTED_ADAPTER,
            integrationStatusId = "Adapter Siap • Butuh URL Server Lokal",
            integrationStatusEn = "Adapter Ready • Self-Hosted URL"
        )
    )

    fun findById(id: String): VideoModelSpec =
        allModels.firstOrNull { it.id == id } ?: allModels.first()
}

sealed interface AdapterCheckResult {
    data class Ready(val detail: String) : AdapterCheckResult
    data class Unavailable(val reason: String) : AdapterCheckResult
}

object OpenSourceAdapterClient {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private fun buildApi(baseUrl: String): OpenSourceServerApi {
        val normalized = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(normalized)
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OpenSourceServerApi::class.java)
    }

    suspend fun testConnection(baseUrl: String, lang: AppLanguage): AdapterCheckResult = withContext(Dispatchers.IO) {
        if (baseUrl.isBlank() || (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://"))) {
            return@withContext AdapterCheckResult.Unavailable(
                if (lang == AppLanguage.ID) {
                    "URL server self-hosted belum dikonfigurasi di Pengaturan (contoh: http://192.168.1.10:8000)."
                } else {
                    "Self-hosted server URL is not configured in Settings (e.g., http://192.168.1.10:8000)."
                }
            )
        }
        return@withContext try {
            val response = buildApi(baseUrl).checkHealth()
            if (response.isSuccessful) {
                val body = response.body()
                AdapterCheckResult.Ready("Connected (${body?.model ?: "Open-Source Worker"} • ${body?.gpu ?: "Ready"})")
            } else {
                AdapterCheckResult.Unavailable("Server returned HTTP ${response.code()}")
            }
        } catch (e: Exception) {
            AdapterCheckResult.Unavailable(
                if (lang == AppLanguage.ID) {
                    "Tidak dapat terhubung ke server self-hosted: ${e.localizedMessage ?: "Connection refused"}"
                } else {
                    "Cannot reach self-hosted server: ${e.localizedMessage ?: "Connection refused"}"
                }
            )
        }
    }

    suspend fun dispatchJob(
        baseUrl: String,
        model: VideoModelSpec,
        mode: VideoGenerationMode,
        prompt: String,
        config: AdvancedVideoConfig
    ): Result<OpenSourceVideoResponse> = withContext(Dispatchers.IO) {
        try {
            val api = buildApi(baseUrl)
            val response = api.submitVideoJob(
                OpenSourceVideoRequest(
                    model_id = model.id,
                    mode = mode.id,
                    prompt = prompt,
                    negative_prompt = config.negativePrompt,
                    duration_sec = config.durationSec,
                    resolution = config.resolution,
                    aspect_ratio = config.aspectRatio,
                    fps = config.fps,
                    seed = config.seed,
                    motion_strength = config.motionStrength
                )
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(IllegalStateException("Worker HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
