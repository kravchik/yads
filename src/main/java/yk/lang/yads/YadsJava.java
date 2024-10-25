package yk.lang.yads;

import yk.ycollections.YList;

import static yk.lang.yads.utils.YadsWords.ARGS;
import static yk.lang.yads.utils.YadsWords.NAMED_ARGS;
import static yk.ycollections.YArrayList.al;

public class YadsJava {
    public YList<Class> knownImports = al();
    public Boolean generateImports = true;
    public Boolean skipDefaultValues = true;
    public Boolean strictReferencing = true;
    public Integer maxWidth = 100;
    public String tab = "  ";

    public YadsJava setKnownImports(YList<Class> knownImports) {
        this.knownImports = knownImports;
        return this;
    }

    public YadsJava setGenerateImports(Boolean generateImports) {
        this.generateImports = generateImports;
        return this;
    }

    public YadsJava setSkipDefaultValues(Boolean skipDefaultValues) {
        this.skipDefaultValues = skipDefaultValues;
        return this;
    }

    public YadsJava setStrictReferencing(Boolean strictReferencing) {
        this.strictReferencing = strictReferencing;
        return this;
    }

    public YadsJava setMaxWidth(Integer maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    public YadsJava setTab(String tab) {
        this.tab = tab;
        return this;
    }

    public String serializeImpl(Object o) {
        return buildPrinter().toString(buildSerializer().serialize(o));
    }

    public String serializeBodyImpl(Object o) {
        return buildPrinter().toStringBody(buildSerializer().serializeBody(o));
    }

    private YadsObjectOutput buildPrinter() {
        YadsObjectOutput printer = new YadsObjectOutput();
        printer.maxWidth = maxWidth;
        printer.tab = tab;
        return printer;
    }

    private YadsJavaSerializer buildSerializer() {
        YadsJavaSerializer serializer = new YadsJavaSerializer();
        serializer.skipDefaultValues = skipDefaultValues;
        serializer.strictReferencing = strictReferencing;
        serializer.generateImports = generateImports;
        serializer.addDefaultImports(knownImports.map(c -> c.getName()));
        return serializer;
    }

    public static Object deserialize(String text) {
        return deserialize(al(), text);
    }

    public static Object deserialize(YList<Class> imports, String text) {
        YadsJavaDeserializer deserializer = new YadsJavaDeserializer();
        deserializer.namespaces.enterScope();
        for (Class c : imports) deserializer.namespaces.addClass(c.getCanonicalName());

        YadsObject parse = YadsObjectParser.parse(text);
        return deserializeTheOnlyElement(deserializer, new YadsObjectResolver().resolve(parse));
    }

    public static Object deserializeBody(String text) {
        YadsObject parsed = YadsObjectParser.parse(text);
        return new YadsJavaDeserializer().deserializeSpecificType(null, new YadsObjectResolver().resolve(parsed));
    }

    public static <T> T deserializeBody(Class<T> type, String text) {
        YadsObject parsed = YadsObjectParser.parse(text);
        return new YadsJavaDeserializer().deserializeSpecificType(type, new YadsObjectResolver().resolve(parsed));
    }

    public static <T> T deserializeBody(YList<Class> imports, String text) {
        return deserializeBody(imports, null, text);
    }

    public static <T> T deserializeBody(YList<Class> imports, Class<T> type, String text) {
        YadsJavaDeserializer deserializer = new YadsJavaDeserializer();
        deserializer.namespaces.enterScope();
        for (Class i : imports) deserializer.namespaces.addClass(i.getName());
        YadsObject parsed = YadsObjectParser.parse(text);
        return deserializer.deserializeSpecificType(type, new YadsObjectResolver().resolve(parsed));
    }

    public static String print(Object someObject) {
        return new YadsJava()
            .setStrictReferencing(false)
            .setGenerateImports(false)
            .serializeImpl(someObject);
    }

    public static String printNoLn(Object someObject) {
        return new YadsJava()
            .setMaxWidth(Integer.MAX_VALUE)
            .setStrictReferencing(false)
            .setSkipDefaultValues(false)
            .setGenerateImports(false)
            .serializeImpl(someObject);
    }

    public static String serialize(Object someObject) {
        return new YadsJava()
            .serializeImpl(someObject);
    }

    public static String serialize(YList<Class> imports, Object someObject) {
        return new YadsJava()
            .setKnownImports(imports)
            .serializeImpl(someObject);
    }

    public static String serializeBody(YList<Class> imports, Object someObject) {
        return new YadsJava()
            .setKnownImports(imports)
            .serializeBodyImpl(someObject);
    }

    public static String serializeBody(Object someObject) {
        return new YadsJava()
            .serializeBodyImpl(someObject);
    }

    private static Object deserializeTheOnlyElement(YadsJavaDeserializer des, YadsObject node) {
        try {
            if (null != node.map.get(NAMED_ARGS)) throw new RuntimeException("Unexpected named arg at top level");
            YList result = des.deserializeRawList(node.getNodeList(ARGS));
            if (result.size() != 1) {
                throw new RuntimeException("Unexpected count of elements: " + result.size() + ", expected exactly 1 element. Do you meant using deserializeBody?");
            }
            return result.get(0);
        } catch (RuntimeException re) {
            des.handleException(re);
            return null;
        }
    }
}
