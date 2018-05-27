package com.bignerdranch.android.ourcqupt.EncapsulateHttp;

/**
 * 通过java的回调机制实现在封装网络请求的类中开线程
 * Created by 14158 on 2018/4/5.
 */

public interface HttpCallbackListener {

    void onFinish(String response);

    void onError(Exception e);

}
