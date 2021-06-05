package com.agile.kouti.receipt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.agile.kouti.db.CurrencyTable
import com.agile.kouti.db.FirebaseDbClient
import com.agile.kouti.db.crm.Crm
import com.agile.kouti.db.invoice.Invoice
import com.agile.kouti.db.receipt.Receipt
import com.agile.kouti.db.receipt.ReceiptAccount
import com.agile.kouti.invoice.invoice_list.AddInvoiceActivity
import com.agile.kouti.utils.Const
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_add_receipt.*
import kotlinx.android.synthetic.main.activity_invoice_detail.*
import kotlinx.android.synthetic.main.activity_receipt_detail.*
import kotlinx.android.synthetic.main.toolbar.*
import timber.log.Timber
import java.util.ArrayList

class ReceiptDetailActivity : KoutiBaseActivity(), View.OnClickListener,AccountItemClickListener {

    var receiptId = ""
    var receiptObj: Receipt? = null

    var documentUrl = ""

    private var receiptAccountList = ArrayList<ReceiptAccount>()
    private val receiptAccountAdapter = ReceiptAccountAdapter(this, receiptAccountList, this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt_detail)

        initData()
    }

    private fun initData() {
        tvToolbarTitle.text = resources.getString(R.string.toolbar_title_receipt)
        tvToolbarSubTitle.visibility = View.GONE
        ivEdit.visibility = View.VISIBLE

        ivBack.setOnClickListener(this)
        ivEdit.setOnClickListener(this)

        if(intent!=null)
            receiptId = intent.getStringExtra(Const.KEYS.RECEIPT_ID)

        setAccountItemAdapter()
    }

    override fun onClick(v: View?) {

        when(v?.id) {
            R.id.ivBack -> {
                finish()
            }

            R.id.ivEdit -> {
                val intent = Intent(this, AddReceiptActivity::class.java)
                intent.putExtra(Const.KEYS.RECEIPT_ID, receiptId)
                intent.putExtra(Const.KEYS.IS_EDIT, true)
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if(TextUtils.isEmpty(receiptId))
            return
        getReceiptData()
    }

    /* Set Adapter */
    private fun setAccountItemAdapter() {
        val layoutManager = LinearLayoutManager(this@ReceiptDetailActivity, LinearLayoutManager.VERTICAL, false)
        rvReceiptList.layoutManager = layoutManager
        rvReceiptList.adapter = receiptAccountAdapter
    }

    private fun getReceiptData() {
        showProgressDialog()
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.receipt.child(receiptId.toString())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var obj: Receipt? = dataSnapshot.getValue(Receipt::class.java)
                        if (obj != null) {
                            receiptObj = obj
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

        tvDateRec.text = receiptObj?.date
        tvReceiptNo.text = receiptObj?.receipt_number
        tvInvoiceRec.text = receiptObj?.invoice
        tvCorrectAdd.text = receiptObj?.correct_address
        tvDescription.text = receiptObj?.description
        tvLocal.text = receiptObj?.local
        tvDiscount.text = receiptObj?.discount
        tvUnsettled.text = receiptObj?.unsettled
        tvReturn.text = receiptObj?.receipt_return
        tvReceive.text = receiptObj?.receive
        tvTotal.text = receiptObj?.total

        documentUrl = receiptObj?.upload_document!!

        getCustomerName()
        getCurrencyName()
        getInvoiceNumber()

        if (!TextUtils.isEmpty(receiptObj!!.receipt_accountIds!!)) {
            var result = receiptObj!!.receipt_accountIds!!.split(",").map { it.trim() }

            result.forEach {
                Timber.e("id -- " + it)
                if (!TextUtils.isEmpty(it))
                    getAccountListFromIds(it)
            }
        }
    }

    private fun getAccountListFromIds(accountId: String) {
        Timber.e("accountId -- " + accountId)
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.receiptAccount.child(accountId.toString())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var accountObj: ReceiptAccount? =
                            dataSnapshot.getValue(ReceiptAccount::class.java)
                        if (accountObj != null) {
                            receiptAccountList.add(accountObj)
//                            if (!TextUtils.isEmpty(accountObj.total_amount)) {
//                                totalAmount = totalAmount + accountObj.total_amount!!.toInt()
//                                etTotal.setText(totalAmount.toString())
//                            }
                            receiptAccountAdapter.notifyDataSetChanged()
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    /* Get Currency Name */
    private fun getCurrencyName() {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.currency.child(receiptObj!!.currency!!)
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
        mFirebaseClient.crm.child(receiptObj!!.customer!!)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var obj: Crm? = dataSnapshot.getValue(Crm::class.java)
                        if (obj != null) {
                            tvCustomerRec.setText(obj?.name)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

    }

    /* Get Invoice Number */
    private fun getInvoiceNumber() {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.invoice.child(receiptObj!!.invoice_no!!)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var obj: Invoice? = dataSnapshot.getValue(Invoice::class.java)
                        if (obj != null) {
                            tvInvoiceNoRec.text = obj.invoice_no
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

    }

    override fun onAccountItemClick(list: ReceiptAccount) {


    }

}