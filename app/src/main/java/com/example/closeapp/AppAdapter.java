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
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;
import java.util.Scanner;

public class AppAdapter extends ArrayAdapter {
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
//            CheckBox checkBox = view.findViewById(R.id.checkbox1);


            appName.setText(data.loadLabel(packageManager));
            packageName.setText(data.packageName);
//            checkBox.setChecked(CheckCheckBox(data.packageName));
//            memory.setText(Long.toString(packageStats.cacheSize));
//            memory.setText(Long.toString(cacheSize));
            iconView.setImageDrawable(data.loadIcon(packageManager));
        }

        return view;
    }
    public boolean CheckCheckBox(String packageName){
        //Проверить в файле есть ли такое название приложения??
        File f = new File(Environment.getExternalStorageState() + "/file");
        try (Scanner scanner = new Scanner(f)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] cols = line.split(" ");
                if(cols[0].equals(packageName)) {
                    return true;
                } else {
                    return false;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return false;
    }

}
