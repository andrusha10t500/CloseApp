package com.example.closeapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Application;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.acl.AclNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ListActivity {
    private PackageManager packageManager = null;
    private List applist = null;
    private AppAdapter listadapter = null;
    int REQUEST_CODE_PERMISSION, package_uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        packageManager = getPackageManager();

//        checkBox = findViewById(R.id.checkbox1);

        new LoadApplications().execute();
    }



    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        ApplicationInfo app = (ApplicationInfo)applist.get(position);
        package_uid = app.uid;
        Toast.makeText(getApplicationContext(),packageManager.getNameForUid(app.uid),Toast.LENGTH_LONG).show();

        int permissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        try {

            if(permissionStatus == PackageManager.PERMISSION_GRANTED) {
                InsertPackageNameIntoFile(packageManager.getNameForUid(app.uid));
            } else {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

//        try{
//            Intent intent = packageManager.getLaunchIntentForPackage(app.packageName);
//
//            if(intent != null) {
//                startActivity(intent);
//            }
//        } catch(ActivityNotFoundException e) {
//            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
//        } catch(Exception e) {
//            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0 :
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        InsertPackageNameIntoFile(packageManager.getNameForUid(package_uid));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Права " + permissions[0] + " не были предоставлены.", Toast.LENGTH_SHORT).show();
                }
        }
    }



    private List checkForLaunchIntent(List<ApplicationInfo> list) {

        ArrayList appList = new ArrayList();

        for(ApplicationInfo info : list) {
            try{
                if(packageManager.getLaunchIntentForPackage(info.packageName) != null) {
                    appList.add(info);
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        return appList;
    }

    private void InsertPackageNameIntoFile(String PackageName) throws IOException {
        File f1 = new File(Environment.getExternalStorageDirectory() + "/file");
        if(!f1.exists()) {
            FileOutputStream f = new FileOutputStream(Environment.getExternalStorageDirectory() + "/file");
            f.close();
            Toast.makeText(getApplicationContext(),"Файл создан : " +
                    Environment.getExternalStorageState() + "/file",Toast.LENGTH_SHORT).show();
        }

        BufferedReader br = new BufferedReader(new FileReader(f1.getAbsoluteFile()));
        String string = br.readLine();
        StringBuilder result = new StringBuilder();
        while (string != null) {
            result.append(string).append("\n");
            string = br.readLine();
        }
        br.close();
        BufferedWriter bw = new BufferedWriter(new FileWriter(f1));
        result.append(PackageName).append("\n");
        bw.append(result);

        bw.close();
        Toast.makeText(getApplicationContext(),"Запись добавлена в файл: " +
                Environment.getExternalStorageState() + "/file",Toast.LENGTH_SHORT).show();
    }

    public void Click_button(View view) {
        Toast.makeText(getApplicationContext(), "вошёл", Toast.LENGTH_LONG).show();
        try {
            Process p = Runtime.getRuntime().exec("su\n");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("closeapp " + Environment.getExternalStorageDirectory() + "/file\n");
            os.writeBytes("exit\n");
            os.flush();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "не вышло: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private class LoadApplications extends AsyncTask<Void, Void, Void> {

        private ProgressDialog progress = null;

        @Override
        protected Void doInBackground(Void... params) {

            applist = checkForLaunchIntent(packageManager.getInstalledApplications(PackageManager.GET_META_DATA));

            listadapter = new AppAdapter(MainActivity.this, android.R.layout.simple_list_item_checked, applist);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            setListAdapter(listadapter);
            progress.dismiss();
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(MainActivity.this, null, "Loading apps info...");
            super.onPreExecute();
        }
    }
}
