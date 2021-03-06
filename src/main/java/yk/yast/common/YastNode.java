package yk.yast.common;

import yk.jcommon.collections.YList;
import yk.jcommon.collections.YMap;
import yk.jcommon.utils.BadException;

import static yk.jcommon.collections.YHashMap.hm;
import static yk.yast.common.YadsWords.*;

/**
 * Created with IntelliJ IDEA.
 * User: yuri
 * Date: 30/08/16
 * Time: 17:54
 */
public class YastNode {//TODO rename YadsNode
    public static long NEXT_ID;
    public final long id;
    public YMap<String, Object> map;

    public static YastNode node(String type, String k, Object v, Object... kv) {
        if ((kv.length % 2) != 0) BadException.die("Expected even count");
        YastNode result = new YastNode();
        result.map = hm();
        result.map.put(NODE_TYPE, type);
        if (v != null) result.map.put(k, v);
        for (int i = 0; i < kv.length; i += 2) if (kv[i+1] != null) result.map.put((String) kv[i], kv[i + 1]);
        return result;
    }

    public static YastNode node(String type) {
        YastNode result = new YastNode();
        result.map = hm();
        result.map.put(NODE_TYPE, type);
        return result;
    }

    public boolean isType(String t) {
        return t.equals(map.get(NODE_TYPE));
    }

    private YastNode() {
        id = NEXT_ID++;
    }

    public YastNode(String k, Object v, Object... kv) {
        id = NEXT_ID++;
        map = hm(k, v, kv);
    }

    public YastNode(long id, YMap<String, Object> map) {
        this.id = id;
        this.map = map;
    }

    public YastNode(long id) {
        this.id = id;
    }

    public YastNode with(String k, Object v, Object... kv) {
        return new YastNode(id, map.with(k, v, kv));
    }

    public YastNode withRearrange(String k, Object v, Object... kv) {
        YastNode result = new YastNode(id, map);
        result.map.remove(k);
        result.map.put(k, v);
        for (int i = 0; i < kv.length; i += 2) result.map.remove(kv[i]);
        for (int i = 0; i < kv.length; i += 2) if (kv[i+1] != null) result.map.put((String) kv[i], kv[i + 1]);
        return result;
    }

    public YastNode with(YMap other) {
        return new YastNode(id, map.with(other));
    }

    @Override
    public String toString() {
        return "{" + id + " " + map.toString(" ") + "}";
    }

    public Long getLong(String name) {
        return (Long)map.get(name);
    }

    public String getType() {
        return (String) map.get(NODE_TYPE);
    }

    public String getString(String name) {
        Object result = map.get(name);
        if (result == null || !(result instanceof String)) {
            throw BadException.die("can't get string " + name + " from " + this);
        }
        return (String) result;
    }

    public String getString(String... nn) {
        return (String) getLast(nn);
    }

    public YastNode getNode(String name) {
        return (YastNode) map.get(name);
    }

    public YastNode getNode(String... nn) {
        return (YastNode) getLast(nn);
    }

    private Object getLast(String... nn) {
        YastNode result = this;
        for (int i = 0; i < nn.length - 1; i++) {
            String n = nn[i];
            Object cur = result.map.get(n);
            if (!(cur instanceof YastNode)) return null;
            result = (YastNode) cur;
        }
        return result.map.get(nn[nn.length - 1]);
    }

    public YList<YastNode> getNodeList(String name) {
        return (YList<YastNode>) map.get(name);
    }

    public boolean getBoolean(String key) {
        Boolean result = (Boolean) map.get(key);
        return result != null && result;
    }

    public boolean getBoolean(String... kk) {
        Boolean result = (Boolean) getLast(kk);
        return result != null && result;
    }

    public float getFloat(String key) {
        Object result = map.get(key);
        if (result == null) throw BadException.die("no (float) value for key: " + key);
        return (Float) result;
    }
    public int getInt(String name) {
        return (Integer)map.get(name);
    }

    public static YastNode ref(String name) {
        return node(REF, NAME, name);
    }
    public static YastNode dot(Object left, String right) {
        if (right == null) {
            BadException.shouldNeverReachHere();
        }
        return node(DOT, LEFT, left, NAME, right);
    }

    public boolean has(String key) {
        return !isAbsent(key);
    }

    public boolean hasNotEmptyList(String key) {
        return !isAbsent(key);
    }

    public boolean isAbsent(String key) {
        return map.get(key) == null;
    }

    public Object get(String key) {
        return map.get(key);
    }
}
