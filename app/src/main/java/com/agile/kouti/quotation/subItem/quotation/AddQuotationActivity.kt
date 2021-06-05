package com.agile.kouti.quotation.subItem.quotation

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.LinearLayoutManager
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.agile.kouti.db.CurrencyTable
import com.agile.kouti.db.FirebaseDbClient
import com.agile.kouti.db.crm.Crm
import com.agile.kouti.db.quotation.MainItemQuotation
import com.agile.kouti.db.quotation.Quotation
import com.agile.kouti.dialog.CurrencyDialog
import com.agile.kouti.dialog.CurrencyListener
import com.agile.kouti.quotation.subItem.main_item.MainItemQuotationActivity
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
import kotlinx.android.synthetic.main.activity_add_crm.*
import kotlinx.android.synthetic.main.activity_add_quotation.*
import kotlinx.android.synthetic.main.toolbar.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class AddQuotationActivity : KoutiBaseActivity(), View.OnClickListener,
    KoutiBaseActivity.OnDateSelectedListener, KoutiBaseActivity.OnImageSelectedListener,
    MainItemClickListener,CurrencyListener {

    private var mainItemList = ArrayList<MainItemQuotation>()
    private var mainItemListTemp = ArrayList<MainItemQuotation>()
    private val mainItemAdapter = MainItemAdapter(this, mainItemList, this)


    private var isEdit = false
    private var colorCode = "#EAE3E3"
    private var user_id = ""

    private var quotationId = ""
    private var quotationObj: Quotation? = null

    private var customerId = ""
    private var customerName = ""

    private var documentUrl = ""
    private var documentPath: Uri? = null

    private var currencyId = ""

    private var firebaseStore: FirebaseStorage? = null
    private var storageReference: StorageReference? = null

    private var totalAmount = 0

    private var currencyList = ArrayList<CurrencyTable>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_quotation)

        initData()
    }

    private fun initData() {
        ivBack.setOnClickListener(this)
        etCustomer.setOnClickListener(this)
        etDateQuotation.setOnClickListener(this)
        btnAddQuotation.setOnClickListener(this)
        etSelectCurrency.setOnClickListener(this)
        ivDocumentQuotation.setOnClickListener(this)
        tvAddMainItem.setOnClickListener(this)
        etCreditTermQuo.setOnClickListener(this)

        if (intent != null) {
            isEdit = intent.getBooleanExtra(Const.KEYS.IS_EDIT, false)
        }

        if (isEdit) {
            quotationId = intent.getStringExtra(Const.KEYS.QUOTATION_ID)
            tvToolbarTitle.text = resources.getString(R.string.toolbar_title_quotation)
            tvToolbarSubTitle.text = resources.getString(R.string.txt_edit)
            tvToolbarSubTitle.visibility = View.VISIBLE

            if (!TextUtils.isEmpty(quotationId))
                getQuotationData()

        } else {
            tvToolbarTitle.text = resources.getString(R.string.toolbar_title_quotation)
            tvToolbarSubTitle.text = resources.getString(R.string.txt_add)
            tvToolbarSubTitle.visibility = View.VISIBLE

            var timeStamp = (System.currentTimeMillis() / 100)
            etQuotationNo.setText("" + timeStamp)
        }

        setMainItemAdapter()

        firebaseStore = FirebaseStorage.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        user_id = Preferences.getPreference(this, Const.SharedPrefs.USER_ID)

        /* Get currency list */
        getCurrencyListData()
    }


    private fun setMainItemAdapter() {
        val layoutManager =
            LinearLayoutManager(this@AddQuotationActivity, LinearLayoutManager.VERTICAL, false)
        rvMainItem.layoutManager = layoutManager
        rvMainItem.adapter = mainItemAdapter
    }


    /* Get Quotation data from Id */
    private fun getQuotationData() {
        showProgressDialog()
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.quotation.child(quotationId).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    var quotationObject: Quotation? = dataSnapshot.getValue(Quotation::class.java)
                    quotationObj = quotationObject
                    if (quotationObj != null) {
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

    /* Set Data*/
    private fun setData() {
        etDateQuotation.setText(quotationObj!!.date)
        etStatus.setText(quotationObj!!.status)
        etQuotationNo.setText(quotationObj!!.quotation_no)


        customerId = quotationObj!!.customer!!

        getCustomerName()

        etCreditTermQuo.setText(quotationObj!!.credit_term_days)

        etCorrAddress.setText(quotationObj!!.correct_address)
        etDeliAddress.setText(quotationObj!!.delivery_address)
        etCustomerPONo.setText(quotationObj!!.customer_po_no)
        etDescription1.setText(quotationObj!!.description1)
        etDescription2.setText(quotationObj!!.description2)

        getCurrencyName()
        etTotalAmount.setText(quotationObj!!.total)

        documentUrl = quotationObj!!.upload_document!!
        Timber.e("documentUrl --- " + documentUrl)
        if(!TextUtils.isEmpty(documentUrl)) {
            Glide.with(ivDocumentQuotation!!)
                .load(documentUrl)
                .centerCrop() //4
                .placeholder(R.drawable.document)
                .into(ivDocumentQuotation)
        }


        if (!TextUtils.isEmpty(quotationObj!!.item_list!!)) {
            var result = quotationObj!!.item_list!!.split(",").map { it.trim() }

            result.forEach {
                Timber.e("id -- " + it)
                if (!TextUtils.isEmpty(it))
                    getMainListFromIds(it)
            }
        }

    }


    /* get Main List from Id */
    private fun getMainListFromIds(mainListId: String) {
        Timber.e("mainListId -- " + mainListId)
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.mainItemQuotation.child(mainListId.toString())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var mainListObj: MainItemQuotation? =
                            dataSnapshot.getValue(MainItemQuotation::class.java)
                        if (mainListObj != null) {
                            mainItemList.add(mainListObj)
                            // mainItemListTemp.add(mainListObj)

                            if (!TextUtils.isEmpty(mainListObj.amount)) {
                                totalAmount = totalAmount + mainListObj.amount!!.toInt()
                                etTotalAmount.setText(totalAmount.toString())
                            }

                            mainItemAdapter.notifyDataSetChanged()
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
        mFirebaseClient.currency.child(quotationObj!!.currency!!)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var obj: CurrencyTable? = dataSnapshot.getValue(CurrencyTable::class.java)
                        if (obj != null) {
                            etSelectCurrency.setText(obj.currency)
                            currencyId = obj.id!!
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    /* Get Customer Name */
    private fun getCustomerName() {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.crm.child(quotationObj!!.customer!!)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var obj: Crm? = dataSnapshot.getValue(Crm::class.java)
                        if (obj != null) {
                            etCustomer.setText(obj!!.name)
                            currencyId = obj.id!!
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

    }

    /* Click Events */
    override fun onClick(v: View?) {

        when (v?.id) {

            R.id.ivBack -> {
                finish()
            }

            R.id.ivDocumentQuotation -> {
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

            R.id.etSelectCurrency -> {
                showCurrencyDialog()
            }

            R.id.etDateQuotation -> {
                setDateListener(this)
                showDatePickerDialog(true, Const.KEYS.DATE_DOB)
            }

            R.id.etCreditTermQuo -> {
                setDateListener(this)
                showDatePickerDialog(true, Const.KEYS.TERM_DATE)
            }

            R.id.etCustomer -> {
                if (!isNetworkConnected) {
                    showError(resources.getString(R.string.internet_error))
                    return
                }
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra(Const.KEYS.SEARCH_TYPE, Const.SearchType.Customer.toString())
                intent.putExtra(Const.KEYS.COLOR_CODE, colorCode)
                startActivityForResult(intent, Const.RequestCodes.CUSTOMER)
            }

            R.id.tvAddMainItem -> {
                if (!isNetworkConnected) {
                    showError(resources.getString(R.string.internet_error))
                    return
                }
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra(
                    Const.KEYS.SEARCH_TYPE,
                    Const.SearchType.AddMainItemQuotation.toString()
                )
                intent.putExtra(Const.KEYS.COLOR_CODE, colorCode)
                startActivityForResult(intent, Const.RequestCodes.ADD_MAIN_ITEM_QUOTATION)
            }

            R.id.btnAddQuotation -> {
                hideKeyBoard(v)
                if (checkValidation()) {
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
        val ref =
            storageReference?.child(Const.ImageType.DOCUMENT + "/" + UUID.randomUUID().toString())
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
            quotationId = FirebaseDatabase.getInstance().getReference(Const.TableName.QUOTATION).push().key.toString()

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
        if (isEdit) {
            if (TextUtils.isEmpty(quotationObj!!.created_date!!))
                createdDate = utcDate
            else
                createdDate = quotationObj!!.created_date!!
        } else {
            createdDate = utcDate
        }

        quotationObj = Quotation(
            quotationId,
            etDateQuotation.text.toString().trim(),
            etStatus.text.toString().trim(),
            etQuotationNo.text.toString().trim(),
            customerId,
            etCorrAddress.text.toString().trim(),
            etDeliAddress.text.toString().trim(),
            etCustomerPONo.text.toString().trim(),
            etDescription1.text.toString().trim(),
            etDescription2.text.toString().trim(),
            currencyId,
            etTotalAmount.text.toString().trim(),
            documentUrl,
            mainList,
            mainListSize.toString(),
            false,
            createdDate,
            etCreditTermQuo.text.toString().trim(),
            user_id
        )

        var firebaseDbClient = FirebaseDbClient()
        if (quotationId != null) {
            firebaseDbClient.quotation.child(quotationId.toString()).setValue(quotationObj)
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
            Const.RequestCodes.CUSTOMER -> {
                if (data != null) {
                    customerName = data.getStringExtra(Const.KEYS.SEARCH_TEXT)
                    customerId = data.getStringExtra(Const.KEYS.CUSTOMER_ID)
                    Timber.e("customerId -- " + customerId)
                    etCustomer.setText(customerName)
                }
            }
            Const.RequestCodes.ADD_MAIN_ITEM_QUOTATION -> {
                if (data != null) {
                    var mainItemName = data.getStringExtra(Const.KEYS.SEARCH_TEXT)
                    var mainItemId = data.getStringExtra(Const.KEYS.MAIN_ITEM_ID)
                    Timber.e("itemId -- " + mainItemId)
                    Timber.e("itemName -- " + mainItemName)

                    var mFirebaseClient = FirebaseDbClient()
                    mFirebaseClient.mainItemQuotation.child(mainItemId)
                        .addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    var mainItemObj: MainItemQuotation? =
                                        dataSnapshot.getValue(MainItemQuotation::class.java)
                                    if (mainItemObj != null) {
                                        //mainItemListTemp.add(mainItemObj)
                                        mainItemList.add(mainItemObj)
                                        mainItemAdapter.notifyDataSetChanged()

                                        totalAmount = totalAmount + mainItemObj.amount!!.toInt()
                                        etTotalAmount.setText(totalAmount.toString())
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                }
            }
        }
    }

    /* Date select*/
    override fun onDateSelected(finalDate: String?, dateType: String?) {
        when (dateType) {
            Const.KEYS.DATE_DOB -> {
                etDateQuotation.setText(finalDate)
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

                etCreditTermQuo.setText(days.toString())
            }
        }
    }

    /* Check validation */
    private fun checkValidation(): Boolean {

        if (TextUtils.isEmpty(etDateQuotation.text.toString().trim())) {
            showError(getString(R.string.select_date))
            return false
        }
        if (TextUtils.isEmpty(etStatus.text.toString().trim())) {
            showError(getString(R.string.enter_status))
            return false
        }
        if (TextUtils.isEmpty(etCreditTermQuo.text.toString().trim())) {
            showError(getString(R.string.enter_credit_term))
            return false
        }
        if (TextUtils.isEmpty(etCustomer.text.toString().trim())) {
            showError(getString(R.string.select_customer))
            return false
        }

        if (TextUtils.isEmpty(etCorrAddress.text.toString().trim())) {
            showError(getString(R.string.enter_crr_add))
            return false
        }

        if (TextUtils.isEmpty(etDeliAddress.text.toString().trim())) {
            showError(getString(R.string.enter_deli_add))
            return false
        }
        if (TextUtils.isEmpty(etCustomerPONo.text.toString().trim())) {
            showError(getString(R.string.enter_po_no))
            return false
        }

        if (TextUtils.isEmpty(etDescription1.text.toString().trim())) {
            showError(getString(R.string.enter_description_one))
            return false
        }
        if (TextUtils.isEmpty(etDescription2.text.toString().trim())) {
            showError(getString(R.string.enter_description_two))
            return false
        }

        if (TextUtils.isEmpty(etSelectCurrency.text.toString().trim())) {
            showError(getString(R.string.enter_currency))
            return false
        }

        if (TextUtils.isEmpty(etTotalAmount.text.toString().trim())) {
            showError(getString(R.string.enter_total_amount))
            return false
        }

        if (mainItemList.isEmpty()) {
            showError("Please enter main item")
            return false
        }

        if (!isEdit) {
            if (documentPath == null) {
                showError("Please select document")
                return false
            }
        }

        if (!isNetworkConnected) {
            showError(resources.getString(R.string.internet_error))
            return false
        }

        return true
    }



    /* Gallery image*/
    override fun onGalleryImage(selectedGalleryImage: Uri?, imageType: String?) {
        if (selectedGalleryImage == null)
            return

        documentPath = selectedGalleryImage
        Timber.e("documentPath --- " + documentPath)

        Glide.with(ivDocumentQuotation)
            .load(R.drawable.document)
            .centerCrop() //4
            .placeholder(R.drawable.upload_document)
            .into(ivDocumentQuotation)
    }

    /* Camera Image*/
    override fun onCameraImage(imageUri: Uri?, imageType: String?) {
        if (imageUri == null)
            return
        documentPath = imageUri
        Timber.e("documentPath --- " + documentPath)
        displayImage(imageUri, ivDocumentQuotation)
    }

    private fun displayImage(selectedGalleryImage: Uri, imageID: AppCompatImageView?) {
        Glide.with(imageID!!)
            .load(selectedGalleryImage)
            .centerCrop() //4
            .placeholder(R.drawable.upload_document)
            .into(imageID)
    }

    /* Main Item Click */
    override fun onMainItemClick(list: MainItemQuotation) {
        val intent = Intent(this, MainItemQuotationActivity::class.java)
        intent.putExtra(Const.KEYS.IS_EDIT, true)
        intent.putExtra(Const.KEYS.MAIN_ITEM_ID, list.id)
        startActivity(intent)
    }

    override fun onItemSelectClick(list: MainItemQuotation, pos: Int) {}

    override fun onSubItemClick(list: MainItemQuotation) {}


    /* Currency dialog */
    private fun showCurrencyDialog() {
        currencyList = currencyListFromDB
        if(currencyList.isEmpty()){
            showError(getString(R.string.no_currency_found))
            return
        }
        CurrencyDialog(this@AddQuotationActivity, currencyList,this).show(supportFragmentManager, resources.getString(R.string.app_name))
    }

    override fun onCurrencyClick(list: CurrencyTable) {
        currencyId = list?.id!!
        etSelectCurrency.setText(list.currency)
    }
}