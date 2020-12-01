package hu.bme.aut.recipeapp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Database
import androidx.room.Room
import com.google.gson.Gson
import hu.bme.aut.recipeapp.adapter.RecipeAdapter
import hu.bme.aut.recipeapp.data.RecipeItem
import hu.bme.aut.recipeapp.data.RecipeListDatabase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.json.JSONObject
import java.io.File
import java.util.zip.Inflater
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity(), RecipeAdapter.RecipeItemClickListener {

    enum class ResultCode {
        CREATED,
        MODIFIED,
        DELETED,
        ABORTED,
        UNCHANGED;

        fun toInt(resultCode: ResultCode): Int {
            return resultCode.ordinal
        }
    }

    val FIRST_START = "MyFirstStart"

    var ricottaCake = RecipeItem(
        ingridients = """[{"unsalted butter":"200g"},{"caster sugar":"385g"},{"egg":"5"},{"flour":"450g"},{"baking powder":"1 tsp"},{"fresh ricotta":"700g"},{"cream cheese":"500g"},{"vanilla extract":"2 tsp"},{"pure icing sugar":" "}]""",
        name = "Baked Ricotta Cake",
        directions = "1. To make the pastry, place butter and 3/4 cup (165g) sugar in a stand mixer with the paddle attachment and beat until smooth. Beat in 1 egg until well combined. Fold in flour, baking powder and a pinch of salt. Cut one-third of the dough, flatten into a rectangle and enclose in plastic wrap. Shape the larger piece into a rectangle and enclose in plastic wrap. Chill both blocks of pastry for 2 hours.\n\n" +
                "2. Preheat oven to 200°C. Grease a 20cm x 30cm x 5cm rectangular lamington pan and line with baking paper, leaving a 3cm overhang. Roll larger piece of pastry out between 2 sheets of lightly floured baking paper to 5mm thick. Use pastry to line the base of prepared pan, trimming the edges to fit snugly, then prick all over with a fork. Place pan on a baking tray and bake for 20-25 minutes until base is golden. Remove from oven and cool completely.\n\n" +
                "3. Roll small pastry rectangle out between 2 sheets of floured baking paper into a 3mm-thick rectangle. Cut into long 6cm- wide strips to line the sides of the pan. Refrigerate while you make the filling.\n\n" +
                "4. Reduce oven to 160°C. To make filling, lightly beat remaining 4 eggs. Place ricotta and cream cheese in a stand mixer with the paddle attachment and beat until smooth. Beat in vanilla, remaining 1 cup (220g) sugar and eggs to combine.\n\n" +
                "5. Line sides of the pan with 6cm strips of pastry, gently pressing them onto top of cooked base to create a join (the pastry is soft, but will come together when cooked). Use a small sharp knife to trim the pastry so it’s flush with the top of the pan. Spoon filling into the pastry shell. Place pan on a baking tray and bake for 1 hour 20 minutes or until edges are set with a very slight wobble in the centre (it may crack a little, but will settle when it cools). Turn off the oven and set the door ajar with a wooden spoon. Leave the cake to cool completely in the oven, then chill for 6 hours to set. Use the baking paper overhang to carefully lift cake out of the pan. Dust cake with icing sugar to serve.",
        photoUri = "android.resource://hu.bme.aut.recipeapp/drawable/ricottacake",
        id = null
    )

    var muhallabia = RecipeItem(
        ingridients = """[{"finely ground white long-grain rice":"50g"},{"cornflour":"1 1\/2 tbs"},{"cold milk":"1l"},{"caster sugar":"100g"},{"almond meal":"75g"},{"orange blossom water":"1 1\/2 tbs"},{"chopped pistachios":"2 tbs"}]""",
        name = "Muhallabia",
        directions = "1. In a large bowl, mix together ground rice and cornflour. Add 1/4 cup (60ml) cold milk and sugar and stir to form a paste.\n\n" +
                "2. Place the remaining milk in a medium saucepan over medium-high heat. Bring to the boil and whisk in the cornflour paste. Reduce heat to medium and simmer, whisking frequently, for 10-15 minutes until thickened. Add the almond meal and continue to stir for a further 5 minutes, then add the orange blossom water.\n\n" +
                "3. Divide mixture among four 1-cup (250ml) capacity serving dishes or cups and refrigerate until cool. Scatter with chopped pistachios to serve.",
        photoUri = "android.resource://hu.bme.aut.recipeapp/drawable/muhallabia",
        id = null
    )
    var rhubarb_tarts = RecipeItem(
        ingridients = """[{"juice of an 🍊":"1"},{"vanilla bean, split, seeds scraped":"1"},{"raw sugar":"80g"},{"rhubarb":"500g"},{"egg, lightly beaten":"1"},{"vanilla ice cream":" "},{"unsalted butter":"200g"},{"flour":"250g"}]""",
        name = "Rustic Rhubarb Tarts",
        directions = "1. For pastry, toss butter and flour in a bowl until coated. Make a well in the centre and add 1/2 cup (125ml) chilled water and a pinch of salt. Using a butter knife, cut water into flour to quickly bring dough together, keeping pieces of butter intact (if dough is too dry add 1-2 tbs water). Shape dough into a block (it will be quite crumbly), and enclose in plastic wrap. Chill for 30 minutes.\n\n" +
                "2. Roll pastry out on a lightly floured work surface to a 30cm x 15cm rectangle. Fold the short end of pastry two-thirds of the way down the length. Fold the other end on top to enclose and form three layers. Enclose in plastic wrap and chill for 30 minutes.\n\n" +
                "3. Remove from fridge and place on a lightly floured surface with the layered seams facing you. Repeat rolling, folding and chilling method another 3 times, making sure the short seam-end faces you each time.\n\n" +
                "4. Place the orange juice, vanilla pod and seeds, sugar and rhubarb in a large frypan over medium heat. Bring to a simmer, then cook for 4 minutes or until the rhubarb is just soft. Set aside to cool.\n\n" +
                "5. Preheat oven to 200°C. Roll pastry out on a lightly floured surface until 3mm thick. Cut into six 12cm squares and place on a baking paper-lined baking tray.\n\n" +
                "6. Divide the rhubarb among pastry, leaving a 1cm border. Brush pastry edges with beaten egg. Bake for 15 minutes, then reduce oven to 180°C and cook for a further 10 minutes or until pastry is golden and puffed.\n\n" +
                "7. Serve tarts warm drizzled with rhubarb syrup and ice cream.",
        photoUri = "android.resource://hu.bme.aut.recipeapp/drawable/rhubarb",
        id = null
    )
    var apple_pie = RecipeItem(
        ingridients = """[{"unsalted butter":"30g"},{"Granny Smith apples, peeled":"1kg"},{"vanilla bean":"1"},{"caster sugar":"100g"},{"🍋":"1"},{"cornflour":"1 tbs"},{"egg":"1"},{"flour":"350g"},{"unsalted butter":"175g"},{"icing sugar":"75g"},{"beaten egg":"1"}]""",
        name = "Apple Pie",
        directions = "1. To make the pastry, place the flour, butter and sugar in a food processor with a pinch of salt. Process until the mixture resembles fine breadcrumbs. Add the egg and 2 tablespoons chilled water, then process until the mixture comes together to form a smooth ball. Divide into 2 portions, one slightly larger than the other, wrap in plastic wrap and refrigerate for 30 minutes.\n\n" +
                "2. Meanwhile, place butter in a large saucepan, add apples, scraped vanilla bean and seeds, sugar and lemon rind and juice. Cook over low heat for 6-8 minutes until just starting to soften. Mix the cornflour with 2 tablespoons cold water and stir into apple mixture. Cook for 1 minute. Remove vanilla bean, strain apples over a colander and discard juices.\n\n" +
                "3. Preheat oven to 200°C. Grease a 19 x 5cm fluted loose-bottomed tart pan. Roll larger piece of pastry on a lightly floured surface and use to line base and sides of pan. Refrigerate for 30 minutes. Line the pastry shell with baking paper and fill with rice or pastry weights.\n\n" +
                "4. Bake for 10 minutes. Remove paper and weights and return to oven for 5 minutes. Allow to cool. Pile apples into pastry. Roll out remaining pastry and press onto top of pie, using your fingers to crimp the edges, making sure edges are well sealed. Use any excess pastry to make decorative pattern for the lid. Brush top with egg, bake for 25 minutes or until cooked and golden. Rest for 10 minutes before removing from pan. Serve with vanilla custard.",
        photoUri = "android.resource://hu.bme.aut.recipeapp/drawable/applepie",
        id = null
    )

    var firstRecipes = mutableListOf<RecipeItem>()


    companion object {
        private const val TAG = "RecipeActivity"
        const val EXTRA_NEW_RECIPE = "extra.newrecipe"
        const val EXTRA_MODIFIED_RECIPE = "extra.modifiedrecipe"
    }

    val LAUNCH_RECIPE_ACTIVITY = 1

    //static recipe for testing
    //var recipe  = RecipeItem(id = null, name = "lasagna", ingridients = "tojas", directions = "sussd meg jol")


    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecipeAdapter
    private lateinit var database: RecipeListDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        fab.setOnClickListener {
            //INTENT TO RECIPE ACTIVITY TO CREATE RECIPE
            val showRecipeIntent = Intent()
            showRecipeIntent.setClass(this@MainActivity, RecipeActivity::class.java)
            showRecipeIntent.putExtra("RECIPE", "")
            startActivityForResult(showRecipeIntent, LAUNCH_RECIPE_ACTIVITY)
        }


        firstRecipes.add(ricottaCake)
        firstRecipes.add(muhallabia)
        firstRecipes.add(rhubarb_tarts)
        firstRecipes.add(apple_pie)


        //TODO CHANGING DATABSASE VERSION!!
        database = Room.databaseBuilder(
            applicationContext,
            RecipeListDatabase::class.java,
            "recipe-list6"
        ).build()
        initRecyclerView()

        //ADDING THE RECIPES ON THE FIRST START WITH SHARED PREFERENCE
        var settings = getSharedPreferences(FIRST_START, 0)
        if (settings.getBoolean("my_first_time", true)) {
            Log.d("App", "my first time using the app")

            for (item in firstRecipes)
                thread {
                    val newId = database.recipeItemDao().insert(item)
                    val newRecipeItem = RecipeItem(
                        id = newId,
                        name = item.name,
                        ingridients = item.ingridients,
                        directions = item.directions,
                        photoUri = item.photoUri
                    )
                    runOnUiThread {
                        adapter.addItem(newRecipeItem)
                    }
                }
            settings.edit().putBoolean("my_first_time", false).commit()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LAUNCH_RECIPE_ACTIVITY) {
            //IF WE CREATED A RECIPE WE SHOULD SAVE IT IN THE DB
            when (resultCode) {
                ResultCode.CREATED.toInt(ResultCode.CREATED) -> {

                    var gson = Gson()
                    var recipe =
                        gson.fromJson(data?.getStringExtra("RESULT"), RecipeItem::class.java)

                    thread {
                        val newId = database.recipeItemDao().insert(recipe)
                        val newRecipeItem = RecipeItem(
                            id = newId,
                            name = recipe.name,
                            ingridients = recipe.ingridients,
                            directions = recipe.directions,
                            photoUri = recipe.photoUri
                        )
                        runOnUiThread {
                            adapter.addItem(newRecipeItem)
                        }
                    }
                }
                ResultCode.ABORTED.toInt(ResultCode.ABORTED) -> Log.d("App", "halasdfasdfasdf")
                ResultCode.MODIFIED.toInt(ResultCode.MODIFIED) -> {

                    var gson = Gson()
                    var recipeModified =
                        gson.fromJson(data?.getStringExtra("RESULT"), RecipeItem::class.java)
                    Log.d("App", "gson parsolas" + recipeModified.id)
                    onItemChanged(recipeModified)


                    //TODO SOMETIMES LOAD ITEMS FINISHES FIRST
                    Thread.sleep(1000)

                    loadItemsInBackground()
                }
                ResultCode.UNCHANGED.toInt(ResultCode.UNCHANGED) -> Log.d("App", " unchanged hallo")
                else -> {
                    Log.d("App", "egyik sem")
                }
            }
        }
    }

    private fun initRecyclerView() {
        recyclerView = MainRecyclerView
        adapter = RecipeAdapter(this)
        loadItemsInBackground()
        recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        recyclerView.adapter = adapter
    }

    private fun loadItemsInBackground() {
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
        thread {
            if (item.photoUri != "null") {
                var picToDelete = File(Uri.parse(item.photoUri).path)
                picToDelete.delete()
            }
        }
    }

    override fun onItemSelected(item: RecipeItem) {
        val showRecipeIntent = Intent()
        showRecipeIntent.setClass(this@MainActivity, RecipeActivity::class.java)
        showRecipeIntent.putExtra("RECIPE", item.toJson())
        startActivityForResult(showRecipeIntent, LAUNCH_RECIPE_ACTIVITY)
        Log.d("App", "to json: " + item.toJson())
    }

    override fun onItemChanged(item: RecipeItem) {
        thread {
            database.recipeItemDao().update(item)
            Log.d("MainActivity", "recipe update was successful")
        }
    }


}