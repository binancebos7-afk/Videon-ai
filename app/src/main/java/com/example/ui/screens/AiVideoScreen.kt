package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.MovieCreation
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.engine.ModelCatalog
import com.example.data.local.CharacterEntity
import com.example.data.local.GenerationJobEntity
import com.example.domain.model.AdvancedVideoConfig
import com.example.domain.model.AppLanguage
import com.example.domain.model.CameraAngle
import com.example.domain.model.CameraMovement
import com.example.domain.model.JobStatus
import com.example.domain.model.MediaType
import com.example.domain.model.ModelAvailabilityStatus
import com.example.domain.model.VideoGenerationMode
import com.example.domain.model.VideoModelSpec
import com.example.domain.model.VisualStyle
import com.example.ui.components.InlineMp4PlayerCard
import com.example.ui.i18n.AppStrings
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldActive
import com.example.ui.theme.GlassChip
import com.example.ui.theme.GlassStatusBadge
import com.example.ui.theme.GradientPrimaryButton
import com.example.ui.theme.LiquidGlassCard
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SectionHeader
import com.example.ui.theme.SlateMuted
import com.example.ui.viewmodel.VideoGeneratorUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiVideoScreen(
    language: AppLanguage,
    uiState: VideoGeneratorUiState,
    characters: List<CharacterEntity>,
    jobs: List<GenerationJobEntity>,
    onSelectMode: (VideoGenerationMode) -> Unit,
    onSelectModel: (VideoModelSpec) -> Unit,
    onPromptChange: (String) -> Unit,
    onEnhancePrompt: () -> Unit,
    onSelectPrimaryMedia: (String?) -> Unit,
    onSelectSecondaryMedia: (String?) -> Unit,
    onSelectCharacter: (CharacterEntity?) -> Unit,
    onUpdateConfig: ((AdvancedVideoConfig) -> AdvancedVideoConfig) -> Unit,
    onSubmitRender: () -> Unit,
    onExportVideo: (String, String) -> Unit,
    onShareVideo: (String, String) -> Unit,
    onOpenInEditor: (String, String) -> Unit
) {
    val isId = language == AppLanguage.ID
    var showModelSheet by remember { mutableStateOf(false) }
    var showAdvanced by remember { mutableStateOf(true) }

    val primaryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) onSelectPrimaryMedia(uri.toString())
    }

    val secondaryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) onSelectSecondaryMedia(uri.toString())
    }

    val activeJob = jobs.firstOrNull { it.id == uiState.activeJobId }

    if (showModelSheet) {
        ModalBottomSheet(onDismissRequest = { showModelSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = AppStrings.selectModel(language),
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (isId) {
                        "Model proprietary tanpa integrasi resmi ditandai 'Belum tersedia'. Pilih mesin lokal gratis atau adapter open-source."
                    } else {
                        "Proprietary models without official backend access are marked 'Not Available'. Use the free local engine or open-source adapter."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 28.dp)
                ) {
                    items(ModelCatalog.allModels, key = { it.id }) { model ->
                        val color = when (model.availability) {
                            ModelAvailabilityStatus.ACTIVE_LOCAL -> EmeraldActive
                            ModelAvailabilityStatus.SELF_HOSTED_ADAPTER -> ElectricBlue
                            ModelAvailabilityStatus.UNAVAILABLE_NO_PROVIDER -> SlateMuted
                        }
                        LiquidGlassCard(
                            accentGlow = if (model.id == uiState.selectedModel.id) NeonCyan else null,
                            onClick = {
                                onSelectModel(model)
                                showModelSheet = false
                            }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(model.name, style = MaterialTheme.typography.titleMedium)
                                    Text(model.provider, style = MaterialTheme.typography.labelMedium, color = NeonCyan)
                                }
                                GlassStatusBadge(
                                    text = if (isId) model.integrationStatusId else model.integrationStatusEn,
                                    color = color
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = if (isId) model.descriptionId else model.descriptionEn,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "${model.qualityRating} • Max ${model.maxDurationSec}s • ${model.supportedResolutions.joinToString("/")} • Audio: ${if (model.supportsAudio) "Yes" else "No"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionHeader(
                title = if (isId) "AI Video Studio" else "AI Video Generator",
                subtitle = AppStrings.gpuQueueInfo(
                    language,
                    jobs.count { it.status == JobStatus.RENDERING },
                    jobs.count { it.status == JobStatus.QUEUED }
                ),
                icon = Icons.Rounded.MovieCreation
            )
        }

        // 14 Generation Modes
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(VideoGenerationMode.entries) { mode ->
                    GlassChip(
                        label = if (isId) mode.labelId else mode.labelEn,
                        selected = uiState.selectedMode == mode,
                        onClick = { onSelectMode(mode) },
                        testTag = "video_mode_${mode.id}"
                    )
                }
            }
        }

        // Model Selector Card
        item {
            val m = uiState.selectedModel
            val badgeColor = when (m.availability) {
                ModelAvailabilityStatus.ACTIVE_LOCAL -> EmeraldActive
                ModelAvailabilityStatus.SELF_HOSTED_ADAPTER -> ElectricBlue
                ModelAvailabilityStatus.UNAVAILABLE_NO_PROVIDER -> AmberWarning
            }
            LiquidGlassCard(
                onClick = { showModelSheet = true },
                accentGlow = badgeColor
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                        Icon(Icons.Rounded.Memory, contentDescription = null, tint = NeonCyan)
                        Column {
                            Text(m.name, style = MaterialTheme.typography.titleMedium)
                            Text("${m.provider} • Max ${m.maxDurationSec}s • ${m.qualityRating}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    GlassStatusBadge(
                        text = if (isId) m.integrationStatusId else m.integrationStatusEn,
                        color = badgeColor
                    )
                }
                if (m.availability == ModelAvailabilityStatus.UNAVAILABLE_NO_PROVIDER) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.WarningAmber, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(18.dp))
                        Text(
                            text = AppStrings.unavailableReason(language),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AmberWarning
                        )
                    }
                    TextButton(onClick = { onSelectModel(ModelCatalog.allModels.first()) }) {
                        Text(if (isId) "Ganti ke Videon Local Motion (Gratis)" else "Switch to Videon Local Motion (Free)")
                    }
                }
            }
        }

        // Prompt Input + Enhancer
        item {
            LiquidGlassCard {
                OutlinedTextField(
                    value = uiState.prompt,
                    onValueChange = onPromptChange,
                    modifier = Modifier.fillMaxWidth().testTag("video_prompt_input"),
                    minLines = 3,
                    maxLines = 6,
                    label = { Text(if (isId) "Prompt Adegan Video" else "Video Scene Prompt") },
                    placeholder = { Text(AppStrings.promptPlaceholder(language)) }
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onEnhancePrompt,
                        enabled = !uiState.isEnhancingPrompt,
                        modifier = Modifier.testTag("enhance_video_prompt_btn")
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (uiState.isEnhancingPrompt) "..." else AppStrings.enhancePrompt(language))
                    }
                    Text(
                        text = "${uiState.prompt.length} chars",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!uiState.modelNoticeMessage.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = uiState.modelNoticeMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AmberWarning
                    )
                }
            }
        }

        // Media & Character Reference Pickers
        item {
            LiquidGlassCard {
                Text(
                    text = if (isId) "Referensi Gambar / Video & Karakter" else "Image / Video & Character Reference",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val type = if (uiState.selectedMode == VideoGenerationMode.VIDEO_TO_VIDEO ||
                                uiState.selectedMode == VideoGenerationMode.VIDEO_EXTEND
                            ) {
                                ActivityResultContracts.PickVisualMedia.ImageAndVideo
                            } else {
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            }
                            primaryPicker.launch(PickVisualMediaRequest(type))
                        },
                        modifier = Modifier.weight(1f).testTag("pick_primary_ref_btn")
                    ) {
                        Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (uiState.referenceImageUri != null) "Ref 1 ✓" else "Frame / Image 1", maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = {
                            secondaryPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.weight(1f).testTag("pick_secondary_ref_btn")
                    ) {
                        Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (uiState.secondaryImageUri != null) "End Frame ✓" else "End / Ref 2", maxLines = 1)
                    }
                }

                if (uiState.referenceImageUri != null) {
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AsyncImage(
                            model = uiState.referenceImageUri,
                            contentDescription = "Primary reference",
                            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        TextButton(onClick = { onSelectPrimaryMedia(null) }) {
                            Text(if (isId) "Hapus Ref 1" else "Clear Ref 1")
                        }
                    }
                }

                if (characters.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = if (isId) "Konsistensi Karakter:" else "Character Consistency:",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            GlassChip(
                                label = if (isId) "Tanpa Karakter" else "None",
                                selected = uiState.selectedCharacter == null,
                                onClick = { onSelectCharacter(null) }
                            )
                        }
                        items(characters, key = { it.id }) { char ->
                            GlassChip(
                                label = char.name,
                                icon = Icons.Rounded.Face,
                                selected = uiState.selectedCharacter?.id == char.id,
                                onClick = { onSelectCharacter(char) }
                            )
                        }
                    }
                }
            }
        }

        // Advanced Video Settings
        item {
            LiquidGlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.Tune, contentDescription = null, tint = NeonCyan)
                        Text(
                            text = if (isId) "Pengaturan Video Lanjutan" else "Advanced Video Settings",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    IconButton(onClick = { showAdvanced = !showAdvanced }) {
                        Icon(
                            imageVector = if (showAdvanced) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                            contentDescription = "Toggle Advanced Settings"
                        )
                    }
                }

                AnimatedVisibility(visible = showAdvanced) {
                    val cfg = uiState.config
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Spacer(Modifier.height(4.dp))
                        // Duration
                        Text(if (isId) "Durasi (detik)" else "Duration (sec)", style = MaterialTheme.typography.labelLarge)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(listOf(5, 10, 15, 30)) { sec ->
                                GlassChip(
                                    label = "${sec}s",
                                    selected = cfg.durationSec == sec,
                                    onClick = { onUpdateConfig { it.copy(durationSec = sec) } }
                                )
                            }
                        }

                        // Resolution
                        Text(if (isId) "Resolusi" else "Resolution", style = MaterialTheme.typography.labelLarge)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(listOf("480p", "720p", "1080p", "2K", "4K")) { res ->
                                GlassChip(
                                    label = res,
                                    selected = cfg.resolution == res,
                                    onClick = { onUpdateConfig { it.copy(resolution = res) } }
                                )
                            }
                        }

                        // Aspect Ratio & FPS
                        Text("Aspect Ratio & FPS", style = MaterialTheme.typography.labelLarge)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(listOf("16:9", "9:16", "1:1", "4:3", "21:9")) { ar ->
                                GlassChip(
                                    label = ar,
                                    selected = cfg.aspectRatio == ar,
                                    onClick = { onUpdateConfig { it.copy(aspectRatio = ar) } }
                                )
                            }
                            items(listOf(24, 30, 60)) { fps ->
                                GlassChip(
                                    label = "${fps}fps",
                                    selected = cfg.fps == fps,
                                    onClick = { onUpdateConfig { it.copy(fps = fps) } }
                                )
                            }
                        }

                        // Visual Style
                        Text(if (isId) "Gaya Visual" else "Visual Style", style = MaterialTheme.typography.labelLarge)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(VisualStyle.entries) { st ->
                                GlassChip(
                                    label = if (isId) st.labelId else st.labelEn,
                                    selected = cfg.style == st,
                                    onClick = { onUpdateConfig { it.copy(style = st) } }
                                )
                            }
                        }

                        // Camera Movement & Angle
                        Text(if (isId) "Gerakan & Sudut Kamera" else "Camera Movement & Angle", style = MaterialTheme.typography.labelLarge)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(CameraMovement.entries) { cam ->
                                GlassChip(
                                    label = if (isId) cam.labelId else cam.labelEn,
                                    selected = cfg.cameraMovement == cam,
                                    onClick = { onUpdateConfig { it.copy(cameraMovement = cam) } }
                                )
                            }
                        }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(CameraAngle.entries) { angle ->
                                GlassChip(
                                    label = if (isId) angle.labelId else angle.labelEn,
                                    selected = cfg.cameraAngle == angle,
                                    onClick = { onUpdateConfig { it.copy(cameraAngle = angle) } }
                                )
                            }
                        }

                        // Sliders
                        Text("Motion Strength: ${(cfg.motionStrength * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
                        Slider(
                            value = cfg.motionStrength,
                            onValueChange = { v -> onUpdateConfig { it.copy(motionStrength = v) } }
                        )
                        Text("Creativity Strength: ${(cfg.creativityStrength * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
                        Slider(
                            value = cfg.creativityStrength,
                            onValueChange = { v -> onUpdateConfig { it.copy(creativityStrength = v) } }
                        )

                        // Negative Prompt & Seed
                        OutlinedTextField(
                            value = cfg.negativePrompt,
                            onValueChange = { neg -> onUpdateConfig { it.copy(negativePrompt = neg) } },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = { Text("Negative Prompt") }
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Seed: ${cfg.seed}", style = MaterialTheme.typography.labelMedium, color = NeonCyan)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { onUpdateConfig { it.copy(seed = (100000L..999999L).random()) } }) {
                                    Icon(Icons.Rounded.Casino, contentDescription = "Random Seed", tint = NeonCyan)
                                }
                                Text("Prompt Enhancer", style = MaterialTheme.typography.bodyMedium)
                                Spacer(Modifier.width(6.dp))
                                Switch(
                                    checked = cfg.promptEnhancer,
                                    onCheckedChange = { on -> onUpdateConfig { it.copy(promptEnhancer = on) } }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Generate Action Button
        item {
            GradientPrimaryButton(
                text = AppStrings.generateVideo(language),
                icon = Icons.Rounded.MovieCreation,
                onClick = onSubmitRender,
                modifier = Modifier.fillMaxWidth(),
                testTag = "submit_video_render_btn"
            )
        }

        // Active Job Progress
        if (activeJob != null && (activeJob.status == JobStatus.QUEUED || activeJob.status == JobStatus.RENDERING)) {
            item {
                LiquidGlassCard(accentGlow = NeonCyan) {
                    Text(
                        text = "${activeJob.status.name} • ${(activeJob.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        color = NeonCyan
                    )
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { activeJob.progress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = NeonCyan
                    )
                }
            }
        }

        // Inline MP4 Video Player
        val videoPath = uiState.latestCompletedVideoPath
        if (!videoPath.isNullOrBlank()) {
            item {
                InlineMp4PlayerCard(
                    videoPath = videoPath,
                    title = uiState.prompt.ifBlank { "Videon Generated MP4" },
                    onExportToGallery = { onExportVideo(videoPath, "Videon_${System.currentTimeMillis()}") },
                    onShare = { onShareVideo(videoPath, "Videon AI Video") },
                    onOpenInEditor = { onOpenInEditor(videoPath, uiState.prompt.ifBlank { "Generated Clip" }) }
                )
            }
        }
    }
}
