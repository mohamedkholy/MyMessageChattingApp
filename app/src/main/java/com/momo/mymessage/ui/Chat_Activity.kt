package com.momo.mymessage.ui


import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.storage.FirebaseStorage
import com.momo.mymessage.Adapter.InChat_Adabter
import com.momo.mymessage.Adapter.InChat_Adabter.onListItemClick
import com.momo.mymessage.Notifications.*
import com.momo.mymessage.R
import com.momo.mymessage.ViewModels.ChatViewModel
import com.momo.mymessage.databinding.ActivityInChatBinding
import com.momo.mymessage.db.ClearDatabase
import com.momo.mymessage.db.db_chats_manage
import com.momo.mymessage.pogo.*
import com.momo.mymessage.pogo.Message
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class Chat_Activity : AppCompatActivity() {

    private val Gallary_Request_code: Int=2
    private val Camera_request_code: Int=1
    lateinit var binding:ActivityInChatBinding
    val id=FirebaseAuth.getInstance().uid
    lateinit var  userid:String
    var databaseReference=FirebaseDatabase.getInstance().getReference().child("users")
    val storageReference=FirebaseStorage.getInstance().getReference()
    lateinit var clearDatabase: ClearDatabase
    lateinit var  list:ArrayList<Message>
    lateinit var  deletelist:ArrayList<Message>
    lateinit var inchatAdabter: InChat_Adabter
    lateinit var sent_Message:MediaPlayer
    lateinit var Img_Message:Uri
    lateinit var  file_name:String
    var rec: MediaRecorder= MediaRecorder()
    lateinit var listpositions:ArrayList<String>
    lateinit var listner: onListItemClick
    lateinit var apIservices: APIservices
    lateinit var userToken:String
    lateinit var sp:SharedPreferences
    lateinit var userSp:SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    lateinit var name:String
    lateinit var chatViewModel: ChatViewModel



    override fun onBackPressed()
    {
        if(binding.contentLayout.visibility==View.VISIBLE){ binding.contentLayout.visibility=View.GONE;return }

        else super.onBackPressed()
    }



    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityInChatBinding.inflate(layoutInflater)
        setContentView(binding.root)




        listner=object :onListItemClick{
            override fun onItemClick(holder: InChat_Adabter.Holder, list: ArrayList<Message>) {

                deletelist=list

                if(list.size>0){
                    binding.deleteMessageButton.visibility=View.VISIBLE
                    binding.cancleDeleteMessageButton.visibility=View.VISIBLE
                }
                else{
                    binding.deleteMessageButton.visibility=View.GONE
                    binding.cancleDeleteMessageButton.visibility=View.GONE

                }
            }
        }



        setInfo()

        initial()


        setonline()

        askFOrMicPer()

        chatViewModel= ChatViewModel(this@Chat_Activity,userid,id!!)
        chatViewModel.get_messages()
        chatViewModel.liveData.observe(this, androidx.lifecycle.Observer {
            val x=list.size
            val y=it.size
            list.clear()
            list.addAll(it)
            inchatAdabter.notifyDataSetChanged()
            if(x!=y)
            binding.recv.scrollToPosition(inchatAdabter.itemCount-1)
            binding.pro.visibility=View.INVISIBLE
        })




            binding.deleteMessageButton.setOnClickListener{
            binding.cancleDeleteMessageButton.visibility=View.GONE
            binding.deleteMessageButton.visibility=View.GONE
            chatViewModel.deletMessages(deletelist)
            deletelist.clear()
            listpositions.clear()
            inchatAdabter.notifyDataSetChanged()


        }
        binding.cancleDeleteMessageButton.setOnClickListener{
            binding.cancleDeleteMessageButton.visibility=View.GONE
            binding.deleteMessageButton.visibility=View.GONE
            deletelist.clear()
            listpositions.clear()
            inchatAdabter.notifyDataSetChanged()

        }


        binding.recordButton.setOnTouchListener(object : View.OnTouchListener {

            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {

                if (p1!!.action == MotionEvent.ACTION_DOWN)
                {
                    binding.recordButtonOnpress.visibility = View.VISIBLE

                    try {
                        rec.setAudioSource(MediaRecorder.AudioSource.DEFAULT)
                        rec.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                        rec.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                        file_name=returnFileName()
                        rec.setOutputFile(file_name)
                        rec.prepare()
                        rec.start()

                        Toast.makeText(this@Chat_Activity, "Recording in progress" , Toast.LENGTH_SHORT).show()
                    }
                    catch (e: Exception)
                    {

                        Toast.makeText(this@Chat_Activity, "Sorry! file creation failed!", Toast.LENGTH_SHORT).show()

                    }

                } else if(p1!!.action == MotionEvent.ACTION_UP)
                {
                    binding.recordButtonOnpress.visibility = View.GONE

                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                        rec.stop()
                        rec.release()
                        rec=MediaRecorder()

                        var x=Uri.fromFile(File(file_name))
                        sendVoice(x)

                    },250)
                }

                return true
            }
        })
        binding.tvback.setOnClickListener({
            startActivity(Intent(this,home::class.java))
            finish() })
        binding.img.setOnClickListener{finish()}
        binding.closeContentImg.setOnClickListener{
        binding.contentImgContianer.visibility=View.GONE
        if(TextUtils.isEmpty(binding.chatEditText.text)) { binding.recordButton.visibility=View.VISIBLE }

        }
        //getContentButton
        binding.contentButton.setOnClickListener{
            if( binding.contentLayout.visibility==View.GONE){
                binding.contentLayout.visibility=View.VISIBLE
            binding.contentImgContianer.visibility=View.GONE
            }
            else {
                binding.contentLayout.visibility = View.GONE
            }

        }
        binding.gallaryButton.setOnClickListener{
            binding.contentLayout.visibility=View.GONE
            askStroagePer_getImageFromgallary()
        }
        binding.cameraButton.setOnClickListener{
            binding.contentLayout.visibility=View.GONE
            askStroagePer_getImageFromCamera()
        }
        //send message
        binding.sendButton.setOnClickListener{
            binding.pro.visibility=View.VISIBLE

             if(!TextUtils.isEmpty(binding.chatEditText.getText().toString())||binding.contentImgContianer.visibility==View.VISIBLE) {

                   var text=binding.chatEditText.text.toString()

                if(binding.contentImgContianer.visibility==View.VISIBLE){
                    binding.contentImgContianer.visibility=View.GONE
                   val downloadref=  storageReference.child("/messages_Imgs").child(SimpleDateFormat("H:mm:ss", Locale.getDefault()).format(Date())+id)

                        downloadref.putFile(Img_Message).addOnCompleteListener{

                         if(it.isSuccessful){

                            downloadref.downloadUrl.addOnSuccessListener {
                                 val message_img_Url=it.toString()
                                 val message = Message(
                                     text,
                                     SimpleDateFormat("h:mm a",
                                         Locale.getDefault()).format(Date()),
                                     id!!,"unseen",System.currentTimeMillis().toString()+id,message_img_Url)

                                 sendmessage(message)
                                 binding.recv.scrollToPosition(inchatAdabter.getItemCount() - 1)


                             }

                         }

                     }


                }

                else{

                    val message = Message(
                        binding.chatEditText.text.toString(),
                        SimpleDateFormat("h:mm a",
                            Locale.getDefault()).format(Date()), id!!,"unseen",System.currentTimeMillis().toString()+id,"noImg")

                    sendmessage(message)

                }
                binding.chatEditText.setText(null)
                binding.recordButton.visibility=View.VISIBLE
            }
        }

        binding.chatEditText.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
             if(p0!!.length==0&&binding.contentImgContianer.visibility!=View.VISIBLE)
                 binding.recordButton.visibility=View.VISIBLE
                else
                 binding.recordButton.visibility=View.GONE

            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })



    }



