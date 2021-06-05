package com.agile.kouti.payroll

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.agile.kouti.db.CurrencyTable
import com.agile.kouti.db.FirebaseDbClient
import com.agile.kouti.db.payroll.*
import com.agile.kouti.utils.Const
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_add_pay_roll.*
import kotlinx.android.synthetic.main.activity_payroll_expense_detail.*
import kotlinx.android.synthetic.main.activity_payroll_expense_list.*
import kotlinx.android.synthetic.main.toolbar.*
import timber.log.Timber

class PayrollExpenseDetailActivity : KoutiBaseActivity(), View.OnClickListener {

    private var payrollData = PayRoll()
    private var payrollItemList = ArrayList<PayRollItem>()
    private val payrollDetailsListAdapter = PayrollDetailsListAdapter(this, payrollItemList)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payroll_expense_detail)

        initData()
    }

    private fun initData() {
        ivBack.setOnClickListener(this)
        ivEdit.setOnClickListener(this)
        ivEdit.visibility = View.VISIBLE
        tvToolbarTitle.text = "Payroll Expenses"
        tvToolbarSubTitle.visibility = View.GONE

        setPayrollDetailAdapter()

        if (intent != null) {
            payrollData = intent.getParcelableExtra<PayRoll>(Const.KEYS.LIST_OBJECT)
            Timber.e(" == " + payrollData.date)

            tvDate.text = payrollData.date
            tvExpensesNo.text = payrollData.expense_no
            tvDescription.text = payrollData.description

            var result = payrollData.staff_list!!.split(",").map { it.trim() }

            result.forEach {
                Timber.e("id -- " + it)
                getPayrollItemData(it.toString())
            }
            tvCurrency.setText(payrollData?.currency.toString())
            getCurrencyName(payrollData.currency)
        }
    }

    private fun getCurrencyName(currency: String?) {
        var mFirebaseClient = FirebaseDbClient()

        mFirebaseClient.currency.child(currency.toString()).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var currencyObj: CurrencyTable? = dataSnapshot.getValue(CurrencyTable::class.java)
                        //tvCurrency.text = currencyObj?.currency
                        tvCurrency.setText(currencyObj?.currency.toString())

                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

    }

    private fun setPayrollDetailAdapter() {
        val layoutManager = LinearLayoutManager(
            this@PayrollExpenseDetailActivity,
            LinearLayoutManager.VERTICAL,
            false
        )
        rvPayRollDetail.layoutManager = layoutManager
        rvPayRollDetail.adapter = payrollDetailsListAdapter
    }

    private fun getPayrollItemData(it: String) {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.payrollItem.child(it)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var payRollObj: PayRollItem? =
                            dataSnapshot.getValue(PayRollItem::class.java)
//                    payrollItemList.add(payRollObj!!)
//                    payrollDetailsListAdapter.notifyDataSetChanged()

                        mFirebaseClient.item.child(payRollObj?.item.toString())
                            .addListenerForSingleValueEvent(object :
                                ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        var itemObje: Item? =
                                            dataSnapshot.getValue(Item::class.java)
                                        Timber.e("PayRollItem size -- " + itemObje?.fourth_level)
                                        var itemName = itemObje?.fourth_level.toString()
                                        payRollObj?.item = itemName

//                                        payrollItemList.add(payRollObj!!)
//                                        payrollDetailsListAdapter.notifyDataSetChanged()


                                        mFirebaseClient.staff.child(payRollObj?.vendor.toString())
                                            .addListenerForSingleValueEvent(object :
                                                ValueEventListener {
                                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                    if (dataSnapshot.exists()) {
                                                        var staffObj: Staff? = dataSnapshot.getValue(Staff::class.java)
                                                        Timber.e("PayRollItem size -- " + staffObj?.name)
                                                        var vendorName = staffObj?.name.toString()
                                                        payRollObj?.vendor = vendorName

                                                        //payrollItemList.add(payRollObj!!)
                                                        //payrollDetailsListAdapter.notifyDataSetChanged()


                                                        mFirebaseClient.shop.child(payRollObj?.shop.toString())
                                                            .addListenerForSingleValueEvent(object :
                                                                ValueEventListener {
                                                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                                    if (dataSnapshot.exists()) {
                                                                        var shopObj: Shop? = dataSnapshot.getValue(Shop::class.java)
                                                                        Timber.e("shopObj  -- " + shopObj?.name)
                                                                        var shopName = shopObj?.name.toString()
                                                                        payRollObj?.shop = shopName

                                                                        payrollItemList.add(payRollObj!!)
                                                                        payrollDetailsListAdapter.notifyDataSetChanged()
                                                                    }
                                                                }
                                                                override fun onCancelled(error: DatabaseError) {}
                                                            })


                                                    }
                                                }
                                                override fun onCancelled(error: DatabaseError) {}
                                        })

                                    }
                                }
                                override fun onCancelled(error: DatabaseError) {}
                            })
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
                val intent = Intent(this, AddPayRollActivity::class.java)
                intent.putExtra(Const.KEYS.IS_EDIT, true)
                intent.putExtra(Const.KEYS.LIST_OBJECT, payrollData)
                intent.putParcelableArrayListExtra(Const.KEYS.LIST, payrollItemList)
                startActivity(intent)
                finish()
            }
        }
    }
}