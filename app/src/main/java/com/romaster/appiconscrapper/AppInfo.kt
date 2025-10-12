package com.romaster.appiconscrapper

import android.os.Parcel
import android.os.Parcelable

data class AppInfo(
    val packageName: String,
    val name: String,
    val isSystemApp: Boolean,
    val isGoogleApp: Boolean,
    var isSelected: Boolean = false
) : Parcelable {
    fun isGapps(): Boolean {
        return packageName.startsWith("com.google") || 
               packageName.startsWith("com.android.vending") ||
               packageName.contains("gms") ||
               name.contains("Google", ignoreCase = true)
    }

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(packageName)
        parcel.writeString(name)
        parcel.writeByte(if (isSystemApp) 1 else 0)
        parcel.writeByte(if (isGoogleApp) 1 else 0)
        parcel.writeByte(if (isSelected) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AppInfo> {
        override fun createFromParcel(parcel: Parcel): AppInfo {
            return AppInfo(parcel)
        }

        override fun newArray(size: Int): Array<AppInfo?> {
            return arrayOfNulls(size)
        }
    }
}