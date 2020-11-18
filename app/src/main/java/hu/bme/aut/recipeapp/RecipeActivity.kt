package hu.bme.aut.recipeapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_recipe.*
import org.json.JSONObject

class RecipeActivity : AppCompatActivity() {

    //modify
    private var modifiable : Boolean = false
    private var modified : Boolean = false

    private var id : Int? = null
    private var name : String? = ""
    private var ingridients : String? = ""
    private var directions : String? = ""


    private var recipeString : String? = ""
    private var recipeNameEt : EditText? = null
    private var recipeIngridientsEt : EditText? = null
    private var recipeDirectionsEt : EditText? = null

    companion object {
        private const val TAG = "RecipeActivity"
        const val EXTRA_RECIPE = "extra.recipe"
    }




    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val toolbarMenu: Menu = toolbarRecipe.menu
        menuInflater.inflate(R.menu.menu_recipe, toolbarMenu)
        for (i in 0 until toolbarMenu.size()){
            val menuItem: MenuItem = toolbarMenu.getItem(i)
            menuItem.setOnMenuItemClickListener { item -> onOptionsItemSelected(item) }
        }
        return super.onCreateOptionsMenu(menu)
    }




    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                toggleModifier()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe)
        setSupportActionBar(toolbarRecipe)

        recipeNameEt = findViewById<View>(R.id.RecipeName) as EditText
        recipeIngridientsEt = findViewById<View>(R.id.RecipeIngridients) as EditText
        recipeDirectionsEt = findViewById<View>(R.id.RecipeDirections) as EditText

        //IF THE RECIPE IS BEEING CREATED:
        if (intent.getStringExtra("RECIPE") == ""){
            recipeNameEt!!.isEnabled = true
            recipeIngridientsEt!!.isEnabled = true
            recipeDirectionsEt!!.isEnabled = true

        } else {
            //OTHERWISE LOAD THE RECIPE
            var recipeJSON = JSONObject(intent.getStringExtra("RECIPE").toString())

            id = recipeJSON["id"].toString().toInt()
            Log.d("App", id.toString())
            name = recipeJSON["name"].toString()
            ingridients = recipeJSON["ingridients"].toString()
            directions = recipeJSON["directions"].toString()

            recipeNameEt!!.setText(recipeJSON["name"].toString())
            recipeIngridientsEt!!.setText(recipeJSON["ingridients"].toString())
            recipeDirectionsEt!!.setText(recipeJSON["directions"].toString())

            recipeNameEt!!.isEnabled = false
            recipeIngridientsEt!!.isEnabled = false
            recipeDirectionsEt!!.isEnabled = false
        }


    }

    override fun onBackPressed() {

        if (intent.getStringExtra("RECIPE") == ""){
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Androidly Alert")
            builder.setMessage("We have a message")

            builder.setPositiveButton("YES") { dialog, which ->
                Log.d("App", "click on yes")
                val resultIntent = Intent()
                resultIntent.putExtra("RESULT",
                    "{ \"name\" : \"${recipeNameEt!!.getText().toString()}\"," +
                            "\"ingridients\":\"${recipeIngridientsEt!!.getText().toString()}\"," +
                            "\"directions\" : \"${recipeDirectionsEt!!.getText().toString()}\"}")
                setResult(MainActivity.ResultCode.CREATED.toInt(MainActivity.ResultCode.CREATED),resultIntent)
                super.onBackPressed()
            }

            builder.setNegativeButton("NO") { dialog, which ->
                Log.d("App", "click on no")
                val resultIntent = Intent()
                resultIntent.putExtra("RESULT", "")
                setResult(MainActivity.ResultCode.ABORTED.toInt(MainActivity.ResultCode.ABORTED),resultIntent)
                super.onBackPressed()
            }
            builder.show()
        } else if(name == recipeNameEt!!.getText().toString() &&
                ingridients == recipeIngridientsEt!!.getText().toString() &&
                directions == recipeDirectionsEt!!.getText().toString()){
            val resultIntent = Intent()
            resultIntent.putExtra("RESULT", "")
            setResult(MainActivity.ResultCode.UNCHANGED.toInt(MainActivity.ResultCode.UNCHANGED),resultIntent)
            Log.d("App" , " RecipeActivity onBackPress() hasnt changed ")
            super.onBackPressed()

        } else {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Sure?")
            builder.setMessage("Modify the recipe?")

            builder.setPositiveButton("YES") { dialog, which ->
                var resultIntent = Intent()
                resultIntent.putExtra("RESULT",
                    "{ \"id\" : \"${id}\"," +
                            " \"name\" : \"${recipeNameEt!!.getText().toString()}\"," +
                            "\"ingridients\":\"${recipeIngridientsEt!!.getText().toString()}\"," +
                            "\"directions\" : \"${recipeDirectionsEt!!.getText().toString()}\"}")
                setResult(MainActivity.ResultCode.MODIFIED.toInt(MainActivity.ResultCode.MODIFIED),resultIntent)
                super.onBackPressed()
            }

            builder.setNegativeButton("NO") { dialog, which ->
                val resultIntent = Intent()
                resultIntent.putExtra("RESULT", "")
                setResult(MainActivity.ResultCode.UNCHANGED.toInt(MainActivity.ResultCode.UNCHANGED),resultIntent)
                super.onBackPressed()
                Log.d("App", "RecipeActivity Recipe unchanged!")
            }
            builder.show()
        }
    }

    fun toggleModifier(){
        recipeNameEt!!.isEnabled = !recipeNameEt!!.isEnabled
        recipeIngridientsEt!!.isEnabled = !recipeIngridientsEt!!.isEnabled
        recipeDirectionsEt!!.isEnabled = !recipeDirectionsEt!!.isEnabled
    }

}