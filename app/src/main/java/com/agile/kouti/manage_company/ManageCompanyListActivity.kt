package com.agile.kouti.manage_company

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.agile.kouti.db.User
import com.agile.kouti.db.crm.Crm
import com.agile.kouti.db.invoice.Invoice
import com.agile.kouti.invoice.invoice_list.AddInvoiceActivity
import com.agile.kouti.utils.Const
import com.agile.kouti.view_picture.ViewPictureActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_invoice_list.*
import kotlinx.android.synthetic.main.activity_manage_company_list.*
import kotlinx.android.synthetic.main.search_layout.*

class ManageCompanyListActivity : KoutiBaseActivity(),View.OnClickListener,UserClickListener {

    private val userList = ArrayList<User>()
    private val selectedUserList = ArrayList<User>()
    private val userListAdapter = UserListAdapter(this,userList,this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_company_list)

        initData()
    }

    private fun initData() {

        tvTitle.text = getString(R.string.toolbar_title_manage_company)
        ivSettings.visibility = View.GONE

        cvFilter.setOnClickListener(this)
        cvAdd.setOnClickListener(this)
        cvDelete.setOnClickListener(this)
        cvSelectAll.setOnClickListener(this)
        ivBack.setOnClickListener(this)
        ivSearch.setOnClickListener(this)
        ivCamera.setOnClickListener(this)

        setUserAdapter()

        getUserData()

        swipeManageCompany.setOnRefreshListener {
            userList.clear()
            userListAdapter.notifyDataSetChanged()
            getUserData()
            swipeManageCompany.isRefreshing = false
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                filter(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                /* searchAdapter.filter.filter(s)*/
            }
        })
    }

    private fun setUserAdapter() {
        val layoutManager = LinearLayoutManager(this@ManageCompanyListActivity, LinearLayoutManager.VERTICAL, false)
        rvManageCompany.layoutManager = layoutManager
        rvManageCompany.adapter = userListAdapter
    }

    private fun getUserData() {
        showProgressDialog()
        val databaseReference= FirebaseDatabase.getInstance().getReference(Const.TableName.USER)
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (data in dataSnapshot.children) {
                        val l: User? = data.getValue<User>(User::class.java)
                        if (l != null) {
                            userList.add(l)
                        }
                    }
                    userListAdapter.notifyDataSetChanged()
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
                val intent = Intent(this, AddSubUserActivity::class.java)
                intent.putExtra(Const.KEYS.IS_EDIT, false)
                startActivity(intent)
            }

            R.id.cvDelete -> {
               // deleteData()
            }

            R.id.cvSelectAll -> {
                //selectAllData()
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

    override fun onUserClick(list: User) {
        val intent = Intent(this, AddSubUserActivity::class.java)
        intent.putExtra(Const.KEYS.IS_EDIT, true)
        intent.putExtra(Const.KEYS.SUB_USER_ID,list.id)
        startActivity(intent)
    }

    fun filter(text: String) {
        val filteredSearchList: ArrayList<User> = ArrayList()
        for (list in userList) {
            if (list.name!!.toLowerCase().contains(text.toLowerCase()) || list.phone!!.toLowerCase().contains(text.toLowerCase())) {
                filteredSearchList.add(list)
            }
        }
        userListAdapter.filterList(filteredSearchList);
    }



}