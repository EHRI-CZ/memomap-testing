package cz.deepvision.iti.is;

import android.app.Application;

import com.google.android.gms.maps.model.LatLng;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class BaseApp extends Application {
    static LatLng gpsLocation = new LatLng(50.088780, 14.419094);
    static boolean gpsEnabled = false;
    static boolean timeLapseEnabled = false;
    static Boolean[] filters = new Boolean[]{true, true, true};


    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        Realm.deleteRealm(Realm.getDefaultConfiguration());
        RealmConfiguration  realmConfiguration = new RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build();
        Realm.setDefaultConfiguration(realmConfiguration);
    }

    public static LatLng getGpsLocation() {
        return gpsLocation;
    }

    public static void setGpsLocation(LatLng gpsLocation) {
        BaseApp.gpsLocation = gpsLocation;
    }

    public static boolean isGpsEnabled() {
        return gpsEnabled;
    }

    public static void setGpsEnabled(boolean gpsEnabled) {
        BaseApp.gpsEnabled = gpsEnabled;
    }

    public static boolean isTimeLapseEnabled() {
        return timeLapseEnabled;
    }

    public static void setTimeLapseEnabled(boolean timeLapseEnabled) {
        BaseApp.timeLapseEnabled = timeLapseEnabled;
    }

    public static Boolean[] getFilters() {
        return filters;
    }

    public static void setFilters(Boolean[] filters) {
        BaseApp.filters = filters;
    }
}
