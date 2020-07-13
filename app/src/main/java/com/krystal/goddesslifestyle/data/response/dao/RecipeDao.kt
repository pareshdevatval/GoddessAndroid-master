package com.krystal.goddesslifestyle.data.response.dao

import androidx.room.*
import com.krystal.goddesslifestyle.data.response.Recipe
import com.krystal.goddesslifestyle.data.response.TodaysRecipe

/**
 * Created by Bhargav Thanki on 28 February,2020.
 */
@Dao
interface RecipeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg recipes: Recipe)

    @Query("SELECT * from Recipe where recipeId = :recipeId")
    fun getRecipe(recipeId: Int) : Recipe?

    @Delete
    fun deleteRecipes(recipe: Recipe)
}