package com.app.partssearchapp.screens.vehicleselection

import com.app.partssearchapp.*
import com.app.partssearchapp.data.models.VehicleEngine
import com.app.partssearchapp.data.models.VehicleMake
import com.app.partssearchapp.data.models.VehicleModel
import com.app.partssearchapp.fakes.FakePartsDataService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class VehicleSelectionViewModelTest {

  private lateinit var fakeService: FakePartsDataService

  @BeforeTest
  fun setup() {
    setupTestDispatchers()
    fakeService = FakePartsDataService()
  }

  @AfterTest
  fun tearDown() {
    tearDownTestDispatchers()
  }

  private fun createVm(
    preselectedMakeId: Int = -1,
    preselectedMakeName: String = "",
  ) = VehicleSelectionViewModel(
    params = VehicleSelectionParams(
      preselectedMakeId = preselectedMakeId,
      preselectedMakeName = preselectedMakeName,
    ),
    partsDataService = fakeService,
  )

  @Test
  fun initialStateLoadsMakes() = runTest {
    val vm = createVm()
    awaitIdle()

    val state = vm.stateFlow.value
    assertEquals(SelectionStep.MAKE, state.currentStep)
    assertEquals(3, state.makes.size)
    assertFalse(state.isLoading)
  }

  @Test
  fun preselectedMakeLoadsModels() = runTest {
    val vm = createVm(preselectedMakeId = 1, preselectedMakeName = "Toyota")
    awaitIdle()

    val state = vm.stateFlow.value
    assertEquals(SelectionStep.MODEL, state.currentStep)
    assertEquals(2, state.models.size)
    assertEquals("Toyota", state.selection.make?.name)
    assertFalse(state.isLoading)
  }

  @Test
  fun makeSelectedLoadsModels() = runTest {
    val vm = createVm()
    awaitIdle()

    val make = VehicleMake(1, "Toyota")
    vm.emitUIEvent(VehicleSelectionUIEvent.MakeSelected(make))
    awaitIdle()

    val state = vm.stateFlow.value
    assertEquals(SelectionStep.MODEL, state.currentStep)
    assertEquals(make, state.selection.make)
    assertEquals(2, state.models.size)
    assertFalse(state.isLoading)
  }

  @Test
  fun modelSelectedLoadsYears() = runTest {
    val vm = createVm()
    awaitIdle()

    vm.emitUIEvent(VehicleSelectionUIEvent.MakeSelected(VehicleMake(1, "Toyota")))
    awaitIdle()

    val model = VehicleModel(1, "Corolla", 1, 2024)
    vm.emitUIEvent(VehicleSelectionUIEvent.ModelSelected(model))
    awaitIdle()

    val state = vm.stateFlow.value
    assertEquals(SelectionStep.YEAR, state.currentStep)
    assertEquals(model, state.selection.model)
    assertEquals(listOf(2024, 2023, 2022), state.years)
    assertFalse(state.isLoading)
  }

  @Test
  fun yearSelectedLoadsEngines() = runTest {
    val vm = createVm()
    awaitIdle()

    vm.emitUIEvent(VehicleSelectionUIEvent.MakeSelected(VehicleMake(1, "Toyota")))
    awaitIdle()
    vm.emitUIEvent(VehicleSelectionUIEvent.ModelSelected(VehicleModel(1, "Corolla", 1, 2024)))
    awaitIdle()
    vm.emitUIEvent(VehicleSelectionUIEvent.YearSelected(2024))
    awaitIdle()

    val state = vm.stateFlow.value
    assertEquals(SelectionStep.ENGINE, state.currentStep)
    assertEquals(2024, state.selection.year)
    assertEquals(2, state.engines.size)
    assertFalse(state.isLoading)
  }

  @Test
  fun engineSelectedEmitsNavigateToCategories() = runTest {
    val vm = createVm()
    awaitIdle()

    val (navEvents, job) = collectEvents(vm.navEvents)

    vm.emitUIEvent(VehicleSelectionUIEvent.MakeSelected(VehicleMake(1, "Toyota")))
    awaitIdle()
    vm.emitUIEvent(VehicleSelectionUIEvent.ModelSelected(VehicleModel(1, "Corolla", 1, 2024)))
    awaitIdle()
    vm.emitUIEvent(VehicleSelectionUIEvent.YearSelected(2024))
    awaitIdle()

    val engine = VehicleEngine(1, "1.8L L4 DOHC", 1)
    vm.emitUIEvent(VehicleSelectionUIEvent.EngineSelected(engine))
    awaitIdle()

    assertTrue(navEvents.any { it is VehicleSelectionNavEvent.NavigateToCategories })
    val nav = navEvents.filterIsInstance<VehicleSelectionNavEvent.NavigateToCategories>().first()
    assertEquals(1, nav.params.engineId)
    job.cancel()
  }

  @Test
  fun backStepFromModelGoesToMake() = runTest {
    val vm = createVm()
    awaitIdle()

    vm.emitUIEvent(VehicleSelectionUIEvent.MakeSelected(VehicleMake(1, "Toyota")))
    awaitIdle()
    assertEquals(SelectionStep.MODEL, vm.stateFlow.value.currentStep)

    vm.emitUIEvent(VehicleSelectionUIEvent.BackStep)
    awaitIdle()

    val state = vm.stateFlow.value
    assertEquals(SelectionStep.MAKE, state.currentStep)
    assertNull(state.selection.make)
    assertTrue(state.models.isEmpty())
  }

  @Test
  fun backStepFromYearGoesToModel() = runTest {
    val vm = createVm()
    awaitIdle()

    vm.emitUIEvent(VehicleSelectionUIEvent.MakeSelected(VehicleMake(1, "Toyota")))
    awaitIdle()
    vm.emitUIEvent(VehicleSelectionUIEvent.ModelSelected(VehicleModel(1, "Corolla", 1, 2024)))
    awaitIdle()
    assertEquals(SelectionStep.YEAR, vm.stateFlow.value.currentStep)

    vm.emitUIEvent(VehicleSelectionUIEvent.BackStep)
    awaitIdle()

    val state = vm.stateFlow.value
    assertEquals(SelectionStep.MODEL, state.currentStep)
    assertNull(state.selection.model)
    assertTrue(state.years.isEmpty())
  }

  @Test
  fun backStepFromEngineGoesToYear() = runTest {
    val vm = createVm()
    awaitIdle()

    vm.emitUIEvent(VehicleSelectionUIEvent.MakeSelected(VehicleMake(1, "Toyota")))
    awaitIdle()
    vm.emitUIEvent(VehicleSelectionUIEvent.ModelSelected(VehicleModel(1, "Corolla", 1, 2024)))
    awaitIdle()
    vm.emitUIEvent(VehicleSelectionUIEvent.YearSelected(2024))
    awaitIdle()
    assertEquals(SelectionStep.ENGINE, vm.stateFlow.value.currentStep)

    vm.emitUIEvent(VehicleSelectionUIEvent.BackStep)
    awaitIdle()

    val state = vm.stateFlow.value
    assertEquals(SelectionStep.YEAR, state.currentStep)
    assertNull(state.selection.year)
    assertTrue(state.engines.isEmpty())
  }

  @Test
  fun backStepFromMakeEmitsNavigateBack() = runTest {
    val vm = createVm()
    awaitIdle()

    val (navEvents, job) = collectEvents(vm.navEvents)

    vm.emitUIEvent(VehicleSelectionUIEvent.BackStep)
    awaitIdle()

    assertTrue(navEvents.any { it is VehicleSelectionNavEvent.NavigateBack })
    job.cancel()
  }

  @Test
  fun makeSearchQueryUpdatesState() = runTest {
    val vm = createVm()
    awaitIdle()

    vm.emitUIEvent(VehicleSelectionUIEvent.MakeSearchChanged("Toy"))
    awaitIdle()

    assertEquals("Toy", vm.stateFlow.value.makeSearchQuery)
  }

  @Test
  fun modelSearchQueryUpdatesState() = runTest {
    val vm = createVm()
    awaitIdle()

    vm.emitUIEvent(VehicleSelectionUIEvent.ModelSearchChanged("Cor"))
    awaitIdle()

    assertEquals("Cor", vm.stateFlow.value.modelSearchQuery)
  }

  @Test
  fun goHomeEmitsNavigateBack() = runTest {
    val vm = createVm()
    awaitIdle()

    val (navEvents, job) = collectEvents(vm.navEvents)

    vm.emitUIEvent(VehicleSelectionUIEvent.GoHome)
    awaitIdle()

    assertTrue(navEvents.any { it is VehicleSelectionNavEvent.NavigateBack })
    job.cancel()
  }

  @Test
  fun makeSelectedResetsDownstreamState() = runTest {
    val vm = createVm()
    awaitIdle()

    // Navigate to ENGINE step
    vm.emitUIEvent(VehicleSelectionUIEvent.MakeSelected(VehicleMake(1, "Toyota")))
    awaitIdle()
    vm.emitUIEvent(VehicleSelectionUIEvent.ModelSelected(VehicleModel(1, "Corolla", 1, 2024)))
    awaitIdle()
    vm.emitUIEvent(VehicleSelectionUIEvent.YearSelected(2024))
    awaitIdle()

    assertTrue(vm.stateFlow.value.engines.isNotEmpty())

    // Go back to MAKE and select again
    vm.emitUIEvent(VehicleSelectionUIEvent.BackStep)
    awaitIdle()
    vm.emitUIEvent(VehicleSelectionUIEvent.BackStep)
    awaitIdle()
    vm.emitUIEvent(VehicleSelectionUIEvent.BackStep)
    awaitIdle()

    vm.emitUIEvent(VehicleSelectionUIEvent.MakeSelected(VehicleMake(2, "Honda")))
    awaitIdle()

    val state = vm.stateFlow.value
    assertEquals(SelectionStep.MODEL, state.currentStep)
    assertNull(state.selection.model)
    assertNull(state.selection.year)
    assertNull(state.selection.engine)
    assertTrue(state.years.isEmpty())
    assertTrue(state.engines.isEmpty())
  }

  @Test
  fun errorLoadingMakesSetsLoadingFalse() = runTest {
    fakeService.shouldThrow = true
    val vm = createVm()
    awaitIdle()

    val state = vm.stateFlow.value
    assertFalse(state.isLoading)
    assertTrue(state.makes.isEmpty())
  }
}
