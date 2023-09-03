package yk.lang.yads;

import yk.lang.yads.utils.BadException;
import yk.ycollections.YList;
import yk.ycollections.YMap;

import java.util.List;

import static yk.lang.yads.utils.YadsWords.*;
import static yk.ycollections.YArrayList.al;
import static yk.ycollections.YHashMap.hm;

/**
 * Created with IntelliJ IDEA.
 * User: yuri
 * Date: 30/08/16
 * Time: 17:54
 */
public class YadsNode {
    public static long NEXT_ID;
    public final long id;
    public YMap<String, Object> map;

    public static YadsNode node(String type, String k, Object v, Object... kv) {
        if ((kv.length % 2) != 0) BadException.die("Expected even count");
        YadsNode result = new YadsNode();
        result.map = hm();
        result.map.put(NODE_TYPE, type);
        if (v != null) result.map.put(k, v);
        for (int i = 0; i < kv.length; i += 2) if (kv[i+1] != null) result.map.put((String) kv[i], kv[i + 1]);
        return result;
    }

    public static YadsNode node(String type) {
        YadsNode result = new YadsNode();
        result.map = hm();
        result.map.put(NODE_TYPE, type);
        return result;
    }

    public boolean isType(String t) {
        return t.equals(map.get(NODE_TYPE));
    }

    private YadsNode() {
        id = NEXT_ID++;
    }

    public YadsNode(String k, Object v, Object... kv) {
        id = NEXT_ID++;
        map = hm(k, v, kv);
    }

    public YadsNode(long id, YMap<String, Object> map) {
        this.id = id;
        this.map = map;
    }

    public YadsNode(long id) {
        this.id = id;
    }

    public YadsNode with(String k, Object v, Object... kv) {
        return new YadsNode(id, map.with(k, v, kv).filter((kk, vv) -> vv != null));
    }

    //TODO remove key if value is null?
    public YadsNode put(String k, Object v, Object... kv) {
        if (v != null) map.put(k, v);
        for (int i = 0; i < kv.length; i += 2) {
            Object value = kv[i + 1];
            if (value != null) map.put((String) kv[i], value);
        }
        return this;
    }

    public YadsNode without(String... kk) {
        return new YadsNode(id, map.without(al(kk)));
    }

    public YadsNode withRearrange(String k, Object v, Object... kv) {
        YadsNode result = new YadsNode(id, map);
        result.map.remove(k);
        result.map.put(k, v);
        for (int i = 0; i < kv.length; i += 2) result.map.remove(kv[i]);
        for (int i = 0; i < kv.length; i += 2) if (kv[i+1] != null) result.map.put((String) kv[i], kv[i + 1]);
        return result;
    }

    public YadsNode with(YMap other) {
        return new YadsNode(id, map.with(other).filter((kk, vv) -> vv != null));
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

    public YadsNode getNode(String name) {
        return (YadsNode) map.get(name);
    }

    public YadsNode getNode(String... nn) {
        return (YadsNode) getLast(nn);
    }

    private Object getLast(String... nn) {
        YadsNode result = this;
        for (int i = 0; i < nn.length - 1; i++) {
            String n = nn[i];
            Object cur = result.map.get(n);
            if (!(cur instanceof YadsNode)) return null;
            result = (YadsNode) cur;
        }
        return result.map.get(nn[nn.length - 1]);
    }

    public YList<YadsNode> getNodeList(String... namesPath) {
        return (YList<YadsNode>) getLast(namesPath);
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

    public static YadsNode ref(String name) {
        return node(REF, NAME, name);
    }
    public static YadsNode dot(Object left, String right) {
        if (right == null) {
            BadException.shouldNeverReachHere();
        }
        return node(DOT, LEFT, left, NAME, right);
    }

    public boolean has(String key) {
        return !isAbsent(key);
    }

    public boolean hasNotEmptyList(String key) {
        Object value = map.get(key);
        return value != null && !((List)value).isEmpty();
    }

    public boolean isAbsent(String key) {
        return map.get(key) == null;
    }

    public Object get(String key) {
        return map.get(key);
    }
}
