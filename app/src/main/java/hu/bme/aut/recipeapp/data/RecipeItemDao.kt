package hu.bme.aut.recipeapp.data

import androidx.room.*

@Dao
interface RecipeItemDao {

    @Query("SELECT * FROM recipeitem")
    fun getAll(): List<RecipeItem>

    @Insert
    fun insert(recipeItem: RecipeItem): Long

    @Update
    fun update(recipeItem: RecipeItem)

    @Delete
    fun deleteItem(recipeItem: RecipeItem)
}