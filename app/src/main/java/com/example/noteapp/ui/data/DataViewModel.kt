package com.example.noteapp.ui.data

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.example.noteapp.MainActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.mindrot.jbcrypt.BCrypt
import java.util.*
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class DataViewModel : ViewModel()  {
    val password: MutableLiveData<String> = MutableLiveData("")

    val title: MutableLiveData<String> = MutableLiveData("")
    val content: MutableLiveData<String> = MutableLiveData("")

    val APP_DATA = "appData"

    fun save(pass: String) {
        savePassword(pass)
        saveNote(pass)
    }

    fun saveByFingerprint() {
        saveNoteByFingerprint()
    }

    fun savePassword(password: String) {
        val sharedPreferences = MainActivity.appCon.getSharedPreferences(
            APP_DATA,
            Context.MODE_PRIVATE
        )
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        val hashPass: String = BCrypt.hashpw(password, BCrypt.gensalt(12))

        editor.putString("PASSWORD", hashPass)
        editor.apply()
    }

    fun saveNote(password: String) {
        val sharedPreferences = MainActivity.appCon.getSharedPreferences(
            APP_DATA,
            Context.MODE_PRIVATE
        )
        val gson = Gson()
        val json = gson.toJson(Note(title.value, content.value))

        val _noteCrypt: String = noteEncrypt(password, json)

        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        editor.putString("NOTE", _noteCrypt)
        editor.apply()

        cleanAll()
    }

    fun saveNoteByFingerprint() {
        val sharedPreferences = MainActivity.appCon.getSharedPreferences(
            APP_DATA,
            Context.MODE_PRIVATE
        )
        val gson = Gson()
        val json = gson.toJson(Note(title.value, content.value))
        Fingerprint.encryption(json)

        var available = true
        Fingerprint.ready.observe(MainActivity.instance, Observer {
            if(available && it == true) {
                available = false
                val _noteCrypt: String = Fingerprint.noteContent
                val editor: SharedPreferences.Editor = sharedPreferences.edit()

                editor.putString("NOTE", _noteCrypt)
                editor.apply()

                cleanAll()
            }
        })
    }

    fun saveByteArray(TYPE: String, _value: ByteArray) {
        val sharedPreferences = MainActivity.appCon.getSharedPreferences(
                APP_DATA,
                Context.MODE_PRIVATE
        )
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(TYPE, Base64.getEncoder().encodeToString(_value))
        editor.apply()
    }

    fun getPasswordData(): String? {
        val sharedPreferences = MainActivity.appCon.getSharedPreferences(
            APP_DATA,
            Context.MODE_PRIVATE
        )
        return sharedPreferences.getString("PASSWORD", "")
    }

    fun getNotesData(password: String) {
        val gson = Gson()
        val sharedPreferences = MainActivity.appCon.getSharedPreferences(
            APP_DATA,
            Context.MODE_PRIVATE
        )
        val _noteCrypt = sharedPreferences.getString("NOTE", "")
        if(!_noteCrypt.isNullOrEmpty()) {
            val json = noteDecrypt(password, _noteCrypt)
            val itemType = object : TypeToken<Note>() {}.type
            val note = gson.fromJson<Note>(json, itemType)

            content.value = note.content
            title.value = note.title
        }
    }

    fun getNoteByFingerprint() {
        val sharedPreferences = MainActivity.appCon.getSharedPreferences(
            APP_DATA,
            Context.MODE_PRIVATE
        )
        val gson = Gson()
        val _noteCrypt = sharedPreferences.getString("NOTE", "")
        if(!_noteCrypt.isNullOrEmpty()) {

            Fingerprint.decryption(_noteCrypt)
            var available = true
            Fingerprint.ready.observe(MainActivity.instance, Observer {
                if(available && it == true) {
                    available = false

                    val json = Fingerprint.noteContent
                    val itemType = object : TypeToken<Note>() {}.type
                    val note = gson.fromJson<Note>(json, itemType)

                    content.value = note.content
                    title.value = note.title
                }
            })
        }
    }

    fun doNoteExist(): Boolean {
        val sharedPreferences = MainActivity.appCon.getSharedPreferences(
            APP_DATA,
            Context.MODE_PRIVATE
        )
        return !sharedPreferences.getString("NOTE", "").isNullOrEmpty()
    }

    fun getKey(pass: String, salt: ByteArray): SecretKeySpec {

        val value = PBEKeySpec(pass.toCharArray(), salt, 65536, 512)

        val sha = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
        var key = sha.generateSecret(value).encoded

        key = Arrays.copyOf(key, 32)

        return SecretKeySpec(key, "AES")
    }

    fun getByteArray(TYPE: String): ByteArray {
        val sharedPreferences = MainActivity.appCon.getSharedPreferences(
                APP_DATA,
                Context.MODE_PRIVATE
        )
        val _value = sharedPreferences.getString(TYPE, "")
        if(!_value.isNullOrEmpty()) {
            return Base64.getDecoder().decode(_value)
        }
        return byteArrayOf()
    }

    fun generateSalt(): ByteArray {
        return UUID.randomUUID().toString().toByteArray(charset("UTF-8"))
    }

    fun noteEncrypt(pass: String, note: String): String {

        val noteByteArray = note.toByteArray(charset("UTF-8"))

        val _iv: ByteArray = AES_GCM.generateIV()
        saveByteArray("IV", _iv)

        val _salt = generateSalt()
        saveByteArray("SALT", _salt)

        val final_key = getKey(pass, _salt)

        return AES_GCM.encrypt(noteByteArray, final_key, _iv)
    }

    fun noteDecrypt(pass: String, note: String): String {

        val noteByteArray = Base64.getDecoder().decode(note)

        val _iv: ByteArray = getByteArray("IV")
        val _salt: ByteArray = getByteArray("SALT")

        val final_key = getKey(pass, _salt)

        return AES_GCM.decrypt(noteByteArray, final_key, _iv)
    }

    fun cleanAll() {
        password.value = ""
        title.value = ""
        content.value = ""
    }
}