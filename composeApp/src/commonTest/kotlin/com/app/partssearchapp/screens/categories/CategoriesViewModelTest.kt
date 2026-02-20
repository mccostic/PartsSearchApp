package com.app.partssearchapp.screens.categories

import com.app.partssearchapp.*
import com.app.partssearchapp.data.models.PartCategory
import com.app.partssearchapp.fakes.FakePartsDataService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class CategoriesViewModelTest {

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

  private fun createVm(engineId: Int = 1) = CategoriesViewModel(
    params = CategoriesParams(engineId = engineId, vehicleBreadcrumb = "Toyota > Corolla > 2024"),
    partsDataService = fakeService,
  )

  @Test
  fun initialStateLoadsCategories() = runTest {
    val vm = createVm()
    awaitIdle()

    val state = vm.stateFlow.value
    assertEquals(2, state.categories.size)
    assertEquals("Toyota > Corolla > 2024", state.vehicleBreadcrumb)
    assertEquals(1, state.engineId)
    assertFalse(state.isLoading)
  }

  @Test
  fun categoryWithSubcategoriesTogglesExpanded() = runTest {
    val vm = createVm()
    awaitIdle()

    val category = PartCategory(1, "Brake & Wheel Hub", subcategories = listOf(
      PartCategory(11, "Brake Pad", parentId = 1),
    ))
    vm.emitUIEvent(CategoriesUIEvent.CategoryClicked(category))
    awaitIdle()

    assertEquals(1, vm.stateFlow.value.expandedCategoryId)

    // Click again to collapse
    vm.emitUIEvent(CategoriesUIEvent.CategoryClicked(category))
    awaitIdle()

    assertNull(vm.stateFlow.value.expandedCategoryId)
  }

  @Test
  fun categoryWithoutSubcategoriesNavigatesToParts() = runTest {
    val vm = createVm()
    awaitIdle()

    val (navEvents, job) = collectEvents(vm.navEvents)

    val leafCategory = PartCategory(11, "Brake Pad", parentId = 1)
    vm.emitUIEvent(CategoriesUIEvent.CategoryClicked(leafCategory))
    awaitIdle()

    assertTrue(navEvents.any { it is CategoriesNavEvent.NavigateToPartsListing })
    val nav = navEvents.filterIsInstance<CategoriesNavEvent.NavigateToPartsListing>().first()
    assertEquals(11, nav.categoryId)
    assertEquals("Brake Pad", nav.categoryName)
    assertEquals(1, nav.engineId)
    job.cancel()
  }

  @Test
  fun subcategoryClickedNavigatesToParts() = runTest {
    val vm = createVm()
    awaitIdle()

    val (navEvents, job) = collectEvents(vm.navEvents)

    val sub = PartCategory(21, "Oil Filter", parentId = 2)
    vm.emitUIEvent(CategoriesUIEvent.SubcategoryClicked(sub))
    awaitIdle()

    assertTrue(navEvents.any { it is CategoriesNavEvent.NavigateToPartsListing })
    val nav = navEvents.filterIsInstance<CategoriesNavEvent.NavigateToPartsListing>().first()
    assertEquals(21, nav.categoryId)
    assertEquals("Oil Filter", nav.categoryName)
    job.cancel()
  }

  @Test
  fun toggleCategoryTogglesExpandedState() = runTest {
    val vm = createVm()
    awaitIdle()

    vm.emitUIEvent(CategoriesUIEvent.ToggleCategory(1))
    awaitIdle()
    assertEquals(1, vm.stateFlow.value.expandedCategoryId)

    vm.emitUIEvent(CategoriesUIEvent.ToggleCategory(1))
    awaitIdle()
    assertNull(vm.stateFlow.value.expandedCategoryId)

    vm.emitUIEvent(CategoriesUIEvent.ToggleCategory(2))
    awaitIdle()
    assertEquals(2, vm.stateFlow.value.expandedCategoryId)
  }

  @Test
  fun backPressedEmitsNavigateBack() = runTest {
    val vm = createVm()
    awaitIdle()

    val (navEvents, job) = collectEvents(vm.navEvents)

    vm.emitUIEvent(CategoriesUIEvent.BackPressed)
    awaitIdle()

    assertTrue(navEvents.any { it is CategoriesNavEvent.NavigateBack })
    job.cancel()
  }

  @Test
  fun goToCartEmitsNavigateToCart() = runTest {
    val vm = createVm()
    awaitIdle()

    val (navEvents, job) = collectEvents(vm.navEvents)

    vm.emitUIEvent(CategoriesUIEvent.GoToCart)
    awaitIdle()

    assertTrue(navEvents.any { it is CategoriesNavEvent.NavigateToCart })
    job.cancel()
  }

  @Test
  fun errorLoadingCategoriesSetsLoadingFalse() = runTest {
    fakeService.shouldThrow = true
    val vm = createVm()
    awaitIdle()

    assertFalse(vm.stateFlow.value.isLoading)
    assertTrue(vm.stateFlow.value.categories.isEmpty())
  }
}
