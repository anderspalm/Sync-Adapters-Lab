package drewmahrt.generalassemb.ly.investingportfolio;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.Objects;

/**
 * Created by ander on 8/22/2016.
 */
public class MySyncService extends Service {

    private static SyncAdapter sSyncAdapter = null;
    public static final Object sSyncLock = new Object();

    @Override
    public void onCreate() {
        synchronized (sSyncLock){
            if(sSyncAdapter == null){
                sSyncAdapter = new SyncAdapter(getApplicationContext(),true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }


}
