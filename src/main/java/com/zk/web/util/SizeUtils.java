package com.zk.web.util;

import java.util.Objects;

/**
 * @author <a href="mailto:zhao___li@163.com">清汤白面<a/>
 * @description
 * @date 2021-12-20 15:11
 */
public class SizeUtils {

    private static final long SIZE = 1024;

    private static final long BYTES = SIZE;

    private static final long KILOGRAM_BYTES = BYTES * SIZE;

    private static final long MEGA_BYTES = KILOGRAM_BYTES * SIZE;

    private static final long GIGA_BYTES = MEGA_BYTES * SIZE;

    private static final long TERA_BYTES = GIGA_BYTES * SIZE;

    public static String convertBytes(byte[] data) {
        String view = "0B";
        if (Objects.isNull(data)) {
            return view;
        }
        double length = data.length;
        if (length < BYTES) {
            view = length / BYTES + "B";
        }
        if (length > BYTES && length < KILOGRAM_BYTES) {
            view = length / KILOGRAM_BYTES + "KB";
        }
        if (length > KILOGRAM_BYTES && length < MEGA_BYTES) {
            view = length / MEGA_BYTES + "MB";
        }
        if (length > MEGA_BYTES && length < GIGA_BYTES) {
            view = length / GIGA_BYTES + "GB";
        }
        if (length > GIGA_BYTES && length < TERA_BYTES) {
            view = length / TERA_BYTES + "TB";
        }
        return view;
    }

}
