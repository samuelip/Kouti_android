package com.agile.kouti.receipt

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.agile.kouti.db.FirebaseDbClient
import com.agile.kouti.db.invoice.Invoice
import com.agile.kouti.db.receipt.Receipt
import com.agile.kouti.invoice.invoice_list.InvoiceListAdapter
import com.agile.kouti.utils.Const
import com.agile.kouti.utils.Preferences
import com.agile.kouti.view_picture.ViewPictureActivity
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_invoice_list.*
import kotlinx.android.synthetic.main.activity_receipt_list.*
import kotlinx.android.synthetic.main.search_layout.*

class ReceiptListActivity :KoutiBaseActivity(), View.OnClickListener,ReceiptClickListener,KoutiBaseActivity.OnDialogClickListener {

    private val receiptList = ArrayList<Receipt>()
    private val selectedReceiptList = ArrayList<Receipt>()
    private val receiptListAdapter = ReceiptListAdapter(this,receiptList, this)

    var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt_list)
        initData()
    }

    private fun initData() {

        tvTitle.text = getString(R.string.toolbar_title_receipt)
        ivSettings.visibility = View.GONE

        cvFilter.setOnClickListener(this)
        cvAdd.setOnClickListener(this)
        cvDelete.setOnClickListener(this)
        cvSelectAll.setOnClickListener(this)
        ivBack.setOnClickListener(this)
        ivSearch.setOnClickListener(this)
        ivCamera.setOnClickListener(this)

        userId = Preferences.getPreference(this@ReceiptListActivity, Const.SharedPrefs.USER_ID)

        setReceiptListAdapter()

        getReceiptData()

        swipeReceipt.setOnRefreshListener {
            receiptList.clear()
            receiptListAdapter.notifyDataSetChanged()
            getReceiptData()
            swipeReceipt.isRefreshing = false
        }
    }

    private fun setReceiptListAdapter() {
        val layoutManager = LinearLayoutManager(this@ReceiptListActivity, LinearLayoutManager.VERTICAL, false)
        rvReceipt.layoutManager = layoutManager
        rvReceipt.adapter = receiptListAdapter
    }

    private fun getReceiptData() {
        showProgressDialog()
        //val databaseReference= FirebaseDatabase.getInstance().getReference(Const.TableName.RECEIPT)

        var mFirebaseClientObj = FirebaseDbClient()
        val recentPostsQuery: Query = mFirebaseClientObj.receipt.orderByChild(Const.KEYS.USER_ID).equalTo(userId)
        recentPostsQuery.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (data in dataSnapshot.children) {
                        val l: Receipt? = data.getValue<Receipt>(Receipt::class.java)
                        if (l != null) {
                            receiptList.add(l)
                        }
                    }
                    receiptListAdapter.notifyDataSetChanged()
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
                val intent = Intent(this, AddReceiptActivity::class.java)
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



    /* Receipt List item Click */
    override fun onReceiptItemClick(list: Receipt) {
        val intent = Intent(this, ReceiptDetailActivity::class.java)
        intent.putExtra(Const.KEYS.RECEIPT_ID, list.id)
        startActivity(intent)
    }

    override fun onItemSelectClick(list: Receipt, pos: Int) {
        list.is_selected = !list.is_selected!!
        receiptListAdapter.notifyDataSetChanged()
    }

    /* Delete Function */
    private fun checkDataBeforeDelete() {
        if(receiptList.isEmpty())
            return

        for (list in receiptList) {
            if (list.is_selected!!) {
                selectedReceiptList.add(list)
            }
        }

        if (selectedReceiptList.isEmpty()) {
            showError(getString(R.string.select_one_receipt))
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
        for (list in selectedReceiptList) {
            var firebaseDbClient = FirebaseDbClient()
            firebaseDbClient.receipt.child(list.id.toString()).removeValue()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                    }
                }
        }
        receiptList.clear()
        receiptListAdapter.notifyDataSetChanged()
        getReceiptData()
    }




    /* Select All */
    private fun selectAllData() {
        if (receiptList.isEmpty()) {
            showError("No data found")
            return
        }
        for (x in receiptList)
            x.is_selected = true

        receiptListAdapter.notifyDataSetChanged()
    }


}