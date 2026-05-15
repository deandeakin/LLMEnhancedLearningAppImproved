package com.deankennedy.llmenhancedlearningapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class UserPrefs {

    private static final String PREF_NAME = "llm_learning_prefs";

    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_INTERESTS = "interests";

    // Returns the app's SharedPreferences file.
    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Saves a newly registered user's details.
    public static void registerUser(Context context, String username, String email, String password, String phone) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, password);
        editor.putString(KEY_PHONE, phone);
        editor.apply();
    }

    // Checks whether a user profile has already been saved.
    public static boolean isUserRegistered(Context context) {
        String email = getPrefs(context).getString(KEY_EMAIL, "");
        return !email.isEmpty();
    }

    // Validates entered login credentials and compares to the saved profile.
    public static boolean validateLogin(Context context, String email, String password) {
        String savedEmail = getPrefs(context).getString(KEY_EMAIL, "");
        String savedPassword = getPrefs(context).getString(KEY_PASSWORD, "");
        return email.equals(savedEmail) && password.equals(savedPassword);
    }

    // Updates the saved login session.
    public static void setLoggedIn(Context context, boolean loggedIn) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putBoolean(KEY_LOGGED_IN, loggedIn);
        editor.apply();
    }

    // Returns whether the user is currently marked as logged in.
    public static boolean isLoggedIn(Context context) {
        return getPrefs(context).getBoolean(KEY_LOGGED_IN, false);
    }

    // Simply saves the users interests.
    public static void saveInterests(Context context, Set<String> interests) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putStringSet(KEY_INTERESTS, interests);
        editor.apply();
    }

    // Returns the user's saved interests.
    public static Set<String> getInterests(Context context) {
        return new HashSet<>(getPrefs(context).getStringSet(KEY_INTERESTS, new HashSet<>()));
    }

    // Returns the user's saved username.
    public static String getUsername(Context context) {
        return getPrefs(context).getString(KEY_USERNAME, "Student");
    }

    // Returns the user's saved email.
    public static String getEmail(Context context) {
        return getPrefs(context).getString(KEY_EMAIL, "");
    }

    // Returns the user's saved phone number.
    public static String getPhone(Context context) {
        return getPrefs(context).getString(KEY_PHONE, "");
    }

    // Logs the user out by clearing only the session flag.
    public static void logout(Context context) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putBoolean(KEY_LOGGED_IN, false);
        editor.apply();
    }

    // Clears the saved interests set.
    public static void clearInterests(Context context) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.remove(KEY_INTERESTS);
        editor.apply();
    }
}