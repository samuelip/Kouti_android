package com.agile.kouti.invoice.invoice_list

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.agile.kouti.db.FirebaseDbClient
import com.agile.kouti.db.crm.Crm
import com.agile.kouti.db.invoice.Invoice
import com.agile.kouti.db.quotation.Quotation
import com.agile.kouti.invoice.main_item.MainItemInvoiceActivity
import com.agile.kouti.invoice.main_item.MainItemInvoiceListActivity
import com.agile.kouti.quotation.subItem.main_item.MainItemListActivity
import com.agile.kouti.quotation.subItem.quotation.AddQuotationActivity
import com.agile.kouti.quotation.subItem.quotation.QuotationListAdapter
import com.agile.kouti.quotation.subItem.quotation_detail.QuotationDetailActivity
import com.agile.kouti.utils.Const
import com.agile.kouti.utils.Preferences
import com.agile.kouti.view_picture.ViewPictureActivity
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_invoice_list.*
import kotlinx.android.synthetic.main.activity_quotation_list.*
import kotlinx.android.synthetic.main.search_layout.*

class InvoiceListActivity : KoutiBaseActivity(), View.OnClickListener,InvoiceClickListener,KoutiBaseActivity.OnDialogClickListener {

    private val invoiceList = ArrayList<Invoice>()
    private val selectedInvoiceList = ArrayList<Invoice>()
    private val invoiceListAdapter = InvoiceListAdapter(this,invoiceList, this)

    var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invoice_list)

        initData()
    }

    private fun initData() {
        tvTitle.text = getString(R.string.toolbar_title_invoice)
        ivSettings.visibility = View.GONE

        cvFilter.setOnClickListener(this)
        cvAdd.setOnClickListener(this)
        cvDelete.setOnClickListener(this)
        cvSelectAll.setOnClickListener(this)
        ivBack.setOnClickListener(this)
        ivSearch.setOnClickListener(this)
        ivCamera.setOnClickListener(this)

        userId = Preferences.getPreference(this@InvoiceListActivity, Const.SharedPrefs.USER_ID)

        setInvoiceListAdapter()

        swipeContainerInv.setOnRefreshListener {
            invoiceList.clear()
            invoiceListAdapter.notifyDataSetChanged()
            getInvoiceData()
            swipeContainerInv.isRefreshing = false
        }

        getInvoiceData()

        /* Search function */
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                //filter(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }
        })
    }


//    override fun onResume() {
//        super.onResume()
//        invoiceList.clear()
//        invoiceListAdapter.notifyDataSetChanged()
//        getInvoiceData()
//    }

    private fun setInvoiceListAdapter() {
        val layoutManager = LinearLayoutManager(this@InvoiceListActivity, LinearLayoutManager.VERTICAL, false)
        rvInvoice.layoutManager = layoutManager
        rvInvoice.adapter = invoiceListAdapter
    }

    private fun getInvoiceData() {
        showProgressDialog()
        //val databaseReference= FirebaseDatabase.getInstance().getReference(Const.TableName.INVOICE)

        var mFirebaseClientObj = FirebaseDbClient()
        val recentPostsQuery: Query = mFirebaseClientObj.invoice.orderByChild(Const.KEYS.USER_ID).equalTo(userId)
        recentPostsQuery.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (data in dataSnapshot.children) {
                        val l: Invoice? = data.getValue<Invoice>(Invoice::class.java)
                        if (l != null) {
                            invoiceList.add(l)
                        }
                    }
                    invoiceListAdapter.notifyDataSetChanged()
                    hideProgressDialog()
                }else{
                    hideProgressDialog()
                    showError("No Data Found")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                hideProgressDialog()
            }
        })

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.cvFilter -> {
                //showFilterDialog()
            }

            R.id.cvAdd -> {
                val intent = Intent(this,AddInvoiceActivity::class.java)
                intent.putExtra(Const.KEYS.IS_EDIT, false)
                startActivity(intent)
            }

            R.id.cvDelete -> {
                checkDataBeforeDelete()
            }

            R.id.cvSelectAll -> {
                selectAllData()
            }

            R.id.ivBack -> {
                finish()
            }

            R.id.ivCamera -> {
                val intent = Intent(this, ViewPictureActivity::class.java)
                startActivity(intent)
            }

            R.id.ivSettings -> {
            }
        }
    }




    /* Invoice List item Click */
    override fun onInvoiceItemClick(list: Invoice) {
        val intent = Intent(this, InvoiceDetailActivity::class.java)
        intent.putExtra(Const.KEYS.INVOICE_ID, list.id)
        startActivity(intent)
    }

    override fun onItemSelectClick(list: Invoice, pos: Int) {
        list.is_selected = !list.is_selected!!
        invoiceListAdapter.notifyDataSetChanged()
    }

    override fun onMainItemSelectClick(list: Invoice, pos: Int) {
        val intent = Intent(this, MainItemInvoiceListActivity::class.java)
        intent.putExtra(Const.KEYS.MAIN_LIST, list.item_list)
        intent.putExtra(Const.KEYS.INVOICE_ID, list.id)
        startActivity(intent)
    }

    /* Select All */
    private fun selectAllData() {
        if (invoiceList.isEmpty()) {
            showError("No data found")
            return
        }
        for (x in invoiceList)
            x.is_selected = true

        invoiceListAdapter.notifyDataSetChanged()
    }


    /* Search Function */
    fun filter(text: String) {
        val filteredSearchList: ArrayList<Invoice> = ArrayList()
        for (list in invoiceList) {
            if (list.invoice_no!!.toLowerCase().contains(text.toLowerCase())) {
                filteredSearchList.add(list)
            }
        }
        invoiceListAdapter.filterList(filteredSearchList)
    }

    /* Delete Function */
    private fun checkDataBeforeDelete() {
        if(invoiceList.isEmpty())
            return

        for (list in invoiceList) {
            if (list.is_selected!!) {
                selectedInvoiceList.add(list)
            }
        }

        if (selectedInvoiceList.isEmpty()) {
            showError(getString(R.string.select_one_invoice))
            return
        }
        setDialogListener(this)
        showTwoButtonDialog(getString(R.string.delete_item),getString(R.string.want_to_delete),getString(R.string.btn_yes),getString(R.string.btn_no))

    }

    override fun onPositiveClick() {
        deleteData()
    }

    override fun onNegativeClick() {}

    private fun deleteData() {
        for (list in selectedInvoiceList) {
            var firebaseDbClient = FirebaseDbClient()
            firebaseDbClient.invoice.child(list.id.toString()).removeValue()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                    }
                }
        }
        invoiceList.clear()
        invoiceListAdapter.notifyDataSetChanged()
        getInvoiceData()
    }

}