package com.agile.kouti.invoice.main_item

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.agile.kouti.db.FirebaseDbClient
import com.agile.kouti.db.invoice.MainItemInvoice
import com.agile.kouti.db.payroll.Shop
import com.agile.kouti.db.quotation.MainItemQuotation
import com.agile.kouti.invoice.sub_item.SubItemInvoiceListActivity
import com.agile.kouti.quotation.subItem.main_item.MainItemQuotationActivity
import com.agile.kouti.quotation.subItem.sub_item.SubItemQuotationListActivity
import com.agile.kouti.utils.Const
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main_item_detail.*
import kotlinx.android.synthetic.main.activity_main_item_invoice_detail.*
import kotlinx.android.synthetic.main.toolbar.*

class MainItemInvoiceDetailActivity : KoutiBaseActivity() , View.OnClickListener {

    var itemId = ""
    var mainItemObj: MainItemInvoice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_item_invoice_detail)
        initData()
    }

    private fun initData() {
        tvToolbarTitle.text = resources.getString(R.string.toolbar_title_invoice)
        tvToolbarSubTitle.text = "Main Item"
        tvToolbarSubTitle.visibility = View.VISIBLE
        ivEdit.visibility = View.VISIBLE

        ivBack.setOnClickListener(this)
        llSubCountInv.setOnClickListener(this)
        ivEdit.setOnClickListener(this)

        if(intent!=null) {
            itemId = intent.getStringExtra(Const.KEYS.MAIN_ITEM_ID_INVOICE)

        }
    }

    override fun onResume() {
        super.onResume()
        if(TextUtils.isEmpty(itemId))
            return

        getMainItemData()
    }

    private fun getMainItemData() {
        showProgressDialog()
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.mainItemInvoice.child(itemId.toString())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var mainItemObjects: MainItemInvoice? = dataSnapshot.getValue(MainItemInvoice::class.java)
                        if (mainItemObjects != null) {
                            mainItemObj = mainItemObjects
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
        tvItemInv.setText(mainItemObj?.item)
        tvDescriptionInv.setText(mainItemObj?.description)

        tvProjectShopInv.setText(mainItemObj?.project)

        tvAmountInv.setText(mainItemObj?.amount)
        tvDiscountInv.setText(mainItemObj?.discount)
        tvNetAmountInv.setText(mainItemObj?.net_amount)

        tvSubStockItemInv.setText("("+mainItemObj?.total_sub_item+") Sub Stock Item")

        getShopName()

    }

    private fun getShopName() {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.shop.child(mainItemObj!!.project!!)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var obj: Shop? = dataSnapshot.getValue(Shop::class.java)
                        if (obj != null) {
                            tvProjectShopInv.setText(obj?.name)

                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }



    override fun onClick(v: View?) {
        when(v?.id){

            R.id.ivBack->{finish()}

            R.id.ivEdit->{
                val intent = Intent(this, MainItemInvoiceActivity::class.java)
                intent.putExtra(Const.KEYS.IS_EDIT,true)
                intent.putExtra(Const.KEYS.MAIN_ITEM_ID_INVOICE, itemId)
                startActivity(intent)
            }

            R.id.llSubCountInv->{
                val intent = Intent(this, SubItemInvoiceListActivity::class.java)
                intent.putExtra(Const.KEYS.SUB_LIST, mainItemObj?.stock_list)
                intent.putExtra(Const.KEYS.MAIN_ITEM_ID_INVOICE, mainItemObj?.id)
                startActivity(intent)
            }
        }

    }
}