package id.assel.caribengkel.tools;

import android.content.Context;

public class LoginPref {
    private static String PREF_KEY = "app_pref";
    private static String PREF_ROLE = "auth_role";

    public static String ROLE_USER = "user";
    public static String ROLE_MECHANIC = "mechanic";

    public static void setAuthRole(Context context, String role) {
        context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE).edit().putString(PREF_ROLE, role).commit();
    }

    public static String getAuthRole(Context context) {
        return context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE).getString(PREF_ROLE, null);
    }

    public static void clearAll(Context context) {
        context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE).edit().clear().apply();
    }
}
