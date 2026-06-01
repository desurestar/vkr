package ru.zagrebin.front_mobile.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.zagrebin.front_mobile.domain.model.RecipeIngredient
import ru.zagrebin.front_mobile.domain.model.RecipeStep
import ru.zagrebin.front_mobile.domain.model.RecipeTag

@Entity(tableName = "recipe_details")
data class RecipeDetailsEntity(
    @PrimaryKey val id: Int,
    val authorId: String,
    val authorName: String,
    val authorHandle: String,
    val authorAvatarUrl: String?,
    val date: String,
    val title: String,
    val imageUrl: String,
    val likes: String,
    val time: String,
    val calories: String,
    val views: String,
    val isSaved: Boolean,
    val proteinsPer100: Float,
    val fatsPer100: Float,
    val carbsPer100: Float,
    val kcalPer100: Int,
    val tags: List<RecipeTag>,
    val ingredients: List<RecipeIngredient>,
    val steps: List<RecipeStep>
)

