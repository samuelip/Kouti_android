package com.agile.kouti.utils

object Const {
    const val IS_DEBUGGABLE = true

    class TableName {
        companion object {
            const val CHART_OF_ACCOUNT = "ChartOfAccount"
            const val ACCOUNT_NATURE = "AccountNature"
            const val SECOND_LEVEL = "SecondLevel"
            const val THIRD_LEVEL = "ThirdLevel"
            const val PAYROLL_STAFF = "Staff"
            const val PAYROLL_PROJECT_SHOP = "Shop"
            const val ITEM = "Item"
            const val PAYROLL_ITEM = "PayrollItem"
            const val PAYROLL = "Payroll"
            const val CURRENCY = "Currency"
            const val GROUP = "Group"
            const val PERSON = "Person"
            const val CRM = "CRM"
            const val LOCATION = "Location"
            const val UNITS = "Units"
            const val STOCK_NAME = "Stock Name"
            const val SUB_ITEM_QUOTATION = "Sub Item Quotation"
            const val MAIN_ITEM_QUOTATION = "Main Item Quotation"
            const val QUOTATION = "Quotation"
            const val MAIN_ITEM_INVOICE = "Main Item Invoice"
            const val INVOICE = "Invoice"
            const val RECEIPT = "Receipt"
            const val RECEIPT_ACCOUNT = "Receipt Account"
            const val USER = "users"
            const val PICTURE = "picture"
        }
    }

    class ImageType{
        companion object {
            const val UPLOADS = "uploads"
            const val PERSON = "Person"
            const val DOCUMENT = "Document"
            const val ID = "id"
            const val PICTURE = "picture"
        }
    }

    class KEYS {
        companion object {
            const val IS_EDIT = "is_edit"
            const val SEARCH_TEXT = "search_text"
            const val SEARCH_TYPE = "search_type"
            const val LIST_OBJECT = "list_object"
            const val COLOR_CODE = "color_code"

            const val SHOP_ID = "shop_id"
            const val STAFF_ID = "staff_id"
            const val ITEM_ID = "item_id"
            const val LIST = "list"

            const val UNIT_ID = "unit_id"
            const val UNIT_NAME = "unit_name"

            const val STOCK_ID = "stock_id"

            const val REMARK = "remark"

            const val ACCOUNT_NATURE = "account_nature"
            const val ACCOUNT_NATURE_ID = "account_nature_id"

            const val SECOND_LEVEL_ID = "second_level_id"
            const val THIRD_LEVEL_ID = "third_level_id"

            const val GROUP_ID = "group_id"
            const val PERSON_ID = "person_id"
            const val LOCATION_ID = "location_id"

            const val IMAGE_TYPE = "image_type"
            const val DOCUMENT = "document"
            const val ID = "id"
            const val REFERENCE = "reference"


            const val DATE_DOB = "date_dob"
            const val DATE_NORMAL = "date_normal"
            const val JOIN_DATE = "join_date"
            const val EXPIRY_DATE = "expiry_date"
            const val TERM_DATE = "term_date"

            const val FILTER_ACCOUNT_NO = "Account No"
            const val FILTER_ACCOUNT_NAME = "Account Name"
            const val FILTER_DATE = "from and To date"

            const val CUSTOMER_CODE = "Customer Code"
            const val CUSTOMER_NAME = "Customer Name"


            const val FILTER_QUO_NO = "Quotation Number"


            const val PERSON_NAME = "person_name"

            const val CHART_OF_ACCOUNT_NAME = "chart_of_account_name"
            const val CHART_OF_ACCOUNT_ID = "chart_of_account_id"

            const val REFERRED_CUSTOMER_ID = "referred_customer_id"
            const val REFERRED_CUSTOMER_NAME = "referred_customer_name"


            const val CRM_ID = "crm_id"
            const val SUB_ITEM_ID = "sub_item_id"
            const val CUSTOMER_ID = "customer_id"
            const val MAIN_ITEM_ID = "main_item_id"
            const val QUOTATION_ID = "quotation_id"

            const val SUB_ITEM_ID_INVOICE = "sub_item_id_invoice"
            const val MAIN_ITEM_ID_INVOICE = "main_item_id_invoice"
            const val INVOICE_ID = "invoice_id"
            const val INVOICE_NUMBER = "invoice_number"

            const val RECEIPT_ID = "receipt_id"

            const val MAIN_LIST = "main_list"
            const val SUB_LIST = "sub_list"


            const val RECEIPT_ACCOUNT_ID = "receipt_account_id"

            const val SUB_USER_ID = "sub_user_id"

            const val FILTER_BY_NAME = "Name"
            const val FILTER_BY_PHONE = "PHone"

            const val USER_ID = "user_id"
        }
    }

