package com.athasamid.sample.pdf.viewer;

import android.Manifest;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.DownloadListener;
import com.androidnetworking.interfaces.DownloadProgressListener;
import com.github.barteksc.pdfviewer.PDFView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private PDFView pdfViewer;
    private TextView info;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pdfViewer = findViewById(R.id.pdfViewer);
        info = findViewById(R.id.info);

        // Request Runtime Permissions for Android Marshmellow or above
        requestPermissions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,0,0, "Load").setIcon(android.R.drawable.ic_menu_search).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 0)
            showDialogUrl();
        return super.onOptionsItemSelected(item);
    }

    private void requestPermissions() {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                    }
                })
                .check();
    }


    private void showDialogUrl(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.layout_input_url, null);
        builder.setNegativeButton("Batal", null)
                .setView(dialogView);

        final EditText url = dialogView.findViewById(R.id.url);

        builder.setPositiveButton("Load", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                downloadAndViewPdf(url.getText().toString());
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void downloadAndViewPdf(String url){
        // Get filename from url ex: https://athasamid.com/files/presentation.pdf
        // it will return 'presentation.pdf'
        Pattern pattern = Pattern.compile("[^/]*$");
        Matcher matcher = pattern.matcher(url);
        
        //check if pattern didn't match so return from this function
        if (!matcher.find())
            return;

        // assign filename from regex to variable
        final String filename = matcher.group();

        Log.e("filename", filename);
        
        // Getting file from local storage
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), filename);

        // Check if file not exist then download it first, otherwise view pdf
        if (!file.exists()){
            pdfViewer.setVisibility(View.GONE);
            info.setVisibility(View.VISIBLE);
            AndroidNetworking.download(url, getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath(), filename)
                    .setPriority(Priority.MEDIUM)
                    .setTag("Downloading Pdf")
                    .build()
                    .setDownloadProgressListener(new DownloadProgressListener() {
                        @Override
                        public void onProgress(long bytesDownloaded, long totalBytes) {
                            // Show loading progress
                            info.setText("Sedang mengunduh\n"+((int) ((bytesDownloaded*100)/totalBytes))+"%");
                        }
                    })
                    .startDownload(new DownloadListener() {
                        @Override
                        public void onDownloadComplete() {
                            File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), filename);
                            pdfViewer.fromFile(file).load();
                            info.setVisibility(View.GONE);
                            pdfViewer.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(ANError anError) {
                            Log.e("error", anError.getErrorBody());
                            info.setVisibility(View.GONE);
                        }
                    });
        } else {
            pdfViewer.fromFile(file).load();
        }
    }

}
