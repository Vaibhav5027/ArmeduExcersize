package com.armedu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.event.ObjectChangeListener;

public class HTMLTableStorage extends HTMLCommon {

    // Automatically adds a new row or column if a given row or column index does
    // not exist
    private boolean autoGrow = true;

    // Array containing the table structure
    private List<List<Map<String, Object>>> structure = new ArrayList<>();
    // private List<Map<String, Object>> structure = new ArrayList<>();
    // Number of rows composing in the table
    private int rows = 0;

    // Number of columns composing the table
    private int cols = 0;

    // Tracks the level of nested tables
    private int nestLevel = 0;

    // Whether to use <thead>, <tfoot> and <tbody> or not
    private boolean useTGroups = false;

    // Automatically fills empty cells
    private String autoFill = "&nbsp;";

    /**
     * Class constructor
     *
     * @param tabOffset  the offset for the table
     * @param useTGroups whether to use <thead>, <tfoot> and <tbody> or not
     */
    public HTMLTableStorage(int tabOffset, boolean useTGroups) {
        super(null, tabOffset);
        this.useTGroups = useTGroups;
    }

    public HTMLTableStorage() {
        this(0, false);
    }

    public void setUseTGroups(boolean useTGroups) {
        this.useTGroups = useTGroups;
    }

    public boolean getUseTGroups() {
        return this.useTGroups;
    }

    public void setAutoFill(String fill) {
        this.autoFill = fill;
    }

    /**
     * Gets the autoFill value
     *
     * @return the autoFill value
     */
    public String getAutoFill() {
        return this.autoFill;
    }

    public void setAutoGrow(boolean grow) {
        this.autoGrow = grow;
    }

    // Getter method for autoGrow
    public boolean getAutoGrow() {
        return this.autoGrow;
    }

    // Setter method for rows
    public void setRowCount(int rows) {
        this.rows = rows;
    }

    // Setter method for cols
    public void setColCount(int cols) {
        this.cols = cols;
    }

    // Getter method for rows
    public int getRowCount() {
        return this.rows;
    }

    // Getter method for cols
    public int getColCount() {
        return this.cols;
    }

    public int getColCount(Integer row) {
        if (row != null) {
            int count = 0;
            for (Object cell : structure.get(row)) {
                if (cell instanceof Object[]) {
                    count++;
                }
            }
            return count;
        }
        return cols;
    }

    public void setRowType(int row, String type) {
        if (row < 0 || row >= structure.size()) {
            throw new IndexOutOfBoundsException("Row index out of bounds: " + row);
        }
        // Loop through each cell in the row and set the 'type' property
        for (int col = 0; col < structure.get(row).size(); col++) {
            structure.get(row).get(col).put("type", type);
        }
    }

    public void setCellAttributes(int row, int col, Map<String, Object> attributes) {
        if (row < 0 || row >= structure.size() || col < 0 || col >= structure.get(row).size()) {
            throw new IndexOutOfBoundsException("Row or column index out of bounds: " + row + ", " + col);
        }
        // Check if the cell is spanned and avoid setting attributes if so
        if (structure.get(row).get(col).get("type").equals("__SPANNED__")) {
            return;
        }
        // Parse attributes if necessary (assuming _parseAttributes is implemented
        // elsewhere)
        attributes = parseAttributes(attributes);
        // Perform any necessary adjustments based on _adjustEnds logic (assuming it's
        // implemented)
        Error error = _adjustEnds(row, col, "setCellAttributes", attributes);
        if (error != null) {
            return;
        }
        // Set the 'attr' key in the cell's Map with the parsed attributes
        structure.get(row).get(col).put("attr", attributes);
        // Call _updateSpanGrid if applicable (assuming it's implemented)
        _updateSpanGrid(row, col);
    }

    public void updateCellAttributes(int row, int col, Map<String, Object> attributes) {
        // Check if the structure and the specific cell are not null and not
        // "__SPANNED__"
        if (structure != null && structure.get(row) != null && structure.get(row).get(col) != null &&
                structure.get(row).get(col).get("type").equals("__SPANNED__")) {
            return;
        }

        // Parse the attributes
        attributes = parseAttributes(attributes);

        // Adjust the ends based on the new attributes
        Error err = _adjustEnds(row, col, "updateCellAttributes", attributes);
        if (err != null) {
            return;
        }

        // Update the cell attributes if the cell is not null
        if (structure.get(row).get(col) != null) {
            Map<String, Object> currentAttributes = (Map<String, Object>) structure.get(row).get(col).get("attr");
            if (currentAttributes != null) {
                currentAttributes.putAll(attributes);
            } else {
                structure.get(row).get(col).put("attr", attributes);
            }
        }

        // Update the span grid if applicable
        _updateSpanGrid(row, col);
    }

