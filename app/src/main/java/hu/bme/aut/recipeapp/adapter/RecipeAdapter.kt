package hu.bme.aut.recipeapp.adapter

import android.net.Uri
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.recipeapp.R
import hu.bme.aut.recipeapp.data.RecipeItem


class RecipeAdapter(private val listener: RecipeItemClickListener) :
    RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    private val items = mutableListOf<RecipeItem>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val itemView: View = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_recipe_list, parent, false)

        return RecipeViewHolder(itemView)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val item = items[position]
        holder.nameTextView.text = item.name
        //holder.position = position
        var photoUri: Uri? = null
        if (item.photoUri != null) {
            photoUri = Uri.parse(item.photoUri)
            //var bitmap = getThumbnail
        }
        holder.recipeIcon.setImageURI(photoUri)
        holder.item = item
    }


    interface RecipeItemClickListener {
        fun onItemChanged(item: RecipeItem)
        fun onItemRemoved(item: RecipeItem)
        fun onItemSelected(item: RecipeItem)
    }

    override fun getItemCount(): Int {
        return items.size
    }


    //View Holder for the recipes
    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView
        val removeButton: ImageButton
        val recipeLayout: LinearLayout
        val recipeIcon: ImageView
        var item: RecipeItem? = null
        var moveAnim = AnimationUtils.loadAnimation(itemView.context, R.anim.move_out)

        init {
            nameTextView = itemView.findViewById(R.id.RecipeItemNameTextView)
            removeButton = itemView.findViewById(R.id.RecipeItemRemoveButton)
            recipeLayout = itemView.findViewById(R.id.RecipeLayout)
            recipeIcon = itemView.findViewById(R.id.RecipeImageIcon)

            removeButton.setOnClickListener() {
                Log.d("App", "Recipe deleting from adapter")

                recipeLayout.startAnimation(moveAnim)


                Handler().postDelayed(Runnable {
                    var pos = items.indexOf(item)
                    removeItem(item!!)
                    listener.onItemRemoved(item!!)
                    //notifyDataSetChanged() didn't work well

                    notifyItemRemoved(pos)
                }, 500)

                //var pos = items.indexOf(item)
                //removeItem(item!!)
                //listener.onItemRemoved(item!!)
                ////notifyDataSetChanged() didn't work well
                //Thread.sleep(2000)
//
                //notifyItemRemoved(pos)
            }

            recipeLayout.setOnClickListener() {
                listener.onItemSelected(item!!)
            }
        }
    }

    fun removeItem(item: RecipeItem) {
        items.remove(item)
    }

    fun addItem(item: RecipeItem) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun update(recipeItems: List<RecipeItem>) {
        items.clear()
        items.addAll(recipeItems)
        notifyDataSetChanged()
    }

}