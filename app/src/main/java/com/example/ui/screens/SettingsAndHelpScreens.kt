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
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Help
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.WifiTethering
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.StudioPreferences
import com.example.domain.model.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.theme.EmeraldActive
import com.example.ui.theme.GlassChip
import com.example.ui.theme.LiquidGlassCard
import com.example.ui.theme.LocalAiNoticeBanner
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SectionHeader

@Composable
fun SettingsScreen(
    prefs: StudioPreferences,
    selfHostedStatus: String?,
    onDarkModeChange: (Boolean) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onSelfHostedUrlChange: (String) -> Unit,
    onTestSelfHosted: (String) -> Unit,
    onUserNameChange: (String) -> Unit,
    onDefaultFpsChange: (Int) -> Unit,
    onAutosaveChange: (Boolean) -> Unit
) {
    val isId = prefs.language == AppLanguage.ID
    var endpointDraft by remember(prefs.selfHostedEndpoint) { mutableStateOf(prefs.selfHostedEndpoint) }
    var nameDraft by remember(prefs.userDisplayName) { mutableStateOf(prefs.userDisplayName) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionHeader(
                title = if (isId) "Pengaturan Studio" else "Studio Settings",
                subtitle = if (isId) "Tema, Bahasa, Profil Lokal & Adapter Open-Source" else "Theme, Language, Local Profile & Open-Source Adapter",
                icon = Icons.Rounded.Settings
            )
        }

        // Theme & Language
        item {
            LiquidGlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Rounded.DarkMode, contentDescription = null, tint = NeonCyan)
                        Column {
                            Text(if (isId) "Mode Gelap Liquid Glass" else "Liquid Glass Dark Mode", style = MaterialTheme.typography.titleMedium)
                            Text(if (isId) "Aktifkan tampilan obsidian VisionOS" else "VisionOS obsidian translucent theme", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = prefs.isDarkMode,
                        onCheckedChange = onDarkModeChange,
                        modifier = Modifier.testTag("settings_dark_mode_switch")
                    )
                }

                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.Language, contentDescription = null, tint = NeonCyan)
                    Text(if (isId) "Bahasa Antarmuka" else "Interface Language", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassChip(
                        label = "Bahasa Indonesia",
                        selected = prefs.language == AppLanguage.ID,
                        onClick = { onLanguageChange(AppLanguage.ID) },
                        testTag = "lang_id_chip"
                    )
                    GlassChip(
                        label = "English",
                        selected = prefs.language == AppLanguage.EN,
                        onClick = { onLanguageChange(AppLanguage.EN) },
                        testTag = "lang_en_chip"
                    )
                }
            }
        }

        // Profile & Autosave
        item {
            LiquidGlassCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.Person, contentDescription = null, tint = NeonCyan)
                    Text(if (isId) "Profil Kreator Lokal" else "Local Creator Profile", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = nameDraft,
                        onValueChange = { nameDraft = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        label = { Text(if (isId) "Nama Kreator" else "Creator Name") }
                    )
                    FilledTonalButton(onClick = { onUserNameChange(nameDraft) }) {
                        Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Save")
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isId) "Autosave Proyek Otomatis" else "Autosave Projects Automatically", style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = prefs.autosaveEnabled, onCheckedChange = onAutosaveChange)
                }
                Spacer(Modifier.height(8.dp))
                Text("Default FPS: ${prefs.defaultFps}", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(24, 30, 60).forEach { fps ->
                        GlassChip(
                            label = "${fps}fps",
                            selected = prefs.defaultFps == fps,
                            onClick = { onDefaultFpsChange(fps) }
                        )
                    }
                }
            }
        }

        // Self-Hosted Open-Source Adapter Configuration (NO API KEY NEEDED)
        item {
            LiquidGlassCard(accentGlow = NeonCyan) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.WifiTethering, contentDescription = null, tint = NeonCyan)
                    Text(
                        text = if (isId) "Adapter Server Open-Source Self-Hosted" else "Self-Hosted Open-Source Server Adapter",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (isId) {
                        "Opsional: hubungkan endpoint ComfyUI / FastAPI lokal Anda untuk Wan 2.1, Mochi 1, atau CogVideoX. Tidak perlu memasukkan API Key."
                    } else {
                        "Optional: connect your own local ComfyUI / FastAPI worker endpoint for Wan 2.1, Mochi 1, or CogVideoX. No user API key required."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = endpointDraft,
                    onValueChange = { endpointDraft = it },
                    modifier = Modifier.fillMaxWidth().testTag("self_hosted_url_input"),
                    singleLine = true,
                    label = { Text("Worker Base URL (e.g. http://192.168.1.15:8000)") }
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(onClick = { onSelfHostedUrlChange(endpointDraft) }) {
                        Text(if (isId) "Simpan URL" else "Save URL")
                    }
                    OutlinedButton(onClick = {
                        onSelfHostedUrlChange(endpointDraft)
                        onTestSelfHosted(endpointDraft)
                    }) {
                        Text(if (isId) "Uji Koneksi" else "Test Connection")
                    }
                }
                if (!selfHostedStatus.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(selfHostedStatus, style = MaterialTheme.typography.labelMedium, color = NeonCyan)
                }
            }
        }

        item {
            LiquidGlassCard(accentGlow = EmeraldActive) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.Security, contentDescription = null, tint = EmeraldActive)
                    Text(AppStrings.freeEngineTitle(prefs.language), style = MaterialTheme.typography.titleMedium, color = EmeraldActive)
                }
                Spacer(Modifier.height(6.dp))
                Text(AppStrings.freeEngineDesc(prefs.language), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun HelpCenterScreen(language: AppLanguage) {
    val isId = language == AppLanguage.ID
    val faqs = if (isId) {
        listOf(
            Triple("1. Mesin Video Lokal Gratis (Aktif)", "Videon Local Motion v1 menggunakan encoder H.264 MediaCodec & MediaMuxer native Android untuk merender video MP4 sungguhan di perangkat Anda dengan dukungan foto referensi, gerakan kamera, filter gaya, dan ekspor langsung ke Galeri.", Icons.Rounded.Memory),
            Triple("2. Mengapa Model Cloud Premium 'Belum Tersedia'?", "Model seperti Seedance 2.5, Kling 3.0/2.6, Veo, Sora, Runway, Hailuo, Luma, dan Pika membutuhkan integrasi backend provider resmi. Kami menampilkan status ini secara jujur tanpa simulasi palsu atau paywall.", Icons.Rounded.CloudOff),
            Triple("3. Cara Menghubungkan Server Open-Source Sendiri", "Anda dapat menjalankan Wan 2.1, Mochi 1, atau CogVideoX di PC GPU Anda melalui FastAPI dengan endpoint GET /health dan POST /api/v1/generate/video, lalu masukkan URL lokalnya di menu Pengaturan.", Icons.Rounded.WifiTethering),
            Triple("4. Keterbatasan GPU & Antrean Perangkat", "Untuk menjaga stabilitas memori perangkat seluler, antrean render lokal memproses 1 tugas video aktif pada satu waktu. Tugas tambahan akan otomatis mengantre dan berjalan berurutan.", Icons.Rounded.Help)
        )
    } else {
        listOf(
            Triple("1. Free Local Video Engine (Active)", "Videon Local Motion v1 uses Android's native MediaCodec H.264 & MediaMuxer pipeline to render real playable MP4 files on-device with reference photos, camera motion, visual styles, and zero-permission Gallery export.", Icons.Rounded.Memory),
            Triple("2. Why Are Proprietary Cloud Models Marked Unavailable?", "Models like Seedance 2.5, Kling 3.0/2.6, Veo, Sora, Runway, Hailuo, Luma, and Pika require official backend provider access. We show their integration status honestly instead of faking results or charging credits.", Icons.Rounded.CloudOff),
            Triple("3. Connecting a Self-Hosted Open-Source Worker", "Run Wan 2.1, Mochi 1, or CogVideoX on your own GPU machine via a FastAPI server exposing GET /health and POST /api/v1/generate/video, then save the local URL in Settings.", Icons.Rounded.WifiTethering),
            Triple("4. Device GPU & Queue Transparency", "To protect mobile encoder memory, the local queue executes 1 active MP4 render at a time. Additional jobs queue cleanly and execute sequentially.", Icons.Rounded.Help)
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = if (isId) "Pusat Bantuan & Dokumentasi" else "Help Center & Architecture",
                subtitle = if (isId) "Panduan transparan arsitektur Videon AI Studio" else "Transparent guide to Videon AI Studio architecture",
                icon = Icons.Rounded.Help
            )
        }
        item { LocalAiNoticeBanner(isIndonesian = isId) }
        items(faqs) { (title, body, icon) ->
            LiquidGlassCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(icon, contentDescription = null, tint = NeonCyan)
                    Text(title, style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(6.dp))
                Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
