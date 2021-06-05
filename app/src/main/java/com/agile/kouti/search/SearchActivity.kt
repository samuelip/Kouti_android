package com.agile.kouti.search

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.agile.kouti.chart_of_account.AddChartOfAccountActivity
import com.agile.kouti.crm.AddCrmActivity
import com.agile.kouti.crm.AddPersonDetailActivity
import com.agile.kouti.db.*
import com.agile.kouti.db.crm.Crm
import com.agile.kouti.db.crm.Group
import com.agile.kouti.db.crm.Person
import com.agile.kouti.db.invoice.Invoice
import com.agile.kouti.db.invoice.MainItemInvoice
import com.agile.kouti.db.payroll.Item
import com.agile.kouti.db.payroll.PayRollItem
import com.agile.kouti.db.payroll.Shop
import com.agile.kouti.db.payroll.Staff
import com.agile.kouti.db.quotation.*
import com.agile.kouti.db.receipt.ReceiptAccount
import com.agile.kouti.invoice.invoice_list.AddInvoiceActivity
import com.agile.kouti.invoice.main_item.MainItemInvoiceActivity
import com.agile.kouti.invoice.sub_item.InvoiceSubItemActivity
import com.agile.kouti.payroll.AddItemActivity
import com.agile.kouti.payroll.AddPayRollActivity
import com.agile.kouti.payroll.AddStaffActivity
import com.agile.kouti.payroll.PayrollAddItemActivity
import com.agile.kouti.quotation.subItem.main_item.MainItemQuotationActivity
import com.agile.kouti.quotation.subItem.quotation.AddQuotationActivity
import com.agile.kouti.quotation.subItem.sub_item.AddUnitsActivity
import com.agile.kouti.quotation.subItem.sub_item.SubItemQuotationActivity
import com.agile.kouti.receipt.AddReceiptActivity
import com.agile.kouti.receipt.ReceiptAccountActivity
import com.agile.kouti.utils.Const
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_search.*
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

class SearchActivity : KoutiBaseActivity(), OnSearchClickListener, View.OnClickListener {

    private val searchList = ArrayList<SearchModel>()
    private val searchAdapter = SearchAdapter(this, searchList, this)

    private var searchType: String = ""
    private var colorCode = "#DAC1E8"

    var accountNatureId: String = ""
    var accountNatureObj: AccountNature? = null

    var secondLevelId: String = ""
    var secondLevelObj: SecondLevel? = null

    var thirdLevelId: String = ""
    var thirdLevelObj: ThirdLevel? = null

    var staffId: String = ""
    var staffObj: Staff? = null

    var shopId: String = ""
    var shopObj: Shop? = null

    var payRollItemId: String = ""
    var payRollItemObj: PayRollItem? = null

    var groupId: String = ""
    var groupObj: Group? = null

    var locationId: String = ""
    var locationObj: LocationDB? = null

