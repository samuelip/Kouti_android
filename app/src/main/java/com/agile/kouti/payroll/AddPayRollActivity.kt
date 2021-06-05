package com.agile.kouti.payroll

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.agile.kouti.db.CurrencyTable
import com.agile.kouti.db.FirebaseDbClient
import com.agile.kouti.db.payroll.Item
import com.agile.kouti.db.payroll.PayRoll
import com.agile.kouti.db.payroll.PayRollItem
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
import kotlinx.android.synthetic.main.activity_add_pay_roll.*
import kotlinx.android.synthetic.main.activity_add_pay_roll.btnSave
import kotlinx.android.synthetic.main.activity_add_pay_roll.ivDocument
import kotlinx.android.synthetic.main.activity_add_receipt.*
import kotlinx.android.synthetic.main.toolbar.*
import timber.log.Timber
import java.util.*


class AddPayRollActivity : KoutiBaseActivity(), View.OnClickListener,
    KoutiBaseActivity.OnImageSelectedListener, KoutiBaseActivity.OnDateSelectedListener,
    PayrollItemClickListener, CurrencyListener {

    private var itemList = ArrayList<PayRollItem>()
    private var itemListTemp = ArrayList<PayRollItem>()
    private val addItemPayrollAdapter = AddItemPayrollAdapter(this, itemList, this)

    private var colorCode = "#EAE3E3"
    private var currencyId = ""
    private var totalAmount = 0
    private var firebaseStore: FirebaseStorage? = null
    private var storageReference: StorageReference? = null
    private var filePath: Uri? = null

    private var isEdit = false
    private var payrollData = PayRoll()

    private var imageUrl = ""
    var userId: String = ""

    private var code = ""

    private var currencyList = ArrayList<CurrencyTable>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_pay_roll)

        initData()
    }

    private fun initData() {

        if (intent != null) {
            isEdit = intent.getBooleanExtra(Const.KEYS.IS_EDIT, false)

            if (isEdit) {
                payrollData = intent.getParcelableExtra<PayRoll>(Const.KEYS.LIST_OBJECT)
                itemListTemp = intent.getParcelableArrayListExtra<PayRollItem>(Const.KEYS.LIST)

                Timber.e("itemListTemp -- " + itemListTemp.size)

                tvToolbarSubTitle.text = getString(R.string.txt_edit)
                tvToolbarSubTitle.visibility = View.VISIBLE
                getDataPayrollData()

                if (!itemListTemp.isEmpty()) {
                    for (list in itemListTemp) {
                        if (!TextUtils.isEmpty(list.amount))
                            totalAmount = totalAmount + list.amount!!.toInt()
                    }
                }
                etTotalAmount.setText(totalAmount.toString())
            } else {
                tvToolbarSubTitle.text = getString(R.string.txt_add)
                tvToolbarSubTitle.visibility = View.VISIBLE
                etTotalAmount.setText(totalAmount.toString())
                colorCode = randomColor.toString()

                code = (System.currentTimeMillis() / 1000).toString()
                etExpenseNo.setText(code)
            }

            getCurrencyListData()
        }

        tvToolbarTitle.text = getString(R.string.toolbar_title_payroll_expense)

        ivBack.setOnClickListener(this)
        btnSave.setOnClickListener(this)
        etDate.setOnClickListener(this)
        etCurrency.setOnClickListener(this)
        ivDocument.setOnClickListener(this)
        tvaddItem.setOnClickListener(this)

        setItemAdapter()

        firebaseStore = FirebaseStorage.getInstance()
        storageReference = FirebaseStorage.getInstance().reference

        userId = Preferences.getPreference(this@AddPayRollActivity, Const.SharedPrefs.USER_ID)
    }



    private fun getDataPayrollData() {
        etDate.setText(payrollData.date)
        etExpenseNo.setText(payrollData.expense_no)
        etDescription.setText(payrollData.description)
        etCurrency.setText(payrollData.currency)


        if(!TextUtils.isEmpty(payrollData.upload_document)) {
            Glide.with(ivDocument)
                .load(payrollData.upload_document)
                .centerCrop()
                .placeholder(R.drawable.document)
                .into(ivDocument)
        }

        if (!itemListTemp.isEmpty()) {
            itemList.addAll(itemListTemp)
            addItemPayrollAdapter.notifyDataSetChanged()
        }

        imageUrl = payrollData.upload_document!!
        currencyId = payrollData.currency.toString()
        getCurrencyName(payrollData.currency)
    }

    private fun getCurrencyName(currency: String?) {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.currency.child(currency.toString()).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    var currencyObj: CurrencyTable? =
                        dataSnapshot.getValue(CurrencyTable::class.java)
                    etCurrency.setText(currencyObj?.currency.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

    }

    private fun setItemAdapter() {
        val layoutManager =
            LinearLayoutManager(this@AddPayRollActivity, LinearLayoutManager.VERTICAL, false)
        rvItemList.layoutManager = layoutManager
        rvItemList.adapter = addItemPayrollAdapter
    }

    override fun onClick(v: View?) {

        when (v?.id) {

            R.id.ivBack -> finish()

            R.id.etDate -> {
                setDateListener(this)
                showDatePickerDialog(true, Const.KEYS.DATE_NORMAL)
            }

            R.id.etCurrency -> showCurrencyDialog()

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

            R.id.tvaddItem -> {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra(Const.KEYS.SEARCH_TYPE, Const.SearchType.PayrollItem.toString())
                intent.putExtra(Const.KEYS.COLOR_CODE, colorCode)
                startActivityForResult(intent, Const.RequestCodes.PAYROLL_ITEM)
            }

            R.id.btnSave -> {
                hideKeyBoard(v)
                if (checkValidation()) {
                    if (isEdit) {
                        if (filePath == null) {
                            showProgressDialog()
                            saveDataInDB()
                        } else {
                            uploadImage()
                        }
                    } else
                        uploadImage()
                }
            }
        }
    }


    private fun checkValidation(): Boolean {

        if (TextUtils.isEmpty(etDate.text.toString().trim())) {
            showError(getString(R.string.error_select_date))
            return false
        }

        if (TextUtils.isEmpty(etExpenseNo.text.toString().trim())) {
            showError(getString(R.string.error_expense_no))
            return false
        }

        if (TextUtils.isEmpty(etDescription.text.toString().trim())) {
            showError(getString(R.string.error_enter_description))
            return false
        }

        if (TextUtils.isEmpty(etCurrency.text.toString().trim())) {
            showError(getString(R.string.error_enter_currency))
            return false
        }

        if (TextUtils.isEmpty(etTotalAmount.text.toString().trim())) {
            showError(getString(R.string.error_enter_amount))
            return false
        }

        if (!isEdit) {
            if (filePath == null) {
                showError(getString(R.string.error_select_document))
                return false
            }
        }

        if (!isNetworkConnected) {
            showError(getString(R.string.internet_error))
            return false
        }

        return true
    }

    private fun uploadImage() {
        showProgressDialog()
        if (filePath != null) {
            val ref = storageReference?.child(Const.ImageType.DOCUMENT+"/" + UUID.randomUUID().toString())
            val uploadTask = ref?.putFile(filePath!!)

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
                        imageUrl = task.result.toString()
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
        /* Get select item id in string */
        var staff_list = getItemId()
        Timber.e("staff_list id == " + staff_list)
        var payrollId = ""

        if (isEdit)
            payrollId = payrollData.id.toString()
        else
            payrollId = FirebaseDatabase.getInstance().getReference(Const.TableName.PAYROLL)
                .push().key.toString()


        val payRollObj = PayRoll(
            currencyId,
            etDate.text.toString().trim(),
            etDescription.text.toString().trim(),
            etExpenseNo.text.toString().trim(),
            payrollId,
            false,
            staff_list,
            etTotalAmount.text.toString().trim(),
            imageUrl,
            userId,
            utcDate
        )

        var firebaseDbClient = FirebaseDbClient()
        if (payRollObj != null) {
            firebaseDbClient.payroll.child(payrollId).setValue(payRollObj)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        hideProgressDialog()
                        showAlert("Success")
                        finish()
                    }
                }
        } else {
            hideProgressDialog()
        }
    }

    private fun getItemId(): String {
        var staff_list = ""
        if (itemList.isEmpty()) {
            staff_list = ""
        } else {
            for (list in itemList) {
                if (TextUtils.isEmpty(staff_list)) {
                    staff_list = list.id.toString()
                } else {
                    staff_list = staff_list + "," + list.id
                }
            }
        }

        return staff_list

    }



    override fun onGalleryImage(selectedGalleryImage: Uri, imageType: String) {
        Glide.with(ivDocument)
            .load(selectedGalleryImage)
            .centerCrop()
            .placeholder(R.drawable.document)
            .into(ivDocument)
        filePath = selectedGalleryImage
    }

    override fun onCameraImage(imageUri: Uri, imageType: String) {
        ivDocument.setImageURI(imageUri)
        filePath = imageUri
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Const.RequestCodes.PAYROLL_ITEM -> {
                val entry: PayRollItem? = data?.getParcelableExtra(Const.KEYS.LIST_OBJECT)
                if (entry != null) {
//                    itemList.add(entry!!)
//                    addItemPayrollAdapter.notifyDataSetChanged()

                    if (isEdit) {
                        totalAmount = etTotalAmount.text.toString().toInt()
                        totalAmount = (totalAmount + entry.amount!!.toDouble()).toInt()
                        etTotalAmount.setText(totalAmount.toString())
                    } else {
                        totalAmount = (totalAmount + entry.amount!!.toDouble()).toInt()
                        etTotalAmount.setText(totalAmount.toString())
                    }

                    var mFirebaseClient = FirebaseDbClient()

                    mFirebaseClient.item.child(entry.item.toString())
                        .addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    var itemObje: Item? = dataSnapshot.getValue(Item::class.java)

                                    Timber.e("PayRollItem size -- " + itemObje?.fourth_level)
                                    var itemName = itemObje?.fourth_level.toString()

                                    entry.item = itemName
                                    itemList.add(entry!!)
                                    addItemPayrollAdapter.notifyDataSetChanged()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                }
            }
        }

    }

    private fun getItemNameFromItemid(item: String?): String {
        var itemName = ""
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.item.child(item!!).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    var itemObje: Item? = dataSnapshot.getValue(Item::class.java)

                    Timber.e("PayRollItem size -- " + itemObje?.fourth_level)
                    itemName = itemObje?.fourth_level.toString()

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
        return itemName

    }


    override fun onPayrollItemClick(list: PayRollItem) {
        val intent = Intent(this, PayrollAddItemActivity::class.java)
        intent.putExtra(Const.KEYS.LIST_OBJECT, list)
        intent.putExtra(Const.KEYS.IS_EDIT, true)

        startActivity(intent)
    }

    override fun onDateSelected(finalDate: String?, dateType: String?) {
        etDate.setText(finalDate)
    }

    /* Currency dialog */
    private fun showCurrencyDialog() {
        currencyList = currencyListFromDB
        if(currencyList.isEmpty()){
            showError(getString(R.string.no_currency_found))
            return
        }
        CurrencyDialog(this@AddPayRollActivity, currencyList,this).show(supportFragmentManager, resources.getString(R.string.app_name))
    }

    override fun onCurrencyClick(list: CurrencyTable) {
        currencyId = list?.id!!
        etCurrency.setText(list.currency)
    }


}