package com.lawrence.fatalis.util.encrypt;

import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;
import java.util.Random;

/**
 * md5盐值加密
 */
public class MD5Coder {

    /**
     * 生成含有随机盐值和自定义盐值的三重md5加密串
     */
    public static String generate(String password, String... salt) {
        Random r = new Random();
        StringBuilder sb = new StringBuilder(16);
        sb.append(r.nextInt(99999999)).append(r.nextInt(99999999));
        int len = sb.length();
        if (len < 16) {
            for (int i = 0; i < 16 - len; i++) {
                sb.append("0");
            }
        }
        String randomsalt = sb.toString();

        String self;
        if (salt.length > 0) {
            self = salt[0];
            password = md5Hex(md5Hex(md5Hex(password).toUpperCase() + randomsalt) + self);
        } else {
            password = md5Hex(md5Hex(password).toUpperCase() + randomsalt);
        }

        char[] cs = new char[48];
        for (int i = 0; i < 48; i += 3) {
            cs[i] = password.charAt(i / 3 * 2);
            char c = randomsalt.charAt(i / 3);
            cs[i + 1] = c;
            cs[i + 2] = password.charAt(i / 3 * 2 + 1);
        }

        return new String(cs);
    }

    /**
     * 校验密码是否正确, 若传自定义salt, 则多一次加密
     */
    public static boolean verify(String password, String md5, String... salt) {
        char[] cs1 = new char[32];
        char[] cs2 = new char[16];
        for (int i = 0; i < 48; i += 3) {
            cs1[i / 3 * 2] = md5.charAt(i);
            cs1[i / 3 * 2 + 1] = md5.charAt(i + 2);
            cs2[i / 3] = md5.charAt(i + 1);
        }
        String randomsalt = new String(cs2);

        String self = null;
        if (salt.length > 0) {
            self = salt[0];

            return md5Hex(md5Hex(md5Hex(password).toUpperCase() + randomsalt) + self).equals(new String(cs1));
        } else {

            return md5Hex(md5Hex(password).toUpperCase() + randomsalt).equals(new String(cs1));
        }
    }

    /**
     * 获取十六进制字符串形式的MD5摘要
     */
    public static String md5Hex(String src) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bs = md5.digest(src.getBytes());

            return new String(new Hex().encode(bs));
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

}
