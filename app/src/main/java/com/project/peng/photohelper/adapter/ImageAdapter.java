package com.project.peng.photohelper.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.project.peng.photohelper.utils.ImageLoader;
import com.project.peng.photohelper.R;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImageAdapter extends BaseAdapter {
    private static Set<String> mSelectImg = new HashSet<String>();
    private List<String> mDatas;
    private String dirPath;
    private LayoutInflater mInflater;
    private String filePath;

    public ImageAdapter(Context context, List<String> mDatas, String dirPath) {
        this.mDatas = mDatas;
        this.dirPath = dirPath;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int i) {
        return mDatas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
         final ViewHodler viewHodler;
        if (view == null) {
            viewHodler = new ViewHodler();
            view = mInflater.inflate(R.layout.item_gridview, null);
            viewHodler.mImg = view.findViewById(R.id.id_item_image);
            viewHodler.mSelect = view.findViewById(R.id.id_item_select);
            view.setTag(viewHodler);
        } else {
            viewHodler = (ViewHodler) view.getTag();
        }
        //重制状态
//        viewHodler.mImg.setImageResource(R.mipmap.ic_launcher);
        viewHodler.mSelect.setImageResource(R.drawable.picture_unselected);
        ImageLoader.getInstace(3, ImageLoader.Type.LIFO).loadImage(dirPath + "/" +
                mDatas.get(i), viewHodler.mImg);
        viewHodler.mImg.setColorFilter(null);



        viewHodler.mImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filePath = dirPath + "/" + mDatas.get(i);
                if (mSelectImg.contains(filePath)) {
                    mSelectImg.remove(filePath);
                    viewHodler.mImg.setColorFilter(null);
                    viewHodler.mSelect.setImageResource(R.drawable.picture_unselected);

                } else {
                    mSelectImg.add(filePath);
                    viewHodler.mImg.setColorFilter(Color.parseColor("#77000000"));
                    viewHodler.mSelect.setImageResource(R.drawable.picture_selected);
                }
                notifyDataSetChanged();
            }
        });
        if (mSelectImg.contains(filePath)) {
            viewHodler.mImg.setColorFilter(Color.parseColor("#77000000"));
            viewHodler.mSelect.setImageResource(R.drawable.picture_selected);
        }

        return view;
    }

    private class ViewHodler {
        ImageView mImg;
        ImageButton mSelect;
    }
}