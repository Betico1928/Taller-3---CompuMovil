package javeriana.edu.co.taller3_compumovil.adapters


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso

import javeriana.edu.co.taller3_compumovil.R

class CustomListAdapter(private val context: Context, private val itemList: List<Item>) : BaseAdapter() {

    private class ViewHolder(view: View) {

        val imageView: ImageView = view.findViewById(R.id.imageView)
        val textView: TextView = view.findViewById(R.id.textView)
        val button: Button = view.findViewById(R.id.button)


    }

    override fun getCount(): Int {
        return itemList.size
    }

    override fun getItem(position: Int): Any {
        return itemList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.custom_list_item, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val item = getItem(position) as Item

        Picasso.get()
            .load(item.imageResource)
            .into(viewHolder.imageView)

        viewHolder.imageView.layoutParams.width = 100
        viewHolder.imageView.layoutParams.height = 150

        viewHolder.textView.text = item.text
        viewHolder.button.setOnClickListener(item.buttonClickListener)



        viewHolder.imageView.requestLayout()




        return view
    }
}