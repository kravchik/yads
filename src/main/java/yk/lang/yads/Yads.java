package yk.lang.yads;

import yk.lang.yads.utils.BadException;
import yk.ycollections.YList;

import static yk.lang.yads.utils.YadsWords.ARGS;

public class Yads {

    public static Object readYadsEntity(String s) {
        return YadsEntityResolver.toYadsList(YadsObjectParser.parse(s).getNodeList(ARGS)).assertSize(1).first();
    }

    public static YList<Object> readYadsEntityBody(String s) {
        return YadsEntityResolver.toYadsList(YadsObjectParser.parse(s).getNodeList(ARGS));
    }
    //TODO test
    public static String printYadsEntity(Object s) {
        return new YadsEntityOutput().print(s);
    }
    //TODO test
    public static String printYadsEntityBody(YadsEntity s) {
        return new YadsEntityOutput().printBody(s);
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
