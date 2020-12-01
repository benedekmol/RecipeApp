package hu.bme.aut.recipeapp

import android.Manifest.permission.*
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.FileUtils
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.FileProvider.getUriForFile
import androidx.core.util.rangeTo
import com.google.gson.Gson
import hu.bme.aut.recipeapp.data.RecipeItem
import kotlinx.android.synthetic.main.activity_recipe.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.net.URI
import java.util.*

class RecipeActivity : AppCompatActivity() {

    val REQUEST_IMAGE_CAPTURE = 2

    private val PERMISSION_REQUEST_CODE = 101

    var image_uri : Uri? = null

    private var mCurrentPhotoPath: String? = null;

    var pictureModified: Boolean = false
    var newRecipe : Boolean = false

    private lateinit var recipe : RecipeItem



    //modify
    private var recipeImage : ImageView? = null
    private var recipeString : String? = ""
    private var recipeNameEt : EditText? = null
    //private var recipeIngridientsEt : EditText? = null
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

    fun loadIngridients(ing : String) {

        val ingridientsListLayout = findViewById<LinearLayout>(R.id.RecipeIngridientsList) as LinearLayout

        var jsonIngs = JSONArray(ing)
        for (i in 0 until jsonIngs.length()) {
            var item = jsonIngs.getJSONObject(i)
            var name = item.keys().next()
            var quantity = item.getString(name)

            var to_add = layoutInflater.inflate(R.layout.ingridients, null,false) as ConstraintLayout
            var nameET = to_add.getChildAt(0) as EditText
            var quanatityET = to_add.getChildAt(1) as EditText
            nameET.setText(name)
            quanatityET.setText(quantity)
            ingridientsListLayout.addView(to_add)
        }
    }

    fun saveIngridients() : String {
        var ings : JSONArray = JSONArray()
        var recipeIngList = findViewById<LinearLayout>(R.id.RecipeIngridientsList) as LinearLayout
        for (i in 0 until recipeIngList.childCount ) {
            var ingridient = recipeIngList.getChildAt(i) as ConstraintLayout
            var ingName = ingridient.getChildAt(0) as EditText
            var ingQuantity = ingridient.getChildAt(1) as EditText
            var ing = JSONObject()
            ing.put(ingName.text.toString(), ingQuantity.text.toString())

            ings.put(ing)
        }
        return ings.toString()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe)
        setSupportActionBar(toolbarRecipe)

        //for (i in 0..4) {
        //    var to_add = layoutInflater.inflate(R.layout.ingridients, null,false)
        //    ingridientsListLayout.addView(to_add)
        //}


        Toast.makeText(this, "hallo", Toast.LENGTH_LONG).show()


        addIngridient.setOnClickListener(){
            val ingridientsListLayout = findViewById<LinearLayout>(R.id.RecipeIngridientsList) as LinearLayout
            var to_add = layoutInflater.inflate(R.layout.ingridients, null,false) as ConstraintLayout
            ingridientsListLayout.addView(to_add)
        }


        recipeImage = findViewById<View>(R.id.RecipeImage) as ImageView
        recipeNameEt = findViewById<View>(R.id.RecipeName) as EditText
        //recipeIngridientsEt = findViewById<View>(R.id.RecipeIngridients) as EditText
        recipeDirectionsEt = findViewById<View>(R.id.RecipeDirections) as EditText

        recipeImage!!.setOnClickListener(){
            Log.d("App", "clicked on image")
            Toast.makeText(this, "3", Toast.LENGTH_SHORT).show()

            if (checkPersmission()) takePicture() else requestPermission()

        }

        //IF THE RECIPE IS BEEING CREATED:
        if (intent.getStringExtra("RECIPE") == ""){

            Toast.makeText(this, "create", Toast.LENGTH_LONG).show()

            //recipeNameEt!!.isEnabled = true
            //recipeIngridientsEt!!.isEnabled = true
            //recipeDirectionsEt!!.isEnabled = true
            RecipeName.isEnabled = true
            //RecipeIngridients.isEnabled = true
            RecipeDirections.isEnabled = true
            newRecipe = true

        } else {
            //OTHERWISE LOAD THE RECIPE
            var recipeJSON = JSONObject(intent.getStringExtra("RECIPE").toString())

            var gson = Gson()
            recipe = gson.fromJson(intent.getStringExtra("RECIPE"), RecipeItem::class.java)

            //LOAD THE INGRIDIENTS
            loadIngridients(recipe.ingridients)
            //recipeNameEt!!.setText(recipe.name)
            //recipeIngridientsEt!!.setText(recipe.ingridients)
            //recipeDirectionsEt!!.setText(recipe.directions)
            RecipeName.setText(recipe.name)
            //RecipeIngridients.setText(recipe.ingridients)
            RecipeDirections.setText(recipe.directions)
            RecipeImage.setImageURI(Uri.parse(recipe.photoUri))
            image_uri = Uri.parse(recipe.photoUri)
            recipeNameEt!!.isEnabled = false
            //recipeIngridientsEt!!.isEnabled = false
            recipeDirectionsEt!!.isEnabled = false
        }

