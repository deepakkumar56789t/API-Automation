package com.lcm.core.utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Predicate;
import com.google.gson.JsonObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonReader;

import org.junit.Assert;
import org.apache.commons.io.FileUtils;
import org.json.simple.parser.JSONParser;

import java.nio.charset.StandardCharsets;

import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.JSONObject;

public class JSONHelper
{
    public static JSONObject messageAsSimpleJson(final String messageFilePath) throws IOException, ParseException {
        return (JSONObject)parseFile(messageFilePath);
    }

    public static javax.json.JsonObject messageAsLongJson(final String messageFilePath) throws IOException, ParseException {
        File file = new File(messageFilePath);
        String message = FileUtils.readFileToString(file, "UTF-8");
        JsonReader jsonReader = Json.createReader(new StringReader(message));
        javax.json.JsonObject object = jsonReader.readObject();
        jsonReader.close();
        return object;
    }

    public static JSONArray messageAsSimpleJsonArray(final String messageFilePath) throws IOException, ParseException {
        return (JSONArray)parseFile(messageFilePath);
    }
    
    private static Object parseFile(final String messageFilePath) throws IOException, ParseException {
        try (final FileInputStream fileInputStream = new FileInputStream(messageFilePath);
             final InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8)) {
            return new JSONParser().parse(inputStreamReader);
        }
    }

    public static JSONObject parseJSONObject(final String message) throws ParseException {
        return (JSONObject)new JSONParser().parse(message);
    }

    public static javax.json.JsonObject parseLongJSONObject(final String message) throws ParseException {
        JsonReader jsonReader = Json.createReader(new StringReader(message));
        javax.json.JsonObject object = jsonReader.readObject();
        jsonReader.close();
        return object;
    }
    
    public static JSONArray parseJSONArray(final String message) throws ParseException {
        return (JSONArray)new JSONParser().parse(message);
    }
    
    public static JSONObject messageAsActualJson(final String messageFilePath) throws IOException, ParseException {
        final File file = new File(messageFilePath);
        final String messageFromLocalData = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        JSONParser parser = new JSONParser();
        return new JSONObject((JSONObject) parser.parse(messageFromLocalData));
    }

    public static void compareJSONMessage(final JSONObject expected, final JSONObject actual) {
        if (expected != null && actual != null) {
            final Set<?> expectedKes = (Set<?>)expected.keySet();
            for (final Object key : expectedKes) {
                Assert.assertEquals("JSON objects are not matched", expected.get((Object)key.toString()), actual.get((Object)key.toString()));
            }
        }
    }

    public static boolean isJSONValid(final String jsonInString) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(jsonInString);
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }
    
    public static String messageAsSimpleString(final String messageFilePath) throws IOException {
        return FileUtils.readFileToString(new File(messageFilePath), StandardCharsets.UTF_8);
    }
}
