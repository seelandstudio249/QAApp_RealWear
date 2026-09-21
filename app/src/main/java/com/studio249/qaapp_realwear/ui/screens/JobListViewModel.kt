package com.studio249.qaapp_realwear.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studio249.qaapp_realwear.data.Repository
import com.studio249.qaapp_realwear.model.Job
import com.studio249.qaapp_realwear.model.JobStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JobListViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _jobs = MutableStateFlow<List<Job>>(emptyList())
    val jobs: StateFlow<List<Job>> = _jobs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun fetchJobs(type: String) {
        val status = when (type) {
            "NewInspections", "Outstanding" -> JobStatus.NewInspections
            "ToBeFixed", "InProgress" -> JobStatus.ToBeFixed
            "Reinspection", "InVerify" -> JobStatus.Reinspection
            "Completed" -> JobStatus.Completed
            else -> JobStatus.NewInspections
        }

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            val result = when (status) {
                JobStatus.NewInspections -> repository.getNewInspectionsList()
                JobStatus.ToBeFixed -> repository.getToBeFixedList()
                JobStatus.Reinspection -> repository.getReinspectionList()
                JobStatus.Completed -> repository.getCompletedList()
            }

            result.fold(
                onSuccess = { fetchedJobs ->
                    _jobs.value = fetchedJobs.sortedByDescending { it.createdAt }
                    _isLoading.value = false
                },
                onFailure = { throwable ->
                    _error.value = throwable.message ?: "Failed to load jobs"
                    _isLoading.value = false
                }
            )
        }
    }
}
