package com.agile.kouti.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.agile.kouti.settings.MenuModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;

public class Preferences {

    private static final String TAG = "Preferences";

    /**
     * @param context - pass context
     * @return SharedPreferences
     */
    public static SharedPreferences getSharedPreferences(Context context) {
        //Log.e(TAG, "getSharedPreferences: contex : "+context );
        return context.getSharedPreferences("GMB", Context.MODE_PRIVATE);
    }

    /**
     * @param context - context
     * @param key     - Constant key, will be used for accessing the stored value
     * @param val     - String value to be stored
     */
    public static void setPreference(Context context, String key, String val) {
        SharedPreferences settings = Preferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, val);
        editor.commit();
    }


    /**
     * @param context - context
     * @param key     - Constant key, will be used for accessing the stored value
     * @param val     - String value to be stored
     */
    public static void setPreference_float(Context context, String key, float val) {
        SharedPreferences settings = Preferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat(key, val);
        editor.commit();
    }

    /**
     * @param context
     * @param key
     * @param val
     */
    public static void setPreference(Context context, String key, boolean val) {
        SharedPreferences settings = Preferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, val);
        editor.commit();
    }

    /**
     * @param context
     * @param key
     * @param val
     */
    public static void setPreference_int(Context context, String key, int val) {
        SharedPreferences settings = Preferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, val);
        editor.commit();
    }


    /**
     * Add preferences
     *
     * @param context - context
     * @param key     - Constant key, will be used for accessing the stored value
     * @param val     - long value to be stored, mostly used to store FB Session value
     */
    public static void setPreference_long(Context context, String key, long val) {
        SharedPreferences settings = Preferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(key, val);
        editor.commit();
    }


    public static boolean setPreferenceArray(Context mContext, String key, ArrayList<String> array) {
        SharedPreferences prefs = Preferences.getSharedPreferences(mContext);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key + "_size", array.size());
        for (int i = 0; i < array.size(); i++)
            editor.putString(key + "_" + i, array.get(i));
        return editor.commit();
    }


    public static void clearPreferenceArray(Context c, String key) {
        SharedPreferences settings = Preferences.getSharedPreferences(c);

        if (getPreferenceArray(c, key) != null && getPreferenceArray(c, key).size() > 0) {
            for (String element : getPreferenceArray(c, key)) {
                if (findPrefrenceKey(c, element) != null && settings.contains(findPrefrenceKey(c, element))) {
                    SharedPreferences.Editor editor = settings.edit();
                    editor.remove(findPrefrenceKey(c, element));
                    editor.commit();
                }
            }
        }
    }


    public static String findPrefrenceKey(Context con, String value) {
        SharedPreferences settings = Preferences.getSharedPreferences(con);

        Map<String, ?> editor = settings.getAll();

        for (Map.Entry<String, ?> entry : editor.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null; // not found
    }


    /**
     * Remove preference key
     *
     * @param context - context
     * @param key     - the key which you stored before
     */
    public static void removePreference(Context context, String key) {
        SharedPreferences settings = Preferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(key);
        editor.apply();
    }

    /**
     * Get preference value by passing related key
     *
     * @param context - context
     * @param key     - key value used when adding preference
     * @return - String value
     */
    public static String getPreference(Context context, String key) {
        SharedPreferences prefs = Preferences.getSharedPreferences(context);
        return prefs.getString(key, "");
    }


    public static ArrayList<String> getPreferenceArray(Context mContext, String key) {
        SharedPreferences prefs = Preferences.getSharedPreferences(mContext);
        int size = prefs.getInt(key + "_size", 0);
        ArrayList<String> array = new ArrayList<String>(size);
        for (int i = 0; i < size; i++)
            array.add(prefs.getString(key + "_" + i, null));
        return array;
    }


    /**
     * Get preference value by passing related key
     *
     * @param context - context
     * @param key     - key value used when adding preference
     * @return - long value
     */
    public static long getPreference_long(Context context, String key) {
        SharedPreferences prefs = Preferences.getSharedPreferences(context);
        return prefs.getLong(key, 0);

    }


    public static boolean getPreference_boolean(Context context, String key) {
        SharedPreferences prefs = Preferences.getSharedPreferences(context);
        return prefs.getBoolean(key, false);

    }

    public static int getPreference_int(Context context, String key) {
        SharedPreferences prefs = Preferences.getSharedPreferences(context);
        return prefs.getInt(key, 0);

    }

    // Save main dashboard list
    public static void setMenuList(Context context, ArrayList<MenuModel> list, String key) {
        SharedPreferences prefs = Preferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();

    }

    // Get main dashboard list
    public static ArrayList<MenuModel> getMenuList(Context context, String key) {
        SharedPreferences prefs = Preferences.getSharedPreferences(context);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<MenuModel>>() {
        }.getType();
        return gson.fromJson(json, type);
    }


    /**
     * Clear all stored  preferences
     *
     * @param context - context
     */
    public static void removeAllPreference(Context context) {
        SharedPreferences settings = Preferences.getSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.commit();
    }

    /**
     * Clear all stored  preferences
     *
     * @param context - context
     */
    public static String getAllPreference(Context context) {
        SharedPreferences settings = Preferences.getSharedPreferences(context);
        Map<String, ?> editor = settings.getAll();
        String text = "";

        try {
            for (Map.Entry<String, ?> entry : editor.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                // do stuff
                text += "\t" + key + " = " + value + "\t";
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

        return text;

    }


}
