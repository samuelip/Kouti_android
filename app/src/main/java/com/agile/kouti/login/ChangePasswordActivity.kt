package com.agile.kouti.login

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.agile.kouti.db.FirebaseDbClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_change_password.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.toolbar.*
import timber.log.Timber

class ChangePasswordActivity : KoutiBaseActivity(),View.OnClickListener,KoutiBaseActivity.OnDialogClickListener {

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        initData()
        mAuth = FirebaseAuth.getInstance()
    }

    private fun initData() {
        ivBack.setOnClickListener(this)
        btnChangePassword.setOnClickListener(this)

        tvToolbarTitle.text = resources.getString(R.string.toolbar_title_change_password)
        tvToolbarSubTitle.visibility = View.GONE
    }

    override fun onClick(v: View?) {

        when(v?.id) {

            R.id.ivBack -> {
                finish()
            }

            R.id.btnChangePassword -> {
                hideKeyBoard(v)

                if(TextUtils.isEmpty(etCurrentPassword.text.toString().trim())){
                    showError("Please enter current password")
                    return
                }

                if (etCurrentPassword.text?.length!! < 6) {
                    showError("Please enter valid current password")
                    return
                }

                if(TextUtils.isEmpty(etNewPassword.text.toString().trim())){
                    showError("Please enter new password")
                    return
                }

                if (etNewPassword.text?.length!! < 6) {
                    showError("Please enter valid new password")
                    return
                }

                if(TextUtils.isEmpty(etConfirmPassword.text.toString().trim())){
                    showError("Please enter confirm password")
                    return
                }

                if (etConfirmPassword.text?.length!! < 6) {
                    showError("Please enter valid confirm password")
                    return
                }

                if(etNewPassword.text.toString().trim() != etConfirmPassword.text.toString().trim()){
                    showError("New password and confirm password not matched")
                    return
                }

                changePassword()
            }
        }
    }

    private fun changePassword() {
        val user = mAuth?.currentUser
        val newPassword = etNewPassword.text.toString().trim()

        user!!.updatePassword(newPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                   showAlert("Password updated successfully")
                    var firebaseDbClient = FirebaseDbClient()

                    firebaseDbClient.getmUserReference().child( mAuth?.currentUser!!.uid).child("password").setValue(newPassword)
                    finish()
                }else{
                    Timber.e(""+task.exception)
                    setDialogListener(this)
                    showTwoButtonDialog("Error",""+task.exception,"Ok","")
                }
            }

    }

    override fun onPositiveClick() {


    }

    override fun onNegativeClick() {




    }

}