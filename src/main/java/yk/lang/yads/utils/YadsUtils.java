package yk.lang.yads.utils;

import yk.ycollections.YMap;

import java.io.StringWriter;

import static yk.ycollections.YHashMap.hm;

public class YadsUtils {
    public static final YMap<Character, Character> JAVA_ESCAPES = hm(
        '\t', 't', '\b', 'b', '\n', 'n', '\r', 'r', '\f', 'f', '\"', '\"', '\\', '\\');
    public static final YMap<Character, Character> JAVA_UNESCAPES = JAVA_ESCAPES
        .map((k, v) -> v, (k, v) -> k)
        .with('\'', '\'');


    private static String handleQuotes(String s) {
        return s.substring(1, s.length() - 1);
    }

    public static String unescapeDoubleQuotes(String s) {
        return unescapeQuotes_sqlStyle(handleQuotes(s), '"');
        //return unescapeQuotes(handleQuotes(s), '"');
    }

    public static String unescapeSingleQuotes(String s) {
        return unescapeQuotes_sqlStyle(handleQuotes(s), '\'');
        //return unescapeQuotes(handleQuotes(s), '\'');
    }

    public static String escapeDoubleQuotes(String s) {
        return escapeQuotes_sqlStyle(s, '"');
        //return escapeQuotes(s, '"');
    }

    public static String escapeSingleQuotes(String s) {
        return escapeQuotes_sqlStyle(s, '\'');
        //return escapeQuotes(s, '\'');
    }

    //We should either unescape EVERYTHING, or keep \\ escaped (for next levels to work with)
    public static String unescapeQuotes(String input, char quote) {
        StringWriter out = new StringWriter();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '\\') {
                i++;
                if (i >= input.length()) throw new RuntimeException("Uncompleted escape sequence");
                c = input.charAt(i);
                if (c == '\\' && i == input.length() - 1) out.write('\\');
                else if (c == quote) out.write(c);
                else {
                    out.write('\\');
                    out.write(c);
                }
            } else {
                out.write(c);
            }
        }
        return out.toString();
    }

    public static String unescapeQuotes_sqlStyle(String input, char quote) {
        StringWriter out = new StringWriter();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == quote) {
                i++;
                if (i >= input.length()) throw new RuntimeException("Uncompleted escape sequence");
                c = input.charAt(i);
                if (c != quote) throw new RuntimeException("Expected " + quote + " to be escaped");
            }
            out.write(c);
        }
        return out.toString();
    }

    public static String escape(String input, YMap<Character, Character> toEscape) {
        StringWriter out = new StringWriter();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (toEscape.containsKey(c)) out.write("\\" + toEscape.get(c));
            else out.write(c);
        }
        return out.toString();
    }

    public static String escapeQuotes(String input, char q) {
        StringWriter out = new StringWriter();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == q) out.write("\\" + c);
            else if (c == '\\') out.write("\\\\");
            else out.write(c);
        }
        return out.toString();
    }

    public static String escapeQuotes_sqlStyle(String input, char q) {
        StringWriter out = new StringWriter();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == q) out.write("" + q + q);
            else out.write(c);
        }
        return out.toString();
    }
}
