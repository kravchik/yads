package yk.lang.yads;

import yk.jcommon.collections.YList;
import yk.yast.common.YastNode;

public class Yads {
    /**
     * Deserialize the only element. It is an error to have several elements in text. It is possible to have imports.
     */
    public static Object deserialize(String text) {
        YastNode parse = YadsParser.parse(text);
        return new YadsDeserializer().deserialize(new YadsResolver().resolve(parse));
    }

    public static Object deserialize(YList<String> imports, String text) {
        YadsDeserializer deserializer = new YadsDeserializer();
        deserializer.namespaces.enterScope();
        for (String i : imports) deserializer.namespaces.addClass(i);

        YastNode parse = YadsParser.parse(text);
        return deserializer.deserialize(new YadsResolver().resolve(parse));
    }

    public static Object deserializeBody(String text) {
        YastNode parsed = YadsParser.parse(text);
        return new YadsDeserializer().deserializeConcreteType(null, new YadsResolver().resolve(parsed));
    }

    public static <T> T deserializeBody(Class<T> type, String text) {
        YastNode parsed = YadsParser.parse(text);
        return new YadsDeserializer().deserializeConcreteType(type, new YadsResolver().resolve(parsed));
    }

    public static <T> T deserializeBody(YList<String> imports, Class<T> type, String text) {
        YadsDeserializer deserializer = new YadsDeserializer();
        deserializer.namespaces.enterScope();
        for (String i : imports) deserializer.namespaces.addClass(i);
        YastNode parsed = YadsParser.parse(text);
        return deserializer.deserializeConcreteType(type, new YadsResolver().resolve(parsed));
    }

    public static String serialize(Object someObject) {
        return new NodesToString().toString(new YadsSerializer().serialize(someObject));
    }

    public static String serialize(YList<String> imports, Object someObject) {
        YadsSerializer yadsToNodes = new YadsSerializer();
        yadsToNodes.addDefaultImports(imports);
        return new NodesToString().toString(yadsToNodes.serialize(someObject));
    }

    //TODO 'avoid compact of the first level' option
    public static String serializeBody(YList<String> imports, Object someObject) {
        YadsSerializer yadsToNodes = new YadsSerializer();
        yadsToNodes.addDefaultImports(imports);
        return new NodesToString().toString(yadsToNodes.serialize(someObject));
    }

    public static String serializeBody(Object someObject) {
        return new NodesToString().toStringBody(new YadsSerializer().serializeBody(someObject));
    }
}
