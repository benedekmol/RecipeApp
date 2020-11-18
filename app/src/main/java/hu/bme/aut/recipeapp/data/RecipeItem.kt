package hu.bme.aut.recipeapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipeitem")
class RecipeItem (
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id : Long?,
    @ColumnInfo(name = "name") val name : String,
    @ColumnInfo(name = "ingridients") val ingridients : String,
    @ColumnInfo(name = "directions") val directions : String,
    //@ColumnInfo(name = "photo") val photo : String,
    ) {

    fun recipeToString() : String {
        return "{\"id\":\"${id}\"," +
                "\"name\":\"${name}\"," +
                "\"ingridients\":\"${ingridients}\"," +
                "\"directions\":\"${directions}\"}"
    }

}