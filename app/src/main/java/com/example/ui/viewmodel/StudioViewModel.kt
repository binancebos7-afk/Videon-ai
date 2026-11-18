package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiRepository
import com.example.data.ai.GeneratedImageResult
import com.example.data.ai.PlaceholderAiRepository
import com.example.data.engine.AdapterCheckResult
import com.example.data.engine.LocalAudioEngine
import com.example.data.engine.LocalVideoRenderEngine
import com.example.data.engine.MediaExportHelper
import com.example.data.engine.ModelCatalog
import com.example.data.engine.OpenSourceAdapterClient
import com.example.data.local.CharacterEntity
import com.example.data.local.DownloadRecordEntity
import com.example.data.local.GenerationJobEntity
import com.example.data.local.PreferencesRepository
import com.example.data.local.ProjectEntity
import com.example.data.local.PromptTemplateEntity
import com.example.data.local.StudioDatabase
import com.example.data.local.StudioPreferences
import com.example.domain.model.AdvancedVideoConfig
import com.example.domain.model.AppLanguage
import com.example.domain.model.AudioGenerationMode
import com.example.domain.model.CameraMovement
import com.example.domain.model.DialogueLine
import com.example.domain.model.EditorTimelineState
import com.example.domain.model.ImageGenerationMode
import com.example.domain.model.JobStatus
import com.example.domain.model.MediaType
import com.example.domain.model.ModelAvailabilityStatus
import com.example.domain.model.StoryboardScene
import com.example.domain.model.StudioNotification
import com.example.domain.model.StudioRoute
import com.example.domain.model.VideoGenerationMode
import com.example.domain.model.VideoModelSpec
import com.example.domain.model.VisualStyle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

data class VideoGeneratorUiState(
    val selectedMode: VideoGenerationMode = VideoGenerationMode.TEXT_TO_VIDEO,
    val selectedModel: VideoModelSpec = ModelCatalog.allModels.first(),
    val prompt: String = "",
    val referenceImageUri: String? = null,
    val secondaryImageUri: String? = null,
    val selectedCharacter: CharacterEntity? = null,
    val config: AdvancedVideoConfig = AdvancedVideoConfig(),
    val isEnhancingPrompt: Boolean = false,
    val latestCompletedVideoPath: String? = null,
    val activeJobId: Long? = null,
    val modelNoticeMessage: String? = null
)

data class ImageStudioUiState(
    val selectedMode: ImageGenerationMode = ImageGenerationMode.TEXT_TO_IMAGE,
    val prompt: String = "",
    val style: VisualStyle = VisualStyle.CINEMATIC,
    val aspectRatio: String = "16:9",
    val referenceImageUri: String? = null,
    val isGenerating: Boolean = false,
    val latestResult: GeneratedImageResult? = null,
    val noticeBanner: String? = null
)

data class AudioStudioUiState(
    val selectedMode: AudioGenerationMode = AudioGenerationMode.TEXT_TO_SPEECH,
    val textInput: String = "",
    val voiceLangTag: String = "id",
    val pitch: Float = 1.0f,
    val speechRate: Float = 1.0f,
    val selectedPreset: String = "cyberpunk_synth",
    val durationSec: Int = 5,
    val isSynthesizing: Boolean = false,
    val isPlaying: Boolean = false,
    val latestAudioPath: String? = null,
    val generatedDialogue: List<DialogueLine> = emptyList(),
    val statusMessage: String? = null
)

