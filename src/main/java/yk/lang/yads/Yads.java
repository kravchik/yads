package yk.lang.yads;

import yk.lang.yads.utils.BadException;
import yk.ycollections.YList;

import static yk.lang.yads.utils.YadsWords.ARGS;

public class Yads {

    public static Object readYadsEntity(String s) {
        return YadsEntityResolver.toYadsList(YadsObjectParser.parse(s).getNodeList(ARGS)).assertSize(1).first();
    }

    public static YList<Object> readYadsEntities(String s) {
        return YadsEntityResolver.toYadsList(YadsObjectParser.parse(s).getNodeList(ARGS));
    }

    public static String printYadsEntity(Object s) {
        return new YadsEntityOutput().print(s);
    }

    public static String printYadsEntities(YList<Object> entities) {
        return new YadsEntityOutput().printBody(entities);
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