/*
    private fun gettime():String {
        var estimatedServerTimeMs=System.currentTimeMillis()
        val offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset")
        offsetRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val offset = snapshot.getValue(Double::class.java)!!!!
                estimatedServerTimeMs= System.currentTimeMillis() + offset.toLong()
            }


            override fun onCancelled(error: DatabaseError) {

            }
        })

        return estimatedServerTimeMs.toString()
    }*/

    private fun sendVoice(x: Uri?) {
        binding.pro.visibility=View.VISIBLE

        if(x!=null){
            storageReference.child("/messages_records").child(SimpleDateFormat("HH:mm:ss:", Locale.getDefault()).format(Date())+id+".mp3").putFile(x).addOnCompleteListener{
                if(it.isSuccessful){


                    it.result.storage.downloadUrl.addOnSuccessListener {itt->
                        val message_record_Url=itt.toString()

                        val message = Message(
                            "",
                            SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date()),
                            id!!,
                            "unseen",
                            System.currentTimeMillis().toString()+id,
                            "noImg",
                            message_record_Url)

                        sendmessage(message)
                        binding.recv.scrollToPosition(inchatAdabter.getItemCount() - 1)

                    }

                }



            }

        }


    }

    private fun returnFileName(): String {

        val contextWrapper=ContextWrapper(applicationContext);
        val musicDirectory=contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        var file=File(musicDirectory,android.text.format.Time().setToNow().toString()+".mp3")
        return file.path

    }


    val permissionlancher3=registerForActivityResult(ActivityResultContracts.RequestPermission()){

    }

    private fun askFOrMicPer() {
        if(ContextCompat.checkSelfPermission(this@Chat_Activity,android.Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED)
        {
           permissionlancher3.launch(android.Manifest.permission.RECORD_AUDIO)


        }
    }








    private fun sendmessage(message: Message) {



        val m= hashMapOf<String,Any>()
        m.put("last",message)




        databaseReference.child(id!!).child("messages").child(userid)
            .child("chats").child(message.id!!).setValue(message).addOnSuccessListener {


                databaseReference.child(id).child("messages").child(userid).updateChildren(m)
                sent_Message.start()
                val text:String
                if(message.record!=null)
                    text="Voice"
                else if(message.text!!.isEmpty()&&!message.imgurl.equals("noImg"))
                    text="Image"
                else
                    text=message.text

                val data= Data(id,R.drawable.ic_baseline_message_24,text,name,userid)

                val sender= Sender(data,userToken)
               if(!userid.equals(id))
                apIservices.sendNotification(sender).enqueue(object :Callback<MyResponse>{
                    override fun onResponse(
                        call: Call<MyResponse>,
                        response: Response<MyResponse>
                    ) {
                        Log.d("NotificationResponse",response.body()!!.response.toString())
                    }

                    override fun onFailure(call: Call<MyResponse>, t: Throwable) {
                        Log.d("NotificationFaild",t.message.toString())
                    }
                })

            }

        databaseReference.child(userid).child("messages").child(id)
            .child("chats").child(message.id!!).setValue(message).addOnSuccessListener {

                databaseReference.child(userid).child("messages").child(id).updateChildren(m)

            }



    }











    private fun initial() {
        Firebase.messaging.isAutoInitEnabled = true
        list= ArrayList()
        deletelist=ArrayList()
        listpositions= ArrayList()
        inchatAdabter= InChat_Adabter(listpositions,deletelist,this@Chat_Activity,list,listner)
        binding.recv.adapter=inchatAdabter
        clearDatabase= ClearDatabase(this@Chat_Activity)
        sent_Message=MediaPlayer.create(this@Chat_Activity,R.raw.gg)
        apIservices=Client.getClient("https://fcm.googleapis.com/").create(APIservices::class.java)
        sp=getSharedPreferences("info", MODE_PRIVATE)
        name=sp.getString("username",null).toString()
       databaseReference.child(userid).child("token").get().addOnSuccessListener {
           userToken=it.child("token").value.toString()
           Log.d("gggggg",it.toString())
        }

    }












    private fun setonline() {
        val hashMap= hashMapOf<String,Any>()
        hashMap.put("status","online")
        databaseReference.child(id!!).updateChildren(hashMap)

        databaseReference.child(userid).child("status").addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.getValue().toString().equals("online"))
                    binding.status.visibility=View.VISIBLE
                else
                    binding.status.visibility=View.INVISIBLE
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }




    private fun setInfo() {
        val user =intent.getSerializableExtra("user") as? User
        binding.nameChat1.setText(user!!.name)
        Picasso.get().load(user.Imageurl).into(binding.img)
         userid=user.userid!!
         userSp=getSharedPreferences("CurrentUser", MODE_PRIVATE)
         editor=userSp.edit()
        editor.putString("userid",userid)
        editor.apply()
    }



    val permissionlancher=registerForActivityResult(ActivityResultContracts.RequestPermission())
    {
        if(it){

            val intent=Intent(Intent.ACTION_GET_CONTENT)
            intent.type="image/*"
            startActivityForResult(intent,Gallary_Request_code)

        }

        else{
            Toast.makeText(this@Chat_Activity,"You can not access gallary", Toast.LENGTH_SHORT).show()
        }



    }


        private fun askStroagePer_getImageFromgallary() {
        if(ContextCompat.checkSelfPermission(this@Chat_Activity,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)
        {
            val intent=Intent(Intent.ACTION_GET_CONTENT)
            intent.type="image/*"
            startActivityForResult(intent,Gallary_Request_code)

        }
        else if(ActivityCompat.shouldShowRequestPermissionRationale(this@Chat_Activity,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            val alertDialog= AlertDialog.Builder(this@Chat_Activity)

            alertDialog.apply {
                setTitle("Permission Required")
                alertDialog.setCancelable(false)
                setMessage("Please accept permission to access gallary")
                setPositiveButton("OK")
                {_, _ ->
                    permissionlancher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.create().show()


        }
        else{
            permissionlancher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }



    val permissionlancher2=registerForActivityResult(ActivityResultContracts.RequestPermission())
    {
        if (it) {

            val intent=Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            startActivityForResult(intent,Camera_request_code)


        } else {
            Toast.makeText(this@Chat_Activity, "You can not access gallary", Toast.LENGTH_SHORT)
                .show()
        }
    }


    private fun askStroagePer_getImageFromCamera() {
        if(ContextCompat.checkSelfPermission(this@Chat_Activity,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)
        {
            val intent=Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            startActivityForResult(intent,Camera_request_code)


        }
        else if(ActivityCompat.shouldShowRequestPermissionRationale(this@Chat_Activity,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            val alertDialog= AlertDialog.Builder(this@Chat_Activity)

            alertDialog.apply {
                setTitle("Permission Required")
                alertDialog.setCancelable(false)
                setMessage("Please accept permission to access gallary")
                setPositiveButton("OK")
                {_, _ ->
                    permissionlancher2.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.create().show()


        }
        else{
            permissionlancher2.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
         if(data!=null){
        if(requestCode==Gallary_Request_code){


            Img_Message=data!!.data!!


            if(Img_Message!=null) {
                binding.contentImg.setImageURI(Img_Message)
                binding.contentImgContianer.visibility = View.VISIBLE
                binding.recordButton.visibility=View.GONE
            }


        }
        else if(requestCode==Camera_request_code){


            val p = data!!.extras!!["data"] as Bitmap?
            Img_Message=getImageUri(this@Chat_Activity,p!!)!!






            if(Img_Message!=null) {
                binding.contentImg.setImageURI(Img_Message)
                binding.contentImgContianer.visibility = View.VISIBLE
                binding.recordButton.visibility=View.GONE
            }

        }




    }
    }

    override fun onResume() {
        super.onResume()
        editor.putString("userid",userid)
        editor.apply()
    }

    override fun onPause() {
        super.onPause()
        editor.putString("userid",null)
        editor.apply()

    }

    override fun onDestroy() {
        super.onDestroy()
        editor.putString("userid",null)
        editor.apply()
        if(list.size>0)
        db_chats_manage(this@Chat_Activity).setLastMesssage( list[list.size-1].text!!,userid!!)
        db_chats_manage(this@Chat_Activity).updateunseen("0",userid)
        if(chatViewModel.flag==1)
            chatViewModel.ref.removeEventListener(chatViewModel.listner)

        this@Chat_Activity.getViewModelStore().clear();


    }






    fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            inContext.getContentResolver(),
            inImage,
            "Title",
            null
        )
        return Uri.parse(path)
    }



    @RequiresApi(Build.VERSION_CODES.M)
     fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)

        return (capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))

    }





}


