package com.example.closeapp;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Scanner;

public class AppAdapter extends ArrayAdapter implements Serializable {
    private List<ApplicationInfo> appList = null;
    private Context context;
    private PackageManager packageManager = null;
    private long cacheSize, dataSize, apkSize, size;

//    private PackageStats packageStats = null;


    public AppAdapter(Context context, int resource, List objects) {
        super(context, resource, objects);

        this.context = context;
        this.appList = objects;
        packageManager = context.getPackageManager();

    }

    @Override
    public int getCount() {
        return ((null != appList) ? appList.size() : 0);
    }

    @Override
    public ApplicationInfo getItem(int position) {
        return (null != appList) ? appList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if(null == view) {
            LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.list_item,null);
        }

        ApplicationInfo data = appList.get(position);

//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            StorageStatsManager storageStatsManager = (StorageStatsManager) context.getSystemService(Context.STORAGE_STATS_SERVICE);
//            StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
//            try {
//                StorageStats storageStats = storageStatsManager.queryStatsForUid(data.storageUuid,data.uid);
//                cacheSize = storageStats.getCacheBytes();
//                dataSize = storageStats.getDataBytes();
//                apkSize = storageStats.getAppBytes();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        else {
//            //-------------до-------------
//            PackageStats packageStats = new PackageStats(context.getPackageName()); //Устарел на 26 (у меня 7й андроид - 24)
//            cacheSize = packageStats.cacheSize;
//            //-------------после-------------
//            CheckBox checkBox = view.findViewById(R.id.checkbox1);
//            if (checkBox.isChecked()) {
//                android.os.Process.killProcess(data.uid);
//            }
//        }

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
}
