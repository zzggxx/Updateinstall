package com.snbc.updateinstall;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getName();
    private String mApkPath;

    // TODO: 2020/9/16 assets资产目录想存放com.magugi.enterprise.apk和app-release.apk文件
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        应用安装,注意都需要release版本.
//        mApkPath = getFilesDir().getAbsolutePath() + File.separator + "app-release.apk";
        mApkPath = getFilesDir().getAbsolutePath() + File.separator + "com.magugi.enterprise.apk";
        Log.i(TAG, "onCreate,mApkPath: " + mApkPath);

        if (!new File(mApkPath).exists()) {
            copyApk();
        }

        findViewById(R.id.install_apk).setOnClickListener(this);

        List<PackageInfo> installedPackages = getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < installedPackages.size(); i++) {
            if (installedPackages.get(i).packageName.equals("com.snbc.androidtest")) {
                Log.i(TAG, "onCreate: --" + installedPackages.get(i).packageName);
            }
        }
    }

    private void copyApk() {
        try {
//            InputStream open = getAssets().open("app-release.apk");
            InputStream open = getAssets().open("com.magugi.enterprise.apk");
            FileOutputStream outputStream = new FileOutputStream(new File(mApkPath));
            byte[] buff = new byte[(int) (1024 * 0.5)];
            int len = 0;
            while ((len = (open.read(buff))) != -1) {
                outputStream.write(buff, 0, len);
            }
            open.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {

            }
        }, 1000);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.install_apk:
//                installApk7x(this,new File(mApkPath),"com.snbc.updateinstall.fileProvider");
//                7.0
//                installApk7x(this,new File(mApkPath),"com.magugi.enterprise.fileProvider");
//                8.0
//                installApk(mApkPath);
                Intent intent = new Intent();
//                intent.setAction("com.snbc.androidtest.VoiceTestActivity");
//                intent.addCategory("android.intent.category.DEFAULT");
                intent.setAction("com.snbc.androidtest.MainActivity");
                intent.addCategory("android.intent.category.DEFAULT");
                startActivity(intent);
                break;
        }
    }

    /**
     * Android 7.x 安装APK，需要配置FileProvider
     *
     * @param context   上下文
     * @param file      要安装的APK文件
     * @param authority FileProvider配置的authority
     */
    public void installApk7x(Context context, File file, String authority) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setAction(Intent.ACTION_INSTALL_PACKAGE);
        Uri uri = FileProvider.getUriForFile(context, authority, file); // 通过FileProvider获取Uri
        intent.setDataAndType(uri, "application/vnd.android.package-archive");// APK的MimeType
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 在新栈中启动
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // 授权读取URI权限
        context.startActivity(intent);
    }

    private void installApk(String apkPath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
//        apk path
        File apkFile = new File(apkPath);
        //        兼容7.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(this, "com.magugi.enterprise.fileProvider", apkFile);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");

            //            兼容8.0(安装位置的apk权限)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                boolean hasInstallPermission = getPackageManager().canRequestPackageInstalls();
                if (!hasInstallPermission) {
                    startInstallPermissionSettingActivity();
                    return;
                }
            }
        } else {
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        if (getPackageManager().queryIntentActivities(intent, 0).size() > 0) {
            startActivity(intent);
        }
    }

    /**
     * 跳转到设置-允许安装未知来源-页面
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startInstallPermissionSettingActivity() {
        //注意这个是8.0新API
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}