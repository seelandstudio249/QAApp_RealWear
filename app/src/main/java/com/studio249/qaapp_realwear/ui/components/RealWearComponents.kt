package com.studio249.qaapp_realwear.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.studio249.qaapp_realwear.ui.theme.*

@Composable
fun RealWearButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = BgSurfaceRaised,
    contentColor: Color = TextPrimary,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val opacity = if (enabled) 1f else 0.4f
    val finalContainerColor = containerColor.copy(alpha = opacity)
    val finalContentColor = contentColor.copy(alpha = opacity)

    Box(
        modifier = modifier
            .padding(2.dp) 
            .clip(RoundedCornerShape(8.dp))
            .background(finalContainerColor)
            .then(
                if (isFocused && enabled) {
                    Modifier.border(3.dp, AccentBlue, RoundedCornerShape(8.dp))
                } else Modifier
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .focusable(enabled = enabled, interactionSource = interactionSource),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = finalContentColor,
            modifier = Modifier.padding(horizontal = 8.dp),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun RealWearTopBar(
    title: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = BgPrimary,
    rightContent: @Composable () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(backgroundColor),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            modifier = Modifier.weight(1f, fill = false),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        rightContent()
    }
}

@Composable
fun RealWearBottomBar(
    modifier: Modifier = Modifier,
    backgroundColor: Color = BgSurface,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(backgroundColor),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

@Composable
fun RealWearTile(
    label: String,
    icon: @Composable BoxScope.() -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Column(
        modifier = modifier
            .padding(4.dp) 
            .clip(RoundedCornerShape(8.dp))
            .background(BgSurfaceRaised)
            .then(
                if (isFocused) {
                    Modifier.border(3.dp, AccentBlue, RoundedCornerShape(8.dp))
                } else Modifier
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .focusable(interactionSource = interactionSource),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight(0.5f) 
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(widthDp = 1280, heightDp = 720)
@Composable
fun ComponentsPreview() {
    QAApp_RealwearTheme {
        Column(
            modifier = Modifier.fillMaxSize().background(BgPrimary).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RealWearTopBar(title = "TOP BAR TITLE") {
                RealWearButton(label = "ACTION", onClick = {})
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                RealWearButton(label = "DEFAULT BUTTON", onClick = {})
                RealWearButton(label = "GREEN BUTTON", onClick = {}, containerColor = AccentGreen)
                RealWearButton(label = "RED BUTTON", onClick = {}, containerColor = AccentRed)
            }
            
            Row(modifier = Modifier.height(200.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                RealWearTile(
                    label = "TILE EXAMPLE",
                    icon = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, modifier = Modifier.size(48.dp), tint = TextPrimary) },
                    onClick = {},
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                Box(modifier = Modifier.weight(1f))
            }

            RealWearBottomBar {
                RealWearButton(label = "BOTTOM LEFT", onClick = {}, modifier = Modifier.weight(1f).fillMaxHeight())
                RealWearButton(label = "BOTTOM RIGHT", onClick = {}, modifier = Modifier.weight(1f).fillMaxHeight())
            }
        }
    }
}
