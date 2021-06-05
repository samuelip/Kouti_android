package com.agile.kouti.payroll

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.agile.kouti.db.FirebaseDbClient
import com.agile.kouti.db.payroll.PayRoll
import com.agile.kouti.utils.Const
import com.agile.kouti.utils.Preferences
import com.agile.kouti.view_picture.ViewPictureActivity
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_payroll_expense_list.*
import kotlinx.android.synthetic.main.search_layout.*

class PayrollExpenseListActivity : KoutiBaseActivity(), View.OnClickListener,PayrollClickListener,
    KoutiBaseActivity.OnDialogClickListener {

    private val payrollList = ArrayList<PayRoll>()
    private val selectedPayrollList = ArrayList<PayRoll>()
    private val payrollListAdapter = PayrollListAdapter(this, payrollList, this)

    var userId: String = ""
    private var accountReference: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payroll_expense_list)

        initData()
    }

    private fun initData() {
        tvTitle.text = getString(R.string.toolbar_payroll_expenses)
        ivSettings.visibility = View.GONE

        cvFilter.setOnClickListener(this)
        cvAdd.setOnClickListener(this)
        cvDelete.setOnClickListener(this)
        cvSelectAll.setOnClickListener(this)

        ivBack.setOnClickListener(this)
        ivSearch.setOnClickListener(this)
        ivCamera.setOnClickListener(this)

        userId = Preferences.getPreference(this@PayrollExpenseListActivity, Const.SharedPrefs.USER_ID)
        accountReference = FirebaseDatabase.getInstance().getReference(Const.TableName.PAYROLL)

        setPayrollAdapter()

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                filter(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }
        })

        swipeContainerPayroll.setOnRefreshListener {
            payrollList.clear()
            payrollListAdapter.notifyDataSetChanged()
            getPayrollData()
            swipeContainerPayroll.isRefreshing = false
        }
    }

    /* Set Adapter */
    private fun setPayrollAdapter() {
        val layoutManager = LinearLayoutManager(this@PayrollExpenseListActivity, LinearLayoutManager.VERTICAL, false)
        rvPayRoll.layoutManager = layoutManager
        rvPayRoll.adapter = payrollListAdapter
    }

    override fun onResume() {
        super.onResume()
        payrollList.clear()
        payrollListAdapter.notifyDataSetChanged()
        getPayrollData()
    }

    /* Get Payroll List */
    private fun getPayrollData() {
        showProgressDialog()
        //val databaseReference= FirebaseDatabase.getInstance().getReference(Const.TableName.PAYROLL)

        var mFirebaseClientObj = FirebaseDbClient()
        val recentPostsQuery: Query = mFirebaseClientObj.payroll.orderByChild(Const.KEYS.USER_ID).equalTo(userId)
        recentPostsQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (data in dataSnapshot.children) {
                        val l: PayRoll? = data.getValue<PayRoll>(PayRoll::class.java)
                        if (l != null) {
                            payrollList.add(l)
                        }
                    }
                    payrollListAdapter.notifyDataSetChanged()
                    hideProgressDialog()
                }else{
                    hideProgressDialog()
                    showError(getString(R.string.no_data_found))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                hideProgressDialog()
            }
        })
    }


    /* Click Event */
    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.cvFilter -> {
                //showFilterDialog()
            }

            R.id.cvAdd -> {
                val intent = Intent(this, AddPayRollActivity::class.java)
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

    override fun onPayrollItemClick(list: PayRoll) {
        val intent = Intent(this, PayrollExpenseDetailActivity::class.java)
        intent.putExtra(Const.KEYS.LIST_OBJECT, list)
        startActivity(intent)
    }

    override fun onItemSelectClick(list: PayRoll, pos: Int) {
        list.is_selected = !list.is_selected!!
        payrollListAdapter.notifyDataSetChanged()
    }

    private fun SelectAllData() {
        if (payrollList.isEmpty()) {
            showError("No data found")
            return
        }
        for (x in payrollList)
            x.is_selected = true

        payrollListAdapter.notifyDataSetChanged()
    }


    /* Search Function */
    fun filter(text: String) {
        val filteredSearchList: ArrayList<PayRoll> = ArrayList()
        for (list in payrollList) {
            if (list.expense_no!!.toLowerCase().contains(text.toLowerCase())) {
                filteredSearchList.add(list)
            }
        }
        payrollListAdapter.filterList(filteredSearchList);
    }


    /* Delete Function */
    private fun checkDataBeforeDelete() {
        if(payrollList.isEmpty())
            return

        for (list in payrollList) {
            if (list.is_selected!!) {
                selectedPayrollList.add(list)
            }
        }

        if (selectedPayrollList.isEmpty()) {
            showError(getString(R.string.select_payroll))
            return
        }


        setDialogListener(this)
        showTwoButtonDialog(getString(R.string.delete_item),getString(R.string.want_to_delete),getString(R.string.btn_yes),getString(R.string.btn_no))
    }

    override fun onPositiveClick() {
        if(!isNetworkConnected){
            showError(resources.getString(R.string.internet_error))
            return
        }
       deleteData()
    }

    override fun onNegativeClick() {}

    private fun deleteData() {
        if(selectedPayrollList.isEmpty())
            return

        for (list in selectedPayrollList) {
            var firebaseDbClient = FirebaseDbClient()
            firebaseDbClient.payroll.child(list.id.toString()).removeValue()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                    }
                }
        }
        payrollList.clear()
        payrollListAdapter.notifyDataSetChanged()
        getPayrollData()
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

    private fun sortData(searchType: String) {}
}