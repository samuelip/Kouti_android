package com.agile.kouti.invoice.sub_item

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.agile.kouti.db.FirebaseDbClient
import com.agile.kouti.db.quotation.LocationDB
import com.agile.kouti.db.quotation.StockName
import com.agile.kouti.db.quotation.SubItemQuotation
import com.agile.kouti.db.quotation.Units
import com.agile.kouti.quotation.subItem.sub_item.SubItemQuotationActivity
import com.agile.kouti.utils.Const
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_sub_item_detail.*
import kotlinx.android.synthetic.main.activity_sub_item_invoice_detail.*
import kotlinx.android.synthetic.main.toolbar.*

class SubItemInvoiceDetailActivity :  KoutiBaseActivity(), View.OnClickListener{

    var subId = ""
    var subItemObj: SubItemQuotation? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sub_item_invoice_detail)
        initData()
    }

    private fun initData() {
        tvToolbarTitle.text = resources.getString(R.string.toolbar_title_invoice)
        tvToolbarSubTitle.text = "Sub Item"
        tvToolbarSubTitle.visibility = View.VISIBLE
        ivEdit.visibility = View.VISIBLE

        ivBack.setOnClickListener(this)
        ivEdit.setOnClickListener(this)

        if(intent!=null) {
            subId = intent.getStringExtra(Const.KEYS.SUB_ITEM_ID_INVOICE)
        }
    }

    override fun onResume() {
        super.onResume()
        if(TextUtils.isEmpty(subId))
            return
        getSubItemData()
    }

    private fun getSubItemData() {
        showProgressDialog()
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.subItemQuotation.child(subId.toString())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var subItemObjects: SubItemQuotation? = dataSnapshot.getValue(SubItemQuotation::class.java)
                        if (subItemObjects != null) {
                            subItemObj = subItemObjects
                            hideProgressDialog()
                            setData()
                        }
                    }else{
                        hideProgressDialog()
                    }
                }
                override fun onCancelled(error: DatabaseError) {hideProgressDialog()}
            })
    }

    private fun setData() {

        getStockName()

        getLocationName()


        tvSpecificationInv.setText(subItemObj?.specification)
        tvDescriptionSubInv.setText(subItemObj?.description)
        tvQtyInv.setText(subItemObj?.quntity)

        getUnitName()

        tvUpInv.setText(subItemObj?.up)
        tvDiscountSubInv.setText(subItemObj?.discount)
        tvNpInv.setText(subItemObj?.np)
        tvLineTotalInv.setText(subItemObj?.line_total)
        tvStockOutDateInv.setText(subItemObj?.stock_date)
    }

    private fun getStockName() {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.stockName.child(subItemObj?.stock_name!!)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var obj: StockName? = dataSnapshot.getValue(StockName::class.java)
                        if (obj != null) {
                            tvStockNameInv.setText(obj?.name)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun getLocationName() {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.location.child(subItemObj?.location!!)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var obj: LocationDB? = dataSnapshot.getValue(LocationDB::class.java)
                        if (obj != null) {
                            tvLocationInv.setText(obj?.name)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun getUnitName() {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.unit.child(subItemObj?.unit!!)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var obj: Units? = dataSnapshot.getValue(Units::class.java)
                        if (obj != null) {
                            tvUnitsInv.setText(obj?.name)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }


    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.ivBack -> {
                finish()
            }

            R.id.ivEdit -> {
                val intent = Intent(this,InvoiceSubItemActivity::class.java)
                intent.putExtra(Const.KEYS.IS_EDIT,true)
                intent.putExtra(Const.KEYS.SUB_ITEM_ID_INVOICE, subId)
                startActivity(intent)
            }
        }
    }
}