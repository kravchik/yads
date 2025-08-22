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

    public static Object readJava(String s) {
        return new YadsJavaDeserializer().deserialize(readYadsEntity(s));
    }

    public static String printJava(Object o) {
        return printYadsEntity(new YadsJavaSerializer().serialize(o));
    }

    //TODO readJava(Class c, String s)
    //TODO readJavaBody(Class c, String s)
    //TODO printJavaBody(String s)
}
