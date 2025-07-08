package yk.lang.yads;

import org.junit.Test;
import yk.lang.yads.congocc.YadsCstParser;
import yk.lang.yads.utils.BadException;
import yk.lang.yads.utils.Reflector;
import yk.ycollections.Tuple;
import yk.ycollections.YList;
import yk.ycollections.YMap;
import yk.ycollections.YSet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static yk.ycollections.YHashSet.hs;

public class TestYadsEntityOutput {
    private static final YSet<String> INT_SETTINGS = hs("maxWidth", "maxLocalWidth", "compactFromLevel");

    @Test
    public void testCases() {
        YMap<String, Integer> settings = INT_SETTINGS.toMap(s -> s, s -> Reflector.get(new YadsCstOutput(), s));

        YList<Object> yl = Yads.readYadsEntities(UtilsForTests.readResource("formatting.cases.sql.style.yads"));
        for (Object o : yl) {
            if (o instanceof Tuple) {
                Tuple t = (Tuple) o;
                if (INT_SETTINGS.contains(t.a)) settings.put((String) t.a, extractInt(t));
                else BadException.notImplemented(o + "");
            } else if (o instanceof String) {
                String s = (String) o;
                //YadsEntityOutput output = new YadsEntityOutput();
                //for (Map.Entry<String, Integer> entry : settings.entrySet()) {
                //    Reflector.set(output, entry.getKey(), entry.getValue());
                //}
                YadsCstOutput cstOutput = new YadsCstOutput();
                for (Map.Entry<String, Integer> entry : settings.entrySet()) {
                    Reflector.set(cstOutput, entry.getKey(), entry.getValue());
                }

                //javacc stack
                //assertEquals(s, "\n" + output.print(YadsEntityResolver.toYadsList(YadsObjectParser.parse(s).getNodeList(ARGS)).assertSize(1).first()) + "\n");

                //congocc stack
                System.out.println(s);
                assertEquals(s, "\n" + cstOutput.print(YadsCstResolver.resolveList(YadsCstParser.parse(s).children).assertSize(1).first()) + "\n");
            }
        }
    }

    private static int extractInt(Tuple t) {
        return ((Number) t.b).intValue();
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
