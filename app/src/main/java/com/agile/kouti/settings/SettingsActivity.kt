package com.agile.kouti.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.agile.kouti.db.CurrencyTable
import com.agile.kouti.login.ChangePasswordActivity
import com.agile.kouti.login.LoginActivity
import com.agile.kouti.manage_company.ManageCompanyListActivity
import com.agile.kouti.utils.Const
import com.agile.kouti.utils.Preferences
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.toolbar.*
import me.rishabhkhanna.recyclerswipedrag.OnDragListener
import me.rishabhkhanna.recyclerswipedrag.RecyclerHelper
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList


class SettingsActivity : KoutiBaseActivity(), View.OnClickListener,
    KoutiBaseActivity.OnDialogClickListener, MenuCheckListener, OnDragListener {

    private var mAuth: FirebaseAuth? = null

    var currencyId: String = ""
    var currencyObj: CurrencyTable? = null

    private var mMenuList = ArrayList<MenuModel>()
    private lateinit var mSettingMenuAdapter: SettingMenuAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        initData()
        mAuth = FirebaseAuth.getInstance()
    }

    private fun initData() {
        ivBack.setOnClickListener(this)
        tvEditProfile.setOnClickListener(this)
        tvChangePassword.setOnClickListener(this)
        tvLogOut.setOnClickListener(this)
        tvManageCompany.setOnClickListener(this)
        tvAddCurrency.setOnClickListener(this)

        tvToolbarTitle.text = resources.getString(R.string.toolbar_title_settings)
        tvToolbarSubTitle.visibility = View.GONE


        setSettingMenu()

    }

    // Set setting menu
    private fun setSettingMenu() {

        rv_dashboard_menu.layoutManager = LinearLayoutManager(this)

        mMenuList = Preferences.getMenuList(this, Const.SharedPrefs.MAIN_DASHBOARD_MENU)

        mSettingMenuAdapter = SettingMenuAdapter(this, mMenuList, this)

        rv_dashboard_menu.adapter = mSettingMenuAdapter

        val mTouchHelper: RecyclerHelper<*> =
            RecyclerHelper(
                mMenuList,
                mSettingMenuAdapter as RecyclerView.Adapter<RecyclerView.ViewHolder>
            )
        mTouchHelper.setRecyclerItemDragEnabled(true)

        mTouchHelper.setRecyclerItemSwipeEnabled(false)

        mTouchHelper.setOnDragItemListener(this)

        val itemTouchHelper = ItemTouchHelper(mTouchHelper)
        itemTouchHelper.attachToRecyclerView(rv_dashboard_menu)

    }


    override fun onClick(v: View?) {

        when (v?.id) {

            R.id.ivBack -> {
                setResult(RESULT_OK)
                finish()
            }

            R.id.tvEditProfile -> {
            }

            R.id.tvAddCurrency -> {
                //addCurrency()
            }

            R.id.tvChangePassword -> {
                val intent = Intent(this, ChangePasswordActivity::class.java)
                startActivity(intent)
            }

            R.id.tvLogOut -> {
                setDialogListener(this)
                showTwoButtonDialog("Logout ?", "Are you sure you want to logout ?", "Yes", "No")
            }

            R.id.tvManageCompany -> {
                val intent = Intent(this, ManageCompanyListActivity::class.java)
                startActivity(intent)
            }
        }

    }

    override fun onPositiveClick() {
        Preferences.removeAllPreference(this)
        signOut()
    }

    override fun onNegativeClick() {}

    private fun signOut() {
        mAuth!!.signOut()

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }

    private fun addCurrency() {
//        currencyId = FirebaseDatabase.getInstance().getReference(Const.TableName.CURRENCY).push().key.toString()
//        currencyObj = CurrencyTable("HKD","Hong Kong", currencyId)
//        var firebaseDbClient = FirebaseDbClient();
//        if (currencyId != null) {
//            firebaseDbClient.currency.child(currencyId).setValue(currencyObj)
//
//        }

    }

    override fun onItemCheckChange(pos: Int, isChecked: Boolean) {
        mMenuList[pos].isSelected = isChecked
        Preferences.setMenuList(this, mMenuList, Const.SharedPrefs.MAIN_DASHBOARD_MENU)
        mSettingMenuAdapter.notifyDataSetChanged()
    }

    override fun onDragItemListener(fromPosition: Int, toPosition: Int) {
        Preferences.setMenuList(this, mMenuList, Const.SharedPrefs.MAIN_DASHBOARD_MENU)

    }

}