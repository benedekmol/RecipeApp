package hu.bme.aut.recipeapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Database
import androidx.room.Room
import hu.bme.aut.recipeapp.adapter.RecipeAdapter
import hu.bme.aut.recipeapp.data.RecipeItem
import hu.bme.aut.recipeapp.data.RecipeListDatabase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.json.JSONObject
import java.util.zip.Inflater
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity(), RecipeAdapter.RecipeItemClickListener {

    enum class ResultCode {
        CREATED,
        MODIFIED,
        DELETED,
        ABORTED,
        UNCHANGED;

        fun toInt(resultCode :ResultCode): Int {
            return resultCode.ordinal
        }
    }

    companion object {
        private const val TAG = "RecipeActivity"
        const val EXTRA_NEW_RECIPE = "extra.newrecipe"
        const val EXTRA_MODIFIED_RECIPE = "extra.modifiedrecipe"
    }

    val LAUNCH_RECIPE_ACTIVITY = 1

    //static recipe for testing
    var recipe  = RecipeItem(id = null, name = "lasagna", ingridients = "tojas", directions = "sussd meg jol")



    private lateinit var recyclerView : RecyclerView
    private lateinit var adapter : RecipeAdapter
    private lateinit var database: RecipeListDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        fab.setOnClickListener{
            //INTENT TO RECIPE ACTIVITY TO CREATE RECIPE
            val showRecipeIntent = Intent()
            showRecipeIntent.setClass(this@MainActivity, RecipeActivity::class.java)
            showRecipeIntent.putExtra("RECIPE","")
            startActivityForResult(showRecipeIntent, LAUNCH_RECIPE_ACTIVITY)
        }

        database = Room.databaseBuilder(
            applicationContext,
            RecipeListDatabase::class.java,
            "recipe-list"
        ).build()
        initRecyclerView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LAUNCH_RECIPE_ACTIVITY){
            //IF WE CREATED A RECIPE WE SHOULD SAVE IT IN THE DB
            when (resultCode) {
                ResultCode.CREATED.toInt(ResultCode.CREATED) -> {
                    var result = data?.getStringExtra("RESULT").toString()
                    Log.d("App", result)
                    var jsonResult = JSONObject(result)
                    var recipe = RecipeItem(id = null, name = jsonResult["name"].toString(),ingridients = jsonResult["ingridients"].toString(), directions = jsonResult["directions"].toString())
                    thread {
                        val newId = database.recipeItemDao().insert(recipe)
                        val newRecipeItem = RecipeItem(
                            id = newId,
                            name = recipe.name,
                            ingridients = recipe.ingridients,
                            directions = recipe.directions
                        )
                        runOnUiThread {
                            adapter.addItem(newRecipeItem)
                        }
                    }
                }
                ResultCode.ABORTED.toInt(ResultCode.ABORTED) -> Log.d("App", "halasdfasdfasdf")
                ResultCode.MODIFIED.toInt(ResultCode.MODIFIED) -> {
                    Log.d("App", data?.getStringExtra("RESULT").toString())
                    var jsonRecipe = JSONObject(data?.getStringExtra("RESULT").toString())
                    var recipe = RecipeItem(
                        id = jsonRecipe["id"].toString().toLong(),
                        name = jsonRecipe["name"].toString(),
                        ingridients = jsonRecipe["ingridients"].toString(),
                        directions = jsonRecipe["directions"].toString())

                    onItemChanged(recipe)

                    //TODO SOMETIMES LOAD ITEMS FINISHES FIRST
                    //Thread.sleep(1000)

                    loadItemsInBackground()
                    Log.d("App", recipe.recipeToString())
                }
                ResultCode.UNCHANGED.toInt(ResultCode.UNCHANGED) -> Log.d("App", " unchanged hallo")
                else -> {
                    Log.d("App", "egyik sem")
                }
            }


        }
    }

    private fun initRecyclerView(){
        recyclerView = MainRecyclerView
        adapter = RecipeAdapter(this)
        loadItemsInBackground()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadItemsInBackground(){
        thread {
            val items = database.recipeItemDao().getAll()
            runOnUiThread {
                adapter.update(items)
            }
        }
    }

    override fun onItemRemoved(item: RecipeItem) {
        thread {
            database.recipeItemDao().deleteItem(item)
            Log.d("MainActivity", "recipe deleted!")
        }
    }

    override fun onItemSelected(item: RecipeItem) {
        val showRecipeIntent = Intent()
        showRecipeIntent.setClass(this@MainActivity, RecipeActivity::class.java)
        //showRecipeIntent.putExtra("RECIPE", item.recipeToString())
        showRecipeIntent.putExtra("RECIPE", item.toJson())
        startActivityForResult(showRecipeIntent, LAUNCH_RECIPE_ACTIVITY)
        Log.d("App", "to json: " + item.toJson())
    }

    override fun onItemChanged(item: RecipeItem) {
        thread {
            database.recipeItemDao().update(item)
            Log.d("MainActivity", "recipe update was successful")
            //haloka
        }
    }


}