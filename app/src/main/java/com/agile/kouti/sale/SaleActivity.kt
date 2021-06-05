package com.agile.kouti.sale

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.agile.kouti.KoutiBaseActivity
import com.agile.kouti.R
import com.agile.kouti.crm.CrmListActivity
import com.agile.kouti.invoice.invoice_list.InvoiceListActivity
import com.agile.kouti.quotation.subItem.quotation.QuotationListActivity
import com.agile.kouti.receipt.ReceiptListActivity
import kotlinx.android.synthetic.main.activity_sale.*
import kotlinx.android.synthetic.main.toolbar.*

class SaleActivity : KoutiBaseActivity(),OnItemClickListener {

    private val salesList = ArrayList<SalesModel>()
    private val saleAdapter = SalesAdapter(this,salesList,this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sale)

        initData()
    }

    private fun initData() {

        tvToolbarTitle.text = "Sales"

        salesList.add(SalesModel("CRM"))
        salesList.add(SalesModel("Quotation"))
        salesList.add(SalesModel("Invoice(Customer)"))
        salesList.add(SalesModel("Receipt/Sales Return"))

        val layoutManager = LinearLayoutManager(this@SaleActivity, LinearLayoutManager.VERTICAL,false)
        rvSale.layoutManager = layoutManager
        rvSale.adapter = saleAdapter
        //saleAdapter.notifyDataSetChanged()

        ivBack.setOnClickListener {
            finish()
        }
    }


    override fun onItemClick(list: SalesModel, pos: Int) {

        when(pos){
            0->{
                val intent = Intent(this,CrmListActivity::class.java)
                startActivity(intent)
            }
            1->{
                val intent = Intent(this,QuotationListActivity::class.java)
                startActivity(intent)
            }
            2->{
                val intent = Intent(this,InvoiceListActivity::class.java)
                startActivity(intent)
            }
            3->{
                val intent = Intent(this,ReceiptListActivity::class.java)
                startActivity(intent)
            }
        }

    }
}