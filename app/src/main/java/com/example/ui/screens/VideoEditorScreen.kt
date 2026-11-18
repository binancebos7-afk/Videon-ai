package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.GenerationJobEntity
import com.example.domain.model.AppLanguage
import com.example.domain.model.EditorTimelineState
import com.example.domain.model.JobStatus
import com.example.domain.model.MediaType
import com.example.domain.model.TransitionPreset
import com.example.domain.model.VideoFilterPreset
import com.example.ui.components.InlineMp4PlayerCard
import com.example.ui.theme.DeepViolet
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GlassChip
import com.example.ui.theme.GradientPrimaryButton
import com.example.ui.theme.LiquidGlassCard
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SectionHeader

@Composable
fun VideoEditorScreen(
    language: AppLanguage,
    state: EditorTimelineState,
    completedVideos: List<GenerationJobEntity>,
    onUpdateState: ((EditorTimelineState) -> EditorTimelineState) -> Unit,
    onExportMp4: () -> Unit,
    onSaveToGallery: (String, String) -> Unit,
    onShareVideo: (String, String) -> Unit
) {
    val isId = language == AppLanguage.ID
    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onUpdateState {
                it.copy(
                    sourceVideoUriString = uri.toString(),
                    sourceVideoPath = null,
                    sourceTitle = "Imported Gallery Video"
                )
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
                title = if (isId) "Editor Video Timeline" else "Timeline Video Editor",
                subtitle = if (isId) "Trim, Cut/Merge, Crop, Teks, Subtitle, Filter, Speed & Ekspor MP4" else "Trim, Cut/Merge, Crop, Text, Subtitles, Filters, Speed & MP4 Export",
                icon = Icons.Rounded.Edit
            )
        }

        // Source Selector
        item {
            LiquidGlassCard {
                Text(
                    text = if (isId) "Sumber Klip Timeline: ${state.sourceTitle}" else "Timeline Clip Source: ${state.sourceTitle}",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { videoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) },
                    modifier = Modifier.fillMaxWidth().testTag("editor_pick_video_btn")
                ) {
                    Icon(Icons.Rounded.VideoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (isId) "Impor Video dari Galeri" else "Import Video from Gallery")
                }

                val studioVideos = completedVideos.filter { it.mediaType == MediaType.VIDEO && it.status == JobStatus.COMPLETED && !it.outputFilePath.isNullOrBlank() }
                if (studioVideos.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Text(if (isId) "Atau Pilih Hasil Studio:" else "Or Pick Studio Render:", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(studioVideos, key = { it.id }) { job ->
                            GlassChip(
                                label = job.title.take(20),
                                icon = Icons.Rounded.Movie,
                                selected = state.sourceVideoPath == job.outputFilePath,
                                onClick = {
                                    onUpdateState {
                                        it.copy(
                                            sourceVideoPath = job.outputFilePath,
                                            sourceVideoUriString = null,
                                            sourceTitle = job.title,
                                            totalDurationMs = (job.durationSec * 1000L).coerceAtLeast(3000L),
                                            trimStartMs = 0L,
                                            trimEndMs = (job.durationSec * 1000L).coerceAtLeast(3000L)
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Visual Timeline Trim & Cut/Split Track
        item {
            LiquidGlassCard(accentGlow = NeonCyan) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.ContentCut, contentDescription = null, tint = NeonCyan)
                    Text(if (isId) "Timeline Trim & Cut" else "Timeline Trim & Cut", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(10.dp))
                // Visual track bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.horizontalGradient(listOf(ElectricBlue.copy(0.35f), DeepViolet.copy(0.45f), NeonCyan.copy(0.35f))))
                        .border(1.dp, NeonCyan.copy(0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "${state.trimStartMs / 1000f}s  ───  ${state.sourceTitle.take(22)}  ───  ${state.trimEndMs / 1000f}s",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Spacer(Modifier.height(8.dp))
                val maxDur = state.totalDurationMs.toFloat().coerceAtLeast(3000f)
                RangeSlider(
                    value = state.trimStartMs.toFloat().coerceIn(0f, maxDur - 500f)..state.trimEndMs.toFloat().coerceIn(500f, maxDur),
                    onValueChange = { range ->
                        val start = range.start.toLong()
                        val end = range.endInclusive.toLong().coerceAtLeast(start + 1000L)
                        onUpdateState { it.copy(trimStartMs = start, trimEndMs = end) }
                    },
                    valueRange = 0f..maxDur
                )
                Text(
                    text = "Trim: ${String.format("%.1f", state.trimStartMs / 1000f)}s → ${String.format("%.1f", state.trimEndMs / 1000f)}s",
                    style = MaterialTheme.typography.labelMedium,
                    color = NeonCyan
                )
            }
        }

        // Crop, Resize & Speed
        item {
            LiquidGlassCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.Speed, contentDescription = null, tint = NeonCyan)
                    Text("Crop, Resize & Speed", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(8.dp))
                Text("Crop Aspect Ratio", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("16:9", "9:16", "1:1", "4:3", "21:9")) { ar ->
                        GlassChip(
                            label = ar,
                            selected = state.cropAspectRatio == ar,
                            onClick = { onUpdateState { it.copy(cropAspectRatio = ar) } }
                        )
                    }
                    items(listOf("480p", "720p", "1080p")) { res ->
                        GlassChip(
                            label = res,
                            selected = state.resizeResolution == res,
                            onClick = { onUpdateState { it.copy(resizeResolution = res) } }
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text("Speed: ${state.playbackSpeed}x", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)) { spd ->
                        GlassChip(
                            label = "${spd}x",
                            selected = state.playbackSpeed == spd,
                            onClick = { onUpdateState { it.copy(playbackSpeed = spd) } }
                        )
                    }
                }
            }
        }

        // Text, Subtitles, Filters & Transitions
        item {
            LiquidGlassCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.Subtitles, contentDescription = null, tint = NeonCyan)
                    Text(if (isId) "Teks, Subtitle, Filter & Transisi" else "Text, Subtitles, Filters & Transitions", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = state.textOverlay,
                    onValueChange = { txt -> onUpdateState { it.copy(textOverlay = txt) } },
                    modifier = Modifier.fillMaxWidth().testTag("editor_text_overlay"),
                    singleLine = true,
                    label = { Text(if (isId) "Overlay Judul Atas" else "Top Title Overlay") }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.subtitleText,
                    onValueChange = { sub -> onUpdateState { it.copy(subtitleText = sub) } },
                    modifier = Modifier.fillMaxWidth().testTag("editor_subtitle_input"),
                    singleLine = true,
                    label = { Text("Subtitle (Burn-In)") }
                )
                Spacer(Modifier.height(10.dp))
                Text("Video Filter", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(VideoFilterPreset.entries) { f ->
                        GlassChip(
                            label = if (isId) f.labelId else f.labelEn,
                            selected = state.filter == f,
                            onClick = { onUpdateState { it.copy(filter = f) } }
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text("Transition", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(TransitionPreset.entries) { tr ->
                        GlassChip(
                            label = if (isId) tr.labelId else tr.labelEn,
                            selected = state.transition == tr,
                            onClick = { onUpdateState { it.copy(transition = tr) } }
                        )
                    }
                }
            }
        }

        // Audio Mix Controls
        item {
            LiquidGlassCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.GraphicEq, contentDescription = null, tint = NeonCyan)
                    Text(if (isId) "Mixing Audio" else "Audio Mixing", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = state.audioTrackName.ifBlank {
                        if (isId) "Belum ada track audio dipilih (buat di AI Audio Studio)" else "No audio track attached (create one in AI Audio Studio)"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = state.audioMixVolume,
                    onValueChange = { vol -> onUpdateState { it.copy(audioMixVolume = vol) } }
                )
                Text("Mix Gain: ${(state.audioMixVolume * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
            }
        }

        // Export MP4 Button
        item {
            GradientPrimaryButton(
                text = if (state.isExporting) "Exporting MP4 ${(state.exportProgress * 100).toInt()}%..." else "Export MP4",
                icon = Icons.Rounded.Movie,
                enabled = !state.isExporting,
                onClick = onExportMp4,
                modifier = Modifier.fillMaxWidth(),
                testTag = "editor_export_mp4_btn"
            )
        }

        if (state.isExporting) {
            item {
                LinearProgressIndicator(
                    progress = { state.exportProgress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = NeonCyan
                )
            }
        }

        val exported = state.lastExportedPath
        if (!exported.isNullOrBlank()) {
            item {
                InlineMp4PlayerCard(
                    videoPath = exported,
                    title = "Edited: ${state.sourceTitle}",
                    onExportToGallery = { onSaveToGallery(exported, "Videon_Edited") },
                    onShare = { onShareVideo(exported, "Videon Edited Video") }
                )
            }
        }
    }
}
