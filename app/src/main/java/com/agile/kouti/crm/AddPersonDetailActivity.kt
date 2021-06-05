package com.agile.kouti.crm

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.agile.kouti.db.FirebaseDbClient
import com.agile.kouti.db.crm.Group
import com.agile.kouti.db.crm.Person
import com.agile.kouti.utils.Const
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_add_crm.*
import kotlinx.android.synthetic.main.activity_add_person_detail.*
import kotlinx.android.synthetic.main.activity_add_person_detail.view.*
import kotlinx.android.synthetic.main.toolbar.*
import timber.log.Timber
import java.util.*

class AddPersonDetailActivity : KoutiBaseActivity(), View.OnClickListener,
    KoutiBaseActivity.OnDateSelectedListener, KoutiBaseActivity.OnImageSelectedListener {

    private var isEdit = false
    private var imagePath: Uri? = null
    private var imageUrl: String? = ""

    private var personName: String? = ""


    var personId: String = "" 
    var personObj: Person? = null

    private var firebaseStore: FirebaseStorage? = null
    private var storageReference: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_person_detail)
        initData()

        firebaseStore = FirebaseStorage.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
    }

    private fun initData() {
        ivBack.setOnClickListener(this)
        etBirthday.setOnClickListener(this)
        ivReferenceImage.setOnClickListener(this)
        btnAddPerson.setOnClickListener(this)

        tvToolbarTitle.text = getString(R.string.toolbar_title_person_detail)
        if (intent != null) {
            isEdit = intent.getBooleanExtra(Const.KEYS.IS_EDIT, false)

            if (isEdit) {
                personId = intent.getStringExtra(Const.KEYS.PERSON_ID)
                tvToolbarSubTitle.text = resources.getString(R.string.txt_edit)
                tvToolbarSubTitle.visibility = View.VISIBLE

                if(!TextUtils.isEmpty(personId)){
                    getPersonDataFromId()
                }
            }
            else {
                personName = intent.getStringExtra(Const.KEYS.PERSON_NAME)
                etPersonName.setText(personName)
            }
        }

    }

    /* Get Person Data from Person Id*/
    private fun getPersonDataFromId() {
        showProgressDialog()
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.person.child(personId).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    var personObj: Person? = dataSnapshot.getValue(Person::class.java)
                    if(personObj!=null){
                        etPersonName.setText(personObj.name)
                        etRemark.setText(personObj.remark)
                        etBirthday.setText(personObj.birthday)
                        imageUrl = personObj.profile_pic

                        Glide.with(ivReferenceImage!!)
                            .load(imageUrl)
                            .centerCrop()
                            .placeholder(R.drawable.upload_document)
                            .into(ivReferenceImage)
                        hideProgressDialog()
                    }
                }else{
                    hideProgressDialog()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivBack -> {
                hideKeyBoard(v)
                finish()
            }
            R.id.etBirthday -> {
                hideKeyBoard(v)
                setDateListener(this)
                showDatePickerDialog(false, Const.KEYS.DATE_DOB)
            }
            R.id.ivReferenceImage -> {
                hideKeyBoard(v)
                setListener(this)
                showImagePickerDialog(Const.KEYS.REFERENCE)
            }
            R.id.btnAddPerson -> {
                hideKeyBoard(v)
                if (checkValidation()) {
                    if (isEdit) {
                        if (imagePath == null) {
                            showProgressDialog()
                            addValueInPersonTable()
                        } else {
                            uploadImage()
                        }
                    } else {
                        personId =
                            FirebaseDatabase.getInstance().getReference(Const.TableName.GROUP)
                                .push().key.toString()
                        uploadImage()
                    }

                }
            }
        }
    }

    /* Upload Image */
    private fun uploadImage() {
        showProgressDialog()
        if (imagePath != null) {
            val ref = storageReference?.child(Const.ImageType.PERSON+"/"+ UUID.randomUUID().toString())
            val uploadTask = ref?.putFile(imagePath!!)
            val urlTask =
                uploadTask?.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    return@Continuation ref.downloadUrl
                })?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        imageUrl = task.result.toString()
                        Timber.e("imageUrl -- " + imageUrl)
                        addValueInPersonTable()
                    } else {
                        hideProgressDialog()
                        showError(getString(R.string.error_image_upload_failed))
                    }
                }?.addOnFailureListener {
                    hideProgressDialog()
                    showError(getString(R.string.error_failed_try_again))
                }
        } else {
            hideProgressDialog()
            Toast.makeText(this, getString(R.string.error_upload_image), Toast.LENGTH_SHORT).show()
        }

    }

    /* Add value in DB */
    private fun addValueInPersonTable() {
        personObj = Person(
            etBirthday.text.toString().trim(),
            personId.toString(),
            etPersonName.text.toString().trim(),
            imageUrl,
            etRemark.text.toString().trim()
        )

        var firebaseDbClient = FirebaseDbClient()
        if (personObj != null) {
            firebaseDbClient.person.child(personId).setValue(personObj)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        hideProgressDialog()
                        showAlert("Success")
                        finish()
                    }
                }
        }
    }

    private fun checkValidation(): Boolean {

        if (TextUtils.isEmpty(etPersonName.text.toString().trim())) {
            showError(getString(R.string.error_enter_name))
            return false
        }

        if (TextUtils.isEmpty(etRemark.text.toString().trim())) {
            showError(getString(R.string.error_enter_remark))
            return false
        }

        if (TextUtils.isEmpty(etBirthday.text.toString().trim())) {
            showError(getString(R.string.error_enter_birthday))
            return false
        }

        if(!isEdit) {
            if (imagePath == null) {
                showError("Please upload reference image.")
                return false
            }
        }

        if (!isNetworkConnected) {
            showError(resources.getString(R.string.internet_error))
            return false
        }

        return true

    }

    override fun onDateSelected(finalDate: String?, dateType: String?) {
        etBirthday.setText(finalDate)
    }

    override fun onGalleryImage(selectedGalleryImage: Uri?, imageType: String?) {
        imagePath = selectedGalleryImage
        displayImage(imagePath!!, ivReferenceImage)
    }

    override fun onCameraImage(imageUri: Uri?, imageType: String?) {
        imagePath = imageUri
        displayImage(imagePath!!, ivReferenceImage)
    }

    private fun displayImage(selectedGalleryImage: Uri, imageID: AppCompatImageView?) {
        Glide.with(imageID!!)
            .load(selectedGalleryImage)
            .centerCrop() //4
            .placeholder(R.drawable.upload_document)
            .into(imageID)
    }
}