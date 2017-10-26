package com.angiellorivas.androidsambatestapp.Network;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;

import com.angiellorivas.androidsambatestapp.Config;
import com.angiellorivas.androidsambatestapp.R;
import com.angiellorivas.androidsambatestapp.ServiceEvent;
import com.angiellorivas.androidsambatestapp.Utils;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

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
            SmbFile sFile;
            switch (bundle.getInt("action")){
                case Config.ACTION_TEST:
                    String url = "smb://"+ip+"/" + sharedFolder + "/" +fileName;
                    if(!TextUtils.isEmpty(user) && !TextUtils.isEmpty(pass)) {
                        sFile = new SmbFile(url, auth);
                    }else{
                        sFile = new SmbFile(url);
                    }
                    bundle.putString(KEY_RESULT, getContext().getString(R.string.canread) + " : " +String.valueOf(sFile.canRead()));
                    return bundle;
                case Config.ACTION_UPLOAD:
                    url = "smb://"+ip+"/" + sharedFolder + "/";
                    String path = url;
                    File filesPath = new File(Utils.getLocalPath(getContext()));
                    bundle.putString(KEY_RESULT, getContext().getString(R.string.uploaded) + " : " +String.valueOf(uploadFiles(fileName,filesPath,auth,path)));
                    return bundle;
                case Config.ACTION_DOWNLOAD:
                    url = "smb://"+ip+"/" + sharedFolder + "/" +fileName;
                    if(!TextUtils.isEmpty(user) && !TextUtils.isEmpty(pass)) {
                        sFile = new SmbFile(url, auth);
                    }else{
                        sFile = new SmbFile(url);
                    }
                    File destFile = new File("");
                    if(TextUtils.isEmpty(fileName)){
                        if(sFile.isDirectory()) {
                            destFile = getFolder(sFile);
                        }
                    }else if(sFile.exists()){
                        BufferedInputStream inBuf = new BufferedInputStream(sFile.getInputStream());
                        destFile = new File(Utils.getLocalPath(getContext()) + File.separator + sFile.getName());
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
                    bundle.putString(KEY_RESULT, getContext().getString(R.string.downloaded) + " : " +String.valueOf(destFile.exists()));
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

    private File getFolder(SmbFile folder) throws IOException {
        File lastFile = null;
        if(folder.isDirectory()) {
            SmbFile[] files = folder.listFiles();
            EventBus.getDefault().post(new ServiceEvent.TotalFiles(files.length));
            for(SmbFile smbFile : files){
                lastFile = getFile(smbFile);
            }
        }
        return lastFile;
    }

    private File getFile(SmbFile smbFile) throws IOException {
        if(smbFile.exists() && smbFile.isFile()) {
            BufferedInputStream inBuf = new BufferedInputStream(smbFile.getInputStream());
            File destFile = new File(Utils.getLocalPath(getContext()) + File.separator + smbFile.getName());
            OutputStream out = new FileOutputStream(destFile);

            // Copy the bits from Instream to Outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = inBuf.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            // Maybe in.close();
            out.close();
            EventBus.getDefault().post(new ServiceEvent.OneFileDownloaded(smbFile.getName()));
            return destFile;
        }else if(smbFile.exists() && smbFile.isDirectory()){
            getFolder(smbFile);
        }
        return null;
    }

    private List<File> getLocalListFiles(File parentDir) {
        ArrayList<File> inFiles = new ArrayList<>();
        File[] files = parentDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                inFiles.addAll(getLocalListFiles(file));
            } else {
                inFiles.add(file);
            }
        }
        EventBus.getDefault().post(new ServiceEvent.TotalFiles(inFiles.size()));
        return inFiles;
    }

    private boolean uploadFiles(String fileName, File filesPath, @Nullable NtlmPasswordAuthentication auth, String path) throws IOException {
        List<File>filesToUpload = new ArrayList<>();
        if(TextUtils.isEmpty(fileName)){
            filesToUpload = getLocalListFiles(filesPath);
        }
        File file = new File(Utils.getLocalPath(getContext()) + File.separator + fileName);
        filesToUpload.add(file);
        SmbFile sFile = null;
        for(File f : filesToUpload) {
            if (f.exists() && f.isFile()) {
                SmbFile sFilePath;
                if(auth != null) {
                    sFile = new SmbFile(path+f.getName(), auth);
                    sFilePath = new SmbFile(sFile.getParent(), auth);
                }else{
                    sFile = new SmbFile(path+f.getName());
                    sFilePath = new SmbFile(sFile.getParent());
                }
                if (!sFilePath.exists()) sFilePath.mkdirs();
                BufferedInputStream inBuf = new BufferedInputStream(new FileInputStream(f));
                final SmbFileOutputStream smbFileOutputStream = new SmbFileOutputStream(sFile);
                final byte[] buf = new byte[16 * 1024 * 1024];
                int len;
                while ((len = inBuf.read(buf)) > 0) {
                    smbFileOutputStream.write(buf, 0, len);
                    smbFileOutputStream.write(buf, 0, len);
                }
                inBuf.close();
                smbFileOutputStream.close();
                EventBus.getDefault().post(new ServiceEvent.OneFileUploaded(f.getName()));
            } else if(f.isDirectory()){
                filesToUpload.addAll(getLocalListFiles(new File(f.getAbsolutePath())));
            } else {
                throw new IOException();
            }
        }
        return sFile != null && sFile.exists();
    }
}
