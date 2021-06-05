package com.agile.kouti.quotation.subItem.sub_item

import android.content.Intent
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
import com.agile.kouti.quotation.subItem.main_item.MainItemQuotationActivity
import com.agile.kouti.utils.Const
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_sub_item_detail.*
import kotlinx.android.synthetic.main.toolbar.*

class SubItemDetailActivity : KoutiBaseActivity(), View.OnClickListener {

    var subId = ""
    var subItemObj: SubItemQuotation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sub_item_detail)
        initData()
    }

    private fun initData() {
        tvToolbarTitle.text = resources.getString(R.string.toolbar_title_quotation)
        tvToolbarSubTitle.text = "Sub Item"
        tvToolbarSubTitle.visibility = View.VISIBLE
        ivEdit.visibility = View.VISIBLE

        ivBack.setOnClickListener(this)
        ivEdit.setOnClickListener(this)

        if(intent!=null) {
            subId = intent.getStringExtra(Const.KEYS.SUB_ITEM_ID)
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


        tvSpecification.setText(subItemObj?.specification)
        tvDescriptionSub.setText(subItemObj?.description)
        tvQty.setText(subItemObj?.quntity)

        getUnitName()

        tvUp.setText(subItemObj?.up)
        tvDiscountSub.setText(subItemObj?.discount)
        tvNp.setText(subItemObj?.np)
        tvLineTotal.setText(subItemObj?.line_total)
        tvStockOutDate.setText(subItemObj?.stock_date)
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
                            tvStockName.setText(obj?.name)
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
                            tvLocation.setText(obj?.name)
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
                            tvUnits.setText(obj?.name)
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
                val intent = Intent(this, SubItemQuotationActivity::class.java)
                intent.putExtra(Const.KEYS.IS_EDIT,true)
                intent.putExtra(Const.KEYS.SUB_ITEM_ID, subId)
                startActivity(intent)
            }
        }
    }
}