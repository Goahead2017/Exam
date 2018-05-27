package com.bignerdranch.android.ourcqupt.MainFragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bignerdranch.android.ourcqupt.CustomView.ImageBarnnerFramLayout;
import com.bignerdranch.android.ourcqupt.ImageToLoad.ImageLoader;
import com.bignerdranch.android.ourcqupt.ImageToLoad.ImageUrls;
import com.bignerdranch.android.ourcqupt.R;
import com.bignerdranch.android.ourcqupt.StaticContent;

import java.util.ArrayList;
import java.util.List;

/**
 * 课表页面
 * Created by 14158 on 2018/5/26.
 */

public class FindFragment extends Fragment implements ImageBarnnerFramLayout.FramLayoutLisenner {

    /*Context mContext;
    ImageLoader mImageLoader;*/

    private int[] ids = new int[]{R.drawable.carousel_one,R.drawable.carousel_two,R.drawable.carousel_three};

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find, null);

        ImageView image = view.findViewById(R.id.image);

        //计算当前手机的宽度
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        StaticContent.WITTH = dm.widthPixels;

        ImageBarnnerFramLayout mGroup = view.findViewById(R.id.image_group);
        mGroup.setLisenner(this);
        List<Bitmap> list = new ArrayList<>();

        for (int id : ids) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), id);
            list.add(bitmap);
        }
        mGroup.addBitmaps(list);

        /*mContext = getActivity();
        mImageLoader = new ImageLoader(mContext);

        for (int i = 0; i < 3; i++) {
            Bitmap bitmap = mImageLoader.bindBitmap(ImageUrls.imageUrls.get(i),image, 325, 193);
            list.add(bitmap);
        }
        mGroup.addBitmaps(list);*/

        return view;
    }

    @Override
    public void clickImageIndex(int pos) {
        //实现轮播图具体图片的点击事件
        switch (pos) {
            case 0:
                Toast.makeText(getActivity(), "第一张图片" + pos, Toast.LENGTH_SHORT).show();
                break;
            case 1:
                Toast.makeText(getActivity(), "第二张图片" + pos, Toast.LENGTH_SHORT).show();
                break;
            case 2:
                Toast.makeText(getActivity(), "第三张图片" + pos, Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
