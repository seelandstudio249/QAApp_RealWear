package com.studio249.qaapp_realwear.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.studio249.qaapp_realwear.ui.components.VoiceButton
import com.studio249.qaapp_realwear.utils.StorageUtils
import java.io.File

@Composable
fun ReviewScreen(
    taskId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var photos by remember { mutableStateOf(emptyList<File>()) }
    var selectedPhoto by remember { mutableStateOf<File?>(null) }

    // Load photos from directory
    LaunchedEffect(Unit) {
        val directory = StorageUtils.getTaskFolder(context, taskId)
        photos = directory.listFiles { file -> file.extension == "jpg" }?.toList() ?: emptyList()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Review Photos: $taskId",
                    style = MaterialTheme.typography.headlineSmall
                )
                VoiceButton(
                    label = "PREVIOUS PAGE",
                    onClick = onBack,
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (photos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No photos captured yet.")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(150.dp),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(photos) { photo ->
                        AsyncImage(
                            model = photo,
                            contentDescription = "Photo ${photo.name}",
                            modifier = Modifier
                                .size(150.dp)
                                .clickable { selectedPhoto = photo },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        // Scale-up View (Overlay)
        selectedPhoto?.let { photo ->
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.8f)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AsyncImage(
                        model = photo,
                        contentDescription = "Selected Photo",
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .fillMaxHeight(0.7f),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        VoiceButton(
                            label = "PREVIOUS PAGE",
                            onClick = { selectedPhoto = null },
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                        VoiceButton(
                            label = "DELETE PHOTO",
                            onClick = {
                                if (photo.delete()) {
                                    photos = photos.filter { it != photo }
                                    selectedPhoto = null
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
