package com.agile.kouti.payroll

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.agile.kouti.db.AccountNature
import com.agile.kouti.db.FirebaseDbClient
import com.agile.kouti.db.SecondLevel
import com.agile.kouti.db.ThirdLevel
import com.agile.kouti.db.payroll.Item
import com.agile.kouti.search.SearchActivity
import com.agile.kouti.utils.Const
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_account_detail.*
import kotlinx.android.synthetic.main.activity_add_item.*
import kotlinx.android.synthetic.main.activity_add_item.btnAdd
import kotlinx.android.synthetic.main.activity_add_item.etAccountNature
import kotlinx.android.synthetic.main.activity_add_item.etSecondLevel
import kotlinx.android.synthetic.main.activity_add_item.etThirdLevel
import kotlinx.android.synthetic.main.toolbar.*
import timber.log.Timber

class AddItemActivity : KoutiBaseActivity(), View.OnClickListener {

    private var isEdit = false
    private var data = Item()

    private var accountNatureId =""
    private var secondLevelId =""
    private var thirdLevelId =""

    private var itemId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)

        initData()
    }

    private fun initData() {
        tvToolbarTitle.text = "Add Item"
        if (intent != null) {
            isEdit = intent.getBooleanExtra(Const.KEYS.IS_EDIT, false)

            if(isEdit) {

                itemId = intent.getStringExtra(Const.KEYS.ITEM_ID)
                if(!TextUtils.isEmpty(itemId)){
                    getItemDataFromItemId()
                }
            }else{
                var itemName = intent.getStringExtra("item_name")

//                accountNatureId = intent.getStringExtra(Const.KEYS.ACCOUNT_NATURE_ID)
//                secondLevelId = intent.getStringExtra(Const.KEYS.SECOND_LEVEL_ID)
//                thirdLevelId = intent.getStringExtra(Const.KEYS.THIRD_LEVEL_ID)
//
//                Timber.e("accountNatureId -- "+accountNatureId)
//                Timber.e("secondLevelId -- "+secondLevelId)
//                Timber.e("thirdLevelId -- "+thirdLevelId)


                etAccountFourthLevel.setText(itemName)
            }
        }

        btnAdd.setOnClickListener(this)
        etAccountNature.setOnClickListener(this)
        etSecondLevel.setOnClickListener(this)
        etThirdLevel.setOnClickListener(this)
        ivBack.setOnClickListener(this)

    }




    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.ivBack ->{
                finish()
            }

            R.id.etAccountNature -> {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra(
                    Const.KEYS.SEARCH_TYPE,
                    Const.SearchType.AccountNatureItem.toString()
                )
                intent.putExtra(Const.KEYS.COLOR_CODE, "")
                startActivityForResult(intent, Const.RequestCodes.ACCOUNT_NATURE_ADD_ITEM)
            }

            R.id.etSecondLevel -> {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra(Const.KEYS.SEARCH_TYPE, Const.SearchType.SecondLevelItem.toString())
                intent.putExtra(Const.KEYS.COLOR_CODE, "")
                startActivityForResult(intent, Const.RequestCodes.SECOND_LEVEL_ADD_ITEM)
            }

            R.id.etThirdLevel -> {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra(Const.KEYS.SEARCH_TYPE, Const.SearchType.ThirdLevelItem.toString())
                intent.putExtra(Const.KEYS.COLOR_CODE, "")
                startActivityForResult(intent, Const.RequestCodes.THIRD_LEVEL_ADD_ITEM)
            }

            R.id.btnAdd -> {
                hideKeyBoard(v)

                if (TextUtils.isEmpty(etAccountNature.text.toString().trim())) {
                    showError(getString(R.string.select_account_nature))
                    return
                }
                if (TextUtils.isEmpty(etSecondLevel.text.toString().trim())) {
                    showError(getString(R.string.select_second_level))
                    return
                }
                if (TextUtils.isEmpty(etThirdLevel.text.toString().trim())) {
                    showError(getString(R.string.select_third_level))
                    return
                }
                if (TextUtils.isEmpty(etAccountNumber.text.toString().trim())) {
                    showError(getString(R.string.enter_account_no))
                    return
                }
                if (TextUtils.isEmpty(etAccountFourthLevel.text.toString().trim())) {
                    showError(getString(R.string.select_fourth_level))
                    return
                }

                if(!isNetworkConnected) {
                    showError(getString(R.string.internet_error))
                    return
                }

                if(isEdit){
                    updateValue()
                }else
                AddValueInDB()

            }
        }
    }

    private fun updateValue() {
        showProgressDialog()
        var itemObj = Item(itemId,
            accountNatureId,
            etAccountNumber.text.toString(),
            etAccountFourthLevel.text.toString(),
            secondLevelId,
            thirdLevelId
        )

        var firebaseDbClient = FirebaseDbClient()
        if (itemId != null) {
            firebaseDbClient.item.child(itemId).setValue(itemObj).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    hideProgressDialog()
                    val intent = Intent(this, SearchActivity::class.java).apply { putExtra(Const.KEYS.LIST_OBJECT, itemObj) }
                    setResult(Const.RequestCodes.ADD_SUB_ITEM, intent)
                    finish()
                }else{
                    hideProgressDialog()
                    showError(getString(R.string.try_again))
                }
            }
        }else{
            hideProgressDialog()
        }

    }

    private fun AddValueInDB() {
        showProgressDialog()
        var itemId = FirebaseDatabase.getInstance().getReference(Const.TableName.ITEM).push().key.toString()
        var itemObj = Item(itemId,
            accountNatureId,
            etAccountNumber.text.toString(),
            etAccountFourthLevel.text.toString(),
            secondLevelId,
            thirdLevelId
        )

        var firebaseDbClient = FirebaseDbClient()
        if (itemId != null) {
            firebaseDbClient.item.child(itemId).setValue(itemObj).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    hideProgressDialog()
                    val intent = Intent(this, SearchActivity::class.java).apply { putExtra(Const.KEYS.LIST_OBJECT, itemObj) }
                    setResult(Const.RequestCodes.ADD_SUB_ITEM, intent)
                    finish()
                }else{
                    hideProgressDialog()
                    showError(getString(R.string.try_again))
                }
            }
        }else{
            hideProgressDialog()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            Const.RequestCodes.ACCOUNT_NATURE_ADD_ITEM -> {
                if (data != null) {
                    var accountName = data.getStringExtra(Const.KEYS.SEARCH_TEXT)
                    accountNatureId = data.getStringExtra(Const.KEYS.ACCOUNT_NATURE_ID)
                    Timber.e("accountNatureId -- "+accountNatureId)
                    etAccountNature.setText(accountName)
                }
            }
            Const.RequestCodes.SECOND_LEVEL_ADD_ITEM -> {
                if (data != null) {
                    var secondLevel = data.getStringExtra(Const.KEYS.SEARCH_TEXT)
                    etSecondLevel.setText(secondLevel)
                    secondLevelId = data.getStringExtra(Const.KEYS.SECOND_LEVEL_ID)
                    Timber.e("secondLevelId -- "+secondLevelId)
                }
            }
            Const.RequestCodes.THIRD_LEVEL_ADD_ITEM -> {
                if (data != null) {
                    var thirdLevel = data.getStringExtra(Const.KEYS.SEARCH_TEXT)
                    etThirdLevel.setText(thirdLevel)
                    thirdLevelId = data.getStringExtra(Const.KEYS.THIRD_LEVEL_ID)
                    Timber.e("thirdLevelId -- "+thirdLevelId)
                }
            }
        }
    }


    /**** Update Data */
    private fun getItemDataFromItemId() {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.item.child(itemId).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    var itemObj: Item? = dataSnapshot.getValue(Item::class.java)
                    if(itemObj!=null) {
                        etAccountNumber.setText(itemObj.account_no)
                        etAccountFourthLevel.setText(itemObj.fourth_level)

                        Timber.e("account_nature -- "+itemObj.account_nature)
                        accountNatureId = itemObj.account_nature.toString()
                        getAccountNature(itemObj.account_nature)

                        Timber.e("second_level -- "+itemObj.second_level)
                        secondLevelId = itemObj.second_level.toString()
                        getSecondlevelname(itemObj.second_level)

                        Timber.e("third_level -- "+itemObj.third_level)
                        thirdLevelId = itemObj.third_level.toString()
                        getThirdLevelName(itemObj.third_level)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }

    private fun getSecondlevelname(secondLevel: String?) {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.secondLevel.child(secondLevel.toString())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var secondLevelObject: SecondLevel? = dataSnapshot.getValue(SecondLevel::class.java)
                        var secondLevelName = secondLevelObject?.name.toString()
                        etSecondLevel.setText(secondLevelName)
                    }
                }

            })
    }

    private fun getAccountNature(accountNature: String?) {
        var mFirebaseClient = FirebaseDbClient()

        mFirebaseClient.accountNature.child(accountNature.toString())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var accountNatureObject: AccountNature? = dataSnapshot.getValue(AccountNature::class.java)
                        var accountNatureName = accountNatureObject?.name.toString()
                        etAccountNature.setText(accountNatureName)
                    }
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun getThirdLevelName(thirdLevel: String?) {
        /* Get Third Level Name*/
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.thirdLevel.child(thirdLevel.toString())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var thirdLevelObject: ThirdLevel? = dataSnapshot.getValue(ThirdLevel::class.java)
                        Timber.e("thirdLevelObject  -- " + thirdLevelObject?.name)
                        var thirdLevelName = thirdLevelObject?.name.toString()
                        etThirdLevel?.setText(thirdLevelName)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}