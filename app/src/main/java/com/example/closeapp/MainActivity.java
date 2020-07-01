package com.example.closeapp;

import androidx.appcompat.app.AppCompatActivity;

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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.acl.AclNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ListActivity {
    private PackageManager packageManager = null;
    private List applist = null;
    private AppAdapter listadapter = null;
    CheckBox checkBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        packageManager = getPackageManager();

        checkBox = findViewById(R.id.checkbox1);

        new LoadApplications().execute();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        ApplicationInfo app = (ApplicationInfo)applist.get(position);
//        if(checkBox.isChecked()) {
//            checkBox.setChecked(false);
//        } else {
//            checkBox.setChecked(true);
//
//            try {
//                InsertPackageNameIntoFile(app.name);
//            } catch (IOException e) {
//                e.printStackTrace();
//                Toast.makeText(getApplicationContext(),"Ошибка ввода/вывода", Toast.LENGTH_LONG).show();
//            }
//        }


//      Запуск приложения по нажатию - не нужно
        try {
            Intent intent = packageManager.getLaunchIntentForPackage(app.packageName);

            if (intent != null) {
                startActivity(intent);
            }
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getApplicationContext(),e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
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

    private boolean InsertPackageNameIntoFile(String PackageName) throws IOException {
        File f = new File(Environment.getExternalStorageState() + "/file");
        if(f.exists()) {
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            bw.write(PackageName);
            bw.close();
            Toast.makeText(getApplicationContext(),"Запись добавлена в файл: " +
                    Environment.getExternalStorageState() + "/file",Toast.LENGTH_LONG).show();
            return true;
        } else {
            Toast.makeText(getApplicationContext(),"Файл не создан : " +
                    Environment.getExternalStorageState() + "/file",Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private class LoadApplications extends AsyncTask<Void, Void, Void> {

        private ProgressDialog progress = null;

        @Override
        protected Void doInBackground(Void... params) {

            applist = checkForLaunchIntent(packageManager.getInstalledApplications(PackageManager.GET_META_DATA));

            listadapter = new AppAdapter(MainActivity.this, R.layout.list_item, applist);

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
