package com.agile.kouti.quotation.subItem.quotation

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.agile.kouti.db.FirebaseDbClient
import com.agile.kouti.db.quotation.Quotation
import com.agile.kouti.quotation.subItem.main_item.MainItemListActivity
import com.agile.kouti.quotation.subItem.quotation_detail.QuotationDetailActivity
import com.agile.kouti.utils.Const
import com.agile.kouti.utils.Preferences
import com.agile.kouti.view_picture.ViewPictureActivity
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_quotation_list.*
import kotlinx.android.synthetic.main.search_layout.*
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class QuotationListActivity : KoutiBaseActivity(),View.OnClickListener,QuotationClickListener,KoutiBaseActivity.OnDialogClickListener {

    private val quotationList = ArrayList<Quotation>()
    private val selectedQuotationList = ArrayList<Quotation>()
    private val quotationListAdapter = QuotationListAdapter(this, quotationList, this)

    var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quotation_list)

        initData()
    }

    private fun initData() {

        tvTitle.text = getString(R.string.toolbar_title_quotation)
        ivSettings.visibility = View.GONE

        cvFilter.setOnClickListener(this)
        cvAdd.setOnClickListener(this)
        cvDelete.setOnClickListener(this)
        cvSelectAll.setOnClickListener(this)
        ivBack.setOnClickListener(this)
        ivSearch.setOnClickListener(this)
        ivCamera.setOnClickListener(this)

        userId = Preferences.getPreference(this@QuotationListActivity, Const.SharedPrefs.USER_ID)

        setQuotationListAdapter()

        swipeContainer.setOnRefreshListener {
            quotationList.clear()
            quotationListAdapter.notifyDataSetChanged()
            getQuotationData()
            swipeContainer.isRefreshing = false
        }


        getQuotationData()
    }

    private fun setQuotationListAdapter() {
        val layoutManager = LinearLayoutManager(this@QuotationListActivity, LinearLayoutManager.VERTICAL, false)
        rvQuotation.layoutManager = layoutManager
        rvQuotation.adapter = quotationListAdapter
    }

//    override fun onResume() {
//        super.onResume()
//        quotationList.clear()
//        quotationListAdapter.notifyDataSetChanged()
//        getQuotationData()
//    }

    private fun getQuotationData() {
        showProgressDialog()
        //val databaseReference= FirebaseDatabase.getInstance().getReference(Const.TableName.QUOTATION)

        var mFirebaseClientObj = FirebaseDbClient()
        val recentPostsQuery: Query = mFirebaseClientObj.quotation.orderByChild(Const.KEYS.USER_ID).equalTo(userId)
        recentPostsQuery.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (data in dataSnapshot.children) {
                        val l: Quotation? = data.getValue<Quotation>(Quotation::class.java)
                        if (l != null) {
                            quotationList.add(l)
                        }
                    }
                    quotationListAdapter.notifyDataSetChanged()
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
                val intent = Intent(this, AddQuotationActivity::class.java)
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


    /* Quotation List item Click */
    override fun onQuotationItemClick(list: Quotation) {
        val intent = Intent(this, QuotationDetailActivity::class.java)
        intent.putExtra(Const.KEYS.QUOTATION_ID, list.id)
        startActivity(intent)
    }

    override fun onItemSelectClick(list: Quotation, pos: Int) {
        list.is_selected = !list.is_selected!!
        quotationListAdapter.notifyDataSetChanged()
    }

    override fun onMainItemSelectClick(list: Quotation, pos: Int) {
        val intent = Intent(this, MainItemListActivity::class.java)
        intent.putExtra(Const.KEYS.MAIN_LIST, list.item_list)
        intent.putExtra(Const.KEYS.QUOTATION_ID, list.id)
        startActivity(intent)

    }


    /* Select All */
    private fun selectAllData() {
        if (quotationList.isEmpty()) {
            showError("No data found")
            return
        }
        for (x in quotationList)
            x.is_selected = true

        quotationListAdapter.notifyDataSetChanged()
    }


    /* Delete Function */
    private fun checkDataBeforeDelete() {
        if(quotationList.isEmpty())
            return

        for (list in quotationList) {
            if (list.is_selected!!) {
                selectedQuotationList.add(list)
            }
        }

        if (selectedQuotationList.isEmpty()) {
            showError(getString(R.string.select_one_quotation))
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
        for (list in selectedQuotationList) {
            var firebaseDbClient = FirebaseDbClient()
            firebaseDbClient.quotation.child(list.id.toString()).removeValue()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                    }
                }
        }
        quotationList.clear()
        quotationListAdapter.notifyDataSetChanged()
        getQuotationData()
    }


    /* Filter Data */
    private fun showFilterDialog() {
        val items = arrayOf(Const.KEYS.FILTER_DATE,Const.KEYS.FILTER_QUO_NO)
        AlertDialog.Builder(this)
            .setTitle("Filter By")
            .setCancelable(true)
            .setItems(items) { dialog, which ->
                sortData(items[which])
                dialog.cancel()
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.cancel()
            }
            .show()
    }

    private fun sortData(filterType: String) {

        when(filterType){

            Const.KEYS.FILTER_DATE ->{
                Collections.sort(quotationList, Comparator<Quotation> { lhs, rhs -> // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                    when {
                        lhs.created_date!!.toLowerCase() > rhs.created_date.toString().toLowerCase() -> -1
                        lhs.created_date!!.toLowerCase() > rhs.created_date.toString().toLowerCase() -> 1
                        else -> 0
                    }
                })
            }

            Const.KEYS.FILTER_QUO_NO ->{
                Collections.sort(quotationList, Comparator<Quotation> { lhs, rhs -> // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                    when {
                        lhs.quotation_no!!.toLowerCase() < rhs.quotation_no.toString().toLowerCase() -> -1
                        lhs.quotation_no!!.toLowerCase() < rhs.quotation_no.toString().toLowerCase() -> 1
                        else -> 0
                    }
                })
            }
        }

    }
}