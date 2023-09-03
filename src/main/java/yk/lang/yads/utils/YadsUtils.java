package yk.lang.yads.utils;

import yk.lang.yads.YadsNode;
import yk.ycollections.YSet;

import java.io.StringWriter;

import static yk.lang.yads.utils.YadsWords.*;
import static yk.ycollections.YHashSet.hs;

public class YadsUtils {


    public static YadsNode constNode(Caret caret, String type, Object value) {
        return new YadsNode(NODE_TYPE, CONST, TYPE, type, VALUE, value, CARET, caret);
    }

    public static YadsNode constNode(Object value) {
        return new YadsNode(NODE_TYPE, CONST, VALUE, value);
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

    public static final YSet<Character> ESCAPED = hs('\\', '\b', '\n', '\r', '\f', '\t');
    public static final YSet<Character> DOUBLE_QUOTES_ESCAPED = ESCAPED.with('\"');
    public static final YSet<Character> SINGLE_QUOTES_ESCAPED = hs('\'');

    private static String handleQuotes(String s) {
        String woQuotes = s.substring(1, s.length() - 1);
        if (woQuotes.startsWith("\n") && woQuotes.endsWith("\n"))
            woQuotes = woQuotes.substring(1, woQuotes.length() - 1);
        return woQuotes;
    }

    public static String unescapeDoubleQuotes(String s) {
        return unescape(handleQuotes(s), DOUBLE_QUOTES_ESCAPED);
    }

    public static String unescapeSingleQuotes(String s) {
        return unescape(handleQuotes(s), SINGLE_QUOTES_ESCAPED);
    }

    public static String escapeDoubleQuotes(String s) {
        return escape(s, DOUBLE_QUOTES_ESCAPED);
    }

    public static String escapeSingleQuotes(String s) {
        return escape(s, SINGLE_QUOTES_ESCAPED);
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
    public static String unescape(String input, YSet<Character> toEscape) {
        StringWriter out = new StringWriter();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '\\') {
                i++;
                if (i >= input.length()) throw new RuntimeException("Uncompleted escape sequence");
                c = input.charAt(i);
                if (!toEscape.contains(c)) throw new RuntimeException("Unexpected escaped character: " + c);
                out.write(c);
            } else {
                out.write(c);
            }
        }
        return out.toString();
    }

    public static String escape(String input, YSet<Character> toEscape) {
        StringWriter out = new StringWriter();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (toEscape.contains(c)) out.write("\\" + c);
            else out.write(c);
        }
        return out.toString();
    }
}
