package team4.afinal.aclass.team4finalproject.fcm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;

import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import team4.afinal.aclass.team4finalproject.Activity15;
import team4.afinal.aclass.team4finalproject.Activity16;
import team4.afinal.aclass.team4finalproject.R;
import team4.afinal.aclass.team4finalproject.adapter.ExchangeAdapter;
import team4.afinal.aclass.team4finalproject.bean.ExchangeBean;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static final String API_KEY = "AAAAVU0Qu1s:APA91bFqxiEgcOnEYOhkVfwhZvnVBmyK6E0YdTy32memGICXEBGB5zBRGBCEli9Q1qYPy5rq0B7ArRGcPaYqeE8oJFhYoTAa2KgAszjLUZ77CGYQAmy7wZPV3KVHL3-Kgq6Jm5HDa03-";
    Intent notiIntent;
    ExchangeBean exchangeBean = null;
    private FirebaseDatabase mDatabase;

    @Override
    public void onNewToken(String s) {
        Log.e("SWU", s);

    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        String title = data.get("title");
        String message =data.get("content");
        String notiType = data.get("notiType");
        String cate = data.get("cate");
        String exchId = data.get("exchId");

        mDatabase = FirebaseDatabase.getInstance();

        if(cate!=null && exchId!=null) {
            DatabaseReference exchange = mDatabase.getReference().child("exchange").child(cate).child(exchId);
            if (exchange != null) {
                exchange.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        exchangeBean = dataSnapshot.getValue(ExchangeBean.class);

                        notiAlarm(title, message, notiType);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
            } else {
            notiAlarm(title, message, notiType);
        }


    }

    public void notiAlarm(String title, String message, String notiType){        //??????????????? ?????? Noti ??????
        String channelId = "channel";
        String channelName = "Channel Name";

        NotificationManager notiMng =
                (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        //????????? ???????????? ????????? ?????? ??????
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel =
                    new NotificationChannel(channelId, channelName, importance);
            notiMng.createNotificationChannel(mChannel);
        }

        //??????????????? ??????????????? ????????? ????????? ????????? ????????????.
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(), channelId);

        int requestID = (int)System.currentTimeMillis();
        //?????? ????????? ???????????? ?????? Activity ??????
        if(notiType.equals("A")) {
            notiIntent = new Intent(getApplicationContext(), Activity16.class);
        }else {
            notiIntent = new Intent(getApplicationContext(), Activity15.class);
            if(exchangeBean != null) {
                notiIntent.putExtra(ExchangeBean.class.getName(), exchangeBean);
            }
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                requestID,
                notiIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
        );

        //???????????? ??????
        builder.setContentTitle(title)
                .setContentText(message)
                .setDefaults(Notification.DEFAULT_ALL) //?????? + ??????????????? ??????
                .setAutoCancel(true) //?????? ????????? ???????????????
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setSmallIcon(android.R.drawable.btn_star)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.main_logo_no_round))
                .setContentIntent(pendingIntent);

        //??????????????? ????????? ???????????? ??????.
        Notification noti = builder.build();
        noti.number = 10; //????????? ??????????????? 10?????? ????????????.
        // 10????????? ?????????
        // + ??? ????????????.

        notiMng.notify(requestID, noti);
    }
}
