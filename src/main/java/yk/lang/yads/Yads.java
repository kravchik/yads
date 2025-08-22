package yk.lang.yads;

import yk.lang.yads.congocc.YadsCstParser;
import yk.lang.yads.utils.BadException;
import yk.ycollections.YList;

public class Yads {

    public static Object readYadsEntity(String s) {
        return YadsCstResolver.resolveKeyValues(YadsCstParser.parse(s).children).assertSize(1).first();
    }

    public static YList<Object> readYadsEntities(String s) {
        return YadsCstResolver.resolveKeyValues(YadsCstParser.parse(s).children);
    }

    public static String printYadsEntity(Object s) {
        return new YadsCstOutput().print(s);
    }

    public static String printYadsEntities(YList<Object> entities) {
        return new YadsCstOutput().printBody(entities);
    }

    /**
     * Converts YadsEntities to Lists and Maps. Throws away any comments.
     * throws exceptions if there are:
     *   YadsEntity with not null name
     *   duplicate keys
     */
    public static Object entitiesToCollections(YadsEntity entity) {
        throw BadException.notImplemented();//TODO implement
    }

}
