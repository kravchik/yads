package yk.lang.yads;

import yk.ycollections.YList;
import yk.ycollections.YMap;

import java.io.InputStream;

import static yk.lang.yads.YadsWords.ARGS;
import static yk.lang.yads.YadsWords.NAMED_ARGS;
import static yk.ycollections.YArrayList.al;

//TODO define default imports as classes instead of strings
//TODO separate methods for serialize serializeFormatted ?
/**
 * Just a collection of some frequently used methods to help avoid the usual boilerplate.
 * Also serves as an example of how to use YadsNodeParser, YadsObjectSerializer, YadsObjectDeserializer.
 * <br>
 * Methods in this class a 'greedy'. They try to read everything available and throw an error if there are 'unexpected more'.
 * You can easily implement 'not greedy' analogs.
 */
public class Yads {

    public static Object parseYadsList(String s) {
        return YadsListResolver.toYadsList(YadsNodeParser.parse(s).getNodeList(ARGS)).assertSize(1).first();
    }

    public static YadsList parseYadsListBody(String s) {
        return new YadsList(null, YadsListResolver.toYadsList(YadsNodeParser.parse(s).getNodeList(ARGS)));
    }

    public static String printYadsList(Object s) {
        return new YadsListOutput().yadsListToString(s);
    }

    public static String printYadsList(Object s, int maxWidth) {
        YadsListOutput yadsListOutput = new YadsListOutput();
        yadsListOutput.maxWidth = maxWidth;
        return yadsListOutput.yadsListToString(s);
    }

    /**
     * Deserialize the only element.
     * <br>The next string will be deserialized into an array of strings "a", "b" and "c":
     * <br>
     * <br>
     * <code>(a "b" 'c')</code>
     * <br>
     * <br>
     * Or, the next string will be deserialized into the instance of class TestClassNumbers (with the help of imports):
     * <br>
     * <br>
     * <code>import yk.lang.yads.TestClassNumbers<br>TestClassNumbers()</code>
     * <br>
     * <br>
     * @param text to be parsed
     * @return deserialized value
     */
    public static Object deserialize(String text) {
        return deserialize(al(), text);
    }

    //TODO other versions
    //TODO comments
    //TODO tests
    public static Object deserialize(InputStream is) {
        return deserialize(al(), is);
    }

    /**
     * Deserialize the only element with the use of a list of default imports to be used.
     * <br>For example, if <code>imports == al("yk.lang.yads.TestClassNumbers")</code>, then the next text can avoid stating imports and being deserialized into the instance of class TestClassNumbers:
     * <br>
     * <br>
     * <code>TestClassNumbers()</code>
     * <br>
     * <br>
     * @param imports default imports
     * @param text to be parsed
     * @return deserialized value
     */
    public static Object deserialize(YList<String> imports, String text) {
        YadsObjectDeserializer deserializer = new YadsObjectDeserializer();
        deserializer.namespaces.enterScope();
        for (String i : imports) deserializer.namespaces.addClass(i);

        YadsNode parse = YadsNodeParser.parse(text);
        return deserializeTheOnlyElement(deserializer, new YadsNodeResolver().resolve(parse));
    }

