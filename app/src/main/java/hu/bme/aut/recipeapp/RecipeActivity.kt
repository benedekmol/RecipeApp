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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.FileProvider.getUriForFile
import com.google.gson.Gson
import hu.bme.aut.recipeapp.data.RecipeItem
import kotlinx.android.synthetic.main.activity_recipe.*
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.util.*

class RecipeActivity : AppCompatActivity() {

    val REQUEST_IMAGE_CAPTURE = 1

    private val PERMISSION_REQUEST_CODE = 101

    var image_uri : Uri? = null

    private var mCurrentPhotoPath: String? = null;


    private lateinit var recipe : RecipeItem

    //TODO CAMERA PICTURE TAKE AND STORE
    //modify
    private var recipeImage : ImageView? = null
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

        Toast.makeText(this, image_uri.toString(), Toast.LENGTH_LONG).show()

        recipeImage = findViewById<View>(R.id.RecipeImage) as ImageView
        recipeNameEt = findViewById<View>(R.id.RecipeName) as EditText
        recipeIngridientsEt = findViewById<View>(R.id.RecipeIngridients) as EditText
        recipeDirectionsEt = findViewById<View>(R.id.RecipeDirections) as EditText

        recipeImage!!.setOnClickListener(){
            Log.d("App", "clicked on image")
            Toast.makeText(this, image_uri.toString(), Toast.LENGTH_LONG).show()

            if (checkPersmission()) takePicture() else requestPermission()

        }

        //IF THE RECIPE IS BEEING CREATED:
        if (intent.getStringExtra("RECIPE") == ""){
            recipeNameEt!!.isEnabled = true
            recipeIngridientsEt!!.isEnabled = true
            recipeDirectionsEt!!.isEnabled = true

        } else {
            //OTHERWISE LOAD THE RECIPE
            var recipeJSON = JSONObject(intent.getStringExtra("RECIPE").toString())

            //TODO JSON WORKS!
            var gson = Gson()
            recipe = gson.fromJson(intent.getStringExtra("RECIPE"), RecipeItem::class.java)

            recipeNameEt!!.setText(recipe.name)
            recipeIngridientsEt!!.setText(recipe.ingridients)
            recipeDirectionsEt!!.setText(recipe.directions)

            recipeNameEt!!.isEnabled = false
            recipeIngridientsEt!!.isEnabled = false
            recipeDirectionsEt!!.isEnabled = false
        }
    }



    override fun onBackPressed() {
        var recipeNew = RecipeItem(id = null,
            name = recipeNameEt!!.getText().toString(),
            ingridients = recipeIngridientsEt!!.getText().toString(),
            directions = recipeDirectionsEt!!.getText().toString())

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
                recipe.directions == recipeNew.directions){
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
                ingridients = recipeIngridientsEt!!.getText().toString(),
                directions = recipeDirectionsEt!!.getText().toString())

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
        /*val intent: Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        //val file: File = createFile()
        /*val uri: Uri = FileProvider.getUriForFile(
            this,
            "com.example.android.fileprovider",
            file
        )
         */
        //intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
         val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEGphoto_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = absolutePath
        }
         */

        /*val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        if(!file.exists()){
            file.mkdir()
        }
        file = File(file, "kakimaki.jpg")

        image_uri = Uri.parse(file.absolutePath)*/
        /*
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        //cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)

         */


        /*
        ///---------------------WOrkS
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "apple pie")
        image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        //camera intent
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)
        */

        /*
        var path = filesDir
        Log.d("App", path.toString())

        var dir = File( filesDir, "Images")
        if (!dir.exists()){
            dir.mkdir()
        }
        try {
            var pic = File(dir, "haloka.jpg")
            var uri = FileProvider.getUriForFile(this, "hu.bme.aut.recipeapp.fileprovider", pic )
            Log.d("App", uri.toString())
        } catch (e: Exception){
            Log.d("App", e.toString())
        }
         */

        val dir = applicationContext.filesDir
        if (dir != null && !dir.exists()){
            dir.mkdir()
        }
        val file = File(dir, "test.jpg")

        image_uri = getUriForFile(applicationContext, "hu.bme.aut.recipeapp.fileprovider", file)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)



    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Toast.makeText(this, "returned with activity result", Toast.LENGTH_LONG).show()
        Toast.makeText(this, resultCode.toString(), Toast.LENGTH_LONG).show()
        Toast.makeText(this, data.toString(), Toast.LENGTH_LONG).show()


        when(requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                if (resultCode == Activity.RESULT_OK && data != null ){
                    //val auxFile = File(mCurrentPhotoPath)
                    Log.d("App", "setting image")
                    RecipeImage.setImageURI(image_uri)
                    Toast.makeText(this, image_uri.toString(), Toast.LENGTH_LONG).show()
                    //RecipeImage.setImageBitmap(data.extras!!.get("data") as Bitmap)

                    //Log.d("App", image_uri.toString())

                    /*var bitmap: Bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath)
                    RecipeImage.setImageBitmap(bitmap)

                     */
                }
            }
            else -> Toast.makeText(this, "Unrecognized request code", Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(IOException::class)
    private fun createFile(): File {
        // Create an image file name
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEGphoto_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = absolutePath
        }
    }

    fun toggleModifier(){
        recipeNameEt!!.isEnabled = !recipeNameEt!!.isEnabled
        recipeIngridientsEt!!.isEnabled = !recipeIngridientsEt!!.isEnabled
        recipeDirectionsEt!!.isEnabled = !recipeDirectionsEt!!.isEnabled
    }
}