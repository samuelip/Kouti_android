package com.agile.kouti.quotation.subItem.sub_item

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.agile.kouti.db.FirebaseDbClient
import com.agile.kouti.db.quotation.Units
import com.agile.kouti.utils.Const
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_add_item.*
import kotlinx.android.synthetic.main.activity_add_units.*
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.toolbar.ivBack

class AddUnitsActivity : KoutiBaseActivity(), View.OnClickListener {

    private var unitId = ""
    var unitObj: Unit? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_units)

        if (intent != null) {
            var name = intent.getStringExtra(Const.KEYS.UNIT_NAME)
            etUnitName.setText(name)
        }
        initData()
    }

    private fun initData() {

        tvToolbarTitle.text = "Units"
        ivBack.setOnClickListener(this)
        btnSaveUnit.setOnClickListener(this)

    }


    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.ivBack -> {
                finish()
            }

            R.id.btnSaveUnit -> {

                if (TextUtils.isEmpty(etUnitName.text.toString().trim())) {
                    showError("Please enter unit name.")
                    return
                }

                if (TextUtils.isEmpty(etDisplay.text.toString().trim())) {
                    showError("Please enter display name.")
                    return
                }

                if (TextUtils.isEmpty(etRate.text.toString().trim())) {
                    showError("Please enter rate name.")
                    return
                }

                if (TextUtils.isEmpty(etConversionUnit.text.toString().trim())) {
                    showError("Please enter conversion unit.")
                    return
                }

                addDataInDB()
            }
        }


    }

    private fun addDataInDB() {
        showProgressDialog()
        unitId = FirebaseDatabase.getInstance().getReference(Const.TableName.PAYROLL_PROJECT_SHOP)
            .push().key.toString()

        var obj = Units(unitId, etUnitName.text.toString().trim(), etDisplay.text.toString().trim(), etRate.text.toString().trim(), etConversionUnit.text.toString().trim())

        var firebaseDbClient = FirebaseDbClient()
        if (unitId != null) {
            firebaseDbClient.unit.child(unitId).setValue(obj).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    showAlert("Success")
                    hideProgressDialog()
                    finish()
                }else
                    hideProgressDialog()
            }


        }

    }
}