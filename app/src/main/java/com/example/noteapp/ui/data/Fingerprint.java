package com.example.noteapp.ui.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;

import com.example.noteapp.MainActivity;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.concurrent.Executor;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class Fingerprint {

    public static String noteContent = "";
    private static String APP_DATA = "appData";

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

    public static void encryption(String content) throws InvalidKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, NoSuchPaddingException, UnrecoverableKeyException, NoSuchProviderException, InvalidAlgorithmParameterException {

        KeyGenParameterSpec.Builder keyBuilder = new KeyGenParameterSpec.Builder(
                "fingerprint",
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(true)
                .setInvalidatedByBiometricEnrollment(true);

        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            keyBuilder.setUserAuthenticationParameters(2,
                    KeyProperties.AUTH_BIOMETRIC_STRONG |
                            KeyProperties.AUTH_DEVICE_CREDENTIAL);

        } else {
            keyBuilder.setUserAuthenticationValidityDurationSeconds(2);
        }

        generateSecretKey(keyBuilder.build());

        Executor executor = ContextCompat.getMainExecutor(MainActivity.appCon);
        BiometricPrompt biometricPrompt = new BiometricPrompt(MainActivity.instance, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
//                Log.e("MY_APP_TAG", "onAuthenticationError.");
                super.onAuthenticationError(errorCode, errString);
                MainActivity.instance.finish();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
//                Log.e("MY_APP_TAG", "onAuthenticationSucceeded.");
                super.onAuthenticationSucceeded(result);

                byte[] encryptedInfo = new byte[0];
                try {
                    Cipher cipher = getCipher();
                    SecretKey secretKey = getSecretKey();
                    cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                    encryptedInfo = cipher.doFinal(content.getBytes(Charset.defaultCharset()));
                    saveByteArray("IV", cipher.getIV());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                noteContent = Base64.getEncoder().encodeToString(encryptedInfo);
                ready.setValue(true);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Log in")
                .setSubtitle("Use your fingerprint")
                .setDeviceCredentialAllowed(true)
                .build();

        biometricPrompt.authenticate(promptInfo);
        ready.setValue(false);
    }

    public static void decryption(String content) throws InvalidKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, NoSuchPaddingException, UnrecoverableKeyException, InvalidAlgorithmParameterException {
        Executor executor = ContextCompat.getMainExecutor(MainActivity.appCon);
        BiometricPrompt biometricPrompt = new BiometricPrompt(MainActivity.instance, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
//                Log.e("MY_APP_TAG", "onAuthenticationError.");
                super.onAuthenticationError(errorCode, errString);
                MainActivity.instance.finish();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
//                Log.e("MY_APP_TAG", "onAuthenticationSucceeded.");
                super.onAuthenticationSucceeded(result);

                byte[] decryptedInfo = new byte[0];
                try {
                    byte[] IV = getByteArray("IV");
                    GCMParameterSpec gcmObject = new GCMParameterSpec(16 * 8, IV);
                    Cipher cipher = getCipher();
                    SecretKey secretKey = getSecretKey();
                    cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmObject);
                    decryptedInfo = cipher.doFinal(Base64.getDecoder().decode(content));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                noteContent = new String(decryptedInfo);
                ready.setValue(true);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Log in")
                .setSubtitle("Use your fingerprint")
                .setDeviceCredentialAllowed(true)
                .build();

        biometricPrompt.authenticate(promptInfo);
        ready.setValue(false);
    }

    private static void generateSecretKey(KeyGenParameterSpec keyGenParameterSpec) throws InvalidAlgorithmParameterException, NoSuchProviderException, NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        keyGenerator.init(keyGenParameterSpec);
        keyGenerator.generateKey();
    }

    private static SecretKey getSecretKey() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");

        // Before the keystore can be accessed, it must be loaded.
        keyStore.load(null);
        return ((SecretKey)keyStore.getKey("fingerprint", null));
    }

    private static Cipher getCipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
        return Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_GCM + "/"
                + KeyProperties.ENCRYPTION_PADDING_NONE);
    }

    private static void saveByteArray(String TYPE, byte[] _value) {
        SharedPreferences sharedPreferences = MainActivity.appCon.getSharedPreferences(
                APP_DATA,
                Context.MODE_PRIVATE
        );
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TYPE, Base64.getEncoder().encodeToString(_value));
        editor.apply();
    }

    private static byte[] getByteArray(String TYPE) {
        SharedPreferences sharedPreferences = MainActivity.appCon.getSharedPreferences(
                APP_DATA,
                Context.MODE_PRIVATE
        );

        String _value = sharedPreferences.getString(TYPE, "");
        if(!_value.isEmpty()) {
            return Base64.getDecoder().decode(_value);
        }
        return new byte[0];
    }
}
