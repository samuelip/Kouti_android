package com.agile.kouti.crm

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.LinearLayoutManager
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.agile.kouti.db.ChartOfAccount
import com.agile.kouti.db.CurrencyTable
import com.agile.kouti.db.FirebaseDbClient
import com.agile.kouti.db.crm.Crm
import com.agile.kouti.db.crm.Group
import com.agile.kouti.db.crm.Person
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
import kotlinx.android.synthetic.main.activity_add_crm.*
import kotlinx.android.synthetic.main.activity_add_crm.btnSave
import kotlinx.android.synthetic.main.activity_add_crm.ivDocument
import kotlinx.android.synthetic.main.activity_add_receipt.*
import kotlinx.android.synthetic.main.activity_crm_detail.*
import kotlinx.android.synthetic.main.activity_payroll_expense_detail.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.toolbar.ivBack
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*


class AddCrmActivity : KoutiBaseActivity(), View.OnClickListener,
    KoutiBaseActivity.OnImageSelectedListener, KoutiBaseActivity.OnDateSelectedListener,
    PersonItemClickListener,CurrencyListener {

    private var personList = ArrayList<Person>()
    private val personAdapter = PersonAdapter(this, personList, this)

    private var firebaseStore: FirebaseStorage? = null
    private var storageReference: StorageReference? = null

    private var isEdit = false
    private var colorCode = "#EAE3E3"

    private var groupId = ""
    private var groupName = ""

    private var personId = ""
    private var personName = ""

    private var accountId = ""
    private var accountName = ""

    private var referredCustomerName = ""
    private var referredCustomerId = ""

    private var crmId = ""
    var crmObj: Crm? = null

    private var documentUrl = ""
    private var documentPath: Uri? = null

    private var idUrl = ""
    private var idPath: Uri? = null

    private var code = ""
    private var currencyId = ""

    private var createdDate = ""

    var userId: String = ""

    private var currencyList = ArrayList<CurrencyTable>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_crm)

        initData()
    }

    private fun initData() {
        etGroup.setOnClickListener(this)
        ivDocument.setOnClickListener(this)
        ivID.setOnClickListener(this)
        etGender.setOnClickListener(this)
        etDob.setOnClickListener(this)
        etJoinDate.setOnClickListener(this)
        etExpiryDate.setOnClickListener(this)
        etTermNoOrDate.setOnClickListener(this)
        etDefaultCurrency.setOnClickListener(this)
        etReturnAccount.setOnClickListener(this)
        etReferredCustomer.setOnClickListener(this)
        etMaritalStatus.setOnClickListener(this)
        tvAddPerson.setOnClickListener(this)
        tvOtherAddress.setOnClickListener(this)
        btnSave.setOnClickListener(this)
        ivBack.setOnClickListener(this)

        tvToolbarTitle.text = getString(R.string.toolbar_title_crm)
        if (intent != null) {
            isEdit = intent.getBooleanExtra(Const.KEYS.IS_EDIT, false)

            if (isEdit) {
                tvToolbarSubTitle.text = getString(R.string.txt_edit)
                tvToolbarSubTitle.visibility = View.VISIBLE
                crmId = intent.getStringExtra(Const.KEYS.CRM_ID)

                if (!TextUtils.isEmpty(crmId) && crmId != null)
                    getCrmDataFromID()

            } else {
                tvToolbarSubTitle.text = getString(R.string.txt_add)
                tvToolbarSubTitle.visibility = View.GONE
                colorCode = randomColor.toString()
                code = (System.currentTimeMillis() / 1000).toString()
                etCode.setText(code)
            }
        }
        firebaseStore = FirebaseStorage.getInstance()
        storageReference = FirebaseStorage.getInstance().reference

        setPersonAdapter()

        userId = Preferences.getPreference(this@AddCrmActivity, Const.SharedPrefs.USER_ID)

        /* Get currency list */
        getCurrencyListData()
    }

    /* Set PersonList Adapter*/
    private fun setPersonAdapter() {
        val layoutManager =
            LinearLayoutManager(this@AddCrmActivity, LinearLayoutManager.VERTICAL, false)
        rvPerson.layoutManager = layoutManager
        rvPerson.adapter = personAdapter
    }

    /* Get CRM data from CRM ID*/
    private fun getCrmDataFromID() {
        showProgressDialog()
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.crm.child(crmId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var crmObject: Crm? = dataSnapshot.getValue(Crm::class.java)
                        crmObj = crmObject
                        Timber.e("crmObje  -- " + crmObj!!.name)
                        setCrmData()
                        hideProgressDialog()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    hideProgressDialog()
                }
            })
    }

    private fun setCrmData() {
        etCode.setText(crmObj!!.code)
        etName.setText(crmObj!!.name)
        etTelephone.setText(crmObj!!.telephone)
        etMobile.setText(crmObj!!.mobile)
        etFax.setText(crmObj!!.fax)
        etEmail.setText(crmObj!!.email)
        etKeyWord.setText(crmObj!!.keyword)
        etCoins.setText(crmObj!!.coins)

        // Get group name from id
        getGroupNameById(crmObj!!.group)
        groupId = crmObj!!.group!!

        etBusinessRegisterNo.setText(crmObj!!.business_register_no)

        // Document url
        displayImageUrl(crmObj!!.document_url!!, ivDocument)
        documentUrl = crmObj!!.document_url!!

        etContact.setText(crmObj!!.contact)

        // ID Url
        displayImageUrl(crmObj!!.id_url!!, ivID)
        idUrl = crmObj!!.id_url!!

        etGender.setText(crmObj!!.gender)
        etDob.setText(crmObj!!.birthday)
        etRemarks.setText(crmObj!!.remark)
        etJoinDate.setText(crmObj!!.join_date)
        etExpiryDate.setText(crmObj!!.membership_expiry_date)
        etDefaultAddress.setText(crmObj!!.default_address)
        etDeliveryAddress.setText(crmObj!!.delivery_address)
        etOtherAddress.setText(crmObj!!.other_address)
        etCreditLimit.setText(crmObj!!.credit_limit)
        etTermNoOrDate.setText(crmObj!!.term_days)
        etContact2.setText(crmObj!!.contact1)

        // Get Currency Name From ID
        getCurrencyName(crmObj!!.currency)
        currencyId = crmObj!!.currency!!

        // Get Account name From ID
        getAccountName(crmObj!!.return_account)
        accountId = crmObj!!.return_account!!

        // Get Referred Customer name from Person table
        getReferredCustomerName(crmObj!!.referred_customer)
        referredCustomerId = crmObj!!.referred_customer!!

        etHeight.setText(crmObj!!.height)
        etWeight.setText(crmObj!!.weight)
        etOccupation.setText(crmObj!!.occupation)
        etMaritalStatus.setText(crmObj!!.marital_status)



        if(TextUtils.isEmpty(crmObj!!.person_detail))
            return

        var result = crmObj!!.person_detail!!.split(",").map { it.trim() }

        result.forEach {
            Timber.e("id -- " + it)
            if (!TextUtils.isEmpty(it))
                getPersonListFromIds(it)
        }

    }


    /* Click Event */
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivBack -> {
                finish()
            }
            R.id.etGroup -> {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra(Const.KEYS.SEARCH_TYPE, Const.SearchType.Group.toString())
                intent.putExtra(Const.KEYS.COLOR_CODE, colorCode)
                startActivityForResult(intent, Const.RequestCodes.GROUP)
            }

            R.id.ivDocument -> {
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

            R.id.ivID -> {
                setListener(this)
                if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)) {
                    if (checkPermission())
                        showImagePickerDialog(Const.KEYS.ID)
                    else
                        showImagePickerDialog(Const.KEYS.ID)
                } else {
                    showImagePickerDialog(Const.KEYS.ID)
                }
            }

            R.id.etGender -> {
                showGenderDialog()
            }

            R.id.etDob -> {
                setDateListener(this)
                showDatePickerDialog(false, Const.KEYS.DATE_DOB)

            }

            R.id.etJoinDate -> {
                setDateListener(this)
                showDatePickerDialog(true, Const.KEYS.JOIN_DATE)
            }

            R.id.etExpiryDate -> {
                setDateListener(this)
                showDatePickerDialog(true, Const.KEYS.EXPIRY_DATE)
            }

            R.id.etTermNoOrDate -> {
                setDateListener(this)
                showDatePickerDialog(true, Const.KEYS.TERM_DATE)
            }

            R.id.etDefaultCurrency -> {
                showCurrencyDialog()
            }

            R.id.etReturnAccount -> {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra(Const.KEYS.SEARCH_TYPE, Const.SearchType.ChartOfAccount.toString())
                intent.putExtra(Const.KEYS.COLOR_CODE, colorCode)
                startActivityForResult(intent, Const.RequestCodes.ADD_CHART_OF_ACCOUNT)
            }

            R.id.etReferredCustomer -> {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra(
                    Const.KEYS.SEARCH_TYPE,
                    Const.SearchType.ReferredCustomer.toString()
                )
                intent.putExtra(Const.KEYS.COLOR_CODE, colorCode)
                startActivityForResult(intent, Const.RequestCodes.REFERRED_CUSTOMER)
            }

            R.id.etMaritalStatus -> {
                showMaritalStatusDialog()
            }

            R.id.tvAddPerson -> {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra(Const.KEYS.SEARCH_TYPE, Const.SearchType.Person.toString())
                intent.putExtra(Const.KEYS.COLOR_CODE, colorCode)
                startActivityForResult(intent, Const.RequestCodes.ADD_PERSON)
            }

            R.id.tvOtherAddress -> {
                etOtherAddress.visibility = View.VISIBLE
            }

            R.id.btnSave -> {
                if (checkValidation()) {
                    if (isEdit) {
                        if (documentPath == null && idPath == null)
                            makeCrmTableObject()
                        else if (documentPath != null && idPath == null)
                            uploadDoc()
                        else if (documentPath == null && idPath != null)
                            uploadId()

                    } else {
                        uploadDoc()
                    }

                }
            }
        }
    }

    /* Upload document file */
    private fun uploadDoc() {
        showProgressDialog()
        if (documentPath != null) {
            val ref =
                storageReference?.child(
                    Const.ImageType.DOCUMENT + "/" + UUID.randomUUID().toString()
                )
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
                        hideProgressDialog()
                        if (idPath != null)
                            uploadId()
                        else
                            saveDataInDB()

                    } else {
                        hideProgressDialog()
                        showError(getString(R.string.error_image_upload_failed))
                    }
                }?.addOnFailureListener {
                    hideProgressDialog()
                    showError(getString(R.string.error_failed_try_again))
                }
        } else {
            hideProgressDialog()
            Toast.makeText(this, getString(R.string.error_upload_image), Toast.LENGTH_SHORT).show()
        }

    }

    /* Upload ID file */
    private fun uploadId() {

        showProgressDialog()
        if (idPath != null) {
            val ref =
                storageReference?.child(Const.ImageType.ID + "/" + UUID.randomUUID().toString())
            val uploadTask = ref?.putFile(idPath!!)

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
                        idUrl = task.result.toString()

                        saveDataInDB()

                    } else {
                        hideProgressDialog()
                        showError(getString(R.string.error_image_upload_failed))
                    }
                }?.addOnFailureListener {
                    hideProgressDialog()
                    showError(getString(R.string.error_failed_try_again))
                }
        } else {
            hideProgressDialog()
            Toast.makeText(this, getString(R.string.error_upload_image), Toast.LENGTH_SHORT).show()
        }

    }

    private fun saveDataInDB() {
        makeCrmTableObject()
    }

    /* Prepare data for insert value */
    private fun makeCrmTableObject() {
        val defaultAddress =
            etDefaultAddress.text.toString().trim() + " " + etDefaultAddress2.text.toString().trim()
        val deliveryAddress =
            etDeliveryAddress.text.toString().trim() + " " + etDeliveryAddress2.text.toString()
                .trim()
        val otherAddress = etOtherAddress.text.toString().trim()

        var personIdList = ""

        if (!personList.isEmpty()) {
            for (list in personList) {
                if (TextUtils.isEmpty(personIdList)) {
                    personIdList = list.id.toString()
                } else {
                    personIdList = personIdList + "," + list.id
                }
            }
        }

        if (!isEdit) {
            createdDate = utcDate
            crmId = FirebaseDatabase.getInstance().getReference(Const.TableName.CRM)
                .push().key.toString()
        } else {
            if (TextUtils.isEmpty(crmObj!!.created_date) || crmObj!!.created_date == null)
                createdDate = utcDate
            else
                createdDate = crmObj!!.created_date!!
        }


        if (TextUtils.isEmpty(crmId)) {
            showError("ID not found")
            return
        }

        crmObj = Crm(
            crmId,
            etCode.text.toString().trim(),
            etName.text.toString().trim(),
            etTelephone.text.toString().trim(),
            etMobile.text.toString().trim(),
            etFax.text.toString().trim(),
            etEmail.text.toString().trim(),
            etKeyWord.text.toString().trim(),
            etCoins.text.toString().trim(),
            groupId,
            etBusinessRegisterNo.text.toString().trim(),
            etContact.text.toString().trim(),
            etGender.text.toString().trim(),
            etDob.text.toString().trim(),
            etRemarks.text.toString().trim(),
            etJoinDate.text.toString().trim(),
            etExpiryDate.text.toString().trim(),
            defaultAddress,
            deliveryAddress,
            otherAddress,
            etCreditLimit.text.toString().trim(),
            etTermNoOrDate.text.toString().trim(),
            etContact2.text.toString().trim(),
            currencyId,
            accountId,
            referredCustomerId,
            etHeight.text.toString().trim(),
            etWeight.text.toString().trim(),
            etOccupation.text.toString().trim(),
            etMaritalStatus.text.toString().trim(),
            personIdList,
            documentUrl,
            idUrl,
            false,
            createdDate,
            userId
        )


        var firebaseDbClient = FirebaseDbClient();
        if (crmId != null) {
            firebaseDbClient.crm.child(crmId).setValue(crmObj).addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    showAlert("Success")
                    finish()
                }
                hideProgressDialog()
            }
        } else {
            hideProgressDialog()
        }
    }


    /* Get Group name from Search */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Const.RequestCodes.GROUP -> {
                if (data != null) {
                    groupName = data.getStringExtra(Const.KEYS.SEARCH_TEXT)
                    etGroup.setText(groupName)
                    groupId = data.getStringExtra(Const.KEYS.GROUP_ID)
                    Timber.e("groupId -- " + groupId)
                }
            }
            Const.RequestCodes.ADD_PERSON -> {
                if (data != null) {
                    personName = data.getStringExtra(Const.KEYS.SEARCH_TEXT)
                    personId = data.getStringExtra(Const.KEYS.PERSON_ID)
                    Timber.e("personId --- " + personId)

                    var mFirebaseClient = FirebaseDbClient()
                    mFirebaseClient.person.child(personId).addListenerForSingleValueEvent(object :
                        ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                var personObj: Person? = dataSnapshot.getValue(Person::class.java)
                                Timber.e("personObj --- " + personObj!!.birthday)
                                personList.add(personObj)
                                personAdapter.notifyDataSetChanged()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
                }
            }

            Const.RequestCodes.ADD_CHART_OF_ACCOUNT -> {
                if (data != null) {
                    accountName = data.getStringExtra(Const.KEYS.CHART_OF_ACCOUNT_NAME)
                    accountId = data.getStringExtra(Const.KEYS.CHART_OF_ACCOUNT_ID)
                    etReturnAccount.setText(accountName)
                }
            }
            Const.RequestCodes.REFERRED_CUSTOMER -> {
                if (data != null) {
                    referredCustomerId = data.getStringExtra(Const.KEYS.REFERRED_CUSTOMER_ID)
                    referredCustomerName = data.getStringExtra(Const.KEYS.REFERRED_CUSTOMER_NAME)
                    etReferredCustomer.setText(referredCustomerName)
                }
            }
        }
    }

    override fun onGalleryImage(selectedGalleryImage: Uri, imageType: String) {
        if (selectedGalleryImage == null)
            return

        if (imageType == Const.KEYS.DOCUMENT) {
            //displayImage(selectedGalleryImage, ivDocument)
            documentPath = selectedGalleryImage

            if(selectedGalleryImage!=null){
                Glide.with(ivDocument!!)
                    .load(selectedGalleryImage)
                    .centerCrop() //4
                    .placeholder(R.drawable.document)
                    .into(ivDocument)
            }

        } else if (imageType == Const.KEYS.ID) {
            displayImage(selectedGalleryImage, ivID)
            idPath = selectedGalleryImage
        }
    }

    override fun onCameraImage(imageUri: Uri, imageType: String)  {
        if (imageUri == null)
            return
        if (imageType == Const.KEYS.DOCUMENT) {
            //displayImage(imageUri, ivDocument)
            documentPath = imageUri

            if(imageUri!=null){
                Glide.with(ivDocument!!)
                    .load(imageUri)
                    .centerCrop()
                    .placeholder(R.drawable.document)
                    .into(ivDocument)
            }
        } else if (imageType == Const.KEYS.ID) {
            displayImage(imageUri, ivID)
            idPath = imageUri
        }
    }

    private fun displayImage(selectedGalleryImage: Uri, imageID: AppCompatImageView?) {
        Glide.with(imageID!!)
            .load(selectedGalleryImage)
            .centerCrop() //4
            .placeholder(R.drawable.upload_document)
            .into(imageID)
    }

    private fun displayImageUrl(imageUrl: String, imageID: AppCompatImageView?) {
        Glide.with(imageID!!)
            .load(imageUrl)
            .centerCrop() //4
            .placeholder(R.drawable.upload_document)
            .into(imageID)
    }

    /* Select Gender Dialog */
    private fun showGenderDialog() {
        val items = arrayOf("Male", "Female", "Other")
        AlertDialog.Builder(this)
            .setTitle("Select Gender")
            .setCancelable(true)
            .setItems(items) { dialog, which ->
                etGender.setText(items[which])
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.cancel()
            }
            .show()
    }

    /* Select Gender Dialog */
    private fun showMaritalStatusDialog() {
        val items = arrayOf("Married", "Unmarried")
        AlertDialog.Builder(this)
            .setTitle("Select Status")
            .setCancelable(true)
            .setItems(items) { dialog, which ->
                etMaritalStatus.setText(items[which])
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.cancel()
            }
            .show()
    }

    /* Display Selected Date */
    override fun onDateSelected(finalDate: String, dateType: String) {
        when (dateType) {
            Const.KEYS.DATE_DOB -> {
                etDob.setText(finalDate)
            }
            Const.KEYS.JOIN_DATE -> {
                etJoinDate.setText(finalDate)
            }
            Const.KEYS.EXPIRY_DATE -> {
                etExpiryDate.setText(finalDate)
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

                etTermNoOrDate.setText(days.toString())
            }
        }
    }

    /* Check Validation */
    private fun checkValidation(): Boolean {

        if (TextUtils.isEmpty(etName.text.toString().trim())) {
            showError(resources.getString(R.string.error_enter_name))
            return false
        }
        if (TextUtils.isEmpty(etTelephone.text.toString().trim())) {
            showError(getString(R.string.enter_phone_no))
            return false
        }
        if (etTelephone.text.toString().trim().length < 10) {
            showError(getString(R.string.enter_valid_phone_no))
            return false
        }
        if (TextUtils.isEmpty(etMobile.text.toString().trim())) {
            showError(getString(R.string.enter_mobile_no))
            return false
        }
        if (etMobile.text.toString().trim().length < 10) {
            showError(getString(R.string.enter_valid_mobile))
            return false
        }
        if (TextUtils.isEmpty(etFax.text.toString().trim())) {
            showError(getString(R.string.enter_fax))
            return false
        }
        if (TextUtils.isEmpty(etEmail.text.toString().trim())) {
            showError(resources.getString(R.string.error_enter_email))
            return false
        }
        if (!isValidEmail(etEmail.text.toString().trim())) {
            showError(resources.getString(R.string.error_enter_valid_email))
            return false
        }
        if (TextUtils.isEmpty(etKeyWord.text.toString().trim())) {
            showError(getString(R.string.enter_keyword))
            return false
        }

        if (TextUtils.isEmpty(etCoins.text.toString().trim())) {
            showError(getString(R.string.enter_coins))
            return false
        }
        if (TextUtils.isEmpty(etGroup.text.toString().trim())) {
            showError(getString(R.string.enter_group))
            return false
        }
        if (TextUtils.isEmpty(etBusinessRegisterNo.text.toString().trim())) {
            showError(getString(R.string.enter_business_reg_no))
            return false
        }
        if (TextUtils.isEmpty(etContact.text.toString().trim())) {
            showError(getString(R.string.enter_contact))
            return false
        }
        if (TextUtils.isEmpty(etGender.text.toString().trim())) {
            showError(getString(R.string.select_gender))
            return false
        }
        if (TextUtils.isEmpty(etDob.text.toString().trim())) {
            showError(getString(R.string.select_dob))
            return false
        }
        if (TextUtils.isEmpty(etRemarks.text.toString().trim())) {
            showError(getString(R.string.enter_remarks))
            return false
        }
        if (TextUtils.isEmpty(etJoinDate.text.toString().trim())) {
            showError(getString(R.string.enter_join_date))
            return false
        }
        if (TextUtils.isEmpty(etExpiryDate.text.toString().trim())) {
            showError(getString(R.string.enter_expiry_date))
            return false
        }
        if (TextUtils.isEmpty(etDefaultAddress.text.toString().trim())) {
            showError(getString(R.string.enter_default_address))
            return false
        }
        if (TextUtils.isEmpty(etDeliveryAddress.text.toString().trim())) {
            showError(getString(R.string.enter_deli_address))
            return false
        }
        if (TextUtils.isEmpty(etCreditLimit.text.toString().trim())) {
            showError(getString(R.string.enter_credit_limit))
            return false
        }
        if (TextUtils.isEmpty(etTermNoOrDate.text.toString().trim())) {
            showError(getString(R.string.enter_credit_days))
            return false
        }
        if (TextUtils.isEmpty(etContact2.text.toString().trim())) {
            showError(getString(R.string.enter_contact))
            return false
        }
        if (TextUtils.isEmpty(etDefaultCurrency.text.toString().trim())) {
            showError(getString(R.string.select_default_currency))
            return false
        }
        if (TextUtils.isEmpty(etReturnAccount.text.toString().trim())) {
            showError(getString(R.string.enter_return_account))
            return false
        }
        if (TextUtils.isEmpty(etReferredCustomer.text.toString().trim())) {
            showError(getString(R.string.enter_referred_customer))
            return false
        }
        if (TextUtils.isEmpty(etHeight.text.toString().trim())) {
            showError(getString(R.string.enter_height))
            return false
        }
        if (TextUtils.isEmpty(etWeight.text.toString().trim())) {
            showError(getString(R.string.enter_weight))
            return false
        }
        if (TextUtils.isEmpty(etOccupation.text.toString().trim())) {
            showError(getString(R.string.enter_occupation))
            return false
        }
        if (TextUtils.isEmpty(etMaritalStatus.text.toString().trim())) {
            showError(getString(R.string.enter_marital_status))
            return false
        }

        if (!isEdit) {
            if (documentPath == null) {
                showError(getString(R.string.upload_document_image))
                return false
            }

            if (idPath == null) {
                showError(getString(R.string.upload_id))
                return false
            }
        }

        if (!isNetworkConnected) {
            showError(resources.getString(R.string.internet_error))
            return false
        }

        return true
    }

    /* Person Item Click Listener */
    override fun onPersonItemClick(list: Person) {
        val intent = Intent(this, AddPersonDetailActivity::class.java)
        intent.putExtra(Const.KEYS.IS_EDIT, true)
        intent.putExtra(Const.KEYS.PERSON_ID, list.id)
        startActivity(intent)

    }


    /* Get Group Name from GroupId */
    private fun getGroupNameById(groupId: String?) {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.group.child(groupId.toString())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var groupObj: Group? = dataSnapshot.getValue(Group::class.java)
                        etGroup.setText(groupObj!!.name)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    hideProgressDialog()
                }
            })
    }

    /* Get Currency Name From ID */
    private fun getCurrencyName(currencyID: String?) {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.currency.child(currencyID.toString())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var currencyObj: CurrencyTable? =
                            dataSnapshot.getValue(CurrencyTable::class.java)

                        etDefaultCurrency.setText(currencyObj?.currency.toString())

                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    /* Get Account Name From ID */
    private fun getAccountName(accountId: String?) {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.chartOfAccountReference.child(accountId.toString())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var accountObj: ChartOfAccount? =
                            dataSnapshot.getValue(ChartOfAccount::class.java)
                        etReturnAccount.setText(accountObj?.account_name.toString())
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

    }

    /* Get Referred CustomerName from Id in Person table*/
    private fun getReferredCustomerName(referredCustomerId: String?) {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.person.child(referredCustomerId.toString())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var personObj: Person? = dataSnapshot.getValue(Person::class.java)
                        etReferredCustomer.setText(personObj?.name.toString())
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun getPersonListFromIds(personDetailId: String?) {
        Timber.e("personDetailId -- " + personDetailId)
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.person.child(personDetailId.toString())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var personObj: Person? = dataSnapshot.getValue(Person::class.java)
                        if (personObj != null)
                            personList.add(personObj!!)
                    }
                    personAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }


    private fun showCurrencyDialog() {
        currencyList = currencyListFromDB
        if(currencyList.isEmpty()){
            showError(getString(R.string.no_currency_found))
            return
        }
        CurrencyDialog(this@AddCrmActivity, currencyList,this).show(supportFragmentManager, resources.getString(R.string.app_name))
    }

    override fun onCurrencyClick(list: CurrencyTable) {
        currencyId = list?.id!!
        etDefaultCurrency.setText(list.currency)
    }

}