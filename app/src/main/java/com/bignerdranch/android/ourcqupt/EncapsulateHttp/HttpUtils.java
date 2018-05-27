package com.bignerdranch.android.ourcqupt.EncapsulateHttp;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.bignerdranch.android.ourcqupt.ImageToLoad.ImageResizer;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * 封装网络请求
 */

public class HttpUtils {

    /**
     * @param address   传入的url
     * @param listener  回调接口的实例
     * 在调用该类的时候还需要将HttpCallbackListener的实例传入
     *       HttpUtil.sendHttpRequest(address,new HttpCallbackListener(){
     *              //@Override
     *              public void onFinish(String response){
     *                  //在这里根据返回的内容执行具体的逻辑
     *              }
     *
     *              //@Override
     *              public void onError(Exception e){
     *                  //在这里对异常情况进行处理
     *              }
     *       });
     */

    public static void sendHttpRequest(final String address, final HttpCallbackListener listener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection)url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    /*DataOutputStream out = new DataOutputStream(connection.getOutputStream());*/
                    /*out.writeBytes("stuNum=stu_Num&idNum=id_Num");*/
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    InputStream in = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null){
                        response.append(line);
                    }
                    if(listener != null){
                        //回调onFinish()方法
                        listener.onFinish(response.toString());
                    }
                }catch (Exception e){
                    if(listener != null){
                        //回调onError()方法
                        listener.onError(e);
                    }
                }finally {
                    if (connection != null){
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }



    public static void  sendHttpPost(final String urls, final Map<String,String> map, final HttpCallbackListener httpCallbackListener){
        new Thread(){
            @Override
            public void run() {
                byte[] data = getRequestData(map, "utf-8").toString().getBytes();//获得请求体
                try {
                    URL url = new URL(urls);
                    HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                    httpURLConnection.setConnectTimeout(3000);     //设置连接超时时间
                    httpURLConnection.setDoInput(true);                  //打开输入流，以便从服务器获取数据
                    httpURLConnection.setDoOutput(true);                 //打开输出流，以便向服务器提交数据
                    httpURLConnection.setRequestMethod("POST");     //设置以Post方式提交数据
                    httpURLConnection.setUseCaches(false);               //使用Post方式不能使用缓存
                    //设置请求体的类型是文本类型
                    httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    //设置请求体的长度
                    httpURLConnection.setRequestProperty("Content-Length", String.valueOf(data.length));
                    //获得输出流，向服务器写入数据
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    outputStream.write(data);

                    int response = httpURLConnection.getResponseCode();            //获得服务器的响应码
                    if(response == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = httpURLConnection.getInputStream();
                        httpCallbackListener.onFinish(dealResponseResult(inputStream));
//                        dealResponseResult(inputStream);                     //处理服务器的响应结果
                    }
                } catch (IOException e) {
                    httpCallbackListener.onError(e);
                    //e.printStackTrace();

                }
            }
        }.start();
    }

    /*
        * Function  :   封装请求体信息
        * Param     :   params请求体内容，encode编码格式
        */
    public static StringBuffer getRequestData(Map<String, String> params, String encode) {
        StringBuffer stringBuffer = new StringBuffer();        //存储封装好的请求体信息
        try {
            for(Map.Entry<String, String> entry : params.entrySet()) {
                stringBuffer.append(entry.getKey())
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), encode))
                        .append("&");
            }
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);    //删除最后的一个"&"
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuffer;
    }

    /*
     * Function  :   处理服务器的响应结果（将输入流转化成字符串）
     * Param     :   inputStream服务器的响应输入流
     */
    public static String dealResponseResult(InputStream inputStream) {
        String resultData = null;      //存储处理结果
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = 0;
        try {
            while((len = inputStream.read(data)) != -1) {
                byteArrayOutputStream.write(data, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        resultData = new String(byteArrayOutputStream.toByteArray());
        return resultData;
    }

    public static Bitmap downloadBitmapFromUrl(String urlString, Resources res, int resId, int reqWidth, int reqHeight){
        Bitmap bitmap = null;
        HttpURLConnection urlConnection = null;
        ImageResizer imageResizer = ImageResizer.getInstance();

        try {
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setConnectTimeout(5 * 1000);
            urlConnection.setReadTimeout(10 * 1000);
            bitmap = BitmapFactory.decodeStream(urlConnection.getInputStream());
            bitmap = imageResizer.decodeSampledBitmapFromResource(res,resId,reqWidth,reqHeight);
        }catch (final IOException e){
            Log.e(TAG,"Error in downloadBitmap:" + e);
        }finally {
            if(urlConnection != null){
                urlConnection.disconnect();
            }
        }
        return bitmap;
    }

}
