package com.angiellorivas.androidsambatestapp;

import android.Manifest;
import android.content.Intent;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.angiellorivas.androidsambatestapp.Network.FileLoader;

import net.rdrei.android.dirchooser.DirectoryChooserActivity;
import net.rdrei.android.dirchooser.DirectoryChooserConfig;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, LoaderManager.LoaderCallbacks<Bundle>{

    private final int PERMISSION_REQUEST = 101;
    private final int PICKFILE_REQUEST_CODE = 102;
    private AppCompatEditText etIp,etUser,etPass,etFilename,etShared,etWg;
    private MaterialDialog mProgressDialog;
    private MaterialDialog progress;
    private int fileCounter = 1;
    private int maxFileCounter = 10;
    private int actualFileCounter = 0;


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

        progress = new MaterialDialog.Builder(this)
                .title(R.string.uploading)
                .contentGravity(GravityEnum.CENTER)
                .canceledOnTouchOutside(false)
                .cancelable(false)
                .theme(Theme.LIGHT)
                .progress(false, actualFileCounter, true)
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
                bundle.putInt("action", Config.ACTION_TEST);
                break;
            case R.id.btUpload:
                bundle.putInt("action", Config.ACTION_UPLOAD);
                break;
            case R.id.btDownload:
                bundle.putInt("action", Config.ACTION_DOWNLOAD);
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
        dismissProgressListDialog();

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

    private void dismissProgressListDialog(){
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                try{
                    if(progress != null){
                        progress.dismiss();
                    }
                }catch (IllegalArgumentException ex){
                    ex.printStackTrace();
                    progress.setCancelable(true);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.default_folder:
                final Intent chooserIntent = new Intent(this, DirectoryChooserActivity.class);

                final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
                        .newDirectoryName("DirChooserSample")
                        .allowReadOnlyDirectory(true)
                        .allowNewDirectoryNameModification(true)
                        .build();

                chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_CONFIG, config);
                // REQUEST_DIRECTORY is a constant integer to identify the request, e.g. 0
                startActivityForResult(chooserIntent, PICKFILE_REQUEST_CODE);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICKFILE_REQUEST_CODE) {
            if (resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {
                String path = (data.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR));
                Utils.setLocalPath(this, path);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ServiceEvent.OneFileDownloaded e) {
        if(mProgressDialog != null)mProgressDialog.dismiss();
        if(!progress.isShowing())progress.show();
        if(fileCounter >= actualFileCounter) {
            fileCounter = 1;
        } else {
            fileCounter++;
            progress.incrementProgress(1);
            progress.setContent(e.getFilename());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ServiceEvent.OneFileUploaded e) {
        if(mProgressDialog != null)mProgressDialog.dismiss();
        if(!progress.isShowing())progress.show();
        if(fileCounter >= actualFileCounter) {
            fileCounter = 1;
        } else {
            fileCounter++;
            progress.incrementProgress(1);
            progress.setContent(e.getFilename());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ServiceEvent.TotalFiles e) {
        actualFileCounter = e.getTotal();
        fileCounter = 1;
        progress.setProgress(fileCounter);
        progress.setMaxProgress(actualFileCounter);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}
