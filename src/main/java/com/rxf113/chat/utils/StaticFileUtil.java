package com.rxf113.chat.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author rxf113
 */
public class StaticFileUtil {

    private StaticFileUtil() {
    }

    private static final String PAGE_BASE_DIR = "public";

    private static final Map<String, byte[]> STATIC_FILE_CACHE = new HashMap<>(4, 1f);

    public static byte[] getFileBytes(String fileName) throws Exception {
        return getFileBytes(fileName, PAGE_BASE_DIR);
    }

    private static byte[] getFileBytes(String fileName, String relativePath) throws Exception {
        byte[] bytes;
        if ((bytes = STATIC_FILE_CACHE.get(fileName)) == null) {
            synchronized (STATIC_FILE_CACHE) {
                if ((bytes = STATIC_FILE_CACHE.get(fileName)) == null) {
                    bytes = getBytes((relativePath) + fileName);
                    STATIC_FILE_CACHE.put(fileName, bytes);
                }
            }
        }
        return bytes;
    }

    private static byte[] getBytes(String completeFileName) throws Exception {
        byte[] resultBytes;
        try (InputStream inputStream = StaticFileUtil.class.getClassLoader().getResourceAsStream(completeFileName);
             ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream()) {
            byte[] bytes = new byte[1024];
            int len;
            while ((len = Objects.requireNonNull(inputStream).read(bytes)) > 0) {
                arrayOutputStream.write(bytes, 0, len);
            }
            resultBytes = arrayOutputStream.toByteArray();
        }
        return resultBytes;
    }
}