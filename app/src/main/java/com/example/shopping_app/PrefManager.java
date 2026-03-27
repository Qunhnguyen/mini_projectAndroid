package com.example.shopping_app;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {
    private static final String PREF_NAME = "ShoppingAppPref";
    private static final String KEY_USER_ID = "userId";
    private final SharedPreferences pref;

    public PrefManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setUserId(int userId) {
        pref.edit().putInt(KEY_USER_ID, userId).apply();
    }

    public int getUserId() {
        return pref.getInt(KEY_USER_ID, AppConstants.INVALID_ID);
    }

    public boolean isLoggedIn() {
        return getUserId() != AppConstants.INVALID_ID;
    }

    public void logout() {
        clearSession();
    }

    public void clearSession() {
        pref.edit().clear().apply();
    }
}
