package com.krystal.goddesslifestyle.data.response.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.krystal.goddesslifestyle.data.response.Recipe

/**
 * Created by Bhargav Thanki on 28 February,2020.
 */
@Dao
interface RecipeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg recipes: Recipe)

    @Query("SELECT * from Recipe where recipeId = :recipeId")
    fun getRecipe(recipeId: Int) : Recipe?
}