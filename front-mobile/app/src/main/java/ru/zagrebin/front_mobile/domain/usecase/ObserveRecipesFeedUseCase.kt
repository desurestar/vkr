package ru.zagrebin.front_mobile.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.zagrebin.front_mobile.data.repository.FeedRepository
import ru.zagrebin.front_mobile.domain.model.FeedItem

class ObserveRecipesFeedUseCase(private val repository: FeedRepository) {
    operator fun invoke(): Flow<List<FeedItem>> = repository.observeRecipes()
}