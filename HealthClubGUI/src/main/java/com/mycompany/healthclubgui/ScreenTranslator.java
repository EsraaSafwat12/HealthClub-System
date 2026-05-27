package com.mycompany.healthclubgui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import java.util.*;
import java.util.function.Consumer;

/**
 * Helper: given a map of key->node, translates all keys and updates nodes.
 * Usage:
 *   ScreenTranslator.apply(Map.of(
 *       "Dashboard", dashboardBtn,
 *       "My Members", myMembersBtn
 *   ), AppState.currentLangCode);
 */
public class ScreenTranslator {

    // translate and apply to Button[]
    public static void applyToButtons(Map<String, Button> keyToBtn, String langCode,
                                      Runnable onDone) {
        List<String> keys = new ArrayList<>(keyToBtn.keySet());
        TranslationService.translateBatch(keys, langCode, translations -> {
            for (Map.Entry<String, Button> e : keyToBtn.entrySet()) {
                e.getValue().setText(translations.getOrDefault(e.getKey(), e.getKey()));
            }
            if (onDone != null) onDone.run();
        });
    }

    // translate and apply to Label[]
    public static void applyToLabels(Map<String, Label> keyToLabel, String langCode) {
        List<String> keys = new ArrayList<>(keyToLabel.keySet());
        TranslationService.translateBatch(keys, langCode, translations -> {
            for (Map.Entry<String, Label> e : keyToLabel.entrySet()) {
                e.getValue().setText(translations.getOrDefault(e.getKey(), e.getKey()));
            }
        });
    }

    // translate and apply to Text nodes
    public static void applyToTexts(Map<String, Text> keyToText, String langCode) {
        List<String> keys = new ArrayList<>(keyToText.keySet());
        TranslationService.translateBatch(keys, langCode, translations -> {
            for (Map.Entry<String, Text> e : keyToText.entrySet()) {
                e.getValue().setText(translations.getOrDefault(e.getKey(), e.getKey()));
            }
        });
    }

    // general: translate a list, get back ordered results
    public static void translate(List<String> texts, String langCode,
                                  Consumer<Map<String, String>> onDone) {
        TranslationService.translateBatch(texts, langCode, onDone);
    }
}
