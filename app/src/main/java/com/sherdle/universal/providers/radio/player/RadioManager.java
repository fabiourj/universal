package com.sherdle.universal.providers.radio.player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.sherdle.universal.providers.radio.RadioStream;
import com.sherdle.universal.providers.radio.StaticEventDistributor;
import com.sherdle.universal.util.Log;

public class RadioManager {

    private static RadioManager instance = null;

    private static RadioService service;

    private boolean serviceBound;

    private RadioManager() {
        serviceBound = false;
    }

    public static RadioManager with() {

        if (instance == null)
            instance = new RadioManager();

        return instance;
    }

    public static RadioService getService(){
        return service;
    }

    public void playOrPause(RadioStream stream){

        if (stream == null)
            service.stop();
        else
            service.playOrPause(stream);

    }

    public boolean isPlaying() {

        return service.isPlaying();
    }

    public void bind(Context context) {

        //Perhaps also catch a LeakedServiceConnection, and if caught: then call unbind and then try to bind again
        if (!serviceBound) {
            Intent intent = new Intent(context, RadioService.class);
            context.startService(intent);

            boolean bound = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

            if (service != null)
                StaticEventDistributor.onEvent(service.getStatus());
        }
    }

    public void unbind(Context context) {

        if (serviceBound) {
            try {
                service.stop();
                context.unbindService(serviceConnection);
                context.stopService(new Intent(context, RadioService.class));
                serviceBound = false;
            } catch (IllegalArgumentException e){
                Log.printStackTrace(e);
            }
        }

    }

    public boolean isBound(){
        return serviceBound;
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName arg0, IBinder binder) {

            service = ((RadioService.LocalBinder) binder).getService();
            serviceBound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

            serviceBound = false;
        }
    };

}
