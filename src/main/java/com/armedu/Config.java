package com.armedu;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Config {

  public static String monetaryItem = "EUR";
  public static String owner = "www.eba.europa.eu";
  public static String prefixOwner = "fba";

  public static Map<String, String> lang = new HashMap<String, String>() {
    {
      put("0", "en");
      put("1", "bs-Latn-BA");
      put("2", "ba");
    }
  };

  public static Map<String, String> confiSet = new HashMap<String, String>() {
    {
      put("lab-codes", "lab-codes");
      put("rend", "rend");
      put("def", "def");
      put("pre", "pre");
      put("tab", "tab");

    }
  };
  public static Map<String, String> moduleSet = new HashMap<String, String>() {
    {
      put("pre", "pre");
      put("rend", "rend");
      put("lab-codes", "lab-codes");

    }
  };
  public static Map<String, String> createInstance = new HashMap<String, String>() {
    {
      put("def", "def");
      put("rend", "rend");
    }
  };

  public static String publicDir() {
    String storageDir = System.getProperty("user.dir");
    String fileDir = "app/public/tax/";
    File path = new File(storageDir, fileDir);

    return path.getAbsolutePath();
  }

  public static String setLogoPath() {

    File file = new File("public");
    if (!file.exists()) {
      boolean created = file.mkdir();
      if (!created) {
        System.out.println("Something went wrong");
      }
    }
    File logoFile = new File(file, "images" + File.separator + "logo.svg");
    System.out.println(logoFile.getPath());
    return logoFile.getAbsolutePath();
  }

  public static Map<String, OwnerInfo> owners() {
    Map<String, OwnerInfo> ownerMap = new HashMap<>();

    ownerMap.put("fba", new OwnerInfo(
        "http://www.fba.ba",
        "http://www.fba.ba/xbrl",
        "fba",
        "(C) FBA"));

    ownerMap.put("eba", new OwnerInfo(
        "http://www.eba.europa.eu/xbrl/crr",
        "http://www.eba.europa.eu/eu/fr/xbrl/crr",
        "eba",
        "(C) EBA"));

    ownerMap.put("audt", new OwnerInfo(
        "http://www.auditchain.finance/",
        "http://www.auditchain.finance/fr/dpm",
        "audt",
        "(C) Auditchain"));

    return ownerMap;
  }

  public static String tempPdfDir() {
    // give exact storage path
    String storageDir = System.getProperty("user.dir");

    File path = new File(storageDir, "logs" + File.separator);

    return path.getAbsolutePath();
  }

}
