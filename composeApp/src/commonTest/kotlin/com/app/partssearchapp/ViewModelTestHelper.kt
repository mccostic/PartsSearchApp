package com.app.partssearchapp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

/**
 * Helper to set up coroutine dispatchers for ViewModel testing.
 * Call in @BeforeTest.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun setupTestDispatchers() {
    Dispatchers.setMain(UnconfinedTestDispatcher())
}

/**
 * Helper to tear down coroutine dispatchers.
 * Call in @AfterTest.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun tearDownTestDispatchers() {
    Dispatchers.resetMain()
}

/**
 * Waits for coroutines dispatched to Dispatchers.Default to complete.
 * Uses multiple context switches with yielding to drain the Default dispatcher queue,
 * plus a small real-time delay to allow suspended collectors (SharedFlow.collect)
 * to wake up and process buffered events.
 */
suspend fun awaitIdle() {
    repeat(3) {
        withContext(Dispatchers.Default) { yield() }
    }
    // Real-time delay on Default gives collector coroutines time to process
    withContext(Dispatchers.Default) { delay(50) }
    repeat(2) {
        withContext(Dispatchers.Default) { yield() }
    }
}

/**
 * Collects SharedFlow events into a list. Returns the job and the list.
 * Start this BEFORE emitting events since SharedFlow has no replay.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun <T> TestScope.collectEvents(flow: SharedFlow<T>): Pair<MutableList<T>, kotlinx.coroutines.Job> {
    val events = mutableListOf<T>()
    val job = launch(UnconfinedTestDispatcher(testScheduler)) {
        flow.collect { events.add(it) }
    }
    return events to job
}