    public Map<String, Object> getCellAttributes(int row, int col) {
        if (structure != null && row < structure.size() && col < structure.get(row).size()) {
            Map<String, Object> cell = structure.get(row).get(col);
            if (cell != null && !cell.get("type").equals("__SPANNED__")) {
                return cell.containsKey("attr") ? (Map<String, Object>) cell.get("attr") : new HashMap<>();
            } else if (cell == null) {
                throw new IllegalArgumentException("Invalid table cell reference[" + row + "][" + col
                        + "] in HTMLTableStorage::getCellAttributes");
            }
        }
        return null;
    }

    public void setCellContents(int row, int col, Object contents, String type) {
        if (contents instanceof List) {
            for (Object singleContent : (List<?>) contents) {
                Error ret = _setSingleCellContents(row, col, singleContent, type);
                if (ret != null) {
                    throw new RuntimeException(ret.getMessage());
                }
                col++;
            }
        } else {
            Error ret = _setSingleCellContents(row, col, contents, type);
            if (ret != null) {
                throw new RuntimeException(ret.getMessage());
            }
        }
    }

    private Error _setSingleCellContents(int row, int col, Object contents, String type) {
        if (row >= structure.size() || col >= structure.get(row).size()) {
            if (autoGrow) {
                while (structure.size() <= row) {
                    structure.add(new ArrayList<>(Collections.nCopies(cols, new HashMap<>())));
                }
                while (structure.get(row).size() <= col) {
                    structure.get(row).add(new HashMap<>());
                }
            } else {
                return new Error("Invalid table cell reference[" + row + "][" + col
                        + "] in HTMLTableStorage::_setSingleCellContents");
            }
        }
        Map<String, Object> cell = structure.get(row).get(col);
        if (cell == null) {
            cell = new HashMap<>();
            structure.get(row).set(col, cell);
        }
        cell.put("contents", contents);
        cell.put("type", type);
        return null;
    }

    public Object getCellContents(int row, int col) {
        if (structure != null && row < structure.size() && col < structure.get(row).size()) {
            Map<String, Object> cell = structure.get(row).get(col);
            if (cell != null && "__SPANNED__".equals(cell.get("type"))) {
                return null;
            }
            if (cell == null) {
                throw new IllegalArgumentException(
                        "Invalid table cell reference[" + row + "][" + col + "] in HTMLTableStorage::getCellContents");
            }
            return cell.get("contents");
        }
        return null;
    }

    public void setHeaderContents(int row, int col, Object contents, Map<String, Object> attributes) {
        setCellContents(row, col, contents, "TH");
        if (attributes != null) {
            updateCellAttributes(row, col, attributes);
        }
    }

    public int addRow(List<Object> contents, Map<String, Object> attributes, String type, boolean inTR) {
        if (contents != null && !(contents instanceof List)) {
            throw new IllegalArgumentException("First parameter to HTMLTableStorage.addRow must be a List");
        }

        if (contents == null) {
            contents = new ArrayList<>();
        }

        type = type.toLowerCase();
        int row = rows++;
        for (int col = 0; col < contents.size(); col++) {
            Object content = contents.get(col);
            if ("td".equals(type)) {
                setCellContents(row, col, content);
            } else if ("th".equals(type)) {
                setHeaderContents(row, col, content, null);
            }
        }
        setRowAttributes(row, attributes, inTR);
        return row;
    }

    public void setCellContents(int row, int col, Object contents) {
        setCellContents(row, col, contents, "td");
    }

    public void setRowAttributes(int row, Map<String, Object> attributes, boolean inTR) {
        if (!inTR) {
            boolean multiAttr = isAttributesArray(attributes);
            for (int i = 0; i < cols; i++) {
                if (multiAttr) {
                    int index = i - ((int) Math.ceil((double) (i + 1) / attributes.size()) - 1) * attributes.size();
                    setCellAttributes(row, i, (Map<String, Object>) attributes.get(index));
                } else {
                    setCellAttributes(row, i, attributes);
                }
            }
        } else {
            attributes = parseAttributes(attributes);
            Error err = _adjustEnds(row, 0, "setRowAttributes", attributes);
            if (err != null) {
                throw new RuntimeException(err.getMessage());
            }
            if (structure.get(row) == null) {
                structure.set(row, new ArrayList<>());
            }
            structure.get(row).add(new HashMap<String, Object>() {
                {
                    put("attr", attributes);
                }
            });
        }
    }

