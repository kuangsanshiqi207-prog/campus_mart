package com.example.utils;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Http工具类
 */
@Slf4j
public class HttpClientUtil {

    private static final int TIMEOUT_MSEC = 5 * 1000;
    private static final String UTF_8 = "UTF-8";

    private HttpClientUtil() {
        // 工具类私有构造，防止实例化
    }

    /**
     * 发送GET方式请求
     */
    public static String doGet(String url, Map<String, String> paramMap) {
        String result = "";
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            URIBuilder builder = new URIBuilder(url);
            if (paramMap != null) {
                for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                    builder.addParameter(entry.getKey(), entry.getValue());
                }
            }
            URI uri = builder.build();
            HttpGet httpGet = new HttpGet(uri);

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                if (response != null && response.getStatusLine().getStatusCode() == 200) {
                    result = EntityUtils.toString(response.getEntity(), UTF_8);
                }
            }
        } catch (Exception e) {
            log.error("GET请求失败, url: {}", url, e);
        }
        return result;
    }

    /**
     * 发送POST方式请求
     */
    public static String doPost(String url, Map<String, String> paramMap) throws IOException {
        String resultString = "";
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);

            if (paramMap != null) {
                List<NameValuePair> paramList = new ArrayList<>();
                for (Map.Entry<String, String> param : paramMap.entrySet()) {
                    paramList.add(new BasicNameValuePair(param.getKey(), param.getValue()));
                }
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList, UTF_8);
                httpPost.setEntity(entity);
            }

            httpPost.setConfig(builderRequestConfig());

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                if (response != null) {
                    resultString = EntityUtils.toString(response.getEntity(), UTF_8);
                }
            }
        } catch (IOException e) {
            log.error("POST请求失败, url: {}", url, e);
            throw e;
        }
        return resultString;
    }

    /**
     * 发送POST方式请求（JSON格式）
     */
    public static String doPost4Json(String url, Map<String, String> paramMap) throws IOException {
        String resultString = "";
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);

            if (paramMap != null) {
                JSONObject jsonObject = new JSONObject();
                for (Map.Entry<String, String> param : paramMap.entrySet()) {
                    jsonObject.put(param.getKey(), param.getValue());
                }
                StringEntity entity = new StringEntity(jsonObject.toString(), UTF_8);
                entity.setContentEncoding(UTF_8);
                entity.setContentType("application/json");
                httpPost.setEntity(entity);
            }

            httpPost.setConfig(builderRequestConfig());

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                if (response != null) {
                    resultString = EntityUtils.toString(response.getEntity(), UTF_8);
                }
            }
        } catch (IOException e) {
            log.error("POST JSON请求失败, url: {}", url, e);
            throw e;
        }
        return resultString;
    }

    private static RequestConfig builderRequestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(TIMEOUT_MSEC)
                .setConnectionRequestTimeout(TIMEOUT_MSEC)
                .setSocketTimeout(TIMEOUT_MSEC).build();
    }
}