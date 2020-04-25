package com.example.kurs.wall

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.kurs.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.valdesekamdem.library.mdtoast.MDToast
import kotlinx.android.synthetic.main.fragment_add_post.*
import java.util.*
import kotlin.collections.HashMap

class PostFragment : Fragment(), View.OnClickListener {

    private var imageUri: Uri? = null
    private var uploadTask:UploadTask?=null
    var loadUri = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        postImage.setOnClickListener(this)
        postAdd.setOnClickListener(this)
    }

    private fun getExtension(uri: Uri): String {
        val resolver = context!!.contentResolver
        val typeMap = MimeTypeMap.getSingleton()
        return typeMap.getExtensionFromMimeType(resolver.getType(uri))!!
    }

    private fun upload() {
            val reference = FirebaseStorage.getInstance().getReference("PostImages")
                .child(System.currentTimeMillis().toString() + "." + getExtension(imageUri!!))

            uploadTask = reference.putFile(imageUri!!)
            uploadTask!!.continueWithTask { p0 ->
                if (!p0.isSuccessful) {
                    throw p0.exception!!
                }
                reference.downloadUrl
            }.addOnCompleteListener { p0 ->
                if (p0.isSuccessful) {
                    val dwUri = p0.result.toString()
                    loadUri = dwUri
                    val ref = FirebaseDatabase.getInstance().getReference("Wall")
                    val ids = ArrayList<String>()
                    val comments = ArrayList<Comment>()
                    val hashMap = HashMap<String, Any>()
                    hashMap["uri"] = loadUri
                    hashMap["text"] = postText.text.trim().toString()
                    hashMap["isLiked"] = false
                    hashMap["likes"] = 0
                    hashMap["date"] = Date()
                    hashMap["sender"] = FirebaseAuth.getInstance().currentUser!!.uid
                    hashMap["id"] = loadUri
                    hashMap["users"] = ids
                    hashMap["comments"] = comments
                    ref.push().setValue(hashMap)
                    fragmentManager!!.popBackStack()

                } else {
                    MDToast.makeText(context, "BAD", Toast.LENGTH_LONG, MDToast.TYPE_ERROR)
                        .show()

                }
            }.addOnFailureListener { p0 ->
                MDToast.makeText(
                    context,
                    p0.localizedMessage,
                    Toast.LENGTH_LONG,
                    MDToast.TYPE_ERROR
                ).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && data != null && data.data != null) {
            imageUri = data.data!!
            context?.let {
                Glide.with(it)
                    .load(imageUri)
                    .into(postImage)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            postImage -> {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(intent, 1)
            }

            postAdd->{
                upload()
            }
        }
    }
}