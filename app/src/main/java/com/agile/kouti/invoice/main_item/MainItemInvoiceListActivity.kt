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
import com.agile.kouti.db.quotation.MainItemQuotation
import com.agile.kouti.db.quotation.Quotation
import com.agile.kouti.invoice.invoice_list.InvoiceListAdapter
import com.agile.kouti.invoice.invoice_list.MainItemInvoiceAdapter
import com.agile.kouti.invoice.invoice_list.MainItemInvoiceClickListener
import com.agile.kouti.invoice.sub_item.SubItemInvoiceListActivity
import com.agile.kouti.quotation.subItem.main_item.MainItemDetailActivity
import com.agile.kouti.quotation.subItem.main_item.MainItemQuotationActivity
import com.agile.kouti.quotation.subItem.main_item.QuotationMainListAdapter
import com.agile.kouti.quotation.subItem.quotation.MainItemClickListener
import com.agile.kouti.quotation.subItem.sub_item.SubItemQuotationListActivity
import com.agile.kouti.utils.Const
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main_item_invoice_list.*
import kotlinx.android.synthetic.main.activity_main_item_list.*
import kotlinx.android.synthetic.main.toolbar.*
import timber.log.Timber
import java.util.ArrayList

class MainItemInvoiceListActivity : KoutiBaseActivity(), View.OnClickListener,
    MainItemInvoiceClickListener {


    var mainItem = ""
    var invoiceId = ""
    var totalMainItem = 0

    private var mainItemList = ArrayList<MainItemInvoice>()
    private val selectedMainItemList = ArrayList<MainItemInvoice>()
    private val mainItemAdapter = InvoiceMainListAdapter(this, mainItemList, this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_item_invoice_list)

        initData()
    }

    private fun initData() {
        tvToolbarTitle.text = resources.getString(R.string.toolbar_title_invoice)
        tvToolbarSubTitle.text = "Main Item"
        tvToolbarSubTitle.visibility = View.VISIBLE
        ivEdit.visibility = View.INVISIBLE

        ivBack.setOnClickListener(this)
        cvAddMain.setOnClickListener(this)
        cvDeleteMain.setOnClickListener(this)
        cvSelectAllMain.setOnClickListener(this)
        ivEdit.setOnClickListener(this)

        if (intent != null) {
            mainItem = intent.getStringExtra(Const.KEYS.MAIN_LIST)
            invoiceId = intent.getStringExtra(Const.KEYS.INVOICE_ID)

            Timber.e("mainItem -- " + mainItem)
            Timber.e("quotationId -- " + invoiceId)

            if (TextUtils.isEmpty(invoiceId))
                return

            setMainItemAdapter()

            //getInvoiceData()

            swipeContainerMainInv.setOnRefreshListener {
                mainItemList.clear()
                selectedMainItemList.clear()
                mainItemAdapter.notifyDataSetChanged()
                getInvoiceData()
                swipeContainerMainInv.setRefreshing(false)
            }
        }
    }

    private fun getInvoiceData() {
        showProgressDialog()
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.invoice.child(invoiceId.toString())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var quotationObj: Quotation? = dataSnapshot.getValue(Quotation::class.java)
                        if (quotationObj != null) {

                            mainItem = quotationObj.item_list!!

                            totalMainItem = quotationObj.total_main_item!!.toInt()

                            if (!TextUtils.isEmpty(mainItem)) {
                                getData()

                            }
                            hideProgressDialog()
                        }
                    } else {
                        hideProgressDialog()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun getData() {
        var result = mainItem!!.split(",").map { it.trim() }
        result.forEach {
            Timber.e("id -- " + it)
            if (!TextUtils.isEmpty(it))
                getMainListFromIds(it)
        }

    }

    /* get Main List from Id */
    private fun getMainListFromIds(mainListId: String) {
        Timber.e("mainListId -- " + mainListId)
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.mainItemInvoice.child(mainListId.toString())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var mainListObj: MainItemInvoice? = dataSnapshot.getValue(MainItemInvoice::class.java)
                        if (mainListObj != null) {
                            mainItemList.add(mainListObj)
                        }
                        mainItemAdapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onResume() {
        super.onResume()
        mainItemList.clear()
        selectedMainItemList.clear()
        mainItemAdapter.notifyDataSetChanged()
        getInvoiceData()
    }

    private fun setMainItemAdapter() {
        val layoutManager = LinearLayoutManager(
            this@MainItemInvoiceListActivity,
            LinearLayoutManager.VERTICAL,
            false
        )
        rvMainListInv.layoutManager = layoutManager
        rvMainListInv.adapter = mainItemAdapter
    }


    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.ivBack -> {
                finish()
            }
            R.id.cvAddMain -> {
                val intent = Intent(this,MainItemInvoiceActivity::class.java)
                intent.putExtra(Const.KEYS.IS_EDIT, false)
                intent.putExtra(Const.KEYS.INVOICE_ID, invoiceId)
                startActivity(intent)
            }

            R.id.cvDeleteMain -> {
                selectedMainItemList.clear()
                for (list in mainItemList) {
                    if (list.is_selected!!) {
                        selectedMainItemList.add(list)
                    }
                }

                if (selectedMainItemList.isEmpty()) {
                    showError("Please select main Item")
                    return
                }

                if (mainItemList.size == selectedMainItemList.size) {
                    showError("You can't delete all item,it must have at least one main item")
                    return
                }
                deleteData()
            }

            R.id.cvSelectAllMain -> {
                selectAllData()
            }
        }
    }

    private fun deleteData() {
        for (list in selectedMainItemList) {

            mainItem = mainItem.replace(list.id.toString(), "")
            mainItem = mainItem.replace(",,", ",")

            // check first char , comma
            if (!TextUtils.isEmpty(mainItem)) {

                var firstChar: Char = mainItem[0]
                if (firstChar == ',') {
                    mainItem = mainItem.substring(1)
                }

                // check last char
                var lastChar: Char = mainItem[(mainItem.length - 1)]
                if (lastChar == ',') {
                    mainItem = mainItem.dropLast(1)
                }
            }
            totalMainItem = totalMainItem - 1

        }
        Timber.e("final mainItem -->> " + mainItem)

        var firebaseDbClient = FirebaseDbClient()
        firebaseDbClient.invoice.child(invoiceId).child("item_list").setValue(mainItem)
        firebaseDbClient.invoice.child(invoiceId).child("total_main_item").setValue(totalMainItem.toString())

        mainItemList.clear()
        mainItemAdapter.notifyDataSetChanged()
        getInvoiceData()

    }

    private fun selectAllData() {
        if (mainItemList.isEmpty()) {
            showError("No data found")
            return
        }
        for (x in mainItemList)
            x.is_selected = true
        mainItemAdapter.notifyDataSetChanged()
    }

    override fun onMainItemClick(list: MainItemInvoice) {
        val intent = Intent(this, MainItemInvoiceDetailActivity::class.java)
        intent.putExtra(Const.KEYS.MAIN_ITEM_ID_INVOICE, list.id)
        startActivity(intent)
    }

    override fun onItemSelectClick(list: MainItemInvoice, pos: Int) {
        list.is_selected = !list.is_selected!!
        mainItemAdapter.notifyDataSetChanged()
    }

    override fun onSubItemClick(list: MainItemInvoice) {
        Timber.e("onMainItemClick -- SubItemQuotationListActivity ")
        val intent = Intent(this, SubItemInvoiceListActivity::class.java)
        intent.putExtra(Const.KEYS.SUB_LIST, list.stock_list)
        intent.putExtra(Const.KEYS.MAIN_ITEM_ID_INVOICE, list.id)
        startActivity(intent)
    }
}