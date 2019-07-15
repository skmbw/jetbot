package com.cetiti.jetbot;

import com.cetiti.iot.agent.http.service.HttpSmartThingService;
import com.cetiti.iot.agent.model.ResponseInfo;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * @author yinlei
 * @since 2019-7-8 14:54
 */
public class JetbotTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(JetbotTest.class);

    public static void main(String[] args) {
        try {
            HttpSmartThingService service = new HttpSmartThingService("http://10.0.20.89:8087");
            service.init("yinlei001", "yinlei1", null, null, (short) 0, null);
            // 暂时使用轮训
            for (;;) {
                String ttl = getSerialData();
                if (StringUtils.isBlank(ttl)) {
                    continue;
                }
                String[] ts = StringUtils.split(ttl, "#");
                LOGGER.info("get serial data=[{}] success.", ttl);
                byte[] data = ("{'v': '" + ttl + "', 's': " + ts[1] + ",'t': " + ts[0] + "}").getBytes();
                try {
                    ResponseInfo responseInfo = service.reportData("yinlei001", "yinlei1", data);
                    if (responseInfo != null) {
                        int code = responseInfo.getCode();
                        if (code == 1) {
                            LOGGER.info("report data success code=1。");
                        } else {
                            String msg = responseInfo.getMsg();
                            LOGGER.info("report data success. code=[{}].", msg, responseInfo.getCode());
                        }
                    } else {
                        LOGGER.info("response is null.");
                    }
                } catch (Exception e) {
                    LOGGER.error("report data error.", e);
                }
                // 1分钟上报一次
                TimeUnit.MINUTES.sleep(1);
            }
        } catch (Exception e) {
            LOGGER.error("main thread error.", e);
        }
    }

    private static String getSerialData() {
        String result = "";
        try {
            String url = "http://10.70.10.112:8000/serial/ttl1";
            byte[] bytes = invoke(url, null, "GET");
            if (bytes != null) {
                result = new String(bytes);
            }
        } catch (Exception e) {
            LOGGER.error("get serial data error, msg=[{}].", e);
        }
        return result;
    }

    private static byte[] invoke(String url, byte[] params, String httpMethod) {
        try {
            URL httpUrl = new URL(url);
            HttpURLConnection connection = getHttpConnection(httpUrl, httpMethod);
            connection.connect();
            if ("POST".equalsIgnoreCase(httpMethod) && params != null) {
                OutputStream os = connection.getOutputStream();
                os.write(params);
                os.flush();
            }

            return getBytes(connection);
        } catch (MalformedURLException e) {
            LOGGER.info("url format error, msg=[{}].", e.getMessage());
        } catch (IOException e) {
            LOGGER.info("http connection io error., msg=[{}].", e.getMessage());
        }
        return null;
    }

    private static byte[] getBytes(HttpURLConnection connection) throws IOException {
        int code = connection.getResponseCode();
        if (code == 200) {
            InputStream inputStream = connection.getInputStream();
            return IOUtils.toByteArray(inputStream);
        } else {
            LOGGER.info("invoke error, code=[{}]", code);
        }
        return null;
    }

    private static HttpURLConnection getHttpConnection(URL url, String name) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setConnectTimeout(5 * 1000);
            connection.setReadTimeout(5 * 1000);
            connection.setRequestMethod(name);
        } catch (Exception e) {
            LOGGER.info("build https url connection error, msg=[{}].", e.getMessage());
        }
        return connection;
    }
}
