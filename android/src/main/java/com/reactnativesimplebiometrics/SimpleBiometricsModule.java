package com.reactnativesimplebiometrics;

import android.app.Activity;
import androidx.annotation.NonNull;

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



@ReactModule(name = SimpleBiometricsModule.NAME)
public class SimpleBiometricsModule extends ReactContextBaseJavaModule {
    public static final String NAME = "SimpleBiometrics";

    static final int authenticators =  BiometricManager.Authenticators.BIOMETRIC_STRONG
        | BiometricManager.Authenticators.BIOMETRIC_WEAK
        | BiometricManager.Authenticators.DEVICE_CREDENTIAL;

    public SimpleBiometricsModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    @NonNull
    public String getName() {
        return NAME;
    }

    @ReactMethod
    public void canAuthenticate(Promise promise) {
        try {
            ReactApplicationContext context = getReactApplicationContext();
            BiometricManager biometricManager = BiometricManager.from(context);

            int res = biometricManager.canAuthenticate(authenticators);
            boolean can = res == BiometricManager.BIOMETRIC_SUCCESS;

            promise.resolve(can);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void requestBioAuth(final String title, final String subtitle, final Promise promise) {
        UiThreadUtil.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ReactApplicationContext context = getReactApplicationContext();
                            Activity activity = getCurrentActivity();
                            Executor mainExecutor = ContextCompat.getMainExecutor(context);
                            final BiometricPrompt.AuthenticationCallback authenticationCallback = new BiometricPrompt.AuthenticationCallback() {
                                @Override
                                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                                    super.onAuthenticationError(errorCode, errString);
                                    promise.reject(new Exception(errString.toString()));
                                }

                                @Override
                                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                                    super.onAuthenticationSucceeded(result);
                                    promise.resolve(true);
                                }
                            };

                            if (activity != null) {
                                BiometricPrompt prompt = new BiometricPrompt((FragmentActivity) activity, mainExecutor, authenticationCallback);

                                BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                                        .setAllowedAuthenticators(authenticators)
                                        .setTitle(title)
                                        .setSubtitle(subtitle)
                                        .build();

                                prompt.authenticate(promptInfo);
                            } else {
                                throw new Exception("null activity");
                            }
                        } catch (Exception e) {
                            promise.reject(e);
                        }
                    }
                }
        );

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
