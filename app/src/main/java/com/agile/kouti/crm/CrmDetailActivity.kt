package com.agile.kouti.crm

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.agile.kouti.db.FirebaseDbClient
import com.agile.kouti.db.SecondLevel
import com.agile.kouti.db.crm.Crm
import com.agile.kouti.db.crm.Group
import com.agile.kouti.db.payroll.Item
import com.agile.kouti.utils.Const
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_crm_detail.*
import kotlinx.android.synthetic.main.toolbar.*
import timber.log.Timber

class CrmDetailActivity : KoutiBaseActivity(),View.OnClickListener{

    private var crmId = ""
    var crmObj: Crm? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crm_detail)

        initData()
    }

    private fun initData() {
        ivBack.setOnClickListener(this)
        ivEdit.setOnClickListener(this)

        ivEdit.visibility = View.VISIBLE
        tvToolbarTitle.text = resources.getString(R.string.toolbar_title_crm)
        tvToolbarSubTitle.visibility = View.GONE

        if (intent != null) {
            crmId = intent.getStringExtra(Const.KEYS.CRM_ID).toString()
            if(!TextUtils.isEmpty(crmId)){
                getCrmData()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if(!TextUtils.isEmpty(crmId)){
            getCrmData()
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.ivBack-> finish()

            R.id.ivEdit-> {
                val intent = Intent(this,AddCrmActivity::class.java)
                intent.putExtra(Const.KEYS.CRM_ID,crmId)
                intent.putExtra(Const.KEYS.IS_EDIT,true)
                startActivity(intent)
                //finish()
            }
        }
    }

    /* Get CRM data from ID*/
    private fun getCrmData() {

        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.crm.child(crmId).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    var crmObject: Crm? = dataSnapshot.getValue(Crm::class.java)
                    crmObj = crmObject
                    Timber.e("crmObje  -- " + crmObj!!.name)
                    setData()

                }
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

    /* Set Data in UI */
    private fun setData() {
        tvCustomerName.text = crmObj?.name
        //tvOtherName.text = crmObj?.referred_customer
        tvContact.text = crmObj?.contact
        tvTelephone.text = crmObj?.telephone
        tvMobile.text = crmObj?.mobile
        tvFax.text = crmObj?.fax
        tvEmail.text = crmObj?.email
        tvAddress.text = crmObj?.default_address
        //tvGroup.text = crmObj?.group
        tvCreditLimit.text = crmObj?.credit_limit
        tvCreditTermDays.text = crmObj?.term_days
        getGroupNameById(crmObj?.group)



    }

    /* Get Group Name from GroupId */
    private fun getGroupNameById(groupId: String?) {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.group.child(groupId.toString()).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    var groupObj: Group? = dataSnapshot.getValue(Group::class.java)
                    tvGroup.text = groupObj?.name
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}