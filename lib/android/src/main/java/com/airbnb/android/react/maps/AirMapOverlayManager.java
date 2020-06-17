package com.airbnb.android.react.maps;

import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.facebook.infer.annotation.Assertions;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javax.annotation.Nullable;

public class AirMapOverlayManager extends ViewGroupManager<AirMapOverlay> {
  private final DisplayMetrics metrics;
  private AirMapOverlay mapOverlayInstance;

  private static final int STEP = 1;

  public AirMapOverlayManager(ReactApplicationContext reactContext) {
    super();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      metrics = new DisplayMetrics();
      ((WindowManager) reactContext.getSystemService(Context.WINDOW_SERVICE))
          .getDefaultDisplay()
          .getRealMetrics(metrics);
    } else {
      metrics = reactContext.getResources().getDisplayMetrics();
    }
  }

  @Override
  public String getName() {
    return "AIRMapOverlay";
  }

  @Override
  public AirMapOverlay createViewInstance(ThemedReactContext context) {
    mapOverlayInstance = new AirMapOverlay(context);
    return mapOverlayInstance;
  }

  @ReactProp(name = "bounds")
  public void setBounds(AirMapOverlay view, ReadableArray bounds) {
    view.setBounds(bounds);
  }

  @ReactProp(name = "zIndex", defaultFloat = 1.0f)
  public void setZIndex(AirMapOverlay view, float zIndex) {
    view.setZIndex(zIndex);
  }

  @ReactProp(name = "opacity", defaultFloat = 1.0f)
  public void setOpacity(AirMapOverlay view, float opacity) {
    view.setTransparency(1 - opacity);
  }

  @ReactProp(name = "image")
  public void setImage(AirMapOverlay view, @Nullable String source) {
    view.setImage(source);
  }

  @ReactProp(name = "imageList")
  public void setImageList(AirMapOverlay view, @Nullable ReadableArray source) throws IOException { view.setImageList(source);}

  @ReactProp(name = "tappable", defaultBoolean = false)
  public void setTappable(AirMapOverlay view, boolean tapabble) {
    view.setTappable(tapabble);
  }

  public AirMapOverlay getMapOverlayInstance()
  {
    return mapOverlayInstance;
  }


  @Override
  public Map<String, Integer> getCommandsMap() {
//    Log.d("React"," View manager getCommandsMap:");
    return MapBuilder.of(
            "step",
            STEP);
  }

////  @androidx.annotation.Nullable
//  @Override
//  public Map getExportedCustomBubblingEventTypeConstants() {
//    return MapBuilder.builder()
//            .put(
//                    "topChange",
//                    MapBuilder.of(
//                            "phasedRegistrationNames",
//                            MapBuilder.of("bubbled", "onChange")))
//            .build();
//  }

  @Override
  public void receiveCommand(
          AirMapOverlay view,
          int commandType,
          @Nullable ReadableArray args) {
    Assertions.assertNotNull(view);
    Assertions.assertNotNull(args);
    switch (commandType) {
      case STEP: {
        view.increaseIndex();
//        view.onReceiveNativeEvent();
//        ((Callback)args).invoke(null, idx);
        return;
      }

      default:
        throw new IllegalArgumentException(String.format(
                "Unsupported command %d received by %s.",
                commandType,
                getClass().getSimpleName()));
    }
  }

//
//
//  @Override
//  public void receiveCommand(@NonNull AirMapOverlay root, String commandId, @androidx.annotation.Nullable ReadableArray args) {
//    super.receiveCommand(root, commandId, args);
//    switch (commandId) {
//      case "step": {
//        int idx = root.increaseIndex();
////        ((Callback)args).invoke(null, idx);
//        return;
//      }
//      default:
//        throw new IllegalArgumentException(String.format(
//                "Unsupported command %d received by %s.",
//                commandId,
//                getClass().getSimpleName()));
//    }
//  }



  @Override
  @Nullable
  public Map getExportedCustomDirectEventTypeConstants() {
    return MapBuilder.of(
        "onPress", MapBuilder.of("registrationName", "onPress")
    );
  }
}
