/**
 *
 * Software written by Remi Emonet.
 *
 */
package com.heeere.eaa.adaptertools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReplaceAll {

    private ReplaceAll() {
    }

    public static String replaceAll(String input, String regex, Replacement replacement) {
        return replaceAll(new StringBuffer(), input, regex, replacement).toString();
    }

    public static StringBuffer replaceAll(StringBuffer res, String input, String regex, Replacement replacement) {
        Matcher matcher = Pattern.compile(regex, Pattern.DOTALL | Pattern.MULTILINE).matcher(input);
        while (matcher.find()) {
            matcher.appendReplacement(res, Matcher.quoteReplacement(replacement.replacement(matcher)));
        }
        matcher.appendTail(res);
        return res;
    }

    public static void forEach(String input, String regex, Replacement replacement) {
        Matcher matcher = Pattern.compile(regex, Pattern.DOTALL | Pattern.MULTILINE).matcher(input);
        while (matcher.find()) {
            replacement.replacement(matcher);
        }
    }
}