    interface SharedPrefs {
        companion object {
            const val FCM_TOKEN = "fcm_token"
            const val USER_ID = "user_id"
            const val MAIN_DASHBOARD_MENU = "main_dashboard_menu"
        }

    }

    object DateFormats{
        const val MMM_DD_YYYY ="dd MMM yyyy"
        const val DD_MM_YYYY ="dd-MM-yyyy"
        const val DD_MM_YYYY_HH_MM_SS ="dd-MM-yyyy HH:mm:ss"
    }

    enum class  SearchType{
        AccountNature,
        SecondLevel,
        ThirdLevel,
        StaffVendor,
        AddItem,
        ProjectShop,
        AccountNatureItem,
        SecondLevelItem,
        ThirdLevelItem,
        PayrollItem,
        Group,
        Person,
        ChartOfAccount,
        ReferredCustomer,
        Location,
        ProjectShopQuotation,
        Units,
        StockName,
        ProjectShopMainItemQuotation,
        AddSubItemQuotation,
        Customer,
        AddMainItemQuotation,
        CustomerInvoice,
        ChartOfAccountInvoice,
        ProjectShopInvoice,
        StockNameInvoice,
        LocationInvoice,
        UnitsInvoice,
        ProjectShopMainItemInvoice,
        AddSubItemInvoice,
        AddMainItemInvoice,
        CustomerReceipt,
        ChartOfAccountReceipt,
        Invoice,
        ReceiptAccount
    }

    object RequestCodes{
        const val GALLERY = 101
        const val CAMERA = 102
        const val ACCOUNT_NATURE = 103
        const val SECOND_LEVEL = 104
        const val THIRD_LEVEL = 105
        const val STAFF_VENDOR = 106
        const val ADD_ITEM = 107
        const val PROJECT_SHOP = 108
        const val ACCOUNT_NATURE_ADD_ITEM = 109
        const val SECOND_LEVEL_ADD_ITEM = 110
        const val THIRD_LEVEL_ADD_ITEM = 111
        const val PAYROLL_ITEM = 112
        const val ADD_STAFF_VENDOR = 113
        const val ADD_SUB_ITEM = 114
        const val GROUP = 115
        const val ADD_PERSON = 116
        const val ADD_CHART_OF_ACCOUNT = 117
        const val REFERRED_CUSTOMER = 118
        const val LOCATION = 119
        const val PROJECT_SHOP_QUOTATION = 120
        const val UNITS = 121
        const val STOCK_NAME = 122
        const val PROJECT_SHOP_QUOTATION_MAIN_ITEM = 123
        const val ADD_SUB_ITEM_QUOTATION = 124
        const val CUSTOMER = 125
        const val ADD_MAIN_ITEM_QUOTATION = 126
        const val CUSTOMER_INVOICE = 127
//        const val ACCOUNT_NATURE_INVOICE = 128
//        const val SECOND_LEVEL_INVOICE = 129
//        const val THIRD_LEVEL_INVOICE = 130
        const val ADD_CHART_OF_ACCOUNT_INVOICE = 131
        const val PROJECT_SHOP_INVOICE = 132
        const val STOCK_NAME_INVOICE = 133
        const val LOCATION_INVOICE = 134
        const val UNITS_INVOICE = 135
        const val PROJECT_SHOP_INVOICE_MAIN_ITEM = 136
        const val ADD_SUB_ITEM_INVOICE = 137
        const val ADD_MAIN_ITEM_INVOICE = 138
        const val CUSTOMER_RECEIPT = 139
        const val ADD_CHART_OF_ACCOUNT_RECEIPT = 140
        const val INVOICE = 141
        const val RECEIPT_ACCOUNT = 142



    }



}