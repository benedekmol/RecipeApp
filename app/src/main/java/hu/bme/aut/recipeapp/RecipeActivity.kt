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
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.FileProvider.getUriForFile
import androidx.core.util.rangeTo
import androidx.core.view.isVisible
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

    //REQUEST CODE FOR IMAGE CAPTURE INTENT
    val REQUEST_IMAGE_CAPTURE = 2

    //REQUEST CODE FOR PERMISSION GRANTING
    private val PERMISSION_REQUEST_CODE = 101

    //IMAGE URI OF THE RECIPE
    var image_uri: Uri? = null

    var pictureModified: Boolean = false
    var newRecipe: Boolean = false

    private lateinit var recipe: RecipeItem

    var modify: Boolean = false

    //RECIPE DATA VIEWS
    private var recipeImage: ImageView? = null
    private var recipeNameEt: EditText? = null
    private var recipeDirectionsEt: EditText? = null

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //LOAD THE MENU
        val toolbarMenu: Menu = toolbarRecipe.menu
        menuInflater.inflate(R.menu.menu_recipe, toolbarMenu)
        for (i in 0 until toolbarMenu.size()) {
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
                //CHANGE THE LAYOUT TO MODIFY THE RECIPE IF WE CLICK THE PENCIL ICON
                toggleModifier()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //MODIFY THE INGRIDIENTS
    fun modifyIngridients() {
        var recipeIngList = findViewById<LinearLayout>(R.id.RecipeIngridientsList) as LinearLayout
        //ITERATE THROUGH THE CHILD VIEWS OF THE LINEARLAYOUT
        for (i in 0 until recipeIngList.childCount) {
            var ing = recipeIngList.getChildAt(i) as LinearLayout
            var name = ing.getChildAt(0) as EditText
            var quantity = ing.getChildAt(1) as EditText
            var removeButton = ing.getChildAt(2) as Button
            //SET TO MODIFIABLE IF THE MODIFY IS TRUE
            if (modify == true) {
                removeButton.visibility = View.VISIBLE
                name.isEnabled = true
                quantity.isEnabled = true
            } else {
                name.isEnabled = false
                quantity.isEnabled = false
                removeButton.visibility = View.INVISIBLE
            }
        }
    }

    //LOAD THE INGRIDIENTS FROM THE RECIPE INTO THE LINEARLAYOUT
    fun loadIngridients(ing: String) {

        //GET THE LINEAR LAYOUT FOR THE INGRIDIENTS
        val ingridientsListLayout =
            findViewById<LinearLayout>(R.id.RecipeIngridientsList) as LinearLayout

        //JSON OF THE RECIPE INGRIDIENTS
        var jsonIngs = JSONArray(ing)
        for (i in 0 until jsonIngs.length()) {
            //LOAD THE DATA
            var item = jsonIngs.getJSONObject(i)
            var name = item.keys().next()
            var quantity = item.getString(name)

            //CREATE A VIEW AND ADD TO THE PARENT LINEAR LAYOUT
            var to_add = layoutInflater.inflate(R.layout.ingridients, null, false) as LinearLayout
            var nameET = to_add.getChildAt(0) as EditText
            var quanatityET = to_add.getChildAt(1) as EditText
            var deleteButton = to_add.getChildAt(2) as Button
            nameET.setText(name)
            quanatityET.setText(quantity)
            deleteButton.setOnClickListener() {
                ingridientsListLayout.removeView(to_add)
                Toast.makeText(this, "Ingridient Deleted", Toast.LENGTH_SHORT).show()
            }
            ingridientsListLayout.addView(to_add)
        }
    }

    fun saveIngridients(): String {
        var ings: JSONArray = JSONArray()
        //LOAD THE INGRIDIENTS LAYOUT VIEW
        var recipeIngList = findViewById<LinearLayout>(R.id.RecipeIngridientsList) as LinearLayout
        for (i in 0 until recipeIngList.childCount) {
            //ITERATE THROUGH THE INGRIDIENTS' VIEWS
            var ingridient = recipeIngList.getChildAt(i) as LinearLayout
            var ingName = ingridient.getChildAt(0) as EditText
            var ingQuantity = ingridient.getChildAt(1) as EditText
            var ing = JSONObject()

            //Only add ingridient if it has a name
            if (ingName.text.toString() != "") {
                if (ingQuantity.text.toString() == "") {
                    ing.put(ingName.text.toString(), " ")
                } else {
                    ing.put(ingName.text.toString(), ingQuantity.text.toString())
                }
                ings.put(ing)
            }
        }
        //RETURN WITH A JSON ARRAY STRING
        return ings.toString()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe)
        setSupportActionBar(toolbarRecipe)

        //ADD INGRIDIENTS BUTTON LISTENER
        addIngridient.setOnClickListener() {
            val ingridientsListLayout =
                findViewById<LinearLayout>(R.id.RecipeIngridientsList) as LinearLayout
            //CREATING VIEW
            var to_add = layoutInflater.inflate(R.layout.ingridients, null, false) as LinearLayout
            var deleteButton = to_add.getChildAt(2) as Button
            //SETTING DELETE BUTTON LISTENER
            deleteButton.setOnClickListener() {
                ingridientsListLayout.removeView(to_add)
                Toast.makeText(this, "Ingridient Deleted", Toast.LENGTH_LONG).show()
            }
            //ADDING IT TO THE LINEARLAYOUT OF THE INGRIDIENTS
            ingridientsListLayout.addView(to_add)
        }

        recipeImage = findViewById<View>(R.id.RecipeImage) as ImageView
        recipeNameEt = findViewById<View>(R.id.RecipeName) as EditText
        recipeDirectionsEt = findViewById<View>(R.id.RecipeDirections) as EditText

        //IF WE CLICK ON THE IMAGE
        recipeImage!!.setOnClickListener() {
            Log.d("App", "clicked on image")
            //MODIFY HAS TO BE TRUE TO MODIFY THE IMAGE
            //CHECKING FOR PERMISSIONS, REQUESTING IF IT ISNT GRANTED
            //TAKING THE PICTURE
            if (modify == true) {
                if (checkPersmission()) {
                    takePicture()
                }
                else {
                    requestPermission()
                }
            }
        }

        //IF THE RECIPE IS BEEING CREATED:
        if (intent.getStringExtra("RECIPE") == "") {

            Toast.makeText(this, "Press Back to save recipe", Toast.LENGTH_LONG).show()

            newRecipe = true
            toggleModifier()

        } else {
            //OTHERWISE LOAD THE RECIPE
            var recipeJSON = JSONObject(intent.getStringExtra("RECIPE").toString())

            var gson = Gson()
            recipe = gson.fromJson(intent.getStringExtra("RECIPE"), RecipeItem::class.java)

            //LOAD THE INGRIDIENTS
            loadIngridients(recipe.ingridients)
            RecipeName.setText(recipe.name)
            RecipeDirections.setText(recipe.directions)
            RecipeImage.setImageURI(Uri.parse(recipe.photoUri))
            image_uri = Uri.parse(recipe.photoUri)

            modify()
        }
    }

    //PRESSING THE BACK BUTTON
    override fun onBackPressed() {
        //LOAD THE BASIC APPLE PIE IMAGE IF WE DIDNT SET THE IMAGE
        if (image_uri == null) {
            image_uri =
                Uri.parse("android.resource://" + application.baseContext.getPackageName() + "/drawable/applepie")
        }
        var recipeNew = RecipeItem(
            id = null,
            name = recipeNameEt!!.getText().toString(),
            ingridients = saveIngridients(),
            directions = recipeDirectionsEt!!.getText().toString(),
            photoUri = image_uri.toString()
        )

        var recipeNewJson = recipeNew.toJson()

        //RETURNING THE CREATED RECIPE
        if (intent.getStringExtra("RECIPE") == "") {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Keep the new recipe?")
            builder.setMessage("If you press Abort you lose your recipe.")

            Log.d("App", "created recipe to json: " + recipeNewJson)

            builder.setPositiveButton("YES") { dialog, which ->
                val resultIntent = Intent()
                //RECIPE JSON GOES BACK AS RESULT TO THE MAIN ACTIVITY WHICH THEN INSERTS IT INTO THE RECYCLER VIEW
                resultIntent.putExtra("RESULT", recipeNewJson)
                setResult(
                    MainActivity.ResultCode.CREATED.toInt(MainActivity.ResultCode.CREATED),
                    resultIntent
                )
                super.onBackPressed()
            }
            //RESULT NOTHING IF WE CLICK ABORT
            builder.setNegativeButton("ABORT") { dialog, which ->
                Log.d("App", "click on no")
                val resultIntent = Intent()
                resultIntent.putExtra("RESULT", "")
                setResult(
                    MainActivity.ResultCode.ABORTED.toInt(MainActivity.ResultCode.ABORTED),
                    resultIntent
                )
                super.onBackPressed()
            }
            //SHOWING THE DIALOG
            builder.show()
        } else if (recipe.name == recipeNew.name &&
            recipe.ingridients == recipeNew.ingridients &&
            recipe.directions == recipeNew.directions && !pictureModified
        ) {
            //IF THE RECIPE DIDNT CHANGE
            val resultIntent = Intent()
            resultIntent.putExtra("RESULT", "")
            setResult(
                MainActivity.ResultCode.UNCHANGED.toInt(MainActivity.ResultCode.UNCHANGED),
                resultIntent
            )
            Log.d("App", " RecipeActivity onBackPress() hasnt changed ")
            super.onBackPressed()

        } else {
            //MODIFING THE RECIPE
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Sure?")
            builder.setMessage("Modify the recipe?")
            //LOADING THE MODIFIED RECIPE
            var recipeModified = RecipeItem(
                id = recipe.id,
                name = recipeNameEt!!.getText().toString(),
                ingridients = saveIngridients(),
                directions = recipeDirectionsEt!!.getText().toString(),
                photoUri = image_uri.toString()
            )
            //JSON FROM RECIPE ITEM
            var recipeModifiedJson = recipeModified.toJson()
            builder.setPositiveButton("YES") { dialog, which ->
                var resultIntent = Intent()
                //RESULT IS THE MODIFIED RECIPE JSON
                resultIntent.putExtra("RESULT", recipeModifiedJson)
                setResult(
                    MainActivity.ResultCode.MODIFIED.toInt(MainActivity.ResultCode.MODIFIED),
                    resultIntent
                )

                super.onBackPressed()
            }
            //IF WE CLICK NO THEN RETURN EMPTY RESULT
            builder.setNegativeButton("NO") { dialog, which ->
                val resultIntent = Intent()
                resultIntent.putExtra("RESULT", "")
                setResult(
                    MainActivity.ResultCode.UNCHANGED.toInt(MainActivity.ResultCode.UNCHANGED),
                    resultIntent
                )
                super.onBackPressed()
            }
            builder.show()
        }
    }
    //CHECK PERMISSIONS OF THE CAMERA
    private fun checkPersmission(): Boolean {
        Log.d("App", "Checking permissions")
        return (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED)
    }

    //REQUESTING PERMISSIONS FOR THE CAMERA USE
    private fun requestPermission() {
        Log.d("App", "Requesting permissions")
        ActivityCompat.requestPermissions(
            this, arrayOf(
                READ_EXTERNAL_STORAGE, CAMERA,
                WRITE_EXTERNAL_STORAGE
            ),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {

                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED
                ) {
                    //TAKE PICTURE IF ACCESS GRANTED
                    takePicture()
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
        //CREATE THE DIR IN INTERNAL STORAGE IF DOESNT EXISTS
        try {
            val dir = applicationContext.filesDir
            if (dir != null && !dir.exists()) {
                dir.mkdir()
            }
            //CREATE FILE WITH RANDOM UUID
            val file = File(dir, UUID.randomUUID().toString() + ".jpg")
            file.createNewFile()
            //CREATING URI FOR THE FILE WITH FILE PROVIDER
            image_uri = getUriForFile(this, "hu.bme.aut.recipeapp.fileprovider", file)

        } catch (e: IOException) {
            Log.d("App", e.toString())
        }
        //CALLING THE CAMERA INTENT WITH image_uri AS THE DESTINATION
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        this.startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                if (resultCode == Activity.RESULT_OK) {
                    //DELETE OLD PICTURE
                    if (newRecipe == false) {
                        if (recipe.photoUri != "null") {
                            var picToDelete = File(Uri.parse(recipe.photoUri).path)
                            picToDelete.delete()
                        }
                        //LOADING THE NEW RECIPE
                        recipe = RecipeItem(
                            id = recipe.id,
                            name = recipe.name,
                            ingridients = recipe.ingridients,
                            directions = recipe.directions,
                            photoUri = image_uri.toString()
                        )

                    }
                    //SETTING IMAGE OF THE RECIPE
                    Log.d("App", "setting image")
                    RecipeImage.setImageURI(image_uri)
                    pictureModified = true

                }
            }
            else -> Toast.makeText(this, "Unrecognized request code", Toast.LENGTH_SHORT).show()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    //IF WE PRESS THE PENCIL WE CAN MODIFY THE RECIPE
    fun toggleModifier() {
        modify = !modify
        modify()
    }
    //SETTING THE ELEMENTS TO MODIFIABLE
    fun modify() {
        if (modify) {
            modifyIngridients()
            RecipeName.isEnabled = true
            RecipeDirections.isEnabled = true
            addIngridient.visibility = View.VISIBLE
        } else {
            modifyIngridients()
            RecipeName.isEnabled = false
            RecipeDirections.isEnabled = false
            addIngridient.visibility = View.INVISIBLE
        }
    }
}