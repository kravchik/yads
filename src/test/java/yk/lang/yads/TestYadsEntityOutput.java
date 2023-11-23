package yk.lang.yads;

import org.junit.Test;
import yk.lang.yads.utils.BadException;
import yk.ycollections.Tuple;
import yk.ycollections.YList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static junit.framework.TestCase.assertEquals;

public class TestYadsEntityOutput {
    @Test
    public void test1() {
        YList<Object> yl = Yads.readYadsEntities(UtilsForTests.readResource("formatting.cases.yads"));
        int maxWidth = 100;
        for (Object o : yl) {
            if (o instanceof Tuple) {
                if (((Tuple<?, ?>) o).a.equals("maxWidth")) {
                    maxWidth = ((Number)((Tuple<?, ?>) o).b).intValue();
                } else BadException.notImplemented("o");
            } else if (o instanceof String) {
                String s = (String) o;
                assertEquals(s, "\n" + new YadsEntityOutput().setMaxWidth(maxWidth).print(Yads.readYadsEntity(s)) + "\n");
            }
        }
    }

    public static String resourceAsString(String name) {
        return streamToString(resourceAsStream(name));
    }
    public static InputStream resourceAsStream(String name) {
        return TestYadsEntityOutput.class.getClassLoader().getResourceAsStream(name);
    }
    public static String streamToString(InputStream in) {
        if (in == null) return null;
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String l;
        StringBuilder sb = new StringBuilder();
        try {
            while((l = br.readLine()) != null) sb.append(l).append("\n");
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