        //Log.d("App", "Created recipe activity for recipe: " + recipe.toJson().toString())
        Toast.makeText(this, "hallo2", Toast.LENGTH_SHORT).show()

    }



    override fun onBackPressed() {
        var recipeNew = RecipeItem(id = null,
            name = recipeNameEt!!.getText().toString(),
            ingridients = saveIngridients(),
            directions = recipeDirectionsEt!!.getText().toString(),
            photoUri = image_uri.toString())

        var recipeNewJson = recipeNew.toJson()

        if (intent.getStringExtra("RECIPE") == ""){
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Androidly Alert")
            builder.setMessage("We have a message")

            Log.d("App", "created recipe to json: " + recipeNewJson)

            builder.setPositiveButton("YES") { dialog, which ->
                Log.d("App", "click on yes")
                val resultIntent = Intent()

                resultIntent.putExtra("RESULT", recipeNewJson)
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
        } else if(recipe.name == recipeNew.name &&
                recipe.ingridients == recipeNew.ingridients &&
                recipe.directions == recipeNew.directions && !pictureModified ){
            val resultIntent = Intent()
            resultIntent.putExtra("RESULT", "")
            setResult(MainActivity.ResultCode.UNCHANGED.toInt(MainActivity.ResultCode.UNCHANGED),resultIntent)
            Log.d("App" , " RecipeActivity onBackPress() hasnt changed ")
            super.onBackPressed()

        } else {

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Sure?")
            builder.setMessage("Modify the recipe?")

            var recipeModified = RecipeItem(id = recipe.id,
                name = recipeNameEt!!.getText().toString(),
                ingridients = saveIngridients(),
                directions = recipeDirectionsEt!!.getText().toString(),
                photoUri = image_uri.toString())

            var recipeModifiedJson = recipeModified.toJson()
            //var recipeModefiedString = recipeModifiedJson.toString()
            builder.setPositiveButton("YES") { dialog, which ->
                var resultIntent = Intent()

                Log.d("App", recipeModifiedJson.toString())
                resultIntent.putExtra("RESULT",recipeModifiedJson)
                setResult(MainActivity.ResultCode.MODIFIED.toInt(MainActivity.ResultCode.MODIFIED),resultIntent)

                super.onBackPressed()
            }

            builder.setNegativeButton("NO") { dialog, which ->
                val resultIntent = Intent()
                resultIntent.putExtra("RESULT", "")
                setResult(MainActivity.ResultCode.UNCHANGED.toInt(MainActivity.ResultCode.UNCHANGED),resultIntent)
                super.onBackPressed()
            }
            builder.show()
        }
    }

    private fun checkPersmission(): Boolean {
        Log.d("App", "Checking permissions")
        return (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
            android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermission() {
        Log.d("App", "Requesting permissions")
        ActivityCompat.requestPermissions(this, arrayOf(READ_EXTERNAL_STORAGE, CAMERA,
            WRITE_EXTERNAL_STORAGE),
            PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {

                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    //takePicture()
                    Log.d("App", "request granted")
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
            else -> {
            }
        }
    }

    private fun takePicture() {
        Log.d("App", "Trying to take a picture")

        try {
            val dir = applicationContext.filesDir
            if (dir != null && !dir.exists()){
                dir.mkdir()
            }
            val file = File(dir, UUID.randomUUID().toString() + ".jpg")
            file.createNewFile()

            image_uri = getUriForFile(this, "hu.bme.aut.recipeapp.fileprovider", file)

        } catch (e : IOException){
            Log.d("App", e.toString())
        }

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        this.startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        //Toast.makeText(this, "returned with activity result", Toast.LENGTH_LONG).show()
        //Toast.makeText(this, resultCode.toString(), Toast.LENGTH_LONG).show()
        //Toast.makeText(this, data.toString(), Toast.LENGTH_LONG).show()
        when(requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                if (resultCode == Activity.RESULT_OK){
                    //val auxFile = File(mCurrentPhotoPath)
                    if (newRecipe == false){
                        if(recipe.photoUri != "null"){
                            var picToDelete = File(Uri.parse(recipe.photoUri).path)
                            picToDelete.delete()
                        }

                        recipe = RecipeItem(id = recipe.id, name = recipe.name, ingridients = recipe.ingridients, directions = recipe.directions, photoUri = image_uri.toString())

                    }

                    Log.d("App", "setting image")
                    RecipeImage.setImageURI(image_uri)
                    pictureModified = true
                    //Toast.makeText(this, image_uri.toString(), Toast.LENGTH_LONG).show()
                    //RecipeImage.setImageBitmap(data.extras!!.get("data") as Bitmap)
                    Toast.makeText(this, "returned with activity result", Toast.LENGTH_LONG).show()

                    //Log.d("App", image_uri.toString())

                    /*var bitmap: Bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath)
                    RecipeImage.setImageBitmap(bitmap)

                     */
                }
            }
            else -> Toast.makeText(this, "Unrecognized request code", Toast.LENGTH_SHORT).show()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun toggleModifier(){
        recipeNameEt!!.isEnabled = !recipeNameEt!!.isEnabled
        recipeDirectionsEt!!.isEnabled = !recipeDirectionsEt!!.isEnabled
    }
}