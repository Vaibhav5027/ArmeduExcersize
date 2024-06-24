package com.armedu;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HTMLCommon1 {
    /**
     * Associative array of attributes
     */
    private Map<String, String> _attributes = new HashMap<>();

    /**
     * Tab offset of the tag
     */
    private int _tabOffset = 0;

    /**
     * Tab string
     */
    private String _tab = "\t";

    /**
     * Contains the line end string
     */
    private String _lineEnd = "\n";

    /**
     * HTML comment on the object
     */
    private String _comment = "";

    /**
     * Class constructor
     * 
     * @param attributes Associative array of table tag attributes or HTML
     *                   attributes name="value" pairs
     * @param tabOffset  Indent offset in tabs
     * @return
     */
    public void HTMLCommon(Map<String, String> attributes, int tabOffset) {
        this.setAttributes(attributes);
        setTabOffset(tabOffset);
    }

    /**
     * Returns the current API version
     * 
     * @return double
     */
    public double apiVersion() {
        return 1.7;
    }

    /**
     * Returns the lineEnd
     * 
     * @return String
     */
    private String _getLineEnd() {
        return this._lineEnd;
    }

    /**
     * Returns a string containing the unit for indenting HTML
     * 
     * @return String
     */
    private String _getTab() {
        return this._tab;
    }

    /**
     * Returns a string containing the offset for the whole HTML code
     * 
     * @return String
     */
    private String _getTabs() {
        return String.join("", Collections.nCopies(this._tabOffset, this._getTab()));
    }

    /**
     * Returns an HTML formatted attribute string
     * 
     * @param attributes
     * @return String
     */
    private String _getAttrString(Map<String, String> attributes) {
        StringBuilder strAttr = new StringBuilder();

        if (attributes != null) {
            String charset = HTMLCommon.charset();
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                strAttr.append(" ").append(key).append("=\"").append(StringEscapeUtils.escapeHtml4(value)).append("\"");
            }
        }
        return strAttr.toString();
    }

    /**
     * Returns a valid atrributes array from either a string or array
     * 
     * @param attributes Either a typical HTML attribute string or an associative
     *                   array
     * @return Map<String, String>
     */
    private Map<String, String> _parseAttributes(String attributes) {
        Map<String, String> ret = new HashMap<>();

        if (attributes != null) {
            String[] attrs = attributes.split(" ");
            for (String attr : attrs) {
                String[] keyValue = attr.split("=");
                String key = keyValue[0];
                String value = keyValue[1].replaceAll("\"", "");
                ret.put(key, value);
            }
        }
        return ret;
    }

    /**
     * Returns the array key for the given non-name-value pair attribute
     * 
     * @param attr       Attribute
     * @param attributes Array of attribute
     * @return boolean
     */
    private boolean _getAttrKey(String attr, Map<String, String> attributes) {
        return attributes.containsKey(attr.toLowerCase());
    }

    /**
     * Updates the attributes in $attr1 with the values in $attr2 without changing
     * the other existing attributes
     * 
     * @param attr1 Original attributes array
     * @param attr2 New attributes array
     */
    private void _updateAttrArray(Map<String, String> attr1, Map<String, String> attr2) {
        if (attr2 != null) {
            attr1.putAll(attr2);
        }
    }

    /**
     * Removes the given attribute from the given array
     * 
     * @param attr       Attribute name
     * @param attributes Attribute array
     */
    private void _removeAttr(String attr, Map<String, String> attributes) {
        attributes.remove(attr.toLowerCase());
    }

    /**
     * Returns the value of the given attribute
     * 
     * @param attr Attribute name
     * @return String|null returns null if an attribute does not exist
     */
    public String getAttribute(String attr) {
        return this._attributes.get(attr.toLowerCase());
    }

    /**
     * Sets the value of the attribute
     * 
     * @param name  Attribute name
     * @param value Attribute value (will be set to $name if omitted)
     */
    public void setAttribute(String name, String value) {
        name = name.toLowerCase();
        if (value == null) {
            value = name;
        }
        this._attributes.put(name, value);
    }

    /**
     * Sets the HTML attributes
     * 
     * @param attributes Either a typical HTML attribute string or an associative
     *                   array
     */
    public void setAttributes(String attributes) {
        this._attributes = _parseAttributes(attributes);
    }

    /**
     * Returns the assoc array (default) or string of attributes
     * 
     * @param asString Whether to return the attributes as string
     * @return mixed attributes
     */
    public Object getAttributes(boolean asString) {
        if (asString) {
            return _getAttrString(this._attributes);
        } else {
            return this._attributes;
        }
    }

    /**
     * Updates the passed attributes without changing the other existing attributes
     * 
     * @param attributes Either a typical HTML attribute string or an associative
     *                   array
     */
    public void updateAttributes(String attributes) {
        _updateAttrArray(this._attributes, _parseAttributes(attributes));
    }

    /**
     * Removes an attribute
     * 
     * @param attr Attribute name
     */
    public void removeAttribute(String attr) {
        _removeAttr(attr, this._attributes);
    }

    /**
     * Sets the line end style to Windows, Mac, Unix or a custom string.
     * 
     * @param style "win", "mac", "unix" or custom string.
     */
    public void setLineEnd(String style) {
        switch (style) {
            case "win":
                this._lineEnd = "\r\n";
                break;
            case "unix":
                this._lineEnd = "\n";
                break;
            case "mac":
                this._lineEnd = "\r";
                break;
            default:
                this._lineEnd = style;
        }
    }

    /**
     * Sets the tab offset
     * 
     * @param offset
     */
    public void setTabOffset(int offset) {
        this._tabOffset = offset;
    }

    /**
     * Returns the tabOffset
     * 
     * @return int
     */
    public int getTabOffset() {
        return this._tabOffset;
    }

    /**
     * Sets the string used to indent HTML
     * 
     * @param string String used to indent ("\t", ' ', etc.).
     */
    public void setTab(String string) {
        this._tab = string;
    }

    /**
     * Sets the HTML comment to be displayed at the beginning of the HTML string
     * 
     * @param comment
     */
    public void setComment(String comment) {
        this._comment = comment;
    }

    /**
     * Returns the HTML comment
     * 
     * @return String
     */
    public String getComment() {
        return this._comment;
    }

    /**
     * Abstract method. Must be extended to return the objects HTML
     * 
     * @return String
     */
    public String toHtml() {
        return "";
    }

    /**
     * Displays the HTML to the screen
     */
    public void display() {
        System.out.println(toHtml());
    }

    /**
     * Sets the charset to use by htmlspecialchars() function
     * 
     * @param newCharset New charset to use. Omit if just getting the current value.
     * @return String Current charset
     */
    public static String charset(String newCharset) {
        String charset = "ISO-8859-1";

        if (newCharset != null) {
            charset = newCharset;
        }
        return charset;
    }
}
