package com.agile.kouti.chart_of_account

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.agile.kouti.db.*
import com.agile.kouti.search.SearchActivity
import com.agile.kouti.utils.Const
import com.agile.kouti.utils.Preferences
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_account_detail.*
import kotlinx.android.synthetic.main.activity_add_chart_of_account.*
import kotlinx.android.synthetic.main.toolbar.*
import timber.log.Timber
import java.util.*

class AddChartOfAccountActivity : KoutiBaseActivity(), View.OnClickListener,KoutiBaseActivity.OnDialogClickListener {

    private var isEdit = false
    private var colorCode = "#DAC1E8"
    private lateinit var database: DatabaseReference

    private var accountData = ChartOfAccount()

    private var accountNatureId =""
    private var secondLevelId =""
    private var thirdLevelId =""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_chart_of_account)
        initData()
        database = FirebaseDatabase.getInstance().reference

        Timber.e("UTC date -- "+utcDate)
    }

    private fun initData() {

        btnSave.setOnClickListener(this)
        ivBack.setOnClickListener(this)
        etAccountNature.setOnClickListener(this)
        etSecondLevel.setOnClickListener(this)
        etThirdLevel.setOnClickListener(this)

        if (intent != null) {
            isEdit = intent.getBooleanExtra(Const.KEYS.IS_EDIT, false)

            if (isEdit) {
                accountData = intent.getParcelableExtra<ChartOfAccount>(Const.KEYS.LIST_OBJECT)

                tvToolbarTitle.text = resources.getString(R.string.toolbar_title_chart_of_account)
                tvToolbarSubTitle.text="Edit"
                tvToolbarSubTitle.visibility = View.VISIBLE

                if (accountData.id != null) {
                    getAccountData()
                }


            } else {
                tvToolbarTitle.text = resources.getString(R.string.toolbar_title_chart_of_account)
                tvToolbarSubTitle.text="Add"
                colorCode = randomColor.toString()
            }
        }
    }

    private fun getAccountData() {
        showProgressDialog()
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.chartOfAccountReference.child(accountData.id.toString())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var obj: ChartOfAccount? = dataSnapshot.getValue(ChartOfAccount::class.java)

                        if (obj != null) {

                            etAccountNo.setText(obj.account_no)
                            etAccountName.setText( obj.account_name)

                            accountNatureId = obj.account_nature!!
                            secondLevelId = obj.second_level!!
                            thirdLevelId = obj.third_level!!

                            if (!TextUtils.isEmpty(accountNatureId))
                                getAccountNature()

                            if (!TextUtils.isEmpty(secondLevelId))
                                getSecondLevel()

                            if (!TextUtils.isEmpty(thirdLevelId))
                                getThirdLevelName()
                        }
                        hideProgressDialog()
                    }
                }

                override fun onCancelled(error: DatabaseError) { hideProgressDialog()}
            })

    }

    private fun getAccountNature() {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.accountNature.child(accountNatureId.toString())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var obj: AccountNature? = dataSnapshot.getValue(AccountNature::class.java)
                        if (obj != null) {
                            etAccountNature.setText(obj.name)
                            accountNatureId = obj.id!!
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun getSecondLevel() {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.secondLevel.child(secondLevelId.toString())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var obj: SecondLevel? = dataSnapshot.getValue(SecondLevel::class.java)
                        if (obj != null) {
                            etSecondLevel.setText(obj.name)
                            secondLevelId = obj?.id!!
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun getThirdLevelName() {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.thirdLevel.child(thirdLevelId.toString())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var obj: ThirdLevel? = dataSnapshot.getValue(ThirdLevel::class.java)
                        if (obj != null) {
                            etThirdLevel.setText(obj?.name)
                            thirdLevelId = obj?.id!!
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }








    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnSave -> {
                hideKeyBoard(v)
                if (isValidation()) {
                    if (!isNetworkConnected) {
                        showError(resources.getString(R.string.internet_error))
                        return
                    }
                    if (isEdit)
                        UpdateDataInDB()
                    else
                        addValueInDataBase()
                }
            }
            R.id.ivBack -> {
                finish()
            }
            R.id.etAccountNature -> {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra(Const.KEYS.SEARCH_TYPE, Const.SearchType.AccountNature.toString())
                intent.putExtra(Const.KEYS.COLOR_CODE, colorCode)
                startActivityForResult(intent, Const.RequestCodes.ACCOUNT_NATURE)
            }
            R.id.etSecondLevel -> {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra(Const.KEYS.SEARCH_TYPE, Const.SearchType.SecondLevel.toString())
                intent.putExtra(Const.KEYS.COLOR_CODE, colorCode)
                startActivityForResult(intent, Const.RequestCodes.SECOND_LEVEL)
            }
            R.id.etThirdLevel -> {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra(Const.KEYS.SEARCH_TYPE, Const.SearchType.ThirdLevel.toString())
                intent.putExtra(Const.KEYS.COLOR_CODE, colorCode)
                startActivityForResult(intent, Const.RequestCodes.THIRD_LEVEL)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            Const.RequestCodes.ACCOUNT_NATURE -> {
                if (data != null) {
                    var accountName = data.getStringExtra(Const.KEYS.SEARCH_TEXT)
                    etAccountNature.setText(accountName)

                    accountNatureId = data.getStringExtra(Const.KEYS.ACCOUNT_NATURE_ID)
                    Timber.e("accountNatureId -- "+accountNatureId)
                }
            }

            Const.RequestCodes.SECOND_LEVEL -> {
                if (data != null) {
                    var secondLevel = data.getStringExtra(Const.KEYS.SEARCH_TEXT)
                    etSecondLevel.setText(secondLevel)

                    secondLevelId = data.getStringExtra(Const.KEYS.SECOND_LEVEL_ID)
                    Timber.e("secondLevelId -- "+secondLevelId)
                }
            }

            Const.RequestCodes.THIRD_LEVEL -> {
                if (data != null) {
                    var thirdLevel = data.getStringExtra(Const.KEYS.SEARCH_TEXT)
                    etThirdLevel.setText(thirdLevel)

                    thirdLevelId = data.getStringExtra(Const.KEYS.THIRD_LEVEL_ID)
                    Timber.e("thirdLevelId -- "+thirdLevelId)
                }
            }
        }
    }

    private fun isValidation(): Boolean {

        if (TextUtils.isEmpty(etAccountNature.text.toString().trim())) {
            showError(getString(R.string.select_account_nature))
            return false
        }
        if (TextUtils.isEmpty(etSecondLevel.text.toString().trim())) {
            showError(getString(R.string.select_second_level))
            return false
        }
        if (TextUtils.isEmpty(etThirdLevel.text.toString().trim())) {
            showError(getString(R.string.select_third_level))
            return false
        }
        if (TextUtils.isEmpty(etAccountNo.text.toString().trim())) {
            showError(getString(R.string.enter_account_no))
            return false
        }
        if (TextUtils.isEmpty(etAccountName.text.toString().trim())) {
            showError(getString(R.string.enter_account_name))
            return false
        }
//        if (TextUtils.isEmpty(etTraditionalChinese.text.toString().trim())) {
//            showError("Please enter traditional chinese.")
//            return false
//        }
//        if (TextUtils.isEmpty(etSimplifiedChinese.text.toString().trim())) {
//            showError("Please enter simplified chinese.")
//            return false
//        }

        return true


    }

    /*Add value in Chart Of Account Table*/
    private fun addValueInDataBase() {

        showProgressDialog()

        val databaseReferenceId = FirebaseDatabase.getInstance().getReference(Const.TableName.CHART_OF_ACCOUNT).push().key
        Timber.e("databaseReference key  -- $databaseReferenceId")

        val chartOfAccount = ChartOfAccount(
            databaseReferenceId,
            Preferences.getPreference(this, Const.SharedPrefs.USER_ID),
            accountNatureId,
            secondLevelId,
            thirdLevelId,
            etAccountNo.text.toString().trim(),
            etAccountName.text.toString().trim(),
            colorCode,
            false,
            utcDate.toString()
        )

        var firebaseDbClient = FirebaseDbClient()
        if (databaseReferenceId != null) {
            firebaseDbClient.chartOfAccountReference.child(databaseReferenceId)
                .setValue(chartOfAccount).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        hideProgressDialog()
                        showAlert(getString(R.string.add_account))
                        finish()
                    } else {
                        hideProgressDialog()
                        setDialogListener(this)
                        showTwoButtonDialog(resources.getString(R.string.app_name),task.exception.toString(), "Ok", "")
                    }
                }
        }

    }

    /* Update Data in DB */
    private fun UpdateDataInDB() {

        showProgressDialog()

        Timber.e("accountData.id   -- $accountData.id")

        val chartOfAccount = ChartOfAccount(
            accountData.id,
            Preferences.getPreference(this, Const.SharedPrefs.USER_ID),
            accountNatureId,
            secondLevelId,
            thirdLevelId,
            etAccountNo.text.toString().trim(),
            etAccountName.text.toString().trim(),
            accountData.color,
            accountData.is_selected,
            accountData.created_date
        )


        var firebaseDbClient = FirebaseDbClient()
        accountData.id?.let {
            firebaseDbClient.chartOfAccountReference.child(it)
                .setValue(chartOfAccount).addOnCompleteListener { task ->
                    hideProgressDialog()
                    showAlert(getString(R.string.accouny_update))
                    finish()
                }
        }


    }

    override fun onPositiveClick() {}

    override fun onNegativeClick() {}


}