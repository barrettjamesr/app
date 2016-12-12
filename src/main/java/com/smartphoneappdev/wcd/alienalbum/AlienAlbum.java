package com.smartphoneappdev.wcd.alienalbum;

import android.app.Application;

public class AlienAlbum extends Application {

    public boolean blnLoggedIn;
    public int intUserID;
    public String strUserName;

    private static AlienAlbum mInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
    }

    public static synchronized AlienAlbum getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }

}