    public static Object deserialize(YList<String> imports, InputStream is) {

        YadsNode result;
        try {
            result = new YadsNodeParser(is).parseListBodyNode();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        YadsObjectDeserializer deserializer = new YadsObjectDeserializer();
        deserializer.namespaces.enterScope();
        for (String i : imports) deserializer.namespaces.addClass(i);
        return deserializeTheOnlyElement(deserializer, new YadsNodeResolver().resolve(result));
    }

    public static Object deserializeBody(String text) {
        YadsNode parsed = YadsNodeParser.parse(text);
        return new YadsObjectDeserializer().deserializeSpecificType(null, new YadsNodeResolver().resolve(parsed));
    }

    public static <T> T deserializeBody(Class<T> type, String text) {
        YadsNode parsed = YadsNodeParser.parse(text);
        return new YadsObjectDeserializer().deserializeSpecificType(type, new YadsNodeResolver().resolve(parsed));
    }

    public static <T> T deserializeBody(YList<String> imports, String text) {
        return deserializeBody(imports, null, text);
    }

    public static <T> T deserializeBody(YList<String> imports, Class<T> type, String text) {
        YadsObjectDeserializer deserializer = new YadsObjectDeserializer();
        deserializer.namespaces.enterScope();
        for (String i : imports) deserializer.namespaces.addClass(i);
        YadsNode parsed = YadsNodeParser.parse(text);
        return deserializer.deserializeSpecificType(type, new YadsNodeResolver().resolve(parsed));
    }

    public static <K, V> YMap<K, V> deserializeMapBody(String text) {
        return deserializeBody(al(), YMap.class, text);
    }

    public static <K, V> YMap<K, V> deserializeMapBody(YList<String> imports, String text) {
        return deserializeBody(imports, YMap.class, text);
    }

    public static <V> YList<V> deserializeListBody(String text) {
        return deserializeBody(al(), YList.class, text);
    }

    public static <V> YList<V> deserializeListBody(YList<String> imports, String text) {
        return deserializeBody(imports, YList.class, text);
    }

    public static String print(Object someObject) {
        return new YadsNodeOutput().toString(new YadsObjectSerializer(false).serialize(someObject));
    }

    /**
     * Serialize the only element.
     * <br>The result of serializing of a list of strings "a", "b" and "c" will be:
     * <br>
     * <br>
     * <code>(a b c)</code>
     * <br>
     * <br>
     * The result of serializing of an instance of class TestClassNumbers (with the help of imports):
     * <br>
     * <br>
     * <code>import yk.lang.yads.TestClassNumbers<br>TestClassNumbers()</code>
     * <br>
     * <br>
     * @param someObject to be serialized
     * @return deserialized value
     */
    public static String serialize(Object someObject) {
        return new YadsNodeOutput().toString(new YadsObjectSerializer().serialize(someObject));
    }

    /**
     * Serialize the only element with default imports which leads to absence of imports in the result text.
     * The result of serializing of an instance of class TestClassNumbers:
     * <br>Yads.serialize(al("yk.lang.yads.TestClass2"), new TestClass2()):
     * <br>
     * <br>
     * <br>
     * <code>TestClassNumbers()</code>
     * <br>
     * <br>
     * @param imports - default values
     * @param someObject to be serialized
     * @return serialized value
     */
    public static String serialize(YList<String> imports, Object someObject) {
        YadsObjectSerializer yadsToNodes = new YadsObjectSerializer();
        yadsToNodes.addDefaultImports(imports);
        return new YadsNodeOutput().toString(yadsToNodes.serialize(someObject));
    }

    //TODO 'avoid compact of the first level' option
    public static String serializeBody(YList<String> imports, Object someObject) {
        YadsObjectSerializer yadsToNodes = new YadsObjectSerializer();
        yadsToNodes.addDefaultImports(imports);
        return new YadsNodeOutput().toStringBody(yadsToNodes.serializeBody(someObject));
    }

    public static String serializeBody(Object someObject) {
        return new YadsNodeOutput().toStringBody(new YadsObjectSerializer().serializeBody(someObject));
    }

    private static Object deserializeTheOnlyElement(YadsObjectDeserializer des, YadsNode node) {
        try {
            if (null != node.map.get(NAMED_ARGS)) throw new RuntimeException("Unexpected named arg at top level");
            YList result = des.deserializeRawList(node.getNodeList(ARGS));
            if (result.size() != 1) {
                throw new RuntimeException("Unexpected count of elements: " + result.size() + ", expected exactly 1 element. Do you meant using deserializeBody?");
            }
            return result.get(0);
        } catch (RuntimeException re) {
            des.handleException(re);
            return null;
        }
    }


}
