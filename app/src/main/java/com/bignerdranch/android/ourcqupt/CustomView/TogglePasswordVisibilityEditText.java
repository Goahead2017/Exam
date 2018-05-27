package com.bignerdranch.android.ourcqupt.CustomView;

/*import android.annotation.SuppressLint;*/
import android.content.Context;
/*import android.graphics.drawable.Drawable;*/
import android.text.InputType;
/*import android.text.Selection;*/
/*import android.text.Spannable;*/
/*import android.text.method.PasswordTransformationMethod;*/
import android.util.AttributeSet;
/*import android.view.MotionEvent;*/
/*import android.view.inputmethod.EditorInfo;*/

/*import com.bignerdranch.android.ourcqupt.R;*/

/**
 * 自定义EditText，实现点击图标更换密码的显示与隐藏
 * Created by 14158 on 2018/4/5.
 */

public class TogglePasswordVisibilityEditText extends android.support.v7.widget.AppCompatEditText {

    /*private Drawable visibilityDrawable;*/
    private boolean visibility = false;

    public TogglePasswordVisibilityEditText(Context context) {
        super(context,null);
        init();
    }

    public TogglePasswordVisibilityEditText(Context context, AttributeSet attrs) {
        super(context, attrs,android.R.attr.editTextStyle);
        init();
    }

    public TogglePasswordVisibilityEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        /*Drawable[] compoundDrawables = getCompoundDrawables();
        visibilityDrawable = compoundDrawables[2];
        if(visibilityDrawable == null){
            visibilityDrawable = getResources().getDrawable(R.drawable.eye_off);
        }*/
        //新增判断在最开始是否获取焦点，获取焦点显示图标，并将文本以密码显示
        if(requestFocus()){
            /*visibilityDrawable = getResources().getDrawable(R.drawable.eye_off);*/
            this.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        /*visibilityDrawable.setBounds(0,0,visibilityDrawable.getMinimumWidth(),visibilityDrawable.getMinimumHeight());
        setCompoundDrawables(getCompoundDrawables()[0],getCompoundDrawables()[1],visibilityDrawable,getCompoundDrawables()[3]);*/
    }
/*
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event){
        int action = event.getAction();
        if (action == MotionEvent.ACTION_UP){
            if (getCompoundDrawables()[2] != null){
                *//*boolean xFlag;*//*
                *//*xFlag = event.getX()>getWidth()-(visibilityDrawable.getIntrinsicWidth() + getCompoundPaddingRight()) && event.getX()<getWidth()-(getTotalPaddingRight()-getCompoundPaddingRight());*//*
                *//*if(xFlag){*//*
                    visibility = !visibility;
                    if (visibility){
                        //明文显示
                        *//*visibilityDrawable = getResources().getDrawable(R.drawable.eye);*//*
                        this.setInputType(EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    }else {
                        //密码显示
                        *//*visibilityDrawable = getResources().getDrawable(R.drawable.eye_off);*//*
                        this.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        this.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    }

                    //将光标定位到指定的位置
                    CharSequence text = this.getText();
                    if(text != null){
                        Spannable spanText = (Spannable)text;
                        Selection.setSelection(spanText,text.length());
                    }

*//*
                    visibilityDrawable.setBounds(0,0,visibilityDrawable.getMinimumWidth(),visibilityDrawable.getMinimumHeight());
                    setCompoundDrawables(getCompoundDrawables()[0],getCompoundDrawables()[1],visibilityDrawable,getCompoundDrawables()[3]);
*//*

                }
            }
        *//*}*//*
        return super.onTouchEvent(event);
    }*/

}
