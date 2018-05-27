package com.bignerdranch.android.ourcqupt;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bignerdranch.android.ourcqupt.EncapsulateHttp.HttpCallbackListener;
import com.bignerdranch.android.ourcqupt.EncapsulateHttp.HttpUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private EditText et1;
    private EditText et2;
    String stuNum;
    String idNum;
    private ImageView login;
    Context context;
    Boolean flag = false;

    //是否使用特殊的标题栏颜色，android5.0以上可以设置状态栏背景色，如果不使用则使用透明色值
    protected boolean useThemestatusBarColor = false;
    //是否使用状态栏文字和图标为暗色，如果状态栏采用了白色系，则需要使状态栏和图标为暗色，android6.0以上可以设置
    protected boolean useStatusBarColor = true;
    private HashMap<String, String> params;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        context = this.getApplicationContext();
        initStatusBar();

        //让第一个EditText首先获取焦点
        et1 = findViewById(R.id.et1);
        et1.setFocusable(true);
        et1.setFocusableInTouchMode(true);
        et1.requestFocus();
        et1.requestFocusFromTouch();

        et2 = findViewById(R.id.et2);

        //设置点击事件，实现界面的跳转
        login = findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取输入的学号和密码
                stuNum = et1.getText().toString();
                idNum = et2.getText().toString();
                if (stuNum.trim().length() == 0 || idNum.trim().length() == 0) {
                    Toast.makeText(getApplicationContext(), "请先把信息填写完整", Toast.LENGTH_SHORT).show();
                } else {
                    StartLogin(stuNum, idNum);
                    if (!flag) {
                        Toast.makeText(getApplicationContext(), "学号或密码错误，请重新输入", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        });
                /*StartLogin("","");*/
    }

    //判断用户能否登录
    private void StartLogin(final String stuNum, final String idNum) {
        params = new HashMap<String, String>();
        params.put("stuNum", "2017210158");
        params.put("idNum", "157526");
        HttpUtils.sendHttpPost
          ("https://wx.idsbllp.cn/api/verify", params, new HttpCallbackListener() {
              @Override
              public void onFinish(String response) {
                  try {
                      JSONObject jsonObject=new JSONObject(response);
                      JSONObject data = jsonObject.getJSONObject("data");
                      String stuNum1 = data.getString("stuNum");
                      String idNum1 = data.getString("idNum");
                      Log.d(TAG, "onFinish: " + stuNum1 + idNum1);
                      if(stuNum.equals(stuNum1) && idNum.equals(idNum1)){
                          flag = true;
                          return;
                      }
                  } catch (JSONException e) {
                      e.printStackTrace();
                  }
                  Log.d(TAG, "onFinish: "+response);
              }

              @Override
              public void onError(Exception e) {

              }
          });

//        HttpUtils.sendHttpRequest("https://wx.idsbllp.cn/cyxbsMobile/index.php/Home/Person/search",new HttpCallbackListener(){
//            String responseStuNum;
//                    @Override
//                    public void onFinish(String response) {
//                        //使用json解析数据
//                        try {
//                            JSONArray jsonArray = new JSONArray(response);
//                                for (int i = 0; i < jsonArray.length(); i++) {
//                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
//                                    responseStuNum = jsonObject.getString("status");
//                                    Log.d("respondData",responseStuNum);
//                                }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//            @Override
//            public void onError(Exception e) {
//                /*Toast.makeText(getApplicationContext(),"网络异常，请重新尝试",Toast.LENGTH_SHORT).show();*/
//            }
//        });
    }

    //获取底部导航栏高度
    private static int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height","dimen","android");
        //获取NavigationBar的高度
        return resources.getDimensionPixelOffset(resourceId);
    }

    //通过反射判断是否存在NavigationBar
    private static boolean checkDeviceHasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar","bool","android");
        if(id > 0){
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            @SuppressLint("PrivateApi") Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get",String.class);
            String navBarOverride = (String)m.invoke(systemPropertiesClass,"qemu.hw.mainkeys");
            if("1".equals(navBarOverride)){
                hasNavigationBar = false;
            }else if("0".equals(navBarOverride)){
                hasNavigationBar = true;
            }
        } catch (Exception e){

        }
        return hasNavigationBar;
    }

    private void initStatusBar() {
        if(checkDeviceHasNavigationBar(context)){
            getWindow().getDecorView().findViewById(android.R.id.content).setPadding(0,0,0,getNavigationBarHeight(context));
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            //5.0及以上
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            //根据上面设置是否对状态栏单独设置颜色
            if(useThemestatusBarColor) {
                /*getWindow().setStatusBarColor(getResources().getColor());*/
            }else {
                getWindow().setStatusBarColor(Color.TRANSPARENT);
            }
        }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            //4.4到5.0
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !useStatusBarColor){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

    }
}
