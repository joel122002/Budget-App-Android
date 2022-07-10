package com.cr7.budgetapp;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.parse.Parse;


public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ApplicationInfo info = null;
        try {
            info = getApplicationContext().getPackageManager().getApplicationInfo(getApplicationContext().getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        assert info != null;
        String APP_ID = info.metaData.getString("APP_ID");
        String CLIENT_KEY = info.metaData.getString("CLIENT_KEY");
        String SERVER = info.metaData.getString("SERVER");
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(APP_ID)
                // if defined
                .clientKey(CLIENT_KEY)
                .server(SERVER)
                .build()
        );
    }
}
