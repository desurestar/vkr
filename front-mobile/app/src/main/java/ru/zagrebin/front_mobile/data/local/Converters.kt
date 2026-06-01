package ru.zagrebin.front_mobile.data.local

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import ru.zagrebin.front_mobile.domain.model.PostComment
import ru.zagrebin.front_mobile.domain.model.RecipeIngredient
import ru.zagrebin.front_mobile.domain.model.RecipeStep
import ru.zagrebin.front_mobile.domain.model.RecipeTag

class Converters {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val tagListType = Types.newParameterizedType(List::class.java, RecipeTag::class.java)
    private val ingredientListType = Types.newParameterizedType(List::class.java, RecipeIngredient::class.java)
    private val stepListType = Types.newParameterizedType(List::class.java, RecipeStep::class.java)
    private val commentListType = Types.newParameterizedType(List::class.java, PostComment::class.java)

    private val tagsAdapter = moshi.adapter<List<RecipeTag>>(tagListType)
    private val ingredientsAdapter = moshi.adapter<List<RecipeIngredient>>(ingredientListType)
    private val stepsAdapter = moshi.adapter<List<RecipeStep>>(stepListType)
    private val commentsAdapter = moshi.adapter<List<PostComment>>(commentListType)

    @TypeConverter
    fun fromTags(value: List<RecipeTag>): String = tagsAdapter.toJson(value)

    @TypeConverter
    fun toTags(value: String): List<RecipeTag> = tagsAdapter.fromJson(value).orEmpty()

    @TypeConverter
    fun fromIngredients(value: List<RecipeIngredient>): String = ingredientsAdapter.toJson(value)

    @TypeConverter
    fun toIngredients(value: String): List<RecipeIngredient> = ingredientsAdapter.fromJson(value).orEmpty()

    @TypeConverter
    fun fromSteps(value: List<RecipeStep>): String = stepsAdapter.toJson(value)

    @TypeConverter
    fun toSteps(value: String): List<RecipeStep> = stepsAdapter.fromJson(value).orEmpty()

    @TypeConverter
    fun fromComments(value: List<PostComment>): String = commentsAdapter.toJson(value)

    @TypeConverter
    fun toComments(value: String): List<PostComment> = commentsAdapter.fromJson(value).orEmpty()
}

