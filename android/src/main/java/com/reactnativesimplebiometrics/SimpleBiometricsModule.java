package com.reactnativesimplebiometrics;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;

import java.util.concurrent.Executor;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.module.annotations.ReactModule;

import androidx.biometric.BiometricPrompt;
import androidx.biometric.BiometricManager;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import android.util.Log;


public class SimpleBiometricsModule  extends ReactContextBaseJavaModule {

    public SimpleBiometricsModule (ReactApplicationContext reactContext) {
        super(reactContext);
          BiometricManager biometricManager = BiometricManager.from(reactContext);

        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d("MY_APP_TAG", "Biometric success");
                // Biometric authentication is available
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Log.e("MY_APP_TAG", "No biometric features available on this device.");
                // Device doesn't have biometric sensors
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Log.e("MY_APP_TAG", "Biometric features are currently unavailable.");
                // Biometric features are currently unavailable
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Log.e("MY_APP_TAG", "The user hasn't enrolled any biometric credentials.");
                // No biometric credentials enrolled
                break;
            case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
                Log.e("MY_APP_TAG", "Biometric security update is required.");
                // Biometric security update is required to use biometric authentication
                break;
            case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
                Log.e("MY_APP_TAG", "Biometric authentication is not supported on this device.");
                // Biometric authentication is not supported
                break;
        }

    }

    @NonNull
    @Override
    public String getName() {
        return "FaceIDAuth";
    }

    @ReactMethod
    public void authenticate(final Promise promise) {

        

        getCurrentActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Activity activity = getCurrentActivity();
                if (activity == null) {
                    promise.reject("ACTIVITY_NULL", "Activity is null");
                    return;
                }
                FragmentActivity fragmentActivity = (FragmentActivity) activity;

                AlertDialog.Builder builder = new AlertDialog.Builder(fragmentActivity);
                builder.setTitle("Select Biometric Method")
                        .setItems(new CharSequence[]{"Face", "Fingerprint"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                 // authenticateWithBiometric(BiometricManager.Authenticators.BIOMETRIC_WEAK, promise);
                                if (which == 0) {
                                   // Authenticate with fingerprint
                                    authenticateWithBiometric(BiometricManager.Authenticators.BIOMETRIC_WEAK, promise);
                                } else {
                                   // Authenticate with face
                                    authenticateWithBiometric(BiometricManager.Authenticators.BIOMETRIC_STRONG, promise);
                                }
                            }
                        });
                builder.show();
            }
        });
    }

    private void authenticateWithBiometric(int authenticatorType, Promise promise) {
        FragmentActivity fragmentActivity = (FragmentActivity) getCurrentActivity();
        BiometricPrompt.PromptInfo.Builder promptInfoBuilder = new BiometricPrompt.PromptInfo.Builder();
        promptInfoBuilder.setTitle("Authenticate to unlock");
        promptInfoBuilder.setSubtitle("");
        promptInfoBuilder.setDescription("");
        promptInfoBuilder.setNegativeButtonText("Cancel");
        promptInfoBuilder.setAllowedAuthenticators(authenticatorType);

        BiometricPrompt.PromptInfo promptInfo = promptInfoBuilder.build();

        // Use FragmentActivity and its support library
        BiometricPrompt biometricPrompt = new BiometricPrompt(fragmentActivity, fragmentActivity.getMainExecutor(), new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                promise.reject("AUTH_ERROR", errString.toString());
            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                promise.resolve("Authenticated successfully");
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                promise.reject("AUTH_FAILED", "Authentication failed");
            }
        });

        biometricPrompt.authenticate(promptInfo);
    }
}
