package com.example.domain.model

enum class AppLanguage(val code: String, val displayName: String) {
    ID("id", "Bahasa Indonesia"),
    EN("en", "English")
}

enum class StudioRoute(
    val route: String,
    val titleId: String,
    val titleEn: String,
    val isPrimaryNav: Boolean = false
) {
    HOME("home", "Beranda", "Home", true),
    DASHBOARD("dashboard", "Dasbor", "Dashboard", true),
    AI_VIDEO("ai_video", "AI Video", "AI Video", true),
    AI_IMAGE("ai_image", "AI Gambar", "AI Image", false),
    AI_AUDIO("ai_audio", "AI Audio", "AI Audio", false),
    VIDEO_EDITOR("video_editor", "Editor Video", "Video Editor", true),
    CHARACTER_STUDIO("character_studio", "Karakter", "Characters", false),
    PROMPT_LIBRARY("prompt_library", "Pustaka Prompt", "Prompt Library", false),
    MY_PROJECTS("my_projects", "Proyek Saya", "My Projects", true),
    HISTORY("history", "Riwayat", "History", false),
    SETTINGS("settings", "Pengaturan", "Settings", false),
    HELP_CENTER("help_center", "Pusat Bantuan", "Help Center", false);

    fun label(lang: AppLanguage): String = if (lang == AppLanguage.ID) titleId else titleEn
}

enum class ModelAvailabilityStatus {
    ACTIVE_LOCAL,
    SELF_HOSTED_ADAPTER,
    UNAVAILABLE_NO_PROVIDER
}

data class VideoModelSpec(
    val id: String,
    val name: String,
    val provider: String,
    val descriptionId: String,
    val descriptionEn: String,
    val qualityRating: String,
    val maxDurationSec: Int,
    val supportedResolutions: List<String>,
    val supportedAspectRatios: List<String>,
    val supportsAudio: Boolean,
    val availability: ModelAvailabilityStatus,
    val integrationStatusId: String,
    val integrationStatusEn: String
)

enum class VideoGenerationMode(val id: String, val labelId: String, val labelEn: String) {
    TEXT_TO_VIDEO("text_to_video", "Teks ke Video", "Text to Video"),
    IMAGE_TO_VIDEO("image_to_video", "Gambar ke Video", "Image to Video"),
    VIDEO_TO_VIDEO("video_to_video", "Video ke Video", "Video to Video"),
    FIRST_FRAME("first_frame", "Frame Awal ke Video", "First Frame to Video"),
    LAST_FRAME("last_frame", "Frame Akhir ke Video", "Last Frame to Video"),
    START_END_FRAME("start_end_frame", "Frame Awal & Akhir", "Start & End Frame"),
    CHARACTER_REF("character_ref", "Referensi Karakter", "Character Reference"),
    MULTI_IMAGE_REF("multi_image_ref", "Multi-Gambar Ref", "Multi-Image Reference"),
    VIDEO_EXTEND("video_extend", "Perpanjang Video", "AI Video Extend"),
    VIDEO_UPSCALE("video_upscale", "Tingkatkan Resolusi", "AI Video Upscale"),
    VIDEO_ENHANCE("video_enhance", "Peningkatan Video", "AI Video Enhancement"),
    VIDEO_INTERPOLATION("video_interpolation", "Interpolasi FPS", "AI Video Interpolation"),
    SCENE_GENERATOR("scene_generator", "Generator Adegan", "AI Scene Generator"),
    STORYBOARD_GENERATOR("storyboard_generator", "Generator Storyboard", "AI Storyboard Generator")
}

enum class ImageGenerationMode(val id: String, val labelId: String, val labelEn: String) {
    TEXT_TO_IMAGE("text_to_image", "Teks ke Gambar", "Text to Image"),
    IMAGE_TO_IMAGE("image_to_image", "Gambar ke Gambar", "Image to Image"),
    CHARACTER_DESIGN("character_design", "Desain Karakter", "Character Design"),
    CHARACTER_SHEET("character_sheet", "Lembar Karakter", "Character Sheet"),
    PRODUCT_PHOTO("product_photo", "Foto Produk", "Product Photography"),
    REALISTIC_PORTRAIT("realistic_portrait", "Potret Realistis", "Realistic Portrait"),
    ANIME_GENERATOR("anime_generator", "Generator Anime", "Anime Generator"),
    CINEMATIC_IMAGE("cinematic_image", "Gambar Sinematik", "Cinematic Image"),
    BACKGROUND_GENERATOR("background_gen", "Generator Latar", "Background Generator")
}

enum class AudioGenerationMode(val id: String, val labelId: String, val labelEn: String) {
    TEXT_TO_SPEECH("tts", "Teks ke Suara (TTS)", "Text to Speech"),
    AI_VOICE("ai_voice", "Suara AI", "AI Voice"),
    VOICE_OVER("voice_over", "Sulih Suara (VO)", "Voice Over"),
    SOUND_EFFECTS("sfx", "Efek Suara (SFX)", "Sound Effects"),
    BACKGROUND_MUSIC("bgm", "Musik Latar", "Background Music"),
    LIP_SYNC("lip_sync", "Sinkronisasi Bibir", "Lip Sync Script"),
    DIALOGUE_GEN("dialogue", "Generator Dialog", "Dialogue Generator"),
    MULTI_LANGUAGE("multi_lang", "Suara Multi-Bahasa", "Multi-language Voice")
}

