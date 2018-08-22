package com.github.carlosgub.mlkitfirebase.presentation.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.carlosgub.mlkitfirebase.R
import kotlinx.android.synthetic.main.item_menu.view.*

class MenuRecyclerAdapter(private val menuList: Array<String>, private var onClickListener: View.OnClickListener):RecyclerView.Adapter<MenuRecyclerAdapter.ViewHolder>() {

    override fun getItemCount():Int{
        return menuList.size
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_menu, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.itemView) {
            val opcion = menuList[position]
            holder.itemView.tvTextoMenu.text = opcion
            holder.itemView.mainLayout.setOnClickListener(onClickListener)
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

}