package ru.zagrebin.front_mobile.domain.usecase

import ru.zagrebin.front_mobile.data.repository.FeedRepository

class RefreshRecipeDetailsUseCase(private val repository: FeedRepository) {
    suspend operator fun invoke(id: Int) = repository.refreshRecipeDetails(id)
}

