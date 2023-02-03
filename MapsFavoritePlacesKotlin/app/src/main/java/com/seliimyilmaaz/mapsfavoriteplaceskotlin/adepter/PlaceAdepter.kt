package com.seliimyilmaaz.mapsfavoriteplaceskotlin.adepter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.Placeholder
import androidx.recyclerview.widget.RecyclerView
import com.seliimyilmaaz.mapsfavoriteplaceskotlin.databinding.RecyclerRowBinding
import com.seliimyilmaaz.mapsfavoriteplaceskotlin.model.Place
import com.seliimyilmaaz.mapsfavoriteplaceskotlin.view.MapsActivity

class PlaceAdepter(val listOfPlace : List<Place>) : RecyclerView.Adapter<PlaceAdepter.ViewHolder>() {

    class ViewHolder(val recyclerRowBinding: RecyclerRowBinding) : RecyclerView.ViewHolder(recyclerRowBinding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return ViewHolder(recyclerRowBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.recyclerRowBinding.recyclerView.text = listOfPlace[position].name

        holder.itemView.setOnClickListener(){
            val intent = Intent(holder.itemView.context,MapsActivity::class.java)
            intent.putExtra("selectedPlace",listOfPlace[position])
            intent.putExtra("info","old")
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return listOfPlace.size
    }
}