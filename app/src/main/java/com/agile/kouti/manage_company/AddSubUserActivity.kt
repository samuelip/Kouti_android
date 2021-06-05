package com.agile.kouti.manage_company

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.agile.kouti.db.FirebaseDbClient
import com.agile.kouti.db.User
import com.agile.kouti.db.crm.Crm
import com.agile.kouti.db.sub_user.SubUser
import com.agile.kouti.home.HomeActivity
import com.agile.kouti.utils.Const
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_add_sub_user.*
import kotlinx.android.synthetic.main.activity_add_sub_user.etName
import kotlinx.android.synthetic.main.toolbar.*
import timber.log.Timber

class AddSubUserActivity : KoutiBaseActivity(),View.OnClickListener{

    private var isEdit = false
    private var createdDate = ""

    private var subUserId = ""
    private var subUserObj: User? = null

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_sub_user)
        initData()
        mAuth = FirebaseAuth.getInstance();
    }

    private fun initData() {
        ivBack.setOnClickListener(this)
        btnCreate.setOnClickListener(this)

        if (intent != null) {
            isEdit = intent.getBooleanExtra(Const.KEYS.IS_EDIT, false)
        }
        tvToolbarSubTitle.visibility = View.GONE

        if (isEdit) {
            tvToolbarTitle.text = resources.getString(R.string.toolbar_title_manage_edit_sub_user)
            subUserId = intent.getStringExtra(Const.KEYS.SUB_USER_ID)

            if (!TextUtils.isEmpty(subUserId)) {
                getSubUserDataFromID()
            }
        } else {
            tvToolbarTitle.text = resources.getString(R.string.toolbar_title_manage_add_sub_user)
        }
    }

    private fun getSubUserDataFromID() {
        showProgressDialog()
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.getmUserReference().child(subUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var userObject: User? = dataSnapshot.getValue(User::class.java)
                        if(userObject!=null) {
                            subUserObj = userObject
                            setUserData()
                        }
                        hideProgressDialog()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    hideProgressDialog()
                }
            })
    }

    private fun setUserData() {

        etName.setText(subUserObj?.name)
        etPhone.setText(subUserObj?.phone)
        etEmailAddress.setText(subUserObj?.email)
        etPasswordUser.setText(subUserObj?.password)

        etEmailAddress.isFocusableInTouchMode = false
        etPasswordUser.isFocusable = false


    }

    override fun onClick(v: View?) {
        when(v?.id){

            R.id.ivBack->{finish()}

            R.id.btnCreate->{
                hideKeyBoard(v)
                if(checkValidation()){
                    if (isEdit){
                        updateUserData()
                    }else {
                        signUpMethod()
                    }
                }
            }
        }

    }

    private fun updateUserData() {

//        var firebaseDbClient = FirebaseDbClient()
//
//        firebaseDbClient.getmUserReference().child(subUserId).child("password").setValue(etPasswordUser.text.toString().trim())
//        firebaseDbClient.getmUserReference().child(subUserId).child("name").setValue(etName.text.toString().trim())
//        firebaseDbClient.getmUserReference().child(subUserId).child("phone").setValue(etPhone.text.toString().trim())
//        firebaseDbClient.getmUserReference().child(subUserId).child("email").setValue(etEmailAddress.text.toString().trim())
//
//        showAlert("Update data successfully")
//        finish()
    }

    /* SignUp */
    private fun signUpMethod() {
        if (!isEdit) {
            createdDate = utcDate
            subUserId = FirebaseDatabase.getInstance().getReference(Const.TableName.USER).push().key.toString()
        }

        mAuth!!.createUserWithEmailAndPassword(etEmailAddress.text.toString().trim(), etPasswordUser.text.toString().trim())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    var authId = mAuth!!.currentUser?.uid
                    var id = task.result?.user?.uid

                    val user = User(
                        authId!!,
                        "",
                        "",
                        etEmailAddress.text.toString().trim(),
                        "",
                        id,
                        etName.text.toString().trim(),
                        etPasswordUser.text.toString().trim(),
                        etPhone.text.toString().trim(),
                        createdDate
                    )
                    var firebaseDbClient = FirebaseDbClient()

                    firebaseDbClient!!.getmUserReference().child(id.toString()).setValue(user)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                showAlert("Add Sub User successfully.")
                               finish()

                            }
                        }
                } else {
                    showError("Account Created Failed")
                }
            }
    }


    private fun checkValidation():Boolean{

        if (TextUtils.isEmpty(etName.text.toString().trim())) {
            showError(getString(R.string.enter_name))
            return false
        }

        if (TextUtils.isEmpty(etPhone.text.toString().trim())) {
            showError(getString(R.string.enter_phone_number))
            return false
        }

        if (etPhone.text.toString().trim()!!.length<10)  {
            showError(getString(R.string.enter_valid_phone_no))
            return false
        }


        if (TextUtils.isEmpty(etEmailAddress.text.toString().trim())) {
            showError(getString(R.string.error_enter_email))
            return false
        }

        if (!isValidEmail(etEmailAddress.text.toString().trim())) {
            showError(getString(R.string.error_enter_valid_email))
            return false
        }

        if (TextUtils.isEmpty(etPasswordUser.text.toString().trim())) {
            showError(getString(R.string.error_enter_password))
            return false
        }

        if (etPasswordUser.text?.length!! < 6) {
            showError(getString(R.string.error_valid_password))
            return false
        }

        return true
    }

    private fun updateEmail(){
        val user = mAuth!!.currentUser
        user!!.updateEmail("user@example.com")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                }
            }
    }

    // reference : https://firebase.google.com/docs/auth/android/manage-users#kotlin+ktx_10
}