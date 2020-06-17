package com.airbnb.android.react.maps;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;


public class AirMapOverlayModule extends ReactContextBaseJavaModule {

    private AirMapOverlay mapOverlayInstance;
    private AirMapOverlayManager mapOverlayManager;

    public AirMapOverlayModule(ReactApplicationContext reactContext, AirMapOverlayManager _mapOverlayManager)
    {
        super(reactContext);
        if (_mapOverlayManager != null)
        {
            mapOverlayManager = _mapOverlayManager;
            mapOverlayInstance = _mapOverlayManager.getMapOverlayInstance();
        }
    }


    @NonNull
    @Override
    public String getName() {
        return "AndroidOverlayModule";
    }

    @ReactMethod
    public void getIdx(Callback callback)
    {
        if (mapOverlayInstance != null) {
            mapOverlayInstance = mapOverlayManager.getMapOverlayInstance();
            int idx = mapOverlayInstance.imageIndex;
            callback.invoke(null, idx);
        }
        else
        {
            mapOverlayInstance = mapOverlayManager.getMapOverlayInstance();
            if (mapOverlayInstance != null)
            {
                int idx = mapOverlayInstance.imageIndex;
                callback.invoke(null, idx);
            }
            else {
                callback.invoke(null, 0);
            }
        }
    }

    @ReactMethod
    public void step(final Callback callback)
    {
        if (mapOverlayInstance != null) {
            mapOverlayInstance = mapOverlayManager.getMapOverlayInstance();
            getCurrentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int idx = mapOverlayInstance.increaseIndex();
                    callback.invoke(null, idx);
                }
            });

        }
        else
        {
            mapOverlayInstance = mapOverlayManager.getMapOverlayInstance();
            if (mapOverlayInstance != null)
            {
                getCurrentActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int idx = mapOverlayInstance.increaseIndex();
                        callback.invoke(null, idx);
                    }
                });
            }
            else {
                callback.invoke(null, 0);
            }
        }
    }
}
