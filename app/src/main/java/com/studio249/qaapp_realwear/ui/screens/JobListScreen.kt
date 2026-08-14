package com.studio249.qaapp_realwear.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.studio249.qaapp_realwear.data.SeedDataRepository
import com.studio249.qaapp_realwear.model.Job
import com.studio249.qaapp_realwear.ui.components.RealWearBottomBar
import com.studio249.qaapp_realwear.ui.components.RealWearButton
import com.studio249.qaapp_realwear.ui.components.RealWearTopBar
import com.studio249.qaapp_realwear.ui.theme.AccentBlue
import com.studio249.qaapp_realwear.ui.theme.BgSurface
import com.studio249.qaapp_realwear.ui.theme.DividerColor
import com.studio249.qaapp_realwear.ui.theme.QAApp_RealwearTheme
import com.studio249.qaapp_realwear.ui.theme.TextPrimary
import com.studio249.qaapp_realwear.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@Composable
fun JobListScreen(
    type: String,
    onJobSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val repository = remember { SeedDataRepository() }
    var allJobs by remember { mutableStateOf<List<Job>>(emptyList()) }
    var displayedJobs by remember { mutableStateOf<List<Job>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isPaging by remember { mutableStateOf(false) }
    
    val pageSize = 5
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(type) {
        isLoading = true
        val result = when (type) {
            "Outstanding" -> repository.getOutstandingList()
            "InProgress" -> repository.getInProgressList()
            "InVerify" -> repository.getInVerifyList()
            "Completed" -> repository.getCompletedList()
            else -> repository.getOutstandingList()
        }
        allJobs = result.getOrDefault(emptyList()).sortedByDescending { it.createdAt }
        displayedJobs = allJobs.take(pageSize)
        isLoading = false
    }

    // Lazy load logic
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItemIndex >= displayedJobs.size - 1 && displayedJobs.size < allJobs.size
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && !isPaging) {
            isPaging = true
            kotlinx.coroutines.delay(800) // Simulate network delay for paging
            val currentSize = displayedJobs.size
            val nextSize = (currentSize + pageSize).coerceAtMost(allJobs.size)
            displayedJobs = allJobs.take(nextSize)
            isPaging = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        RealWearTopBar(
            title = "JOB - ${type.uppercase()}",
            rightContent = {
                // Removed height(50.dp) to prevent vertical clipping
                RealWearButton(
                    label = "PREVIOUS PAGE",
                    onClick = onBack
                )
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.76f)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AccentBlue)
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(displayedJobs) { index, job ->
                        JobRow(job = job, onClick = { onJobSelected(job.id) })
                        if (index < displayedJobs.size - 1) {
                            HorizontalDivider(color = DividerColor, thickness = 1.dp)
                        }
                    }
                    
                    if (isPaging) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = AccentBlue)
                            }
                        }
                    }
                }
            }
        }

        // Restored bottom bar to keep layout proportions
       // RealWearBottomBar {
            // Reserved for future actions
       // }
    }
}

@Composable
fun JobRow(job: Job, onClick: () -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp) // ~12% of content area
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = job.title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = job.createdAt.format(formatter),
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Preview(showBackground = true, widthDp = 1280, heightDp = 720)
@Composable
fun JobListScreenPreview() {
    QAApp_RealwearTheme {
        JobListScreen(
            type = "Outstanding",
            onJobSelected = {},
            onBack = {}
        )
    }
}
