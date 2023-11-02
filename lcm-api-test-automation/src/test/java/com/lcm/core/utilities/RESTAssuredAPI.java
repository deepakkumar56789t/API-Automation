package com.lcm.core.utilities;

import com.lcm.core.exceptions.HttpRequestException;
import io.restassured.RestAssured;
import io.restassured.config.DecoderConfig;
import io.restassured.config.EncoderConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import net.serenitybdd.rest.SerenityRest;
import org.apache.tika.Tika;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class RESTAssuredAPI
{
    private static final Logger log;
    public Response globalResponse;
    public static Response globalStaticResponse;
    private final LCMProperties lcmProperties = new LCMProperties("serenity.properties");

    public Response get(final String endPoint, final Map<String, ?> queryParam) {
        final Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return this.get(endPoint, headers, queryParam, null);
    }

    public Response get(final String endPoint, final Map<String, String> headersAsMap, final Map<String, ?> queryParam) {
        return this.get(endPoint, headersAsMap, queryParam, null);
    }

    public Response get(final Map<String, String> headers, final String endPoint) {
        return this.get(endPoint, headers, null, null);
    }

    public Response get(final Map<String, String> headers, final String endPoint, final boolean allowRedirects) {
        final RequestSpecification httpRequest = this.getRequestSpecification(endPoint, allowRedirects);
        if (headers != null) {
            httpRequest.headers(headers);
            if (!headers.containsKey("Accept-Encoding")) {
                httpRequest.config(this.getRestAssuredConfig().decoderConfig(DecoderConfig.decoderConfig().noContentDecoders()));
            }
        }
        final Response response = httpRequest.get(endPoint);
        return this.globalResponse = (RESTAssuredAPI.globalStaticResponse = response);
    }

    public Response get(final String endPoint, final boolean allowRedirects, final Map<String, String> headers) {
        final RequestSpecification httpRequest = this.getRequestSpecification(endPoint, allowRedirects);
        if (headers != null) {
            httpRequest.headers(headers);
        }
        final Response response = httpRequest.get(endPoint);
        return this.globalResponse = (RESTAssuredAPI.globalStaticResponse = response);
    }

    public Response get(final String endPoint) {
        return this.get(endPoint, "");
    }

    public Response get(String endPoint, final String param) {
        final Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        if (param != null && !param.equals("")) {
            endPoint = (endPoint.endsWith("/") ? (endPoint + param) : (endPoint + "/" + param));
        }
        return this.get(endPoint, headers, null, null);
    }

    public Response post(final String messageFilePath, final String endPoint) {
        try {
            final JSONParser parser = new JSONParser();
            final JSONObject jsonObj = (JSONObject)parser.parse(new FileReader(messageFilePath));
            return this.post(jsonObj, endPoint);
        }
        catch (IOException | ParseException ex3) {
            RESTAssuredAPI.log.severe(ex3.getMessage());
            throw new HttpRequestException(ex3.getMessage());
        }
    }

    public Response postMap(final Map<String, String> messageAsMap, final String endPoint) {
        return this.post(new JSONObject(messageAsMap), endPoint);
    }

    public Response post(final Object jsonObj, final String endPoint) {
        final Map<String, String> map = new HashMap<String, String>();
        map.put("Content-Type", "application/json");
        return this.post(endPoint, false, (Map<String, String>) jsonObj, map, null, null);
    }

    public void post(final Object requestBodyJSON, final Map<String, String> headers, final String endPoint) {
        this.post(endPoint, false, (Map<String, String>) requestBodyJSON, headers, null, null);
    }

    public void post(final String endPoint, final Object requestBodyJSON, final Map<String, String> headers, final String urlParams, final Map<String, String> queryParams) {
        this.post(endPoint, false, (Map<String, String>) requestBodyJSON, headers, urlParams, queryParams);
    }

    public void put(final Object requestBodyJSON, final Map<String, String> headers, final String endPoint) {
        this.put(endPoint, false, (Map<String, String>) requestBodyJSON, headers, null, null);
    }

    public Response put(final String endPoint, final Object currentMessage) {
        return this.put(endPoint, currentMessage.toString());
    }

    public Response put(final String endPoint, final String currentMessage) {
        final Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        return this.put(endPoint, currentMessage, headers);
    }

    public Response delete(final String endPoint, final Map<String, String> headers) {
        return this.delete(endPoint, headers, null, null, null);
    }

    public void delete(final String endPoint, final Map<String, String> headers, final String urlParam) {
        this.delete(endPoint, headers, urlParam, null, null);
    }

    public Response delete(final String endPoint) {
        return this.delete(endPoint, null, null, null, null);
    }

    public Response get(String endPoint, final Map<String, String> headers, final Map<String, ?> queryParam, final String urlParam) {
        if (urlParam != null) {
            endPoint = endPoint + "/" + urlParam;
        }

        final RequestSpecification httpRequest = this.getRequestSpecification(endPoint);
        if (headers != null) {
            httpRequest.headers(headers);
        }
        if (queryParam != null) {
            httpRequest.params(queryParam);
        }
        final Response response = httpRequest.get(endPoint);
        this.globalResponse = response;
        RESTAssuredAPI.globalStaticResponse = this.globalResponse;
        return response;
    }

    public void put(String endPoint, final Map<String, ?> requestBody, final Map<String, String> headers, final String urlParam) {
        this.put(endPoint, requestBody, headers, urlParam, null);
    }

    public Response put(String endPoint, final Map<String, ?> requestBody, final Map<String, String> headers, final String urlParam, final Map<String, ?> queryParam) {
        if (urlParam != null) {
            endPoint = endPoint + "/" + urlParam;
        }

        final RequestSpecification httpRequest = this.getRequestSpecification(endPoint);
        if (headers != null) {
            httpRequest.headers(headers);
        }

        if (queryParam != null) {
            httpRequest.params(queryParam);
        }

        httpRequest.header("Content-Type", "application/json");
        httpRequest.body(requestBody);

        final Response response = httpRequest.put(endPoint);
        this.globalResponse = response;
        RESTAssuredAPI.globalStaticResponse = this.globalResponse;
        return response;
    }

    public Response post(final File file, final String endPoint, final Map<String, String> headers) throws IOException {
        final RequestSpecification httpRequest = this.getRequestSpecification(endPoint);
        if (headers != null) {
            httpRequest.headers(headers);
        }
        httpRequest.multiPart("file", file, new Tika().detect(file));
        final Response response = httpRequest.post(endPoint);
        this.globalResponse = response;
        RESTAssuredAPI.globalStaticResponse = this.globalResponse;
        return response;
    }

    public Response post(final String endPoint, final Map<String, String> headers, final File file) {
        final RequestSpecification httpRequest = this.getRequestSpecification(endPoint);
        if (headers != null) {
            httpRequest.headers(headers);
        }
        httpRequest.body(file);
        final Response response = httpRequest.post(endPoint);
        this.globalResponse = response;
        RESTAssuredAPI.globalStaticResponse = this.globalResponse;
        return response;
    }

    public void post(final String endPoint, final Map<String, ?> queryParam) {
        final RequestSpecification httpRequest = this.getRequestSpecification(endPoint);
        if (queryParam != null) {
            httpRequest.params(queryParam);
        }

        final Response response = httpRequest.post(endPoint);
        this.globalResponse = response;
        RESTAssuredAPI.globalStaticResponse = this.globalResponse;
    }

    public Response post(String endPoint, final boolean urlencodedForm, final Map<String, StringBuilder> requestBodyAsMap, final Map<String, String> headers, String encryptedData) {
        final RequestSpecification httpRequest = this.getRequestSpecification(endPoint);
        httpRequest.config(this.getRestAssuredConfig().encoderConfig(EncoderConfig.encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false)));
        if (urlencodedForm) {
            httpRequest.config(this.getRestAssuredConfig().encoderConfig(EncoderConfig.encoderConfig().encodeContentTypeAs("x-www-form-urlencoded", ContentType.URLENC)));
        }
        if (endPoint.contains("https")) {
            httpRequest.relaxedHTTPSValidation();
            httpRequest.port(443);
        }
        if (headers != null) {
            httpRequest.headers(headers);
        }
        if (requestBodyAsMap != null) {
            requestBodyAsMap.put("encryptedData", new StringBuilder(encryptedData));
            httpRequest.body(requestBodyAsMap);
        }
        final Response response = httpRequest.post(endPoint);
        this.globalResponse = response;
        RESTAssuredAPI.globalStaticResponse = this.globalResponse;
        return response;
    }

    public Response post(String endPoint, final boolean urlencodedForm, final Map<String, String> requestBodyAsMap, final Map<String, String> headers, final String urlParams, final Map<String, ?> queryParam) {
        if (urlParams != null) {
            endPoint = endPoint + "/" + urlParams;
        }

        final RequestSpecification httpRequest = this.getRequestSpecification(endPoint);
        httpRequest.config(this.getRestAssuredConfig().encoderConfig(EncoderConfig.encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false)));
        if (urlencodedForm) {
            httpRequest.config(this.getRestAssuredConfig().encoderConfig(EncoderConfig.encoderConfig().encodeContentTypeAs("x-www-form-urlencoded", ContentType.URLENC)));
        }
        if (endPoint.contains("https")) {
            httpRequest.relaxedHTTPSValidation();
            httpRequest.port(443);
        }
        if (headers != null) {
            httpRequest.headers(headers);
        }
        if (requestBodyAsMap != null) {
            httpRequest.body(requestBodyAsMap);
        }
        if (queryParam != null) {
            httpRequest.queryParams(queryParam);
        }

        final Response response = httpRequest.post(endPoint);
        this.globalResponse = response;
        RESTAssuredAPI.globalStaticResponse = this.globalResponse;
        return response;
    }

    public Response post(String endPoint, final Map<String, String> headers, final Map<String, ?> queryParam,final String username, final String password) {
        final RequestSpecification httpRequest = this.getRequestSpecification().auth().preemptive().basic(username, password);
        if (endPoint.contains("https")) {
            httpRequest.relaxedHTTPSValidation();
            httpRequest.port(443);
        }
        if (headers != null) {
            httpRequest.headers(headers);
        }
        if (queryParam != null) {
            httpRequest.queryParams(queryParam);
        }

        final Response response = httpRequest.post(endPoint);
        this.globalResponse = response;
        RESTAssuredAPI.globalStaticResponse = this.globalResponse;
        return response;
    }

    public Response put(String endPoint, final boolean urlencodedForm, final Map<String, String> requestBodyAsMap, final Map<String, String> headers, final String urlParams, final Map<String, ?> queryParam) {
        if (urlParams != null) {
            endPoint = endPoint + "/" + urlParams;
        }

        final RequestSpecification httpRequest = this.getRequestSpecification(endPoint);
        httpRequest.config(this.getRestAssuredConfig().encoderConfig(EncoderConfig.encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false)));
        if (urlencodedForm) {
            httpRequest.config(this.getRestAssuredConfig().encoderConfig(EncoderConfig.encoderConfig().encodeContentTypeAs("x-www-form-urlencoded", ContentType.URLENC)));
        }
        if (endPoint.contains("https")) {
            httpRequest.relaxedHTTPSValidation();
            httpRequest.port(443);
        }
        if (headers != null) {
            httpRequest.headers(headers);
        }
        if (requestBodyAsMap != null) {
            httpRequest.body(requestBodyAsMap);
        }
        if (queryParam != null) {
            httpRequest.queryParams(queryParam);
        }

        final Response response = httpRequest.put(endPoint);
        this.globalResponse = response;
        RESTAssuredAPI.globalStaticResponse = this.globalResponse;
        return response;
    }

    public Response put(final String endPoint, final Object currentMessage, final Map<String, String> headers) {
        final RequestSpecification httpRequest = this.getRequestSpecification(endPoint).body(currentMessage.toString());
        if (headers != null) {
            httpRequest.headers(headers);
        }
        final Response response = httpRequest.when().put(endPoint);
        this.globalResponse = response;
        RESTAssuredAPI.globalStaticResponse = this.globalResponse;
        return response;
    }

    public Response options(final String endPoint, final Map<String, String> headers) {
        final RequestSpecification httpRequest = this.getRequestSpecification(endPoint);
        if (headers != null) {
            httpRequest.headers(headers);
        }
        final Response response = httpRequest.options(endPoint);
        this.globalResponse = response;
        RESTAssuredAPI.globalStaticResponse = this.globalResponse;
        return response;
    }

    public Response head(final Map<String, String> headers, final String endPoint) {
        return this.head(endPoint, headers, null, null);
    }

    public Response head(String endPoint, final Map<String, String> headers, final Map<String, ?> queryParam, final String urlParam) {
        Response response;
        if (urlParam != null) {
            endPoint = endPoint + "/" + urlParam;
        }
        final RequestSpecification httpRequest = this.getRequestSpecification(endPoint);
        if (headers != null) {
            httpRequest.headers(headers);
            if (!headers.containsKey("Accept-Encoding")) {
                httpRequest.config(this.getRestAssuredConfig().decoderConfig(DecoderConfig.decoderConfig().noContentDecoders()));
            }
        }
        if (queryParam != null) {
            httpRequest.params(queryParam);
        }
        response = httpRequest.head(endPoint);
        this.globalResponse = response;
        RESTAssuredAPI.globalStaticResponse = this.globalResponse;
        return response;
    }

    public Response delete(String endPoint, final Map<String, String> headers, final String urlParam, final Map<String, ?> queryParam, final Object requestBody) {
        if (urlParam != null) {
            endPoint = endPoint + "/" + urlParam;
        }

        RequestSpecification httpRequest = this.getRequestSpecification(endPoint);
        if (requestBody != null) {
            httpRequest = this.getRequestSpecification(endPoint).body(requestBody.toString());
        }
        else {
            httpRequest.body("[]");
        }
        if (headers != null) {
            httpRequest.headers(headers);
        }
        if (queryParam != null) {
            httpRequest.params(queryParam);
        }
        final Response response = httpRequest.delete(endPoint);
        this.globalResponse = response;
        RESTAssuredAPI.globalStaticResponse = this.globalResponse;
        return response;
    }

    public static void verifyResponse(JSONObject keyValues) throws ParseException {
        final JSONObject responseObject = JSONHelper.parseJSONObject(RESTAssuredAPI.globalStaticResponse.getBody().asString());
        Assert.assertEquals("Unexpected response payload is returned", keyValues, responseObject);
    }

    public static void verifyResponseFields(JSONObject keyOnly) throws ParseException {
        final JSONObject responseObject = JSONHelper.parseJSONObject(RESTAssuredAPI.globalStaticResponse.getBody().asString());

        Assert.assertTrue("Unexpected response payload is returned", recursiveCompare(keyOnly, responseObject));
    }

    public static boolean recursiveCompare(JSONObject jsonObj1, JSONObject jsonObj2) {
        for (Object key1 : jsonObj1.keySet()) {

            String keyStr1 = (String)key1;
            if (!jsonObj2.containsKey(keyStr1))
                return false;

            Object keyvalue1 = jsonObj1.get(keyStr1);
            Object keyvalue2 = jsonObj2.get(keyStr1);

            //for nested objects iteration if required
            if (keyvalue1 instanceof JSONArray array1) {
                for(int idx =0; idx < array1.size(); idx++){
                    recursiveCompare((JSONObject) ((JSONArray) keyvalue1).get(idx), (JSONObject) ((JSONArray) keyvalue2).get(idx));
                }
            }
            else if (keyvalue1 instanceof JSONObject)
                recursiveCompare((JSONObject) keyvalue1, (JSONObject)keyvalue2);
        }

        return true;
    }

    private RequestSpecification getRequestSpecification(final String endPoint) {
        lcmProperties.loadSystemProperties();
        RequestSpecification httpRequest;
        if (lcmProperties.getProperty("serenity.http.reporting").equalsIgnoreCase("true")) {
            httpRequest = SerenityRest.given();
        }
        else {
            httpRequest = RestAssured.given();
        }
        if (endPoint.contains("https")) {
            httpRequest = httpRequest.relaxedHTTPSValidation();
            httpRequest.port(443);
        }
        return httpRequest;
    }

    private RequestSpecification getRequestSpecification(final String endPoint, final boolean followRedirects) {
        lcmProperties.loadSystemProperties();
        RequestSpecification httpRequest;
        if (lcmProperties.getProperty("serenity.http.reporting").equalsIgnoreCase("true")) {
            httpRequest = SerenityRest.given();
        }
        else {
            httpRequest = RestAssured.given();
        }
        if (endPoint.contains("https")) {
            httpRequest = httpRequest.relaxedHTTPSValidation();
            httpRequest.port(443);
        }
        return httpRequest.redirects().follow(followRedirects).param("http.protocol.handle-redirects", followRedirects);
    }

    private RequestSpecification getRequestSpecification() {
        lcmProperties.loadSystemProperties();
        RequestSpecification httpRequest;
        if (lcmProperties.getProperty("serenity.http.reporting").equalsIgnoreCase("true")) {
            httpRequest = SerenityRest.given();
        }
        else {
            httpRequest = RestAssured.given();
        }
        return httpRequest;
    }

    private RestAssuredConfig getRestAssuredConfig() {
        lcmProperties.loadSystemProperties();
        RestAssuredConfig config;
        if (lcmProperties.getProperty("serenity.http.reporting").equalsIgnoreCase("true")) {
            config = SerenityRest.config();
        }
        else {
            config = RestAssured.config();
        }
        return config;
    }

    static {
        log = Logger.getLogger(RESTAssuredAPI.class.getName());
    }
}