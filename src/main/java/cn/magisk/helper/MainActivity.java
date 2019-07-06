package cn.magisk.helper;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
//import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.os.Environment;

import java.util.List;
//import android.util.Log;
import android.view.View;
//import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import android.content.pm.PackageManager;
import android.Manifest;

import com.leon.lfilepickerlibrary.LFilePicker;
import com.leon.lfilepickerlibrary.utils.Constant;
import com.leon.lfilepickerlibrary.utils.ShellUtils;

public class MainActivity extends AppCompatActivity {

    private final int REQUESTCODE_SEL_MAGISK = 1000;
    private final int REQUESTCODE_SEL_BOOT = 2000;
    private final int CODE_FOR_WRITE_PERMISSION = 200;

    //am start -n com.topjohnwu.magisk/a.f -d file:///storage/emulated/0/boot.img --es action patch
    //data/data/com.topjohnwu.magisk/cache/
    //private final String cp_command="cp %s /data/data/com.topjohnwu.magisk/cache/magisk.zip";
    //private final String patch_command="am start -n com.topjohnwu.magisk/a.f -d file://%s --es action patch";
    private String MagiskFilePath;
    private String BootFilePath;
    private TextView textMain;
    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textMain = findViewById(R.id.textMain);
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what){
                    case 1:
                    {
                        String cmd = String.format(getString(R.string.cp_command), MagiskFilePath);
                        showCommand(cmd);
                        ShellUtils.CommandResult result = ShellUtils.execCmd(cmd, true, true);
                        String text;
                        if(result.result==0){
                            text = result.successMsg+"\n- success!";
                        }else {
                            text = result.errorMsg;
                        }
                        showCommand(text);
                    }
                    break;
                    case 2:
                    {
                        String cmd = String.format(getString(R.string.patch_command), BootFilePath);
                        showCommand(cmd);
                        ShellUtils.CommandResult result = ShellUtils.execCmd(cmd, true, true);
                        String text;
                        if(result.result==0)
                            text = result.successMsg + "\n- success!";
                        else {
                            text = result.errorMsg;
                        }
                        showCommand(text);
                    }
                    break;
                    case 3:
                    {
                        String str = (String) msg.obj;
                        textMain.append("\n");
                        textMain.append(str);
                    }
                    break;
                    default:
                        break;
                }
                return false;
            }
        });

    }
    public void choseMagiskFile(View v){
        if(requestpermiss(this)==1) {
            new LFilePicker()
                    .withTitle(getString(R.string.choseMagiskFile))
                    .withActivity(MainActivity.this)
                    .withRequestCode(REQUESTCODE_SEL_MAGISK)
                    .withStartPath(Environment.getExternalStorageDirectory().getAbsolutePath())
                    //.withIsGreater(false)
                    .withMutilyMode(false)
                    .withFileSize(500 * 1024)
                    .start();
        }
    }

    public void choseBootFile(View v) {
        if(requestpermiss(this)==1){
            new LFilePicker()
                    .withTitle(getString(R.string.choseBootFile))
                    .withActivity(MainActivity.this)
                    .withRequestCode(REQUESTCODE_SEL_BOOT)
                    .withStartPath(Environment.getExternalStorageDirectory().getAbsolutePath())
                    //.withIsGreater(false)
                    .withMutilyMode(false)
                    .withFileSize(500 * 1024)
                    .start();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUESTCODE_SEL_MAGISK) {
                assert data != null;
                List<String> list = data.getStringArrayListExtra(Constant.RESULT_INFO);
                for (String s : list) {
                    Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                }
                if(list.size()>0){
                    MagiskFilePath = list.get(0);
                    showCommand("Magisk.zip: " + MagiskFilePath);
                    //textMain.setText(MagiskFilePath);
                }
            }else if (requestCode == REQUESTCODE_SEL_BOOT) {
                assert data != null;
                List<String> list = data.getStringArrayListExtra(Constant.RESULT_INFO);
                for (String s : list) {
                    Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                }
                if(list.size()>0){
                    BootFilePath = list.get(0);
                    showCommand("boot.img: " + BootFilePath);
                    //textMain.setText(BootFilePath);
                }
            }
        }
    }

    public void patchBoot(View view) {
        if(BootFilePath!=null) {
            handler.sendEmptyMessage(2);
        }else {
            Toast.makeText(getApplicationContext(),getString(R.string.boot_file_not_found),Toast.LENGTH_SHORT).show();
        }
    }

    public void copyToCache(View v) {
        if(MagiskFilePath!=null) {
            handler.sendEmptyMessage(1);
        }else {
            Toast.makeText(getApplicationContext(),getString(R.string.magisk_file_not_found),Toast.LENGTH_SHORT).show();
        }
    }

    private  int requestpermiss(Activity thisActivity){
        //使用兼容库就无需判断系统版本
        int hasWriteStoragePermission = ContextCompat.checkSelfPermission(thisActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteStoragePermission == PackageManager.PERMISSION_GRANTED) {
            //拥有权限，执行操作
            //initScan();
            return 1;
        }else{
            //没有权限，向用户请求权限
            ActivityCompat.requestPermissions(thisActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CODE_FOR_WRITE_PERMISSION);
            return 0;
        }
    }
    private void showCommand(String text){
        Message message =new Message();
        message.what=3;
        message.obj = "- "+text;
        handler.sendMessage(message);
    }
}
