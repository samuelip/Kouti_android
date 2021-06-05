package com.agile.kouti.receipt

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.agile.kouti.db.CurrencyTable
import com.agile.kouti.db.FirebaseDbClient
import com.agile.kouti.db.crm.Crm
import com.agile.kouti.db.invoice.Invoice
import com.agile.kouti.db.receipt.Receipt
import com.agile.kouti.db.receipt.ReceiptAccount
import com.agile.kouti.dialog.CurrencyDialog
import com.agile.kouti.dialog.CurrencyListener
import com.agile.kouti.search.SearchActivity
import com.agile.kouti.utils.Const
import com.agile.kouti.utils.Preferences
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_add_receipt.*
import kotlinx.android.synthetic.main.toolbar.*
import timber.log.Timber
import java.util.*

class AddReceiptActivity : KoutiBaseActivity(), View.OnClickListener,
    KoutiBaseActivity.OnDateSelectedListener, KoutiBaseActivity.OnImageSelectedListener,AccountItemClickListener,CurrencyListener {


    private var isEdit = false
    private var colorCode = "#EAE3E3"

    private var customerId = ""
    private var customerName = ""

    private var receiptAccountId = ""
    private var receiptAccountName = ""

    private var invoiceId = ""
    private var invoiceName = ""
    private var invoiceNumber = ""

    private var documentUrl = ""
    private var documentPath: Uri? = null

    private var receiptId = ""
    private var receiptObj: Receipt? = null

    private var currencyId = ""

    private var receiptAccountList = ArrayList<ReceiptAccount>()
    private val receiptAccountAdapter = ReceiptAccountAdapter(this, receiptAccountList, this)

    private var user_id = ""
    private var firebaseStore: FirebaseStorage? = null
    private var storageReference: StorageReference? = null

    private var totalAmount = 0

    private var currencyList = ArrayList<CurrencyTable>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_receipt)
        initData()
    }


    private fun initData() {
        ivBack.setOnClickListener(this)
        etDateRec.setOnClickListener(this)
        etCustomerRec.setOnClickListener(this)
        ivDocumentRec.setOnClickListener(this)
        etCurrencyRec.setOnClickListener(this)
        //etAccounts.setOnClickListener(this)
        etInvoiceNoRec.setOnClickListener(this)
        btnAddReceipt.setOnClickListener(this)
        tvAddReceiptAccount.setOnClickListener(this)

        if (intent != null) {
            isEdit = intent.getBooleanExtra(Const.KEYS.IS_EDIT, false)
        }

        if (isEdit) {
            receiptId = intent.getStringExtra(Const.KEYS.RECEIPT_ID)
            tvToolbarTitle.text = resources.getString(R.string.toolbar_title_receipt)
            tvToolbarSubTitle.text = resources.getString(R.string.txt_edit)
            tvToolbarSubTitle.visibility = View.VISIBLE

            if (!TextUtils.isEmpty(receiptId))
                getReceiptData()

        } else {
            tvToolbarTitle.text = resources.getString(R.string.toolbar_title_receipt)
            tvToolbarSubTitle.text = resources.getString(R.string.txt_add)
            tvToolbarSubTitle.visibility = View.VISIBLE

            var timeStamp = (System.currentTimeMillis() / 100)
            etReceiptNo.setText("" + timeStamp)
        }

        firebaseStore = FirebaseStorage.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        user_id = Preferences.getPreference(this, Const.SharedPrefs.USER_ID)

        setAccountItemAdapter()

        getCurrencyListData()

    }

    /* Set Adapter */
    private fun setAccountItemAdapter() {
        val layoutManager = LinearLayoutManager(this@AddReceiptActivity, LinearLayoutManager.VERTICAL, false)
        rvReceiptAccountList.layoutManager = layoutManager
        rvReceiptAccountList.adapter = receiptAccountAdapter
    }

    /* Get Receipt Data */
    private fun getReceiptData() {
        showProgressDialog()
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.receipt.child(receiptId.toString())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var obj: Receipt? = dataSnapshot.getValue(Receipt::class.java)
                        if (obj != null) {
                            receiptObj = obj
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
        etDateRec.setText(receiptObj?.date)
        etReceiptNo.setText(receiptObj?.receipt_number)
        etCorrectAddressRec.setText(receiptObj?.correct_address)
        etDescriptionRec.setText(receiptObj?.description)
        etLocal.setText(receiptObj?.local)
        etDiscount.setText(receiptObj?.discount)
        etUnsettled.setText(receiptObj?.unsettled)
        etReturn.setText(receiptObj?.receipt_return)
        etReceive.setText(receiptObj?.receive)
        etTotal.setText(receiptObj?.total)
        etInvoiceRec.setText(receiptObj?.invoice)

        getCustomerName()
        getCurrencyName()
        getInvoiceNumber()

        documentUrl = receiptObj!!.upload_document!!
        if(!TextUtils.isEmpty(documentUrl)) {
            Glide.with(ivDocumentRec!!)
                .load(documentUrl)
                .centerCrop() //4
                .placeholder(R.drawable.document)
                .into(ivDocumentRec)
        }

        if (!TextUtils.isEmpty(receiptObj!!.receipt_accountIds!!)) {
            var result = receiptObj!!.receipt_accountIds!!.split(",").map { it.trim() }

            result.forEach {
                Timber.e("id -- " + it)
                if (!TextUtils.isEmpty(it))
                    getAccountListFromIds(it)
            }
        }
    }


    private fun getAccountListFromIds(accountId: String) {
        Timber.e("accountId -- " + accountId)
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.receiptAccount.child(accountId.toString())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var accountObj: ReceiptAccount? =
                            dataSnapshot.getValue(ReceiptAccount::class.java)
                        if (accountObj != null) {
                            receiptAccountList.add(accountObj)
                            if (!TextUtils.isEmpty(accountObj.total_amount)) {
                                totalAmount = totalAmount + accountObj.total_amount!!.toInt()
                                etTotal.setText(totalAmount.toString())
                            }
                            receiptAccountAdapter.notifyDataSetChanged()
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    /* Get Currency Name */
    private fun getCurrencyName() {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.currency.child(receiptObj!!.currency!!)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var obj: CurrencyTable? = dataSnapshot.getValue(CurrencyTable::class.java)
                        if (obj != null) {
                            etCurrencyRec.setText(obj.currency)
                            currencyId = obj?.id!!
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    /* Get Customer Name */
    private fun getCustomerName() {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.crm.child(receiptObj!!.customer!!)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var obj: Crm? = dataSnapshot.getValue(Crm::class.java)
                        if (obj != null) {
                            etCustomerRec.setText(obj.name)
                            customerId = obj?.id!!
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    /* Get Invoice Number */
    private fun getInvoiceNumber() {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.invoice.child(receiptObj!!.invoice_no!!)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var obj: Invoice? = dataSnapshot.getValue(Invoice::class.java)
                        if (obj != null) {
                            invoiceId = obj?.id!!
                            etInvoiceNoRec.setText(obj.invoice_no)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

    }

    override fun onClick(v: View?) {

        when(v?.id){
            R.id.ivBack ->{
                finish()
            }

            R.id.etDateRec ->{
                setDateListener(this)
                showDatePickerDialog(true, Const.KEYS.DATE_DOB)
            }
            R.id.etCustomerRec->{
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra(Const.KEYS.SEARCH_TYPE, Const.SearchType.CustomerReceipt.toString())
                intent.putExtra(Const.KEYS.COLOR_CODE, colorCode)
                startActivityForResult(intent, Const.RequestCodes.CUSTOMER_RECEIPT)
            }

            R.id.ivDocumentRec->{
                setListener(this)
                if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)) {
                    if (checkPermission())
                        showImagePickerDialog(Const.KEYS.DOCUMENT)
                    else
                        showImagePickerDialog(Const.KEYS.DOCUMENT)
                } else {
                    showImagePickerDialog(Const.KEYS.DOCUMENT)
                }
            }
            R.id.etCurrencyRec->{
                showCurrencyDialog()
            }

            R.id.etInvoiceNoRec->{
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra(Const.KEYS.SEARCH_TYPE, Const.SearchType.Invoice.toString())
                intent.putExtra(Const.KEYS.COLOR_CODE, colorCode)
                startActivityForResult(intent, Const.RequestCodes.INVOICE)
            }
            R.id.btnAddReceipt->{
                if(checkValidation()){
                    if (documentPath != null) {
                        uploadDocument()
                    } else
                        saveDataInDB()
                }
            }

            R.id.tvAddReceiptAccount->{
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra(Const.KEYS.SEARCH_TYPE, Const.SearchType.ReceiptAccount.toString())
                intent.putExtra(Const.KEYS.COLOR_CODE, colorCode)
                startActivityForResult(intent, Const.RequestCodes.RECEIPT_ACCOUNT)
            }

        }

    }

    /* Upload Document */
    private fun uploadDocument() {
        showProgressDialog()
        val ref = storageReference?.child(Const.ImageType.DOCUMENT + "/" + UUID.randomUUID().toString())
        val uploadTask = ref?.putFile(documentPath!!)

        val urlTask =
            uploadTask?.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation ref.downloadUrl
            })?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    documentUrl = task.result.toString()
                    Timber.e("documentUrl ==== " + documentUrl)
                    hideProgressDialog()
                    saveDataInDB()
                } else {
                    hideProgressDialog()
                    showError(getString(R.string.error_image_upload_failed))
                }
            }?.addOnFailureListener {
                hideProgressDialog()
                showError(getString(R.string.error_failed_try_again))
            }

    }

    /* Save data in DB*/
    private fun saveDataInDB() {
        showProgressDialog()
        if (!isEdit)
            receiptId = FirebaseDatabase.getInstance().getReference(Const.TableName.RECEIPT).push().key.toString()

        var createdDate = ""
        createdDate = if (isEdit) {
            if (TextUtils.isEmpty(receiptObj!!.created_date!!))
                utcDate
            else
                receiptObj!!.created_date!!
        } else {
            utcDate
        }

        /* Get receipt account id */
        var accountIds = ""
        if (!receiptAccountList.isEmpty()) {
            for (list in receiptAccountList) {
                if (TextUtils.isEmpty(accountIds))
                    accountIds = list.id.toString()
                else
                    accountIds = accountIds + "," + list.id
            }
        }
        Timber.e("accountIds -- " + accountIds)

        receiptObj = Receipt(
            receiptId,
            etDateRec.text.toString().trim(),
            etReceiptNo.text.toString().trim(),
            customerId,
            etCorrectAddressRec.text.toString().trim(),
            etDescriptionRec.text.toString().trim(),
            currencyId,
            etLocal.text.toString().trim(),
            etDiscount.text.toString().trim(),
            etUnsettled.text.toString().trim(),
            etReturn.text.toString().trim(),
            etReceive.text.toString().trim(),
            etTotal.text.toString().trim(),
            documentUrl,
            createdDate,
            false,
            user_id,
            invoiceId,
            etInvoiceRec.text.toString().trim(),
            accountIds
        )

        var firebaseDbClient = FirebaseDbClient()
        if (receiptId != null) {
            firebaseDbClient.receipt.child(receiptId.toString()).setValue(receiptObj)
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

            Const.RequestCodes.CUSTOMER_RECEIPT -> {
                if (data != null) {
                    customerName = data.getStringExtra(Const.KEYS.SEARCH_TEXT)
                    customerId = data.getStringExtra(Const.KEYS.CUSTOMER_ID)
                    Timber.e("customerId -- " + customerId)
                    etCustomerRec.setText(customerName)
                }
            }

            Const.RequestCodes.INVOICE ->{
                if (data != null) {
                    invoiceNumber = data.getStringExtra(Const.KEYS.SEARCH_TEXT)
                    invoiceId = data.getStringExtra(Const.KEYS.INVOICE_ID)

                    Timber.e("invoiceId -- " + invoiceId)
                    etInvoiceNoRec.setText(invoiceNumber)
                }
            }

            Const.RequestCodes.RECEIPT_ACCOUNT ->{
                if (data != null) {
                    receiptAccountName =  data.getStringExtra(Const.KEYS.SEARCH_TEXT)
                    receiptAccountId = data.getStringExtra(Const.KEYS.RECEIPT_ACCOUNT_ID)
                    Timber.e("receiptAccountId -- " + receiptAccountId)

                    var mFirebaseClient = FirebaseDbClient()
                    mFirebaseClient.receiptAccount.child(receiptAccountId)
                        .addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    var accountObj: ReceiptAccount? = dataSnapshot.getValue(ReceiptAccount::class.java)
                                    if (accountObj != null) {
                                        receiptAccountList.add(accountObj)
                                        receiptAccountAdapter.notifyDataSetChanged()

                                        totalAmount = totalAmount + accountObj.total_amount!!.toInt()
                                        etTotal.setText(totalAmount.toString())

                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })
                }
            }
        }
    }

    /* Check Validation*/
    private fun checkValidation(): Boolean {

        if (TextUtils.isEmpty(etDateRec.text.toString().trim())) {
            showError(getString(R.string.select_date))
            return false
        }
        if (TextUtils.isEmpty(etDateRec.text.toString().trim())) {
            showError(getString(R.string.select_date))
            return false
        }
        if (TextUtils.isEmpty(etCustomerRec.text.toString().trim())) {
            showError(getString(R.string.select_customer))
            return false
        }

        if (TextUtils.isEmpty(etInvoiceNoRec.text.toString().trim())) {
            showError("Please enter invoice no.")
            return false
        }

        if (TextUtils.isEmpty(etInvoiceRec.text.toString().trim())) {
            showError("Please enter invoice")
            return false
        }
        if (TextUtils.isEmpty(etCorrectAddressRec.text.toString().trim())) {
            showError(getString(R.string.enter_crr_add))
            return false
        }
        if (TextUtils.isEmpty(etDescriptionRec.text.toString().trim())) {
            showError(getString(R.string.enter_description))
            return false
        }
        if (TextUtils.isEmpty(etDescriptionRec.text.toString().trim())) {
            showError(getString(R.string.enter_description))
            return false
        }
        if (TextUtils.isEmpty(etCurrencyRec.text.toString().trim())) {
            showError(getString(R.string.enter_currency))
            return false
        }

        if (TextUtils.isEmpty(etLocal.text.toString().trim())) {
            showError("Please enter local.")
            return false
        }

        if (TextUtils.isEmpty(etDiscount.text.toString().trim())) {
            showError("Please enter discount.")
            return false
        }

        if (TextUtils.isEmpty(etUnsettled.text.toString().trim())) {
            showError("Please enter unsettled.")
            return false
        }

        if (TextUtils.isEmpty(etReturn.text.toString().trim())) {
            showError("Please enter return.")
            return false
        }

        if (TextUtils.isEmpty(etReceive.text.toString().trim())) {
            showError("Please enter receive.")
            return false
        }

        if (TextUtils.isEmpty(etTotal.text.toString().trim())) {
            showError("Please enter total.")
            return false
        }

        if (!isEdit) {
            if (documentPath == null) {
                showError("Please select document.")
                return false
            }
        }

        if(!isNetworkConnected){
            showError(resources.getString(R.string.internet_error))
            return false
        }
        return true
    }

    override fun onGalleryImage(selectedGalleryImage: Uri?, imageType: String?) {
        if (selectedGalleryImage == null)
            return
        documentPath = selectedGalleryImage
        Timber.e("documentPath --- " + documentPath)

        Glide.with(ivDocumentRec!!)
            .load(R.drawable.document)
            .centerCrop()
            .placeholder(R.drawable.upload_document)
            .into(ivDocumentRec)
    }

    override fun onCameraImage(imageUri: Uri?, imageType: String?) {
        if (imageUri == null)
            return
        documentPath = imageUri
        Timber.e("documentPath --- " + documentPath)

        Glide.with(ivDocumentRec!!)
            .load(imageUri)
            .centerCrop()
            .placeholder(R.drawable.upload_document)
            .into(ivDocumentRec)
    }

    override fun onDateSelected(finalDate: String?, dateType: String?) {
        etDateRec.setText(finalDate)
    }

    override fun onAccountItemClick(list: ReceiptAccount) {
        val intent = Intent(this, ReceiptAccountActivity::class.java)
        intent.putExtra(Const.KEYS.IS_EDIT,true)
        intent.putExtra(Const.KEYS.RECEIPT_ACCOUNT_ID,list.id)
        startActivity(intent)

    }



    /* Currency dialog */
    private fun showCurrencyDialog() {
        currencyList = currencyListFromDB
        if(currencyList.isEmpty()){
            showError(getString(R.string.no_currency_found))
            return
        }
        CurrencyDialog(this@AddReceiptActivity, currencyList,this).show(supportFragmentManager, resources.getString(R.string.app_name))
    }

    override fun onCurrencyClick(list: CurrencyTable) {
        currencyId = list?.id!!
        etCurrencyRec.setText(list.currency)
    }

}