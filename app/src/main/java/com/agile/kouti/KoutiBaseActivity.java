package com.agile.kouti;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.agile.kouti.db.CurrencyTable;
import com.agile.kouti.db.FirebaseDbClient;
import com.agile.kouti.utils.Const;
import com.agile.kouti.utils.CustomProgressDialog;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import timber.log.Timber;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public abstract class KoutiBaseActivity extends AppCompatActivity {

    protected InputMethodManager mInputMethodManager;
    CustomProgressDialog progressDialog = new CustomProgressDialog();

    public static final int RequestPermissionCode = 7;

    private OnImageSelectedListener listener;
    private OnDateSelectedListener dateListener;
    private Uri imageUri = null;

    private OnDialogClickListener onDialogClickListener;

    public String imageType = "";
    String selectedDate = "";
    private Uri docUri = null;

    String currencyName = "";

    public ArrayList<CurrencyTable> currencyTables = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        this.listener = listener;

    }

    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }


    public void showAlert(String msg) {
        if (msg == null) return;
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void showError(String msg) {
        if (msg == null) return;
        Snackbar.make(findViewById(android.R.id.content), msg, BaseTransientBottomBar.LENGTH_SHORT).show();
    }

    public void hideKeyBoard(View view) {
        if (view != null) {
            mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            mInputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void addFragment(Fragment fragment, int containerId, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(containerId, fragment);
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(fragment.getClass().getSimpleName());
        }
        fragmentTransaction.commit();
    }

    public void replaceFragment(Fragment fragment, int containerId, boolean isAddedToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(containerId, fragment);
        if (isAddedToBackStack) {
            fragmentTransaction.addToBackStack(fragment.getClass().getSimpleName());
        }
        fragmentTransaction.commit();
    }

    public void showTwoButtonDialog(String title, String message, String positiveButtonName, String negativeButtonName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setTitle(title)
                .setCancelable(false)
                .setPositiveButton(positiveButtonName, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        onDialogClickListener.onPositiveClick();
                    }
                })
                .setNegativeButton(negativeButtonName, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        onDialogClickListener.onNegativeClick();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public interface OnDialogClickListener {
        void onPositiveClick();

        void onNegativeClick();
    }

    public void setDialogListener(OnDialogClickListener listener) {
        this.onDialogClickListener = listener;
    }


    public boolean isNetworkConnected() {
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            return connected;
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }
        return connected;
    }

    public String getDeviceId() {
        String android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        return android_id;
    }

    public void showProgressDialog() {
        if (!progressDialog.isShowing()) {
            progressDialog.show(this, "Please Wait...");
        }
    }

    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.dialog != null)
            progressDialog.dialog.dismiss();
    }

    //    /* Get Random Color */
    public String getRandomColor() {

        int colorInt = Color.rgb(getRandomNumberBetweenRange(), getRandomNumberBetweenRange(), getRandomNumberBetweenRange());
        return String.format("#%06X", colorInt & 0x00FFFFFF);

    }


    // Get random number between range
    private int getRandomNumberBetweenRange() {
        int minRange = 190;
        int maxRange = 250;
        Random mRandom = new Random();
        return mRandom.nextInt(maxRange - minRange) + minRange;
    }


    /******************* Check Run-Time Permission ***************/

    public boolean checkPermission() {
        if (CheckingPermissionIsEnabledOrNot()) {
            return true;
        } else {
            //Calling method to enable permission.
            RequestMultiplePermission();
            return false;
        }
    }

    //Permission function starts from here
    private void RequestMultiplePermission() {
        // Creating String Array with Permissions.
        ActivityCompat.requestPermissions(this, new String[]{
                CAMERA, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, RequestPermissionCode);

    }

    // Calling override method.
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
//        switch (requestCode) {
//
//            case RequestPermissionCode:
//
//                if (grantResults.length > 0) {
//                    boolean CameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
//                    boolean WritePermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
//                    boolean ReadPermission = grantResults[2] == PackageManager.PERMISSION_GRANTED;
//                    if (CameraPermission && WritePermission && ReadPermission) {
//                        //Toast.makeText(this, "Permission Granted", Toast.LENGTH_LONG).show();
//                    } else {
//                        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, RECORD_AUDIO)) {
//                            gotoSettingScreenDialog();
//                        }
////                        else
////                            Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
//                    }
//                }
//                break;
//        }
//    }

    // Checking permission is enabled or not using function starts from here.
    public boolean CheckingPermissionIsEnabledOrNot() {
        int FirstPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        int SecondPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int ThirdPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        return FirstPermissionResult == PackageManager.PERMISSION_GRANTED &&
                SecondPermissionResult == PackageManager.PERMISSION_GRANTED &&
                ThirdPermissionResult == PackageManager.PERMISSION_GRANTED;

    }

    private void gotoSettingScreenDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Permission needed")
                .setTitle(R.string.app_name)
                .setCancelable(false)
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent();
                        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        i.addCategory(Intent.CATEGORY_DEFAULT);
                        i.setData(Uri.parse("package:" + getPackageName()));
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        startActivity(i);
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    protected void openGallery(final String image_type) {
        imageType = image_type;


        if (imageType.equalsIgnoreCase(Const.KEYS.DOCUMENT)) {
            String fileName = getResources().getString(R.string.app_name) + (System.currentTimeMillis() / 1000);
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, fileName);
            values.put(MediaStore.Images.Media.DESCRIPTION, "Image capture by camera");
            docUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/pdf");
            //intent.putExtra(Intent.EXTRA_TITLE, "");

            // Optionally, specify a URI for the directory that should be opened in
            // the system file picker when your app creates the document.
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, docUri);
            startActivityForResult(intent, 1);
        } else {
            Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            //pickPhoto.putExtra(Const.KEYS.IMAGE_TYPE, imageType);
            startActivityForResult(pickPhoto, 1);//one can be replaced with any action code
        }
    }


    /*Image Picker*/
    public void showImagePickerDialog(final String image_type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Select Image")
                .setTitle(R.string.app_name)
                .setCancelable(true)
                .setPositiveButton("Camera", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        imageType = image_type;

                        String fileName = getResources().getString(R.string.app_name) + (System.currentTimeMillis() / 1000);
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Images.Media.TITLE, fileName);
                        values.put(MediaStore.Images.Media.DESCRIPTION, "Image capture by camera");
                        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                        startActivityForResult(intent, 501);

                    }
                })
                .setNegativeButton("Gallery", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openGallery(image_type);


//                        Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                        startActivityForResult(pickPhoto, 1);
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 501:
                if (resultCode == RESULT_OK) {
                    if (imageUri != null) {
                        listener.onCameraImage(imageUri, imageType);
                    }
                }

                break;
            case 1:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        Timber.e("docUri -- " + docUri);
                        Uri selectedGalleryImage = data.getData();
                        listener.onGalleryImage(selectedGalleryImage, imageType);
                    }
                }
                break;
        }
    }

    public void setListener(OnImageSelectedListener listener) {
        this.listener = listener;
    }

    public interface OnImageSelectedListener {
        void onCameraImage(Uri imageUri, String imageType);

        void onGalleryImage(Uri selectedGalleryImage, String imageType);
    }


    /*Date Picker Dialog */
    public void showDatePickerDialog(boolean isSelectFutureDate, final String dateType) {
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        String selectdate = "";

                        if ((monthOfYear + 1) < 10)
                            selectdate = "" + dayOfMonth + "-" + "0" + (monthOfYear + 1) + "-" + year;
                        else
                            selectdate = "" + dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
                        String finalDate = changeDateFormate(Const.DateFormats.DD_MM_YYYY, Const.DateFormats.MMM_DD_YYYY, selectdate);
                        Timber.e("finalDate -- " + finalDate);

                        dateListener.onDateSelected(finalDate, dateType);
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
        if (!isSelectFutureDate)
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());


    }

    /* Change date formate */
    public String changeDateFormate(String inputPattern, String outputPattern, String input) {
        Timber.v("changeDateFormat() called with: " + "inputPattern = [" + inputPattern + "], outputPattern = [" + outputPattern + "], input = [" + input + "]");
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern, Locale.ENGLISH);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern, Locale.ENGLISH);

        Date date;
        String output = "";
        try {
            date = inputFormat.parse(input);
            output = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Timber.v("changeDateFormat() returned: " + output);
        return output;
    }

    public void setDateListener(OnDateSelectedListener listener) {
        this.dateListener = listener;
    }

    public interface OnDateSelectedListener {
        void onDateSelected(String finalDate, String dateType);
    }

    public String getUtcDate() {
        final SimpleDateFormat sdf = new SimpleDateFormat(Const.DateFormats.DD_MM_YYYY_HH_MM_SS);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String utcDate = sdf.format(new Date());
        Timber.e("utcDate -- " + utcDate);

        return utcDate;

//        DateFormat df = DateFormat.getTimeInstance();
//        df.setTimeZone(TimeZone.getTimeZone("gmt"));
//        String gmtTime = df.format(new Date());
//        Timber.e("gmtTime -- "+gmtTime);

    }

    public void getCurrencyListData() {
        FirebaseDbClient mFirebaseClient = new FirebaseDbClient();
        mFirebaseClient.getCurrency().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    if (dataSnapshot.getValue() != null) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            CurrencyTable table = postSnapshot.getValue(CurrencyTable.class);
                            currencyTables.add(table);
                        }

                        Timber.e("currency list --- " + currencyTables.size());

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public ArrayList<CurrencyTable> getCurrencyListFromDB() {
        return currencyTables;
    }


}
