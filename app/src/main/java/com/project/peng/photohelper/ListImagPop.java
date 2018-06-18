package com.project.peng.photohelper;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

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

    private class ListDirAdapter extends ArrayAdapter<FolderBean> {
        private LayoutInflater mInflater;
        private List<FolderBean> mDatas;

        public ListDirAdapter(@NonNull Context context, List<FolderBean> mDatas) {
            super(context, 0, mDatas);
            mInflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ViewHodler hodler = null;
            if (convertView == null) {
                hodler = new ViewHodler();
                convertView = mInflater.inflate(R.layout.item_list, null);
                hodler.mImg = convertView.findViewById(R.id.id_dir_item_image);
                hodler.mDirCount = convertView.findViewById(R.id.id_dir_item_count);
                hodler.mDirName = convertView.findViewById(R.id.id_dir_item_name);
                convertView.setTag(hodler);
            } else {
                hodler = (ViewHodler) convertView.getTag();
            }
            FolderBean bean = getItem(position);
            //重置
            hodler.mImg.setImageResource(R.mipmap.ic_launcher);
            ImageLoader.getInstace(3, ImageLoader.Type.LIFO).loadImage(bean.getFirstImagePath(), hodler.mImg);

            hodler.mDirCount.setText(bean.getCount() + "");
            hodler.mDirName.setText(bean.getName());
            return convertView;
        }

        private class ViewHodler {
            ImageView mImg;
            TextView mDirName;
            TextView mDirCount;
        }
    }
}
