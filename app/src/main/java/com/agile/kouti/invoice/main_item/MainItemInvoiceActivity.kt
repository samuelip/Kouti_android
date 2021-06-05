package com.agile.kouti.invoice.main_item

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.agile.kouti.db.FirebaseDbClient
import com.agile.kouti.db.invoice.MainItemInvoice
import com.agile.kouti.db.payroll.Shop
import com.agile.kouti.db.quotation.LocationDB
import com.agile.kouti.db.quotation.MainItemQuotation
import com.agile.kouti.db.quotation.StockName
import com.agile.kouti.db.quotation.SubItemQuotation
import com.agile.kouti.invoice.sub_item.InvoiceSubItemActivity
import com.agile.kouti.quotation.subItem.main_item.SubItemAdapter
import com.agile.kouti.quotation.subItem.main_item.SubItemClickListener
import com.agile.kouti.quotation.subItem.sub_item.SubItemQuotationActivity
import com.agile.kouti.search.SearchActivity
import com.agile.kouti.utils.Const
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_add_invoice.*
import kotlinx.android.synthetic.main.activity_main_item_invoice.*
import kotlinx.android.synthetic.main.activity_main_item_quotation.*
import kotlinx.android.synthetic.main.toolbar.*
import timber.log.Timber
import java.util.ArrayList

class MainItemInvoiceActivity : KoutiBaseActivity(), View.OnClickListener, SubItemClickListener {

    private var isEdit = false

    private var mainItemId = ""
    private var mainItemObj: MainItemInvoice? = null

    private var shopId = ""
    private var shopName = ""

    private var subItemId = ""
    private var subItemName = ""

    private var colorCode = "#EAE3E3"

    var invoiceId = ""

    /**
     *  Note below adapter is common in  MainItemQuotationActivity and this activity
     **/
    private var subItemList = ArrayList<SubItemQuotation>()
    private var subItemListTemp = ArrayList<SubItemQuotation>()
    private val subItemAdapter = SubItemAdapter(this, subItemListTemp, this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_item_invoice)