    public void updateRowAttributes(int row, Object attributes, boolean inTR) {
        if (!inTR) {
            boolean multiAttr = isAttributesArray(attributes);
            for (int i = 0; i < this.cols; i++) {
                if (multiAttr) {
                    List<Map<String, Object>> attrList = (List<Map<String, Object>>) attributes;
                    int index = i - ((int) Math.ceil((double) (i + 1) / attrList.size()) - 1) * attrList.size();
                    updateCellAttributes(row, i, attrList.get(index));
                } else {
                    updateCellAttributes(row, i, (Map<String, Object>) attributes);
                }
            }
        } else {
            Map<String, Object> parsedAttributes = parseAttributes(attributes);
            Error err = _adjustEnds(row, 0, "updateRowAttributes", parsedAttributes);
            if (err != null) {
                return;
            }
            updateAttrArray(this.structure.get(row).get(0), parsedAttributes);
        }
    }

    public Map<String, Object> getRowAttributes(int row) {
        if (this.structure.get(row).get(0).containsKey("attr")) {
            return (Map<String, Object>) this.structure.get(row).get(0).get("attr");
        }
        return null;
    }

    public void altRowAttributes(int start, Object attributes1, Object attributes2, boolean inTR, int firstAttributes) {
        for (int row = start; row < this.rows; row++) {
            Object attributes;
            if ((row + start + (firstAttributes - 1)) % 2 == 0) {
                attributes = attributes1;
            } else {
                attributes = attributes2;
            }
            updateRowAttributes(row, attributes, inTR);
        }
    }

    public int addCol(List<Object> contents, Map<String, Object> attributes, String type) {
        if (contents != null && !(contents instanceof List)) {
            throw new IllegalArgumentException("First parameter to HTMLTable::addCol must be a List");
        }
        if (contents == null) {
            contents = List.of();
        }

        type = type.toLowerCase();
        int col = cols++;
        for (int row = 0; row < contents.size(); row++) {
            Object content = contents.get(row);
            if (type.equals("td")) {
                setCellContents(row, col, content);
            } else if (type.equals("th")) {
                setHeaderContents(row, col, content, null);
            }
        }
        setColAttributes(col, (List<Map<String, Object>>) attributes);
        return col;
    }

    public void setColAttributes(int col, List<Map<String, Object>> attributes) {
        boolean multiAttr = isAttributesArray(attributes);
        for (int i = 0; i < this.rows; i++) {
            if (multiAttr) {
                int index = i - ((int) Math.ceil((i + 1) / (double) attributes.size()) - 1) * attributes.size();
                setCellAttributes(i, col, attributes.get(index));
            } else {
                setCellAttributes(i, col, (Map<String, Object>) attributes);
            }
        }
    }

