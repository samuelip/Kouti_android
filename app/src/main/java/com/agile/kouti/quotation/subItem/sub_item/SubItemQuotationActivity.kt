package com.agile.kouti.quotation.subItem.sub_item

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.agile.kouti.db.FirebaseDbClient
import com.agile.kouti.db.payroll.Shop
import com.agile.kouti.db.quotation.*
import com.agile.kouti.search.SearchActivity
import com.agile.kouti.utils.Const
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_sub_item_quatation.*
import kotlinx.android.synthetic.main.toolbar.*
import timber.log.Timber

class SubItemQuotationActivity : KoutiBaseActivity(), View.OnClickListener,
    KoutiBaseActivity.OnDateSelectedListener {

    private var isEdit = false

    private var subItemId = "" //-MGJFr8FisdBP0mJ83oF
    var subItemObj: SubItemQuotation? = null

    var mainItemId = ""

    private var locationId = ""
    private var locationName = ""

    private var shopId = ""
    private var shopName = ""

    private var unitId = ""
    private var unitName = ""

    private var stockNameId = ""
    private var stockName = ""

    private var colorCode = "#EAE3E3"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sub_item_quatation)

        initData()
    }

    private fun initData() {

        ivBack.setOnClickListener(this)
        etLocation.setOnClickListener(this)
        etProjectShop.setOnClickListener(this)
        etStockoutdate.setOnClickListener(this)
        btnAddSubItem.setOnClickListener(this)
        etUnit.setOnClickListener(this)
        etStockName.setOnClickListener(this)

        if (intent != null) {
            isEdit = intent.getBooleanExtra(Const.KEYS.IS_EDIT, false)
        }

        if (isEdit) {
            subItemId = intent.getStringExtra(Const.KEYS.SUB_ITEM_ID)

            tvToolbarTitle.text = resources.getString(R.string.toolbar_title_quotation)
            tvToolbarSubTitle.text = "Sub Item Edit"
            tvToolbarSubTitle.visibility = View.VISIBLE

            if (!TextUtils.isEmpty(subItemId)) {
                getSubItemData()
            }
        } else {
            tvToolbarTitle.text = resources.getString(R.string.toolbar_title_quotation)
            tvToolbarSubTitle.text = "Sub Item Add"
            tvToolbarSubTitle.visibility = View.VISIBLE

            /* this intent came from  SubItemQuotationListActivity
            *  add sub item from subItem List  */
            if (intent.getStringExtra(Const.KEYS.MAIN_ITEM_ID) != null)
                mainItemId = intent.getStringExtra(Const.KEYS.MAIN_ITEM_ID)

        }
        colorCode = randomColor.toString()
    }

    /**
     *  Edit Module *
     */

    private fun getSubItemData() {
        showProgressDialog()
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.subItemQuotation.child(subItemId).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    var obj: SubItemQuotation? = dataSnapshot.getValue(SubItemQuotation::class.java)
                    if (obj != null) {
                        subItemObj = obj
                        Timber.e("subItemObj  -- " + subItemObj!!.stock_name)
                        setSubItemData()
                    }
                    hideProgressDialog()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                hideProgressDialog()
            }
        })
    }

    private fun setSubItemData() {

        // Stock Name
        getStockNameFromID(subItemObj!!.stock_name)

        // get Location
        getLocationName(subItemObj!!.location)

        // get shop
        getShopName(subItemObj!!.shop)

        // Get Unit Name
        Timber.e("Unit --- " + subItemObj!!.unit)
        getUnitName(subItemObj!!.unit)

        etSpecification.setText(subItemObj!!.specification)
        etDescription.setText(subItemObj!!.description)
        etQty.setText(subItemObj!!.quntity)

        etUP.setText(subItemObj!!.up)
        etDiscount.setText(subItemObj!!.discount)
        etNP.setText(subItemObj!!.np)
        etLineTotal.setText(subItemObj!!.line_total)
        etStockoutdate.setText(subItemObj!!.stock_date)


    }

    /* Get Stock Name From Id */
    private fun getStockNameFromID(id: String?) {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.stockName.child(id.toString()).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    var obj: StockName? = dataSnapshot.getValue(StockName::class.java)
                    if (obj != null) {
                        etStockName.setText(obj.name)
                        stockNameId = obj.id!!
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    /* Get location From Id */
    private fun getLocationName(id: String?) {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.location.child(id.toString()).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    var obj: LocationDB? = dataSnapshot.getValue(LocationDB::class.java)
                    if (obj != null) {
                        etLocation.setText(obj.name)
                        locationId = obj.id!!
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    /* get Shop Name From Id*/
    private fun getShopName(id: String?) {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.shop.child(id.toString()).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    var obj: Shop? = dataSnapshot.getValue(Shop::class.java)
                    if (obj != null) {
                        etProjectShop.setText(obj.name)
                        shopId = obj.id!!
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    /* Get Unit Name*/
    private fun getUnitName(id: String?) {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.unit.child(id.toString()).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    var obj: Units? = dataSnapshot.getValue(Units::class.java)
                    if (obj != null) {
                        etUnit.setText(obj.name)
                        unitId = obj.id!!
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

    }


    /*** Click Events ***/
    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.ivBack -> {
                finish()
            }

            R.id.etStockName -> {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra(Const.KEYS.SEARCH_TYPE, Const.SearchType.StockName.toString())
                intent.putExtra(Const.KEYS.COLOR_CODE, "")
                startActivityForResult(intent, Const.RequestCodes.STOCK_NAME)
            }

            R.id.etLocation -> {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra(Const.KEYS.SEARCH_TYPE, Const.SearchType.Location.toString())
                intent.putExtra(Const.KEYS.COLOR_CODE, "")
                startActivityForResult(intent, Const.RequestCodes.LOCATION)
            }

            R.id.etProjectShop -> {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra(
                    Const.KEYS.SEARCH_TYPE,
                    Const.SearchType.ProjectShopQuotation.toString()
                )
                intent.putExtra(Const.KEYS.COLOR_CODE, colorCode)
                startActivityForResult(intent, Const.RequestCodes.PROJECT_SHOP_QUOTATION)
            }

            R.id.etUnit -> {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra(Const.KEYS.SEARCH_TYPE, Const.SearchType.Units.toString())
                intent.putExtra(Const.KEYS.COLOR_CODE, colorCode)
                startActivityForResult(intent, Const.RequestCodes.UNITS)
            }

            R.id.etStockoutdate -> {
                setDateListener(this)
                showDatePickerDialog(true, Const.KEYS.DATE_DOB)
            }

            R.id.btnAddSubItem -> {
                if (checkValidation()) {
                    saveDataInDB()
                }
            }
        }

    }


    /* Add Data in DB*/
    private fun saveDataInDB() {
        showProgressDialog()
        if (!isEdit)
            subItemId =
                FirebaseDatabase.getInstance().getReference(Const.TableName.SUB_ITEM_QUOTATION)
                    .push().key.toString()

        subItemObj = SubItemQuotation(
            subItemId,
            stockNameId,
            locationId,
            shopId,
            etSpecification.text.toString().trim(),
            etDescription.text.toString().trim(),
            etQty.text.toString().trim(),
            unitId,
            etUP.text.toString().trim(),
            etDiscount.text.toString().trim(),
            etNP.text.toString().trim(),
            etLineTotal.text.toString().trim(),
            etStockoutdate.text.toString().trim()
        )

        var firebaseDbClient = FirebaseDbClient()
        if (subItemId != null) {
            firebaseDbClient.subItemQuotation.child(subItemId.toString()).setValue(subItemObj)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        hideProgressDialog()

                        if (!TextUtils.isEmpty(mainItemId)) {
                            getSubListFromMainItemId()
                        } else {
                            finish()
                        }


                    } else
                        hideProgressDialog()
                }
        } else
            hideProgressDialog()

    }

    private fun getSubListFromMainItemId() {
        showProgressDialog()
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.mainItemQuotation.child(mainItemId)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var obj: MainItemQuotation? =
                            dataSnapshot.getValue(MainItemQuotation::class.java)

                        var totalSubCount = obj?.total_sub_item!!.toInt()
                        var subItemList = obj?.stock_list

                        if (TextUtils.isEmpty(subItemList))
                            subItemList = subItemId
                        else
                            subItemList = subItemList + "," + subItemId

                        totalSubCount = totalSubCount + 1

                        mFirebaseClient.mainItemQuotation.child(mainItemId).child("stock_list")
                            .setValue(subItemList)
                        mFirebaseClient.mainItemQuotation.child(mainItemId).child("total_sub_item")
                            .setValue(totalSubCount.toString())

                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Const.RequestCodes.LOCATION -> {
                if (data != null) {
                    locationName = data.getStringExtra(Const.KEYS.SEARCH_TEXT)
                    locationId = data.getStringExtra(Const.KEYS.LOCATION_ID)
                    etLocation.setText(locationName)
                    Timber.e("locationId -- " + locationId)
                }
            }
            Const.RequestCodes.PROJECT_SHOP_QUOTATION -> {
                if (data != null) {
                    shopName = data.getStringExtra(Const.KEYS.SEARCH_TEXT)
                    shopId = data.getStringExtra(Const.KEYS.SHOP_ID)
                    etProjectShop.setText(shopName)
                    Timber.e("shopId -- " + shopId)
                }
            }
            Const.RequestCodes.UNITS -> {
                if (data != null) {
                    unitName = data.getStringExtra(Const.KEYS.SEARCH_TEXT)
                    unitId = data.getStringExtra(Const.KEYS.UNIT_ID)
                    etUnit.setText(unitName)
                    Timber.e("unitId -- " + unitId)
                }
            }
            Const.RequestCodes.STOCK_NAME -> {
                if (data != null) {
                    stockName = data.getStringExtra(Const.KEYS.SEARCH_TEXT)
                    stockNameId = data.getStringExtra(Const.KEYS.STOCK_ID)
                    etStockName.setText(stockName)
                    Timber.e("stockId -- " + stockNameId)
                }
            }
        }
    }

    private fun checkValidation(): Boolean {

        if (TextUtils.isEmpty(etStockName.text.toString().trim())) {
            showError(getString(R.string.enter_stock_name))
            return false
        }

        if (TextUtils.isEmpty(etLocation.text.toString().trim())) {
            showError(getString(R.string.enter_location))
            return false
        }

        if (TextUtils.isEmpty(etProjectShop.text.toString().trim())) {
            showError(getString(R.string.enter_shop))
            return false
        }
        if (TextUtils.isEmpty(etSpecification.text.toString().trim())) {
            showError(getString(R.string.enter_specification))
            return false
        }
        if (TextUtils.isEmpty(etDescription.text.toString().trim())) {
            showError(getString(R.string.enter_description))
            return false
        }
        if (TextUtils.isEmpty(etQty.text.toString().trim())) {
            showError(getString(R.string.enter_qty))
            return false
        }
        if (TextUtils.isEmpty(etUnit.text.toString().trim())) {
            showError(getString(R.string.enter_unit))
            return false
        }
        if (TextUtils.isEmpty(etUP.text.toString().trim())) {
            showError(getString(R.string.enter_up))
            return false
        }
        if (TextUtils.isEmpty(etDiscount.text.toString().trim())) {
            showError(getString(R.string.enter_discount))
            return false
        }
        if (TextUtils.isEmpty(etNP.text.toString().trim())) {
            showError(getString(R.string.enter_np))
            return false
        }
        if (TextUtils.isEmpty(etLineTotal.text.toString().trim())) {
            showError(getString(R.string.enter_line_total))
            return false
        }
        if (TextUtils.isEmpty(etStockoutdate.text.toString().trim())) {
            showError(getString(R.string.enter_stockout_date))
            return false
        }

        if (!isNetworkConnected) {
            showError(resources.getString(R.string.internet_error))
            return false
        }
        return true

    }

    override fun onDateSelected(finalDate: String?, dateType: String?) {
        etStockoutdate.setText(finalDate)
    }

}