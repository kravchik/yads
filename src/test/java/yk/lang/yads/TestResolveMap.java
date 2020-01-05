package yk.lang.yads;

import org.junit.Test;
import yk.jcommon.collections.YMap;
import yk.jcommon.match2.Matcher;
import yk.yast.common.YastNode;

import static org.junit.Assert.*;
import static yk.jcommon.collections.YArrayList.al;
import static yk.jcommon.collections.YHashMap.hm;
import static yk.lang.yads.YadsShorts.*;
import static yk.yast.common.Words.*;

public class TestResolveMap {

    @Test
    public void translate() {

        //assertPattern(hm(NODE_TYPE, YADS_MAP, NAMED_ARGS, hm("a", hm(NODE_TYPE, CONST, VALUE, "b"))),
        //        new ResolveMap().resolve(YadsSimpleParser.parse("(a : b)")));
        //
        //assertPattern(hm(NODE_TYPE, YADS_UNNAMED, NAMED_ARGS, hm("a", hm(NODE_TYPE, CONST, VALUE, "b")), ARGS, al(constNode("c"))),
        //        new ResolveMap().resolve(YadsSimpleParser.parse("(a : b c)")));
        //
        //assertPattern(hm(NODE_TYPE, YADS_ARRAY, ARGS, al(constNode("a"), constNode("b"))),
        //        new ResolveMap().resolve(YadsSimpleParser.parse("(a b)")));
        //
        //assertPattern(hm(NODE_TYPE, YADS_NAMED, ARGS, al(constNode("a"), constNode("b"))),
        //        new ResolveMap().resolve(YadsSimpleParser.parse("hello(a b)")));
        //
        //assertPattern(hm(NODE_TYPE, YADS_NAMED, NAMED_ARGS, hm("a", hm(NODE_TYPE, CONST, VALUE, "b")), ARGS, al(constNode("c"))),
        //        new ResolveMap().resolve(YadsSimpleParser.parse("hello(a : b c)")));
        //
        //assertPattern(hm(NODE_TYPE, YADS_NAMED, NAMED_ARGS, hm("a", hm(NODE_TYPE, CONST, VALUE, "b"))),
        //        new ResolveMap().resolve(YadsSimpleParser.parse("hello(a : b)")));

    }

}