package com.memfault.bort.tokenbucket

import android.content.SharedPreferences
import com.memfault.bort.BortJson
import com.memfault.bort.shared.Logger
import com.memfault.bort.shared.PreferenceKeyProvider

interface TokenBucketStorage {
    fun readMap(): StoredTokenBucketMap
    fun writeMap(map: StoredTokenBucketMap)
}

class RealTokenBucketStorage(
    sharedPreferences: SharedPreferences,
    preferenceKey: String,
    val tag: String,
) : TokenBucketStorage, PreferenceKeyProvider<String>(
    sharedPreferences = sharedPreferences,
    defaultValue = BortJson.encodeToString(StoredTokenBucketMap.serializer(), StoredTokenBucketMap()),
    preferenceKey = preferenceKey,
) {
    override fun readMap(): StoredTokenBucketMap =
        try {
            val stringValue = super.getValue()
            Logger.logEvent("RealTokenBucketStorage.readMap", tag, stringValue)
            BortJson.decodeFromString(StoredTokenBucketMap.serializer(), stringValue)
        } catch (e: Exception) {
            Logger.logEvent("RealTokenBucketStorage.readMap.exception", tag, e.stackTraceToString())
            StoredTokenBucketMap()
        }

    override fun writeMap(map: StoredTokenBucketMap) =
        super.setValue(
            BortJson.encodeToString(StoredTokenBucketMap.serializer(), map)
        )
}
