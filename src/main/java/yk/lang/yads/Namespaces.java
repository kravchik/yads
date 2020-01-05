package yk.lang.yads;

import yk.jcommon.collections.YList;

import static yk.jcommon.collections.YArrayList.al;

public class Namespaces {

    public YList<NamespaceEntry> entries = al();

    public static Namespaces packages(String... ss) {
        Namespaces result = new Namespaces();
        result.enterScope();
        for (String s : ss) result.addPackage(s);
        return result;
    }

    public void enterScope() {
        entries.add(new NamespaceEntry());
    }

    public void exitScope() {
        entries.remove(entries.size() - 1);
    }

    public void addPackage(String p) {
        if (entries.isEmpty()) throw new RuntimeException("Not in any scope");
        entries.get(entries.size() - 1).packages.add(p);
    }

    public void addClass(String className) {
        if (entries.isEmpty()) throw new RuntimeException("Not in any scope");
        String cn = al(className.split("\\.")).last();
        entries.get(entries.size() - 1).classes.put(cn, className);
    }

    public Class findClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ignore) { }

        for (int i = entries.size() - 1; i >= 0; i--) {
            NamespaceEntry entry = entries.get(i);
            if (entry.classes.containsKey(className)) try {
                return Class.forName(entry.classes.get(className));
            } catch (ClassNotFoundException ignore) { }

            for (String p : entry.packages) try {
                return Class.forName((p.length() > 0 ? p + "." : "") + className);
            } catch (ClassNotFoundException ignore) { }
        }
        return null;
    }

}
