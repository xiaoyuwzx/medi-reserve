package com.medireserve.common.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5 加密工具类
 * 用于对用户密码进行加密存储
 */
public class Md5Util {

    /**
     * 将字符串进行 MD5 加密（返回 32 位小写十六进制字符串）
     * @param input 原始字符串
     * @return MD5 加密后的字符串
     */
    public static String encrypt(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));  // 转换为小写十六进制
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 加密失败", e);
        }
    }

    /**
     * 校验原始字符串与 MD5 加密后的密文是否匹配
     * @param rawPassword 原始密码
     * @param encryptedPassword MD5 加密后的密码
     * @return true-匹配，false-不匹配
     */
    public static boolean verify(String rawPassword, String encryptedPassword) {
        return encrypt(rawPassword).equals(encryptedPassword);
    }

    /**
     * 测试入口
     */
    public static void main(String[] args) {
        System.out.println("123456 的 MD5 加密结果：" + encrypt("123456"));
        // 输出：e10adc3949ba59abbe56e057f20f883e
        System.out.println("验证：" + verify("123456", "e10adc3949ba59abbe56e057f20f883e"));
    }
}