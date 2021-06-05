package com.agile.kouti.invoice.invoice_list

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.agile.kouti.db.CurrencyTable
import com.agile.kouti.db.FirebaseDbClient
import com.agile.kouti.db.crm.Crm
import com.agile.kouti.db.invoice.Invoice
import com.agile.kouti.db.invoice.MainItemInvoice
import com.agile.kouti.db.quotation.MainItemQuotation
import com.agile.kouti.db.quotation.Quotation
import com.agile.kouti.dialog.CurrencyDialog
import com.agile.kouti.dialog.CurrencyListener
import com.agile.kouti.invoice.main_item.MainItemInvoiceActivity
import com.agile.kouti.quotation.subItem.main_item.MainItemQuotationActivity
import com.agile.kouti.quotation.subItem.quotation.MainItemAdapter
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
import kotlinx.android.synthetic.main.activity_add_invoice2.*
import kotlinx.android.synthetic.main.activity_add_quotation.*
import kotlinx.android.synthetic.main.toolbar.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class AddInvoiceActivity : KoutiBaseActivity(), View.OnClickListener,
    KoutiBaseActivity.OnDateSelectedListener, KoutiBaseActivity.OnImageSelectedListener,
    MainItemInvoiceClickListener,CurrencyListener {

    private var mainItemList = ArrayList<MainItemInvoice>()
    private var mainItemListTemp = ArrayList<MainItemInvoice>()
    private val mainItemInvoiceAdapter = MainItemInvoiceAdapter(this, mainItemList, this)


    private var isEdit = false
    private var colorCode = "#EAE3E3"

    private var invoiceId = ""
    private var invoiceObj: Invoice? = null

    private var customerId = ""
    private var customerName = ""

    private var documentUrl = ""
    private var documentPath: Uri? = null

    private var currencyId = ""

    private var user_id = ""
    private var firebaseStore: FirebaseStorage? = null
    private var storageReference: StorageReference? = null

    private var totalAmount = 0

    private var currencyList = ArrayList<CurrencyTable>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_invoice2)
        initData()
    }

    private fun initData() {
        ivBack.setOnClickListener(this)
        etCurrencyInv.setOnClickListener(this)
        etCustomerInv.setOnClickListener(this)
        etDateInvoice.setOnClickListener(this)
        etCreditTermInv.setOnClickListener(this)
        tvAddMainItemInvoice.setOnClickListener(this)
        ivDocumentInvoice.setOnClickListener(this)
        btnAddInvoice.setOnClickListener(this)

        if (intent != null) {
            isEdit = intent.getBooleanExtra(Const.KEYS.IS_EDIT, false)
        }

        if (isEdit) {
            invoiceId = intent.getStringExtra(Const.KEYS.INVOICE_ID)
            tvToolbarTitle.text = resources.getString(R.string.toolbar_title_invoice)
            tvToolbarSubTitle.text = resources.getString(R.string.txt_edit)
            tvToolbarSubTitle.visibility = View.VISIBLE

            if (!TextUtils.isEmpty(invoiceId))
                getInvoiceData()

        } else {
            tvToolbarTitle.text = resources.getString(R.string.toolbar_title_invoice)
            tvToolbarSubTitle.text = resources.getString(R.string.txt_add)
            tvToolbarSubTitle.visibility = View.VISIBLE

            var timeStamp = (System.currentTimeMillis() / 100)
            etInvoiceNo.setText("" + timeStamp)
        }

        setMainItemAdapter()

        firebaseStore = FirebaseStorage.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        user_id = Preferences.getPreference(this, Const.SharedPrefs.USER_ID)

        /* Get currency list */
        getCurrencyListData()
    }

    /* Set Adapter */
    private fun setMainItemAdapter() {
        val layoutManager = LinearLayoutManager(this@AddInvoiceActivity, LinearLayoutManager.VERTICAL, false)
        rvMainItemInv.layoutManager = layoutManager
        rvMainItemInv.adapter = mainItemInvoiceAdapter
    }

    /* Get Invoice Data From invoice ID */
    private fun getInvoiceData() {
        showProgressDialog()
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.invoice.child(invoiceId).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    var invoiceObject: Invoice? = dataSnapshot.getValue(Invoice::class.java)
                    invoiceObj = invoiceObject
                    if (invoiceObj != null) {
                        setData()
                        hideProgressDialog()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                hideProgressDialog()
            }
        })
    }

    private fun setData() {
        etDateInvoice.setText(invoiceObj?.date)
        etStatusInvoice.setText(invoiceObj?.status)
        etInvoiceNo.setText(invoiceObj?.invoice_no)
        etCreditTermInv.setText(invoiceObj?.credit_term_days)

        getCustomerName()

        etCorrectAddressInv.setText(invoiceObj?.correct_address)
        etDeliveryAddressInv.setText(invoiceObj?.delivery_address)
        etCustomerPONoInv.setText(invoiceObj?.customer_po_no)
        etQuotationNumberInv.setText(invoiceObj?.quotation_no)
        etDebiteNo.setText(invoiceObj?.debite_no)
        etDeliveryNoteNo.setText(invoiceObj?.delivery_note_no)
        etPackingListNo.setText(invoiceObj?.packing_list_no)
        etDescriptionInv1.setText(invoiceObj?.description1)
        etDescriptionInv2.setText(invoiceObj?.description2)

        getCurrencyName()

        etTotalInv.setText(invoiceObj?.total)

        if (!TextUtils.isEmpty(invoiceObj!!.item_list!!)) {
            var result = invoiceObj!!.item_list!!.split(",").map { it.trim() }

            result.forEach {
                Timber.e("id -- " + it)
                if (!TextUtils.isEmpty(it))
                    getMainListFromIds(it)
            }
        }
    }

    private fun getMainListFromIds(mainListId: String) {
        Timber.e("mainListId -- " + mainListId)

        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.mainItemInvoice.child(mainListId.toString())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var mainListObj: MainItemInvoice? =
                            dataSnapshot.getValue(MainItemInvoice::class.java)
                        if (mainListObj != null) {
                            mainItemList.add(mainListObj)
                            // mainItemListTemp.add(mainListObj)

                            if (!TextUtils.isEmpty(mainListObj.amount)) {
                                totalAmount = totalAmount + mainListObj.amount!!.toInt()
                                etTotalInv.setText(totalAmount.toString())
                            }

                            mainItemInvoiceAdapter.notifyDataSetChanged()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

    }

    /* Get Customer Name */
    private fun getCustomerName() {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.crm.child(invoiceObj!!.customer!!)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var obj: Crm? = dataSnapshot.getValue(Crm::class.java)
                        if (obj != null) {
                            etCustomerInv.setText(obj!!.name)
                            currencyId = obj.id!!
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    /* Get Currency Name */
    private fun getCurrencyName() {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.currency.child(invoiceObj!!.currency!!)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var obj: CurrencyTable? = dataSnapshot.getValue(CurrencyTable::class.java)
                        if (obj != null) {
                            etCurrencyInv.setText(obj.currency)
                            currencyId = obj.id!!
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    /* Click Event */
    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.ivBack -> {
                finish()
            }

            R.id.etDateInvoice -> {
                setDateListener(this)
                showDatePickerDialog(true, Const.KEYS.DATE_DOB)
            }

            R.id.etCreditTermInv -> {
                setDateListener(this)
                showDatePickerDialog(true, Const.KEYS.TERM_DATE)
            }

            R.id.etCurrencyInv -> {
                showCurrencyDialog()
            }

            R.id.etCustomerInv -> {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra(Const.KEYS.SEARCH_TYPE, Const.SearchType.CustomerInvoice.toString())
                intent.putExtra(Const.KEYS.COLOR_CODE, colorCode)
                startActivityForResult(intent, Const.RequestCodes.CUSTOMER_INVOICE)
            }

            R.id.tvAddMainItemInvoice->{
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra(Const.KEYS.SEARCH_TYPE, Const.SearchType.AddMainItemInvoice.toString())
                intent.putExtra(Const.KEYS.COLOR_CODE, colorCode)
                startActivityForResult(intent, Const.RequestCodes.ADD_MAIN_ITEM_INVOICE)
            }

            R.id.ivDocumentInvoice->{
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

            R.id.btnAddInvoice ->{

               if(checkValidation()){
                   if (documentPath != null) {
                       uploadDocument()
                   } else
                       saveDataInDB()
               }
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

    /* Save Data In DB */
    private fun saveDataInDB() {
        showProgressDialog()
        if (!isEdit)
            invoiceId = FirebaseDatabase.getInstance().getReference(Const.TableName.RECEIPT).push().key.toString()

        /* Get Main id from list */
        var mainList = ""
        if (!mainItemList.isEmpty()) {
            for (list in mainItemList) {
                if (TextUtils.isEmpty(mainList))
                    mainList = list.id.toString()
                else
                    mainList = mainList + "," + list.id
            }
        }
        Timber.e("mainList -- " + mainList)
        var mainListSize = mainItemList.size

        var createdDate = ""
        createdDate = if (isEdit) {
            if (TextUtils.isEmpty(invoiceObj!!.created_date!!))
                utcDate
            else
                invoiceObj!!.created_date!!
        } else {
            utcDate
        }

        invoiceObj = Invoice(
            invoiceId,
            etDateInvoice.text.toString().trim(),
            etStatusInvoice.text.toString().trim(),
            etInvoiceNo.text.toString().trim(),
            etCreditTermInv.text.toString().trim(),
            customerId,
            etCorrectAddressInv.text.toString().trim(),
            etDeliveryAddressInv.text.toString().trim(),
            documentUrl,
            etCustomerPONoInv.text.toString().trim(),
            etQuotationNumberInv.text.toString().trim(),
            etDebiteNo.text.toString().trim(),
            etDeliveryNoteNo.text.toString().trim(),
            etPackingListNo.text.toString().trim(),
            etDescriptionInv1.text.toString().trim(),
            etDescriptionInv2.text.toString().trim(),
            currencyId,
            etTotalInv.text.toString().trim(),
            mainList,
            mainListSize.toString(),
            false,
            createdDate,
            user_id
        )

        var firebaseDbClient = FirebaseDbClient()
        if (invoiceId != null) {
            firebaseDbClient.invoice.child(invoiceId.toString()).setValue(invoiceObj)
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

    private fun checkValidation():Boolean {

        if (TextUtils.isEmpty(etDateInvoice.text.toString().trim())) {
            showError(getString(R.string.select_date))
            return false
        }
        if (TextUtils.isEmpty(etStatusInvoice.text.toString().trim())) {
            showError(getString(R.string.enter_status))
            return false
        }
        if (TextUtils.isEmpty(etCreditTermInv.text.toString().trim())) {
            showError(getString(R.string.enter_credit_term))
            return false
        }
        if (TextUtils.isEmpty(etCustomerInv.text.toString().trim())) {
            showError(getString(R.string.select_customer))
            return false
        }
        if (TextUtils.isEmpty(etCorrectAddressInv.text.toString().trim())) {
            showError(getString(R.string.enter_crr_add))
            return false
        }
        if (TextUtils.isEmpty(etDeliveryAddressInv.text.toString().trim())) {
            showError(getString(R.string.enter_deli_add))
            return false
        }
        if (TextUtils.isEmpty(etCustomerPONoInv.text.toString().trim())) {
            showError(getString(R.string.enter_po_no))
            return false
        }
        if (TextUtils.isEmpty(etQuotationNumberInv.text.toString().trim())) {
            showError("Please enter quotation no.")
            return false
        }
        if (TextUtils.isEmpty(etDebiteNo.text.toString().trim())) {
            showError("Please enter debite no.")
            return false
        }
        if (TextUtils.isEmpty(etDeliveryNoteNo.text.toString().trim())) {
            showError("Please enter delivery note no.")
            return false
        }
        if (TextUtils.isEmpty(etPackingListNo.text.toString().trim())) {
            showError("Please enter packing list no.")
            return false
        }
        if (TextUtils.isEmpty(etDescriptionInv1.text.toString().trim())) {
            showError(getString(R.string.enter_description_one))
            return false
        }
        if (TextUtils.isEmpty(etDescriptionInv2.text.toString().trim())) {
            showError(getString(R.string.enter_description_two))
            return false
        }
        if (TextUtils.isEmpty(etCurrencyInv.text.toString().trim())) {
            showError(getString(R.string.enter_currency))
            return false
        }

        if (mainItemList.isEmpty()) {
            showError("Please enter main item")
            return false
        }

        if (!isNetworkConnected) {
            showError(resources.getString(R.string.internet_error))
            return false
        }






        return true
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Const.RequestCodes.CUSTOMER_INVOICE -> {
                if (data != null) {
                    customerName = data.getStringExtra(Const.KEYS.SEARCH_TEXT)
                    customerId = data.getStringExtra(Const.KEYS.CUSTOMER_ID)
                    Timber.e("customerId -- " + customerId)
                    etCustomerInv.setText(customerName)
                }
            }

            Const.RequestCodes.ADD_MAIN_ITEM_INVOICE ->{
                if(data!=null){
                    var mainItemName = data.getStringExtra(Const.KEYS.SEARCH_TEXT)
                    var mainItemId = data.getStringExtra(Const.KEYS.MAIN_ITEM_ID_INVOICE)
                    Timber.e("itemId -- " + mainItemId)
                    Timber.e("itemName -- " + mainItemName)

                    var mFirebaseClient = FirebaseDbClient()
                    mFirebaseClient.mainItemInvoice.child(mainItemId)
                        .addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    var mainItemObj: MainItemInvoice? =
                                        dataSnapshot.getValue(MainItemInvoice::class.java)
                                    if (mainItemObj != null) {
                                        //mainItemListTemp.add(mainItemObj)
                                        mainItemList.add(mainItemObj)
                                        mainItemInvoiceAdapter.notifyDataSetChanged()
                                        totalAmount = totalAmount + mainItemObj.amount!!.toInt()
                                        etTotalInv.setText(totalAmount.toString())
                                    }
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                }
            }
        }
    }


    /* Item Click event */
    override fun onMainItemClick(list: MainItemInvoice) {
        val intent = Intent(this, MainItemInvoiceActivity::class.java)
        intent.putExtra(Const.KEYS.IS_EDIT, true)
        intent.putExtra(Const.KEYS.MAIN_ITEM_ID_INVOICE, list.id)
        startActivity(intent)
    }

    override fun onItemSelectClick(list: MainItemInvoice, pos: Int) {}

    override fun onSubItemClick(list: MainItemInvoice) {}


    override fun onGalleryImage(selectedGalleryImage: Uri?, imageType: String?) {
        if (selectedGalleryImage == null)
            return
        documentPath = selectedGalleryImage
        Timber.e("documentPath --- " + documentPath)

        Glide.with(ivDocumentInvoice!!)
            .load(R.drawable.document)
            .centerCrop()
            .placeholder(R.drawable.upload_document)
            .into(ivDocumentInvoice)
    }

    override fun onCameraImage(imageUri: Uri?, imageType: String?) {
        if (imageUri == null)
            return
        documentPath = imageUri
        Timber.e("documentPath --- " + documentPath)

        Glide.with(ivDocumentInvoice!!)
            .load(imageUri)
            .centerCrop()
            .placeholder(R.drawable.upload_document)
            .into(ivDocumentInvoice)
    }

    override fun onDateSelected(finalDate: String?, dateType: String?) {
        when (dateType) {
            Const.KEYS.DATE_DOB -> {
                etDateInvoice.setText(finalDate)
            }

            Const.KEYS.TERM_DATE -> {
                val sdf = SimpleDateFormat(Const.DateFormats.MMM_DD_YYYY)
                val currentDate = sdf.format(Date())
                val current_date: Date = sdf.parse(currentDate)
                val selected_date: Date = sdf.parse(finalDate)

                val diff: Long = current_date.getTime() - selected_date.getTime()
                val seconds = diff / 1000
                val minutes = seconds / 60
                val hours = minutes / 60
                var days = hours / 24

                Timber.e("days -- " + days)
                if (days < 0)
                    days *= (-1)

                etCreditTermInv.setText(days.toString())
            }
        }
    }


    /* Currency dialog */
    private fun showCurrencyDialog() {
        currencyList = currencyListFromDB
        if(currencyList.isEmpty()){
            showError(getString(R.string.no_currency_found))
            return
        }
        CurrencyDialog(this@AddInvoiceActivity, currencyList,this).show(supportFragmentManager, resources.getString(R.string.app_name))
    }

    override fun onCurrencyClick(list: CurrencyTable) {
        currencyId = list?.id!!
        etCurrencyInv.setText(list.currency)
    }
}