    var stockNameId: String = ""
    var stockNameObj: StockName? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initData()
        generateRandomColor()
    }


    private fun initData() {
        ivBack.setOnClickListener(this)
        ivCancel.setOnClickListener(this)
        etSelectSearch.setOnClickListener(this)

        setSearchAdapter()

        if (intent != null) {
            searchType = intent.getStringExtra(Const.KEYS.SEARCH_TYPE)
            colorCode = intent.getStringExtra(Const.KEYS.COLOR_CODE)
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (etSearch.text?.trim()?.length!! > 0) {
                    etSelectSearch.visibility = View.VISIBLE
                    ivCancel.visibility = View.VISIBLE

                    etSelectSearch.text = etSearch.text
                } else {
                    etSelectSearch.visibility = View.GONE
                    ivCancel.visibility = View.GONE
                }

                filter(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                /* searchAdapter.filter.filter(s)*/
            }
        })
    }

    private fun setSearchAdapter() {
        val layoutManager =
            LinearLayoutManager(this@SearchActivity, LinearLayoutManager.VERTICAL, false)
        rvSearch.layoutManager = layoutManager
        rvSearch.adapter = searchAdapter
    }

    override fun onResume() {
        super.onResume()
        searchList.clear()
        searchAdapter.notifyDataSetChanged()
        when (searchType) {
            /* Account Nature*/
            Const.SearchType.AccountNature.toString() -> {
                getSearchList(Const.TableName.ACCOUNT_NATURE)
            }
            /*Second Level*/
            Const.SearchType.SecondLevel.toString() -> {
                getSearchList(Const.TableName.SECOND_LEVEL)
            }
            /*Third Level*/
            Const.SearchType.ThirdLevel.toString() -> {
                getSearchList(Const.TableName.THIRD_LEVEL)
            }
            /*Payroll Item*/
            Const.SearchType.PayrollItem.toString() -> {
                getSearchList(Const.TableName.PAYROLL_ITEM)
            }
            /*Staff Vendor*/
            Const.SearchType.StaffVendor.toString() -> {
                getSearchList(Const.TableName.PAYROLL_STAFF)
            }
            /*Item*/
            Const.SearchType.AddItem.toString() -> {
                getSearchList(Const.TableName.ITEM)
            }
            /*Project Shop */
            Const.SearchType.ProjectShop.toString() -> {
                getSearchList(Const.TableName.PAYROLL_PROJECT_SHOP)
            }
            /*Project Shop Quotation */
            Const.SearchType.ProjectShopQuotation.toString() -> {
                getSearchList(Const.TableName.PAYROLL_PROJECT_SHOP)
            }

            // below type used in Add Item Activity
            Const.SearchType.AccountNatureItem.toString() -> {
                getSearchList(Const.TableName.ACCOUNT_NATURE)
            }
            Const.SearchType.SecondLevelItem.toString() -> {
                getSearchList(Const.TableName.SECOND_LEVEL)
            }
            Const.SearchType.ThirdLevelItem.toString() -> {
                getSearchList(Const.TableName.THIRD_LEVEL)
            }

            /* Group */
            Const.SearchType.Group.toString() -> {
                getSearchList(Const.TableName.GROUP)
            }

            /* Add Person*/
            Const.SearchType.Person.toString() -> {
                getSearchList(Const.TableName.PERSON)
            }

            /* Chart of account*/
            Const.SearchType.ChartOfAccount.toString() -> {
                getSearchList(Const.TableName.CHART_OF_ACCOUNT)
            }

            /* Chart of account*/
            Const.SearchType.ReferredCustomer.toString() -> {
                getSearchList(Const.TableName.PERSON)
            }
            /* Location */
            Const.SearchType.Location.toString() -> {
                getSearchList(Const.TableName.LOCATION)
            }
            /* Units */
            Const.SearchType.Units.toString() -> {
                getSearchList(Const.TableName.UNITS)
            }
            /* StockName */
            Const.SearchType.StockName.toString() -> {
                getSearchList(Const.TableName.STOCK_NAME)
            }

            /*Project Shop Quotation Main Item*/
            Const.SearchType.ProjectShopMainItemQuotation.toString() -> {
                getSearchList(Const.TableName.PAYROLL_PROJECT_SHOP)
            }
            /* Get sub Item Quotation list*/
            Const.SearchType.AddSubItemQuotation.toString() -> {
                getSearchList(Const.TableName.SUB_ITEM_QUOTATION)
            }
            /*Get Customer List (CRM)*/
            Const.SearchType.Customer.toString() -> {
                getSearchList(Const.TableName.CRM)
            }
            /*Get Main item List (Quotation)*/
            Const.SearchType.AddMainItemQuotation.toString() -> {
                getSearchList(Const.TableName.MAIN_ITEM_QUOTATION)
            }
            /*Get Customer List (Invoice)*/
            Const.SearchType.CustomerInvoice.toString() -> {
                getSearchList(Const.TableName.CRM)
            }


            /* Chart of account invoice */
            Const.SearchType.ChartOfAccountInvoice.toString() -> {
                getSearchList(Const.TableName.CHART_OF_ACCOUNT)
            }

            /* Chart of account invoice */
            Const.SearchType.ProjectShopInvoice.toString() -> {
                getSearchList(Const.TableName.PAYROLL_PROJECT_SHOP)
            }

            /* StockName Invoice */
            Const.SearchType.StockNameInvoice.toString() -> {
                getSearchList(Const.TableName.STOCK_NAME)
            }
            /* Location Invoice */
            Const.SearchType.LocationInvoice.toString() -> {
                getSearchList(Const.TableName.LOCATION)
            }
            /* Units Invoice*/
            Const.SearchType.UnitsInvoice.toString() -> {
                getSearchList(Const.TableName.UNITS)
            }

            /*Project Shop Invoice */
            Const.SearchType.ProjectShopMainItemInvoice.toString() -> {
                getSearchList(Const.TableName.PAYROLL_PROJECT_SHOP)
            }

            /* Get sub Item list (invoice)*/
            Const.SearchType.AddSubItemInvoice.toString() -> {
                getSearchList(Const.TableName.SUB_ITEM_QUOTATION)
            }

            /* Get sub Item invoice list*/
            Const.SearchType.AddSubItemInvoice.toString() -> {
                getSearchList(Const.TableName.SUB_ITEM_QUOTATION)
            }
            /* Get main Item invoice list */
            Const.SearchType.AddMainItemInvoice.toString() -> {
                getSearchList(Const.TableName.MAIN_ITEM_INVOICE)
            }

            /*Get Customer List (Receipt)*/
            Const.SearchType.CustomerReceipt.toString() -> {
                getSearchList(Const.TableName.CRM)
            }

            /* Chart of account receipt */
            Const.SearchType.ChartOfAccountReceipt.toString() -> {
                getSearchList(Const.TableName.CHART_OF_ACCOUNT)
            }

            /* Invoice */
            Const.SearchType.Invoice.toString() -> {
                getSearchList(Const.TableName.INVOICE)
            }

            /* receipt Account */
            Const.SearchType.ReceiptAccount.toString() -> {
                getSearchList(Const.TableName.RECEIPT_ACCOUNT)
            }

        }
    }


    private fun getSearchList(tableName: String) {
        showProgressDialog()
        val databaseReference = FirebaseDatabase.getInstance().getReference(tableName)
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (data in snapshot.children) {

                        if (tableName == Const.TableName.PAYROLL_STAFF) {
                            val l: Staff? = data.getValue<Staff>(Staff::class.java)
                            if (l != null) {
                                searchList.add(SearchModel(l.id, l.name, colorCode, l.group))
                            }
                        } else if (tableName == Const.TableName.ITEM) {
                            val l: Item? = data.getValue<Item>(Item::class.java)
                            if (l != null) {
                                searchList.add(
                                    SearchModel(
                                        l.id,
                                        l.fourth_level,
                                        "",
                                        "",
                                        l.account_nature,
                                        l.account_no,
                                        l.fourth_level,
                                        l.second_level,
                                        l.third_level
                                    )
                                )
                            }
                        } else if (tableName == Const.TableName.PAYROLL_ITEM) {
                            //payRollItemObj = data.getValue<PayRollItem>(PayRollItem::class.java)

                            val l: PayRollItem? =
                                data.getValue<PayRollItem>(PayRollItem::class.java)
                            if (l != null) {
                                payRollItemObj = l
                            }

                            if (payRollItemObj != null) {
                                payRollItemId = payRollItemObj!!.id.toString()

                                Timber.e("list.amount -- " + payRollItemObj!!.amount)
                                Timber.e("list.id -- " + payRollItemObj!!.id)
                                Timber.e("list.item -- " + payRollItemObj!!.item)
                                Timber.e("list.remark -- " + payRollItemObj!!.remark)
                                Timber.e("list.shop -- " + payRollItemObj!!.shop)
                                Timber.e("list.vendor -- " + payRollItemObj!!.vendor)

                                searchList.add(
                                    SearchModel(
                                        payRollItemObj!!.id,
                                        payRollItemObj!!.remark,
                                        "",
                                        "",
                                        "",
                                        "",
                                        "",
                                        "",
                                        "",
                                        payRollItemObj!!.amount,
                                        payRollItemObj!!.item,
                                        payRollItemObj!!.remark,
                                        payRollItemObj!!.shop,
                                        payRollItemObj!!.vendor
                                    )
                                )

                                //searchList.add(SearchModel(payRollItemObj!!.id,payRollItemObj!!.remark,colorCode,""))
                            }
                        } else if (tableName == Const.TableName.PERSON) {
                            val l: Person? = data.getValue<Person>(Person::class.java)
                            if (l != null) {
                                searchList.add(SearchModel(l.id, l.name, colorCode))
                            }
                        } else if (tableName == Const.TableName.CHART_OF_ACCOUNT) {
                            val l: ChartOfAccount? =
                                data.getValue<ChartOfAccount>(ChartOfAccount::class.java)
                            if (l != null) {
                                searchList.add(SearchModel(l.id, l.account_name, colorCode))
                            }
                        } else if (tableName == Const.TableName.UNITS) {
                            val l: Units? = data.getValue<Units>(Units::class.java)
                            if (l != null) {
                                searchList.add(SearchModel(l.id, l.name, ""))
                            }
                        } else if (tableName == Const.TableName.STOCK_NAME) {
                            val l: StockName? = data.getValue<StockName>(StockName::class.java)
                            if (l != null) {
                                searchList.add(SearchModel(l.id, l.name, ""))
                            }
                        } else if (tableName == Const.TableName.SUB_ITEM_QUOTATION) {
                            val l: SubItemQuotation? =
                                data.getValue<SubItemQuotation>(SubItemQuotation::class.java)
                            if (l != null) {
                                getStockNameFromId(l.stock_name, l.id)
                                //searchList.add(SearchModel(l.id,l.stock_name, ""))
                            }
                        } else if (tableName == Const.TableName.CRM) {
                            val l: Crm? = data.getValue<Crm>(Crm::class.java)
                            if (l != null) {
                                searchList.add(SearchModel(l.id, l.name, ""))
                            }
                        } else if (tableName == Const.TableName.MAIN_ITEM_QUOTATION) {
                            val l: MainItemQuotation? =
                                data.getValue<MainItemQuotation>(MainItemQuotation::class.java)
                            if (l != null) {
                                searchList.add(SearchModel(l.id, l.item, ""))
                            }
                        } else if (tableName == Const.TableName.MAIN_ITEM_INVOICE) {
                            val l: MainItemInvoice? =
                                data.getValue<MainItemInvoice>(MainItemInvoice::class.java)
                            if (l != null) {
                                searchList.add(SearchModel(l.id, l.item, ""))
                            }

                        } else if (tableName == Const.TableName.INVOICE) {
                            val l: Invoice? = data.getValue<Invoice>(Invoice::class.java)
                            if (l != null) {
                                searchList.add(SearchModel(l.id, l.invoice_no, ""))
                            }
                        } else if (tableName == Const.TableName.RECEIPT_ACCOUNT) {
                            val l: ReceiptAccount? =
                                data.getValue<ReceiptAccount>(ReceiptAccount::class.java)
                            if (l != null) {
                                searchList.add(SearchModel(l.id, l.accounts_name, ""))
                            }
                        } else {
                            val l: SearchModel? =
                                data.getValue<SearchModel>(SearchModel::class.java)
                            if (l != null) {
                                searchList.add(l)
                            }
                        }
                    }
                    searchAdapter.notifyDataSetChanged()
                    hideProgressDialog()
                } else {
                    hideProgressDialog()
                    showError("No data found")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                hideProgressDialog()
            }
        })
    }

    private fun getStockNameFromId(stockName: String?, id: String?) {
        var mFirebaseClient = FirebaseDbClient()
        mFirebaseClient.stockName.child(stockName.toString())
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        var obj: StockName? = dataSnapshot.getValue(StockName::class.java)
                        if (obj != null) {
//                        etStockName.setText(obj.name)
//                        stockNameId = obj.id!!

                            searchList.add(SearchModel(id, obj.name, ""))
                            searchAdapter.notifyDataSetChanged()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onSearchItemClick(list: SearchModel) {
        if (searchType.equals(Const.SearchType.PayrollItem.toString())) {
            var item = PayRollItem(
                list.amount!!,
                list.id!!,
                list.item!!,
                list.remark!!,
                list.shop!!,
                list.vendor!!
            )
            navigateToPayrollScreen(item)
        } else
            navigateToBackSearchWithSearchResult(list.name, list.id.toString())
    }


    /*** Click Event ****/
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivBack -> {
                finish()
            }
            R.id.ivCancel -> {
                etSearch.setText("")
                etSelectSearch.setText("")
            }
            R.id.etSelectSearch -> {
                when (searchType) {
                    Const.SearchType.AccountNature.toString() -> {
                        addValueInAccountNatureTable(etSelectSearch.text?.trim().toString())
                    }
                    Const.SearchType.SecondLevel.toString() -> {
                        addValueInSecondLevelTable(etSelectSearch.text?.trim().toString())
                    }
                    Const.SearchType.ThirdLevel.toString() -> {
                        addValueInThirdLevelTable(etSelectSearch.text?.trim().toString())
                    }
                    Const.SearchType.PayrollItem.toString() -> {
                        etSelectSearch.setText("")
                        val i = Intent(this, PayrollAddItemActivity::class.java)
                        i.putExtra(Const.KEYS.REMARK, etSearch.text?.trim().toString())
                        i.putExtra(Const.KEYS.COLOR_CODE, colorCode)
                        i.putExtra(Const.KEYS.IS_EDIT, false)
                        startActivity(i)
                    }
                    Const.SearchType.StaffVendor.toString() -> {
                        val i = Intent(this, AddStaffActivity::class.java)
                        i.putExtra("staff_name", etSearch.text?.trim().toString())
                        startActivityForResult(i, Const.RequestCodes.ADD_STAFF_VENDOR)
                    }
                    Const.SearchType.AddItem.toString() -> {
                        val i = Intent(this, AddItemActivity::class.java)
                        i.putExtra("item_name", etSearch.text?.trim().toString())
                        i.putExtra(Const.KEYS.IS_EDIT, false)
                        startActivityForResult(i, Const.RequestCodes.ADD_SUB_ITEM)

                    }
                    Const.SearchType.ProjectShop.toString() -> {
                        addValueInShopTable(etSelectSearch.text?.trim().toString())
                    }

                    Const.SearchType.AccountNatureItem.toString() -> {
                        addValueInAccountNatureTable(etSelectSearch.text?.trim().toString())
                    }
                    Const.SearchType.SecondLevelItem.toString() -> {
                        addValueInSecondLevelTable(etSelectSearch.text?.trim().toString())
                    }
                    Const.SearchType.ThirdLevelItem.toString() -> {
                        addValueInThirdLevelTable(etSelectSearch.text?.trim().toString())
                    }
                    Const.SearchType.Group.toString() -> {
                        addValueInGroupTable(etSelectSearch.text?.trim().toString())
                    }
                    /* Add Person*/
                    Const.SearchType.Person.toString() -> {
                        val intent = Intent(this, AddPersonDetailActivity::class.java)
                        intent.putExtra(Const.KEYS.IS_EDIT, false)
                        intent.putExtra(
                            Const.KEYS.PERSON_NAME,
                            etSelectSearch.text?.trim().toString()
                        )
                        startActivity(intent)
                    }

                    /* ReferredCustomer */
                    Const.SearchType.ReferredCustomer.toString() -> {
                        val intent = Intent(this, AddPersonDetailActivity::class.java)
                        intent.putExtra(Const.KEYS.IS_EDIT, false)
                        intent.putExtra(
                            Const.KEYS.PERSON_NAME,
                            etSelectSearch.text?.trim().toString()
                        )
                        startActivity(intent)
                    }
                    /* Location */
                    Const.SearchType.Location.toString() -> {
                        addValueInLocationTable(etSelectSearch.text?.trim().toString())
                    }
                    Const.SearchType.ProjectShopQuotation.toString() -> {
                        addValueInShopTable(etSelectSearch.text?.trim().toString())
                    }
                    /* Units */
                    Const.SearchType.Units.toString() -> {
                        val intent = Intent(this, AddUnitsActivity::class.java)
                        intent.putExtra(Const.KEYS.IS_EDIT, false)
                        intent.putExtra(
                            Const.KEYS.UNIT_NAME,
                            etSelectSearch.text?.trim().toString()
                        )
                        startActivity(intent)
                    }
                    /* Stock Name */
                    Const.SearchType.StockName.toString() -> {
                        addValueInStockNameTable(etSelectSearch.text?.trim().toString())
                    }
                    /* shop for main item */
                    Const.SearchType.ProjectShopMainItemQuotation.toString() -> {
                        addValueInShopTable(etSelectSearch.text?.trim().toString())
                    }
                    /* sub item quotation */
                    Const.SearchType.AddSubItemQuotation.toString() -> {
                        val intent = Intent(this, SubItemQuotationActivity::class.java)
                        intent.putExtra(Const.KEYS.IS_EDIT, false)
                        intent.putExtra(
                            Const.KEYS.SEARCH_TEXT,
                            etSelectSearch.text?.trim().toString()
                        )
                        startActivity(intent)
                    }
                    /* Add Customer Add Quotation */
                    Const.SearchType.Customer.toString() -> {
                        val intent = Intent(this, AddCrmActivity::class.java)
                        intent.putExtra(Const.KEYS.IS_EDIT, false)
                        startActivity(intent)
                    }
                    /* Add Main Item Quotation*/
                    Const.SearchType.AddMainItemQuotation.toString() -> {
                        val intent = Intent(this, MainItemQuotationActivity::class.java)
                        intent.putExtra(Const.KEYS.IS_EDIT, false)
                        intent.putExtra(
                            Const.KEYS.SEARCH_TEXT,
                            etSelectSearch.text?.trim().toString()
                        )
                        startActivity(intent)
                    }

                    /* Add Customer  */
                    Const.SearchType.CustomerInvoice.toString() -> {
                        val intent = Intent(this, AddCrmActivity::class.java)
                        intent.putExtra(Const.KEYS.IS_EDIT, false)
                        startActivity(intent)
                    }


                    /* shop invoice */
                    Const.SearchType.ProjectShopInvoice.toString() -> {
                        addValueInShopTable(etSelectSearch.text?.trim().toString())
                    }
                    /* Stock Invoice */
                    Const.SearchType.StockNameInvoice.toString() -> {
                        addValueInStockNameTable(etSelectSearch.text?.trim().toString())
                    }

                    /* Location Invoice*/
                    Const.SearchType.LocationInvoice.toString() -> {
                        addValueInLocationTable(etSelectSearch.text?.trim().toString())
                    }

                    /* Units Invoice */
                    Const.SearchType.UnitsInvoice.toString() -> {
                        val intent = Intent(this, AddUnitsActivity::class.java)
                        intent.putExtra(Const.KEYS.IS_EDIT, false)
                        intent.putExtra(
                            Const.KEYS.UNIT_NAME,
                            etSelectSearch.text?.trim().toString()
                        )
                        startActivity(intent)
                    }

                    /* shop invoice main item */
                    Const.SearchType.ProjectShopMainItemInvoice.toString() -> {
                        addValueInShopTable(etSelectSearch.text?.trim().toString())
                    }

                    /* sub item quotation */
                    Const.SearchType.AddSubItemInvoice.toString() -> {
                        val intent = Intent(this, InvoiceSubItemActivity::class.java)
                        intent.putExtra(Const.KEYS.IS_EDIT, false)
                        intent.putExtra(
                            Const.KEYS.SEARCH_TEXT,
                            etSelectSearch.text?.trim().toString()
                        )
                        startActivity(intent)
                    }

                    /* Main Item Invoice*/
                    Const.SearchType.AddMainItemInvoice.toString() -> {
                        val intent = Intent(this, MainItemInvoiceActivity::class.java)
                        intent.putExtra(Const.KEYS.IS_EDIT, false)
                        intent.putExtra(
                            Const.KEYS.SEARCH_TEXT,
                            etSelectSearch.text?.trim().toString()
                        )
                        startActivity(intent)
                    }

                    /* Add Customer  */
                    Const.SearchType.CustomerReceipt.toString() -> {
                        val intent = Intent(this, AddCrmActivity::class.java)
                        intent.putExtra(Const.KEYS.IS_EDIT, false)
                        startActivity(intent)
                    }

                    Const.SearchType.CustomerReceipt.toString() -> {
                        val intent = Intent(this, AddCrmActivity::class.java)
                        intent.putExtra(Const.KEYS.IS_EDIT, false)
                        startActivity(intent)
                    }

                    Const.SearchType.ChartOfAccountReceipt.toString() -> {
                        val intent = Intent(this, AddChartOfAccountActivity::class.java)
                        intent.putExtra(Const.KEYS.IS_EDIT, false)
                        startActivity(intent)
                    }

                    Const.SearchType.ReceiptAccount.toString() -> {
                        val intent = Intent(this, ReceiptAccountActivity::class.java)
                        intent.putExtra(Const.KEYS.IS_EDIT, false)
                        startActivity(intent)
                    }

                }
            }
        }

    }


    /*Add value in Account Nature Table*/
    private fun addValueInAccountNatureTable(searchValue: String) {
        accountNatureId =
            FirebaseDatabase.getInstance().getReference(Const.TableName.ACCOUNT_NATURE)
                .push().key.toString()
        accountNatureObj = AccountNature(accountNatureId, searchValue, colorCode)
        var firebaseDbClient = FirebaseDbClient();
        if (accountNatureId != null) {
            firebaseDbClient.accountNature.child(accountNatureId).setValue(accountNatureObj)
            navigateToBackSearchWithSearchResult(
                etSelectSearch.text?.trim().toString(),
                accountNatureId
            )
        }
    }

    /*Add value in Second level Table*/
    private fun addValueInSecondLevelTable(searchValue: String) {
        secondLevelId = FirebaseDatabase.getInstance().getReference(Const.TableName.SECOND_LEVEL)
            .push().key.toString()
        secondLevelObj = SecondLevel(secondLevelId.toString(), searchValue, colorCode)
        var firebaseDbClient = FirebaseDbClient()
        if (secondLevelId != null) {
            firebaseDbClient.secondLevel.child(secondLevelId).setValue(secondLevelObj)
            navigateToBackSearchWithSearchResult(
                etSelectSearch.text?.trim().toString(),
                secondLevelId
            )
        }
    }

    /*Add value in Third level Table*/
    private fun addValueInThirdLevelTable(searchValue: String) {
        thirdLevelId = FirebaseDatabase.getInstance().getReference(Const.TableName.THIRD_LEVEL)
            .push().key.toString()
        thirdLevelObj = ThirdLevel(thirdLevelId.toString(), searchValue, colorCode)
        var firebaseDbClient = FirebaseDbClient()
        if (thirdLevelId != null) {
            firebaseDbClient.thirdLevel.child(thirdLevelId).setValue(thirdLevelObj)
            navigateToBackSearchWithSearchResult(
                etSelectSearch.text?.trim().toString(),
                thirdLevelId
            )
        }
    }

    /*Add value in Shop Table*/
    private fun addValueInShopTable(searchValue: String) {
        shopId = FirebaseDatabase.getInstance().getReference(Const.TableName.PAYROLL_PROJECT_SHOP)
            .push().key.toString()
        shopObj = Shop(shopId, searchValue, colorCode)
        var firebaseDbClient = FirebaseDbClient()
        if (shopId != null) {
            firebaseDbClient.shop.child(shopId).setValue(shopObj)

            navigateToBackSearchWithSearchResult(etSelectSearch.text?.trim().toString(), shopId)
        }
    }

    /*Add value in Group Table*/
    private fun addValueInGroupTable(searchValue: String) {
        groupId =
            FirebaseDatabase.getInstance().getReference(Const.TableName.GROUP).push().key.toString()
        groupObj = Group(
            groupId.toString(),
            searchValue,
            colorCode
        )
        var firebaseDbClient = FirebaseDbClient()
        if (groupId != null) {
            firebaseDbClient.group.child(groupId.toString()).setValue(groupObj)
            navigateToBackSearchWithSearchResult(
                etSelectSearch.text?.trim().toString(),
                groupId
            )
        }
    }

    /* Add value in Location */
    private fun addValueInLocationTable(searchValue: String) {
        locationId =
            FirebaseDatabase.getInstance().getReference(Const.TableName.GROUP).push().key.toString()
        locationObj = LocationDB(locationId, searchValue)
        var firebaseDbClient = FirebaseDbClient()
        if (locationId != null) {
            firebaseDbClient.location.child(locationId.toString()).setValue(locationObj)
            navigateToBackSearchWithSearchResult(
                etSelectSearch.text?.trim().toString(),
                locationId
            )
        }

    }

    /* Add value in Stock Name */
    private fun addValueInStockNameTable(searchValue: String) {
        stockNameId = FirebaseDatabase.getInstance().getReference(Const.TableName.STOCK_NAME)
            .push().key.toString()
        stockNameObj = StockName(stockNameId, searchValue)
        var firebaseDbClient = FirebaseDbClient()
        if (stockNameId != null) {
            firebaseDbClient.stockName.child(stockNameId.toString()).setValue(stockNameObj)
            navigateToBackSearchWithSearchResult(
                etSelectSearch.text?.trim().toString(),
                stockNameId
            )
        }

    }


    private fun navigateToBackSearchWithSearchResult(searchText: String?, id: String) {
        when (searchType) {
            /*Account Nature*/
            Const.SearchType.AccountNature.toString() -> {
                val data = Intent(this, AddChartOfAccountActivity::class.java)
                data.putExtra(Const.KEYS.SEARCH_TEXT, searchText)
                data.putExtra(Const.KEYS.ACCOUNT_NATURE_ID, id)
                setResult(Const.RequestCodes.ACCOUNT_NATURE, data)
                finish()
            }
            /*Second Level*/
            Const.SearchType.SecondLevel.toString() -> {
                val data = Intent(this, AddChartOfAccountActivity::class.java)
                data.putExtra(Const.KEYS.SEARCH_TEXT, searchText)
                data.putExtra(Const.KEYS.SECOND_LEVEL_ID, id)
                setResult(Const.RequestCodes.SECOND_LEVEL, data)
                finish()
            }
            /*Third Level*/
            Const.SearchType.ThirdLevel.toString() -> {
                val data = Intent(this, AddChartOfAccountActivity::class.java)
                data.putExtra(Const.KEYS.SEARCH_TEXT, searchText)
                data.putExtra(Const.KEYS.THIRD_LEVEL_ID, id)
                setResult(Const.RequestCodes.THIRD_LEVEL, data)
                finish()
            }

            Const.SearchType.StaffVendor.toString() -> {
                val data = Intent(this, PayrollAddItemActivity::class.java)
                data.putExtra(Const.KEYS.SEARCH_TEXT, searchText)
                data.putExtra(Const.KEYS.STAFF_ID, id)
                setResult(Const.RequestCodes.STAFF_VENDOR, data)
                finish()
            }

            Const.SearchType.AddItem.toString() -> {
                val data = Intent(this, PayrollAddItemActivity::class.java)
                data.putExtra(Const.KEYS.SEARCH_TEXT, searchText)
                data.putExtra(Const.KEYS.ITEM_ID, id)
                setResult(Const.RequestCodes.ADD_ITEM, data)
                finish()
            }

            Const.SearchType.ProjectShop.toString() -> {
                val data = Intent(this, PayrollAddItemActivity::class.java)
                data.putExtra(Const.KEYS.SEARCH_TEXT, searchText)
                data.putExtra(Const.KEYS.SHOP_ID, id)
                setResult(Const.RequestCodes.PROJECT_SHOP, data)
                finish()
            }

            /*Account Nature*/
            Const.SearchType.AccountNatureItem.toString() -> {
                val data = Intent(this, AddItemActivity::class.java)
                data.putExtra(Const.KEYS.SEARCH_TEXT, searchText)
                data.putExtra(Const.KEYS.ACCOUNT_NATURE_ID, id)
                setResult(Const.RequestCodes.ACCOUNT_NATURE_ADD_ITEM, data)
                finish()
            }

            Const.SearchType.SecondLevelItem.toString() -> {
                val data = Intent(this, AddItemActivity::class.java)
                data.putExtra(Const.KEYS.SEARCH_TEXT, searchText)
                data.putExtra(Const.KEYS.SECOND_LEVEL_ID, id)
                setResult(Const.RequestCodes.SECOND_LEVEL_ADD_ITEM, data)
                finish()
            }

            Const.SearchType.ThirdLevelItem.toString() -> {
                val data = Intent(this, AddItemActivity::class.java)
                data.putExtra(Const.KEYS.SEARCH_TEXT, searchText)
                data.putExtra(Const.KEYS.THIRD_LEVEL_ID, id)
                setResult(Const.RequestCodes.THIRD_LEVEL_ADD_ITEM, data)
                finish()
            }

//            Const.SearchType.PayrollItem.toString() -> {
//                val data = Intent(this, AddPayRollActivity::class.java)
//                data.putExtra(Const.KEYS.SEARCH_TEXT, searchText)
//                setResult(Const.RequestCodes.PAYROLL_ITEM, data)
//                finish()
//            }
            Const.SearchType.Group.toString() -> {
                val data = Intent(this, AddCrmActivity::class.java)
                data.putExtra(Const.KEYS.SEARCH_TEXT, searchText)
                data.putExtra(Const.KEYS.GROUP_ID, id)
                setResult(Const.RequestCodes.GROUP, data)
                finish()
            }

            Const.SearchType.Person.toString() -> {
                val data = Intent(this, AddCrmActivity::class.java)
                data.putExtra(Const.KEYS.SEARCH_TEXT, searchText)
                data.putExtra(Const.KEYS.PERSON_ID, id)
                setResult(Const.RequestCodes.ADD_PERSON, data)
                finish()
            }

            Const.SearchType.ChartOfAccount.toString() -> {
                val data = Intent(this, AddCrmActivity::class.java)
                data.putExtra(Const.KEYS.CHART_OF_ACCOUNT_NAME, searchText)
                data.putExtra(Const.KEYS.CHART_OF_ACCOUNT_ID, id)
                setResult(Const.RequestCodes.ADD_CHART_OF_ACCOUNT, data)
                finish()
            }

            Const.SearchType.ReferredCustomer.toString() -> {
                val data = Intent(this, AddCrmActivity::class.java)
                data.putExtra(Const.KEYS.REFERRED_CUSTOMER_NAME, searchText)
                data.putExtra(Const.KEYS.REFERRED_CUSTOMER_ID, id)
                setResult(Const.RequestCodes.REFERRED_CUSTOMER, data)
                finish()
            }

            /* Location */
            Const.SearchType.Location.toString() -> {
                val data = Intent(this, SubItemQuotationActivity::class.java)
                data.putExtra(Const.KEYS.SEARCH_TEXT, searchText)
                data.putExtra(Const.KEYS.LOCATION_ID, id)
                setResult(Const.RequestCodes.LOCATION, data)
                finish()
            }
            Const.SearchType.ProjectShopQuotation.toString() -> {
                val data = Intent(this, SubItemQuotationActivity::class.java)
                data.putExtra(Const.KEYS.SEARCH_TEXT, searchText)
                data.putExtra(Const.KEYS.SHOP_ID, id)
                setResult(Const.RequestCodes.PROJECT_SHOP_QUOTATION, data)
                finish()
            }

            Const.SearchType.Units.toString() -> {
                val data = Intent(this, SubItemQuotationActivity::class.java)
                data.putExtra(Const.KEYS.SEARCH_TEXT, searchText)
                data.putExtra(Const.KEYS.UNIT_ID, id)
                setResult(Const.RequestCodes.UNITS, data)
                finish()
            }

            Const.SearchType.StockName.toString() -> {
                val data = Intent(this, SubItemQuotationActivity::class.java)
                data.putExtra(Const.KEYS.SEARCH_TEXT, searchText)
                data.putExtra(Const.KEYS.STOCK_ID, id)
                setResult(Const.RequestCodes.STOCK_NAME, data)
                finish()
            }
            Const.SearchType.ProjectShopMainItemQuotation.toString() -> {
                val data = Intent(this, MainItemQuotationActivity::class.java)
                data.putExtra(Const.KEYS.SEARCH_TEXT, searchText)
                data.putExtra(Const.KEYS.SHOP_ID, id)
                setResult(Const.RequestCodes.PROJECT_SHOP_QUOTATION_MAIN_ITEM, data)
                finish()
            }

            Const.SearchType.AddSubItemQuotation.toString() -> {
                val data = Intent(this, MainItemQuotationActivity::class.java)
                data.putExtra(Const.KEYS.SEARCH_TEXT, searchText)
                data.putExtra(Const.KEYS.SUB_ITEM_ID, id)
                setResult(Const.RequestCodes.ADD_SUB_ITEM_QUOTATION, data)
                finish()
            }

            /* Add Customer Add Quotation */
            Const.SearchType.Customer.toString() -> {
                val data = Intent(this, AddQuotationActivity::class.java)
                data.putExtra(Const.KEYS.SEARCH_TEXT, searchText)
                data.putExtra(Const.KEYS.CUSTOMER_ID, id)
                setResult(Const.RequestCodes.CUSTOMER, data)
                finish()
            }

            Const.SearchType.AddMainItemQuotation.toString() -> {
                val data = Intent(this, AddQuotationActivity::class.java)
                data.putExtra(Const.KEYS.SEARCH_TEXT, searchText)
                data.putExtra(Const.KEYS.MAIN_ITEM_ID, id)
                setResult(Const.RequestCodes.ADD_MAIN_ITEM_QUOTATION, data)
                finish()
            }

            Const.SearchType.CustomerInvoice.toString() -> {
                val data = Intent(this, AddInvoiceActivity::class.java)
                data.putExtra(Const.KEYS.SEARCH_TEXT, searchText)
                data.putExtra(Const.KEYS.CUSTOMER_ID, id)
                setResult(Const.RequestCodes.CUSTOMER_INVOICE, data)
                finish()
            }
            Const.SearchType.ChartOfAccountInvoice.toString() -> {
                val data = Intent(this, AddInvoiceActivity::class.java)
                data.putExtra(Const.KEYS.CHART_OF_ACCOUNT_NAME, searchText)
                data.putExtra(Const.KEYS.CHART_OF_ACCOUNT_ID, id)
                setResult(Const.RequestCodes.ADD_CHART_OF_ACCOUNT_INVOICE, data)
                finish()
            }

            Const.SearchType.ProjectShopInvoice.toString() -> {
                val data = Intent(this, InvoiceSubItemActivity::class.java)
                data.putExtra(Const.KEYS.SEARCH_TEXT, searchText)
                data.putExtra(Const.KEYS.SHOP_ID, id)
                setResult(Const.RequestCodes.PROJECT_SHOP_INVOICE, data)
                finish()
            }

            Const.SearchType.StockNameInvoice.toString() -> {
                val data = Intent(this, InvoiceSubItemActivity::class.java)
                data.putExtra(Const.KEYS.SEARCH_TEXT, searchText)
                data.putExtra(Const.KEYS.STOCK_ID, id)
                setResult(Const.RequestCodes.STOCK_NAME_INVOICE, data)
                finish()
            }

            Const.SearchType.LocationInvoice.toString() -> {
                val data = Intent(this, InvoiceSubItemActivity::class.java)
                data.putExtra(Const.KEYS.SEARCH_TEXT, searchText)
                data.putExtra(Const.KEYS.LOCATION_ID, id)
                setResult(Const.RequestCodes.LOCATION_INVOICE, data)
                finish()
            }

            /* Unit Invoice */
            Const.SearchType.UnitsInvoice.toString() -> {
                val data = Intent(this, InvoiceSubItemActivity::class.java)
                data.putExtra(Const.KEYS.SEARCH_TEXT, searchText)
                data.putExtra(Const.KEYS.UNIT_ID, id)
                setResult(Const.RequestCodes.UNITS_INVOICE, data)
                finish()
            }

            Const.SearchType.ProjectShopMainItemInvoice.toString() -> {
                val data = Intent(this, MainItemInvoiceActivity::class.java)
                data.putExtra(Const.KEYS.SEARCH_TEXT, searchText)
                data.putExtra(Const.KEYS.SHOP_ID, id)
                setResult(Const.RequestCodes.PROJECT_SHOP_INVOICE_MAIN_ITEM, data)
                finish()
            }

            Const.SearchType.AddSubItemInvoice.toString() -> {
                val data = Intent(this, MainItemInvoiceActivity::class.java)
                data.putExtra(Const.KEYS.SEARCH_TEXT, searchText)
                data.putExtra(Const.KEYS.SUB_ITEM_ID_INVOICE, id)
                setResult(Const.RequestCodes.ADD_SUB_ITEM_INVOICE, data)
                finish()
            }

            Const.SearchType.AddMainItemInvoice.toString() -> {
                val data = Intent(this, AddInvoiceActivity::class.java)
                data.putExtra(Const.KEYS.SEARCH_TEXT, searchText)
                data.putExtra(Const.KEYS.MAIN_ITEM_ID_INVOICE, id)
                setResult(Const.RequestCodes.ADD_MAIN_ITEM_INVOICE, data)
                finish()
            }

            /* Receipt */
            Const.SearchType.CustomerReceipt.toString() -> {
                val data = Intent(this, AddReceiptActivity::class.java)
                data.putExtra(Const.KEYS.SEARCH_TEXT, searchText)
                data.putExtra(Const.KEYS.CUSTOMER_ID, id)
                setResult(Const.RequestCodes.CUSTOMER_RECEIPT, data)
                finish()
            }
            /* Chart of account Receipt */
            Const.SearchType.ChartOfAccountReceipt.toString() -> {
                val data = Intent(this, ReceiptAccountActivity::class.java)
                data.putExtra(Const.KEYS.CHART_OF_ACCOUNT_NAME, searchText)
                data.putExtra(Const.KEYS.CHART_OF_ACCOUNT_ID, id)
                setResult(Const.RequestCodes.ADD_CHART_OF_ACCOUNT_RECEIPT, data)
                finish()
            }

            Const.SearchType.Invoice.toString() -> {
                val data = Intent(this, AddReceiptActivity::class.java)
                data.putExtra(Const.KEYS.SEARCH_TEXT, searchText)
                data.putExtra(Const.KEYS.INVOICE_ID, id)
                setResult(Const.RequestCodes.INVOICE, data)
                finish()
            }

            Const.SearchType.ReceiptAccount.toString() -> {
                val data = Intent(this, AddReceiptActivity::class.java)
                data.putExtra(Const.KEYS.SEARCH_TEXT, searchText)
                data.putExtra(Const.KEYS.RECEIPT_ACCOUNT_ID, id)
                setResult(Const.RequestCodes.RECEIPT_ACCOUNT, data)
                finish()
            }


        }

    }

    private fun navigateToPayrollScreen(item: PayRollItem) {
        val intent = Intent(this, AddPayRollActivity::class.java).apply {
            putExtra(Const.KEYS.LIST_OBJECT, item)
        }
        setResult(Const.RequestCodes.PAYROLL_ITEM, intent)
        finish()
    }

    fun filter(text: String) {
        val filteredSearchList: ArrayList<SearchModel> = ArrayList()
        for (list in searchList) {
            if (list.name!!.toLowerCase().contains(text.toLowerCase())) {
                filteredSearchList.add(list)
            }
        }
        searchAdapter.filterList(filteredSearchList);
    }

    private fun generateRandomColor() {
//        val rnd = Random()
//        val color: Int = Color.argb(255, rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255))
//        colorCode = color.toString()
//        Timber.e("color code --- $colorCode")
        //view.setBackgroundColor(color)

//        colorCode = randomColor


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Const.RequestCodes.ADD_STAFF_VENDOR -> {
                val staff: Staff? = data?.getParcelableExtra(Const.KEYS.LIST_OBJECT)
                if (staff != null) {
                    staffId = staff.id.toString()
                    val data = Intent(this, PayrollAddItemActivity::class.java)
                    data.putExtra(Const.KEYS.SEARCH_TEXT, staff.name)
                    data.putExtra(Const.KEYS.STAFF_ID, staffId)
                    setResult(Const.RequestCodes.STAFF_VENDOR, data)
                    finish()
                }
            }

            Const.RequestCodes.ADD_SUB_ITEM -> {
                val item: Item? = data?.getParcelableExtra(Const.KEYS.LIST_OBJECT)
                if (item != null) {
                    val data = Intent(this, PayrollAddItemActivity::class.java)
                    data.putExtra(Const.KEYS.SEARCH_TEXT, item?.fourth_level)
                    data.putExtra(Const.KEYS.ITEM_ID, item?.id)
                    setResult(Const.RequestCodes.ADD_ITEM, data)
                    finish()
                }
            }
        }
    }

}