package com.krystal.goddesslifestyle.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.krystal.goddesslifestyle.data.db.dao.CartAmountDao
import com.krystal.goddesslifestyle.data.db.dao.CartDao
import com.krystal.goddesslifestyle.data.db.dao.UserDao
import com.krystal.goddesslifestyle.data.db.model.Cart
import com.krystal.goddesslifestyle.data.db.model.CartAmount
import com.krystal.goddesslifestyle.data.db.model.User
import com.krystal.goddesslifestyle.data.response.*
import com.krystal.goddesslifestyle.data.response.dao.*


@Database(
    entities = [(User::class), (CalendarsData::class), (Equipment::class),
        (Journal::class), (Practice::class), (PracticeEquipment::class),
        (Recipe::class), (RecipeImage::class), (RecipeStep::class),
        (Theme::class), (ThemeMaster::class), (TodaysJournal::class),
        (TodaysPractic::class), (TodaysRecipe::class), (Cart::class),
        (Product::class), (CartAmount::class) , (UserSubscription::class)], version = 2
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    abstract fun calenderDataDao(): CalendarsDataDao

    abstract fun equipmentDao(): EquipmentDao

    abstract fun journalDao(): JournalDao

    abstract fun practiceDao(): PracticeDao

    abstract fun practiceEquipmentDao(): PracticeEquipmentDao

    abstract fun recipeDao(): RecipeDao

    abstract fun recipeImageDao(): RecipeImageDao

    abstract fun recipeStepDao(): RecipeStepDao

    abstract fun themeDao(): ThemeDao

    abstract fun themeMasterDao(): ThemeMasterDao

    abstract fun todaysJournalDao(): TodaysJournalDao

    abstract fun todaysPracticeDao(): TodaysPracticeDao

    abstract fun todaysRecipeDao(): TodaysRecipeDao

    abstract fun cartDao(): CartDao

    abstract fun cartProductDao(): CartProductDao

    abstract fun cartAmountDao(): CartAmountDao

    abstract fun userSubscriptionDao(): UserSubscriptionDao



    /*abstract fun ofTheMonthDao(): OfTheMonthDao

    abstract fun goddessOfTheMonthDao(): GoddessOfTheMonthDao

    abstract fun recipeOfTheMonthDao(): RecipeOfTheMonthDao

    abstract fun recipeImageOfTheMonthDao(): RecipeImageOfTheMonthDao

    abstract fun teacherOfTheMonthDao(): TeacherOfTheMonthDao*/

}
