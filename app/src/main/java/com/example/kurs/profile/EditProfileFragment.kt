package com.example.kurs.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.EditText
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
import kotlinx.android.synthetic.main.layout_edit_account.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class EditProfileFragment : Fragment(), View.OnClickListener {

    private var imageUri: Uri? = null
    private var uploadTask: UploadTask? = null
    var loadUri = ""
    val phoneList = ArrayList<Phone>()
    var phoneViews = ArrayList<View>()
    var canAdd = false

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
        val referencePhones = FirebaseDatabase.getInstance().getReference("Phones")

        reference.addListenerForSingleValueEvent(object : ValueEventListener {
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
                        }else{
                            Glide.with(context!!).load(R.drawable.ava).into(ivUserAva)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })
        referencePhones.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Timber.d(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                val curUser = FirebaseAuth.getInstance().currentUser
                if (p0.children.count() > 0) {
                    p0.children.forEach {
                        val phone = it.getValue(Phone::class.java)
                        if (!phoneList.contains(phone) && phone != null && curUser != null && phone.owner == curUser.uid) {
                            phoneList.add(phone)
                            addPhoneView(phone)
                        }
                    }
                }
            }
        })

        ivUserAva.setOnClickListener(this)
        btnSave.setOnClickListener(this)
        btnAddPhone.setOnClickListener(this)
    }

    private fun addPhoneView(phone: Phone?) {
        if (phoneViews.size < 4) {
            try {
                val view = LayoutInflater.from(context).inflate(R.layout.item_phone, null, false)
                view.findViewById<Button>(R.id.btnDeletePhone).setOnClickListener {
                    ltPhones.removeView(view)
                    phoneViews.remove(view)
                    btnAddPhone.visibility = View.VISIBLE
                }
                phoneViews.add(view)
                if (phoneViews.size == 4) btnAddPhone.visibility = View.GONE
                if (phone != null) {
                    view.findViewById<EditText>(R.id.etPhoneNumber).setText(phone.phone)
                }
                ltPhones.addView(view)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun upload() {
        if (imageUri != null) {
            doAsync {
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
                val ref =
                    FirebaseDatabase.getInstance().getReference("Users").child(currentUser.uid)
                val hashMap = HashMap<String, Any>()
                if(imageUri!=null){
                    hashMap["imageUri"] = loadUri
                }
                hashMap["username"] = etName.text.toString()
                hashMap["lowerName"] = etName.text.toString().toLowerCase(Locale.getDefault())
                ref.updateChildren(hashMap)

                val referencePhones = FirebaseDatabase.getInstance().getReference("Phones")

                referencePhones.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        Timber.d(p0.message)
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        val currentList = ArrayList<Phone>()
                        val curUser = FirebaseAuth.getInstance().currentUser

                        if (p0.children.count() > 0) {
                            p0.children.forEach {
                                val phone = it.getValue(Phone::class.java)
                                if (!currentList.contains(phone) && phone != null && curUser != null && phone.owner == curUser.uid) {
                                    currentList.add(phone)
                                }
                            }

                            p0.children.forEach {
                                referencePhones.child(it.key.toString()).setValue(null)
                            }
                        }
                    }
                })


                phoneViews.forEach {
                    val hashMap2 = HashMap<String, Any>()
                    val phone = it.findViewById<EditText>(R.id.etPhoneNumber)
                    if(phone.text.toString().isNotEmpty()) {
                        hashMap2["phone"] = phone.text.toString()
                        hashMap2["owner"] = currentUser.uid
                        referencePhones.push().setValue(hashMap2)
                    }
                }


                fragmentManager!!.popBackStack()
            }
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

            btnAddPhone -> {
                addPhoneView(null)
            }
        }
    }
}