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
import kotlinx.android.synthetic.main.layout_post.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*
import kotlin.collections.HashMap

class PostFragment : Fragment(), View.OnClickListener {

    private var imageUri: Uri? = null
    private var uploadTask: UploadTask? = null
    var loadUri = ""
    var canAdd = false

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
        if (imageUri != null) {
            doAsync {
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
                        canAdd = true
                    } else {
                        uiThread {
                            MDToast.makeText(
                                context,
                                "Success",
                                Toast.LENGTH_LONG,
                                MDToast.TYPE_ERROR
                            )
                                .show()
                        }

                    }
                }.addOnFailureListener { p0 ->
                    loadUri = ""
                    uiThread {
                        MDToast.makeText(
                            context,
                            p0.localizedMessage,
                            Toast.LENGTH_LONG,
                            MDToast.TYPE_ERROR
                        ).show()
                    }
                }
            }
        }


        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            if (imageUri != null && canAdd || imageUri == null) {
                val ref = FirebaseDatabase.getInstance().getReference("Wall")
                val ids = ArrayList<String>()
                val hashMap = HashMap<String, Any>()
                if (imageUri != null) {
                    hashMap["uri"] = loadUri
                    hashMap["id"] = loadUri
                }
                hashMap["text"] = postText.text.trim().toString()
                hashMap["isLiked"] = false
                hashMap["date"] = Date()
                hashMap["sender"] = FirebaseAuth.getInstance().currentUser!!.uid
                hashMap["users"] = ids
                ref.push().setValue(hashMap)
                fragmentManager!!.popBackStack()
            }
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
                loadImage.visibility = View.GONE
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

            postAdd -> {
                if (postText.text.trim().toString().isNotEmpty()) {
                    upload()
                } else {
                    MDToast.makeText(
                        context,
                        resources.getString(R.string.empty),
                        Toast.LENGTH_LONG,
                        MDToast.TYPE_ERROR
                    ).show()
                }
            }
        }
    }
}