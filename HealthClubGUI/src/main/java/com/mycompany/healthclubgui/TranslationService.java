package com.mycompany.healthclubgui;

import javafx.application.Platform;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class TranslationService {

    public static final LinkedHashMap<String, String> LANGUAGES = new LinkedHashMap<>();
    static {
        LANGUAGES.put("English",    "en");
        LANGUAGES.put("العربية",    "ar");
        LANGUAGES.put("Français",   "fr");
        LANGUAGES.put("Español",    "es");
        LANGUAGES.put("Deutsch",    "de");
        LANGUAGES.put("Italiano",   "it");
        LANGUAGES.put("Português",  "pt");
        LANGUAGES.put("Türkçe",     "tr");
        LANGUAGES.put("中文",        "zh");
        LANGUAGES.put("日本語",      "ja");
        LANGUAGES.put("한국어",      "ko");
        LANGUAGES.put("Русский",    "ru");
        LANGUAGES.put("हिन्दी",     "hi");
        LANGUAGES.put("فارسی",      "fa");
        LANGUAGES.put("اردو",       "ur");
    }

    // cache: langCode -> (original -> translated)
    private static final Map<String, Map<String, String>> cache = new ConcurrentHashMap<>();
    public  static String currentLangCode = "en";
    private static final ExecutorService executor = Executors.newFixedThreadPool(3);


    // ===== OFFLINE FALLBACK MAP (Arabic) =====
    private static final Map<String, String> AR_OFFLINE = new HashMap<>();
    static {
        AR_OFFLINE.put("Dashboard",             "\u0644\u0648\u062d\u0629 \u0627\u0644\u062a\u062d\u0643\u0645");
        AR_OFFLINE.put("Manage Users",          "\u0625\u062f\u0627\u0631\u0629 \u0627\u0644\u0645\u0633\u062a\u062e\u062f\u0645\u064a\u0646");
        AR_OFFLINE.put("Assign Members",        "\u062a\u0639\u064a\u064a\u0646 \u0627\u0644\u0623\u0639\u0636\u0627\u0621");
        AR_OFFLINE.put("Billing",               "\u0627\u0644\u0641\u0648\u0627\u062a\u064a\u0631");
        AR_OFFLINE.put("Reports",               "\u0627\u0644\u062a\u0642\u0627\u0631\u064a\u0631");
        AR_OFFLINE.put("Notifications",         "\u0627\u0644\u0625\u0634\u0639\u0627\u0631\u0627\u062a");
        AR_OFFLINE.put("Update Info",           "\u062a\u062d\u062f\u064a\u062b \u0627\u0644\u0645\u0639\u0644\u0648\u0645\u0627\u062a");
        AR_OFFLINE.put("My Members",            "\u0623\u0639\u0636\u0627\u0626\u064a");
        AR_OFFLINE.put("Add Schedule",          "\u0625\u0636\u0627\u0641\u0629 \u062c\u062f\u0648\u0644");
        AR_OFFLINE.put("View Schedules",        "\u0639\u0631\u0636 \u0627\u0644\u062c\u062f\u0627\u0648\u0644");
        AR_OFFLINE.put("Send Message",          "\u0625\u0631\u0633\u0627\u0644 \u0631\u0633\u0627\u0644\u0629");
        AR_OFFLINE.put("My Subscription",       "\u0627\u0634\u062a\u0631\u0627\u0643\u064a");
        AR_OFFLINE.put("My Schedules",          "\u062c\u062f\u0627\u0648\u0644\u064a");
        AR_OFFLINE.put("My Messages",           "\u0631\u0633\u0627\u0626\u0644\u064a");
        AR_OFFLINE.put("My Bills",              "\u0641\u0648\u0627\u062a\u064a\u0631\u064a");
        AR_OFFLINE.put("LOGIN",                 "\u062a\u0633\u062c\u064a\u0644 \u0627\u0644\u062f\u062e\u0648\u0644");
        AR_OFFLINE.put("REGISTER",              "\u0625\u0646\u0634\u0627\u0621 \u062d\u0633\u0627\u0628");
        AR_OFFLINE.put("Welcome Back!",         "\u0645\u0631\u062d\u0628\u0627\u064b \u0628\u0639\u0648\u062f\u062a\u0643!");
        AR_OFFLINE.put("Sign in to continue",   "\u0633\u062c\u0644 \u0627\u0644\u062f\u062e\u0648\u0644 \u0644\u0644\u0645\u062a\u0627\u0628\u0639\u0629");
        AR_OFFLINE.put("USERNAME",              "\u0627\u0633\u0645 \u0627\u0644\u0645\u0633\u062a\u062e\u062f\u0645");
        AR_OFFLINE.put("PASSWORD",              "\u0643\u0644\u0645\u0629 \u0627\u0644\u0645\u0631\u0648\u0631");
        AR_OFFLINE.put("Username",              "\u0627\u0633\u0645 \u0627\u0644\u0645\u0633\u062a\u062e\u062f\u0645");
        AR_OFFLINE.put("Password",              "\u0643\u0644\u0645\u0629 \u0627\u0644\u0645\u0631\u0648\u0631");
        AR_OFFLINE.put("ID",                    "\u0627\u0644\u0631\u0642\u0645 \u0627\u0644\u062a\u0639\u0631\u064a\u0641\u064a");
        AR_OFFLINE.put("NAME",                  "\u0627\u0644\u0627\u0633\u0645");
        AR_OFFLINE.put("ROLE",                  "\u0627\u0644\u062f\u0648\u0631");
        AR_OFFLINE.put("COACH NAME",            "\u0627\u0633\u0645 \u0627\u0644\u0645\u062f\u0631\u0628");
        AR_OFFLINE.put("MEMBER ID",             "\u0631\u0642\u0645 \u0627\u0644\u0639\u0636\u0648");
        AR_OFFLINE.put("AMOUNT",                "\u0627\u0644\u0645\u0628\u0644\u063a");
        AR_OFFLINE.put("DESCRIPTION",           "\u0627\u0644\u0648\u0635\u0641");
        AR_OFFLINE.put("Add New User",          "\u0625\u0636\u0627\u0641\u0629 \u0645\u0633\u062a\u062e\u062f\u0645 \u062c\u062f\u064a\u062f");
        AR_OFFLINE.put("Edit Existing User",    "\u062a\u0639\u062f\u064a\u0644 \u0645\u0633\u062a\u062e\u062f\u0645");
        AR_OFFLINE.put("List  Search  Delete",  "\u0627\u0644\u0642\u0627\u0626\u0645\u0629 \u0648 \u0627\u0644\u0628\u062d\u062b \u0648 \u0627\u0644\u062d\u0630\u0641");
        AR_OFFLINE.put("Add Bill",              "\u0625\u0636\u0627\u0641\u0629 \u0641\u0627\u062a\u0648\u0631\u0629");
        AR_OFFLINE.put("Assign",                "\u062a\u0639\u064a\u064a\u0646");
        AR_OFFLINE.put("Member Status",         "\u062d\u0627\u0644\u0629 \u0627\u0644\u0623\u0639\u0636\u0627\u0621");
        AR_OFFLINE.put("Update My Info",        "\u062a\u062d\u062f\u064a\u062b \u0628\u064a\u0627\u0646\u0627\u062a\u064a");
        AR_OFFLINE.put("NEW NAME",              "\u0627\u0644\u0627\u0633\u0645 \u0627\u0644\u062c\u062f\u064a\u062f");
        AR_OFFLINE.put("NEW USERNAME",          "\u0627\u0633\u0645 \u0645\u0633\u062a\u062e\u062f\u0645 \u062c\u062f\u064a\u062f");
        AR_OFFLINE.put("NEW PASSWORD",          "\u0643\u0644\u0645\u0629 \u0645\u0631\u0648\u0631 \u062c\u062f\u064a\u062f\u0629");
        AR_OFFLINE.put("CURRENT PASSWORD",      "\u0643\u0644\u0645\u0629 \u0627\u0644\u0645\u0631\u0648\u0631 \u0627\u0644\u062d\u0627\u0644\u064a\u0629");
        AR_OFFLINE.put("LOGOUT",                "\u062a\u0633\u062c\u064a\u0644 \u062e\u0631\u0648\u062c");
    }

    private static String getOfflineFallback(String text, String targetLang) {
        if (targetLang.equals("ar")) {
            String result = AR_OFFLINE.get(text);
            if (result != null) return result;
            // try trimmed/cleaned version
            String cleaned = text.replaceAll("[^\\p{L}\\p{N} /]", "").trim();
            result = AR_OFFLINE.get(cleaned);
            return result != null ? result : text;
        }
        return text;
    }

    // ===== BATCH (async) =====
    public static void translateBatch(List<String> texts, String targetLang,
                                      Consumer<Map<String, String>> onDone) {
        if (targetLang.equals("en")) {
            Map<String, String> r = new HashMap<>();
            for (String t : texts) r.put(t, t);
            Platform.runLater(() -> onDone.accept(r));
            return;
        }
        executor.submit(() -> {
            Map<String, String> result   = new HashMap<>();
            Map<String, String> langCache = cache.computeIfAbsent(targetLang, k -> new ConcurrentHashMap<>());
            List<String> toTranslate = new ArrayList<>();
            for (String text : texts) {
                if (langCache.containsKey(text)) result.put(text, langCache.get(text));
                else toTranslate.add(text);
            }
            for (String text : toTranslate) {
                String tr = callMyMemory(text, targetLang);
                langCache.put(text, tr);
                result.put(text, tr);
            }
            Platform.runLater(() -> onDone.accept(result));
        });
    }

    // ===== SYNC (blocking — only for AppState.t()) =====
    public static String translateNow(String text, String targetLang) {
        if (targetLang.equals("en")) return text;
        Map<String, String> langCache = cache.computeIfAbsent(targetLang, k -> new ConcurrentHashMap<>());
        if (langCache.containsKey(text)) return langCache.get(text);
        String result = callMyMemory(text, targetLang);
        langCache.put(text, result);
        return result;
    }

    // ===== MyMemory API — UTF-8 fixed =====
    // ===== MyMemory API — UTF-8 fixed =====
    private static String callMyMemory(String text, String targetLang) {
        try {
            String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8.name());

            String urlStr = "https://api.mymemory.translated.net/get?q="
                    + encoded + "&langpair=en|" + targetLang;

            URL url = new URL(urlStr);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

        // IMPORTANT
            conn.setRequestProperty("Accept", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept-Charset", "UTF-8");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            if (conn.getResponseCode() != 200)
                return text;

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();

            String line;

            while ((line = br.readLine()) != null)
                sb.append(line);

            br.close();
            conn.disconnect();

            String json = sb.toString();

            String marker = "\"translatedText\":\"";

            int start = json.indexOf(marker);

            if (start == -1)
                return text;

            start += marker.length();

            int end = json.indexOf("\"", start);

            if (end == -1)
                return text;

            String translated = json.substring(start, end)
                    .replace("\\n", "\n")
                    .replace("\\t", "\t")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");

        // FIX UNICODE
            translated = unescapeUnicode(translated);

            return translated.isEmpty() ? text : translated;

        } catch (Exception e) {
            return getOfflineFallback(text, targetLang);
        }
    }   
        private static String unescapeUnicode(String s) {

        StringBuilder sb = new StringBuilder();

        int i = 0;

        while (i < s.length()) {

            if (i + 5 < s.length()
                    && s.charAt(i) == '\\'
                    && s.charAt(i + 1) == 'u') {

                try {

                    int code = Integer.parseInt(
                            s.substring(i + 2, i + 6), 16);

                    sb.append((char) code);

                    i += 6;

                } catch (NumberFormatException e) {

                    sb.append(s.charAt(i++));
                }
            
            } else {

                sb.append(s.charAt(i++));
            }
        }

        return sb.toString();
    }
    public static void clearCache() { cache.clear(); }

    public static String getCode(String fullName) {
        return LANGUAGES.getOrDefault(fullName, "en");
    }
}
