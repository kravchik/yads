package yk.lang.yads;

import org.apache.commons.lang3.text.translate.*;

import static yk.lang.yads.YadsWords.*;

public class YadsUtils {
    public static YadsNode constNode(Caret caret, String type, Object value) {
        return new YadsNode(NODE_TYPE, CONST, TYPE, type, VALUE, value, CARET, caret);
    }

    public static YadsNode constNode(Object value) {
        return new YadsNode(NODE_TYPE, CONST, VALUE, value);
    }

    //TODO avoid using commons.lang3
    public static final CharSequenceTranslator UNESCAPE_YADS_SINGLE_QUOTES =
            new AggregateTranslator(
                    new OctalUnescaper(),     // .between('\1', '\377'),
                    new UnicodeUnescaper(),
                    new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_UNESCAPE()),
                    new LookupTranslator(
                            new String[][] {
                                    {"\\\\", "\\"},
                                    {"\\'", "'"},
                                    {"\\", ""}
                            })
            );

    public static String unescapeDoubleQuotes(String s) {
        String woQuotes = s.substring(1, s.length() - 1);
        if (woQuotes.startsWith("\n") && woQuotes.endsWith("\n"))
            woQuotes = woQuotes.substring(1, woQuotes.length() - 1);
        return UNESCAPE_YADS_DOUBLE_QUOTES.translate(woQuotes);
    }

    public static String unescapeSingleQuotes(String s) {
        String woQuotes = s.substring(1, s.length() - 1);
        if (woQuotes.startsWith("\n") && woQuotes.endsWith("\n"))
            woQuotes = woQuotes.substring(1, woQuotes.length() - 1);
        return UNESCAPE_YADS_SINGLE_QUOTES.translate(woQuotes);
    }

    public static final CharSequenceTranslator UNESCAPE_YADS_DOUBLE_QUOTES =
            new AggregateTranslator(
                    new OctalUnescaper(),     // .between('\1', '\377'),
                    new UnicodeUnescaper(),
                    new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_UNESCAPE()),
                    new LookupTranslator(
                            new String[][] {
                                    {"\\\\", "\\"},
                                    {"\\\"", "\""},
                                    {"\\", ""}
                            })
            );

    public static final AggregateTranslator ESCAPE_YADS_SINGLE_QUOTES = new AggregateTranslator(
            UnicodeEscaper.outsideOf(' ', '~'),
            new LookupTranslator(new String[][]{{"'", "\\'"}, {"\\", "\\\\"}}),
            new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_ESCAPE()));
    public static final AggregateTranslator ESCAPE_YADS_DOUBLE_QUOTES = new AggregateTranslator(
            UnicodeEscaper.outsideOf(' ', '~'),
            new LookupTranslator(new String[][]{{"\"", "\\\""}, {"\\", "\\\\"}}),
            new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_ESCAPE()));

}
