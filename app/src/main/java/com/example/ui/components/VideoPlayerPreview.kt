package com.example.ui.components

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.NeonCyan
import java.io.File

@Composable
fun InlineMp4PlayerCard(
    videoPath: String,
    title: String,
    onExportToGallery: () -> Unit,
    onShare: () -> Unit,
    onOpenInEditor: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var replayTrigger by remember(videoPath) { mutableIntStateOf(0) }
    val exists = remember(videoPath) { File(videoPath).exists() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xCC070C1C))
            .border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
            .padding(12.dp)
    ) {
        if (exists) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            val controller = MediaController(ctx)
                            controller.setAnchorView(this)
                            setMediaController(controller)
                            setOnPreparedListener { mp ->
                                mp.isLooping = true
                                start()
                            }
                        }
                    },
                    update = { view ->
                        val targetUri = Uri.fromFile(File(videoPath))
                        if (view.tag != "$videoPath#$replayTrigger") {
                            view.tag = "$videoPath#$replayTrigger"
                            view.setVideoURI(targetUri)
                            view.start()
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0A1024)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Video file path: $videoPath",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = if (exists) "${File(videoPath).length() / 1024} KB • MP4 H.264" else "Remote / Pending URL",
            style = MaterialTheme.typography.labelMedium,
            color = NeonCyan
        )

        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (exists) {
                OutlinedButton(
                    onClick = { replayTrigger++ },
                    modifier = Modifier.weight(1f).testTag("replay_video_button")
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("Replay", maxLines = 1)
                }
                FilledTonalButton(
                    onClick = onExportToGallery,
                    modifier = Modifier.weight(1f).testTag("export_video_button")
                ) {
                    Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("Save", maxLines = 1)
                }
                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier.weight(1f).testTag("share_video_button")
                ) {
                    Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("Share", maxLines = 1)
                }
            }
        }
        if (onOpenInEditor != null && exists) {
            Spacer(Modifier.height(6.dp))
            OutlinedButton(
                onClick = onOpenInEditor,
                modifier = Modifier.fillMaxWidth().testTag("send_to_editor_button")
            ) {
                Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Text("Open in Timeline Editor")
            }
        }
    }
}
