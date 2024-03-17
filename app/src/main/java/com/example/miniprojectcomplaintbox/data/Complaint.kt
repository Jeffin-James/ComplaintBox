package com.example.miniprojectcomplaintbox.data

import com.google.firebase.ktx.Firebase

data class Complaint
    (val location: String? = null,
     val imageUrl: String? = null,
     val description: String?= null,
     val videoUrl: String ?= null,
     val district : String ?= null,
     val timeStamp : Long ?= null,
     val userId: String ?= null,
     val complaintId: String ?= null,
     val solved: Boolean ?= false,
     val imageUrlList: List<String> ?= null,
     val count: Long ?= null
     ) : java.io.Serializable
