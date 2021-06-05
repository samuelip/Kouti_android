package com.agile.kouti.invoice.sub_item

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.agile.kouti.db.FirebaseDbClient
import com.agile.kouti.db.payroll.Shop
import com.agile.kouti.db.quotation.LocationDB
import com.agile.kouti.db.quotation.StockName
import com.agile.kouti.db.quotation.SubItemQuotation
import com.agile.kouti.db.quotation.Units
import com.agile.kouti.search.SearchActivity
import com.agile.kouti.utils.Const
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_add_invoice.*
import kotlinx.android.synthetic.main.activity_invoice_sub_item.*
import kotlinx.android.synthetic.main.activity_invoice_sub_item.etDescriptionInv
import kotlinx.android.synthetic.main.activity_invoice_sub_item.etDiscountInv
import kotlinx.android.synthetic.main.activity_invoice_sub_item.etLineTotalInv
import kotlinx.android.synthetic.main.activity_invoice_sub_item.etLocationInv
import kotlinx.android.synthetic.main.activity_invoice_sub_item.etProjectShopInv
import kotlinx.android.synthetic.main.activity_invoice_sub_item.etQtyInv
import kotlinx.android.synthetic.main.activity_invoice_sub_item.etSpecificationInv
import kotlinx.android.synthetic.main.activity_invoice_sub_item.etStockNameInv
import kotlinx.android.synthetic.main.activity_invoice_sub_item.etUPInv
import kotlinx.android.synthetic.main.activity_sub_item_quatation.*
import kotlinx.android.synthetic.main.activity_sub_item_quatation.etNP
import kotlinx.android.synthetic.main.toolbar.*
import timber.log.Timber

class InvoiceSubItemActivity : KoutiBaseActivity(), View.OnClickListener,KoutiBaseActivity.OnDateSelectedListener {

    /***
     *
     *  Note SubItemQuotation Table common for invoice sub item and quotation sub item *
     *
     * **/
    private var isEdit = false

    private var subInvoiceItemId = ""
    var subItemObj: SubItemQuotation? = null

