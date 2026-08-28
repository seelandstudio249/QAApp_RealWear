package com.studio249.qaapp_realwear.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.studio249.qaapp_realwear.ui.components.RealWearTile
import com.studio249.qaapp_realwear.ui.components.RealWearTopBar
import com.studio249.qaapp_realwear.ui.theme.QAApp_RealwearTheme
import com.studio249.qaapp_realwear.ui.theme.TextPrimary

@Composable
fun JobHubScreen(
    onTabSelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        RealWearTopBar(title = "JOB HUB")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.9f) // Increased weight to enlarge tiles
                .padding(4.dp), // Minimal padding to maximize tile size
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                RealWearTile(
                    label = "NEW INSPECTIONS",
                    icon = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, modifier = Modifier.size(84.dp), tint = TextPrimary) },
                    onClick = { onTabSelected("NewInspections") },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                RealWearTile(
                    label = "TO BE FIXED",
                    icon = { Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(84.dp), tint = TextPrimary) },
                    onClick = { onTabSelected("ToBeFixed") },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                RealWearTile(
                    label = "REINSPECTION",
                    icon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(84.dp), tint = TextPrimary) },
                    onClick = { onTabSelected("Reinspection") },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                RealWearTile(
                    label = "COMPLETED",
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(84.dp), tint = TextPrimary) },
                    onClick = { onTabSelected("Completed") },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 1280, heightDp = 720)
@Composable
fun JobHubScreenPreview() {
    QAApp_RealwearTheme {
        JobHubScreen(onTabSelected = {})
    }
}
