package dev.forint.deafmute.cameraxdemo.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CountdownManager(
    private val scope: CoroutineScope,
    private val durationMs: Long,
    private val tickMs: Long,
    private val onFinished: () -> Unit,
) {
    private val _remainingMillis = MutableStateFlow(0L)
    val remainingMillis: StateFlow<Long> = _remainingMillis.asStateFlow()

    private var job: Job? = null

    fun restart() {
        cancel(resetValue = false)
        job = scope.launch {
            var remaining = durationMs
            _remainingMillis.value = remaining
            while (remaining > 0) {
                delay(tickMs)
                remaining = (remaining - tickMs).coerceAtLeast(0L)
                _remainingMillis.value = remaining
            }
            onFinished()
        }
    }

    fun cancel(resetValue: Boolean = true) {
        job?.cancel()
        job = null
        if (resetValue) {
            _remainingMillis.value = 0L
        }
    }
}

