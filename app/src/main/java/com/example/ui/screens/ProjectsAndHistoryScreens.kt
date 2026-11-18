package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.FolderSpecial
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.local.DownloadRecordEntity
import com.example.data.local.GenerationJobEntity
import com.example.data.local.ProjectEntity
import com.example.domain.model.AppLanguage
import com.example.domain.model.JobStatus
import com.example.domain.model.MediaType
import com.example.ui.components.InlineMp4PlayerCard
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldActive
import com.example.ui.theme.GlassChip
import com.example.ui.theme.GlassStatusBadge
import com.example.ui.theme.LiquidGlassCard
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RoseError
import com.example.ui.theme.SectionHeader
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MyProjectsScreen(
    language: AppLanguage,
    projects: List<ProjectEntity>,
    onOpenProject: (ProjectEntity) -> Unit,
    onOpenInEditor: (String?, String) -> Unit,
    onToggleFavorite: (ProjectEntity) -> Unit,
    onDeleteProject: (Long) -> Unit,
    onShareFile: (String, MediaType, String) -> Unit
) {
    val isId = language == AppLanguage.ID
    val fmt = remember { SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = if (isId) "Proyek Saya (${projects.size})" else "My Projects (${projects.size})",
                subtitle = if (isId) "Disimpan otomatis setiap kali Anda membuat video" else "Autosaved locally every time you render",
                icon = Icons.Rounded.FolderSpecial
            )
        }

        if (projects.isEmpty()) {
            item {
                LiquidGlassCard {
                    Text(
                        text = if (isId) "Belum ada proyek tersimpan. Mulai buat video di menu AI Video!" else "No saved projects yet. Start creating in AI Video!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(projects, key = { it.id }) { proj ->
                LiquidGlassCard(accentGlow = if (proj.isFavorite) RoseError else null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(proj.title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "${proj.mode} • ${proj.resolution} ${proj.aspectRatio} • ${fmt.format(Date(proj.updatedAt))}",
                                style = MaterialTheme.typography.labelMedium,
                                color = NeonCyan
                            )
                        }
                        Row {
                            IconButton(onClick = { onToggleFavorite(proj) }) {
                                Icon(
                                    imageVector = if (proj.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (proj.isFavorite) RoseError else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onDeleteProject(proj.id) }) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = RoseError)
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(proj.prompt, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(onClick = { onOpenProject(proj) }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(if (isId) "Buka" else "Open")
                        }
                        if (!proj.outputPath.isNullOrBlank() && File(proj.outputPath).exists()) {
                            OutlinedButton(
                                onClick = { onOpenInEditor(proj.outputPath, proj.title) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Edit")
                            }
                            OutlinedButton(
                                onClick = { onShareFile(proj.outputPath, MediaType.VIDEO, proj.title) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Share")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryScreen(
    language: AppLanguage,
    jobs: List<GenerationJobEntity>,
    downloads: List<DownloadRecordEntity>,
    onToggleFavorite: (GenerationJobEntity) -> Unit,
    onCancelJob: (Long) -> Unit,
    onDeleteJob: (Long) -> Unit,
    onExportMedia: (String, String, MediaType) -> Unit,
    onShareMedia: (String, MediaType, String) -> Unit,
    onOpenInEditor: (String, String) -> Unit
) {
    val isId = language == AppLanguage.ID
    var tabIndex by remember { mutableIntStateOf(0) } // 0 = All, 1 = Favorites, 2 = Downloads
    var previewVideoJob by remember { mutableIntStateOf(-1) }

    val displayedJobs = when (tabIndex) {
        1 -> jobs.filter { it.isFavorite }
        else -> jobs
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = if (isId) "Riwayat & Antrean Render" else "Generation History & Queue",
                subtitle = if (isId) "Pantau progres, favorit, dan riwayat unduhan" else "Monitor jobs, favorites, and export downloads",
                icon = Icons.Rounded.History
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassChip(label = if (isId) "Semua (${jobs.size})" else "All (${jobs.size})", selected = tabIndex == 0, onClick = { tabIndex = 0 })
                GlassChip(label = if (isId) "Favorit" else "Favorites", selected = tabIndex == 1, onClick = { tabIndex = 1 })
                GlassChip(label = if (isId) "Unduhan (${downloads.size})" else "Downloads (${downloads.size})", selected = tabIndex == 2, onClick = { tabIndex = 2 })
            }
        }

        if (tabIndex == 2) {
            if (downloads.isEmpty()) {
                item {
                    LiquidGlassCard {
                        Text(if (isId) "Belum ada file yang diekspor ke galeri." else "No files exported to device gallery yet.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                items(downloads, key = { it.id }) { dl ->
                    LiquidGlassCard {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Rounded.Download, contentDescription = null, tint = EmeraldActive)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(dl.assetTitle, style = MaterialTheme.typography.titleMedium)
                                Text("${dl.mediaType.name} • ${dl.exportDestination} • ${dl.fileSizeBytes / 1024} KB", style = MaterialTheme.typography.labelMedium, color = NeonCyan)
                            }
                        }
                    }
                }
            }
        } else {
            if (displayedJobs.isEmpty()) {
                item {
                    LiquidGlassCard {
                        Text(if (isId) "Riwayat generasi masih kosong." else "No generation records yet.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                items(displayedJobs, key = { it.id }) { job ->
                    val statusColor = when (job.status) {
                        JobStatus.COMPLETED -> EmeraldActive
                        JobStatus.RENDERING, JobStatus.QUEUED -> NeonCyan
                        JobStatus.FAILED -> RoseError
                        JobStatus.CANCELLED -> AmberWarning
                    }
                    LiquidGlassCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(job.title, style = MaterialTheme.typography.titleMedium)
                                Text("${job.modelName} • ${job.mediaType.name}", style = MaterialTheme.typography.labelMedium, color = NeonCyan)
                            }
                            GlassStatusBadge(text = job.status.name, color = statusColor)
                        }

                        if (job.status == JobStatus.RENDERING || job.status == JobStatus.QUEUED) {
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(progress = { job.progress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                OutlinedButton(onClick = { onCancelJob(job.id) }) {
                                    Icon(Icons.Rounded.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Cancel")
                                }
                            }
                        }

                        if (!job.errorMessage.isNullOrBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text(job.errorMessage, style = MaterialTheme.typography.bodyMedium, color = RoseError)
                        }

                        val path = job.outputFilePath
                        if (job.status == JobStatus.COMPLETED && !path.isNullOrBlank() && File(path).exists()) {
                            Spacer(Modifier.height(8.dp))
                            if (job.mediaType == MediaType.VIDEO && previewVideoJob == job.id.toInt()) {
                                InlineMp4PlayerCard(
                                    videoPath = path,
                                    title = job.title,
                                    onExportToGallery = { onExportMedia(path, job.title, job.mediaType) },
                                    onShare = { onShareMedia(path, job.mediaType, job.title) },
                                    onOpenInEditor = { onOpenInEditor(path, job.title) }
                                )
                            } else {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (job.mediaType == MediaType.VIDEO) {
                                        FilledTonalButton(onClick = { previewVideoJob = job.id.toInt() }) {
                                            Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Text("Play MP4")
                                        }
                                    }
                                    OutlinedButton(onClick = { onExportMedia(path, job.title, job.mediaType) }) {
                                        Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Text("Save")
                                    }
                                    OutlinedButton(onClick = { onShareMedia(path, job.mediaType, job.title) }) {
                                        Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Text("Share")
                                    }
                                }
                            }
                        }

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            IconButton(onClick = { onToggleFavorite(job) }) {
                                Icon(
                                    imageVector = if (job.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (job.isFavorite) RoseError else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onDeleteJob(job.id) }) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = RoseError)
                            }
                        }
                    }
                }
            }
        }
    }
}
