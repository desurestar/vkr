package ru.zagrebin.front_mobile.domain.usecase

import ru.zagrebin.front_mobile.data.repository.FeedRepository
import ru.zagrebin.front_mobile.data.repository.RefreshResult

class RefreshRecipesFeedUseCase(private val repository: FeedRepository) {
    suspend operator fun invoke(): RefreshResult = repository.refreshRecipes()
}