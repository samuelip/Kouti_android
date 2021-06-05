package com.agile.kouti.quotation.subItem.quotation_detail

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.agile.kouti.db.CurrencyTable
import com.agile.kouti.db.FirebaseDbClient
import com.agile.kouti.db.crm.Crm
import com.agile.kouti.db.quotation.Quotation
import com.agile.kouti.quotation.subItem.main_item.MainItemListActivity
import com.agile.kouti.quotation.subItem.quotation.AddQuotationActivity
import com.agile.kouti.utils.Const
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_add_quotation.*
import kotlinx.android.synthetic.main.activity_quotation_detail.*
import kotlinx.android.synthetic.main.toolbar.*
import timber.log.Timber

class QuotationDetailActivity : KoutiBaseActivity(),View.OnClickListener {

    var mainItem = ""

    var quotationObj: Quotation? = null
    var quotationId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quotation_detail)

        initData()

    }

    private fun initData() {
        tvToolbarTitle.text = resources.getString(R.string.toolbar_title_quotation)
        tvToolbarSubTitle.visibility = View.GONE
        ivEdit.visibility = View.VISIBLE

        ivBack.setOnClickListener(this)
        ivEdit.setOnClickListener(this)
        tvMainItemCount.setOnClickListener(this)

        if(intent!=null) {
            quotationId = intent.getStringExtra(Const.KEYS.QUOTATION_ID)

//            if(TextUtils.isEmpty(quotationId))
//                return
//            getQuotationData()
        }
    }

    override fun onResume() {
        super.onResume()
        if(TextUtils.isEmpty(quotationId))
            return
        getQuotationData()
    }

    override fun onClick(v: View?) {
       when(v?.id){
           R.id.ivBack->{finish()}

           R.id.ivEdit->{
               val intent = Intent(this, AddQuotationActivity::class.java)
               intent.putExtra(Const.KEYS.QUOTATION_ID,quotationId)
               intent.putExtra(Const.KEYS.IS_EDIT,true)
               startActivity(intent)
           }

           R.id.tvMainItemCount->{
               val intent = Intent(this, MainItemListActivity::class.java)
               intent.putExtra(Const.KEYS.QUOTATION_ID,quotationId)
               intent.putExtra(Const.KEYS.MAIN_LIST,quotationObj!!.item_list)
               startActivity(intent)
           }
       }
    }

    private fun getQuotationData() {
        showProgressDialog()
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.quotation.child(quotationId.toString())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var quotationObjects: Quotation? = dataSnapshot.getValue(Quotation::class.java)
                        if (quotationObjects != null) {
                            quotationObj = quotationObjects
                            mainItem = quotationObjects.item_list!!
                            setData()
                            hideProgressDialog()
                        }
                    }else{
                        hideProgressDialog()
                    }
                }
                override fun onCancelled(error: DatabaseError) {hideProgressDialog()}
            })

    }

    private fun setData() {

        tvDate.setText(quotationObj?.date)
        tvStatus.setText(quotationObj?.status)
        tvQuotationNo.setText(quotationObj?.quotation_no)

        getCustomerName()

        tvCreditTermDays.setText(""+quotationObj?.credit_term_days)

        tvCorrAddress.setText(quotationObj?.correct_address)
        tvDeliAddress.setText(quotationObj?.delivery_address)
        tvCustomerPoNo.setText(quotationObj?.customer_po_no)
        tvDescription1.setText(quotationObj?.description1)
        tvDescription2.setText(quotationObj?.description2)

        getCurrencyName()

        tvTotalAmount.setText(quotationObj?.total)
        tvMainItemCount.setText("("+quotationObj?.total_main_item+") Main Item")

    }


    /* Get Currency Name */
    private fun getCurrencyName() {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.currency.child(quotationObj!!.currency!!)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var obj: CurrencyTable? = dataSnapshot.getValue(CurrencyTable::class.java)
                        if (obj != null) {
                            tvCurrency.setText(obj?.currency)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    /* Get Customer Name */
    private fun getCustomerName() {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.crm.child(quotationObj!!.customer!!)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var obj: Crm? = dataSnapshot.getValue(Crm::class.java)
                        if (obj != null) {
                            tvCustomer.setText(obj?.name)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

    }
}