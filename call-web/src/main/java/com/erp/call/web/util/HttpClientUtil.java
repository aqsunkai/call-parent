package com.erp.call.web.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.IOUtils;
import com.google.common.base.Joiner;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * <p>Class: HttpClientUtil</p>
 * <p>Description: http工具类</p>
 *
 * @author sunkai
 * @date 2020/2/28
 **/
public class HttpClientUtil {

    private static Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

    private static final int CONNECTION_REQUEST_TIMEOUT = 5000;
    private static final int CONNECT_TIMEOUT = 20000;
    private static final int SOCKET_TIMEOUT = 20000;
    private static final String CHAR_SET = "utf-8";
    private static final CloseableHttpClient httpClient;

    private HttpClientUtil() {
    }

    static {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(200);
        cm.setDefaultMaxPerRoute(20);
        httpClient = HttpClients.custom()
            .setConnectionManager(cm)
            .build();
    }

    /**
     * post请求
     *
     * @param url
     * @param jsonStr
     * @param connectTimeout
     * @param socketTimeout
     * @return
     * @throws IOException
     */
    public static String post(String url, String jsonStr, int connectTimeout, int socketTimeout) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "application/json; charset=" + CHAR_SET);

        RequestConfig config = RequestConfig.custom()
            .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
            .setConnectTimeout(connectTimeout)
            .setSocketTimeout(socketTimeout).build();

        httpPost.setConfig(config);
        StringEntity se = new StringEntity(jsonStr, CHAR_SET);
        httpPost.setEntity(se);
        HttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return EntityUtils.toString(response.getEntity(), CHAR_SET);
            } else {
                logger.warn("request is failure,url:{},statusCode:{},errorMsg:{}", url,
                    response.getStatusLine().getStatusCode(), EntityUtils.toString(response.getEntity(), CHAR_SET));
                throw new RuntimeException("request is failure,code:" + response.getStatusLine().getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            if (null != response) {
                IOUtils.close(response.getEntity().getContent());
            }
        }
    }

    public static <T> T post(String url, Object dto, Class<T> clazz) throws IOException {
        String responseJson = post(url, JSON.toJSONString(dto), CONNECT_TIMEOUT, SOCKET_TIMEOUT);
        return JSON.parseObject(responseJson, clazz);
    }

    public static <T> T get(String url, Map<String, String> params, Class<T> clazz) throws IOException {
        String responseJson = getWithString(url, params);
        return JSON.parseObject(responseJson, clazz);
    }

    /**
     * get 请求，返回HttpResponse
     *
     * @param url
     * @param params
     * @return
     * @throws IOException
     */
    public static HttpResponse getWithHttpResponse(String url, Map<String, String> params) throws IOException {
        if (null != params && !params.isEmpty()) {
            String paramStr = Joiner.on("&")
                .withKeyValueSeparator("=")
                .useForNull("")
                .join(params);
            url = url + "?" + paramStr;
        }
        return get(url, CONNECT_TIMEOUT, SOCKET_TIMEOUT);
    }


    /**
     * get 请求，返回字符串
     *
     * @param url
     * @param params
     * @return
     * @throws IOException
     */
    public static String getWithString(String url, Map<String, String> params) throws IOException {
        HttpResponse response = null;
        try {
            response = getWithHttpResponse(url, params);
            return EntityUtils.toString(response.getEntity(), CHAR_SET);
        } finally {
            if (response != null) {
                IOUtils.close(response.getEntity().getContent());
            }
        }
    }


    public static HttpResponse get(String url, int connectTimeout, int socketTimeout) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        RequestConfig config = RequestConfig.custom()
            .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
            .setConnectTimeout(connectTimeout)
            .setSocketTimeout(socketTimeout).build();
        httpGet.setConfig(config);
        HttpResponse response = httpClient.execute(httpGet);
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            logger.error("request is failure,url:{},statusCode:{},errorMsg:{}", url, response.getStatusLine().getStatusCode(), EntityUtils.toString(response.getEntity(), CHAR_SET));
            throw new RuntimeException("request is failure,code:" + response.getStatusLine().getStatusCode());
        }
        return response;
    }

    public static void destroy() {
        try {
            httpClient.close();
        } catch (IOException e) {
            logger.error("httpClient destroy error ", e);
        }
    }

//	public static void main(String[] args) throws IOException {
//		System.out.println(HttpClientUtil.getWithString("http://localhost:8080/api/ss", null));
//	}
}
