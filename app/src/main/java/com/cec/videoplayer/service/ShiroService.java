package com.cec.videoplayer.service;

import com.cec.videoplayer.model.User;

import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.crypto.hash.DefaultHashService;
import org.apache.shiro.crypto.hash.format.DefaultHashFormatFactory;
import org.apache.shiro.crypto.hash.format.Shiro1CryptFormat;

import java.security.MessageDigest;

public class ShiroService {

    private static final String SALT = "tamboo";

    /**
     * 通过shiro进行密码加密
     * @param user
     * @return
     */
    public static User shiroPwd(User user) {
        DefaultPasswordService passwordService = new DefaultPasswordService();
        DefaultHashService hashService = new DefaultHashService();
        passwordService.setHashService(hashService);

        Shiro1CryptFormat hashFormat = new Shiro1CryptFormat();
        passwordService.setHashFormat(hashFormat);

        DefaultHashFormatFactory hashFormatFactory = new DefaultHashFormatFactory();
        passwordService.setHashFormatFactory(hashFormatFactory);

        String pwd = passwordService.encryptPassword(user.getPassword());
        user.setPassword(pwd);
        return user;
    }

    public static User md5(User user) {
        String password = user.getPassword()+ SALT;
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        char[] charArray = password.toCharArray();
        byte[] byteArray = new byte[charArray.length];

        for (int i = 0; i < charArray.length; i++)
            byteArray[i] = (byte) charArray[i];
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }

            hexValue.append(Integer.toHexString(val));
        }
        user.setPassword(hexValue.toString());
        return user;
    }


}
