package com.momo.mymessage.Notifications

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface APIservices {



    @Headers(
        "Content-Type:application/json",
        "Authorization:key=AAAAryHsZcU:APA91bGl04-uo09KNGQDjKfBlhtaGTwQ91ToXB7eUln4_m3fXg75oYXLmzzy5xkVElrOJoym2fOeEhXnzIKtWRj3CNU6jOfNSDyddxrT6HOTPECntbuBl7q8D8CVc37ASy247m9yy0TE"
    )
    @POST("fcm/send")
    fun sendNotification(@Body body: Sender ):Call<MyResponse>

}