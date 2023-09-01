package com.example.notes

import android.os.Bundle
import android.provider.ContactsContract
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.SearchView.OnQueryTextListener
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.notes.adapter.NotesAdapter
import com.example.notes.database.NotesDatabase
import com.example.notes.entities.Notes
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList


class Home : BaseFragment() {
   var arrNotes = ArrayList<Notes>()
    var notesAdapter: NotesAdapter = NotesAdapter()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    companion object {


        @JvmStatic
        fun newInstance() =
            Home().apply {
                arguments = Bundle().apply {

                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
       recycler_view.setHasFixedSize(true)
        recycler_view.layoutManager=StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)

        launch {
            context?.let {
                var notes = NotesDatabase.getDatabase(it).noteDao().getAllNotes()
                notesAdapter!!.setData(notes)
                arrNotes = notes as ArrayList<Notes>
                recycler_view.adapter = notesAdapter
            }
        }
        notesAdapter.OnItemClickListener(onClicked)

        fabBtnCreateNote.setOnClickListener{
         replacement(CreateNote.newInstance(),false)
        }
        search_view.setOnQueryTextListener(object : OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
               var temp=ArrayList<Notes>()
                for (arr in arrNotes)
                {
                   if (arr.title!!.toLowerCase(Locale.getDefault()).contains(p0.toString()))
                       temp.add(arr)
                }
                notesAdapter.setData(temp)
                notesAdapter.notifyDataSetChanged()
                return true
            }

        })
    }
  private val onClicked = object :NotesAdapter.OnItemClickListener{
    override fun onClicked(noteId: Int) {
    var fragment:Fragment
    var bundle=Bundle()
    bundle.putInt("noteId",noteId)
        fragment=CreateNote.newInstance()
        fragment.arguments=bundle
        replacement(fragment,false)

    }

}


    fun replacement(fragment:Fragment,istransition:Boolean)
    {
        val fragmentTransition=requireActivity().supportFragmentManager.beginTransaction()
        if(istransition)
        {
            fragmentTransition.setCustomAnimations(android.R.anim.slide_out_right,android.R.anim.slide_in_left)
        }
        fragmentTransition.replace(R.id.frame_layout,fragment).addToBackStack(fragment.javaClass.simpleName).commit()
    }
}