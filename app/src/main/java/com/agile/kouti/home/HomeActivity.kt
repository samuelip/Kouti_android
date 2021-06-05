package com.agile.kouti.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.agile.kouti.chart_of_account.AccountListActivity
import com.agile.kouti.crm.CrmListActivity
import com.agile.kouti.payroll.PayrollExpenseListActivity
import com.agile.kouti.sale.SaleActivity
import com.agile.kouti.settings.MenuModel
import com.agile.kouti.settings.SettingsActivity
import com.agile.kouti.utils.Const
import com.agile.kouti.utils.Preferences
import com.agile.kouti.view_picture.UploadImageActivity
import com.agile.kouti.view_picture.ViewPictureActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_home.*
import timber.log.Timber


class HomeActivity : KoutiBaseActivity(), View.OnClickListener, OnSetUpItemClickListener,
    TransactionClickListener {

    private val mSettingRequestCode = 101
    private val setupData = ArrayList<SetupModel>()
    private val transactionData = ArrayList<TransactionModel>()
    private val mMainMenuList = ArrayList<MenuModel>()

    private val setUpListAdapter = SetUpAdapter(this, setupData, this)
    private val transactionListAdapter = TransactionAdapter(this, transactionData, this)

    private val mDashboardMainMenuAdapter = DashboardMainMenuAdapter(this, mMainMenuList, this)

    private var mAuth: FirebaseAuth? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        mAuth = FirebaseAuth.getInstance()
        initData()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == mSettingRequestCode && resultCode == RESULT_OK) {
            updateMenuList()
        }


    }

    private fun initData() {

        ivDashBoard.setOnClickListener(this)
        ivSearch.setOnClickListener(this)
        ivSettings.setOnClickListener(this)
        ivCamera.setOnClickListener(this)

        llReceivable.setOnClickListener(this)
        llClaimed.setOnClickListener(this)
        llSalesPerformance.setOnClickListener(this)
        llSalesOfTheDay.setOnClickListener(this)
        llSalesOfTheMonth.setOnClickListener(this)
        llPayableForStaff.setOnClickListener(this)
        llPayableForTheSupplier.setOnClickListener(this)

//        setSetUpListAdapter()
//        setUpListData()
//
//        setTransactionAdapter()
//        setTransactionListData()

        setDashboardMenu()
    }

    /* Setup menu adapter*/
    private fun setDashboardMenu() {

        val layoutManager = GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false)
        rv_dashboard_main_menu.layoutManager = layoutManager
        rv_dashboard_main_menu.adapter = mDashboardMainMenuAdapter

        updateMenuList()

    }

    /* Update main menu list*/
    private fun updateMenuList() {
        mMainMenuList.clear()
        mMainMenuList.addAll(
            Preferences.getMenuList(this, Const.SharedPrefs.MAIN_DASHBOARD_MENU)
                .filter { it.isSelected })
        mDashboardMainMenuAdapter.notifyDataSetChanged()
    }


    /* Set SetUp adapter*/
    private fun setSetUpListAdapter() {
        val layoutManager =
            LinearLayoutManager(this@HomeActivity, LinearLayoutManager.HORIZONTAL, false)
        rvSetup.layoutManager = layoutManager
        rvSetup.adapter = setUpListAdapter
    }

    /* Fill setup List*/
    private fun setUpListData() {
        setupData.add(SetupModel("Data\nImport", R.drawable.data_import, ""))
        setupData.add(SetupModel("Opening Balance", R.drawable.opening_balance, ""))
        setupData.add(SetupModel("Chart Of Account", R.drawable.data_import, ""))
        setupData.add(SetupModel("Stock", R.drawable.data_import, ""))
        setupData.add(SetupModel("CRM", R.drawable.data_import, ""))
        setupData.add(SetupModel("Supplier", R.drawable.data_import, ""))
        setupData.add(SetupModel("Staff/\nVendor", R.drawable.data_import, ""))
        setupData.add(SetupModel("Multi-Currency", R.drawable.data_import, ""))
        setupData.add(SetupModel("Project/Shop", R.drawable.data_import, ""))
        setUpListAdapter.notifyDataSetChanged()
    }

    /* Set Transaction adapter*/
    private fun setTransactionAdapter() {
//        val layoutManager =
//            LinearLayoutManager(this@HomeActivity, LinearLayoutManager.HORIZONTAL, false)

        val layoutManager = GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false)
        rvTransaction.layoutManager = layoutManager
        rvTransaction.adapter = transactionListAdapter
    }

    private fun setTransactionListData() {
        transactionData.add(TransactionModel("Accounting", R.drawable.accounting, ""))
        transactionData.add(TransactionModel("Sales", R.drawable.sales, ""))
        transactionData.add(TransactionModel("POS", R.drawable.sales, ""))
        transactionData.add(TransactionModel("Projects/\nReports", R.drawable.sales, ""))
        transactionData.add(TransactionModel("Purchase", R.drawable.sales, ""))
        transactionData.add(TransactionModel("StockRecord/\nAdj.", R.drawable.sales, ""))
        transactionData.add(TransactionModel("Fixed\nAssets", R.drawable.sales, ""))
        transactionData.add(TransactionModel("Payroll/\nExp", R.drawable.sales, ""))
        transactionData.add(TransactionModel("Bills\nImport", R.drawable.sales, ""))
        transactionData.add(TransactionModel("Staff/\nDirector/\nPlay List", R.drawable.sales, ""))

        transactionListAdapter.notifyDataSetChanged()

    }

    override fun onClick(v: View?) {

        when (v?.id) {

            R.id.llReceivable -> {
                showError("This feature is under maintenance.")
            }

            R.id.llClaimed -> {
                showError("This feature is under maintenance.")
            }

            R.id.llSalesPerformance -> {
                showError("This feature is under maintenance.")
            }

            R.id.llSalesOfTheDay -> {
                showError("This feature is under maintenance.")
            }

            R.id.llSalesOfTheMonth -> {
                showError("This feature is under maintenance.")
            }

            R.id.llPayableForStaff -> {
                showError("This feature is under maintenance.")
            }

            R.id.llPayableForTheSupplier -> {
                showError("This feature is under maintenance.")
            }

            R.id.ivDashBoard -> {
                showError("This feature is under maintenance.")
            }

            R.id.ivSearch -> {
                showError("This feature is under maintenance.")
            }

            R.id.ivSettings -> {
                //showError("This feature is under maintenance.")
                val intent = Intent(this, SettingsActivity::class.java)
                startActivityForResult(intent, mSettingRequestCode)

            }

            R.id.ivCamera -> {
                //showError("This feature is under maintenance.")


                val intent = Intent(this, UploadImageActivity::class.java)
                startActivity(intent)
            }
        }
    }

    override fun onSetUpItemClick(list: MenuModel) {


        if (!isNetworkConnected) {
            showError(resources.getString(R.string.internet_error))
            return
        }

        when (list.title) {
            "Chart Of Account" -> {
                val intent = Intent(this, AccountListActivity::class.java)
                startActivity(intent)
            }
            "CRM" -> {
                val intent = Intent(this, CrmListActivity::class.java)
                startActivity(intent)
            }

            "Payroll/\nExp" -> {
                val intent = Intent(this, PayrollExpenseListActivity::class.java)
                startActivity(intent)
            }
            "Sales" -> {
                val intent = Intent(this, SaleActivity::class.java)
                startActivity(intent)
            }

            else -> {
                showError("This feature is under maintenance.")
            }
        }

    }


