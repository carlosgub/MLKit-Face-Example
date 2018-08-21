package com.github.carlosgub.mlkitfirebase.presentation.views

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.View
import com.github.carlosgub.mlkitfirebase.R
import com.github.carlosgub.mlkitfirebase.presentation.adapters.MenuRecyclerAdapter
import kotlinx.android.synthetic.main.activity_menu.*
import kotlinx.android.synthetic.main.item_menu.view.*

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        val opciones = arrayOf(getString(R.string.take_photo), getString(R.string.elegir_foto_de_la_galeria))
        val adapter = MenuRecyclerAdapter(opciones, View.OnClickListener {
            val intent = if(it.tvTextoMenu.text.toString() == opciones[0]){
                Intent(this,MainActivity::class.java)
            }else{
                Intent(this,GaleriaActivity::class.java)
            }
            startActivity(intent)
        })
        rvMenu.layoutManager = StaggeredGridLayoutManager(1, 1)
        rvMenu.adapter = adapter
        adapter.notifyDataSetChanged()
    }


}
