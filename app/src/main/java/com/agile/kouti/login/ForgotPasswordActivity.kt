package com.agile.kouti.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_forgot_password.*
import kotlinx.android.synthetic.main.activity_forgot_password.etEmail
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.toolbar.*

class ForgotPasswordActivity : KoutiBaseActivity(), View.OnClickListener {

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        mAuth = FirebaseAuth.getInstance()

        btnSubmit.setOnClickListener(this)
        ivBack.setOnClickListener(this)
        tvToolbarTitle.text = getString(R.string.forgot_password)
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.ivBack -> {
                finish()
            }

            R.id.btnSubmit -> {
                if (TextUtils.isEmpty(etEmail.text?.trim())) {
                    showError(getString(R.string.error_enter_email))
                    return
                }
                if (!isValidEmail(etEmail.text.toString().trim())) {
                    showError(getString(R.string.error_enter_valid_email))
                    return
                }
                resetPasswordEmail(etEmail.text.toString())
            }
        }

    }

    private fun resetPasswordEmail(emailAddress: String) {
        //val emailAddress = "user@example.com"
        showProgressDialog()
        mAuth?.sendPasswordResetEmail(emailAddress)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    hideProgressDialog()
                    showError(getString(R.string.reset_password_link_message))
                    finish()
                }else{
                    hideProgressDialog()
                }
            }
    }
}