package com.app.partssearchapp.screens.categories

import com.app.partssearchapp.data.models.PartCategory

data class CategoriesState(
  val categories: List<PartCategory> = emptyList(),
  val vehicleBreadcrumb: String = "",
  val engineId: Int = 0,
  val expandedCategoryId: Int? = null,
  val isLoading: Boolean = false,
)

sealed class CategoriesUIEvent {
  data class CategoryClicked(val category: PartCategory) : CategoriesUIEvent()
  data class SubcategoryClicked(val subcategory: PartCategory) : CategoriesUIEvent()
  data class ToggleCategory(val categoryId: Int) : CategoriesUIEvent()
  data object BackPressed : CategoriesUIEvent()
  data object GoToCart : CategoriesUIEvent()
}

sealed class CategoriesNavEvent {
  data class NavigateToPartsListing(
    val categoryId: Int,
    val categoryName: String,
    val engineId: Int,
    val vehicleBreadcrumb: String,
  ) : CategoriesNavEvent()
  data object NavigateBack : CategoriesNavEvent()
  data object NavigateToCart : CategoriesNavEvent()
}

sealed class CategoriesUIEffect
