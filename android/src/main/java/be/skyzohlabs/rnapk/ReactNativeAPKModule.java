package be.skyzohlabs.rnapk;

import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.ApplicationInfo;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Binder;
import androidx.core.content.FileProvider;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.File;

import javax.annotation.Nullable;

public class ReactNativeAPKModule extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;

  public ReactNativeAPKModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "ReactNativeAPK";
  }

  @ReactMethod
  public void isAppInstalled(String packageName, Callback cb) {
    try {
      PackageInfo pInfo = this.reactContext.getPackageManager().getPackageInfo(packageName,
          PackageManager.GET_ACTIVITIES);

      cb.invoke(true);
    } catch (PackageManager.NameNotFoundException e) {
      cb.invoke(false);
    }
  }

  @ReactMethod
  public void installApp(String packagePath) {
    File file = new File(packagePath);
    if (Build.VERSION.SDK_INT >= 24) {
      Uri apkUri = FileProvider.getUriForFile(this.reactContext, "com.logisticsdriverapp.fileprovider", file);
      Intent install = new Intent(Intent.ACTION_VIEW);
      install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//添加这一句表示对目标应用临时授权该Uri所代表的文件
      install.setDataAndType(apkUri, "application/vnd.android.package-archive");
      this.reactContext.startActivity(install);
      return;
    } else {
      String cmd = "chmod 777 " +packagePath;
      try {
        Runtime.getRuntime().exec(cmd);
      } catch (Exception e) {
        e.printStackTrace();
      }
      Intent installIntent = new Intent(Intent.ACTION_VIEW);
      installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      installIntent.setDataAndType(Uri.parse("file://" + packagePath),"application/vnd.android.package-archive");
      this.reactContext.startActivity(installIntent);
    }
  }

  @ReactMethod
  public void uninstallApp(String packageName, Callback cb) {
    Intent intent = new Intent(Intent.ACTION_DELETE);
    intent.setData(Uri.parse("package:" + packageName));
    this.reactContext.startActivity(intent);
    cb.invoke(true);
  }

  @ReactMethod
  public void getAppVersion(String packageName, Callback cb) {
    try {
      PackageInfo pInfo = this.reactContext.getPackageManager().getPackageInfo(packageName, 0);

      cb.invoke(pInfo.versionName);
    } catch (PackageManager.NameNotFoundException e) {
      cb.invoke(false);
    }
  }

  @ReactMethod
  public void getApps(Callback cb) {
    List<PackageInfo> packages = this.reactContext.getPackageManager().getInstalledPackages(0);

    List<String> ret = new ArrayList<>();
    for (final PackageInfo p : packages) {
      ret.add(p.packageName);
    }
    cb.invoke(ret);
  }

  @ReactMethod
  public void getNonSystemApps(Callback cb) {
    List<PackageInfo> packages = this.reactContext.getPackageManager().getInstalledPackages(0);

    List<String> ret = new ArrayList<>();
    for (final PackageInfo p : packages) {
      if ((p.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
        ret.add(p.packageName);
      }
    }
    cb.invoke(ret);
  }

  @ReactMethod
  public void runApp(String packageName) {
    // TODO: Allow to pass Extra's from react.
    Intent launchIntent = this.reactContext.getPackageManager().getLaunchIntentForPackage(packageName);
    //launchIntent.putExtra("test", "12331");
    this.reactContext.startActivity(launchIntent);
  }

  /*@Override
  public @Nullable Map<String, Object> getConstants() {
      Map<String, Object> constants = new HashMap<>();
  
      constants.put("getApps", getApps());
      constants.put("getNonSystemApps", getNonSystemApps());
      return constants;
  }*/
}