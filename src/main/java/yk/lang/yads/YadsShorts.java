package yk.lang.yads;

import org.apache.commons.lang3.text.translate.*;

import static yk.lang.yads.YadsWords.*;

public class YadsShorts {
    //TODO LIST, MAP, COMPLEX(NAME ARGS NAMED_ARGS)

    //TODO move all consts to YAST and make them common
    public static final String YADS_NAMED = "YADS_NAMED";
    public static final String YADS_UNNAMED = "YADS_UNNAMED";
    public static final String YADS_ARRAY = "YADS_ARRAY";
    public static final String YADS_MAP = "YADS_MAP";

    public static final String YADS_RAW_CLASS = "YADS_RAW_CLASS";//class with a name, but ":" isn't addressed

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
