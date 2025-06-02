package com.example.termproject

import android.os.Parcel
import android.os.Parcelable

data class FileData(
    val question: String,
    val choice1: String,
    val choice2: String,
    val choice3: String,
    val answer: String,
    var userAnswer: String? = null,
    var correctAnswerIndex: Int = -1
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(question)
        parcel.writeString(choice1)
        parcel.writeString(choice2)
        parcel.writeString(choice3)
        parcel.writeString(answer)
        parcel.writeString(userAnswer)
        parcel.writeInt(correctAnswerIndex)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FileData> {
        override fun createFromParcel(parcel: Parcel): FileData {
            return FileData(parcel)
        }

        override fun newArray(size: Int): Array<FileData?> {
            return arrayOfNulls(size)
        }
    }
}