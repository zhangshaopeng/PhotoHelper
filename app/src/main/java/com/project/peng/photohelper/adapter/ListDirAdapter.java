package com.project.peng.photohelper.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.project.peng.photohelper.bean.FolderBean;
import com.project.peng.photohelper.utils.ImageLoader;
import com.project.peng.photohelper.R;

import java.util.List;


/**
 * Describe：popupwind的适配器
 * auther：  zhangshaopeng
 * Emile：   1377785991@qq.com
 * Date：    2018/6/18
 */
public class ListDirAdapter extends ArrayAdapter<FolderBean> {
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

        hodler.mDirCount.setText(bean.getCount() + "张");
        hodler.mDirName.setText(bean.getName());
        return convertView;
    }

    private class ViewHodler {
        ImageView mImg;
        TextView mDirName;
        TextView mDirCount;
    }
}