    public String toHtml(String tabs, String tab) {
        StringBuilder strHtml = new StringBuilder();
        String extraTab = "";
        if (tabs == null) {
            tabs = getTabs(); // Assuming you have a separate method for getting tabs
        }
        if (tab == null) {
            tab = getTab(); // Assuming you have a separate method for getting tab
        }
        String lineEnd = getLineEnd(); // Assuming you have a separate method for getting line ending

        if (useTGroups) {
            extraTab = tab;
        } else {
            extraTab = "";
        }

        if (this.cols > 0) {
            for (int i = 0; i < this.rows; i++) {
                String attr = "";
                if (this.structure != null && this.structure.get(i) != null && this.structure.get(i).contains("attr")) {
                    attr = getAttrString(this.structure.get(i).get("attr"));
                }
                strHtml.append(tabs).append(tab).append(extraTab).append("<tr").append(attr).append(">")
                        .append(lineEnd);

                for (int j = 0; j < this.cols; j++) {
                    String cellAttr = "";
                    String contents = "";
                    String type = "td";

                    if (this.structure.get(i).get(j) != null && this.structure.get(i) != null
                            && this.structure.get(i).get(j).equals("__SPANNED__")
                            &&
                            this.structure != null) {
                        continue;
                    }

                    if (this.structure != null && this.structure.get(i) != null && this.structure.get(i).get(j) != null
                            &&
                            this.structure.get(i).get(j).containsKey("type")) {
                        type = (this.structure.get(i).get(j).get("type").equals("th") ? "th" : "td");
                    }

                    if (this.structure != null && this.structure.get(i) != null && this.structure.get(i).get(j) != null
                            &&
                            this.structure.get(i).get(j).containsKey("attr")) {
                        cellAttr = (String) this.structure.get(i).get(j).get("attr");
                    }

                    if (this.structure != null && this.structure.get(i) != null && this.structure.get(i).get(j) != null
                            &&
                            this.structure.get(i).get(j).containsKey("contents")) {
                        contents = (String) this.structure.get(i).get(j).get("contents");
                    }

                    strHtml.append(tabs).append(tab).append(tab).append(extraTab).append("<").append(type)
                            .append(getAttrString(cellAttr)).append(">");

                    if (contents instanceof HTMLCommon) {
                        ((HTMLCommon) contents).setTab(tab + extraTab);
                        ((HTMLCommon) contents).setTabOffset(this.tabOffset + 3);
                        ((HTMLCommon) contents)._nestLevel = this._nestLevel + 1;
                        ((HTMLCommon) contents).setLineEnd(lineEnd);
                    } else if (contents instanceof Object && methods.exists(contents, "toHtml")) {
                        contents = ((Object) contents).toString(); // Assuming toHtml returns a String
                    } else if (contents instanceof Object && methods.exists(contents, "toString")) {
                        contents = ((Object) contents).toString();
                    } else if (contents instanceof String[]) {
                        contents = String.join(", ", (String[]) contents);
                    }

                    if (this.autoFill != null && contents.isEmpty()) {
                        contents = this.autoFill;
                    }

                    strHtml.append(contents).append("</").append(type).append(">").append(lineEnd);
                }
                strHtml.append(tabs).append(tab).append(extraTab).append("</tr>").append(lineEnd);
            }
        }
        return strHtml.toString();
    }

    private void _updateSpanGrid(int row, int col) {
        // Check for colspan attribute
        if (structure.get(row).get(col).containsKey("attr")) {
            Map<String, Object> attributes = (Map<String, Object>) structure.get(row).get(col).get("attr");
            if (attributes.containsKey("colspan")) {
                int colspan = (int) attributes.get("colspan");
                for (int j = col + 1; j < structure.get(row).size() && j <= col + colspan - 1; j++) {
                    structure.get(row).set(j, Collections.singletonMap("type", "__SPANNED__"));
                }
            }
            // Check for rowspan attribute
            if (attributes.containsKey("rowspan")) {
                int rowspan = (int) attributes.get("rowspan");
                for (int i = row + 1; i < structure.size() && i <= row + rowspan - 1; i++) {
                    structure.get(i).set(col, Collections.singletonMap("type", "__SPANNED__"));
                }
            }
        }
    }

    private Error _adjustEnds(int row, int col, String method, Map<String, Object> attributes) {
        int colspan = (int) attributes.getOrDefault("colspan", 1);
        int rowspan = (int) attributes.getOrDefault("rowspan", 1);
        if (row + rowspan - 1 >= this.structure.size()) {
            if (this.autoGrow) {
                // Adjust structure size if autoGrow is enabled
                while (this.structure.size() < row + rowspan) {
                    this.structure.add((List<Map<String, Object>>) Collections.emptyMap());
                }
            } else {
                return new Error("Invalid table row reference[" + row + "] in HTML_Table::" + method);
            }
        }
        if (col + colspan - 1 >= structure.get(row).size()) {
            if (this.autoGrow) {
                // Adjust inner list size if autoGrow is enabled
                int requiredSize = col + colspan;
                for (int i = structure.get(row).size(); i < requiredSize; i++) {
                    structure.get(row).add(Collections.singletonMap("type", ""));
                }
            } else {
                return new Error("Invalid table column reference[" + col + "] in HTML_Table::" + method);
            }
        }
        return null;
    }

    private boolean isAttributesArray(Object attributes) {
        if (attributes instanceof List && !((List<?>) attributes).isEmpty()) {
            Object firstElement = ((List<?>) attributes).get(0);
            if (firstElement instanceof Map || (firstElement instanceof String && ((List<?>) attributes).size() > 1)) {
                return true;
            }
        }
        return false;
    }
}
