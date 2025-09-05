package yk.lang.yads;

import org.junit.Test;
import yk.ycollections.YList;
import yk.ycollections.YMap;

import static org.junit.Assert.assertEquals;
import static yk.ycollections.YArrayList.al;
import static yk.ycollections.YHashMap.hm;

public class TestYads {

    public static class Point {
        public int x;
        public int y;

        public Point() {}

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Point)) return false;
            Point other = (Point) obj;
            return x == other.x && y == other.y;
        }
    }

    @Test
    public void testEnity() {
        Object entity = Yads.readYadsEntity("hello(world)");
        assertEquals(new YadsEntity("hello", al("world")), entity);
        assertEquals("hello(world)", Yads.printYadsEntity(entity));

        assertEquals(null, Yads.readYadsEntity("null"));
        assertEquals("null", Yads.printYadsEntity(null));
    }

    @Test
    public void testEnities() {
        YadsEntity ye = new YadsEntity("hello", al("world"));

        Object entity = Yads.readYadsEntities("hello(world) hello(world)");
        assertEquals(al(ye, ye), entity);
        assertEquals("hello(world) hello(world)", Yads.printYadsEntities(al(ye, ye)));

        assertEquals(al(null, null), Yads.readYadsEntities("null null"));
        assertEquals("null null", Yads.printYadsEntities(al(null, null)));
    }

    @Test
    public void testJava() {
        Point point = new Point(10, 20);
        String serialized = Yads.printJava(point);
        assertEquals("Point(x = 10 y = 20)", serialized);
        assertEquals(point, Yads.readJava(Point.class, serialized));

        serialized = Yads.printJava(al(point));
        assertEquals("(Point(x = 10 y = 20))", serialized);
        assertEquals(al(point), Yads.readJava(YList.class, serialized, Point.class));

        serialized = Yads.printJava(hm("a", point));
        assertEquals("(a = Point(x = 10 y = 20))", serialized);
        assertEquals(hm("a", point), Yads.readJava(YMap.class, serialized, Point.class));
    }

    @Test
    public void testJavaBody() {
        Point point = new Point(5, 15);

        String bodyText = Yads.printJavaBody(point);
        assertEquals("x = 5 y = 15", bodyText);
        assertEquals(point, Yads.readJavaBody(Point.class, bodyText));

        bodyText = Yads.printJavaBody(al(point, point));
        assertEquals("ref(1 Point(x = 5 y = 15)) ref(1)", bodyText);
        assertEquals(al(point, point), Yads.readJavaBody(YList.class, bodyText, Point.class));

        bodyText = Yads.printJavaBody(hm("a", point));
        assertEquals("a = Point(x = 5 y = 15)", bodyText);
        assertEquals(hm("a", point), Yads.readJavaBody(YMap.class, bodyText, Point.class));
    }
}