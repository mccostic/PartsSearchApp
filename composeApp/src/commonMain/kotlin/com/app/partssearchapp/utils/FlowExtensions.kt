package com.app.partssearchapp.utils

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce

/**
 * Debounce extension for flows with 300ms delay
 */
@OptIn(FlowPreview::class)
fun <T> Flow<T>.debounce300(): Flow<T> = debounce(300)
