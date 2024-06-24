package com.armedu;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTMLCommon {

    private Map<String, Object> attributes = new HashMap<>();
    private int tabOffset = 0;
    private String tab = "\11";
    private String lineEnd = "\12";
    private String comment = "";

    // Constructor
    public HTMLCommon(Map<String, String> attributes, int tabOffset) {
        setAttributes(attributes);
        setTabOffset(tabOffset);
    }

    // Returns the current API version
    public double apiVersion() {
        return 1.7;
    }

    // Returns the line end string
    public String getLineEnd() {
        return lineEnd;
    }

    // Returns the tab string
    public String getTab() {
        return tab;
    }

    // Returns the tabs string for indentation
    public String getTabs() {
        return getTab().repeat(tabOffset);
    }

    // Returns an HTML formatted attribute string
    public String getAttrString(String attr) {
        StringBuilder strAttr = new StringBuilder();
        if (attr != null) {
            for (Entry<String, Object> entry : attr.entrySet()) {
                strAttr.append(" ").append(entry.getKey()).append("=\"").append(htmlspecialchars(entry.getValue()))
                        .append("\"");
            }
        }
        return strAttr.toString();
    }

    // Parses attributes from a string or map
    public Map<String, Object> parseAttributes(Object attributes) {
        Map<String, Object> parsedAttributes = new HashMap<>();
        if (attributes instanceof Map) {
            ((Map<?, ?>) attributes).forEach((key, value) -> {
                if (key instanceof String && value instanceof String) {
                    parsedAttributes.put(((String) key).toLowerCase(), (String) value);
                }
            });
        } else if (attributes instanceof String) {
            String attrString = (String) attributes;
            Pattern pattern = Pattern.compile(
                    "(([A-Za-z_:]|[^\\x00-\\x7F])([A-Za-z0-9_:.-]|[^\\x00-\\x7F])*)\\s*(=\\s*\"([^\"]*)\"|'([^']*)'|([^\\s\"'>]+))?");
            Matcher matcher = pattern.matcher(attrString);
            while (matcher.find()) {
                String name = matcher.group(1).toLowerCase();
                String value = matcher.group(5) != null ? matcher.group(5)
                        : (matcher.group(6) != null ? matcher.group(6)
                                : (matcher.group(7) != null ? matcher.group(7) : name));
                parsedAttributes.put(name, value);
            }
        }
        return parsedAttributes;
    }

    // Checks if an attribute key exists
    private boolean getAttrKey(String attr, Map<String, String> attributes) {
        return attributes.containsKey(attr.toLowerCase());
    }

    // Updates attributes without changing other existing attributes
    protected void updateAttrArray(Map<String, Object> attributes2, Map<String, Object> map) {
        if (map != null) {
            attributes2.putAll(map);
        }
    }

    // Removes an attribute from the attributes map
    private void removeAttr(String attr, Map<String, Object> attributes2) {
        attributes2.remove(attr.toLowerCase());
    }

    // Returns the value of a specific attribute
    public String getAttribute(String attr) {
        return (String) attributes.get(attr.toLowerCase());
    }

    // Sets the value of a specific attribute
    public void setAttribute(String name, String value) {
        attributes.put(name.toLowerCase(), value != null ? value : name);
    }

    // Sets HTML attributes from a string or map
    public void setAttributes(Object attributes) {
        this.attributes = parseAttributes(attributes);
    }

    // Returns attributes as a string or map
    public Object getAttributes(boolean asString) {
        return asString ? getAttrString(attributes) : attributes;
    }

    // Updates the attributes
    public void updateAttributes(Object attributes) {
        updateAttrArray(this.attributes, parseAttributes(attributes));
    }

    // Removes a specific attribute
    public void removeAttribute(String attr) {
        removeAttr(attr, attributes);
    }

    // Sets the line end style
    public void setLineEnd(String style) {
        switch (style.toLowerCase()) {
            case "win":
                this.lineEnd = "\15\12";
                break;
            case "unix":
                this.lineEnd = "\12";
                break;
            case "mac":
                this.lineEnd = "\15";
                break;
            default:
                this.lineEnd = style;
        }
    }

    // Sets the tab offset
    public void setTabOffset(int offset) {
        this.tabOffset = offset;
    }

    // Returns the tab offset
    public int getTabOffset() {
        return tabOffset;
    }

    // Sets the tab string
    public void setTab(String string) {
        this.tab = string;
    }

    // Sets the HTML comment
    public void setComment(String comment) {
        this.comment = comment;
    }

    // Returns the HTML comment
    public String getComment() {
        return comment;
    }

    // Abstract method for returning the object's HTML
    public String toHtml() {
        return "";
    }

    // Displays the HTML to the screen
    public void display() {
        System.out.print(toHtml());
    }

    // Sets the charset for htmlspecialchars() function
    public static String charset(String newCharset) {
        return newCharset != null ? newCharset : "ISO-8859-1";
    }

    // Helper method for htmlspecialchars equivalent
    private String htmlspecialchars(Object object) {
        if (object == null) {
            return "";
        }
        return object.replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}