enum class VisualStyle(val id: String, val labelId: String, val labelEn: String) {
    CINEMATIC("cinematic", "Sinematik", "Cinematic"),
    REALISTIC("realistic", "Realistis", "Realistic"),
    ANIME("anime", "Anime", "Anime"),
    CYBERPUNK("cyberpunk", "Cyberpunk Neon", "Cyberpunk Neon"),
    NOIR("noir", "Film Noir", "Film Noir")
}

enum class CameraMovement(val id: String, val labelId: String, val labelEn: String) {
    STATIC("static", "Statis", "Static"),
    DOLLY_IN("dolly_in", "Dolly Masuk", "Dolly In"),
    PAN_RIGHT("pan_right", "Geser Kanan", "Pan Right"),
    ORBIT("orbit", "Orbit 360", "Orbit"),
    CRANE_UP("crane_up", "Naik Vertikal", "Crane Up"),
    HANDHELD("handheld", "Handheld Dinamis", "Dynamic Handheld")
}

enum class CameraAngle(val id: String, val labelId: String, val labelEn: String) {
    EYE_LEVEL("eye", "Sejajar Mata", "Eye Level"),
    LOW_ANGLE("low", "Sudut Rendah", "Low Angle"),
    HIGH_ANGLE("high", "Sudut Tinggi", "High Angle"),
    WIDE_ANAMORPHIC("wide", "Lebar Anamorfik", "Wide Anamorphic"),
    CLOSE_UP("closeup", "Close-Up", "Close-Up")
}

data class AdvancedVideoConfig(
    val durationSec: Int = 5,
    val resolution: String = "720p",
    val aspectRatio: String = "16:9",
    val fps: Int = 24,
    val cameraMovement: CameraMovement = CameraMovement.DOLLY_IN,
    val cameraAngle: CameraAngle = CameraAngle.EYE_LEVEL,
    val motionStrength: Float = 0.65f,
    val creativityStrength: Float = 0.70f,
    val negativePrompt: String = "blurry, distorted, low-res, watermark",
    val seed: Long = 420789L,
    val promptEnhancer: Boolean = true,
    val style: VisualStyle = VisualStyle.CINEMATIC
)

enum class JobStatus {
    QUEUED,
    RENDERING,
    COMPLETED,
    FAILED,
    CANCELLED
}

enum class MediaType {
    VIDEO,
    IMAGE,
    AUDIO
}

enum class VideoFilterPreset(val id: String, val labelId: String, val labelEn: String) {
    NONE("none", "Asli", "Original"),
    CINEMATIC_TEAL("teal_orange", "Sinematik Teal", "Cinematic Teal"),
    CYBER_NEON("cyber_neon", "Neon Ungu-Cyan", "Cyber Neon"),
    NOIR_BW("noir_bw", "Hitam Putih Noir", "Noir B&W"),
    GOLDEN_HOUR("golden", "Hangat Emas", "Golden Hour"),
    ANIME_VIVID("vivid", "Anime Cerah", "Anime Vivid")
}

enum class TransitionPreset(val id: String, val labelId: String, val labelEn: String) {
    NONE("none", "Tanpa Transisi", "None"),
    FADE("fade", "Fade Hitam", "Fade In/Out"),
    CROSS_DISSOLVE("dissolve", "Cross Dissolve", "Cross Dissolve"),
    WIPE_CYAN("wipe", "Sapuan Cahaya", "Light Wipe")
}

data class EditorTimelineState(
    val sourceVideoPath: String? = null,
    val sourceVideoUriString: String? = null,
    val sourceTitle: String = "Untitled Timeline",
    val totalDurationMs: Long = 5000L,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = 5000L,
    val splitPointMs: Long = 2500L,
    val mergeClipPath: String? = null,
    val cropAspectRatio: String = "16:9",
    val resizeResolution: String = "720p",
    val textOverlay: String = "",
    val subtitleText: String = "",
    val transition: TransitionPreset = TransitionPreset.FADE,
    val audioTrackPath: String? = null,
    val audioTrackName: String = "",
    val audioMixVolume: Float = 0.8f,
    val filter: VideoFilterPreset = VideoFilterPreset.CINEMATIC_TEAL,
    val playbackSpeed: Float = 1.0f,
    val isExporting: Boolean = false,
    val exportProgress: Float = 0f,
    val lastExportedPath: String? = null
)

data class DialogueLine(
    val speaker: String,
    val emotion: String,
    val line: String
)

data class StoryboardScene(
    val sceneNumber: Int,
    val shotType: String,
    val durationSec: Int,
    val visualPrompt: String,
    val audioCue: String
)

data class StudioNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
