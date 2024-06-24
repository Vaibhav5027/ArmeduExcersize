package com.armedu;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DomToArray {

    public static Document invoke(String path) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            Document document = docBuilder.parse(new File(path));
            document.getDocumentElement().normalize();
            return document;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static Map<String, Object> getArray(String path) throws ParserConfigurationException, IOException {
        Document document = invoke(path);
        Element rootElement = document.getDocumentElement();
        Map<String, Object> output = (Map<String, Object>) domNodeToArray(rootElement);
        output.put("@root", rootElement.getNodeName());
        return output;
    }

    private static Object domNodeToArray(Node node) {
        Map<String, Object> output;
        switch (node.getNodeType()) {
            case Node.CDATA_SECTION_NODE:
            case Node.TEXT_NODE:
                output = new HashMap<>();
                output.put("@content", ((Text) node).getTextContent().trim());
                break;
            case Node.ELEMENT_NODE:
                output = new HashMap<>();
                Element element = (Element) node;
                NodeList children = element.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    Node child = children.item(i);
                    Object childValue = domNodeToArray(child);
                    if (child instanceof Element) {
                        String tagName = ((Element) child).getNodeName();
                        if (!output.containsKey(tagName)) {
                            output.put(tagName, new ArrayList<>());
                        }
                        ((List) output.get(tagName)).add(childValue);
                    } else if (childValue != null) {
                        output.put("@content", childValue);
                    }
                }
                NamedNodeMap attributes = element.getAttributes();
                if (attributes.getLength() > 0) {
                    Map<String, String> attributeMap = new HashMap<>();
                    for (int i = 0; i < attributes.getLength(); i++) {
                        Attr attr = (Attr) attributes.item(i);
                        attributeMap.put(attr.getName(), attr.getValue());
                    }
                    output.put("@attributes", attributeMap);
                }
                for (String key : output.keySet()) {
                    Object value = output.get(key);
                    if (value instanceof Map && ((Map) value).size() == 1 &&
                            !key.equals("@attributes")) {
                        output.put(key, ((Map) value).values().iterator().next());
                    }
                }
                break;
            default:
                output = null;
        }
        return output;
    }

    public static List<Map<String, Object>> searchMultDim(List<Map<String, Object>> arr, String field, Object value) {
        if (arr != null && !arr.isEmpty()) {
            List<Map<String, Object>> found = new ArrayList<>();
            for (Map<String, Object> row : arr) {
                if (row.containsKey(field) && row.get(field).equals(value)) {
                    found.add(row);
                }
            }
            return found;
        }
        return null;
    }

    public static String searchMultDimMultiVal(List<Map<String, Object>> arr, String value, String role) {
        if (arr == null || arr.isEmpty()) {
            return null;
        }

        List<Map<String, Object>> foundKey = arr.stream()
                .filter(element -> role.equals(element.get("role")))
                .collect(Collectors.toList());

        if (!foundKey.isEmpty()) {
            switch (role) {
                case "http://www.eba.europa.eu/xbrl/role/dpm-db-id":
                    List<Map<String, Object>> a = searchMultDim(foundKey, "from", value);
                    if (!a.isEmpty()) {
                        return (String) a.get(0).get("@content");
                    }
                    break;
            }
        }

        return null;
    }

    public static Map<Integer, List<String>> getPath(String path, List<String> strings, String returnVal)
            throws IOException {
        final List<String> extensions = Arrays.asList("xsd");
        final Map<Integer, List<String>> dir = new HashMap<>();

        Files.walkFileTree(Paths.get(path), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (attrs.isRegularFile()) {
                    String content = file.toString();
                    String extension = getFileExtension(content);

                    for (int i = 0; i < strings.size(); i++) {
                        String str = strings.get(i);
                        if (content.contains(str) && extensions.contains(extension)) {
                            if (returnVal == null) {
                                dir.computeIfAbsent(i, k -> new ArrayList<>()).add(content);
                            } else {
                                dir.clear();
                                dir.computeIfAbsent(i, k -> new ArrayList<>()).add(content);
                                return FileVisitResult.TERMINATE;
                            }
                        }
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });

        if (returnVal != null && !dir.isEmpty()) {
            return dir;
        }

        return dir;
    }

    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }

    public static String buildUrl(Map<String, String> parts) {
        String scheme = parts.containsKey("scheme") ? parts.get("scheme") + "://" : "";
        String host = Config.owner != null ? Config.owner : "";
        String port = parts.containsKey("port") ? ":" + parts.get("port") : "";
        String user = parts.getOrDefault("user", "");
        String path = parts.getOrDefault("path", "");
        String query = parts.containsKey("query") ? "?" + parts.get("query") : "";
        String fragment = parts.containsKey("fragment") ? "#" + parts.get("fragment") : "";

        return scheme + user + host + port + path + query + fragment;
    }

    public static int strposArr(String haystack, List<String> needle) {
        for (String str : needle) {
            int pos = str.indexOf(haystack);
            if (pos != -1) {
                return pos;
            }
        }
        return -1;
    }

    public static Map<String, Object> multidimensionalArrToSingle(Map<String, Object> array) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : array.entrySet()) {
            if (entry.getValue() instanceof Map) {
                result.putAll(multidimensionalArrToSingle((Map<String, Object>) entry.getValue()));
            } else {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
}
