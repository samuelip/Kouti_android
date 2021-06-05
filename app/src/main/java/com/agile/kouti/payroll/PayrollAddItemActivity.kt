package com.agile.kouti.payroll

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.agile.kouti.db.CurrencyTable
import com.agile.kouti.db.FirebaseDbClient
import com.agile.kouti.db.payroll.Item
import com.agile.kouti.db.payroll.PayRollItem
import com.agile.kouti.db.payroll.Shop
import com.agile.kouti.db.payroll.Staff
import com.agile.kouti.search.SearchActivity
import com.agile.kouti.utils.Const
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_add_pay_roll.*
import kotlinx.android.synthetic.main.activity_add_receipt.*
import kotlinx.android.synthetic.main.activity_payroll_add_item.*
import kotlinx.android.synthetic.main.toolbar.*
import timber.log.Timber

class PayrollAddItemActivity : KoutiBaseActivity(), View.OnClickListener {

    private var colorCode = "#EAE3E3"

    private var vendorId = ""
    private var itemId = ""
    private var shopId = ""

    private var isEdit = false

    private var payRollData = PayRollItem()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payroll_add_item)

        initData()
    }

    private fun initData() {

        ivBack.setOnClickListener(this)
        etStaffVendor.setOnClickListener(this)
        etAddItem.setOnClickListener(this)
        etAddProject.setOnClickListener(this)
        btnAdd.setOnClickListener(this)

        if (intent != null) {
            isEdit = intent.getBooleanExtra(Const.KEYS.IS_EDIT, false)

            if (isEdit) {
                tvToolbarTitle.text = getString(R.string.toolbar_payroll_add_item)
                tvToolbarSubTitle.text = getString(R.string.txt_edit)
                tvToolbarSubTitle.visibility = View.VISIBLE

                payRollData = intent.getParcelableExtra<PayRollItem>(Const.KEYS.LIST_OBJECT)

                if (payRollData != null) {
                    getPayrollDataFromId(payRollData.id.toString())
                }

            } else {
                tvToolbarTitle.text = getString(R.string.toolbar_payroll_add_item)
                tvToolbarSubTitle.text = getString(R.string.txt_add)
                tvToolbarSubTitle.visibility = View.VISIBLE

                var remark = intent.getStringExtra(Const.KEYS.REMARK)
                etAddRemark.setText(remark)
            }
        }


    }

    /*** Get Payroll from id */
    private fun getPayrollDataFromId(payrollId: String) {
        var mFirebaseClient = FirebaseDbClient()

        mFirebaseClient.payrollItem.child(payrollId).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    var payRollObj: PayRollItem? = dataSnapshot.getValue(PayRollItem::class.java)
                    if (payRollObj != null) {
                        Timber.e("payRollObj -- " + payRollObj?.id)
                        Timber.e("payRollObj -- " + payRollObj?.item)
                        Timber.e("payRollObj -- " + payRollObj?.shop)
                        Timber.e("payRollObj -- " + payRollObj?.vendor)

                        vendorId = payRollObj.vendor.toString()
                        itemId = payRollObj.item.toString()
                        shopId = payRollObj.shop.toString()


                        etAddRemark.setText(payRollData.remark)
                        etAddAmount.setText(payRollData.amount)

                        if (!TextUtils.isEmpty(vendorId))
                            getStaffName()

                        if (!TextUtils.isEmpty(shopId))
                            getProjectShopName()

                        if (!TextUtils.isEmpty(itemId))
                            getItemName()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}

        })
    }

    /* get Item name */
    private fun getItemName() {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.item.child(itemId)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var obj: Item? = dataSnapshot.getValue(Item::class.java)
                        if (obj != null) {
                            itemId = obj?.id!!
                            etAddItem.setText(obj.fourth_level)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    /* get Staff/Vendoe Name*/
    private fun getStaffName() {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.staff.child(vendorId)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var obj: Staff? = dataSnapshot.getValue(Staff::class.java)
                        if (obj != null) {
                            vendorId = obj?.id!!
                            etStaffVendor.setText(obj.name)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })


    }

    /* Get project shop name */
    private fun getProjectShopName() {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.shop.child(shopId)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var obj: Shop? = dataSnapshot.getValue(Shop::class.java)
                        if (obj != null) {
                            shopId = obj?.id!!
                            etAddProject.setText(obj.name)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

    }


    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.etStaffVendor -> {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra(Const.KEYS.SEARCH_TYPE, Const.SearchType.StaffVendor.toString())
                intent.putExtra(Const.KEYS.COLOR_CODE, colorCode)
                startActivityForResult(intent, Const.RequestCodes.STAFF_VENDOR)
            }

            R.id.etAddItem -> {

                if (isEdit) {
                    val intent = Intent(this, AddItemActivity::class.java)
                    intent.putExtra(Const.KEYS.ITEM_ID, itemId)
                    intent.putExtra(Const.KEYS.IS_EDIT, true)
                    startActivity(intent)
                } else {
                    val intent = Intent(this, SearchActivity::class.java)
                    intent.putExtra(Const.KEYS.SEARCH_TYPE, Const.SearchType.AddItem.toString())
                    intent.putExtra(Const.KEYS.COLOR_CODE, colorCode)
                    startActivityForResult(intent, Const.RequestCodes.ADD_ITEM)
                }
            }

            R.id.etAddProject -> {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra(Const.KEYS.SEARCH_TYPE, Const.SearchType.ProjectShop.toString())
                intent.putExtra(Const.KEYS.COLOR_CODE, colorCode)
                startActivityForResult(intent, Const.RequestCodes.PROJECT_SHOP)
            }

            R.id.btnAdd -> {
                hideKeyBoard(v)

                if (TextUtils.isEmpty(etStaffVendor.text.toString().trim())) {
                    showError(getString(R.string.error_select_staff))
                    return
                }
                if (TextUtils.isEmpty(etAddItem.text.toString().trim())) {
                    showError(getString(R.string.error_select_item))
                    return
                }
                if (TextUtils.isEmpty(etAddRemark.text.toString().trim())) {
                    showError(getString(R.string.error_enter_remark))
                    return
                }

                if (TextUtils.isEmpty(etAddProject.text.toString().trim())) {
                    showError(getString(R.string.select_project_shop))
                    return
                }
                if (TextUtils.isEmpty(etAddAmount.text.toString().trim())) {
                    showError(getString(R.string.error_please_enter_amount))
                    return
                }

                if (!isNetworkConnected) {
                    showError(getString(R.string.internet_error))
                    return
                }

                if (isEdit)
                    updateData()
                else
                    insertData()

//                val payroll_id = FirebaseDatabase.getInstance().getReference(Const.TableName.PAYROLL_ITEM).push().key
//                val payRollItem = PayRollItem(payroll_id.toString(),)
            }

            R.id.ivBack -> {
                finish()
            }
        }

    }

    private fun updateData() {
        showProgressDialog()
        val payRollItemObj = PayRollItem(
            etAddAmount.text.toString(),
            payRollData.id,
            itemId,
            etAddRemark.text.toString(),
            shopId,
            vendorId
        )

        var firebaseDbClient = FirebaseDbClient()
        if (payRollItemObj != null) {
            firebaseDbClient.payrollItem.child(payRollData.id.toString()).setValue(payRollItemObj)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        hideProgressDialog()
                        finish()
                    } else {
                        hideProgressDialog()
                        showError(getString(R.string.try_again))
                    }
                }
        } else {
            hideProgressDialog()
        }


    }

    private fun insertData() {
        showProgressDialog()
        val payrollItemId =
            FirebaseDatabase.getInstance().getReference(Const.TableName.PAYROLL_ITEM)
                .push().key.toString()
        val payRollItemObj = PayRollItem(
            etAddAmount.text.toString(),
            payrollItemId,
            itemId,
            etAddRemark.text.toString(),
            shopId,
            vendorId
        )

        var firebaseDbClient = FirebaseDbClient()
        if (payRollItemObj != null) {
            firebaseDbClient.payrollItem.child(payrollItemId).setValue(payRollItemObj)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        hideProgressDialog()
                        finish()
                    } else {
                        hideProgressDialog()
                        showError(getString(R.string.try_again))
                    }
                }
        } else {
            hideProgressDialog()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Const.RequestCodes.STAFF_VENDOR -> {
                if (data != null) {
                    var staffName = data.getStringExtra(Const.KEYS.SEARCH_TEXT)
                    vendorId = data.getStringExtra(Const.KEYS.STAFF_ID)
                    Timber.e("staffId -- " + vendorId)
                    etStaffVendor.setText(staffName)


                }
            }
            Const.RequestCodes.ADD_ITEM -> {
                if (data != null) {
                    var itemName = data.getStringExtra(Const.KEYS.SEARCH_TEXT)
                    etAddItem.setText(itemName)
                    itemId = data.getStringExtra(Const.KEYS.ITEM_ID)
                    Timber.e("itemId -- " + itemId)

                }
            }
            Const.RequestCodes.PROJECT_SHOP -> {
                if (data != null) {
                    var shopName = data.getStringExtra(Const.KEYS.SEARCH_TEXT)
                    shopId = data.getStringExtra(Const.KEYS.SHOP_ID)
                    Timber.e("shopId -- " + shopId)
                    etAddProject.setText(shopName)
                }
            }
        }
    }
}