package com.agile.kouti.login

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.agile.kouti.db.FirebaseDbClient
import com.agile.kouti.db.User
import com.agile.kouti.home.HomeActivity
import com.agile.kouti.utils.Const
import com.agile.kouti.utils.Preferences
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_add_sub_user.*
import kotlinx.android.synthetic.main.activity_login.*
import timber.log.Timber

class LoginActivity : KoutiBaseActivity(), View.OnClickListener,
    KoutiBaseActivity.OnDialogClickListener {

    private var mAuth: FirebaseAuth? = null
    private var fcmToken: String? = null
    private lateinit var database: DatabaseReference

   private var device_id =""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().reference
        getFcmToken()
        device_id = deviceId
        tvForgotPassword.setOnClickListener(this)
        btnLogin.setOnClickListener(this)
        btnSignUp.setOnClickListener(this)
    }

    override fun onClick(v: View?) {

        when (v?.id) {

            R.id.tvForgotPassword -> {
                hideKeyBoard(v)
                val i = Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
                startActivity(i)
            }

            R.id.btnLogin -> {
                hideKeyBoard(v!!)
                checkValidation()
            }

            R.id.btnSignUp -> {
                //signUpMethod()
            }
        }
    }

    private fun checkValidation() {
        if (TextUtils.isEmpty(etEmail.text?.trim())) {
            showError(getString(R.string.error_enter_email))
            return
        }

        if (!isValidEmail(etEmail.text.toString().trim())) {
            showError(getString(R.string.error_enter_valid_email))
            return
        }
        if (TextUtils.isEmpty(etPassword.text?.trim())) {
            showError(getString(R.string.error_enter_password))
            return
        }
        if (etPassword.text?.length!! < 6) {
            showError(getString(R.string.error_valid_password))
            return
        }

        if(isNetworkConnected){
            showProgressDialog()
            signInMethod(etEmail.text?.trim().toString(), etPassword.text?.trim().toString())
        }else{
            showError(getString(R.string.internet_error))
        }


    }

    private fun signInMethod(email: String, password: String) {
        mAuth!!.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {

                var firebaseDbClient = FirebaseDbClient()

                firebaseDbClient.getmUserReference().child(task.result?.user?.uid!!).child("password").setValue(etPassword.text.toString().trim())
                firebaseDbClient.getmUserReference().child(task.result?.user?.uid!!).child("email").setValue(etEmail.text.toString().trim())
                firebaseDbClient.getmUserReference().child(task.result?.user?.uid!!).child("device_id").setValue(device_id)
                firebaseDbClient.getmUserReference().child(task.result?.user?.uid!!).child("device_type").setValue("Android")
                firebaseDbClient.getmUserReference().child(task.result?.user?.uid!!).child("fcm_token").setValue(fcmToken)

                showError("SignIn Successful.")

                hideProgressDialog()
                Preferences.setPreference(this,Const.SharedPrefs.USER_ID,task.result?.user?.uid!!)

                val i = Intent(this@LoginActivity, HomeActivity::class.java)
                startActivity(i)
                finish()

            } else {
                hideProgressDialog()
                setDialogListener(this)
                showTwoButtonDialog(resources.getString(R.string.app_name),task.exception.toString(), "Ok", "")

            }
        }
    }


    private fun getFcmToken() {
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }
            // Get new Instance ID token
            fcmToken = task.result!!.token
            Timber.e("fcm token -- : %s", fcmToken)
            Preferences.setPreference(this,Const.SharedPrefs.FCM_TOKEN,fcmToken)
        })
    }



    override fun onPositiveClick() {}

    override fun onNegativeClick() {}


    /* SignUp */
    private fun signUpMethod() {
        mAuth!!.createUserWithEmailAndPassword(etEmail.text.toString().trim(), etPassword.text.toString().trim())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    var authId = mAuth!!.currentUser?.uid
                    var id = task.result?.user?.uid

                    var createdDate = utcDate

                    val user = User(
                        authId!!,
                        "",
                        "",
                        etEmail.text.toString().trim(),
                        "",
                        id,
                        "",
                        etPassword.text.toString().trim(),
                        "",
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

}