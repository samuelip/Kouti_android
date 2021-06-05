package com.agile.kouti.invoice.invoice_list

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
import com.agile.kouti.db.invoice.Invoice
import com.agile.kouti.db.quotation.Quotation
import com.agile.kouti.invoice.main_item.MainItemInvoiceListActivity
import com.agile.kouti.quotation.subItem.main_item.MainItemListActivity
import com.agile.kouti.quotation.subItem.quotation.AddQuotationActivity
import com.agile.kouti.utils.Const
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_invoice_detail.*
import kotlinx.android.synthetic.main.activity_quotation_detail.*
import kotlinx.android.synthetic.main.toolbar.*

class InvoiceDetailActivity : KoutiBaseActivity(), View.OnClickListener  {

    var mainItem = ""

    var invoiceObj: Invoice? = null
    var invoiceId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invoice_detail)
        initData()
    }

    private fun initData() {
        tvToolbarTitle.text = resources.getString(R.string.toolbar_title_invoice)
        tvToolbarSubTitle.visibility = View.GONE
        ivEdit.visibility = View.VISIBLE

        ivBack.setOnClickListener(this)
        ivEdit.setOnClickListener(this)
        tvMainItemCountInvc.setOnClickListener(this)

        if(intent!=null)
            invoiceId = intent.getStringExtra(Const.KEYS.INVOICE_ID)
    }

    override fun onResume() {
        super.onResume()
        if(TextUtils.isEmpty(invoiceId))
            return
        getInvoiceData()
    }

    private fun getInvoiceData() {
        showProgressDialog()
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.invoice.child(invoiceId.toString())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var invoiceObjects: Invoice? = dataSnapshot.getValue(Invoice::class.java)
                        if (invoiceObjects != null) {
                            invoiceObj = invoiceObjects
                            mainItem = invoiceObjects.item_list!!
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

        tvDateInvc.setText(invoiceObj?.date)
        tvStatusInvc.setText(invoiceObj?.status)
        tvInvoiceNo.setText(invoiceObj?.invoice_no)

        getCustomerName()

        tvCreditTermDaysInvc.setText(""+invoiceObj?.credit_term_days)

        tvCorrAddressInvc.setText(invoiceObj?.correct_address)
        tvDeliAddressInvc.setText(invoiceObj?.delivery_address)
        tvCustomerPoNoInvc.setText(invoiceObj?.customer_po_no)

        tvDebiteNo.setText(invoiceObj?.debite_no)
        tvDebiteNoteNo.setText(invoiceObj?.delivery_note_no)
        tvPackingListNo.setText(invoiceObj?.packing_list_no)


        tvDescription1Invc.setText(invoiceObj?.description1)
        tvDescription2Invc.setText(invoiceObj?.description2)

        getCurrencyName()

        tvTotalAmountInvc.setText(invoiceObj?.total)
        tvMainItemCountInvc.setText("("+invoiceObj?.total_main_item+") Main Item")

    }

    /* Get Currency Name */
    private fun getCurrencyName() {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.currency.child(invoiceObj!!.currency!!)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var obj: CurrencyTable? = dataSnapshot.getValue(CurrencyTable::class.java)
                        if (obj != null) {
                            tvCurrencyInvc.setText(obj?.currency)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    /* Get Customer Name */
    private fun getCustomerName() {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.crm.child(invoiceObj!!.customer!!)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var obj: Crm? = dataSnapshot.getValue(Crm::class.java)
                        if (obj != null) {
                            tvCustomerInvc.setText(obj?.name)
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
                val intent = Intent(this, AddInvoiceActivity::class.java)
                intent.putExtra(Const.KEYS.INVOICE_ID,invoiceId)
                intent.putExtra(Const.KEYS.IS_EDIT,true)
                startActivity(intent)
            }

            R.id.tvMainItemCountInvc->{
                val intent = Intent(this, MainItemInvoiceListActivity::class.java)
                intent.putExtra(Const.KEYS.INVOICE_ID,invoiceId)
                intent.putExtra(Const.KEYS.MAIN_LIST,invoiceObj!!.item_list)
                startActivity(intent)
            }
        }
    }

}