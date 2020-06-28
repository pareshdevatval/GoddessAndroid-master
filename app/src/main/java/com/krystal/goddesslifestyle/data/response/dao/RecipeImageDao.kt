package com.krystal.goddesslifestyle.data.response.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.krystal.goddesslifestyle.data.response.RecipeImage

/**
 * Created by Bhargav Thanki on 28 February,2020.
 */
@Dao
interface RecipeImageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg recipeImages: RecipeImage)

    @Query("SELECT recipeImageUrl from RecipeImage where recipeImageRecipeId = :recipeId")
    fun getRecipeImages(recipeId: Int) : List<String>
}