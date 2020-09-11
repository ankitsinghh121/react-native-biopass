package com.reactlibrary;

import android.Manifest.permission;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.CancellationSignal;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.UiThreadUtil;
import java.lang.Runnable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Executors;

public class RNBioPrompt extends ReactContextBaseJavaModule {

  public static class AuthenticateCallback {

    public void reject(final Throwable e) {}

    public void resolve() {}
  }

  private final ReactApplicationContext reactContext;
  private final String applicationLabel;
  private final String promptText;
  private BiometricPrompt.PromptInfo promptInfo;
  private Activity activity;
  private BiometricPrompt myBiometricPrompt;

  public RNBioPrompt(
    final ReactApplicationContext reactContext,
    final String promptText
  ) {
    super(reactContext);
    this.reactContext = reactContext;
    final Activity activity = reactContext.getCurrentActivity();
    this.activity = activity;
    final ApplicationInfo info = activity.getApplicationInfo();
    final PackageManager packageManager = activity.getPackageManager();
    this.applicationLabel = packageManager.getApplicationLabel(info).toString();
    this.promptText = promptText;
  }

  @Override
  public String getName() {
    return "RNBioPrompt";
  }

  public void authenticate(final AuthenticateCallback callback) {
    final Activity activity = this.activity;
    final String promptText = this.promptText;
    final String applicationLabel = this.applicationLabel;
    final FragmentActivity fragmentActivity = (FragmentActivity) this.activity;
    UiThreadUtil.runOnUiThread(
      new Runnable() {

        @Override
        public void run() {
          try {
            final CancellationSignal cancellationSignal = new CancellationSignal();
            Executor executor = ContextCompat.getMainExecutor(activity);
            final BiometricPrompt biometricPrompt = new BiometricPrompt(
              fragmentActivity,
              executor,
              new BiometricPrompt.AuthenticationCallback() {

                @Override
                public void onAuthenticationSucceeded(
                  BiometricPrompt.AuthenticationResult result
                ) {
                  super.onAuthenticationSucceeded(result);
                  callback.resolve();
                }

                @Override
                public void onAuthenticationFailed() {
                  super.onAuthenticationFailed();
                }

                @Override
                public void onAuthenticationError(
                  int errorCode,
                  CharSequence errString
                ) {
                  super.onAuthenticationError(errorCode, errString);
                  callback.reject(new Exception(errString.toString()));
                }
              }
            );
            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
              .setTitle("Fingerprint for \"" + applicationLabel + "\"")
              .setDescription(promptText)
              .setDeviceCredentialAllowed(true)
              .build();
            biometricPrompt.authenticate(promptInfo);
          } catch (Exception e) {}
        }
      }
    );
  }
}
