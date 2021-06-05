package com.agile.kouti.chart_of_account

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.agile.kouti.db.*
import com.agile.kouti.utils.Const
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_account_detail.*
import kotlinx.android.synthetic.main.toolbar.*
import timber.log.Timber

class AccountDetailActivity : KoutiBaseActivity(), View.OnClickListener {

    private var accountData = ChartOfAccount()

    private var accountNatureId = ""
    private var secondLevelId = ""
    private var thirdLevelId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_detail)
        setSupportActionBar(toolbar)

        ivBack.setOnClickListener(this)
        ivEdit.setOnClickListener(this)
        ivEdit.visibility = View.VISIBLE

        initData()
    }

    private fun initData() {
        if (intent != null) {
            accountData = intent.getParcelableExtra<ChartOfAccount>(Const.KEYS.LIST_OBJECT)
            tvToolbarTitle.text = resources.getString(R.string.toolbar_title_chart_of_account)

            if(!isNetworkConnected){
                showError(resources.getString(R.string.internet_error))
                return
            }

            if (accountData.id != null) {
                getAccountData()
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
                            tvAccountNo.text = obj.account_no
                            tvAccountName.text = obj.account_name

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


    /* Get Account Nature From ID */
    private fun getAccountNature() {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.accountNature.child(accountNatureId.toString())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var obj: AccountNature? = dataSnapshot.getValue(AccountNature::class.java)
                        if (obj != null) {
                            tvAccountNature.text = obj.name
                            accountNatureId = obj.id!!
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    /* Get Second Level From ID */
    private fun getSecondLevel() {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.secondLevel.child(secondLevelId.toString())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var obj: SecondLevel? = dataSnapshot.getValue(SecondLevel::class.java)
                        if (obj != null) {
                            tvSecondLevel.text = obj.name
                            secondLevelId = obj?.id!!
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    /* Get Third Level From ID */
    private fun getThirdLevelName() {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.thirdLevel.child(thirdLevelId.toString())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var obj: ThirdLevel? = dataSnapshot.getValue(ThirdLevel::class.java)
                        if (obj != null) {
                            tvThirdLevel.text = obj?.name
                            thirdLevelId = obj?.id!!
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
            R.id.ivEdit -> {
                val intent = Intent(this, AddChartOfAccountActivity::class.java)
                intent.putExtra(Const.KEYS.IS_EDIT, true)
                intent.putExtra(Const.KEYS.LIST_OBJECT, accountData)
                intent.putExtra(Const.KEYS.ACCOUNT_NATURE_ID, accountNatureId)
                intent.putExtra(Const.KEYS.SECOND_LEVEL_ID, secondLevelId)
                intent.putExtra(Const.KEYS.THIRD_LEVEL_ID, thirdLevelId)
                startActivity(intent)
                finish()
            }
        }
    }

}