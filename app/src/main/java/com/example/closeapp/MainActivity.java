package com.example.closeapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Application;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.acl.AclNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainActivity extends ListActivity {
    private PackageManager packageManager = null;
    private List applist = null;
    private ListAdapter listadapter = null;
    private CheckBox checkBox = null;
    int REQUEST_CODE_PERMISSION, package_uid;

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

    public void InsertPackageNameIntoFile(String PackageName) throws IOException {
        File f1 = new File(Environment.getExternalStorageDirectory(), "file");
        if(!f1.exists()) {
            FileOutputStream f = new FileOutputStream(Environment.getExternalStorageDirectory() + "/file");
            f.close();
            Toast.makeText(getApplicationContext(),"Файл создан : " +
                    Environment.getExternalStorageState() + "/file",Toast.LENGTH_SHORT).show();
        }

        BufferedReader br = new BufferedReader(new FileReader(f1));
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

//            listadapter = new AppAdapter(MainActivity.this, android.R.layout.simple_list_item_checked, applist);
            listadapter = new ListAdapter() {
                private List<ApplicationInfo> appList = null;
                @Override
                public boolean areAllItemsEnabled() {
                    return false;
                }

                @Override
                public boolean isEnabled(int i) {
                    return false;
                }

                @Override
                public void registerDataSetObserver(DataSetObserver dataSetObserver) {

                }

                @Override
                public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

                }

                @Override
                public int getCount() {
                    return ((null != appList) ? appList.size() : 0);
                }

                @Override
                public Object getItem(int i) {
                    return (null != appList) ? appList.get(i) : null;
                }

                @Override
                public long getItemId(int i) {
                    return i;
                }

                @Override
                public boolean hasStableIds() {
                    return false;
                }

                @Override
                public View getView(int position, View convertView, ViewGroup viewGroup) {
                    View view = convertView;

                    if(null == view) {
                        LayoutInflater layoutInflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        view = layoutInflater.inflate(R.layout.list_item,null);
                    }

                    ApplicationInfo data = appList.get(position);
                    if(null != data) {
                        TextView appName = view.findViewById(R.id.app_name);
                        TextView packageName = view.findViewById(R.id.app_package);
//            TextView memory = view.findViewById(R.id.memory);
                        ImageView iconView = view.findViewById(R.id.app_icon);
                        CheckBox checkBox = view.findViewById(R.id.checkbox1);


                        appName.setText(data.loadLabel(packageManager));
                        packageName.setText(data.packageName);
                        try {
                            checkBox.setChecked(CheckCheckBox(data.packageName));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        checkBox.setOnCheckedChangeListener(click);
//            memory.setText(Long.toString(packageStats.cacheSize));
//            memory.setText(Long.toString(cacheSize));
                        iconView.setImageDrawable(data.loadIcon(packageManager));
                    }

                    return view;
                }

                public boolean CheckCheckBox(String packageName) throws FileNotFoundException {
                    //Проверить в файле есть ли такое название приложения??
                    File f = new File(Environment.getExternalStorageDirectory() + "/file");
                    try (Scanner scanner = new Scanner(f)) {
                        while (scanner.hasNextLine()) {
                            String line = scanner.nextLine();
                            if(packageName.equals(line)) {
                                return true;
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }

                    return false;
                }

                CompoundButton.OnCheckedChangeListener click = new CompoundButton.OnCheckedChangeListener() {
                    private Context context;

                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        context = compoundButton.getContext();
                        LinearLayout layout = (LinearLayout)compoundButton.getParent();
                        TextView textView = layout.findViewById(R.id.app_package);
                        String packageName = (String)textView.getText();

                        if (b) {
                            try {
                                insertPackageNameIntoFile(packageName);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                removePackageNameInFile(packageName);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    };

                    public void insertPackageNameIntoFile(String PackageName) throws IOException {
                        File f1 = new File(Environment.getExternalStorageDirectory(), "file");
                        if(!f1.exists()) {
                            FileOutputStream f = new FileOutputStream(Environment.getExternalStorageDirectory() + "/file");
                            f.close();
                            Toast.makeText(context,"Файл создан : " +
                                    Environment.getExternalStorageState() + "/file",Toast.LENGTH_SHORT).show();
                        }

                        BufferedReader br = new BufferedReader(new FileReader(f1));
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
                        Toast.makeText(context,"Запись добавлена в файл: " +
                                Environment.getExternalStorageState() + "/file",Toast.LENGTH_SHORT).show();
                    }

                    public void removePackageNameInFile(String PackageName) throws IOException {
                        File f1 = new File(Environment.getExternalStorageDirectory(), "file");
                        File f2 = new File(Environment.getExternalStorageDirectory(), "file.new");
                        if(!f2.exists()) {
                            FileOutputStream f = new FileOutputStream(Environment.getExternalStorageDirectory() + "/file.new");
                            f.close();
                        }
                        String line;
                        BufferedReader br = new BufferedReader(new FileReader(f1));
                        BufferedWriter bw = new BufferedWriter(new FileWriter(f2));
                        while ((line = br.readLine()) != null) {
                            if (!PackageName.contentEquals(line)) {
                                bw.write(line);
                                bw.newLine();
                            }
                        }
                        br.close();
                        bw.close();
                        f1.delete();
                        f2.renameTo(f1);

                        Toast.makeText(context,"Запись удалена из файла: " +
                                Environment.getExternalStorageState() + "/file",Toast.LENGTH_SHORT).show();
                    }
                };

                @Override
                public int getItemViewType(int i) {
                    return 0;
                }

                @Override
                public int getViewTypeCount() {
                    return 0;
                }

                @Override
                public boolean isEmpty() {
                    return false;
                }
            };

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
