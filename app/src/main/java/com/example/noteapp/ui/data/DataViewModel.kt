package com.example.noteapp.ui.data

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.noteapp.MainActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.mindrot.jbcrypt.BCrypt
import java.security.MessageDigest
import java.util.*
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

    fun saveIV(_iv: ByteArray) {
        val sharedPreferences = MainActivity.appCon.getSharedPreferences(
            APP_DATA,
            Context.MODE_PRIVATE
        )
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString("IV", Base64.getEncoder().encodeToString(_iv))
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

    fun getIV(): ByteArray {
        val sharedPreferences = MainActivity.appCon.getSharedPreferences(
            APP_DATA,
            Context.MODE_PRIVATE
        )
        val _iv = sharedPreferences.getString("IV", "")
        if(!_iv.isNullOrEmpty()) {
            return Base64.getDecoder().decode(_iv)
        }
        return byteArrayOf()
    }

    fun getKey(pass: String): SecretKeySpec {
        var key = pass.toByteArray(charset("UTF-8"))
        val sha = MessageDigest.getInstance("SHA-256")
        key = sha.digest(key)
        key = Arrays.copyOf(key, 32)

        return SecretKeySpec(key, "AES")
    }

    fun noteEncrypt(pass: String, note: String): String {

        val noteByteArray = note.toByteArray(charset("UTF-8"))
        val final_key = getKey(pass)

        val _iv: ByteArray = AES_GCM.generateIV()
        saveIV(_iv)

        return AES_GCM.encrypt(noteByteArray, final_key, _iv)
    }

    fun noteDecrypt(pass: String, note: String): String {

        val noteByteArray = Base64.getDecoder().decode(note)
        val final_key = getKey(pass)
        val _iv: ByteArray = getIV()

        return AES_GCM.decrypt(noteByteArray, final_key, _iv)
    }

    fun cleanAll() {
        password.value = ""
        title.value = ""
        content.value = ""
    }
}