package org.lazberry.xmaslegacy.utils;

import java.util.ArrayList;
import java.util.List;

public class KoreanTypewriter {

    private static final char[] CHOSUNG = {
            'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    };
    private static final char[] JUNGSUNG = {
            'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ', 'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ'
    };
    private static final char[] JONGSUNG = {
            '\0', 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ', 'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    };

    public static List<String> buildTypingSteps(String text) {
        List<String> steps = new ArrayList<>();
        StringBuilder prefix = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);

            if (ch >= 0xAC00 && ch <= 0xD7A3) {
                int base = ch - 0xAC00;
                int choIdx = base / (21 * 28);
                int jungIdx = (base % (21 * 28)) / 28;
                int jongIdx = base % 28;

                steps.add(prefix.toString() + CHOSUNG[choIdx]);

                char choJungChar = (char) (0xAC00 + (choIdx * 21 * 28) + (jungIdx * 28));
                steps.add(prefix.toString() + choJungChar);

                if (jongIdx > 0) {
                    steps.add(prefix.toString() + ch);
                }
            } else {
                steps.add(prefix.toString() + ch);
            }

            prefix.append(ch);
        }

        return steps;
    }
}
