package yk.lang.yads;

import yk.lang.yads.congocc.YadsCstParser;
import yk.ycollections.YList;

public class Yads {

    public static Object readYadsEntity(String s) {
        return YadsEntityFromCst.translate(YadsCstParser.parse(s).children).assertSize(1).first();
    }

    public static YList<Object> readYadsEntities(String s) {
        return YadsEntityFromCst.translate(YadsCstParser.parse(s).children);
    }

    public static String printYadsEntity(Object s) {
        return new YadsPrinter().print(s);
    }

    public static String printYadsEntities(YList<Object> entities) {
        return new YadsPrinter().printBody(entities);
    }

    public static Object readJava(String s, Class... cc) {
        return new YadsJavaFromEntity()
            .addImport(cc)
            .deserialize(readYadsEntity(s));
    }

    public static <T> T readJava(Class<T> clazz, String s, Class... cc) {
        return (T) new YadsJavaFromEntity()
            .addImport(clazz)
            .addImport(cc)
            .deserialize(readYadsEntity(s));
    }

    public static <T> T readJavaBody(Class<T> clazz, String text, Class... cc) {
        return (T) new YadsJavaFromEntity()
            .addImport(cc)
            .deserializeObject(null, clazz, readYadsEntities(text));
    }

    public static String printJava(Object o) {
        return printYadsEntity(new YadsJavaToEntity().serialize(o));
    }

    public static String printJavaBody(Object o) {
        return printYadsEntities(((YadsEntity) new YadsJavaToEntity().serialize(o)).children);
    }
}
