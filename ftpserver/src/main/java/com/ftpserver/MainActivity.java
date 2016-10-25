package com.ftpserver;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    static {
        System.setProperty("java.net.preferIPv6Addresses", "false");
    }

    @InjectView(R.id.textView2)
    TextView textView2;

    private FtpServer ftp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        String strIP;
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        strIP = intToIp(ipAddress);
        textView2.setText(strIP + ":" + pot);
    }

    int pot = 2221;
    String path = Environment.getExternalStorageDirectory() + File.separator + "FTP_TEST" + File.separator;
    private void startFtpServer() {

        File file = new File(path);
        if (!file.isDirectory()) {
            file.mkdir();
        }
        FtpServerFactory fsf = new FtpServerFactory();
        ListenerFactory lf = new ListenerFactory();

        lf.setPort(pot);
        fsf.addListener("default", lf.createListener());

        //通过读取配置文件来配置ftp设置

        copyResourceFile(R.raw.users, path + "ftpserver.properties");
        PropertiesUserManagerFactory usermanagerfactory = new PropertiesUserManagerFactory();
        usermanagerfactory.setFile(new File(path + "ftpserver.properties"));
        fsf.setUserManager(usermanagerfactory.createUserManager());


/*        //添加用户
        BaseUser user = new BaseUser();
        user.setName("test");
        user.setPassword("123456");
        user.setHomeDirectory("/mnt/sdcard");
        List<Authority> authorities = new ArrayList<>();
        authorities.add(new WritePermission());


        user.setAuthorities(authorities);

        try {
            fsf.getUserManager().save(user);
        } catch (FtpException e) {
            e.printStackTrace();
        }*/


        ftp = fsf.createServer();
        try {
            ftp.start();

        } catch (FtpException e) {
            e.printStackTrace();
        }

    }


    void stopFtp() {
        if (ftp != null)
            ftp.stop();

    }

    private void copyResourceFile(int rid, String targetFile) {
        InputStream fin = ((Context) this).getResources().openRawResource(rid);
        FileOutputStream fos = null;
        int length;
        try {
            fos = new FileOutputStream(targetFile);
            byte[] buffer = new byte[1024];
            while ((length = fin.read(buffer)) != -1) {
                fos.write(buffer, 0, length);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String intToIp(int i) {

        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    @OnClick({R.id.button, R.id.button2})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button:
                startFtpServer();
                Toast.makeText(this, "开启服务", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button2:
                stopFtp();
                Toast.makeText(this, "关闭服务", Toast.LENGTH_SHORT).show();
                break;
        }
    }




}
