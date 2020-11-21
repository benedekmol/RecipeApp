package hu.bme.aut.recipeapp.adapter
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.recipeapp.R
import hu.bme.aut.recipeapp.data.RecipeItem
import hu.bme.aut.recipeapp.data.RecipeItemDao
import kotlinx.android.synthetic.main.item_recipe_list.view.*


class RecipeAdapter(private val listener: RecipeItemClickListener):
        RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    private val items = mutableListOf<RecipeItem>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val itemView : View = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_recipe_list,parent,false)

        return RecipeViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val item = items[position]
        holder.nameTextView.text = item.name
        var photoUri = if (item.photoUri != null) Uri.parse(item.photoUri) else null
        holder.recipeIcon.setImageURI( photoUri )
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
    inner class RecipeViewHolder(itemView : View): RecyclerView.ViewHolder(itemView) {
        val nameTextView : TextView
        val removeButton : ImageButton
        val recipeLayout : LinearLayout
        val recipeIcon : ImageView

        var item : RecipeItem? = null

        init{
            nameTextView = itemView.findViewById(R.id.RecipeItemNameTextView)
            removeButton = itemView.findViewById(R.id.RecipeItemRemoveButton)
            recipeLayout = itemView.findViewById(R.id.RecipeLayout)
            recipeIcon = itemView.findViewById(R.id.RecipeImageIcon)

            //TODO RemoveButton
            removeButton.setOnClickListener() {
                Log.d("RecipeAdapter", "Recipe deleting from adapter")
                removeItem(item!!)
                listener.onItemRemoved(item!!)
                notifyDataSetChanged()
            }

            recipeLayout.setOnClickListener(){
                listener.onItemSelected(item!!)
            }

        }

    }

    fun removeItem(item: RecipeItem){
        items.remove(item)
    }
    fun addItem(item: RecipeItem){
        items.add(item)
        notifyItemInserted(items.size - 1)
    }
    fun update(recipeItems : List<RecipeItem>){
        items.clear()
        items.addAll(recipeItems)
        notifyDataSetChanged()
    }

}