package com.agile.kouti.payroll

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.agile.kouti.db.FirebaseDbClient
import com.agile.kouti.db.payroll.Staff
import com.agile.kouti.search.SearchActivity
import com.agile.kouti.utils.Const
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_add_staff.*
import kotlinx.android.synthetic.main.toolbar.*

class AddStaffActivity : KoutiBaseActivity(),View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_staff)
        initData()
    }

    private fun initData() {
        tvToolbarTitle.text = "Staff/Vendor"

        btnAdd.setOnClickListener(this)
        ivBack.setOnClickListener(this)

        if(intent != null){
            var staffName = intent.getStringExtra("staff_name")
            etVendorName.setText(staffName)
        }

    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.ivBack->{
                finish()
            }
            R.id.btnAdd->{
                hideKeyBoard(v)

                if(TextUtils.isEmpty(etVendorName.text.toString().trim())){
                    showError(getString(R.string.enter_vendor_name))
                    return
                }
                if(TextUtils.isEmpty(etVendorGroup.text.toString().trim())){
                    showError(getString(R.string.enter_group))
                    return
                }

                var staffId = FirebaseDatabase.getInstance().getReference(Const.TableName.PAYROLL_STAFF).push().key.toString()
                var staffObj = Staff(staffId,etVendorName.text.toString(),etVendorGroup.text.toString())
                var firebaseDbClient = FirebaseDbClient()

                if (staffId != null) {
                    firebaseDbClient.staff.child(staffId).setValue(staffObj).addOnCompleteListener { task ->
                        if(task.isSuccessful){
                            val intent = Intent(this, SearchActivity::class.java).apply { putExtra(Const.KEYS.LIST_OBJECT, staffObj) }
                            setResult(Const.RequestCodes.ADD_STAFF_VENDOR, intent)
                            finish()
                        }else{
                            showError("Please try again")
                        }
                    }
                }
            }
        }
    }
}