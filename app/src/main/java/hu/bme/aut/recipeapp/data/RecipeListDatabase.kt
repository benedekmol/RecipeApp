package hu.bme.aut.recipeapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters


@Database(entities = [RecipeItem::class], version = 1)
abstract class RecipeListDatabase : RoomDatabase() {
    abstract fun recipeItemDao(): RecipeItemDao
}