    private var mainInvoiceItemId = ""

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
        setContentView(R.layout.activity_invoice_sub_item)
        initData()
    }

    private fun initData() {
        ivBack.setOnClickListener(this)
        etLocationInv.setOnClickListener(this)
        etProjectShopInv.setOnClickListener(this)
        btnAddInv.setOnClickListener(this)
        etUnitInv.setOnClickListener(this)
        etStockNameInv.setOnClickListener(this)
        etStockoutdateInv.setOnClickListener(this)

        if (intent != null) {
            isEdit = intent.getBooleanExtra(Const.KEYS.IS_EDIT, false)
        }

        if (isEdit) {
            subInvoiceItemId = intent.getStringExtra(Const.KEYS.SUB_ITEM_ID_INVOICE)

            tvToolbarTitle.text = resources.getString(R.string.toolbar_title_invoice)
            tvToolbarSubTitle.text = "Sub Item Edit"
            tvToolbarSubTitle.visibility = View.VISIBLE

            btnAddInv.text = "SAVE"

            if (!TextUtils.isEmpty(subInvoiceItemId)) {
                getSubItemData()
            }
        } else {
            tvToolbarTitle.text = resources.getString(R.string.toolbar_title_invoice)
            tvToolbarSubTitle.text = "Sub Item Add"
            tvToolbarSubTitle.visibility = View.VISIBLE

            /* this intent came from  SubItemQuotationListActivity
            *  add sub item from subItem List  */
            //mainItemId = intent.getStringExtra(Const.KEYS.MAIN_ITEM_ID_INVOICE)

        }
        colorCode = randomColor.toString()
    }

    /**
     *  Edit Module *
     */

    private fun getSubItemData() {
        showProgressDialog()
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.subItemQuotation.child(subInvoiceItemId).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    var obj: SubItemQuotation? = dataSnapshot.getValue(SubItemQuotation::class.java)
                    if(obj!=null) {
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
        Timber.e("Unit --- "+subItemObj!!.unit)
        getUnitName(subItemObj!!.unit)

        etSpecificationInv.setText(subItemObj!!.specification)
        etDescriptionInv.setText(subItemObj!!.description)
        etQtyInv.setText(subItemObj!!.quntity)

        etUPInv.setText(subItemObj!!.up)
        etDiscountInv.setText(subItemObj!!.discount)
        etNPInv.setText(subItemObj!!.np)
        etLineTotalInv.setText(subItemObj!!.line_total)
        etStockoutdateInv.setText(subItemObj!!.stock_date)


    }

    /* Get Stock Name From Id */
    private fun getStockNameFromID(id: String?) {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.stockName.child(id.toString()).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    var obj: StockName? = dataSnapshot.getValue(StockName::class.java)
                    if(obj!=null) {
                        etStockNameInv.setText(obj.name)
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
                    if(obj!=null) {
                        etLocationInv.setText(obj.name)
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
                    if(obj!=null) {
                        etProjectShopInv.setText(obj.name)
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
                    if(obj!=null) {
                        etUnitInv.setText(obj.name)
                        unitId = obj.id!!
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

    }


    override fun onClick(v: View?) {

        when (v?.id) {

            R.id.ivBack -> {
                finish()
            }

            R.id.etLocationInv -> {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra(Const.KEYS.SEARCH_TYPE, Const.SearchType.LocationInvoice.toString())
                intent.putExtra(Const.KEYS.COLOR_CODE, colorCode)
                startActivityForResult(intent, Const.RequestCodes.LOCATION_INVOICE)
            }

            R.id.etProjectShopInv -> {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra(
                    Const.KEYS.SEARCH_TYPE,
                    Const.SearchType.ProjectShopInvoice.toString()
                )
                intent.putExtra(Const.KEYS.COLOR_CODE, colorCode)
                startActivityForResult(intent, Const.RequestCodes.PROJECT_SHOP_INVOICE)
            }

            R.id.etUnitInv -> {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra(Const.KEYS.SEARCH_TYPE, Const.SearchType.UnitsInvoice.toString())
                intent.putExtra(Const.KEYS.COLOR_CODE, colorCode)
                startActivityForResult(intent, Const.RequestCodes.UNITS_INVOICE)
            }

            R.id.etStockNameInv -> {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra(Const.KEYS.SEARCH_TYPE, Const.SearchType.StockNameInvoice.toString())
                intent.putExtra(Const.KEYS.COLOR_CODE, colorCode)
                startActivityForResult(intent, Const.RequestCodes.STOCK_NAME_INVOICE)
            }

            R.id.etStockoutdateInv -> {
                setDateListener(this)
                showDatePickerDialog(true, Const.KEYS.DATE_DOB)
            }

            R.id.btnAddInv -> {
                hideKeyBoard(v)

                if(checkValidation()){
                    saveDataInDB()
                }
            }
        }

    }

    /* Add Data in DB*/
    private fun saveDataInDB() {
        showProgressDialog()
        if (!isEdit)
            subInvoiceItemId = FirebaseDatabase.getInstance().getReference(Const.TableName.SUB_ITEM_QUOTATION).push().key.toString()

        subItemObj = SubItemQuotation(
            subInvoiceItemId,
            stockNameId,
            locationId,
            shopId,
            etSpecificationInv.text.toString().trim(),
            etDescriptionInv.text.toString().trim(),
            etQtyInv.text.toString().trim(),
            unitId,
            etUPInv.text.toString().trim(),
            etDiscountInv.text.toString().trim(),
            etNPInv.text.toString().trim(),
            etLineTotalInv.text.toString().trim(),
            etStockoutdateInv.text.toString().trim()
        )

        var firebaseDbClient = FirebaseDbClient()
        if (subInvoiceItemId != null) {
            firebaseDbClient.subItemQuotation.child(subInvoiceItemId.toString()).setValue(subItemObj)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        hideProgressDialog()
                        finish()
//                        if(!TextUtils.isEmpty(mainItemId)){
//                            getSubListFromMainItemId()
//                        }else{
//                            finish()
//                        }
                    } else
                        hideProgressDialog()
                }
        } else
            hideProgressDialog()

    }

    /* Check validation */
    private fun checkValidation(): Boolean {

        if (TextUtils.isEmpty(etStockNameInv.text.toString().trim())) {
            showError(getString(R.string.enter_stock_name))
            return false
        }

        if (TextUtils.isEmpty(etLocationInv.text.toString().trim())) {
            showError(getString(R.string.enter_location))
            return false
        }

        if (TextUtils.isEmpty(etProjectShopInv.text.toString().trim())) {
            showError(getString(R.string.enter_shop))
            return false
        }

        if (TextUtils.isEmpty(etSpecificationInv.text.toString().trim())) {
            showError(getString(R.string.enter_specification))
            return false
        }
        if (TextUtils.isEmpty(etDescriptionInv.text.toString().trim())) {
            showError(getString(R.string.enter_description))
            return false
        }
        if (TextUtils.isEmpty(etQtyInv.text.toString().trim())) {
            showError(getString(R.string.enter_qty))
            return false
        }
        if (TextUtils.isEmpty(etUnitInv.text.toString().trim())) {
            showError(getString(R.string.enter_unit))
            return false
        }
        if (TextUtils.isEmpty(etUPInv.text.toString().trim())) {
            showError(getString(R.string.enter_up))
            return false
        }
        if (TextUtils.isEmpty(etDiscountInv.text.toString().trim())) {
            showError(getString(R.string.enter_discount))
            return false
        }
        if (TextUtils.isEmpty(etNPInv.text.toString().trim())) {
            showError(getString(R.string.enter_np))
            return false
        }
        if (TextUtils.isEmpty(etLineTotalInv.text.toString().trim())) {
            showError(getString(R.string.enter_line_total))
            return false
        }

        if (TextUtils.isEmpty(etStockoutdateInv.text.toString().trim())) {
            showError(getString(R.string.enter_stockout_date))
            return false
        }

        if (!isNetworkConnected) {
            showError(resources.getString(R.string.internet_error))
            return false
        }

        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {

            Const.RequestCodes.PROJECT_SHOP_INVOICE -> {
                if (data != null) {
                    shopName = data!!.getStringExtra(Const.KEYS.SEARCH_TEXT)
                    shopId = data!!.getStringExtra(Const.KEYS.SHOP_ID)
                    etProjectShopInv.setText(shopName)
                    Timber.e("shopId -- " + shopId)
                }
            }

            Const.RequestCodes.STOCK_NAME_INVOICE -> {
                if (data != null) {
                    stockName = data!!.getStringExtra(Const.KEYS.SEARCH_TEXT)
                    stockNameId = data!!.getStringExtra(Const.KEYS.STOCK_ID)
                    etStockNameInv.setText(stockName)
                    Timber.e("stockId -- " + stockNameId)
                }
            }

            Const.RequestCodes.LOCATION_INVOICE -> {
                if (data != null) {
                    locationName = data!!.getStringExtra(Const.KEYS.SEARCH_TEXT)
                    locationId = data!!.getStringExtra(Const.KEYS.LOCATION_ID)
                    etLocationInv.setText(locationName)
                    Timber.e("locationId -- " + locationId)
                }
            }

            Const.RequestCodes.UNITS_INVOICE -> {
                if (data != null) {
                    unitName = data!!.getStringExtra(Const.KEYS.SEARCH_TEXT)
                    unitId = data!!.getStringExtra(Const.KEYS.UNIT_ID)
                    etUnitInv.setText(unitName)
                    Timber.e("unitId -- " + unitId)
                }
            }
        }
    }

    override fun onDateSelected(finalDate: String?, dateType: String?) {
        etStockoutdateInv.setText(finalDate)
    }


}