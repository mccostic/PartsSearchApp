package com.app.partssearchapp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext

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
 * Since our fakes are synchronous, a quick switch to Default and back is enough.
 */
suspend fun awaitIdle() {
  withContext(Dispatchers.Default) {}
}

/**
 * Collects SharedFlow events into a list. Returns the job and the list.
 * Start this BEFORE emitting events since SharedFlow has no replay.
 */
fun <T> TestScope.collectEvents(flow: SharedFlow<T>): Pair<MutableList<T>, kotlinx.coroutines.Job> {
  val events = mutableListOf<T>()
  val job = launch(UnconfinedTestDispatcher(testScheduler)) {
    flow.collect { events.add(it) }
  }
  return events to job
}
