package aot.armsproject.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable
import java.lang.reflect.Type


class AppSharedPreferenceManager constructor(context: Context): Serializable {
    // constants.
    private val PREFERENCES = "MPC_preferences"
    private val mSharedPreference: SharedPreferences

    /*
     * Method to remove key from sharedpreference
     *
     * */
    fun remove(key: String?) {
        try {
            val editor = mSharedPreference.edit()
            editor.remove(key)
            editor.apply()
        } catch (ex: Exception) {
            //Silent fail
        }
    }

    /*
     * Save string value in shared pref.
     * */
    fun save(key: String?, value: String?) {
        val editor = mSharedPreference.edit()
        editor.putString(key, value)
        editor.apply()
    }

    /*
     Save boolean value in shared pref.
    */
    fun save(key: String?, value: Boolean) {
        val editor = mSharedPreference.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    /*
     * Save int value in shared pref.
     * */
    fun save(key: String?, value: Int) {
        val editor = mSharedPreference.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    /*
    Save float value in shared pref.
    */
    fun save(key: String?, value: Float) {
        val editor = mSharedPreference.edit()
        editor.putFloat(key, value)
        editor.apply()
    }

    /**
     * Save the values stored in the map in shared preferences.
     *
     * @param values values to save.
     */
    fun save(values: Map<String, String?>) {
        val editor = mSharedPreference.edit()
        val keySet = values.keys
        for (key in keySet) {
            val value = values[key]
            editor.putString(key, value)
        }
        editor.apply()
    }

    /**
     * Save the values stored in the map in shared preferences.
     *
     * @param values values to save.
     */
    fun saveList(values: Map<String, List<String?>?>) {
        val editor = mSharedPreference.edit()
        val keySet = values.keys
        for (key in keySet) {
            val set: Set<String?> = HashSet(values[key])
            //List<String> value = values.get(key);
            editor.putStringSet(key, set)
        }
        editor.apply()
    }

    fun saveArrayList(values: Map<String, ArrayList<String?>?>) {
        val editor = mSharedPreference.edit()
        val keySet = values.keys
        for (key in keySet) {
            val set: Set<String?> = HashSet(values[key])
            //List<String> value = values.get(key);
            editor.putStringSet(key, set)
        }
        editor.apply()
    }

    fun contains(key: String?): Boolean {
        return mSharedPreference.contains(key)
    }

    fun <T> setList(key: String?, list: List<T>?) {
        val gson = Gson()
        val json: String = gson.toJson(list)
        set(key, json)
    }

    operator fun set(key: String?, value: String?) {
        val editor = mSharedPreference.edit()
        editor.putString(key, value)
        editor.apply()
    }

//    fun getList(): List<T>? {
//        val arrayItems= mutableListOf<YourModel>()
//        val serializedObject: String? = mSharedPreference.getString(KEY_PREFS)
//        if (serializedObject != null) {
//            val gson = Gson()
//            val type: Type = object : TypeToken<List<YourModel?>?>() {}.type
//            arrayItems = gson.fromJson(serializedObject, type)
//        }
//        return arrayItems
//    }

    ///
    // Get String
    ///
    fun getString(key: String?): String? {
        return mSharedPreference.getString(key, "")
    }

    /*
     *   Get float.
     * */
    fun getFloat(key: String?): Float {
        return mSharedPreference.getFloat(key, 0.0f)
    }

    /*
     * Get integer.
     * */
    fun getInt(key: String?): Int {
        return mSharedPreference.getInt(key, 0)
    }

    /*
     * Get StringList
     *
     */
    fun getStringList(key: String?): Set<String>? {
        return mSharedPreference.getStringSet(key, null)
    }

    fun getStringArrayList(key: String?): Set<String>? {
        return mSharedPreference.getStringSet(key, null)
    }

    /*
     * Get boolean
     * */
    fun getBoolean(key: String?): Boolean {
        return mSharedPreference.getBoolean(key, false)
    }

    /*
     * Get boolean with default value
     * */
    fun getBooleanWithDefaultTrue(key: String?): Boolean {
        return mSharedPreference.getBoolean(key, true)
    }

    /*
     * clear data from shared pref.
     * */
    fun clear() {
        val editor = mSharedPreference.edit()
        editor.clear()
        editor.apply()
    }

    fun save(key: String?, value: Long) {
        val editor = mSharedPreference.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun getLong(key: String?): Long {
        return mSharedPreference.getLong(key, 0L)
    }

    companion object {
        // members
        private var sSingleton: AppSharedPreferenceManager? = null

        ///
        // Get instance.
        ///
        @Synchronized
        fun getInstance(context: Context): AppSharedPreferenceManager? {
            if (sSingleton == null) {
                sSingleton = AppSharedPreferenceManager(context)
            }
            return sSingleton
        }
    }

    ///
    // constructor
    ///
    init {
        mSharedPreference = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
    }
}