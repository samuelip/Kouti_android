package com.agile.kouti.chart_of_account

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.agile.kouti.db.AccountNature
import com.agile.kouti.db.ChartOfAccount
import com.agile.kouti.db.FirebaseDbClient
import com.agile.kouti.db.SecondLevel
import com.agile.kouti.utils.Const
import com.agile.kouti.utils.Preferences
import com.agile.kouti.view_picture.ViewPictureActivity
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_accont_list.*
import kotlinx.android.synthetic.main.search_layout.*
import timber.log.Timber
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList


class AccountListActivity : KoutiBaseActivity(), View.OnClickListener, AccountClickListener,
    KoutiBaseActivity.OnDialogClickListener {

    private val accountList = ArrayList<ChartOfAccount>()
    private val accountSelectedList = ArrayList<ChartOfAccount>()
    private val accountListAdapter = ChartOfAccountAdapter(this, accountList, this)
    var userId: String = ""

    private var accountReference: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accont_list)
        initData()
    }


    private fun initData() {
        tvTitle.text = resources.getString(R.string.chart_of_account)
        ivSettings.visibility = View.GONE

        cvFilter.setOnClickListener(this)
        cvAdd.setOnClickListener(this)
        cvDelete.setOnClickListener(this)
        cvSelectAll.setOnClickListener(this)

        ivBack.setOnClickListener(this)
        ivSearch.setOnClickListener(this)
        ivCamera.setOnClickListener(this)
        ivSettings.setOnClickListener(this)

        setAccountAdapter()
        //setData()

        userId = Preferences.getPreference(this@AccountListActivity, Const.SharedPrefs.USER_ID)
        Timber.e("userId -- $userId")

        //firebaseListenerInit()

        accountReference = FirebaseDatabase.getInstance().getReference(Const.TableName.CHART_OF_ACCOUNT)


        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                filter(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        swipeContainerAccount.setOnRefreshListener {
            accountList.clear()
            accountListAdapter.notifyDataSetChanged()
            getChartOfAccountList()
            swipeContainerAccount.isRefreshing = false
        }
    }

    override fun onResume() {
        super.onResume()
        accountList.clear()
        accountListAdapter.notifyDataSetChanged()
        getChartOfAccountList()
    }

    override fun onClick(v: View?) {

        when (v?.id) {

            R.id.cvFilter -> {
                showFilterDialog()
            }

            R.id.cvAdd -> {
                val intent = Intent(this, AddChartOfAccountActivity::class.java)
                intent.putExtra(Const.KEYS.IS_EDIT, false)
                startActivity(intent)
            }

            R.id.cvDelete -> {
                checkDataBeforeDelete()
            }

            R.id.cvSelectAll -> {
                SelectAllData()
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


    private fun setAccountAdapter() {
        val layoutManager = LinearLayoutManager(this@AccountListActivity, LinearLayoutManager.VERTICAL, false)
        rvAccountList.layoutManager = layoutManager
        rvAccountList.adapter = accountListAdapter
    }

    /* Display data */
    private fun getChartOfAccountList() {
        showProgressDialog()
        //val databaseReference= FirebaseDatabase.getInstance().getReference(Const.TableName.CHART_OF_ACCOUNT)

        var mFirebaseClientObj = FirebaseDbClient()
        val recentPostsQuery: Query = mFirebaseClientObj.chartOfAccountReference.orderByChild(Const.KEYS.USER_ID).equalTo(userId)
        recentPostsQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    var mFirebaseClient = FirebaseDbClient()
                    for (data in dataSnapshot.children) {
                        Timber.e("accountNatureObj  -- " )

                        val chartObj: ChartOfAccount? = data.getValue<ChartOfAccount>(ChartOfAccount::class.java)
                        if (chartObj != null) {
                            /*get Account Nature name*/
                            mFirebaseClient.accountNature.child(chartObj.account_nature.toString())
                                .addListenerForSingleValueEvent(object :
                                    ValueEventListener {
                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            var accountNatureObj: AccountNature? = dataSnapshot.getValue(AccountNature::class.java)
                                            Timber.e("accountNatureObj  -- " + accountNatureObj?.name)
                                            var accountNatureName = accountNatureObj?.name.toString()
                                            chartObj?.account_nature = accountNatureName

//                                            accountList.add(chartObj)
//                                            accountListAdapter.notifyDataSetChanged()

                                            /* GetSecond Level Name*/
                                            mFirebaseClient.secondLevel.child(chartObj.second_level.toString())
                                                .addListenerForSingleValueEvent(object :
                                                    ValueEventListener {
                                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                        if (dataSnapshot.exists()) {
                                                            var SecondLevelObj: SecondLevel? = dataSnapshot.getValue(SecondLevel::class.java)
                                                            Timber.e("SecondLevelObj  -- " + SecondLevelObj?.name)
                                                            var secondLevelName = SecondLevelObj?.name.toString()
                                                            chartObj?.second_level = secondLevelName

                                                            accountList.add(chartObj)
                                                            accountListAdapter.notifyDataSetChanged()
                                                        }
                                                    }
                                                    override fun onCancelled(error: DatabaseError) {hideProgressDialog()}
                                                })
                                        }
                                    }
                                    override fun onCancelled(error: DatabaseError) {hideProgressDialog()}
                                })
                        }
                    }

                    hideProgressDialog()
                }else{
                    hideProgressDialog()
                    showError("No Data Found")
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                hideProgressDialog()
            }
        })

    }

    override fun onAccountItemClick(list: ChartOfAccount) {
        if(!isNetworkConnected){
            showError(resources.getString(R.string.internet_error))
            return
        }

        val i = Intent(this, AccountDetailActivity::class.java)
        i.putExtra(Const.KEYS.LIST_OBJECT, list)
        startActivity(i)
    }

    override fun onItemSelectClick(list: ChartOfAccount, pos: Int) {
        list.is_selected = !list.is_selected!!
        accountListAdapter.notifyDataSetChanged()
    }

    /* Select All*/
    private fun SelectAllData() {
        if (accountList.isEmpty()) {
            showError("No data found")
            return
        }
        for (x in accountList)
            x.is_selected = true

        accountListAdapter.notifyDataSetChanged()
    }

    /* Search List */
    fun filter(text: String) {
        val filteredSearchList: ArrayList<ChartOfAccount> = ArrayList()
        for (list in accountList) {
            if (list.account_nature!!.toLowerCase().contains(text.toLowerCase())) {
                filteredSearchList.add(list)
            }
        }
        accountListAdapter.filterList(filteredSearchList);
    }


    /* Delete Function */
    private fun checkDataBeforeDelete() {
        if(accountList.isEmpty())
            return

        for (list in accountList) {
            if (list.is_selected!!) {
                accountSelectedList.add(list)
            }
        }

        if (accountSelectedList.isEmpty()) {
            showError(getString(R.string.select_account))
            return
        }
        setDialogListener(this)
        showTwoButtonDialog(getString(R.string.delete_item),getString(R.string.want_to_delete),getString(R.string.btn_yes),getString(R.string.btn_no))
    }

    override fun onPositiveClick() { deleteData() }

    override fun onNegativeClick() {}

    private fun deleteData() {
        if(!isNetworkConnected){
            showError(resources.getString(R.string.internet_error))
            return
        }

        for (list in accountSelectedList) {
            var firebaseDbClient = FirebaseDbClient()
            firebaseDbClient.chartOfAccountReference.child(list.id.toString()).removeValue()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                    }
                }
        }
        accountList.clear()
        accountListAdapter.notifyDataSetChanged()
        getChartOfAccountList()
    }


    /* Filter Function */
    private fun showFilterDialog() {
        val items = arrayOf(Const.KEYS.FILTER_ACCOUNT_NO,Const.KEYS.FILTER_ACCOUNT_NAME,Const.KEYS.FILTER_DATE)
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

    /* Sort Data*/
    private fun sortData(filterType: String) {
        when(filterType){
            Const.KEYS.FILTER_ACCOUNT_NO ->{
                Collections.sort(accountList, Comparator<ChartOfAccount> { lhs, rhs -> // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                    when {
                        lhs.account_no!!.toLowerCase() < rhs.account_no.toString().toLowerCase() -> -1
                        lhs.account_no!!.toLowerCase() < rhs.account_no.toString().toLowerCase() -> 1
                        else -> 0
                    }
                })
            }
            Const.KEYS.FILTER_ACCOUNT_NAME ->{
                Collections.sort(accountList, Comparator<ChartOfAccount> { lhs, rhs -> // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                    when {
                        lhs.account_name!!.toLowerCase() < rhs.account_name.toString().toLowerCase() -> -1
                        lhs.account_name!!.toLowerCase() < rhs.account_name.toString().toLowerCase() -> 1
                        else -> 0
                    }
                })
            }
            Const.KEYS.FILTER_DATE ->{
                Collections.sort(accountList, Comparator<ChartOfAccount> { lhs, rhs -> // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                    when {
                        lhs.created_date!!.toLowerCase() > rhs.created_date.toString().toLowerCase() -> -1
                        lhs.created_date!!.toLowerCase() > rhs.created_date.toString().toLowerCase() -> 1
                        else -> 0
                    }
                })
            }
        }
        accountListAdapter.notifyDataSetChanged()
    }


}


