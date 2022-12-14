package com.momo.mymessage.Notifications


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.momo.mymessage.R
import com.momo.mymessage.db.dbChatsManage
import com.momo.mymessage.ui.ChatActivity
import com.momo.mymessage.ui.home


class MyFirebaseMessaging:FirebaseMessagingService() {

     val channelId="MyMessage"

    lateinit var sp:SharedPreferences
    lateinit var dbChatsManage: dbChatsManage
    val id=FirebaseAuth.getInstance().currentUser?.uid
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val sented= message.data.get("sented")
        sp=getSharedPreferences("CurrentUser", MODE_PRIVATE)
        dbChatsManage=dbChatsManage(this)
        val firebaseUser=FirebaseAuth.getInstance().currentUser

        if(firebaseUser!=null&&sented.equals(firebaseUser.uid)){

           sendnotify(message)


        }



    }



    private fun sendnotify(message: RemoteMessage) {
        val user_= message.data.get("user")
        val icon= message.data.get("icon")
        val title= message.data.get("title")
        val body= message.data.get("body")

       val notification=message.notification


        val userId=sp.getString("userid",null)


           val user= dbChatsManage.getChatById(user_.toString(),id!!)


            if(user.userid!=null){


            val intent=Intent(this,ChatActivity::class.java)
            intent.putExtra("user",user)
            intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TOP
            val pi=PendingIntent.getActivity(this,System.currentTimeMillis().toInt(),intent,PendingIntent.FLAG_IMMUTABLE)

            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
            {
                val channel=NotificationChannel(channelId,"My Message",NotificationManager.IMPORTANCE_HIGH)
                val nm:NotificationManager=getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                nm.createNotificationChannel(channel)
            }

            val builder=NotificationCompat.Builder(this,channelId)
            builder.setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(icon!!.toInt())
                .setSound(Uri.parse(
                    ("android.resource://"
                            + baseContext.getPackageName()) + "/" + R.raw.new_message
                ))
                .setContentIntent(pi)
                .setAutoCancel(true)

            if(userId.equals(null)||!user!!.userid.equals(userId))
             NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(),builder.build())



         }

        else{

                val intent=Intent(this,home::class.java)
                val pi=PendingIntent.getActivity(this,System.currentTimeMillis().toInt(),intent,PendingIntent.FLAG_IMMUTABLE)

                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
                {
                    val channel=NotificationChannel(channelId,"My Message",NotificationManager.IMPORTANCE_HIGH)
                    val nm:NotificationManager=getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    nm.createNotificationChannel(channel)
                }

                val builder=NotificationCompat.Builder(this,channelId)
                builder.setContentTitle(title)
                    .setContentText(body)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setSmallIcon(icon!!.toInt())
                    .setSound(Uri.parse(
                        ("android.resource://"
                                + baseContext.getPackageName()) + "/" + R.raw.new_message
                    ))
                    .setContentIntent(pi)
                    .setAutoCancel(true)


                    NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(),builder.build())


            }

    }

}