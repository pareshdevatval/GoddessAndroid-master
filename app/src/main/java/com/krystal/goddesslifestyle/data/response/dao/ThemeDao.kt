package com.krystal.goddesslifestyle.data.response.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.krystal.goddesslifestyle.data.response.Theme

/**
 * Created by Bhargav Thanki on 28 February,2020.
 */
@Dao
interface ThemeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(theme: Theme)

    @Query("SELECT * from Theme where themeId = :themeId")
    fun getCurrentMonthTheme(themeId: Int) : Theme?


}