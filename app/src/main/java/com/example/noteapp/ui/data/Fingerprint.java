package com.example.noteapp.ui.data;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;

import com.example.noteapp.MainActivity;

import java.util.concurrent.Executor;

public class Fingerprint {
    public static final MutableLiveData<Boolean> ready = new MutableLiveData(false);

    public static Boolean checkPhoneBiometricSetings() {
        BiometricManager biometricManager = BiometricManager.from(MainActivity.appCon);
        switch (biometricManager.canAuthenticate()) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d("MY_APP_TAG", "App can authenticate using biometrics.");
                return true;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Log.e("MY_APP_TAG", "No biometric features available on this device.");
                return false;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Log.e("MY_APP_TAG", "Biometric features are currently unavailable.");
                return false;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Log.e("MY_APP_TAG", "Biometric error none enrolled.");
                return false;
        }
        return false;
    }

    public static void authenticationDialog() {
        Executor executor = ContextCompat.getMainExecutor(MainActivity.appCon);
        BiometricPrompt biometricPrompt = new BiometricPrompt(MainActivity.instance, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                Log.e("MY_APP_TAG", "onAuthenticationError.");
                super.onAuthenticationError(errorCode, errString);
                MainActivity.instance.finish();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                Log.e("MY_APP_TAG", "onAuthenticationSucceeded.");
                ready.setValue(true);
                super.onAuthenticationSucceeded(result);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Log in")
                .setSubtitle("Use your fingerprint")
                .setNegativeButtonText("Cancel")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }
}
