package ru.zagrebin.front_mobile.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.zagrebin.front_mobile.data.repository.FeedRepository
import ru.zagrebin.front_mobile.domain.model.ArticleDetails

class ObserveArticleDetailsUseCase(private val repository: FeedRepository) {
    operator fun invoke(id: Int): Flow<ArticleDetails?> = repository.observeArticleDetails(id)
}

