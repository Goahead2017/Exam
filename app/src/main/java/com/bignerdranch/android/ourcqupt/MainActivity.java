package com.bignerdranch.android.ourcqupt;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioButton;

import com.bignerdranch.android.ourcqupt.MainFragment.AskFragment;
import com.bignerdranch.android.ourcqupt.MainFragment.ClassFragment;
import com.bignerdranch.android.ourcqupt.MainFragment.FindFragment;
import com.bignerdranch.android.ourcqupt.MainFragment.MyFragment;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Context context;

    //是否使用特殊的标题栏颜色，android5.0以上可以设置状态栏背景色，如果不使用则使用透明色值
    protected boolean useThemestatusBarColor = false;
    //是否使用状态栏文字和图标为暗色，如果状态栏采用了白色系，则需要使状态栏和图标为暗色，android6.0以上可以设置
    protected boolean useStatusBarColor = true;

    RadioButton one;
    RadioButton two;
    RadioButton three;
    RadioButton four;
    private FragmentManager manager;
    private FragmentTransaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this.getApplicationContext();
        initStatusBar();

        one = findViewById(R.id.one);
        two = findViewById(R.id.two);
        three = findViewById(R.id.three);
        four = findViewById(R.id.four);

        //默认显示第一个Fragment
        manager=getSupportFragmentManager();
        transaction=manager.beginTransaction();
        transaction.add(R.id.layout,new ClassFragment());
        transaction.commit();

        //给RadioButton的各个按键设置监听事件
        one.setOnClickListener(this);
        two.setOnClickListener(this);
        three.setOnClickListener(this);
        four.setOnClickListener(this);

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
        /*if(checkDeviceHasNavigationBar(context)){
            getWindow().getDecorView().findViewById(android.R.id.content).setPadding(0,0,0,getNavigationBarHeight(context));
        }*/
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
    @Override
    public void onClick(View v) {
        //点击不同的底部导航按钮切换到不同的Fragment
        transaction=manager.beginTransaction();
        switch (v.getId()) {
            case R.id.one:
                transaction.replace(R.id.layout, new ClassFragment());
                break;
            case R.id.two:
                transaction.replace(R.id.layout, new AskFragment());
                break;
            case R.id.three:
                transaction.replace(R.id.layout, new FindFragment());
                break;
            case R.id.four:
                transaction.replace(R.id.layout, new MyFragment());
        }
        transaction.commit();
    }
}
