package yk.lang.yads;

import yk.lang.yads.utils.BadException;
import yk.ycollections.YList;

import static yk.lang.yads.utils.BadException.die;
import static yk.ycollections.Tuple.tuple;
import static yk.ycollections.YArrayList.al;
import static yk.ycollections.YHashMap.hm;

/**
 * Deserializer that converts YadsCst back to YadsEntity, String, Number, Boolean
 */
public class YadsCstResolver {
    public static final String DELIMITER = "=";

    /**
     * Main entry point: converts a YadsCst node to appropriate data object
     */
    public static Object resolve(YadsCst node) {
        if (node == null) return null;
        
        switch (node.type) {
            case "LIST_BODY":
                return resolveKeyValues(node.children);
                
            case "NAMED_CLASS":
                return new YadsEntity(node.childByField.get("name").value.toString(),
                                      resolveKeyValues(node.childByField.get("body").children));
                
            case "UNNAMED_CLASS":
                YList<YadsCst> bodyChildren = node.childByField.get("body").children;
                // Special case: (=) means empty map - return YHashMap directly
                if (bodyChildren.size() == 1 && isDelimiter(bodyChildren.get(0))) {
                    return hm(); // Return empty YHashMap directly
                }
                return new YadsEntity(null, resolveKeyValues(bodyChildren));
                
            case "COMMENT_SINGLE_LINE":
                // Convert to YadsComment - remove "//" prefix
                return new YadsEntity.YadsComment(true, node.value.toString().substring(2));
                
            case "COMMENT_MULTI_LINE":
                // Convert to YadsComment - remove "/*" and "*/" 
                String multiLineText = node.value.toString();
                return new YadsEntity.YadsComment(false, multiLineText.substring(2, multiLineText.length() - 2));
                
            case "INTEGER_LITERAL":
            case "FLOATING_POINT_LITERAL":
            case "STRING_LITERAL_DQ":
            case "STRING_LITERAL_SQ":
            case "ANY_LITERAL":
            case "ANY_OPERATOR":
                // Return the parsed value directly
                return node.value;
                
            default:
                throw new BadException("Unknown YadsCst node type: " + node.type);
        }
    }

    /**
     * Converts a list of YadsCst nodes, handling the special case of 'a = b' -> Tuple conversion
     */
    public static YList<Object> resolveKeyValues(YList<YadsCst> nodes) {
        if (nodes == null) return al();
        
        Object left = null;
        YadsCst leftNode = null;
        YList<Object> result = al();
        
        for (int i = 0; i < nodes.size(); i++) {
            YadsCst node = nodes.get(i);
            
            if (isDelimiter(node)) {
                // Found '=' delimiter - convert previous element and next element to Tuple
                if (leftNode == null) die("Expected key before '=' at " + node.caret);
                if (isComment(leftNode)) die("Comment instead of key at " + leftNode.caret);

                i++; // Move to next element (the value)
                if (i >= nodes.size()) die("Expected value after '=' at " + node.caret);
                
                YadsCst rightNode = nodes.get(i);
                if (isComment(rightNode)) die("Comment instead of value at " + rightNode.caret);
                if (isDelimiter(rightNode)) die("Expected value at " + rightNode.caret);
                
                // Replace left element from result (it was the key)
                result.set(result.size() - 1, tuple(left, resolve(rightNode)));
                
                leftNode = null;
                left = null;
            } else {
                // Regular element - resolve and add to result
                leftNode = node;
                left = resolve(node);
                result.add(left);
            }
        }
        
        return result;
    }

    private static boolean isDelimiter(YadsCst node) {
        return "ANY_OPERATOR".equals(node.type) && DELIMITER.equals(node.value.toString());
    }

    private static boolean isComment(YadsCst node) {
        return "COMMENT_SINGLE_LINE".equals(node.type) || "COMMENT_MULTI_LINE".equals(node.type);
    }
}