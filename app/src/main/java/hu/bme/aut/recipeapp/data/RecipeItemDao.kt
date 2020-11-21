package hu.bme.aut.recipeapp.data

import android.net.Uri
import androidx.room.*
import java.io.File

@Dao
interface RecipeItemDao {

    @Query("SELECT * FROM recipeitem")
    fun getAll(): List<RecipeItem>

    @Insert
    fun insert(recipeItem: RecipeItem): Long

    @Update
    fun update(recipeItem: RecipeItem)

    @Delete
    fun deleteItem(recipeItem: RecipeItem)  {
        if (recipeItem.photoUri != "null") {
            var picToDelete = File(Uri.parse(recipeItem.photoUri).path)
            picToDelete.delete()
        }
    }
}