package yk.lang.yads.utils;

import yk.lang.yads.YadsObject;
import yk.ycollections.YMap;

import java.io.StringWriter;

import static yk.lang.yads.utils.YadsWords.*;

public class YadsUtils {


    public static YadsObject constNode(Caret caret, String type, Object value) {
        return new YadsObject(NODE_TYPE, CONST, TYPE, type, VALUE, value, CARET, caret);
    }

    public static YadsObject constNode(Object value) {
        return new YadsObject(NODE_TYPE, CONST, VALUE, value);
    }

    //public static final CharSequenceTranslator UNESCAPE_YADS_SINGLE_QUOTES =
    //        new AggregateTranslator(
    //                new OctalUnescaper(),     // .between('\1', '\377'),
    //                new UnicodeUnescaper(),
    //                new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_UNESCAPE()),
    //                new LookupTranslator(
    //                        new String[][] {
    //                                {"\\\\", "\\"},
    //                                {"\\'", "'"},
    //                                {"\\", ""}
    //                        })
    //        );

    private static String handleQuotes(String s) {
        return s.substring(1, s.length() - 1);
    }

    public static String unescapeDoubleQuotes(String s) {
        return unescapeQuotes(handleQuotes(s), '"');
    }

    public static String unescapeSingleQuotes(String s) {
        return unescapeQuotes(handleQuotes(s), '\'');
    }

    public static String escapeDoubleQuotes(String s) {
        return escapeQuotes(s, '"');
    }

    public static String escapeSingleQuotes(String s) {
        return escapeQuotes(s, '\'');
    }

    //public static final CharSequenceTranslator UNESCAPE_YADS_DOUBLE_QUOTES =
    //        new AggregateTranslator(
    //                new OctalUnescaper(),     // .between('\1', '\377'),
    //                new UnicodeUnescaper(),
    //                new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_UNESCAPE()),
    //                new LookupTranslator(
    //                        new String[][] {
    //                                {"\\\\", "\\"},
    //                                {"\\\"", "\""},
    //                                {"\\", ""}
    //                        })
    //        );
    //public static final AggregateTranslator ESCAPE_YADS_SINGLE_QUOTES = new AggregateTranslator(
    //        UnicodeEscaper.outsideOf(' ', '~'),
    //        new LookupTranslator(new String[][]{{"'", "\\'"}, {"\\", "\\\\"}}),
    //        new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_ESCAPE()));
    //public static final AggregateTranslator ESCAPE_YADS_DOUBLE_QUOTES = new AggregateTranslator(
    //        UnicodeEscaper.outsideOf(' ', '~'),
    //        new LookupTranslator(new String[][]{{"\"", "\\\""}, {"\\", "\\\\"}}),
    //        new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_ESCAPE()));

    //TODO OCTAL
    //TODO UNICODE
    public static String unescape(String input, YMap<Character, Character> unescapes) {
        StringWriter out = new StringWriter();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '\\') {
                i++;
                if (i >= input.length()) throw new RuntimeException("Uncompleted escape sequence");
                c = input.charAt(i);
                if (!unescapes.containsKey(c)) throw new RuntimeException("Unexpected escaped character: " + c);
                out.write(unescapes.get(c));
            } else {
                out.write(c);
            }
        }
        return out.toString();
    }

    public static String unescapeQuotes(String input, char quote) {
        StringWriter out = new StringWriter();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '\\') {
                i++;
                if (i >= input.length()) throw new RuntimeException("Uncompleted escape sequence");
                c = input.charAt(i);
                if (c == quote) out.write(c);
                else if (c == '\\') out.write(c);
                else throw new RuntimeException("Unsupported escaped symbol " + c);
            } else {
                out.write(c);
            }
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
}