//    override fun onSetUpItemClick(list: SetupModel) {
//        Timber.e("onSetUpItemClick -- ${list.title}")
//
//        if (!isNetworkConnected) {
//            showError(resources.getString(R.string.internet_error))
//            return
//        }
//
//        when (list.title) {
//            "Chart Of Account" -> {
//                val intent = Intent(this, AccountListActivity::class.java)
//                startActivity(intent)
//            }
//            "CRM" -> {
//                val intent = Intent(this, CrmListActivity::class.java)
//                startActivity(intent)
//            }
//            else -> {
//                showError("This feature is under maintenance.")
//            }
//        }
//    }

    override fun onTransactionItemClick(list: TransactionModel) {
        Timber.e("onTransactionItemClick -- ${list.title}")

        if (!isNetworkConnected) {
            showError(resources.getString(R.string.internet_error))
            return
        }

        when (list.title) {
            "Payroll/\nExp" -> {
                val intent = Intent(this, PayrollExpenseListActivity::class.java)
                startActivity(intent)
            }
            "Sales" -> {
                val intent = Intent(this, SaleActivity::class.java)
                startActivity(intent)
            }
            else -> {
                showError("This feature is under maintenance.")
                //            val intent = Intent(this, SaleActivity::class.java)
                //            startActivity(intent)
            }
        }
    }


}