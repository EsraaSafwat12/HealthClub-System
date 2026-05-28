package com.mycompany.healthclubsystem;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * WhatsAppService — sends WhatsApp messages via Twilio API.
 *
 * SETUP (do once):
 *   1. Create a Twilio account at https://www.twilio.com
 *   2. Enable the WhatsApp sandbox (or buy a WhatsApp number)
 *   3. Set your credentials in the constants below (or load from a config file)
 *
 * Usage:
 *   WhatsAppService.send("+201001234567", "Hello from Power Gym! 💪");
 */
public class WhatsAppService {

    // ── Twilio credentials ───────────────────────────────────────
    // IMPORTANT: Replace with your real Twilio credentials.
    // For production, load from a config file or environment variable.
    private static String ACCOUNT_SID  = "YOUR_TWILIO_ACCOUNT_SID";
    private static String AUTH_TOKEN   = "YOUR_TWILIO_AUTH_TOKEN";
    private static String FROM_NUMBER  = "whatsapp:+14155238886"; // Twilio sandbox default

    /** Update credentials at runtime (call from Settings screen). */
    public static void setCredentials(String sid, String token, String from) {
        ACCOUNT_SID = sid;
        AUTH_TOKEN  = token;
        FROM_NUMBER = "whatsapp:" + from;
    }

    // ── Core send method ─────────────────────────────────────────

    /**
     * Send a WhatsApp message to a phone number.
     * @param toPhone  recipient phone in international format, e.g. "+201001234567"
     * @param message  message body
     * @return true if Twilio accepted the request (HTTP 200/201)
     */
    public static boolean send(String toPhone, String message) {
        if (ACCOUNT_SID.startsWith("YOUR_")) {
            System.out.println("[WhatsApp] Credentials not configured. Simulating send to "
                + toPhone + ": " + message);
            return true; // simulate in dev mode
        }
        try {
            String urlStr = "https://api.twilio.com/2010-04-01/Accounts/"
                          + ACCOUNT_SID + "/Messages.json";
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            String auth = Base64.getEncoder().encodeToString(
                (ACCOUNT_SID + ":" + AUTH_TOKEN).getBytes(StandardCharsets.UTF_8));
            conn.setRequestProperty("Authorization", "Basic " + auth);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String body = "To="    + URLEncoder.encode("whatsapp:" + toPhone, "UTF-8")
                        + "&From=" + URLEncoder.encode(FROM_NUMBER, "UTF-8")
                        + "&Body=" + URLEncoder.encode(message, "UTF-8");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            System.out.println("[WhatsApp] Sent to " + toPhone + " → HTTP " + code);
            return code == 200 || code == 201;

        } catch (Exception e) {
            System.out.println("[WhatsApp ERROR] " + e.getMessage());
            return false;
        }
    }

    // ── Pre-built message templates ───────────────────────────────

    /** Subscription expiry warning (sent 3 days before expiry). */
    public static String msgExpiryWarning(String memberName, String expiryDate) {
        return "🏋️ *POWER GYM*\n\n"
             + "مرحباً " + memberName + "!\n"
             + "⚠️ اشتراكك ينتهي في: *" + expiryDate + "*\n"
             + "جدد اشتراكك الآن وواصل رحلتك! 💪\n"
             + "📞 اتصل بنا أو زور الجيم لتجديد الاشتراك.";
    }

    /** Check-in confirmation sent to member on attendance scan. */
    public static String msgCheckIn(String memberName, String time) {
        return "🏋️ *POWER GYM*\n\n"
             + "✅ تم تسجيل دخولك يا " + memberName + "!\n"
             + "🕐 الوقت: " + time + "\n"
             + "💪 تمرين موفق — اشتغل بجد وانت هتوصل! 🔥";
    }

    /** Workout completion congratulations. */
    public static String msgWorkoutComplete(String memberName) {
        return "🏋️ *POWER GYM*\n\n"
             + "🎉 *تهانينا " + memberName + "!*\n"
             + "✅ أكملت كل تمارين اليوم!\n"
             + "📈 استمر هكذا وستصل إلى هدفك! 💪\n"
             + "شارك إنجازك مع أصدقائك! 🏆";
    }

    /** New workout plan assigned by coach. */
    public static String msgNewPlan(String memberName, String coachName, String goal) {
        return "🏋️ *POWER GYM*\n\n"
             + "📋 خطة تدريب جديدة لـ " + memberName + "!\n"
             + "👨‍🏫 المدرب: *" + coachName + "*\n"
             + "🎯 الهدف: *" + goal + "*\n"
             + "سجل دخولك للتطبيق لمشاهدة خطتك كاملة! 💪";
    }

    /** Bill payment reminder. */
    public static String msgBillReminder(String memberName, double amount, String dueDate) {
        return "🏋️ *POWER GYM*\n\n"
             + "💳 تذكير بالدفع — " + memberName + "\n"
             + "المبلغ المستحق: *" + String.format("%.2f", amount) + " EGP*\n"
             + "تاريخ الاستحقاق: *" + dueDate + "*\n"
             + "للدفع أو الاستفسار تواصل معنا! 📞";
    }

    /** Referral reward notification. */
    public static String msgReferralReward(String memberName, String newEnd) {
        return "🏋️ *POWER GYM*\n\n"
             + "🎁 *مكافأة الإحالة!*\n"
             + "شكراً " + memberName + " على دعوة صديقك!\n"
             + "🎉 تم إضافة *شهر مجاني* لاشتراكك!\n"
             + "الاشتراك الجديد ينتهي في: *" + newEnd + "*\n"
             + "واصل دعوة أصدقائك للمزيد من المكافآت! 🏆";
    }

    /** Freeze confirmation. */
    public static String msgFreezeConfirm(String memberName, String fromDate) {
        return "🏋️ *POWER GYM*\n\n"
             + "❄️ تم تجميد اشتراكك يا " + memberName + "\n"
             + "من تاريخ: *" + fromDate + "*\n"
             + "سيتم إضافة أيام التجميد لاشتراكك تلقائياً عند الإلغاء.\n"
             + "نتمنى لك الشفاء العاجل! 🙏";
    }

    /** Contract signed notification. */
    public static String msgContractSigned(String memberName, String contractCode) {
        return "🏋️ *POWER GYM*\n\n"
             + "📄 *عقد العضوية موقع!*\n"
             + "عزيزي " + memberName + "\n"
             + "رقم العقد: *" + contractCode + "*\n"
             + "✅ تم توقيع عقدك بنجاح.\n"
             + "مرحباً بك في عائلة Power Gym! 💪";
    }
}
