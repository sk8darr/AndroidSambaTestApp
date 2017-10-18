package com.angiellorivas.androidsambatestapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatEditText;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.angiellorivas.androidsambatestapp.Network.FileLoader;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, LoaderManager.LoaderCallbacks<Bundle>{

    private final int PERMISSION_REQUEST = 101;
    private AppCompatEditText etIp,etUser,etPass,etFilename,etShared,etWg;
    private MaterialDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPermissions();
        etWg = findViewById(R.id.etWg);
        etIp = findViewById(R.id.etIp);
        etUser = findViewById(R.id.etUser);
        etPass = findViewById(R.id.etPassword);
        etFilename = findViewById(R.id.etFileName);
        etShared = findViewById(R.id.etShared);
        AppCompatCheckBox cBox = findViewById(R.id.checkBox);

        cBox.setChecked(Utils.getCheck(this));
        if(cBox.isChecked()) {
            etWg.setText(Utils.getWorkgroup(this));
            etIp.setText(Utils.getIp(this));
            etUser.setText(Utils.getUser(this));
            etPass.setText(Utils.getPass(this));
            etShared.setText(Utils.getFolder(this));
            etFilename.setText(Utils.getFileName(this));
        }

        findViewById(R.id.btSend).setOnClickListener(this);
        findViewById(R.id.btUpload).setOnClickListener(this);
        findViewById(R.id.btDownload).setOnClickListener(this);
        cBox.setOnCheckedChangeListener(this);

        mProgressDialog = new MaterialDialog.Builder(this)
                .title(R.string.loading)
                .cancelable(false)
                .content(R.string.wait)
                .progress(true, 0)
                .theme(Theme.LIGHT)
                .build();
    }

    @Override
    public void onClick(View view) {
        Utils.setWorkgroup(etWg.getText().toString(),MainActivity.this);
        Utils.setIp(etIp.getText().toString(),MainActivity.this);
        Utils.setUser(etUser.getText().toString(),MainActivity.this);
        Utils.setPass(etPass.getText().toString(),MainActivity.this);
        Utils.setFolder(etShared.getText().toString(),MainActivity.this);
        Utils.setFileName(etFilename.getText().toString(),MainActivity.this);

        Bundle bundle = new Bundle();
        bundle.putString("wg",etWg.getText().toString());
        bundle.putString("ip",etIp.getText().toString());
        bundle.putString("user",etUser.getText().toString());
        bundle.putString("pass",etPass.getText().toString());
        bundle.putString("shared",etShared.getText().toString());
        bundle.putString("fileName",etFilename.getText().toString());
        switch (view.getId()){
            case R.id.btSend:
                bundle.putInt("action", 0);
                break;
            case R.id.btUpload:
                bundle.putInt("action", 1);
                break;
            case R.id.btDownload:
                bundle.putInt("action", 2);
                break;
        }
        if(bundle.containsKey("action")){
            showProgressDialog();
            getSupportLoaderManager().restartLoader(0,bundle,this).forceLoad();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        Utils.setCheck(b, MainActivity.this);
    }

    @Override
    public Loader<Bundle> onCreateLoader(int i, Bundle bundle) {
        return new FileLoader(MainActivity.this, bundle);
    }

    @Override
    public void onLoadFinished(Loader<Bundle> loader, Bundle data) {
        Toast.makeText(MainActivity.this, data.getString(FileLoader.KEY_RESULT),Toast.LENGTH_LONG).show();
        dismissProgressDialog();
    }

    @Override
    public void onLoaderReset(Loader<Bundle> loader) {

    }

    private void showProgressDialog(){
        if(mProgressDialog != null && !mProgressDialog.isShowing()){
            mProgressDialog.show();
        }
    }

    private void dismissProgressDialog(){
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                try{
                    if(mProgressDialog != null){
                        mProgressDialog.dismiss();
                    }
                }catch (IllegalArgumentException ex){
                    ex.printStackTrace();
                    mProgressDialog.setCancelable(true);
                }
            }
        });
    }

    private void getPermissions(){
        // Assume thisActivity is the current activity
        int permissionCheckW = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionCheckR = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        // Here, thisActivity is the current activity
        if (permissionCheckR != PackageManager.PERMISSION_GRANTED || permissionCheckW != PackageManager.PERMISSION_GRANTED ) {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(MainActivity.this,"OK",Toast.LENGTH_SHORT).show();

                } else {
                    getPermissions();
                }
            }
        }
    }
}
