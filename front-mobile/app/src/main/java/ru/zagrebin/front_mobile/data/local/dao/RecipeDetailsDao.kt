package ru.zagrebin.front_mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.zagrebin.front_mobile.data.local.entities.RecipeDetailsEntity

@Dao
interface RecipeDetailsDao {
    @Query("SELECT * FROM recipe_details WHERE id = :id")
    fun observeById(id: Int): Flow<RecipeDetailsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: RecipeDetailsEntity)
}

