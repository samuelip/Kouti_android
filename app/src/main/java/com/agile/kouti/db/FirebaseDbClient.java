package com.agile.kouti.db;

import android.content.Context;

import com.agile.kouti.utils.Const;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseDbClient {

    DatabaseReference  mUserReference,chartOfAccountReference,accountNature,secondLevel,thirdLevel,
            staff,shop,item,payrollItem,payroll,currency,group,person,crm,location,unit,stockName,
            subItemQuotation,mainItemQuotation,quotation,mainItemInvoice,invoice,receipt,
            receiptAccount,picture;


    public FirebaseDbClient() {
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUserReference = mFirebaseDatabase.getReference(Const.TableName.USER);
        chartOfAccountReference = mFirebaseDatabase.getReference(Const.TableName.CHART_OF_ACCOUNT);
        accountNature = mFirebaseDatabase.getReference(Const.TableName.ACCOUNT_NATURE);
        secondLevel = mFirebaseDatabase.getReference(Const.TableName.SECOND_LEVEL);
        thirdLevel = mFirebaseDatabase.getReference(Const.TableName.THIRD_LEVEL);
        staff = mFirebaseDatabase.getReference(Const.TableName.PAYROLL_STAFF.toString());
        shop = mFirebaseDatabase.getReference(Const.TableName.PAYROLL_PROJECT_SHOP.toString());
        item = mFirebaseDatabase.getReference(Const.TableName.ITEM.toString());
        payrollItem = mFirebaseDatabase.getReference(Const.TableName.PAYROLL_ITEM.toString());
        payroll = mFirebaseDatabase.getReference(Const.TableName.PAYROLL.toString());
        currency = mFirebaseDatabase.getReference(Const.TableName.CURRENCY.toString());
        person = mFirebaseDatabase.getReference(Const.TableName.PERSON.toString());
        group = mFirebaseDatabase.getReference(Const.TableName.GROUP.toString());
        crm = mFirebaseDatabase.getReference(Const.TableName.CRM.toString());
        location = mFirebaseDatabase.getReference(Const.TableName.LOCATION.toString());
        unit = mFirebaseDatabase.getReference(Const.TableName.UNITS.toString());
        stockName = mFirebaseDatabase.getReference(Const.TableName.STOCK_NAME.toString());
        subItemQuotation = mFirebaseDatabase.getReference(Const.TableName.SUB_ITEM_QUOTATION.toString());
        mainItemQuotation = mFirebaseDatabase.getReference(Const.TableName.MAIN_ITEM_QUOTATION.toString());
        quotation = mFirebaseDatabase.getReference(Const.TableName.QUOTATION.toString());
        mainItemInvoice = mFirebaseDatabase.getReference(Const.TableName.MAIN_ITEM_INVOICE.toString());
        invoice = mFirebaseDatabase.getReference(Const.TableName.INVOICE.toString());
        receipt = mFirebaseDatabase.getReference(Const.TableName.RECEIPT.toString());
        receiptAccount = mFirebaseDatabase.getReference(Const.TableName.RECEIPT_ACCOUNT.toString());
        picture = mFirebaseDatabase.getReference(Const.TableName.PICTURE.toString());

    }

    public DatabaseReference getmUserReference() {
        return mUserReference;
    }

    public DatabaseReference getChartOfAccountReference() {
        return chartOfAccountReference;
    }

    public DatabaseReference getAccountNature() {
        return accountNature;
    }

    public DatabaseReference getSecondLevel() {
        return secondLevel;
    }

    public DatabaseReference getThirdLevel() {
        return thirdLevel;
    }

    public DatabaseReference getStaff() {
        return staff;
    }

    public DatabaseReference getShop() {
        return shop;
    }

    public DatabaseReference getItem() {
        return item;
    }

    public DatabaseReference getPayrollItem() {
        return payrollItem;
    }

    public DatabaseReference getPayroll() {
        return payroll;
    }

    public DatabaseReference getCurrency() {
        return currency;
    }

    public DatabaseReference getGroup() { return group; }

    public DatabaseReference getPerson() {
        return person;
    }

    public DatabaseReference getCrm() {
        return crm;
    }

    public DatabaseReference getLocation() {
        return location;
    }

    public DatabaseReference getUnit() {
        return unit;
    }

    public DatabaseReference getStockName() {
        return stockName;
    }

    public DatabaseReference getSubItemQuotation() {
        return subItemQuotation;
    }

    public DatabaseReference getMainItemQuotation() {
        return mainItemQuotation;
    }

    public DatabaseReference getQuotation() {
        return quotation;
    }

    public DatabaseReference getMainItemInvoice() {
        return mainItemInvoice;
    }

    public DatabaseReference getInvoice() {
        return invoice;
    }

    public DatabaseReference getReceipt() {
        return receipt;
    }

    public DatabaseReference getReceiptAccount() {
        return receiptAccount;
    }

    public DatabaseReference getPicture() {
        return picture;
    }
}
