package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.MovieCreation
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.domain.model.AppLanguage
import com.example.domain.model.AudioGenerationMode
import com.example.domain.model.ImageGenerationMode
import com.example.domain.model.VisualStyle
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldActive
import com.example.ui.theme.GlassChip
import com.example.ui.theme.GradientPrimaryButton
import com.example.ui.theme.LiquidGlassCard
import com.example.ui.theme.LocalAiNoticeBanner
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SectionHeader
import com.example.ui.viewmodel.AudioStudioUiState
import com.example.ui.viewmodel.ImageStudioUiState
import java.io.File

@Composable
fun AiImageScreen(
    language: AppLanguage,
    uiState: ImageStudioUiState,
    onSelectMode: (ImageGenerationMode) -> Unit,
    onPromptChange: (String) -> Unit,
    onSelectStyle: (VisualStyle) -> Unit,
    onSelectAspect: (String) -> Unit,
    onSelectReference: (String?) -> Unit,
    onGenerate: () -> Unit,
    onSaveImage: (String, String) -> Unit,
    onShareImage: (String, String) -> Unit,
    onSendToVideo: (String) -> Unit
) {
    val isId = language == AppLanguage.ID
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onSelectReference(uri.toString())
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionHeader(
                title = if (isId) "AI Image Studio" else "AI Image Generator",
                subtitle = if (isId) "9 mode kreatif + Studio Color Grading lokal" else "9 creative modes + local Studio Color Grading",
                icon = Icons.Rounded.Image
            )
        }

        item { LocalAiNoticeBanner(isIndonesian = isId) }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ImageGenerationMode.entries) { mode ->
                    GlassChip(
                        label = if (isId) mode.labelId else mode.labelEn,
                        selected = uiState.selectedMode == mode,
                        onClick = { onSelectMode(mode) }
                    )
                }
            }
        }

        item {
            LiquidGlassCard {
                OutlinedTextField(
                    value = uiState.prompt,
                    onValueChange = onPromptChange,
                    modifier = Modifier.fillMaxWidth().testTag("image_prompt_input"),
                    minLines = 3,
                    label = { Text(if (isId) "Prompt Gambar / Instruksi Edit" else "Image Prompt / Edit Instruction") }
                )
                Spacer(Modifier.height(10.dp))
                Text(if (isId) "Gaya & Rasio Aspek" else "Style & Aspect Ratio", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(VisualStyle.entries) { st ->
                        GlassChip(
                            label = if (isId) st.labelId else st.labelEn,
                            selected = uiState.style == st,
                            onClick = { onSelectStyle(st) }
                        )
                    }
                    items(listOf("1:1", "16:9", "9:16", "4:3")) { ar ->
                        GlassChip(
                            label = ar,
                            selected = uiState.aspectRatio == ar,
                            onClick = { onSelectAspect(ar) }
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        modifier = Modifier.weight(1f).testTag("image_pick_ref_btn")
                    ) {
                        Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (uiState.referenceImageUri != null) "Photo Loaded ✓" else if (isId) "Pilih Foto Referensi" else "Pick Reference Photo")
                    }
                    if (uiState.referenceImageUri != null) {
                        OutlinedButton(onClick = { onSelectReference(null) }) {
                            Text("Clear")
                        }
                    }
                }
            }
        }

        item {
            GradientPrimaryButton(
                text = if (uiState.isGenerating) "Processing..." else if (isId) "Proses Studio Gambar" else "Process Studio Image",
                icon = Icons.Rounded.Image,
                enabled = !uiState.isGenerating,
                onClick = onGenerate,
                modifier = Modifier.fillMaxWidth(),
                testTag = "generate_image_btn"
            )
        }

        uiState.latestResult?.let { res ->
            item {
                LiquidGlassCard(accentGlow = if (res.filePath != null) EmeraldActive else AmberWarning) {
                    Text(res.summary, style = MaterialTheme.typography.bodyMedium)
                    if (!res.filePath.isNullOrBlank() && File(res.filePath).exists()) {
                        Spacer(Modifier.height(12.dp))
                        AsyncImage(
                            model = File(res.filePath),
                            contentDescription = "Processed Studio Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = { onSaveImage(res.filePath, "Videon_Image") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Save")
                            }
                            OutlinedButton(
                                onClick = { onShareImage(res.filePath, "Videon Image") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Share")
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = { onSendToVideo(res.filePath) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Rounded.MovieCreation, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (isId) "Jadikan Video (Image-to-Video)" else "Animate in Video Studio")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AiAudioScreen(
    language: AppLanguage,
    uiState: AudioStudioUiState,
    onSelectMode: (AudioGenerationMode) -> Unit,
    onTextChange: (String) -> Unit,
    onLangChange: (String) -> Unit,
    onPitchChange: (Float) -> Unit,
    onRateChange: (Float) -> Unit,
    onPresetChange: (String) -> Unit,
    onDurationChange: (Int) -> Unit,
    onSynthesize: () -> Unit,
    onTogglePlay: (String) -> Unit,
    onSaveAudio: (String, String) -> Unit,
    onShareAudio: (String, String) -> Unit,
    onSendToEditor: (String, String) -> Unit
) {
    val isId = language == AppLanguage.ID
    val isSfxOrBgm = uiState.selectedMode == AudioGenerationMode.SOUND_EFFECTS ||
        uiState.selectedMode == AudioGenerationMode.BACKGROUND_MUSIC

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionHeader(
                title = if (isId) "AI Audio Studio" else "AI Audio Studio",
                subtitle = if (isId) "Sintesis suara TTS WAV asli & efek/musik PCM native" else "Real native TTS WAV voice & PCM SFX/music synthesis",
                icon = Icons.Rounded.GraphicEq
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(AudioGenerationMode.entries) { mode ->
                    GlassChip(
                        label = if (isId) mode.labelId else mode.labelEn,
                        selected = uiState.selectedMode == mode,
                        onClick = { onSelectMode(mode) }
                    )
                }
            }
        }

        item {
            LiquidGlassCard {
                if (isSfxOrBgm) {
                    Text(if (isId) "Pilih Preset Sintesis WAV" else "Select WAV Synth Preset", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    val presets = listOf(
                        "cyberpunk_synth" to "Cyberpunk Synth",
                        "cinematic_riser" to "Cinematic Riser",
                        "rain_ambience" to "Rain Ambience",
                        "scifi_whoosh" to "Sci-Fi Whoosh",
                        "lofi_pad" to "Lo-Fi Pad"
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(presets) { (id, label) ->
                            GlassChip(
                                label = label,
                                selected = uiState.selectedPreset == id,
                                onClick = { onPresetChange(id) }
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("Duration: ${uiState.durationSec}s", style = MaterialTheme.typography.labelMedium)
                    Slider(
                        value = uiState.durationSec.toFloat(),
                        onValueChange = { onDurationChange(it.toInt()) },
                        valueRange = 2f..15f
                    )
                } else {
                    OutlinedTextField(
                        value = uiState.textInput,
                        onValueChange = onTextChange,
                        modifier = Modifier.fillMaxWidth().testTag("audio_tts_input"),
                        minLines = 3,
                        label = {
                            Text(
                                when (uiState.selectedMode) {
                                    AudioGenerationMode.DIALOGUE_GEN -> if (isId) "Konteks Adegan Dialog" else "Dialogue Scene Context"
                                    else -> if (isId) "Teks Naskah Suara / Sulih Suara" else "Voice / Narration Script"
                                }
                            )
                        }
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(if (isId) "Bahasa Suara" else "Voice Language", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf("id" to "Indonesia", "en" to "English", "ja" to "Japanese", "es" to "Spanish")) { (tag, label) ->
                            GlassChip(
                                label = label,
                                selected = uiState.voiceLangTag == tag,
                                onClick = { onLangChange(tag) }
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("Pitch: ${String.format("%.2f", uiState.pitch)}x", style = MaterialTheme.typography.labelMedium)
                    Slider(value = uiState.pitch, onValueChange = onPitchChange, valueRange = 0.6f..1.5f)
                    Text("Speed: ${String.format("%.2f", uiState.speechRate)}x", style = MaterialTheme.typography.labelMedium)
                    Slider(value = uiState.speechRate, onValueChange = onRateChange, valueRange = 0.6f..1.6f)
                }
            }
        }

        item {
            GradientPrimaryButton(
                text = if (uiState.isSynthesizing) "Synthesizing WAV..." else if (isId) "Hasilkan Audio WAV" else "Generate Audio WAV",
                icon = Icons.Rounded.GraphicEq,
                enabled = !uiState.isSynthesizing,
                onClick = onSynthesize,
                modifier = Modifier.fillMaxWidth(),
                testTag = "synthesize_audio_btn"
            )
        }

        if (uiState.isSynthesizing) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator(color = NeonCyan)
                }
            }
        }

        if (!uiState.statusMessage.isNullOrBlank()) {
            item {
                LiquidGlassCard(accentGlow = EmeraldActive) {
                    Text(uiState.statusMessage, style = MaterialTheme.typography.bodyMedium, color = NeonCyan)
                }
            }
        }

        val audioPath = uiState.latestAudioPath
        if (!audioPath.isNullOrBlank() && File(audioPath).exists()) {
            item {
                LiquidGlassCard(accentGlow = NeonCyan) {
                    Text(File(audioPath).name, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { onTogglePlay(audioPath) },
                            modifier = Modifier.weight(1f).testTag("play_audio_btn")
                        ) {
                            Icon(
                                imageVector = if (uiState.isPlaying) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(if (uiState.isPlaying) "Stop" else "Play")
                        }
                        OutlinedButton(
                            onClick = { onSaveAudio(audioPath, "Videon_Audio") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Save")
                        }
                        OutlinedButton(
                            onClick = { onShareAudio(audioPath, "Videon Audio") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Share")
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = { onSendToEditor(audioPath, File(audioPath).name) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (isId) "Campurkan ke Video Editor" else "Mix in Video Editor")
                    }
                }
            }
        }

        if (uiState.generatedDialogue.isNotEmpty()) {
            items(uiState.generatedDialogue) { line ->
                LiquidGlassCard {
                    Text("${line.speaker} (${line.emotion})", style = MaterialTheme.typography.titleMedium, color = NeonCyan)
                    Spacer(Modifier.height(4.dp))
                    Text(line.line, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
