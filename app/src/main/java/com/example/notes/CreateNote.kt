package com.example.notes

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.PatternMatcher
import android.provider.MediaStore
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.notes.database.NotesDatabase
import com.example.notes.entities.Notes
import com.example.notes.util.NoteBottomSheetFragment
import kotlinx.android.synthetic.main.fragment_create_note.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.item_rv_notes.view.*
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern


class CreateNote : BaseFragment(),EasyPermissions.PermissionCallbacks,EasyPermissions.RationaleCallbacks {
    var selectedColor = "#171C26"
  var currentDate:String? = null
    private var READ_STORAGE_PERM = 123
    private var REQUEST_CODE_IMAGE = 456
    private var selectedImagePath = ""
    private var noteId = -1
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_note, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        noteId=requireArguments().getInt("noteId",-1)
    }
    companion object {

        fun newInstance() =
            CreateNote().apply {
                arguments = Bundle().apply {

                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (noteId != -1){

            launch {
                context?.let {
                    var notes = NotesDatabase.getDatabase(it).noteDao().getSpecificNote(noteId)
                    colorView.setBackgroundColor(Color.parseColor(notes.color))
                    ed_NoteTitle.setText(notes.title)
                    edNoteSubTitle.setText(notes.subTitle)
                    etNoteDesc.setText(notes.noteText)
                    if (notes.imgPath != ""){
                        selectedImagePath = notes.imgPath!!
                        imgNote.setImageBitmap(BitmapFactory.decodeFile(notes.imgPath))
                        layoutImage.visibility = View.VISIBLE
                        imgNote.visibility = View.VISIBLE
                        imgDelete.visibility = View.VISIBLE
                    }else{
                        layoutImage.visibility = View.GONE
                        imgNote.visibility = View.GONE
                        imgDelete.visibility = View.GONE
                    }

                    if (notes.webLink != ""){
                        tvWebLink.text = notes.webLink
                      //  layoutWebUrl.visibility = View.VISIBLE
                    //    etWebLink.setText(notes.webLink)
                        imgUrlDelete.visibility = View.VISIBLE
                    }else{
                        imgUrlDelete.visibility = View.GONE
                        layoutWebUrl.visibility = View.GONE
                    }
                }
            }
        }

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            BroadcastReceiver, IntentFilter("bottom_sheet_action")
        )
        val sdf=SimpleDateFormat("dd/M/yyyy hh:mm:ss ")
        currentDate=sdf.format(Date())
        colorView.setBackgroundColor(Color.parseColor(selectedColor))
        tvDateTime.text=currentDate
        imgDone.setOnClickListener{
            if (noteId!=-1)
            {
                updateNote()
            }
            else{
                saveNote()
            }

        }
        imgUrlDelete.setOnClickListener {
            tvWebLink.visibility = View.GONE
            imgUrlDelete.visibility = View.GONE
            layoutWebUrl.visibility = View.GONE
        }

        img_back.setOnClickListener{
           requireActivity().supportFragmentManager.popBackStack()
        }
        imgMore.setOnClickListener{
            var noteBottomSheetFragment = NoteBottomSheetFragment.newInstance(noteId)
            noteBottomSheetFragment.show(requireActivity().supportFragmentManager,"Note Bottom Sheet Fragment")
        }
        imgDelete.setOnClickListener{
            selectedImagePath=" "
            layoutImage.visibility=View.GONE
        }
    }
    private fun updateNote() {
        launch {

            context?.let {
                var notes = NotesDatabase.getDatabase(it).noteDao().getSpecificNote(noteId)
                NotesDatabase.getDatabase(it).noteDao().updateNote(notes)


                notes.title = ed_NoteTitle.text.toString()
                notes.subTitle = edNoteSubTitle.text.toString()
                notes.noteText = etNoteDesc.text.toString()
                notes.color = selectedColor
                notes.imgPath = selectedImagePath
                notes.dateTime = currentDate.toString()
                NotesDatabase.getDatabase(it).noteDao().updateNote(notes)
                ed_NoteTitle.setText("")
                edNoteSubTitle.setText("")
                etNoteDesc.setText("")
                layoutImage.visibility = View.GONE
                imgNote.visibility = View.GONE
                tvWebLink.visibility = View.GONE
                requireActivity().supportFragmentManager.popBackStack()
            }

        }
    }
    private fun saveNote(){
        if(ed_NoteTitle.text.isNullOrEmpty()){
     Toast.makeText(context,"Title is Required ",Toast.LENGTH_SHORT).show()
        }
        else if(edNoteSubTitle.text.isNullOrEmpty()){
            Toast.makeText(context,"Note Sub Title is Required ",Toast.LENGTH_SHORT).show()
        }
     else if(etNoteDesc.text.isNullOrEmpty()){
            Toast.makeText(context,"Note Description is Required ",Toast.LENGTH_SHORT).show()
        }
      else {
            launch {
                var notes = Notes()
                notes.title = ed_NoteTitle.text.toString()
                notes.subTitle = edNoteSubTitle.text.toString()
                notes.noteText = etNoteDesc.text.toString()
                notes.color = selectedColor
                notes.imgPath = selectedImagePath
                notes.dateTime = currentDate.toString()
                context?.let {
                    NotesDatabase.getDatabase(it).noteDao().insertNotes(notes)
                    ed_NoteTitle.setText("")
                    edNoteSubTitle.setText("")
                    etNoteDesc.setText("")
                    imgNote.visibility = View.GONE
                    requireActivity().supportFragmentManager.popBackStack()
                }

            }
        }
    }


   private fun deleteNote()
   {
       launch {
           context?.let {
               NotesDatabase.getDatabase(it).noteDao().deleteSpecificNote(noteId)
               requireActivity().supportFragmentManager.popBackStack()
           }
       }
   }
    private val  BroadcastReceiver:BroadcastReceiver=object :BroadcastReceiver(){

        override fun onReceive(p0: Context?, p1: Intent?) {
            var actionColor= p1!!.getStringExtra("actionColor")
            when(actionColor!!)
            {
              "Blue"->{
                   selectedColor=p1.getStringExtra("selectedColor")!!
                  colorView.setBackgroundColor(Color.parseColor(selectedColor))
              }
                "Yellow"->{
                     selectedColor=p1.getStringExtra("selectedColor")!!
                    colorView.setBackgroundColor(Color.parseColor(selectedColor))
                }
                "Orange"->{
                     selectedColor=p1.getStringExtra("selectedColor")!!
                    colorView.setBackgroundColor(Color.parseColor(selectedColor))
                }
                "Green"->{
                     selectedColor=p1.getStringExtra("selectedColor")!!
                    colorView.setBackgroundColor(Color.parseColor(selectedColor))
                }
                "Purple"->{
                     selectedColor=p1.getStringExtra("selectedColor")!!
                    colorView.setBackgroundColor(Color.parseColor(selectedColor))
                }
                "Black"->{
                     selectedColor=p1.getStringExtra("selectedColor")!!
                    colorView.setBackgroundColor(Color.parseColor(selectedColor))
                }
                "Image"->{
                  readStorageTask()
                }
                "deleteNote"->{
                        deleteNote()
                }
             else->{
                  selectedColor=p1.getStringExtra("selectedColor")!!
                 colorView.setBackgroundColor(Color.parseColor(selectedColor))
             }
            }
        }
    }
    private fun hasReadStoragePer():Boolean
    {
        return EasyPermissions.hasPermissions(requireContext(),Manifest.permission.READ_EXTERNAL_STORAGE)

    }
    private fun readStorageTask()
    {
        if (hasReadStoragePer()){
          pickImageFromGallery()
        }
        else
        {
       EasyPermissions.requestPermissions(
           requireActivity(),
           "",
           READ_STORAGE_PERM,
           Manifest.permission.READ_EXTERNAL_STORAGE
       )
        }
    }
    private fun pickImageFromGallery()
    {
        var intent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if(intent.resolveActivity(requireActivity().packageManager)!=null)
        {
            startActivityForResult(intent,REQUEST_CODE_IMAGE)
        }
    }
    private fun getPathFromUri(contentUri: Uri): String?
    {
        var filePath:String? = null
        var cursor = requireActivity().contentResolver.query(contentUri,null,null,null,null)
        if (cursor == null){
            filePath = contentUri.path
        }else{
            cursor.moveToFirst()
            var index = cursor.getColumnIndex("_data")
            filePath = cursor.getString(index)
            cursor.close()
        }
        return filePath
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMAGE && resultCode == RESULT_OK){
            if (data != null){
                var selectedImageUrl = data.data
                if (selectedImageUrl != null){
                    try {
                        var inputStream = requireActivity().contentResolver.openInputStream(selectedImageUrl)
                        var bitmap = BitmapFactory.decodeStream(inputStream)
                        imgNote.setImageBitmap(bitmap)
                        imgNote.visibility = View.VISIBLE
                        layoutImage.visibility = View.VISIBLE

                       selectedImagePath = getPathFromUri(selectedImageUrl)!!
                    }catch (e:Exception){
                        Toast.makeText(requireContext(),e.message,Toast.LENGTH_SHORT).show()
                    }

                }
            }
        }


    }
    override fun onDestroy() {

        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(BroadcastReceiver)
        super.onDestroy()


    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
       if (EasyPermissions.somePermissionPermanentlyDenied(requireActivity(),perms))
       {
         AppSettingsDialog.Builder(requireActivity()).build().show()
       }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this)
    }

    override fun onRationaleAccepted(requestCode: Int) {

    }

    override fun onRationaleDenied(requestCode: Int) {

    }

}