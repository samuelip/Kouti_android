package com.agile.kouti.quotation.subItem.sub_item

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.agile.kouti.db.FirebaseDbClient
import com.agile.kouti.db.quotation.LocationDB
import com.agile.kouti.db.quotation.MainItemQuotation
import com.agile.kouti.db.quotation.StockName
import com.agile.kouti.db.quotation.SubItemQuotation
import com.agile.kouti.quotation.subItem.main_item.MainItemQuotationActivity
import com.agile.kouti.quotation.subItem.main_item.QuotationMainListAdapter
import com.agile.kouti.quotation.subItem.main_item.SubItemClickListener
import com.agile.kouti.utils.Const
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main_item_list.*
import kotlinx.android.synthetic.main.activity_main_item_list.cvAdd
import kotlinx.android.synthetic.main.activity_main_item_list.cvDelete
import kotlinx.android.synthetic.main.activity_main_item_list.cvSelectAll
import kotlinx.android.synthetic.main.activity_quotation_list.*
import kotlinx.android.synthetic.main.activity_sub_item_detail.*
import kotlinx.android.synthetic.main.activity_sub_item_quotation_list.*
import kotlinx.android.synthetic.main.toolbar.*
import timber.log.Timber
import java.util.ArrayList

class SubItemQuotationListActivity : KoutiBaseActivity(), View.OnClickListener,
    SubItemClickListener {

    var subItem = ""
    var mainItemId = ""
    var totalSubCount = 0

    private var subItemList = ArrayList<SubItemQuotation>()
    private var subItemListTemp = ArrayList<SubItemQuotation>()
    private val selectedSubItemList = ArrayList<SubItemQuotation>()
    private val subItemAdapter = QuotationSubListAdapter(this, subItemList, this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sub_item_quotation_list)
        initData()
    }

    private fun initData() {

        tvToolbarTitle.text = resources.getString(R.string.toolbar_title_quotation)
        tvToolbarSubTitle.text = "Sub Item"
        tvToolbarSubTitle.visibility = View.VISIBLE

        ivBack.setOnClickListener(this)
        cvAdd.setOnClickListener(this)
        cvDelete.setOnClickListener(this)
        cvSelectAll.setOnClickListener(this)

        if (intent != null) {
            subItem = intent.getStringExtra(Const.KEYS.SUB_LIST)
            mainItemId = intent.getStringExtra(Const.KEYS.MAIN_ITEM_ID)
            Timber.e("subItem -- " + subItem)
            Timber.e("mainItemId -- " + mainItemId)

            setSubItemAdapter()

            getSubListFromMainItemId()

            swipeContainerSub.setOnRefreshListener {
                subItemList.clear()
                subItemListTemp.clear()
                selectedSubItemList.clear()
                subItemAdapter.notifyDataSetChanged()
                getSubListFromMainItemId()
                swipeContainerSub.setRefreshing(false)
            }

        }
    }

    override fun onResume() {
        super.onResume()
        if (TextUtils.isEmpty(mainItemId))
            return
//        getSubListFromMainItemId()

    }

    private fun getSubListFromMainItemId() {
        showProgressDialog()
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.mainItemQuotation.child(mainItemId)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var obj: MainItemQuotation? =
                            dataSnapshot.getValue(MainItemQuotation::class.java)

                        totalSubCount = obj?.total_sub_item!!.toInt()

                        var result = obj?.stock_list!!.split(",").map { it.trim() }

                        result.forEach {

                            Timber.e("id -- " + it)

                            if (!TextUtils.isEmpty(it))
                                getSubListFromIds(it)
                        }
                        hideProgressDialog()
                    } else
                        hideProgressDialog()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun getSubListFromIds(subListId: String) {
        Timber.e("subListId -- " + subListId)
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.subItemQuotation.child(subListId.toString())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var subListObj: SubItemQuotation? =
                            dataSnapshot.getValue(SubItemQuotation::class.java)
                        if (subListObj != null) {
                            subItemListTemp.add(subListObj)


                            /*Stock Name */
                            var mFirebaseClient = FirebaseDbClient()
                            mFirebaseClient.stockName.child(subListObj?.stock_name!!)
                                .addListenerForSingleValueEvent(object :
                                    ValueEventListener {
                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            var obj: StockName? =
                                                dataSnapshot.getValue(StockName::class.java)
                                            if (obj != null) {
                                                subListObj.stock_name = obj.name


                                                /* Location Name */
                                                var mFirebaseClient = FirebaseDbClient()
                                                mFirebaseClient.location.child(subListObj?.location!!)
                                                    .addListenerForSingleValueEvent(object :
                                                        ValueEventListener {
                                                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                            if (dataSnapshot.exists()) {
                                                                var obj: LocationDB? =
                                                                    dataSnapshot.getValue(
                                                                        LocationDB::class.java
                                                                    )
                                                                if (obj != null) {

                                                                    subListObj.location = obj.name
                                                                    subItemList.add(subListObj)

                                                                    subItemAdapter.notifyDataSetChanged()
                                                                }
                                                            }
                                                        }

                                                        override fun onCancelled(error: DatabaseError) {}
                                                    })
                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

    }

    private fun setSubItemAdapter() {
        val layoutManager = LinearLayoutManager(
            this@SubItemQuotationListActivity,
            LinearLayoutManager.VERTICAL,
            false
        )
        rvSubList.layoutManager = layoutManager
        rvSubList.adapter = subItemAdapter
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.ivBack -> {
                finish()
            }

            R.id.cvAdd -> {
                val intent = Intent(this, SubItemQuotationActivity::class.java)
                intent.putExtra(Const.KEYS.IS_EDIT, false)
                intent.putExtra(Const.KEYS.MAIN_ITEM_ID, mainItemId)
                startActivity(intent)
            }

            R.id.cvDelete -> {
                selectedSubItemList.clear()

                for (list in subItemList) {
                    if (list.is_selected!!) {
                        selectedSubItemList.add(list)
                    }
                }

                if (selectedSubItemList.isEmpty()) {
                    showError("Please select sub item")
                    return
                }

                if(subItemList.size == selectedSubItemList.size) {
                    showError("You can't delete all Sub item,it must have at least one sub item")
                    return
                }
                Timber.e("dsfcsdhfohfods")
                deleteData()
            }

            R.id.cvSelectAll -> {
                SelectAllData()
            }
        }

    }

    private fun SelectAllData() {
        if (subItemList.isEmpty()) {
            showError("No data found")
            return
        }

        for (x in subItemList)
            x.is_selected = true

        subItemAdapter.notifyDataSetChanged()
    }

    private fun deleteData() {


        for (list in selectedSubItemList) {

            subItem = subItem.replace(list.id.toString(), "")
            subItem = subItem.replace(",,", ",")

            // check first char , comma
            var firstChar: Char = subItem[0]
            if (firstChar == ',') {
                subItem = subItem.substring(1)
            }
            // check last char , comma
            var lastChar: Char = subItem[(subItem.length-1)]
            if (lastChar == ',') {
                subItem = subItem.dropLast(1)
            }
            totalSubCount = totalSubCount - 1
        }

        var firebaseDbClient = FirebaseDbClient()
        firebaseDbClient.mainItemQuotation.child(mainItemId).child("stock_list").setValue(subItem)
        firebaseDbClient.mainItemQuotation.child(mainItemId).child("total_sub_item").setValue(totalSubCount.toString())


        subItemList.clear()
        subItemAdapter.notifyDataSetChanged()
        getSubListFromMainItemId()

    }

    override fun onSubItemClick(list: SubItemQuotation) {
        val intent = Intent(this, SubItemDetailActivity::class.java)
        intent.putExtra(Const.KEYS.SUB_ITEM_ID, list.id)
        startActivity(intent)
    }

    override fun onItemSelectClick(list: SubItemQuotation, pos: Int) {
        list.is_selected = !list.is_selected!!
        subItemAdapter.notifyDataSetChanged()
    }

}