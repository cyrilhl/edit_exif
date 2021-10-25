package com.peersafe.edit_exif;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

import androidx.exifinterface.media.ExifInterface;

import java.lang.reflect.Field;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** FlutterExifPlugin */
public class FlutterExifPlugin implements FlutterPlugin, MethodCallHandler  {
  /** Plugin registration. */
  private Result result;
  private MethodCall call;
  private MethodChannel channel;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "edit_exif");
    channel.setMethodCallHandler(this);
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "edit_exif");
    final FlutterExifPlugin plugin = new FlutterExifPlugin();
    channel.setMethodCallHandler(plugin);
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    this.call = call;
    this.result = result;
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    } else if (call.method.equals("setExif")) {
      setExif();
    } else if (call.method.equals("getExif")) {
      getExif();
    } else {
      result.notImplemented();
    }
  }


  public void setExif() {
    String filepath = call.argument("path");
    Map<String,String> map = call.argument("exif");
    try {
      ExifInterface exif = new ExifInterface(filepath); // 根据图片的路径获取图片的Exif
      for(String key:map.keySet()){
        Field staticfield = ExifInterface.class.getDeclaredField(key);
        exif.setAttribute(staticfield.get(null).toString(), map.get(key)); 
      } 
      // 把纬度写进MODEL
      exif.saveAttributes();
      result.success(null); // 最后保存起来
    } catch (Exception e) {
      result.error("error", "IOexception", e);
      e.printStackTrace();
    }
  }


  public void getExif() {
    String filepath = call.argument("path");
    String key = call.argument("key");
    try {
      ExifInterface exif = new ExifInterface(filepath);
      Field staticfield = ExifInterface.class.getDeclaredField(key);
      String value = exif.getAttribute(staticfield.get(null).toString());
      result.success(value);
    } catch (Exception e) {
      result.error("error", "IOexception", null);
      e.printStackTrace();
    }
  }

}
