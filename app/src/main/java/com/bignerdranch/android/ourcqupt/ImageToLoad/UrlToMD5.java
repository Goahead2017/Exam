package com.bignerdranch.android.ourcqupt.ImageToLoad;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 把图片的url转换成key，一般采用url的md5值作为key
 */

class UrlToMD5 {

    private static class SingletonHolder{
        private static final UrlToMD5 INSTANCE = new UrlToMD5();
    }

    private UrlToMD5(){}

    public static UrlToMD5 getInstance(){
        return SingletonHolder.INSTANCE;
    }

    public String hashKeyFormUrl(String url){
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(url.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        }catch (NoSuchAlgorithmException e){
            cacheKey = String.valueOf(url.hashCode());
        }
        return cacheKey;
    }

    private String bytesToHexString(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for(int i = 0;i<bytes.length;i++){
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if(hex.length() == 1){
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

}
