package com.armedu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Unit test for simple App.
 */
public class AppTest {
    /**
     * Rigorous Test :-)
     */
    @Test
    void testInvoke_validFile() throws ParserConfigurationException, IOException {
        String filePath = "D:\\ArmeduPVTLTD\\student.xml";
        Document doc = DomToArray.invoke(filePath);
        assertNotNull(doc);

    }

    @Test
    void testSearchMultDim_found() {
        List<Map<String, Object>> arr = new ArrayList<>();
        Map<String, Object> map1 = new HashMap<>();
        map1.put("field", "value1");
        arr.add(map1);

        Map<String, Object> map2 = new HashMap<>();
        map2.put("field", "value2");
        arr.add(map2);

        List<Map<String, Object>> result = DomToArray.searchMultDim(arr, "field", "value1");
        assertEquals(1, result.size());
        assertEquals("value1", result.get(0).get("field"));
    }

    @Test
    void testSearchMultDimMultiVal_found() {
        List<Map<String, Object>> arr = new ArrayList<>();
        Map<String, Object> map1 = new HashMap<>();
        map1.put("role", "http://www.eba.europa.eu/xbrl/role/dpm-db-id");
        map1.put("from", "value1");
        arr.add(map1);

        String result = DomToArray.searchMultDimMultiVal(arr, "value1", "http://www.eba.europa.eu/xbrl/role/dpm-db-id");
        assertEquals("value1", result);
    }

    @Test
    void testGetPath_validPath() throws IOException {
        String rootDir = "D:\\ArmeduPVTLTD\\public";
        List<String> strings = List.of("searchString1", "searchString2");
        Map<Integer, List<String>> result = DomToArray.getPath(rootDir, strings, null);
        assertFalse(result.isEmpty());
        // Add assertions for specific expected results
    }

    @Test
    void testGetPath_returnVal() throws IOException {
        String rootDir = "D:\\ArmeduPVTLTD\\public";
        List<String> strings = List.of("searchString1", "searchString2");
        Map<Integer, List<String>> result = DomToArray.getPath(rootDir, strings, "expectedReturnValue");
        assertFalse(result.isEmpty());
        // Add assertions for specific expected results when returnVal is provided
    }
}
