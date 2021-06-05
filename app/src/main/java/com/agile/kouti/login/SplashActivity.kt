package com.agile.kouti.login

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.view.WindowManager
import com.agile.kouti.R
import com.agile.kouti.home.HomeActivity
import com.agile.kouti.home.SetupModel
import com.agile.kouti.home.TransactionModel
import com.agile.kouti.settings.MenuModel
import com.agile.kouti.utils.Const
import com.agile.kouti.utils.Preferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import timber.log.Timber

class SplashActivity : AppCompatActivity() {

    private val SPLASH_TIME_OUT = 3000L
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        mAuth = FirebaseAuth.getInstance()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

    }

    override fun onStart() {
        super.onStart()

        // Check Menu item is saved in preference or not
        if (Preferences.getMenuList(this, Const.SharedPrefs.MAIN_DASHBOARD_MENU).isNullOrEmpty()) {
            val mMenuList = arrayListOf(

                MenuModel("Data\nImport", "Data Import", R.drawable.data_import, "", true),
                MenuModel(
                    "Opening Balance",
                    "Opening Balance",
                    R.drawable.opening_balance,
                    "",
                    true
                ),
                MenuModel("Chart Of Account", "Chart Of Account", R.drawable.data_import, "", true),
                MenuModel("Stock", "Stock", R.drawable.data_import, "", true),
                MenuModel("CRM", "CRM", R.drawable.data_import, "", true),
                MenuModel("Supplier", "Supplier", R.drawable.data_import, "", true),
                MenuModel("Staff/\nVendor", "Staff/Vendor", R.drawable.data_import, "", true),
                MenuModel("Multi-Currency", "Multi-Currency", R.drawable.data_import, "", true),
                MenuModel("Project/Shop", "Project/Shop", R.drawable.data_import, "", true),
                MenuModel("Accounting", "Accounting", R.drawable.accounting, "", true),
                MenuModel("Sales", "Sales", R.drawable.sales, "", true),
                MenuModel("POS", "POS", R.drawable.sales, "", true),
                MenuModel("Projects/\nReports", "Projects/Reports", R.drawable.sales, "", true),
                MenuModel("Purchase", "Purchase", R.drawable.sales, "", true),
                MenuModel("StockRecord/\nAdj.", "StockRecord/Adj.", R.drawable.sales, "", true),
                MenuModel("Fixed\nAssets", "Fixed Assets", R.drawable.sales, "", true),
                MenuModel("Payroll/\nExp", "Payroll Exp", R.drawable.sales, "", true),
                MenuModel("Bills\nImport", "Bills Import", R.drawable.sales, "", true),
                MenuModel(
                    "Staff/\nDirector/\nPlay List",
                    "Staff Director/Play List",
                    R.drawable.sales,
                    "",
                    true
                )
            )

            Preferences.setMenuList(this, mMenuList, Const.SharedPrefs.MAIN_DASHBOARD_MENU)

        }

        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth!!.currentUser
        updateUI(currentUser)
    }

    private fun updateUI(currentUser: FirebaseUser?) {

        Handler().postDelayed(
            {
                if (currentUser == null) {
                    val i = Intent(this@SplashActivity, LoginActivity::class.java)
                    startActivity(i)
                    finish()
                } else {
                    Timber.e("HomeActivity ....")
                    val i = Intent(this@SplashActivity, HomeActivity::class.java)
                    startActivity(i)
                    finish()
                }
            }, SPLASH_TIME_OUT
        )
    }

}