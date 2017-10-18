package com.angiellorivas.androidsambatestapp.Network;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;

import com.angiellorivas.androidsambatestapp.Utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;

/**
 * @author sk8 on 25/09/17.
 */

public class FileLoader extends AsyncTaskLoader<Bundle> {

    private Bundle bundle;
    public static final String KEY_RESULT = "result";

    public FileLoader(Context context, Bundle bundle) {
        super(context);
        this.bundle = bundle;
    }

    @Override
    public Bundle loadInBackground() {
        try {
            String wg = bundle.getString("wg").trim();
            String ip = bundle.getString("ip").trim();
            String user = bundle.getString("user").trim();
            String pass = bundle.getString("pass").trim();
            String sharedFolder= bundle.getString("shared").trim();
            String fileName = bundle.getString("fileName").trim();
            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(wg, user, pass);
            SmbFile sFile = null;
            switch (bundle.getInt("action")){
                case 0:
                    String url = "smb://"+ip+"/" + sharedFolder + "/" +fileName;
                    if(!TextUtils.isEmpty(user) && !TextUtils.isEmpty(pass)) {
                        sFile = new SmbFile(url, auth);
                    }else{
                        sFile = new SmbFile(url);
                    }
                    if(sFile.isDirectory()) {
                        SmbFile[] files = sFile.listFiles();
                        for(SmbFile smbFile : files){
                            System.out.println(smbFile.getCanonicalPath());
                        }
                    }
                    bundle.putString(KEY_RESULT, String.valueOf(sFile.canRead()));
                    return bundle;
                case 1:
                    url = "smb://"+ip+"/" + sharedFolder + "/";
                    String path = url + fileName;
                    sFile = new SmbFile(path, auth);
                    SmbFile sFilePath = new SmbFile(sFile.getParent(), auth);
                    if (!sFilePath.exists()) sFilePath.mkdirs();
                    File file = new File(Utils.getLocalPath()+File.separator+fileName);
                    if(file.exists()) {
                        BufferedInputStream inBuf = new BufferedInputStream(new FileInputStream(file));
                        final SmbFileOutputStream smbFileOutputStream = new SmbFileOutputStream(sFile);
                        final byte[] buf = new byte[16 * 1024 * 1024];
                        int len;
                        while ((len = inBuf.read(buf)) > 0) {
                            smbFileOutputStream.write(buf, 0, len);
                        }
                        inBuf.close();
                        smbFileOutputStream.close();
                    }else{
                        throw new IOException();
                    }
                    bundle.putString(KEY_RESULT, String.valueOf(sFile.exists()));
                    return bundle;
                case 2:
                    url = "smb://"+ip+"/" + sharedFolder + "/" +fileName;
                    if(!TextUtils.isEmpty(user) && !TextUtils.isEmpty(pass)) {
                        sFile = new SmbFile(url, auth);
                    }else{
                        sFile = new SmbFile(url);
                    }
                    File destFile = new File("");
                    if(sFile.exists()){
                        BufferedInputStream inBuf = new BufferedInputStream(sFile.getInputStream());
                        destFile = new File(Utils.getLocalPath() + File.separator + sFile.getName());
                        OutputStream out = new FileOutputStream(destFile);

                        // Copy the bits from Instream to Outstream
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = inBuf.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                        // Maybe in.close();
                        out.close();
                    }
                    bundle.putString(KEY_RESULT, String.valueOf(destFile.exists()));
                    return bundle;
            }

        } catch (Exception e) {
            // Output the stack trace.
            e.printStackTrace();
            bundle.putString(KEY_RESULT, e.toString());
            return bundle;
        }
        return null;
    }
}
