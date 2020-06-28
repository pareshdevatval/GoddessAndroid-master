package com.krystal.goddesslifestyle.data.response.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.krystal.goddesslifestyle.data.response.RecipeStep

/**
 * Created by Bhargav Thanki on 28 February,2020.
 */
@Dao
interface RecipeStepDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg recipeSteps: RecipeStep)
}