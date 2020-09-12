package com.reactlibrary;

import android.app.Activity;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.lang.Math;
import java.lang.Runnable;

public class RNBioPassDialog
  extends BottomSheetDialog
  implements ActivityEventListener {

  public static class AuthenticateCallback {

    public void reject(Throwable e) {}

    public void resolve() {}
  }

  private ReactApplicationContext reactContext;
  private CancellationSignal cancellationSignal;
  private KeyguardManager mKeyguardManager;
  private Context context;
  private Activity activity;
  private AuthenticateCallback callback;
  private ImageView icon;

  @Override
  public void onNewIntent(Intent intent) {}

  private static ColorStateList themeAttributeToColorStateList(
    int themeAttributeId,
    Context context,
    int fallbackColorId
  ) {
    TypedValue outValue = new TypedValue();
    Resources.Theme theme = context.getTheme();
    boolean wasResolved = theme.resolveAttribute(
      themeAttributeId,
      outValue,
      true
    );

    if (wasResolved == false) return ContextCompat.getColorStateList(
      context,
      fallbackColorId
    );
    if (outValue.resourceId == 0) return ColorStateList.valueOf(outValue.data);

    return ContextCompat.getColorStateList(context, outValue.resourceId);
  }

  private static int themeAttributeToColor(
    int themeAttributeId,
    Context context,
    int fallbackColorId
  ) {
    TypedValue outValue = new TypedValue();
    Resources.Theme theme = context.getTheme();
    boolean wasResolved = theme.resolveAttribute(
      themeAttributeId,
      outValue,
      true
    );

    if (wasResolved == false) return ContextCompat.getColor(
      context,
      fallbackColorId
    );
    if (outValue.resourceId == 0) return outValue.data;

    return ContextCompat.getColor(context, outValue.resourceId);
  }

  public RNBioPassDialog(
    ReactApplicationContext reactContext,
    String promptText
  ) {
    super((Context) reactContext.getCurrentActivity());
    System.out.println("Activity" + reactContext.getCurrentActivity());
    this.reactContext = reactContext;
    this.mKeyguardManager =
      (KeyguardManager) this.reactContext.getSystemService(
          Context.KEYGUARD_SERVICE
        );
    Activity activity = reactContext.getCurrentActivity();
    this.activity = activity;
    Context context = (Context) activity;
    this.context = context;
    float density = context.getResources().getDisplayMetrics().density;
    this.reactContext.addActivityEventListener(this);

    LinearLayout content = new LinearLayout(context);

    TextView title = new TextView(context);
    TextView prompt = new TextView(context);
    icon = new ImageView(context);
    TextView instructions = new TextView(context);
    Button cancelAction = new Button(context);
    setCancelable(false);

    final RNBioPassDialog dialog = this;

    {
      LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
      );

      layout.setMargins(
        Math.round(24 * density),
        Math.round(24 * density),
        Math.round(24 * density),
        Math.round(24 * density)
      );

      content.setLayoutParams(layout);
      content.setOrientation(LinearLayout.VERTICAL);
    }

    {
      ApplicationInfo info = activity.getApplicationInfo();
      PackageManager packageManager = activity.getPackageManager();
      String applicationLabel = packageManager
        .getApplicationLabel(info)
        .toString();

      title.setText("Fingerprint for \"" + applicationLabel + "\"");
      title.setTextColor(
        themeAttributeToColor(
          android.R.attr.textColorPrimary,
          context,
          android.R.color.black
        )
      );
      title.setTextSize(24);
    }

    {
      LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
      );

      layout.setMargins(0, 0, 0, Math.round(8 * density));

      prompt.setLayoutParams(layout);
      prompt.setText(promptText);
      prompt.setTextColor(
        themeAttributeToColor(
          android.R.attr.textColorPrimary,
          context,
          android.R.color.black
        )
      );
      prompt.setTextSize(18);
    }

    {
      LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        Math.round(64 * density)
      );

      layout.setMargins(
        0,
        Math.round(48 * density),
        0,
        Math.round(16 * density)
      );

      icon.setImageResource(R.drawable.fingerprint);
      icon.setLayoutParams(layout);

      ImageViewCompat.setImageTintList(
        icon,
        themeAttributeToColorStateList(
          android.R.attr.colorAccent,
          context,
          android.R.color.black
        )
      );
    }
    {
      instructions.setGravity(Gravity.CENTER);
      instructions.setText("Touch the fingerprint sensor");
    }
    {
      cancelAction.setText("Cancel");
      prompt.setTextColor(
        themeAttributeToColor(
          android.R.attr.textColorPrimary,
          context,
          android.R.color.black
        )
      );
      cancelAction.setTextSize(18);
      GradientDrawable gdDefault = new GradientDrawable();
      cancelAction.setBackground(gdDefault);
      cancellationSignal = new CancellationSignal();

      cancelAction.setOnClickListener(
        new View.OnClickListener() {

          public void onClick(View v) {
            // Perform action on click
            if (cancellationSignal != null) {
              cancellationSignal.cancel();
              cancellationSignal = null;
            }
            dialog.hide();
          }
        }
      );
    }

    content.addView(title);
    content.addView(prompt);
    content.addView(icon);
    content.addView(instructions);
    content.addView(cancelAction);

    setContentView(content);
  }

  public void authenticate(
    FingerprintManager fingerprintManager,
    final AuthenticateCallback callback
  ) {
    this.callback = callback;
    final RNBioPassDialog dialog = this;
    final Activity activity = reactContext.getCurrentActivity();
    cancellationSignal = new CancellationSignal();
    dialog.setCancelable(false);
    fingerprintManager.authenticate(
      null,
      cancellationSignal,
      0,
      new FingerprintManager.AuthenticationCallback() {

        @Override
        public void onAuthenticationSucceeded(
          FingerprintManager.AuthenticationResult result
        ) {
          icon.post(
            new Runnable() {

              public void run() {
                dialog.hide();
                callback.resolve();
              }
            }
          );
        }

        @Override
        public void onAuthenticationError(
          final int errorCode,
          final CharSequence errString
        ) {
          icon.post(
            new Runnable() {

              public void run() {
                dialog.hide();
                if (errorCode == 7) {
                  promptForKeyguard();
                } else callback.reject(new Exception(errString.toString()));
              }
            }
          );
        }
      },
      null
    );

    show();
  }

  public void promptForKeyguard() {
    Intent intent =
      this.mKeyguardManager.createConfirmDeviceCredentialIntent(null, null);
    this.activity.startActivityForResult(intent, 1);
  }

  public void onActivityResult(
    Activity activity,
    int requestCode,
    int resultCode,
    Intent data
  ) {
    if (requestCode == 1) {
      if (resultCode == Activity.RESULT_OK) {
        this.callback.resolve();
      } else if (resultCode == Activity.RESULT_CANCELED) {
        this.callback.reject(
            new Exception(
              "Unable to verify identity, Unable to register for Quick Balance"
            )
          );
      }
    }
  }
}
