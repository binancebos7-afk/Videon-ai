package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MovieCreation
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.ViewCarousel
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.CharacterEntity
import com.example.data.local.PromptTemplateEntity
import com.example.domain.model.AppLanguage
import com.example.domain.model.CameraMovement
import com.example.domain.model.VisualStyle
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.DeepViolet
import com.example.ui.theme.EmeraldActive
import com.example.ui.theme.GlassChip
import com.example.ui.theme.GlassStatusBadge
import com.example.ui.theme.GradientPrimaryButton
import com.example.ui.theme.LiquidGlassCard
import com.example.ui.theme.LocalAiNoticeBanner
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RoseError
import com.example.ui.theme.SectionHeader
import com.example.ui.viewmodel.PromptAssistantUiState

@Composable
fun CharacterStudioScreen(
    language: AppLanguage,
    characters: List<CharacterEntity>,
    onSaveCharacter: (String, String, String, String, String, String, String?) -> Unit,
    onDeleteCharacter: (Long) -> Unit,
    onUseInVideoStudio: (CharacterEntity) -> Unit,
    onGenerateConsistentScenes: (CharacterEntity) -> Unit
) {
    val isId = language == AppLanguage.ID
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var faceDesc by remember { mutableStateOf("") }
    var outfitDesc by remember { mutableStateOf("") }
    var poseRef by remember { mutableStateOf("") }
    var styleTag by remember { mutableStateOf("Cinematic Sci-Fi") }
    var pickedUri by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) pickedUri = uri.toString()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionHeader(
                title = if (isId) "Character Consistency Studio" else "Character Consistency Studio",
                subtitle = if (isId) "Simpan wajah, kostum, pose & seed agar konsisten lintas adegan" else "Lock face, outfit, pose & seed for cross-scene consistency",
                icon = Icons.Rounded.Face
            )
        }

        // Create Character Form
        item {
            LiquidGlassCard {
                Text(if (isId) "Buat Karakter Baru" else "Create New Character", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth().testTag("char_name_input"),
                    singleLine = true,
                    label = { Text(if (isId) "Nama Karakter" else "Character Name") }
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(if (isId) "Peran / Arketipe" else "Role / Archetype") }
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = faceDesc,
                    onValueChange = { faceDesc = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(if (isId) "Referensi Wajah" else "Face Consistency Specs") }
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = outfitDesc,
                    onValueChange = { outfitDesc = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(if (isId) "Referensi Kostum / Outfit" else "Outfit Reference") }
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = poseRef,
                    onValueChange = { poseRef = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(if (isId) "Referensi Pose" else "Pose Reference") }
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (pickedUri != null) "Photo ✓" else "Upload Photo")
                    }
                    GradientPrimaryButton(
                        text = if (isId) "Simpan Karakter" else "Save Character",
                        icon = Icons.Rounded.Add,
                        onClick = {
                            onSaveCharacter(name, role, faceDesc, outfitDesc, poseRef, styleTag, pickedUri)
                            name = ""; role = ""; faceDesc = ""; outfitDesc = ""; poseRef = ""; pickedUri = null
                        },
                        modifier = Modifier.weight(1.2f),
                        testTag = "save_character_btn"
                    )
                }
            }
        }

        // Character Library
        item {
            Text(
                text = if (isId) "Pustaka Karakter (${characters.size})" else "Character Library (${characters.size})",
                style = MaterialTheme.typography.titleLarge
            )
        }

        items(characters, key = { it.id }) { char ->
            LiquidGlassCard(accentGlow = DeepViolet) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                    val imgMod = Modifier.size(86.dp).clip(RoundedCornerShape(16.dp))
                    if (!char.imageUriString.isNullOrBlank()) {
                        AsyncImage(
                            model = char.imageUriString,
                            contentDescription = char.name,
                            modifier = imgMod,
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        val resId = if (char.drawableResName == "img_char_anime") R.drawable.img_char_anime else R.drawable.img_char_cyber
                        Image(
                            painter = painterResource(id = resId),
                            contentDescription = char.name,
                            modifier = imgMod,
                            contentScale = ContentScale.Crop
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(char.name, style = MaterialTheme.typography.titleMedium)
                            IconButton(onClick = { onDeleteCharacter(char.id) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = RoseError, modifier = Modifier.size(18.dp))
                            }
                        }
                        Text(char.role, style = MaterialTheme.typography.labelMedium, color = NeonCyan)
                        Spacer(Modifier.height(4.dp))
                        GlassStatusBadge(text = "Seed #${char.consistencySeed}", color = EmeraldActive, dot = false)
                        Spacer(Modifier.height(6.dp))
                        Text("Face: ${char.faceDescription}", style = MaterialTheme.typography.bodyMedium)
                        Text("Outfit: ${char.outfitDescription}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = { onUseInVideoStudio(char) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Rounded.MovieCreation, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (isId) "Pakai di Video" else "Use in Video", maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = { onGenerateConsistentScenes(char) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Rounded.ViewCarousel, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Multi-Scene", maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
fun PromptLibraryScreen(
    language: AppLanguage,
    assistantState: PromptAssistantUiState,
    templates: List<PromptTemplateEntity>,
    onTopicChange: (String) -> Unit,
    onStyleChange: (VisualStyle) -> Unit,
    onCameraChange: (CameraMovement) -> Unit,
    onRunEnhanceAndTranslate: () -> Unit,
    onRunStoryboard: () -> Unit,
    onRunImageToPrompt: (String) -> Unit,
    onUsePrompt: (String) -> Unit,
    onToggleFavorite: (PromptTemplateEntity) -> Unit
) {
    val isId = language == AppLanguage.ID
    val clipboard = LocalClipboardManager.current
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onRunImageToPrompt(uri.toString())
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionHeader(
                title = if (isId) "Asisten & Pustaka Prompt AI" else "AI Prompt Assistant & Library",
                subtitle = if (isId) "Sempurnakan prompt, terjemahkan ID→EN, gambar-ke-prompt & storyboard" else "Enhance prompts, translate ID→EN, image-to-prompt & storyboard",
                icon = Icons.Rounded.Psychology
            )
        }

        item { LocalAiNoticeBanner(isIndonesian = isId) }

        // Assistant Composer
        item {
            LiquidGlassCard(accentGlow = NeonCyan) {
                OutlinedTextField(
                    value = assistantState.inputTopic,
                    onValueChange = onTopicChange,
                    modifier = Modifier.fillMaxWidth().testTag("assistant_topic_input"),
                    minLines = 2,
                    label = { Text(if (isId) "Ide Adegan (Indonesia / Inggris)" else "Scene Idea (Indonesian / English)") }
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(VisualStyle.entries) { st ->
                        GlassChip(
                            label = if (isId) st.labelId else st.labelEn,
                            selected = assistantState.selectedStyle == st,
                            onClick = { onStyleChange(st) }
                        )
                    }
                    items(CameraMovement.entries) { cam ->
                        GlassChip(
                            label = if (isId) cam.labelId else cam.labelEn,
                            selected = assistantState.selectedCamera == cam,
                            onClick = { onCameraChange(cam) }
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GradientPrimaryButton(
                        text = if (isId) "Sempurnakan + ID→EN" else "Enhance + ID→EN",
                        icon = Icons.Rounded.AutoAwesome,
                        enabled = !assistantState.isWorking,
                        onClick = onRunEnhanceAndTranslate,
                        modifier = Modifier.weight(1.2f)
                    )
                    OutlinedButton(
                        onClick = onRunStoryboard,
                        enabled = !assistantState.isWorking,
                        modifier = Modifier.weight(1f).height(52.dp)
                    ) {
                        Icon(Icons.Rounded.ViewCarousel, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Storyboard")
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (isId) "Buat Prompt dari Gambar (Photo Picker)" else "Generate Prompt from Photo")
                }
            }
        }

        if (assistantState.enhancedResult.isNotBlank()) {
            item {
                LiquidGlassCard(accentGlow = EmeraldActive) {
                    Text("Cinematic Enhanced Prompt", style = MaterialTheme.typography.titleMedium, color = NeonCyan)
                    Spacer(Modifier.height(4.dp))
                    Text(assistantState.enhancedResult, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { clipboard.setText(AnnotatedString(assistantState.enhancedResult)) }) {
                            Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Copy")
                        }
                        FilledTonalButton(onClick = { onUsePrompt(assistantState.enhancedResult) }) {
                            Text(if (isId) "Gunakan di Studio" else "Use in Studio")
                        }
                    }
                    if (assistantState.translatedResult.isNotBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Rounded.Translate, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(16.dp))
                            Text("ID → EN Translation", style = MaterialTheme.typography.labelLarge, color = AmberWarning)
                        }
                        Text(assistantState.translatedResult, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        if (assistantState.imageDerivedPrompt.isNotBlank()) {
            item {
                LiquidGlassCard(accentGlow = DeepViolet) {
                    Text("Image-to-Prompt Output", style = MaterialTheme.typography.titleMedium, color = NeonCyan)
                    Spacer(Modifier.height(4.dp))
                    Text(assistantState.imageDerivedPrompt, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    FilledTonalButton(onClick = { onUsePrompt(assistantState.imageDerivedPrompt) }) {
                        Text(if (isId) "Gunakan di Video Studio" else "Use in Video Studio")
                    }
                }
            }
        }

        if (assistantState.storyboardScenes.isNotEmpty()) {
            items(assistantState.storyboardScenes) { sc ->
                LiquidGlassCard {
                    Text("Scene ${sc.sceneNumber} • ${sc.shotType} (${sc.durationSec}s)", style = MaterialTheme.typography.titleMedium, color = NeonCyan)
                    Spacer(Modifier.height(4.dp))
                    Text(sc.visualPrompt, style = MaterialTheme.typography.bodyMedium)
                    Text("Audio Cue: ${sc.audioCue}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    OutlinedButton(onClick = { onUsePrompt(sc.visualPrompt) }) {
                        Text(if (isId) "Render Scene Ini" else "Render This Scene")
                    }
                }
            }
        }

        if (assistantState.consistentScenes.isNotEmpty()) {
            items(assistantState.consistentScenes) { scenePrompt ->
                LiquidGlassCard(accentGlow = DeepViolet) {
                    Text(scenePrompt, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(6.dp))
                    FilledTonalButton(onClick = { onUsePrompt(scenePrompt) }) {
                        Text(if (isId) "Pakai di Video" else "Send to Video")
                    }
                }
            }
        }

        // Curated Library
        item {
            Text(
                text = if (isId) "Pustaka Prompt Terkurasi" else "Curated Prompt Library",
                style = MaterialTheme.typography.titleLarge
            )
        }

        items(templates, key = { it.id }) { tpl ->
            val promptBody = if (isId) tpl.promptId else tpl.promptEn
            LiquidGlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(tpl.title, style = MaterialTheme.typography.titleMedium)
                        Text("${tpl.category} • ${tpl.cameraHint}", style = MaterialTheme.typography.labelMedium, color = NeonCyan)
                    }
                    IconButton(onClick = { onToggleFavorite(tpl) }) {
                        Icon(
                            imageVector = if (tpl.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (tpl.isFavorite) RoseError else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(promptBody, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { clipboard.setText(AnnotatedString(promptBody)) }) {
                        Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Copy")
                    }
                    FilledTonalButton(onClick = { onUsePrompt(promptBody) }) {
                        Text(if (isId) "Gunakan" else "Use")
                    }
                }
            }
        }
    }
}
