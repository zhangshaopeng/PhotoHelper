package com.project.peng.photohelper;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.project.peng.photohelper.adapter.ImageAdapter;
import com.project.peng.photohelper.bean.FolderBean;
import com.project.peng.photohelper.utils.ListImagPop;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SelectPhotoActivity extends AppCompatActivity {
    private GridView mGridView;
    private RelativeLayout mBottomly;
    private TextView mDirName, mDirCount;
    private File mCurrentDir;
    private int mMaxCount;

    private List<FolderBean> mFolderBean = new ArrayList<>();
    private ProgressDialog progressDialog;
    private List<String> mImgs;
    private ListImagPop mDirPopupWindow;
    private ImageAdapter mImageAdapter;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x110) {
                progressDialog.dismiss();
                data2View();
                initPopupWindw();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initDatas();
        initEvent();
    }

    /**
     * 选中pop条目设置数据
     */
    private void data2View() {
        if (mCurrentDir == null) {
            Toast.makeText(this, "未扫描到图片", Toast.LENGTH_SHORT).show();
            return;
        }
        mImgs = Arrays.asList(mCurrentDir.list());
        mImageAdapter = new ImageAdapter(this, mImgs, mCurrentDir.getAbsolutePath());
        mGridView.setAdapter(mImageAdapter);
        mDirCount.setText(mMaxCount + "张");
        mDirName.setText(mCurrentDir.getName() + "");
    }


    /**
     * 添加点击事件
     */
    private void initEvent() {
        mBottomly.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View view) {
                mDirPopupWindow.showAsDropDown(mBottomly, 0, 0);
                lightOff();
            }
        });
    }

    /**
     * 利用contentProvider扫描
     */
    private void initDatas() {
        if (!Environment.getExternalStorageState().
                equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "当前储存卡不可用！",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog = ProgressDialog.show(this,
                null, "正在加载...");
        new Thread() {
            @Override
            public void run() {
                super.run();
                Uri mImgUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver cr = SelectPhotoActivity.this.getContentResolver();
                String s = MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "= ?";
                Log.e("aaaa", s);
                Cursor cursor = cr.query(mImgUri, null,
                        MediaStore.Images.Media.MIME_TYPE + "= ? or " + MediaStore.Images.Media.MIME_TYPE + "= ?"
                        , new String[]{"image/jpeg", "image/png"}
                        , MediaStore.Images.Media.DATE_MODIFIED);
                Set<String> mDirPaths = new HashSet<>();
                while (cursor.moveToNext()) {
                    String path = cursor.getString
                            (cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    File parentFile = new File(path).getParentFile();
                    if (parentFile == null)
                        continue;
                    String dirPath = parentFile.getAbsolutePath();
                    FolderBean folderBean = null;

                    if (mDirPaths.contains(dirPath)) {
                        continue;
                    } else {
                        mDirPaths.add(dirPath);
                        folderBean = new FolderBean();
                        folderBean.setDir(dirPath);
                        folderBean.setFirstImagePath(path);
                    }

                    if (parentFile.list() == null)
                        continue;

                    int picSize = parentFile.list(new FilenameFilter() {
                        @Override
                        public boolean accept(File file, String filename) {
                            if (filename.endsWith(".jpg")
                                    || filename.endsWith(".png")
                                    || filename.endsWith(".jpeg"))
                                return true;
                            return false;

                        }
                    }).length;
                    folderBean.setCount(picSize);
                    mFolderBean.add(folderBean);
                    if (picSize > mMaxCount) {
                        mMaxCount = picSize;
                        mCurrentDir = parentFile;

                    }

                }
                //扫描完成，释放临时变量空间内存
                cursor.close();
//                mDirPaths = null;
                mHandler.sendEmptyMessage(0x110);

            }
        }.start();

    }

    private void initView() {
        mGridView = findViewById(R.id.id_gridView);
        mBottomly = findViewById(R.id.id_bottom_ly);
        mDirName = findViewById(R.id.id_dir_name);
        mDirCount = findViewById(R.id.id_dir_count);
    }

    /**
     * 初始化pop
     */
    private void initPopupWindw() {
        mDirPopupWindow = new ListImagPop(this, mFolderBean);
        mDirPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                lightOn();
            }
        });
        mDirPopupWindow.setOnDirSelectedListener(new ListImagPop.OnDirSelectedListener() {
            @Override
            public void onSelected(FolderBean folderBean) {
                mCurrentDir = new File(folderBean.getDir());
                mImgs = Arrays.asList(mCurrentDir.list(new FilenameFilter() {
                    @Override
                    public boolean accept(File file, String filename) {
                        if (filename.endsWith(".jpg")
                                || filename.endsWith(".png")
                                || filename.endsWith(".jpeg"))
                            return true;
                        return false;
                    }
                }));
                mImageAdapter = new ImageAdapter(SelectPhotoActivity.this, mImgs
                        , mCurrentDir.getAbsolutePath());
                mGridView.setAdapter(mImageAdapter);
                mDirCount.setText(mImgs.size() + "");
                mDirName.setText(folderBean.getName());
                mDirPopupWindow.dismiss();
            }
        });
    }

    /**
     * pop的背景光暗颜色
     */
    private void lightOn() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 1.0f;
        getWindow().setAttributes(lp);

    }

    private void lightOff() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 1.3f;
        getWindow().setAttributes(lp);

    }
}
