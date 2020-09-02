package com.airbnb.android.react.maps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;

import androidx.annotation.MainThread;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.stream.Collectors;

public class AirMapOverlay extends AirMapFeature implements ImageReadable {

  private GroundOverlayOptions groundOverlayOptions;
  private GroundOverlay groundOverlay;
  private LatLngBounds bounds;
  private BitmapDescriptor iconBitmapDescriptor;
  private Bitmap iconBitmap;
  private boolean tappable;
  private float zIndex;
  private float transparency;
  public int imageIndex;
  private List<String> imageList;
  private Stack<String> iteratingImageList = new Stack<>();
  private ArrayList<Bitmap> overlayImageList = new ArrayList<Bitmap>();
  private AsyncTask<String, String, Bitmap> imageFetchTask;


  private final ImageReader mImageReader;
  private GoogleMap map;

  public AirMapOverlay(Context context) {
    super(context);
    this.mImageReader = new ImageReader(context, getResources(), this);
  }

  public void onReceiveNativeEvent() {
    try {
      WritableMap event = Arguments.createMap();
      event.putInt("idx", imageIndex);
      ReactContext reactContext = (ReactContext) getContext();
      reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
              getId(),
              "topChange",
              event);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public void setBounds(ReadableArray bounds) {
    LatLng sw = new LatLng(bounds.getArray(0).getDouble(0), bounds.getArray(0).getDouble(1));
    LatLng ne = new LatLng(bounds.getArray(1).getDouble(0), bounds.getArray(1).getDouble(1));
    this.bounds = new LatLngBounds(sw, ne);
    if (this.groundOverlay != null) {
      this.groundOverlay.setPositionFromBounds(this.bounds);
    }
  }

  public void setZIndex(float zIndex) {
    this.zIndex = zIndex;
    if (this.groundOverlay != null) {
      this.groundOverlay.setZIndex(zIndex);
    }
  }

  public void setTransparency(float transparency) {
      this.transparency = transparency;
      if (groundOverlay != null) {
          groundOverlay.setTransparency(transparency);
      }
  }

  public int increaseIndex() {

    imageIndex++;
    BitmapDescriptor bmpDesc = BitmapDescriptorFactory.defaultMarker();
    try {
      bmpDesc = BitmapDescriptorFactory.fromBitmap(overlayImageList.get(imageIndex));
      getGroundOverlayOptions().visible(true);
    }
    catch(IndexOutOfBoundsException oobException)
    {
      if (overlayImageList.size() > 0) {
        bmpDesc = BitmapDescriptorFactory.fromBitmap(overlayImageList.get(0));
      }
      imageIndex = 0;

    }
    if (groundOverlay != null)
      groundOverlay.setImage(bmpDesc);
    return imageIndex;
  }

  public void setImageList(ReadableArray uriImageList) throws IOException, InterruptedException {
    ArrayList<Object> arrayList = uriImageList.toArrayList();
    List<String> strings = new ArrayList<>(arrayList.size());
    for (Object object : arrayList) {
      strings.add(Objects.toString(object, null));
    }

    if (imageFetchTask != null)
    {
      imageFetchTask.cancel(true);
      imageFetchTask = null;
      Thread.sleep(100);
    }
    imageList = strings;
    for( Bitmap bmp: overlayImageList
         ) {
      bmp.recycle();
    }
    
    //Maybe show a short dialog, so we have time to clear?


    overlayImageList.clear();

    //start loading images one at a time starting here.
    //for (String image: imageList) {
      loadNext();
    //}
  }


  public void loadNext()
  {
//    imageList

    iteratingImageList.addAll(imageList);
    String url = iteratingImageList.remove(0);
    imageFetchTask = new LoadImage().execute(url);

//    AsyncTask<String, String, Bitmap> imageLoader = new LoadImage();
//    imageLoader.execute(url);

  }
  public void loadNext(Bitmap image)
  {
    //Maybe check a member variable here if it should load the next???
    if (image != null) {
      overlayImageList.add(image);

      GroundOverlayOptions gOpt = getGroundOverlayOptions();
      if (gOpt.getImage() == BitmapDescriptorFactory.defaultMarker() || gOpt.getImage() == null)
      {
        BitmapDescriptor bmpDesc = BitmapDescriptorFactory.fromBitmap(image);
        gOpt.image(bmpDesc);
        gOpt.visible(true);
      }
//      if (!gOpt.isVisible())
//      {
//
//        gOpt.visible(true);
//      }

      if (!iteratingImageList.empty())
      {
        String url = iteratingImageList.remove(0);
        imageFetchTask = new LoadImage().execute(url);
      }
    }
  }





  public void setImage(String uri) {
    this.mImageReader.setImage(uri);
  }

  public void setTappable(boolean tapabble) {
    this.tappable = tapabble;
    if (groundOverlay != null) {
      groundOverlay.setClickable(tappable);
    }
  }


  public GroundOverlayOptions getGroundOverlayOptions() {
    if (this.groundOverlayOptions == null) {
      this.groundOverlayOptions = createGroundOverlayOptions();
    }
    return this.groundOverlayOptions;
  }

  private GroundOverlayOptions createGroundOverlayOptions() {
    if (this.groundOverlayOptions != null) {
      return this.groundOverlayOptions;
    }
    GroundOverlayOptions options = new GroundOverlayOptions();
    if (this.overlayImageList != null && this.overlayImageList.size() > 0) {
      options.image(BitmapDescriptorFactory.fromBitmap(this.overlayImageList.get(0)));
    } else {
      // add stub image to be able to instantiate the overlay
      // and store a reference to it in MapView
      options.image(BitmapDescriptorFactory.defaultMarker());
      // hide overlay until real image gets added
      options.visible(false);
    }
    options.positionFromBounds(bounds);
    options.zIndex(zIndex);
    return options;
  }

  @Override
  public Object getFeature() {
    return groundOverlay;
  }

  @Override
  public void addToMap(GoogleMap map) {
    GroundOverlayOptions groundOverlayOptions = getGroundOverlayOptions();
    if (groundOverlayOptions != null) {
      this.groundOverlay = map.addGroundOverlay(groundOverlayOptions);
      this.groundOverlay.setClickable(this.tappable);
    } else {
      this.map = map;
    }
  }

  @Override
  public void removeFromMap(GoogleMap map) {
    this.map = null;
    if (this.groundOverlay != null) {
      this.groundOverlay.remove();
      this.groundOverlay = null;
      this.groundOverlayOptions = null;
    }
  }

  @Override
  public void setIconBitmap(Bitmap bitmap) {
    this.iconBitmap = bitmap;
  }

  @Override
  public void setIconBitmapDescriptor(
      BitmapDescriptor iconBitmapDescriptor) {
    this.iconBitmapDescriptor = iconBitmapDescriptor;
  }

  @Override
  public void update() {
    this.groundOverlay = getGroundOverlay();
    if (this.groundOverlay != null) {
      this.groundOverlay.setVisible(true);
      this.groundOverlay.setImage(this.iconBitmapDescriptor);
      this.groundOverlay.setTransparency(this.transparency);
      this.groundOverlay.setClickable(this.tappable);
    }
  }



  private GroundOverlay getGroundOverlay() {
    if (this.groundOverlay != null) {
      return this.groundOverlay;
    }
    if (this.map == null) {
      return null;
    }
    GroundOverlayOptions groundOverlayOptions = getGroundOverlayOptions();
    if (groundOverlayOptions != null) {
      return this.map.addGroundOverlay(groundOverlayOptions);
    }
    return null;
  }


  private class LoadImage extends AsyncTask<String, String, Bitmap> {
    Bitmap bitmap;
    Boolean isCanceled = false;
    @Override
    protected void onPreExecute() {
      super.onPreExecute();

    }
    @Override
    protected Bitmap doInBackground(String... args) {
      if (isCancelled() != true) {
        try {
//          bitmap = BitmapFactory.decodeStream((InputStream) new URL(args[0]).getContent());
          bitmap = BitmapFactory.decodeStream(new URL(args[0]).openStream());
//        overlayImageList.add(bitmap);
        }
        catch (IOException ioEx)
        {
          ioEx.printStackTrace();
        }
        catch (Exception e) {
          e.printStackTrace();

        }
        return bitmap;
      }
      else return null;
    }
    @Override
    protected void onPostExecute(Bitmap image) {
      if (!isCancelled()) {

        if (image != null) {
          loadNext(image);
        }
      }
    }
  }
}

