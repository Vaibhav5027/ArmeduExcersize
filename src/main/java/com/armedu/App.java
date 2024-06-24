package com.armedu;

import java.io.IOException;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) throws ParserConfigurationException, IOException {
        System.out.println("Hello World!");
        Map<String, Object> obj = DomToArray.getArray("D:/ArmeduPVTLTD/student.xml");
        System.out.println(obj);
    }
}
