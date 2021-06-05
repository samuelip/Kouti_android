package com.agile.kouti.quotation.subItem.main_item

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.agile.kouti.db.FirebaseDbClient
import com.agile.kouti.db.quotation.MainItemQuotation
import com.agile.kouti.db.quotation.Quotation
import com.agile.kouti.quotation.subItem.quotation.MainItemAdapter
import com.agile.kouti.quotation.subItem.quotation.MainItemClickListener
import com.agile.kouti.quotation.subItem.sub_item.SubItemQuotationListActivity
import com.agile.kouti.utils.Const
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_add_quotation.*
import kotlinx.android.synthetic.main.activity_main_item_list.*
import kotlinx.android.synthetic.main.activity_quotation_list.*
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.toolbar.ivBack
import timber.log.Timber
import java.util.ArrayList

class MainItemListActivity : KoutiBaseActivity(), View.OnClickListener, MainItemClickListener {

    var mainItem = ""
    var quotationId = ""

    var totalMainItem = 0

    private var mainItemList = ArrayList<MainItemQuotation>()
    private val selectedMainItemList = ArrayList<MainItemQuotation>()
    private val mainItemAdapter = QuotationMainListAdapter(this, mainItemList, this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_item_list)

        initData()
    }

    private fun initData() {
        tvToolbarTitle.text = resources.getString(R.string.toolbar_title_quotation)
        tvToolbarSubTitle.text = "Main Item"
        tvToolbarSubTitle.visibility = View.VISIBLE
        ivEdit.visibility = View.GONE

        ivBack.setOnClickListener(this)
        cvAdd.setOnClickListener(this)
        cvDelete.setOnClickListener(this)
        cvSelectAll.setOnClickListener(this)
        ivEdit.setOnClickListener(this)

        if (intent != null) {
            mainItem = intent.getStringExtra(Const.KEYS.MAIN_LIST)
            quotationId = intent.getStringExtra(Const.KEYS.QUOTATION_ID)

            Timber.e("mainItem -- " + mainItem)
            Timber.e("quotationId -- " + quotationId)

            if (TextUtils.isEmpty(quotationId))
                return

            setMainItemAdapter()

            //getQuotationData()

            swipeContainerMain.setOnRefreshListener {
                mainItemList.clear()
                selectedMainItemList.clear()
                mainItemAdapter.notifyDataSetChanged()
                getQuotationData()
                swipeContainerMain.setRefreshing(false)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mainItemList.clear()
        selectedMainItemList.clear()
        mainItemAdapter.notifyDataSetChanged()
        getQuotationData()
    }

    private fun getQuotationData() {
        showProgressDialog()
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.quotation.child(quotationId.toString())
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
        mFirebaseClient.mainItemQuotation.child(mainListId.toString())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var mainListObj: MainItemQuotation? =
                            dataSnapshot.getValue(MainItemQuotation::class.java)
                        if (mainListObj != null) {
                            mainItemList.add(mainListObj)
                        }
                        mainItemAdapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun setMainItemAdapter() {
        val layoutManager =
            LinearLayoutManager(this@MainItemListActivity, LinearLayoutManager.VERTICAL, false)
        rvMainList.layoutManager = layoutManager
        rvMainList.adapter = mainItemAdapter
    }

    override fun onClick(v: View?) {

        when (v?.id) {

            R.id.ivBack -> {
                finish()
            }
            R.id.cvAdd -> {
                val intent = Intent(this, MainItemQuotationActivity::class.java)
                intent.putExtra(Const.KEYS.IS_EDIT, false)
                intent.putExtra(Const.KEYS.QUOTATION_ID, quotationId)
                startActivity(intent)
            }

            R.id.cvDelete -> {
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

            R.id.cvSelectAll -> {
                SelectAllData()
            }
        }
    }

    override fun onMainItemClick(list: MainItemQuotation) {
        val intent = Intent(this, MainItemDetailActivity::class.java)
        intent.putExtra(Const.KEYS.MAIN_ITEM_ID, list.id)
        startActivity(intent)

    }

    override fun onItemSelectClick(list: MainItemQuotation, pos: Int) {
        list.is_selected = !list.is_selected!!
        mainItemAdapter.notifyDataSetChanged()
    }

    override fun onSubItemClick(list: MainItemQuotation) {
        val intent = Intent(this, SubItemQuotationListActivity::class.java)
        intent.putExtra(Const.KEYS.SUB_LIST, list.stock_list)
        intent.putExtra(Const.KEYS.MAIN_ITEM_ID, list.id)
        startActivity(intent)

    }

    private fun SelectAllData() {
        if (mainItemList.isEmpty()) {
            showError("No data found")
            return
        }
        for (x in mainItemList)
            x.is_selected = true

        mainItemAdapter.notifyDataSetChanged()
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
        firebaseDbClient.quotation.child(quotationId).child("item_list").setValue(mainItem)
        firebaseDbClient.quotation.child(quotationId).child("total_main_item")
            .setValue(totalMainItem.toString())

        mainItemList.clear()
        mainItemAdapter.notifyDataSetChanged()
        getQuotationData()
    }
}