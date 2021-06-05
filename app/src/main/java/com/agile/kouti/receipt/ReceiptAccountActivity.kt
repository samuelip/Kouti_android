package com.agile.kouti.receipt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.agile.kouti.db.CurrencyTable
import com.agile.kouti.db.FirebaseDbClient
import com.agile.kouti.db.receipt.Receipt
import com.agile.kouti.db.receipt.ReceiptAccount
import com.agile.kouti.dialog.CurrencyDialog
import com.agile.kouti.dialog.CurrencyListener
import com.agile.kouti.search.SearchActivity
import com.agile.kouti.utils.Const
import com.agile.kouti.utils.Preferences
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_add_receipt.*
import kotlinx.android.synthetic.main.activity_receipt_acount.*
import kotlinx.android.synthetic.main.toolbar.*
import timber.log.Timber
import java.util.ArrayList

class ReceiptAccountActivity : KoutiBaseActivity(), View.OnClickListener, CurrencyListener {

    private var colorCode = "#EAE3E3"
    private var isEdit = false

    private var accountId = ""
    private var accountName = ""

    private var currencyId = ""

    private var receiptAccountTableId = ""
    private var receiptAccountTableObj: ReceiptAccount? = null

    var userId: String = ""

    private var currencyList = ArrayList<CurrencyTable>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt_acount)

        initData()

        getCurrencyListData()
    }

    private fun initData() {

        ivBack.setOnClickListener(this)
        etAccounts.setOnClickListener(this)
        btnAddAccount.setOnClickListener(this)
        etCurrency.setOnClickListener(this)

        if (intent != null) {
            isEdit = intent.getBooleanExtra(Const.KEYS.IS_EDIT, false)
        }

        if (isEdit) {
            receiptAccountTableId = intent.getStringExtra(Const.KEYS.RECEIPT_ACCOUNT_ID)
            tvToolbarTitle.text = resources.getString(R.string.toolbar_title_receipt_account)
            tvToolbarSubTitle.text = resources.getString(R.string.txt_edit)
            tvToolbarSubTitle.visibility = View.VISIBLE

            if (!TextUtils.isEmpty(receiptAccountTableId))
                getReceiptData()

        } else {
            tvToolbarTitle.text = resources.getString(R.string.toolbar_title_receipt_account)
            tvToolbarSubTitle.text = resources.getString(R.string.txt_add)
            tvToolbarSubTitle.visibility = View.VISIBLE
        }

        userId = Preferences.getPreference(this@ReceiptAccountActivity, Const.SharedPrefs.USER_ID)
    }

    private fun getReceiptData() {
        showProgressDialog()
        var mFirebaseClient = FirebaseDbClient()

        mFirebaseClient.receiptAccount.child(receiptAccountTableId.toString())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var obj: ReceiptAccount? = dataSnapshot.getValue(ReceiptAccount::class.java)
                        if (obj != null) {
                            receiptAccountTableObj = obj
                            setData()
                            hideProgressDialog()
                        }
                    }else{
                        hideProgressDialog()
                    }
                }
                override fun onCancelled(error: DatabaseError) {hideProgressDialog()}
            })
    }

    private fun setData() {

        etAccounts.setText(receiptAccountTableObj?.accounts_name)
        accountId = receiptAccountTableObj?.accounts!!

        etRemarksRec.setText(receiptAccountTableObj?.remarks)
        etRateRec.setText(receiptAccountTableObj?.rate)
        etCreditNoteNoRec.setText(receiptAccountTableObj?.credit_note_no)
        etTotalAmountRec.setText(receiptAccountTableObj?.total_amount)

        getCurrencyName();
    }

    /* Get Currency Name */
    private fun getCurrencyName() {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.currency.child(receiptAccountTableObj!!.currency!!)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var obj: CurrencyTable? = dataSnapshot.getValue(CurrencyTable::class.java)
                        if (obj != null) {
                            etCurrency.setText(obj.currency)
                            currencyId = obj?.id!!
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.ivBack -> {
                finish()
            }

            R.id.etCurrency -> {

                showCurrencyDialog()
            }
            R.id.etAccounts -> {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra(Const.KEYS.SEARCH_TYPE, Const.SearchType.ChartOfAccountReceipt.toString())
                intent.putExtra(Const.KEYS.COLOR_CODE, colorCode)
                startActivityForResult(intent, Const.RequestCodes.ADD_CHART_OF_ACCOUNT_RECEIPT)
            }

            R.id.btnAddAccount -> {
                hideKeyBoard(v)
                if(checkValidation()){

                    saveDataInDB()

                }
            }

        }

    }

    /* Save data in DB*/
    private fun saveDataInDB() {

        showProgressDialog()
        if (!isEdit)
            receiptAccountTableId = FirebaseDatabase.getInstance().getReference(Const.TableName.RECEIPT_ACCOUNT).push().key.toString()

        receiptAccountTableObj = ReceiptAccount(
            receiptAccountTableId,
            accountId,
            etAccounts.text.toString().trim(),
            etRemarksRec.text.toString().trim(),
            currencyId,
            etRateRec.text.toString().trim(),
            etCreditNoteNoRec.text.toString().trim(),
            etTotalAmountRec.text.toString().trim()
        )

        var firebaseDbClient = FirebaseDbClient()
        if (receiptAccountTableId != null) {
            firebaseDbClient.receiptAccount.child(receiptAccountTableId).setValue(receiptAccountTableObj)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        hideProgressDialog()
                        finish()
                    } else
                        hideProgressDialog()
                }
        } else
            hideProgressDialog()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Const.RequestCodes.ADD_CHART_OF_ACCOUNT_RECEIPT ->{
                if (data != null) {
                    accountName = data.getStringExtra(Const.KEYS.CHART_OF_ACCOUNT_NAME)
                    accountId = data.getStringExtra(Const.KEYS.CHART_OF_ACCOUNT_ID)
                    Timber.e("accountId -- " + accountId)
                    etAccounts.setText(accountName)
                }
            }
        }
    }


    private fun checkValidation():Boolean {

        if (TextUtils.isEmpty(etAccounts.text.toString().trim())) {
            showError("Please enter account")
            return false
        }

        if (TextUtils.isEmpty(etRemarksRec.text.toString().trim())) {
            showError(resources.getString(R.string.enter_remarks))
            return false
        }

        if (TextUtils.isEmpty(etCurrency.text.toString().trim())) {
            showError(resources.getString(R.string.enter_currency))
            return false
        }

        if (TextUtils.isEmpty(etRateRec.text.toString().trim())) {
            showError(resources.getString(R.string.enter_rate))
            return false
        }

        if (TextUtils.isEmpty(etCreditNoteNoRec.text.toString().trim())) {
            showError("Please enter credit note no.")
            return false
        }


        if (TextUtils.isEmpty(etTotalAmountRec.text.toString().trim())) {
            showError("Please enter total amount.")
            return false
        }

        if(!isNetworkConnected){
            showError(resources.getString(R.string.internet_error))
            return false
        }
        return true
    }

 
    /* Currency dialog */
    private fun showCurrencyDialog() {
        currencyList = currencyListFromDB
        if(currencyList.isEmpty()){
            showError(getString(R.string.no_currency_found))
            return
        }
        CurrencyDialog(this@ReceiptAccountActivity, currencyList,this).show(supportFragmentManager, resources.getString(R.string.app_name))
    }

    override fun onCurrencyClick(list: CurrencyTable) {
        currencyId = list?.id!!
        etCurrency.setText(list.currency)
    }
}