package com.mathbeta.models.utils;

/**
 * Created by xiuyou.xu on 2017/8/14.
 */
public class NameUtil {
    public static String getEntityName(String name, boolean hasPrefix, String tableNamePrefix) {
        if (hasPrefix && name.startsWith(tableNamePrefix)) {
            name = name.substring(tableNamePrefix.length());
        }
        String[] names = name.split("_");
        StringBuilder sb = new StringBuilder();
        if (names != null && names.length > 0) {
            for (int i = 0; i < names.length; i++) {
                sb.append(camel(names[i]));
            }
        }
        return sb.toString();
    }

    public static String getFieldName(String name, boolean hasPrefix, String tableNamePrefix) {
        if (hasPrefix && name.startsWith(tableNamePrefix)) {
            name = name.substring(tableNamePrefix.length());
        }
        String[] names = name.split("_");
        StringBuilder sb = new StringBuilder();
        if (names != null && names.length > 0) {
            sb.append(names[0]);
            for (int i = 1; i < names.length; i++) {
                sb.append(camel(names[i]));
            }
        }
        return sb.toString();
    }

    public static String camel(String name) {
        return String.valueOf(name.charAt(0)).toUpperCase() + name.substring(1);
    }
}
