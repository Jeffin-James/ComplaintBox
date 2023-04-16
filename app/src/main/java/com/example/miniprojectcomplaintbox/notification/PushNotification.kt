package com.example.miniprojectcomplaintbox.notification

import com.example.miniprojectcomplaintbox.notification.NotificationData


data class PushNotification(
    val data: NotificationData,
    val to: String
)