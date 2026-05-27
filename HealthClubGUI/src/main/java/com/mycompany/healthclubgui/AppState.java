package com.mycompany.healthclubgui;

public class AppState {

    public static String  currentLangName = "English";
    public static String  currentLangCode = "en";
    public static boolean darkMode        = true;

    public static void setLanguage(String fullName) {
        currentLangName = fullName;
        currentLangCode = TranslationService.getCode(fullName);
        TranslationService.currentLangCode = currentLangCode;
    }

    public static boolean isRTL() {
        return currentLangCode.equals("ar") ||
               currentLangCode.equals("fa") ||
               currentLangCode.equals("ur");
    }

    // للـ AdminScreen و MemberScreen القديمين
    public static String t(String text) {
        return TranslationService.translateNow(text, currentLangCode);
    }
}
