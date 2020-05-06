package com.erp.call.web.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * http工具类
 *
 * @author sunkai
 * @date 2020/4/13
 */
@Component
public class HttpClientHelper {

    private static Logger logger = LoggerFactory.getLogger(HttpClientHelper.class);

    private static final int CONNECTION_REQUEST_TIMEOUT = 5000;
    private static final int CONNECT_TIMEOUT = 20000;
    private static final int SOCKET_TIMEOUT = 20000;
    private static final String CHAR_SET = "utf-8";
    private CloseableHttpClient httpClient;

    @PostConstruct
    private void init() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(200);
        cm.setDefaultMaxPerRoute(20);
        this.httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .build();
    }

    public <T> T postFile(String url, File file, Map<String, String> headers, Class<T> clazz) {
        try {
            String responseJson = executeFile(url, file, headers);
            return JSON.parseObject(responseJson, clazz);
        } catch (Exception e) {
            logger.error("httpClient post file error ", e);
        }
        return null;
    }

    public <T> T put(String url, Object dto, Map<String, String> headers, Class<T> clazz) {
        try {
            String responseJson = execute(url, JSON.toJSONString(dto), headers);
            return JSON.parseObject(responseJson, clazz);
        } catch (Exception e) {
            logger.error("httpClient put error ", e);
        }
        return null;
    }

    private String execute(String url, String jsonStr, Map<String, String> headers) throws IOException {
        HttpPut httpput = new HttpPut(url);
        //设置header
        httpput.setHeader("Content-type", "application/json");
        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpput.addHeader(entry.getKey(), entry.getValue());
            }
        }

        RequestConfig config = RequestConfig.custom()
                .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
                .setConnectTimeout(CONNECT_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT).build();
        httpput.setConfig(config);

        StringEntity se = new StringEntity(jsonStr, CHAR_SET);
        httpput.setEntity(se);

        HttpResponse response = null;
        try {
            response = httpClient.execute(httpput);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                return EntityUtils.toString(response.getEntity(), CHAR_SET);
            }
        } finally {
            if (null != response) {
                IOUtils.close(response.getEntity().getContent());
            }
        }
        logger.warn("request is failure,url:{},body:{},statusCode:{}", url, jsonStr, response.getStatusLine().getStatusCode());
        return null;
    }

    /**
     * 发送 http post 请求，支持文件上传
     */
    public String executeFile(String url, File file, Map<String, String> headers) throws IOException {
        HttpPost httpost = new HttpPost(url);
        //设置header
        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpost.addHeader(entry.getKey(), entry.getValue());
            }
        }

        RequestConfig config = RequestConfig.custom()
                .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
                .setConnectTimeout(CONNECT_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT).build();
        httpost.setConfig(config);

        MultipartEntityBuilder mEntityBuilder = MultipartEntityBuilder.create();
        mEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        mEntityBuilder.setCharset(Charset.forName(CHAR_SET));

        // 二进制参数
        mEntityBuilder.addBinaryBody("file", file);
        httpost.setEntity(mEntityBuilder.build());

        HttpResponse response = null;
        try {
            response = httpClient.execute(httpost);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return EntityUtils.toString(response.getEntity(), CHAR_SET);
            }
        } finally {
            if (null != response) {
                IOUtils.close(response.getEntity().getContent());
            }
        }
        logger.warn("request is failure,url:{},fileName:{},statusCode:{}", url, file.getName(), response.getStatusLine().getStatusCode());
        return null;
    }

    @PreDestroy
    public void destroy() {
        try {
            httpClient.close();
        } catch (IOException e) {
            logger.error("httpClient destroy error ", e);
        }
    }
}
