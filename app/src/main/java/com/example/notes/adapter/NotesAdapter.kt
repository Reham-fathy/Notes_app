package com.example.notes.adapter

import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.notes.R
import com.example.notes.entities.Notes
import kotlinx.android.synthetic.main.item_rv_notes.view.*

class NotesAdapter( ):RecyclerView.Adapter<NotesAdapter.NotesViewHolder>() {
    var listener:OnItemClickListener? = null
   var arrList =ArrayList<Notes>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
      return NotesViewHolder(
          LayoutInflater.from(parent.context).inflate(R.layout.item_rv_notes,parent,false))
    }
   fun setData(arrNotesList: List<Notes>){
        arrList = arrNotesList as ArrayList<Notes>
    }
    fun OnItemClickListener(listener1:OnItemClickListener)
    {
        listener=listener1
    }
    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        holder.itemView.tvTitle.text=arrList[position].title
        holder.itemView.tvDesc.text=arrList[position].noteText
        holder.itemView.tvDateTime.text=arrList[position].dateTime
        if (arrList[position].color != null){
            holder.itemView.cardview.setCardBackgroundColor(Color.parseColor(arrList[position].color))
        }
//        else{
//            holder.itemView.cardview.setCardBackgroundColor(Color.parseColor(R.color.ColorLightBlack.toString()))
//        }
        if (arrList[position].imgPath != null){
            holder.itemView.imgNote.setImageBitmap(BitmapFactory.decodeFile(arrList[position].imgPath))
            holder.itemView.imgNote.visibility = View.VISIBLE
        }else{
            holder.itemView.imgNote.visibility = View.GONE
        }
    holder.itemView.cardview.setOnClickListener{
    listener!!.onClicked(arrList[position].id!!)
}
    }

    override fun getItemCount(): Int {
        return arrList.size
    }
    class NotesViewHolder(view:View):RecyclerView.ViewHolder(view){

    }

    interface OnItemClickListener
    {
        fun onClicked(noteId: Int )
    }
}