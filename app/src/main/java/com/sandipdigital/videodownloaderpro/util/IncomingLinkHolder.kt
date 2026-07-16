package com.sandipdigital.videodownloaderpro.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Small in-memory bridge so a link shared into the app (via ACTION_SEND) can be picked up
 * by the Home screen without a full deep-link graph. MainActivity sets this on launch;
 * HomeViewModel consumes and clears it once.
 */
object IncomingLinkHolder {
    private val _pendingLink = MutableStateFlow<String?>(null)
    val pendingLink = _pendingLink.asStateFlow()

    fun set(link: String) {
        _pendingLink.value = link
    }

    fun consume(): String? {
        val value = _pendingLink.value
        _pendingLink.value = null
        return value
    }
}
