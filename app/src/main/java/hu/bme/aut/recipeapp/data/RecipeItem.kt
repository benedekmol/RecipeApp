package hu.bme.aut.recipeapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

import com.google.gson.Gson
import com.google.gson.GsonBuilder

//RECIPE ITEM
@Entity(tableName = "recipeitem")
class RecipeItem (
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id : Long?,
    @ColumnInfo(name = "name") val name : String,
    @ColumnInfo(name = "ingridients") val ingridients : String,
    @ColumnInfo(name = "directions") val directions : String,
    @ColumnInfo(name = "photo") val photoUri : String
    ) {

    //CREATING A JSON OUT OF THE RECIPE
    fun toJson() : String {
        var gson = Gson()
        return gson.toJson(this)
    }
}