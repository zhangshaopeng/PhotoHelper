package com.project.peng.photohelper.utils;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.project.peng.photohelper.R;
import com.project.peng.photohelper.adapter.ListDirAdapter;
import com.project.peng.photohelper.bean.FolderBean;

import java.util.List;

/**
 * Describe：重写popupwindow
 * auther：  zhangshaopeng
 * Emile：   1377785991@qq.com
 * Date：    2018/6/18
 */

public class ListImagPop extends PopupWindow {
    private int mWidth;
    private int mHeight;
    private View mConvertView;
    private ListView mListview;
    private List<FolderBean> mData;
    private OnDirSelectedListener mListener;

    public interface OnDirSelectedListener {
        void onSelected(FolderBean folderBean);
    }

    public void setOnDirSelectedListener(OnDirSelectedListener mListener) {
        this.mListener = mListener;
    }

    public ListImagPop(Context context, List<FolderBean> data) {
        calWidthAndHeight(context);
        mConvertView = LayoutInflater.from(context).inflate(R.layout.item_pop, null);
        mData = data;
        setContentView(mConvertView);
        setWidth(mWidth);
        setHeight(mHeight);
        setFocusable(true);
        setTouchable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new BitmapDrawable());
        setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    dismiss();
                }
                return false;
            }
        });
        initView(context);
        initEvent();
    }

    private void initEvent() {

        mListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mListener != null) {
                    mListener.onSelected(mData.get(i));
                }
            }
        });
    }

    private void initView(Context context) {
        mListview = mConvertView.findViewById(R.id.id_list_dir);
        mListview.setAdapter(new ListDirAdapter(context, mData));
    }

    /**
     * 屏幕高度
     *
     * @param context
     */
    private void calWidthAndHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        mWidth = outMetrics.widthPixels;
        mHeight = (int) (outMetrics.heightPixels * 0.7);
    }
}
