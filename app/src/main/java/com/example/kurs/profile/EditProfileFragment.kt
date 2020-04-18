package com.example.kurs.profile

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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.valdesekamdem.library.mdtoast.MDToast
import kotlinx.android.synthetic.main.fragment_edit_account.*
import timber.log.Timber
import java.util.*
import kotlin.collections.HashMap

class EditProfileFragment : Fragment(), View.OnClickListener {

    private var imageUri: Uri? = null
    private var uploadTask: UploadTask? = null
    var loadUri = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val user = FirebaseAuth.getInstance().currentUser!!
        val reference = FirebaseDatabase.getInstance().getReference("Users").child(user.uid)

        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.d(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                val user2 = p0.getValue(User::class.java)
                if (user2 != null) {
                    try {
                        etName.setText(user2.username)
                        if (user2.imageUri != null) {
                            Glide.with(context!!).load(user2.imageUri).into(ivUserAva)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })

        ivUserAva.setOnClickListener(this)
        btnSave.setOnClickListener(this)
    }

    private fun upload() {
        val reference = FirebaseStorage.getInstance().getReference("Avatars")
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
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    val ref =
                        FirebaseDatabase.getInstance().getReference("Users").child(currentUser.uid)
                    val hashMap = HashMap<String, Any>()
                    hashMap["imageUri"] = loadUri
                    hashMap["username"] = etName.text.toString()
                    hashMap["lowerName"] = etName.text.toString().toLowerCase(Locale.getDefault())
                    ref.updateChildren(hashMap)

                    //TODO show and edit phones
//                    val ref2 = FirebaseDatabase.getInstance().getReference("Phones")

                    fragmentManager!!.popBackStack()
                }
            } else {
                MDToast.makeText(context, "Success", Toast.LENGTH_LONG, MDToast.TYPE_ERROR)
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

    private fun getExtension(uri: Uri): String {
        val resolver = context!!.contentResolver
        val typeMap = MimeTypeMap.getSingleton()
        return typeMap.getExtensionFromMimeType(resolver.getType(uri))!!
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2 && data != null && data.data != null) {
            imageUri = data.data!!

            //TODO load image
            Glide.with(context!!)
                .load(imageUri)
                .into(ivUserAva)

        }
    }

    override fun onClick(v: View?) {
        when (v) {
            ivUserAva -> {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(intent, 2)
            }

            btnSave -> {
                upload()
            }
        }
    }
}