package com.diavolo.gogroceriesapp.api.category.domain.delete

/** Items that used the deleted category keep existing and fall back to no category. */
interface DeleteCategoryUseCase {
    suspend operator fun invoke(id: Long)
}