        initData()
    }

    private fun initData() {
        ivBack.setOnClickListener(this)
        etProjectShopMainInv.setOnClickListener(this)
        btnSaveMainItemInv.setOnClickListener(this)
        tvAddSubItemInv.setOnClickListener(this)

        if (intent != null) {
            isEdit = intent.getBooleanExtra(Const.KEYS.IS_EDIT, false)
        }

        if (isEdit) {
            mainItemId = intent.getStringExtra(Const.KEYS.MAIN_ITEM_ID_INVOICE)
            tvToolbarTitle.text = resources.getString(R.string.toolbar_title_invoice)
            tvToolbarSubTitle.text = "Main Item Edit"
            tvToolbarSubTitle.visibility = View.VISIBLE

            if (!TextUtils.isEmpty(mainItemId))
                getMainItemData()
        } else {
            tvToolbarTitle.text = resources.getString(R.string.toolbar_title_invoice)
            tvToolbarSubTitle.text = "Main Item Add"
            tvToolbarSubTitle.visibility = View.VISIBLE

            var searchValue = intent.getStringExtra(Const.KEYS.SEARCH_TEXT)
            if (!TextUtils.isEmpty(searchValue)) {
                etItemNameInv.setText(searchValue)
            }

            /* this intent came from Main Item invoice List Activity
            *  add main item button click */
            if (intent.getStringExtra(Const.KEYS.INVOICE_ID) != null)
                invoiceId = intent.getStringExtra(Const.KEYS.INVOICE_ID)

        }
        colorCode = randomColor.toString()

        setSubItemAdapter()
    }

    /**
     * Edit Module *
     */
    private fun getMainItemData() {
        showProgressDialog()
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.mainItemInvoice.child(mainItemId).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    var obj: MainItemInvoice? = dataSnapshot.getValue(MainItemInvoice::class.java)
                    if (obj != null) {
                        mainItemObj = obj
                        setMainItemData()
                    }
                    hideProgressDialog()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                hideProgressDialog()
            }
        })

    }

    private fun setMainItemData() {
        etItemNameInv.setText(mainItemObj!!.item)
        etDescriptionMainInv.setText(mainItemObj!!.description)
        etAmountInvoice.setText(mainItemObj!!.amount)
        etDiscountMainInv.setText(mainItemObj!!.discount)
        etNetAmountInvoice.setText(mainItemObj!!.net_amount)

        // get project/shop
        getShopName(mainItemObj!!.project)

        var result = mainItemObj!!.stock_list!!.split(",").map { it.trim() }
        result.forEach {
            Timber.e("id -- " + it)
            if (!TextUtils.isEmpty(it))
                getSubListFromIds(it)
        }
    }

    /* get Sub List from Id */
    private fun getSubListFromIds(subListId: String) {
        Timber.e("subListId -- " + subListId)
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.subItemQuotation.child(subListId.toString())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var subListObj: SubItemQuotation? =
                            dataSnapshot.getValue(SubItemQuotation::class.java)
                        if (subListObj != null) {
                            //subItemListTemp.add(subListObj)
                            subItemList.add(subListObj!!)

                            // Get Stock Name
                            var mFirebaseClient1 = FirebaseDbClient()
                            mFirebaseClient1.stockName.child(subListObj.stock_name.toString())
                                .addListenerForSingleValueEvent(object :
                                    ValueEventListener {
                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            var obj: StockName? =
                                                dataSnapshot.getValue(StockName::class.java)
                                            if (obj != null) {
                                                subListObj.stock_name = obj.name


                                                // get Location Name
                                                var mFirebaseClient2 = FirebaseDbClient()
                                                mFirebaseClient2.location.child(subListObj.location.toString())
                                                    .addListenerForSingleValueEvent(object :
                                                        ValueEventListener {
                                                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                            if (dataSnapshot.exists()) {
                                                                var objLoc: LocationDB? =
                                                                    dataSnapshot.getValue(LocationDB::class.java)
                                                                if (objLoc != null) {
                                                                    subListObj.location =
                                                                        objLoc.name

                                                                    subItemListTemp.add(subListObj)
                                                                    subItemAdapter.notifyDataSetChanged()
                                                                }
                                                            }
                                                        }

                                                        override fun onCancelled(error: DatabaseError) {}
                                                    })
                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                        }
                    }
                    subItemAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    /* Get Shop Name*/
    private fun getShopName(id: String?) {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.shop.child(id.toString()).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    var obj: Shop? = dataSnapshot.getValue(Shop::class.java)
                    if (obj != null) {
                        etProjectShopMainInv.setText(obj.name)
                        shopId = obj.id!!
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    /* Set PersonList Adapter*/
    private fun setSubItemAdapter() {
        val layoutManager =
            LinearLayoutManager(this@MainItemInvoiceActivity, LinearLayoutManager.VERTICAL, false)
        rvSubItemInv.layoutManager = layoutManager
        rvSubItemInv.adapter = subItemAdapter
    }


    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.ivBack -> {
                finish()
            }

            R.id.etProjectShopMainInv -> {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra(
                    Const.KEYS.SEARCH_TYPE,
                    Const.SearchType.ProjectShopMainItemInvoice.toString()
                )
                intent.putExtra(Const.KEYS.COLOR_CODE, colorCode)
                startActivityForResult(intent, Const.RequestCodes.PROJECT_SHOP_INVOICE_MAIN_ITEM)
            }

            R.id.tvAddSubItemInv -> {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra(
                    Const.KEYS.SEARCH_TYPE,
                    Const.SearchType.AddSubItemInvoice.toString()
                )
                intent.putExtra(Const.KEYS.COLOR_CODE, colorCode)
                startActivityForResult(intent, Const.RequestCodes.ADD_SUB_ITEM_INVOICE)
            }

            R.id.btnSaveMainItemInv -> {
                hideKeyBoard(v)
                if (checkValidation()) {
                    saveDataInDB()
                }
            }
        }
    }

    /* Save Data IN DB */
    private fun saveDataInDB() {
        showProgressDialog()
        if (!isEdit)
            mainItemId =
                FirebaseDatabase.getInstance().getReference(Const.TableName.MAIN_ITEM_INVOICE)
                    .push().key.toString()

        var subList = ""
        if (!subItemList.isEmpty()) {
            for (list in subItemList) {
                if (TextUtils.isEmpty(subList)) {
                    subList = list.id.toString()
                } else {
                    subList = subList + "," + list.id
                }
            }
        }

        Timber.e("subList -- " + subList)
        var subItemListSize = subItemList.size

        mainItemObj = MainItemInvoice(
            mainItemId,
            etItemNameInv.text.toString().trim(),
            etDescriptionMainInv.text.toString().trim(),
            shopId,
            etAmountInvoice.text.toString().trim(),
            etDiscountMainInv.text.toString().trim(),
            etNetAmountInvoice.text.toString().trim(),
            subList,
            subItemListSize.toString(),
            false
        )

        var firebaseDbClient = FirebaseDbClient()
        if (!TextUtils.isEmpty(mainItemId) && mainItemId != null) {
            firebaseDbClient.mainItemInvoice.child(mainItemId.toString()).setValue(mainItemObj)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        hideProgressDialog()

//                        if (!TextUtils.isEmpty(invoiceId)) {
//                            getInvoiceData()
//                        } else {
//                            finish()
//                        }
                        finish()
                    } else
                        hideProgressDialog()
                }
        } else
            hideProgressDialog()

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Const.RequestCodes.PROJECT_SHOP_INVOICE_MAIN_ITEM -> {
                if (data != null) {
                    shopName = data.getStringExtra(Const.KEYS.SEARCH_TEXT)
                    shopId = data.getStringExtra(Const.KEYS.SHOP_ID)
                    etProjectShopMainInv.setText(shopName)
                    Timber.e("shopId -- " + shopId)
                }
            }

            Const.RequestCodes.ADD_SUB_ITEM_INVOICE -> {
                if (data != null) {
                    subItemName = data.getStringExtra(Const.KEYS.SEARCH_TEXT)
                    subItemId = data.getStringExtra(Const.KEYS.SUB_ITEM_ID_INVOICE)
                    Timber.e("subItemId -- " + subItemId)
                    Timber.e("subItemName -- " + subItemName)

                    // Get SubItem list
                    var mFirebaseClient = FirebaseDbClient()
                    mFirebaseClient.subItemQuotation.child(subItemId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    var subItemObj: SubItemQuotation? =
                                        dataSnapshot.getValue(SubItemQuotation::class.java)
                                    Timber.e("personObj --- " + subItemObj!!.location)
                                    subItemList.add(subItemObj)

                                    // Get Stock Name
                                    var mFirebaseClient1 = FirebaseDbClient()
                                    mFirebaseClient1.stockName.child(subItemObj.stock_name.toString())
                                        .addListenerForSingleValueEvent(object :
                                            ValueEventListener {
                                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    var obj: StockName? =
                                                        dataSnapshot.getValue(StockName::class.java)
                                                    if (obj != null) {
                                                        subItemObj.stock_name = obj.name


                                                        // get Location Name
                                                        var mFirebaseClient2 = FirebaseDbClient()
                                                        mFirebaseClient2.location.child(subItemObj.location.toString())
                                                            .addListenerForSingleValueEvent(object :
                                                                ValueEventListener {
                                                                override fun onDataChange(
                                                                    dataSnapshot: DataSnapshot
                                                                ) {
                                                                    if (dataSnapshot.exists()) {
                                                                        var objLoc: LocationDB? =
                                                                            dataSnapshot.getValue(
                                                                                LocationDB::class.java
                                                                            )
                                                                        if (objLoc != null) {
                                                                            subItemObj.location =
                                                                                objLoc.name

                                                                            subItemListTemp.add(
                                                                                subItemObj
                                                                            )
                                                                            subItemAdapter.notifyDataSetChanged()


                                                                        }
                                                                    }
                                                                }

                                                                override fun onCancelled(error: DatabaseError) {}
                                                            })
                                                    }
                                                }
                                            }

                                            override fun onCancelled(error: DatabaseError) {

                                            }
                                        })
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                }
            }
        }
    }

    override fun onSubItemClick(list: SubItemQuotation) {
        val intent = Intent(this, InvoiceSubItemActivity::class.java)
        intent.putExtra(Const.KEYS.IS_EDIT, true)
        intent.putExtra(Const.KEYS.SUB_ITEM_ID_INVOICE, list.id)
        startActivity(intent)
    }

    override fun onItemSelectClick(list: SubItemQuotation, pos: Int) {}

    /* Check Validation */
    private fun checkValidation(): Boolean {

        if (TextUtils.isEmpty(etItemNameInv.text.toString().trim())) {
            showError(getString(R.string.enter_item_name))
            return false
        }

        if (TextUtils.isEmpty(etDescriptionMainInv.text.toString().trim())) {
            showError(getString(R.string.enter_description))
            return false
        }

        if (TextUtils.isEmpty(etProjectShopMainInv.text.toString().trim())) {
            showError(getString(R.string.enter_shop))
            return false
        }

        if (TextUtils.isEmpty(etAmountInvoice.text.toString().trim())) {
            showError(getString(R.string.enter_amount))
            return false
        }

        if (TextUtils.isEmpty(etDiscountMainInv.text.toString().trim())) {
            showError(getString(R.string.enter_discount))
            return false
        }

        if (TextUtils.isEmpty(etNetAmountInvoice.text.toString().trim())) {
            showError(getString(R.string.enter_net_amount))
            return false
        }

        if (subItemListTemp.isEmpty()) {
            showError("Please select atleast one sub item.")
            return false
        }

        if (!isNetworkConnected) {
            showError(getString(R.string.internet_error))
            return false
        }
        return true
    }
}