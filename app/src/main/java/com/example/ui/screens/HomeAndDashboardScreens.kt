package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Collections
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.MovieCreation
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.engine.ModelCatalog
import com.example.data.local.DownloadRecordEntity
import com.example.data.local.GenerationJobEntity
import com.example.data.local.ProjectEntity
import com.example.domain.model.AppLanguage
import com.example.domain.model.JobStatus
import com.example.domain.model.ModelAvailabilityStatus
import com.example.domain.model.StudioRoute
import com.example.ui.i18n.AppStrings
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.DeepViolet
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldActive
import com.example.ui.theme.GlassStatusBadge
import com.example.ui.theme.GradientPrimaryButton
import com.example.ui.theme.LiquidGlassCard
import com.example.ui.theme.LocalAiNoticeBanner
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RoseError
import com.example.ui.theme.SectionHeader
import com.example.ui.theme.SlateMuted

@Composable
fun HomeScreen(
    language: AppLanguage,
    jobs: List<GenerationJobEntity>,
    onNavigate: (StudioRoute) -> Unit
) {
    val isId = language == AppLanguage.ID
    val activeQueueCount = jobs.count { it.status == JobStatus.RENDERING || it.status == JobStatus.QUEUED }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Liquid Glass VisionOS Showcase
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .border(
                        1.dp,
                        Brush.linearGradient(listOf(NeonCyan.copy(alpha = 0.6f), DeepViolet.copy(alpha = 0.5f))),
                        RoundedCornerShape(28.dp)
                    )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_studio),
                    contentDescription = "Videon AI Studio Hero",
                    modifier = Modifier
                        .matchParentSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xAA050814),
                                    Color(0xCC050814),
                                    Color(0xEE050814)
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlassStatusBadge(
                            text = if (isId) "Gratis & Open-Source" else "Free & Open Engine",
                            color = EmeraldActive
                        )
                        if (activeQueueCount > 0) {
                            GlassStatusBadge(
                                text = "$activeQueueCount Rendering",
                                color = NeonCyan
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "VIDEON AI STUDIO",
                        style = MaterialTheme.typography.displayLarge,
                        color = Color.White
                    )
                    Text(
                        text = AppStrings.tagline(language),
                        style = MaterialTheme.typography.titleMedium,
                        color = NeonCyan
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = AppStrings.heroDescription(language),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFCBD5E1)
                    )

                    Spacer(Modifier.height(18.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GradientPrimaryButton(
                            text = AppStrings.startCreatingVideo(language),
                            icon = Icons.Rounded.MovieCreation,
                            onClick = { onNavigate(StudioRoute.AI_VIDEO) },
                            modifier = Modifier.weight(1.2f),
                            testTag = "home_create_video_btn"
                        )
                        OutlinedButton(
                            onClick = { onNavigate(StudioRoute.VIDEO_EDITOR) },
                            modifier = Modifier
                                .weight(0.9f)
                                .height(52.dp)
                                .testTag("home_open_editor_btn"),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(AppStrings.openEditor(language), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }

        item {
            LocalAiNoticeBanner(isIndonesian = isId)
        }

        // Quick Studio Suites Grid
        item {
            SectionHeader(
                title = if (isId) "Suite Studio Lengkap" else "All-In-One Studio Suites",
                subtitle = if (isId) "Semua alat produksi dalam satu ruang kaca futuristik" else "Every creative engine in one liquid glass workspace",
                icon = Icons.Rounded.AutoAwesome
            )
            Spacer(Modifier.height(10.dp))
            val suites = listOf(
                Triple(StudioRoute.AI_VIDEO, Icons.Rounded.MovieCreation, if (isId) "14 Mode Video AI" else "14 AI Video Modes"),
                Triple(StudioRoute.AI_IMAGE, Icons.Rounded.Image, if (isId) "9 Mode Gambar AI" else "9 AI Image Modes"),
                Triple(StudioRoute.AI_AUDIO, Icons.Rounded.GraphicEq, if (isId) "TTS, SFX & Musik" else "TTS, SFX & Music"),
                Triple(StudioRoute.VIDEO_EDITOR, Icons.Rounded.Edit, if (isId) "Timeline & Ekspor MP4" else "Timeline & MP4 Export"),
                Triple(StudioRoute.CHARACTER_STUDIO, Icons.Rounded.Face, if (isId) "Konsistensi Karakter" else "Character Consistency"),
                Triple(StudioRoute.PROMPT_LIBRARY, Icons.Rounded.Psychology, if (isId) "Asisten & Storyboard" else "Assistant & Storyboard")
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                suites.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { (route, icon, desc) ->
                            QuickSuiteCard(
                                title = route.label(language),
                                subtitle = desc,
                                icon = icon,
                                onClick = { onNavigate(route) },
                                modifier = Modifier.weight(1f),
                                testTag = "home_suite_${route.route}"
                            )
                        }
                    }
                }
            }
        }

        // Transparent Model Adapter Showcase
        item {
            SectionHeader(
                title = if (isId) "Arsitektur Adapter Model" else "Model Adapter Status",
                subtitle = if (isId) "Status jujur: lokal aktif gratis, cloud tanpa akses ditandai belum tersedia" else "Honest status: local free active, unconfigured cloud models marked unavailable",
                icon = Icons.Rounded.VideoLibrary
            )
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(ModelCatalog.allModels) { model ->
                    val statusColor = when (model.availability) {
                        ModelAvailabilityStatus.ACTIVE_LOCAL -> EmeraldActive
                        ModelAvailabilityStatus.SELF_HOSTED_ADAPTER -> ElectricBlue
                        ModelAvailabilityStatus.UNAVAILABLE_NO_PROVIDER -> SlateMuted
                    }
                    LiquidGlassCard(
                        modifier = Modifier.width(240.dp),
                        accentGlow = if (model.availability == ModelAvailabilityStatus.ACTIVE_LOCAL) NeonCyan else null,
                        onClick = { onNavigate(StudioRoute.AI_VIDEO) }
                    ) {
                        GlassStatusBadge(
                            text = if (isId) model.integrationStatusId else model.integrationStatusEn,
                            color = statusColor
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(model.name, style = MaterialTheme.typography.titleMedium)
                        Text(model.provider, style = MaterialTheme.typography.labelMedium, color = NeonCyan)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (isId) model.descriptionId else model.descriptionEn,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Transparent Free System Notice
        item {
            LiquidGlassCard(accentGlow = EmeraldActive) {
                Text(
                    text = AppStrings.freeEngineTitle(language),
                    style = MaterialTheme.typography.titleMedium,
                    color = EmeraldActive
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = AppStrings.freeEngineDesc(language),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun QuickSuiteCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    LiquidGlassCard(
        modifier = modifier.testTag(testTag),
        onClick = onClick,
        contentPadding = PaddingValues(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.linearGradient(listOf(ElectricBlue.copy(0.28f), DeepViolet.copy(0.28f)))),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun DashboardScreen(
    language: AppLanguage,
    totalVideos: Int,
    totalImages: Int,
    totalAudio: Int,
    storageBytes: Long,
    jobs: List<GenerationJobEntity>,
    projects: List<ProjectEntity>,
    downloads: List<DownloadRecordEntity>,
    onCancelJob: (Long) -> Unit,
    onToggleFavorite: (GenerationJobEntity) -> Unit,
    onOpenProject: (ProjectEntity) -> Unit,
    onNavigate: (StudioRoute) -> Unit
) {
    val isId = language == AppLanguage.ID
    val activeJobs = jobs.filter { it.status == JobStatus.RENDERING || it.status == JobStatus.QUEUED }
    val favoriteJobs = jobs.filter { it.isFavorite }
    val storageMb = String.format("%.2f MB", storageBytes / (1024f * 1024f))

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionHeader(
                title = if (isId) "Dasbor Produksi" else "Production Dashboard",
                subtitle = AppStrings.gpuQueueInfo(language, jobs.count { it.status == JobStatus.RENDERING }, jobs.count { it.status == JobStatus.QUEUED }),
                icon = Icons.Rounded.Collections
            )
        }

        // Stats Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatGlassTile(
                        label = if (isId) "Total Video" else "Total Videos",
                        value = "$totalVideos",
                        icon = Icons.Rounded.PlayCircle,
                        accent = NeonCyan,
                        modifier = Modifier.weight(1f)
                    )
                    StatGlassTile(
                        label = if (isId) "Total Gambar" else "Total Images",
                        value = "$totalImages",
                        icon = Icons.Rounded.Image,
                        accent = DeepViolet,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatGlassTile(
                        label = if (isId) "Audio & Suara" else "Audio & Voice",
                        value = "$totalAudio",
                        icon = Icons.Rounded.GraphicEq,
                        accent = ElectricBlue,
                        modifier = Modifier.weight(1f)
                    )
                    StatGlassTile(
                        label = if (isId) "Penyimpanan" else "Storage Used",
                        value = storageMb,
                        icon = Icons.Rounded.Storage,
                        accent = EmeraldActive,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Active Render Status
        item {
            SectionHeader(
                title = if (isId) "Status Rendering & Antrean" else "Rendering Queue Status",
                subtitle = if (activeJobs.isEmpty()) {
                    if (isId) "Semua slot encoder perangkat siap" else "Device encoder slot is idle and ready"
                } else {
                    "${activeJobs.size} active/queued"
                },
                icon = Icons.Rounded.MovieCreation
            )
        }

        if (activeJobs.isEmpty()) {
            item {
                LiquidGlassCard {
                    Text(
                        text = if (isId) "Tidak ada pekerjaan render yang sedang berjalan." else "No active render jobs in the queue.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(activeJobs, key = { it.id }) { job ->
                LiquidGlassCard(accentGlow = NeonCyan) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(job.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${job.modelName} • ${(job.progress * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, color = NeonCyan)
                        }
                        IconButton(onClick = { onCancelJob(job.id) }) {
                            Icon(Icons.Rounded.Cancel, contentDescription = "Cancel", tint = RoseError)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { job.progress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(4.dp)),
                        color = NeonCyan
                    )
                }
            }
        }

        // Recent Projects
        item {
            SectionHeader(
                title = if (isId) "Proyek Terbaru" else "Recent Projects",
                icon = Icons.Rounded.VideoLibrary,
                action = {
                    OutlinedButton(onClick = { onNavigate(StudioRoute.MY_PROJECTS) }) {
                        Text(if (isId) "Lihat Semua" else "View All")
                    }
                }
            )
        }

        if (projects.isEmpty()) {
            item {
                LiquidGlassCard {
                    Text(
                        text = if (isId) "Proyek yang Anda buat akan otomatis tersimpan di sini." else "Projects you generate will autosave here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(projects.take(4), key = { it.id }) { project ->
                LiquidGlassCard(onClick = { onOpenProject(project) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(project.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${project.mode} • ${project.resolution} • ${project.aspectRatio}", style = MaterialTheme.typography.labelMedium, color = NeonCyan)
                        }
                        if (project.outputPath != null) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = EmeraldActive)
                        }
                    }
                }
            }
        }

        // Favorites & Download History summary
        item {
            SectionHeader(
                title = if (isId) "Favorit & Unduhan (${favoriteJobs.size} • ${downloads.size})" else "Favorites & Downloads (${favoriteJobs.size} • ${downloads.size})",
                icon = Icons.Rounded.Favorite,
                action = {
                    OutlinedButton(onClick = { onNavigate(StudioRoute.HISTORY) }) {
                        Text(if (isId) "Riwayat" else "History")
                    }
                }
            )
        }
        items(downloads.take(3), key = { "dl_${it.id}" }) { dl ->
            LiquidGlassCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Rounded.Download, contentDescription = null, tint = EmeraldActive)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(dl.assetTitle, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${dl.exportDestination} • ${dl.fileSizeBytes / 1024} KB", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatGlassTile(
    label: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(modifier = modifier, accentGlow = accent, contentPadding = PaddingValues(14.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
