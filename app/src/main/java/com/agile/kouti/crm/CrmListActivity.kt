package com.agile.kouti.crm

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
import com.agile.kouti.db.crm.Crm
import com.agile.kouti.utils.Const
import com.agile.kouti.utils.Preferences
import com.agile.kouti.view_picture.ViewPictureActivity
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_crm_list.*
import kotlinx.android.synthetic.main.search_layout.*
import timber.log.Timber
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class CrmListActivity : KoutiBaseActivity(),View.OnClickListener,CrmClickListener,KoutiBaseActivity.OnDialogClickListener {

    private val crmList = ArrayList<Crm>()
    private val selectedCrmList = ArrayList<Crm>()
    private val crmListAdapter = CrmListAdapter(this, crmList, this)

    var userId: String = ""
    private var accountReference: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crm_list)

        initData()
    }

    private fun initData() {
        tvTitle.text = getString(R.string.toolbar_title_crm)
        ivSettings.visibility = View.GONE

        cvFilter.setOnClickListener(this)
        cvAdd.setOnClickListener(this)
        cvDelete.setOnClickListener(this)
        cvSelectAll.setOnClickListener(this)
        ivBack.setOnClickListener(this)
        ivSearch.setOnClickListener(this)
        ivCamera.setOnClickListener(this)

        userId = Preferences.getPreference(this@CrmListActivity, Const.SharedPrefs.USER_ID)
        accountReference = FirebaseDatabase.getInstance().getReference(Const.TableName.PAYROLL)

        setCrmListAdapter()

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                filter(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                /* searchAdapter.filter.filter(s)*/
            }
        })

        swipeContainerCrm.setOnRefreshListener {
            crmList.clear()
            crmListAdapter.notifyDataSetChanged()
            getCrmData()
            swipeContainerCrm.isRefreshing = false
        }

    }

    private fun setCrmListAdapter() {
        val layoutManager = LinearLayoutManager(this@CrmListActivity, LinearLayoutManager.VERTICAL, false)
        rvCrm.layoutManager = layoutManager
        rvCrm.adapter = crmListAdapter
    }

    override fun onResume() {
        super.onResume()
        crmList.clear()
        crmListAdapter.notifyDataSetChanged()
        getCrmData()
    }

    private fun getCrmData() {
        showProgressDialog()
        //val databaseReference= FirebaseDatabase.getInstance().getReference(Const.TableName.CRM)

        var mFirebaseClientObj = FirebaseDbClient()
        val recentPostsQuery: Query = mFirebaseClientObj.crm.orderByChild(Const.KEYS.USER_ID).equalTo(userId)
        recentPostsQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (data in dataSnapshot.children) {
                        val l: Crm? = data.getValue<Crm>(Crm::class.java)
                        if (l != null) {
                            crmList.add(l)
                        }
                    }
                    crmListAdapter.notifyDataSetChanged()
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
                showFilterDialog()
            }

            R.id.cvAdd -> {
                val intent = Intent(this, AddCrmActivity::class.java)
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

    override fun onCrmItemClick(list: Crm) {
        Timber.e("CRM_ID -- "+list.id)
        val intent = Intent(this, CrmDetailActivity::class.java)
        intent.putExtra(Const.KEYS.CRM_ID,list.id)
        startActivity(intent)
    }

    override fun onItemSelectClick(list: Crm, pos: Int) {
        list.is_selected = !list.is_selected!!
        crmListAdapter.notifyDataSetChanged()
    }


    /* Select All */
    private fun selectAllData() {
        if (crmList.isEmpty()) {
            showError("No data found")
            return
        }
        for (x in crmList)
            x.is_selected = true

        crmListAdapter.notifyDataSetChanged()
    }

    /* Search Function */
    fun filter(text: String) {
        val filteredSearchList: ArrayList<Crm> = ArrayList()
        for (list in crmList) {
            if (list.name!!.toLowerCase().contains(text.toLowerCase())) {
                filteredSearchList.add(list)
            }
        }
        crmListAdapter.filterList(filteredSearchList)
    }

    /* Filter Function */
    private fun showFilterDialog() {
        val items = arrayOf(Const.KEYS.CUSTOMER_CODE,Const.KEYS.CUSTOMER_NAME,Const.KEYS.FILTER_DATE)
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
            Const.KEYS.CUSTOMER_CODE ->{
                Collections.sort(crmList, Comparator<Crm> { lhs, rhs -> // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                    when {
                        lhs.code!!.toLowerCase() < rhs.code.toString().toLowerCase() -> -1
                        lhs.code!!.toLowerCase() < rhs.code.toString().toLowerCase() -> 1
                        else -> 0
                    }
                })
            }
            Const.KEYS.CUSTOMER_NAME ->{
                Collections.sort(crmList, Comparator<Crm> { lhs, rhs -> // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                    when {
                        lhs.name!!.toLowerCase() < rhs.name.toString().toLowerCase() -> -1
                        lhs.name!!.toLowerCase() < rhs.name.toString().toLowerCase() -> 1
                        else -> 0
                    }
                })
            }
            Const.KEYS.FILTER_DATE ->{
                Collections.sort(crmList, Comparator<Crm> { lhs, rhs -> // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                    when {
                        lhs.created_date!!.toLowerCase() > rhs.created_date.toString().toLowerCase() -> -1
                        lhs.created_date!!.toLowerCase() > rhs.created_date.toString().toLowerCase() -> 1
                        else -> 0
                    }
                })
            }
        }
        crmListAdapter.notifyDataSetChanged()
    }


    /* Delete Function */
    private fun checkDataBeforeDelete() {
        if(crmList.isEmpty())
            return

        for (list in crmList) {
            if (list.is_selected!!) {
                selectedCrmList.add(list)
            }
        }

        if (selectedCrmList.isEmpty()) {
            showError(getString(R.string.select_one_crm))
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
        for (list in selectedCrmList) {
            var firebaseDbClient = FirebaseDbClient()
            firebaseDbClient.crm.child(list.id.toString()).removeValue()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                    }
                }
        }
        crmList.clear()
        crmListAdapter.notifyDataSetChanged()
        getCrmData()
    }


}