data class PromptAssistantUiState(
    val inputTopic: String = "",
    val selectedStyle: VisualStyle = VisualStyle.CINEMATIC,
    val selectedCamera: CameraMovement = CameraMovement.DOLLY_IN,
    val isWorking: Boolean = false,
    val enhancedResult: String = "",
    val translatedResult: String = "",
    val storyboardScenes: List<StoryboardScene> = emptyList(),
    val consistentScenes: List<String> = emptyList(),
    val imageDerivedPrompt: String = "",
    val aiNotice: String? = null
)

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "StudioViewModel"
    }

    private val db = StudioDatabase.getInstance(application)
    private val dao = db.studioDao()
    private val prefsRepo = PreferencesRepository(application)
    private val aiRepo: AiRepository = PlaceholderAiRepository(application)
    private val videoEngine = LocalVideoRenderEngine(application)
    private val audioEngine = LocalAudioEngine(application)

    val preferences: StateFlow<StudioPreferences> = prefsRepo.preferences
        .stateIn(viewModelScope, SharingStarted.Eagerly, StudioPreferences())

    val projects: StateFlow<List<ProjectEntity>> = dao.observeProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val jobs: StateFlow<List<GenerationJobEntity>> = dao.observeJobs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val characters: StateFlow<List<CharacterEntity>> = dao.observeCharacters()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val promptTemplates: StateFlow<List<PromptTemplateEntity>> = dao.observePrompts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloads: StateFlow<List<DownloadRecordEntity>> = dao.observeDownloads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalVideosCount: StateFlow<Int> = dao.observeCompletedCount(MediaType.VIDEO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalImagesCount: StateFlow<Int> = dao.observeCompletedCount(MediaType.IMAGE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalAudioCount: StateFlow<Int> = dao.observeCompletedCount(MediaType.AUDIO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalStorageBytes: StateFlow<Long> = dao.observeTotalStorageBytes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    private val _videoUi = MutableStateFlow(VideoGeneratorUiState())
    val videoUi: StateFlow<VideoGeneratorUiState> = _videoUi.asStateFlow()

    private val _imageUi = MutableStateFlow(ImageStudioUiState())
    val imageUi: StateFlow<ImageStudioUiState> = _imageUi.asStateFlow()

    private val _audioUi = MutableStateFlow(AudioStudioUiState())
    val audioUi: StateFlow<AudioStudioUiState> = _audioUi.asStateFlow()

    private val _promptUi = MutableStateFlow(PromptAssistantUiState())
    val promptUi: StateFlow<PromptAssistantUiState> = _promptUi.asStateFlow()

    private val _editorUi = MutableStateFlow(EditorTimelineState())
    val editorUi: StateFlow<EditorTimelineState> = _editorUi.asStateFlow()

    private val _notifications = MutableStateFlow<List<StudioNotification>>(emptyList())
    val notifications: StateFlow<List<StudioNotification>> = _notifications.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _selfHostedStatus = MutableStateFlow<String?>(null)
    val selfHostedStatus: StateFlow<String?> = _selfHostedStatus.asStateFlow()

    private val _navigateToRoute = MutableStateFlow<StudioRoute?>(null)
    val navigateToRoute: StateFlow<StudioRoute?> = _navigateToRoute.asStateFlow()

    private val activeRenderJobs = mutableMapOf<Long, Job>()
    private var isQueueRunning = false
    private val pendingQueue = ArrayDeque<QueuedVideoTask>()

    private data class QueuedVideoTask(
        val jobId: Long,
        val prompt: String,
        val mode: VideoGenerationMode,
        val model: VideoModelSpec,
        val config: AdvancedVideoConfig,
        val referenceImageUri: String?,
        val secondaryImageUri: String?,
        val character: CharacterEntity?
    )

    init {
        seedInitialData()
    }

    private fun seedInitialData() {
        viewModelScope.launch {
            if (dao.countCharacters() == 0) {
                dao.insertCharacter(
                    CharacterEntity(
                        name = "Kael Vance",
                        role = "Cyberpunk Recon Pilot",
                        faceDescription = "Sharp jawline, glowing cyan ocular reflection, subtle scar above left brow",
                        outfitDescription = "Iridescent obsidian & navy tactical jacket with neon cyan seam piping",
                        poseReference = "Heroic three-quarter stance under rim light",
                        styleTag = "Photorealistic Sci-Fi",
                        consistencySeed = 774920L,
                        drawableResName = "img_char_cyber"
                    )
                )
                dao.insertCharacter(
                    CharacterEntity(
                        name = "Aoi Ren",
                        role = "Astral Cyber-Mage",
                        faceDescription = "Luminous violet eyes, silver-indigo bob haircut, calm expression",
                        outfitDescription = "Flowing violet-cyan holographic trench coat with glowing runes",
                        poseReference = "Floating cast pose with energy particles",
                        styleTag = "Modern Anime",
                        consistencySeed = 918273L,
                        drawableResName = "img_char_anime"
                    )
                )
            }
            if (dao.countPrompts() == 0) {
                dao.insertPrompts(
                    listOf(
                        PromptTemplateEntity(
                            title = "Neo-Tokyo Rain Reflection",
                            category = "Cyberpunk",
                            style = "Cinematic",
                            promptEn = "Slow dolly-in across a rain-slicked obsidian boulevard reflecting neon cyan and violet holograms, volumetric mist, 35mm anamorphic lens.",
                            promptId = "Kamera dolly-in lambat melintasi jalanan obsidian basah hujan yang memantulkan hologram cyan dan ungu, kabut volumetrik, lensa anamorfik 35mm.",
                            cameraHint = "Dolly In • 24fps • 16:9"
                        ),
                        PromptTemplateEntity(
                            title = "VisionOS Glass Studio Reveal",
                            category = "Product",
                            style = "Realistic",
                            promptEn = "Minimalist translucent liquid glass monolith floating in dark obsidian space, soft cyan rim light, gentle 360 orbital rotation.",
                            promptId = "Monolit kaca cair transparan minimalis melayang di ruang obsidian gelap, cahaya tepi cyan lembut, rotasi orbit 360 derajat.",
                            cameraHint = "Orbit • 30fps • 16:9"
                        ),
                        PromptTemplateEntity(
                            title = "Anime Skybridge Twilight",
                            category = "Anime",
                            style = "Anime",
                            promptEn = "Anime character standing on a futuristic glass skybridge at sunset, wind flowing through coat, glowing purple clouds, crane up.",
                            promptId = "Karakter anime berdiri di jembatan kaca futuristik saat senja, angin mengibarkan mantel, awan ungu bercahaya, kamera crane naik.",
                            cameraHint = "Crane Up • 24fps • 9:16"
                        ),
                        PromptTemplateEntity(
                            title = "Volcanic Coast Drone Sweep",
                            category = "Nature",
                            style = "Cinematic",
                            promptEn = "Aerial drone sweep over black volcanic basalt cliffs meeting glowing turquoise ocean waves at blue hour.",
                            promptId = "Sapu kamera drone di atas tebing basal vulkanik hitam yang bertemu ombak laut biru pirus saat senja biru.",
                            cameraHint = "Pan Right • 30fps • 21:9"
                        )
                    )
                )
            }
        }
    }

    fun consumeNavigation() {
        _navigateToRoute.value = null
    }

    fun consumeSnackbar() {
        _snackbarMessage.value = null
    }

    fun postSnackbar(msg: String) {
        _snackbarMessage.value = msg
    }

    private fun pushNotification(title: String, message: String) {
        _notifications.update { list ->
            listOf(
                StudioNotification(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    message = message
                )
            ) + list.take(29)
        }
    }

    fun markAllNotificationsRead() {
        _notifications.update { list -> list.map { it.copy(isRead = true) } }
    }

    // Settings updates
    fun setDarkMode(dark: Boolean) = viewModelScope.launch { prefsRepo.setDarkMode(dark) }
    fun setLanguage(lang: AppLanguage) = viewModelScope.launch { prefsRepo.setLanguage(lang) }
    fun setSelfHostedEndpoint(url: String) = viewModelScope.launch { prefsRepo.setSelfHostedEndpoint(url) }
    fun setUserDisplayName(name: String) = viewModelScope.launch { prefsRepo.setUserDisplayName(name) }
    fun setDefaultFps(fps: Int) = viewModelScope.launch { prefsRepo.setDefaultFps(fps) }
    fun setAutosaveEnabled(enabled: Boolean) = viewModelScope.launch { prefsRepo.setAutosaveEnabled(enabled) }

    fun testSelfHostedServer(url: String) {
        viewModelScope.launch {
            _selfHostedStatus.value = "Testing..."
            val res = OpenSourceAdapterClient.testConnection(url, preferences.value.language)
            _selfHostedStatus.value = when (res) {
                is AdapterCheckResult.Ready -> res.detail
                is AdapterCheckResult.Unavailable -> res.reason
            }
        }
    }

    // Video Generator actions
    fun updateVideoMode(mode: VideoGenerationMode) = _videoUi.update { it.copy(selectedMode = mode) }
    fun updateVideoModel(model: VideoModelSpec) = _videoUi.update { it.copy(selectedModel = model, modelNoticeMessage = null) }
    fun updateVideoPrompt(prompt: String) = _videoUi.update { it.copy(prompt = prompt) }
    fun updateVideoRefImage(uri: String?) = _videoUi.update { it.copy(referenceImageUri = uri) }
    fun updateVideoSecondaryImage(uri: String?) = _videoUi.update { it.copy(secondaryImageUri = uri) }
    fun updateVideoCharacter(character: CharacterEntity?) = _videoUi.update { it.copy(selectedCharacter = character) }
    fun updateVideoConfig(transform: (AdvancedVideoConfig) -> AdvancedVideoConfig) {
        _videoUi.update { it.copy(config = transform(it.config)) }
    }

    fun enhanceVideoPrompt() {
        viewModelScope.launch {
            _videoUi.update { it.copy(isEnhancingPrompt = true) }
            val state = _videoUi.value
            val charPrefix = state.selectedCharacter?.let { "${it.name} (${it.outfitDescription}), " } ?: ""
            val out = aiRepo.enhancePrompt(
                rawPrompt = charPrefix + state.prompt,
                style = state.config.style,
                language = preferences.value.language
            )
            _videoUi.update {
                it.copy(
                    prompt = out.data,
                    isEnhancingPrompt = false,
                    modelNoticeMessage = out.noticeMessage
                )
            }
        }
    }

    fun submitVideoGeneration() {
        val state = _videoUi.value
        val lang = preferences.value.language
        val model = state.selectedModel

        if (model.availability == ModelAvailabilityStatus.UNAVAILABLE_NO_PROVIDER) {
            val reason = if (lang == AppLanguage.ID) {
                "${model.name}: Belum tersedia karena tidak ada akses provider resmi. Silakan pilih 'Videon Local Motion v1' atau Adapter Open-Source Self-Hosted."
            } else {
                "${model.name}: Not available — official backend provider access is not configured. Switch to 'Videon Local Motion v1' or Self-Hosted Open-Source Adapter."
            }
            _videoUi.update { it.copy(modelNoticeMessage = reason) }
            postSnackbar(reason)
            return
        }

        val effectivePrompt = buildString {
            state.selectedCharacter?.let {
                append("${it.name}, ${it.faceDescription}, ${it.outfitDescription}. ")
            }
            append(state.prompt.trim().ifBlank { "Futuristic luminous glass cinema scene" })
        }

        viewModelScope.launch {
            val projectId = if (preferences.value.autosaveEnabled) {
                dao.insertProject(
                    ProjectEntity(
                        title = effectivePrompt.take(36),
                        mode = state.selectedMode.labelEn,
                        modelId = model.id,
                        prompt = effectivePrompt,
                        aspectRatio = state.config.aspectRatio,
                        resolution = state.config.resolution,
                        durationSec = state.config.durationSec,
                        thumbnailResName = state.selectedCharacter?.drawableResName ?: "img_scene_preset"
                    )
                )
            } else null

            val jobId = dao.insertJob(
                GenerationJobEntity(
                    projectId = projectId,
                    title = "${state.selectedMode.labelEn}: ${effectivePrompt.take(28)}",
                    mediaType = MediaType.VIDEO,
                    mode = state.selectedMode.labelEn,
                    modelId = model.id,
                    modelName = model.name,
                    prompt = effectivePrompt,
                    status = JobStatus.QUEUED,
                    progress = 0f,
                    durationSec = state.config.durationSec,
                    resolution = state.config.resolution,
                    aspectRatio = state.config.aspectRatio,
                    fps = state.config.fps,
                    seed = state.config.seed
                )
            )

            _videoUi.update { it.copy(activeJobId = jobId, modelNoticeMessage = null) }
            pendingQueue.addLast(
                QueuedVideoTask(
                    jobId = jobId,
                    prompt = effectivePrompt,
                    mode = state.selectedMode,
                    model = model,
                    config = state.config,
                    referenceImageUri = state.referenceImageUri,
                    secondaryImageUri = state.secondaryImageUri,
                    character = state.selectedCharacter
                )
            )
            pushNotification(
                title = if (lang == AppLanguage.ID) "Ditambahkan ke Antrean" else "Added to Render Queue",
                message = "${model.name} • ${state.config.resolution} ${state.config.aspectRatio}"
            )
            processQueueIfIdle()
        }
    }

    private fun processQueueIfIdle() {
        if (isQueueRunning) return
        val nextTask = pendingQueue.removeFirstOrNull() ?: return
        isQueueRunning = true

        val job = viewModelScope.launch {
            try {
                dao.updateJobProgress(nextTask.jobId, JobStatus.RENDERING, 0.05f, null, 0L, null, null)

                if (nextTask.model.availability == ModelAvailabilityStatus.SELF_HOSTED_ADAPTER) {
                    val endpoint = preferences.value.selfHostedEndpoint
                    val check = OpenSourceAdapterClient.testConnection(endpoint, preferences.value.language)
                    if (check is AdapterCheckResult.Unavailable) {
                        dao.updateJobProgress(
                            nextTask.jobId,
                            JobStatus.FAILED,
                            0f,
                            null,
                            0L,
                            check.reason,
                            System.currentTimeMillis()
                        )
                        _videoUi.update { it.copy(modelNoticeMessage = check.reason) }
                        postSnackbar(check.reason)
                        return@launch
                    }
                    val dispatch = OpenSourceAdapterClient.dispatchJob(
                        baseUrl = endpoint,
                        model = nextTask.model,
                        mode = nextTask.mode,
                        prompt = nextTask.prompt,
                        config = nextTask.config
                    )
                    dispatch.onSuccess { resp ->
                        val msg = "Submitted to self-hosted worker: job_id=${resp.job_id ?: "local"} (${resp.status ?: "queued"})"
                        dao.updateJobProgress(nextTask.jobId, JobStatus.COMPLETED, 1f, resp.video_url, 0L, null, System.currentTimeMillis())
                        postSnackbar(msg)
                    }.onFailure { err ->
                        dao.updateJobProgress(nextTask.jobId, JobStatus.FAILED, 0f, null, 0L, err.localizedMessage, System.currentTimeMillis())
                        postSnackbar(err.localizedMessage ?: "Worker error")
                    }
                    return@launch
                }

                // Active Free On-Device MediaCodec MP4 Engine
                val outputFile = videoEngine.renderGeneratedVideo(
                    jobId = nextTask.jobId,
                    prompt = nextTask.prompt,
                    mode = nextTask.mode,
                    config = nextTask.config,
                    referenceImageUri = nextTask.referenceImageUri,
                    secondImageUri = nextTask.secondaryImageUri,
                    characterDrawableName = nextTask.character?.drawableResName,
                    characterImageUri = nextTask.character?.imageUriString,
                    onProgress = { progress ->
                        dao.updateJobProgress(
                            nextTask.jobId,
                            JobStatus.RENDERING,
                            progress.coerceIn(0.05f, 0.99f),
                            null,
                            0L,
                            null,
                            null
                        )
                    }
                )

                val size = outputFile.length()
                dao.updateJobProgress(
                    id = nextTask.jobId,
                    status = JobStatus.COMPLETED,
                    progress = 1f,
                    outputPath = outputFile.absolutePath,
                    sizeBytes = size,
                    error = null,
                    completedAt = System.currentTimeMillis()
                )
                val jobEntity = dao.getJobById(nextTask.jobId)
                if (jobEntity?.projectId != null) {
                    val existing = projects.value.firstOrNull { it.id == jobEntity.projectId }
                    if (existing != null) {
                        dao.updateProject(existing.copy(outputPath = outputFile.absolutePath, updatedAt = System.currentTimeMillis()))
                    }
                }
                _videoUi.update { it.copy(latestCompletedVideoPath = outputFile.absolutePath) }
                pushNotification(
                    title = "Render Complete",
                    message = "${outputFile.name} (${size / 1024} KB)"
                )
                postSnackbar("MP4 Ready: ${outputFile.name}")
            } catch (e: CancellationException) {
                dao.updateJobProgress(nextTask.jobId, JobStatus.CANCELLED, 0f, null, 0L, "Cancelled by user", System.currentTimeMillis())
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Render failed", e)
                dao.updateJobProgress(
                    nextTask.jobId,
                    JobStatus.FAILED,
                    0f,
                    null,
                    0L,
                    e.localizedMessage ?: "Encoding failed",
                    System.currentTimeMillis()
                )
                postSnackbar("Render failed: ${e.localizedMessage}")
            } finally {
                activeRenderJobs.remove(nextTask.jobId)
                isQueueRunning = false
                processQueueIfIdle()
            }
        }
        activeRenderJobs[nextTask.jobId] = job
    }

    fun cancelJob(jobId: Long) {
        pendingQueue.removeAll { it.jobId == jobId }
        activeRenderJobs[jobId]?.cancel()
        viewModelScope.launch {
            dao.updateJobProgress(jobId, JobStatus.CANCELLED, 0f, null, 0L, "Cancelled", System.currentTimeMillis())
        }
    }

    fun toggleJobFavorite(job: GenerationJobEntity) {
        viewModelScope.launch { dao.setJobFavorite(job.id, !job.isFavorite) }
    }

    fun deleteJob(jobId: Long) {
        viewModelScope.launch { dao.deleteJob(jobId) }
    }

    fun exportMediaToDevice(path: String, title: String, type: MediaType) {
        viewModelScope.launch {
            val res = MediaExportHelper.exportToDeviceGallery(getApplication(), path, title, type)
            res.onSuccess { dest ->
                val size = File(path).length()
                dao.insertDownload(
                    DownloadRecordEntity(
                        assetTitle = title,
                        mediaType = type,
                        filePath = path,
                        exportDestination = dest,
                        fileSizeBytes = size
                    )
                )
                postSnackbar("Saved to $dest")
                pushNotification("Exported", "$title -> $dest")
            }.onFailure { err ->
                postSnackbar("Export failed: ${err.localizedMessage}")
            }
        }
    }

    fun shareMedia(path: String, type: MediaType, title: String) {
        MediaExportHelper.shareFile(getApplication(), path, type, title)
    }

    // Image Studio
    fun updateImageMode(mode: ImageGenerationMode) = _imageUi.update { it.copy(selectedMode = mode) }
    fun updateImagePrompt(prompt: String) = _imageUi.update { it.copy(prompt = prompt) }
    fun updateImageStyle(style: VisualStyle) = _imageUi.update { it.copy(style = style) }
    fun updateImageAspect(aspect: String) = _imageUi.update { it.copy(aspectRatio = aspect) }
    fun updateImageRefUri(uri: String?) = _imageUi.update { it.copy(referenceImageUri = uri) }

    fun generateStudioImage() {
        val state = _imageUi.value
        viewModelScope.launch {
            _imageUi.update { it.copy(isGenerating = true) }
            val refBitmap = state.referenceImageUri?.let { decodeBitmap(it) }
            val out = aiRepo.generateStudioImage(
                prompt = state.prompt,
                mode = state.selectedMode,
                style = state.style,
                aspectRatio = state.aspectRatio,
                referenceBitmap = refBitmap
            )
            val filePath = out.data.filePath
            if (filePath != null) {
                dao.insertJob(
                    GenerationJobEntity(
                        title = "${state.selectedMode.labelEn}: ${state.prompt.take(24).ifBlank { "Graded Ref" }}",
                        mediaType = MediaType.IMAGE,
                        mode = state.selectedMode.labelEn,
                        modelId = "local_color_grade",
                        modelName = "Studio Reference Grader",
                        prompt = state.prompt,
                        status = JobStatus.COMPLETED,
                        progress = 1f,
                        aspectRatio = state.aspectRatio,
                        outputFilePath = filePath,
                        fileSizeBytes = File(filePath).length(),
                        completedAt = System.currentTimeMillis()
                    )
                )
            }
            _imageUi.update {
                it.copy(
                    isGenerating = false,
                    latestResult = out.data,
                    noticeBanner = out.noticeMessage
                )
            }
        }
    }

    fun sendImageToVideoStudio(uriOrPath: String) {
        val uriString = if (uriOrPath.startsWith("/")) Uri.fromFile(File(uriOrPath)).toString() else uriOrPath
        _videoUi.update {
            it.copy(
                selectedMode = VideoGenerationMode.IMAGE_TO_VIDEO,
                referenceImageUri = uriString
            )
        }
        _navigateToRoute.value = StudioRoute.AI_VIDEO
    }

    // Audio Studio
    fun updateAudioMode(mode: AudioGenerationMode) = _audioUi.update { it.copy(selectedMode = mode) }
    fun updateAudioText(text: String) = _audioUi.update { it.copy(textInput = text) }
    fun updateAudioLang(tag: String) = _audioUi.update { it.copy(voiceLangTag = tag) }
    fun updateAudioPitch(p: Float) = _audioUi.update { it.copy(pitch = p) }
    fun updateAudioRate(r: Float) = _audioUi.update { it.copy(speechRate = r) }
    fun updateAudioPreset(preset: String) = _audioUi.update { it.copy(selectedPreset = preset) }
    fun updateAudioDuration(sec: Int) = _audioUi.update { it.copy(durationSec = sec) }

    fun synthesizeAudio() {
        val state = _audioUi.value
        viewModelScope.launch {
            _audioUi.update { it.copy(isSynthesizing = true, statusMessage = null) }
            when (state.selectedMode) {
                AudioGenerationMode.DIALOGUE_GEN -> {
                    val out = aiRepo.generateDialogue(
                        sceneContext = state.textInput,
                        speakers = listOf("Kael", "Aoi"),
                        language = preferences.value.language
                    )
                    _audioUi.update {
                        it.copy(
                            isSynthesizing = false,
                            generatedDialogue = out.data,
                            statusMessage = out.noticeMessage
                        )
                    }
                }
                AudioGenerationMode.SOUND_EFFECTS, AudioGenerationMode.BACKGROUND_MUSIC -> {
                    val file = audioEngine.synthesizeProceduralAudio(state.selectedPreset, state.durationSec)
                    dao.insertJob(
                        GenerationJobEntity(
                            title = "${state.selectedMode.labelEn}: ${state.selectedPreset}",
                            mediaType = MediaType.AUDIO,
                            mode = state.selectedMode.labelEn,
                            modelId = "local_pcm_synth",
                            modelName = "Videon PCM Synth",
                            prompt = state.selectedPreset,
                            status = JobStatus.COMPLETED,
                            progress = 1f,
                            durationSec = state.durationSec,
                            outputFilePath = file.absolutePath,
                            fileSizeBytes = file.length(),
                            completedAt = System.currentTimeMillis()
                        )
                    )
                    _audioUi.update {
                        it.copy(
                            isSynthesizing = false,
                            latestAudioPath = file.absolutePath,
                            statusMessage = "WAV generated: ${file.name} (${file.length() / 1024} KB)"
                        )
                    }
                }
                else -> {
                    val sampleText = state.textInput.trim().ifBlank {
                        if (state.voiceLangTag == "id") {
                            "Selamat datang di Videon AI Studio. Ciptakan apa saja, bayangkan segalanya."
                        } else {
                            "Welcome to Videon AI Studio. Create anything, imagine everything."
                        }
                    }
                    val res = audioEngine.synthesizeSpeechToWav(
                        text = sampleText,
                        languageTag = state.voiceLangTag,
                        pitch = state.pitch,
                        speechRate = state.speechRate
                    )
                    res.onSuccess { file ->
                        dao.insertJob(
                            GenerationJobEntity(
                                title = "TTS (${state.voiceLangTag.uppercase()}): ${sampleText.take(24)}",
                                mediaType = MediaType.AUDIO,
                                mode = state.selectedMode.labelEn,
                                modelId = "android_tts_wav",
                                modelName = "Android Native TTS",
                                prompt = sampleText,
                                status = JobStatus.COMPLETED,
                                progress = 1f,
                                outputFilePath = file.absolutePath,
                                fileSizeBytes = file.length(),
                                completedAt = System.currentTimeMillis()
                            )
                        )
                        _audioUi.update {
                            it.copy(
                                isSynthesizing = false,
                                latestAudioPath = file.absolutePath,
                                statusMessage = "Voice WAV ready: ${file.name}"
                            )
                        }
                    }.onFailure { err ->
                        _audioUi.update {
                            it.copy(
                                isSynthesizing = false,
                                statusMessage = "TTS error: ${err.localizedMessage}"
                            )
                        }
                    }
                }
            }
        }
    }

    fun toggleAudioPlayback(path: String) {
        if (_audioUi.value.isPlaying) {
            audioEngine.stopPlayback()
            _audioUi.update { it.copy(isPlaying = false) }
        } else {
            _audioUi.update { it.copy(isPlaying = true) }
            audioEngine.playAudioFile(path) {
                _audioUi.update { it.copy(isPlaying = false) }
            }
        }
    }

    fun sendAudioToEditor(path: String, label: String) {
        _editorUi.update { it.copy(audioTrackPath = path, audioTrackName = label) }
        _navigateToRoute.value = StudioRoute.VIDEO_EDITOR
    }

    // Video Editor
    fun openVideoInEditor(path: String?, uriString: String?, title: String) {
        _editorUi.update {
            it.copy(
                sourceVideoPath = path,
                sourceVideoUriString = uriString,
                sourceTitle = title,
                lastExportedPath = null
            )
        }
        _navigateToRoute.value = StudioRoute.VIDEO_EDITOR
    }

    fun updateEditorState(transform: (EditorTimelineState) -> EditorTimelineState) {
        _editorUi.update(transform)
    }

    fun exportEditedVideo() {
        val state = _editorUi.value
        if (state.isExporting) return
        viewModelScope.launch {
            _editorUi.update { it.copy(isExporting = true, exportProgress = 0.05f) }
            try {
                val outFile = videoEngine.exportEditedVideo(state) { p ->
                    _editorUi.update { it.copy(exportProgress = p) }
                }
                dao.insertJob(
                    GenerationJobEntity(
                        title = "Edited: ${state.sourceTitle}",
                        mediaType = MediaType.VIDEO,
                        mode = "Timeline Editor Export",
                        modelId = "videon_local_editor",
                        modelName = "Videon Timeline Engine",
                        prompt = state.textOverlay.ifBlank { state.subtitleText.ifBlank { state.sourceTitle } },
                        status = JobStatus.COMPLETED,
                        progress = 1f,
                        resolution = state.resizeResolution,
                        aspectRatio = state.cropAspectRatio,
                        outputFilePath = outFile.absolutePath,
                        fileSizeBytes = outFile.length(),
                        completedAt = System.currentTimeMillis()
                    )
                )
                _editorUi.update {
                    it.copy(
                        isExporting = false,
                        exportProgress = 1f,
                        lastExportedPath = outFile.absolutePath
                    )
                }
                postSnackbar("Exported MP4: ${outFile.name}")
                pushNotification("Editor Export Complete", outFile.name)
            } catch (e: Exception) {
                Log.e(TAG, "Editor export failed", e)
                _editorUi.update { it.copy(isExporting = false, exportProgress = 0f) }
                postSnackbar("Export failed: ${e.localizedMessage}")
            }
        }
    }

    // Character Studio
    fun saveCharacter(
        name: String,
        role: String,
        faceDesc: String,
        outfitDesc: String,
        poseRef: String,
        styleTag: String,
        imageUri: String?
    ) {
        viewModelScope.launch {
            dao.insertCharacter(
                CharacterEntity(
                    name = name.trim().ifBlank { "New Character" },
                    role = role.trim().ifBlank { "Protagonist" },
                    faceDescription = faceDesc.trim(),
                    outfitDescription = outfitDesc.trim(),
                    poseReference = poseRef.trim(),
                    styleTag = styleTag.trim(),
                    consistencySeed = (100000L..999999L).random(),
                    imageUriString = imageUri,
                    drawableResName = if (imageUri == null) "img_char_cyber" else null
                )
            )
            postSnackbar("Character saved to library")
        }
    }

    fun deleteCharacter(id: Long) {
        viewModelScope.launch { dao.deleteCharacter(id) }
    }

    fun useCharacterInVideoStudio(character: CharacterEntity) {
        _videoUi.update {
            it.copy(
                selectedCharacter = character,
                selectedMode = VideoGenerationMode.CHARACTER_REF,
                config = it.config.copy(seed = character.consistencySeed)
            )
        }
        _navigateToRoute.value = StudioRoute.AI_VIDEO
    }

    // Prompt Assistant & Library
    fun updatePromptAssistantTopic(topic: String) = _promptUi.update { it.copy(inputTopic = topic) }
    fun updatePromptAssistantStyle(style: VisualStyle) = _promptUi.update { it.copy(selectedStyle = style) }
    fun updatePromptAssistantCamera(cam: CameraMovement) = _promptUi.update { it.copy(selectedCamera = cam) }

    fun runPromptEnhanceAndTranslate() {
        val state = _promptUi.value
        viewModelScope.launch {
            _promptUi.update { it.copy(isWorking = true) }
            val enhanced = aiRepo.createCinematicPrompt(state.inputTopic, state.selectedCamera, state.selectedStyle)
            val translated = aiRepo.translateIndonesianToEnglish(state.inputTopic)
            _promptUi.update {
                it.copy(
                    isWorking = false,
                    enhancedResult = enhanced.data,
                    translatedResult = translated.data,
                    aiNotice = enhanced.noticeMessage
                )
            }
        }
    }

    fun runStoryboardGenerator() {
        val state = _promptUi.value
        viewModelScope.launch {
            _promptUi.update { it.copy(isWorking = true) }
            val board = aiRepo.generateStoryboard(state.inputTopic, 4, preferences.value.language)
            _promptUi.update {
                it.copy(
                    isWorking = false,
                    storyboardScenes = board.data,
                    aiNotice = board.noticeMessage
                )
            }
        }
    }

    fun runImageToPrompt(uriString: String) {
        viewModelScope.launch {
            _promptUi.update { it.copy(isWorking = true) }
            val bmp = decodeBitmap(uriString)
            if (bmp != null) {
                val out = aiRepo.promptFromImage(bmp, _promptUi.value.inputTopic, preferences.value.language)
                _promptUi.update {
                    it.copy(
                        isWorking = false,
                        imageDerivedPrompt = out.data,
                        aiNotice = out.noticeMessage
                    )
                }
            } else {
                _promptUi.update { it.copy(isWorking = false) }
                postSnackbar("Could not read selected image")
            }
        }
    }

    fun generateCharacterConsistentScenes(character: CharacterEntity) {
        viewModelScope.launch {
            _promptUi.update { it.copy(isWorking = true) }
            val out = aiRepo.generateConsistentScenePrompts(
                character.name,
                "${character.faceDescription}, ${character.outfitDescription}",
                3
            )
            _promptUi.update {
                it.copy(
                    isWorking = false,
                    consistentScenes = out.data,
                    aiNotice = out.noticeMessage
                )
            }
            _navigateToRoute.value = StudioRoute.PROMPT_LIBRARY
        }
    }

    fun usePromptInVideoStudio(prompt: String) {
        _videoUi.update { it.copy(prompt = prompt) }
        _navigateToRoute.value = StudioRoute.AI_VIDEO
    }

    fun togglePromptFavorite(item: PromptTemplateEntity) {
        viewModelScope.launch { dao.setPromptFavorite(item.id, !item.isFavorite) }
    }

    fun toggleProjectFavorite(project: ProjectEntity) {
        viewModelScope.launch { dao.updateProject(project.copy(isFavorite = !project.isFavorite)) }
    }

    fun deleteProject(id: Long) {
        viewModelScope.launch { dao.deleteProject(id) }
    }

    fun openProjectInStudio(project: ProjectEntity) {
        _videoUi.update {
            it.copy(
                prompt = project.prompt,
                selectedModel = ModelCatalog.findById(project.modelId),
                config = it.config.copy(
                    aspectRatio = project.aspectRatio,
                    resolution = project.resolution,
                    durationSec = project.durationSec
                ),
                latestCompletedVideoPath = project.outputPath
            )
        }
        _navigateToRoute.value = StudioRoute.AI_VIDEO
    }

    private fun decodeBitmap(uriString: String): Bitmap? {
        return try {
            val uri = Uri.parse(uriString)
            getApplication<Application>().contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it)
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun onCleared() {
        audioEngine.stopPlayback()
        super.onCleared()
    }
}
