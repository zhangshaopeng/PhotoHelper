package com.project.peng.photohelper;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.project.peng.photohelper.utils.DefaultRationale;
import com.project.peng.photohelper.utils.PermissionSetting;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RequestExecutor;

import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private PermissionSetting mSetting = new PermissionSetting(this);
    private Rationale mRationale = new DefaultRationale();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    public void bt(View view) {
        AndPermission.with(this).permission(new String[]{Permission.WRITE_EXTERNAL_STORAGE})
                .rationale(mRationale)
                .onGranted(new Action() {
                    @Override
                    public void onAction(List<String> permissions) {
                        startActivity(new Intent(HomeActivity.this, SelectPhotoActivity.class));
                    }
                }).onDenied(new Action() {
            @Override
            public void onAction(List<String> permissions) {
                if (AndPermission.hasAlwaysDeniedPermission(HomeActivity.this, permissions)) {
                    mSetting.showSetting(permissions);
                }
            }
        }).start();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
