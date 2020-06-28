package com.krystal.goddesslifestyle.data.response


import com.google.gson.annotations.SerializedName

//@Entity
data class TeacherOfTheMonth(
    //@PrimaryKey
    @SerializedName("teacher_id")
    var teacherId: Int? = null,
    @SerializedName("teacher_title")
    var teacherTitle: String? = null,
    @SerializedName("teacher_description")
    var teacherDescription: String? = null,
    @SerializedName("teacher_image")
    var teacherImage: String? = null
)