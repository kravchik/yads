package yk.lang.yads;

import yk.lang.yads.congocc.YadsCstParser;
import yk.ycollections.YList;

public class Yads {

    public static Object readYadsEntity(String s) {
        return YadsEntityDeserializer.resolveKeyValues(YadsCstParser.parse(s).children).assertSize(1).first();
    }

    public static YList<Object> readYadsEntities(String s) {
        return YadsEntityDeserializer.resolveKeyValues(YadsCstParser.parse(s).children);
    }

    public static String printYadsEntity(Object s) {
        return new YadsCstPrinter().print(s);
    }

    public static String printYadsEntities(YList<Object> entities) {
        return new YadsCstPrinter().printBody(entities);
    }

    public static Object readJava(String s, Class... cc) {
        return new YadsJavaDeserializer(cc).deserialize(readYadsEntity(s));
    }

    public static <T> T readJava(Class<T> clazz, String s, Class... cc) {
        Object result = new YadsJavaDeserializer(cc).deserialize(readYadsEntity(s));
        if (!clazz.isInstance(result)) throw new RuntimeException("Expected " + clazz.getSimpleName() + " but got " + result.getClass().getSimpleName());
        return (T) result;
    }

    public static <T> T readJavaBody(Class<T> clazz, String text, Class... cc) {
        return (T) new YadsJavaDeserializer(cc).deserializeObject(null, clazz, readYadsEntities(text));
    }

    public static String printJava(Object o) {
        return printYadsEntity(new YadsJavaSerializer().serialize(o));
    }

    public static String printJavaBody(Object o) {
        return printYadsEntities(((YadsEntity)new YadsJavaSerializer().serialize(o)).children);
    }
}
