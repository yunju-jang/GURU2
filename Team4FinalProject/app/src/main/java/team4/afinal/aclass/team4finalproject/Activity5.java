package team4.afinal.aclass.team4finalproject;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.storage.FirebaseStorage;

import org.pixsee.fcm.Message;
import org.pixsee.fcm.Sender;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


import team4.afinal.aclass.team4finalproject.bean.ExchangeBean;
import team4.afinal.aclass.team4finalproject.bean.GiverBean;
import team4.afinal.aclass.team4finalproject.bean.NotiBean;
import team4.afinal.aclass.team4finalproject.fcm.MyFirebaseMessagingService;
import team4.afinal.aclass.team4finalproject.util.Utils;

public class Activity5 extends AppCompatActivity implements OnMapReadyCallback {

    private ImageView imgGiver;
    private TextView txtName, txtField, txtPay, txtCareer, txtContents;
    public static List<GiverBean> mFoundBeanList = new ArrayList<GiverBean>();


    private GiverBean mGiverBean;


    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private FirebaseDatabase mDatabase;

    private GoogleMap mMap;
    private Button btnAdr;
    private Geocoder geocoder;
    private TextView edtAdr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_5);




        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        imgGiver = findViewById(R.id.imgGiver);
        txtName = findViewById(R.id.txtName);
        txtField = findViewById(R.id.txtField);
        txtPay = findViewById(R.id.txtPay);
        txtCareer = findViewById(R.id.txtCareer);
        txtContents = findViewById(R.id.txtContents);
        btnAdr = findViewById(R.id.btnAdr);
        edtAdr = findViewById(R.id.edtAdr);

        // DB ?????? ?????? ????????? bean??? ??????
        mGiverBean = (GiverBean) getIntent().getSerializableExtra("detail");

        try {
            new DownImgTask(imgGiver).execute(new URL(mGiverBean.imgUrl));
        } catch (Exception e){
            e.printStackTrace();
        }


        Log.e("SWU", mGiverBean.key);
        // ???????????? ???????????? bean????????? ??????
        txtName.setText(mGiverBean.studentId);
        txtField.setText(mGiverBean.field);
        txtPay.setText(mGiverBean.pay);
        txtCareer.setText(mGiverBean.career);
        txtContents.setText(mGiverBean.contents);
        edtAdr.setText((mGiverBean.address));

       /* if(mGiverBean != null) {
            Sender fcm = new Sender(MyFirebaseMessagingService.API_KEY);
            Message message = new Message.MessageBuilder()
                    .toToken(mGiverBean.key) // single android/ios device
                    .addData("title", "?????? ?????? ???????????????.")
                    .addData("content", "????????? ?????? ????????? ?????? ???????????????.")
                    .build();
            fcm.send(message);
        }*/

        // ?????? ?????????
       Button btnRequest = findViewById(R.id.btnRequest);
       btnRequest.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               //???????????? ????????? ??? ????????? ?????????
               if(mGiverBean != null) {
                   AlertDialog.Builder builder = new AlertDialog.Builder(Activity5.this);
                   builder.setTitle("????????????");
                   builder.setMessage( mGiverBean.studentId + "?????? ?????????????????????????");
                   builder.setCancelable(false);
                   builder.setPositiveButton("???", new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int which) {
                           //??????????????? ??? ?????????
                           Utils.showProgress(Activity5.this);

                           //fcm ????????? ?????????
                           Sender fcm = new Sender(MyFirebaseMessagingService.API_KEY);
                           Message message = new Message.MessageBuilder()
                                   .toToken(mGiverBean.key) // single android/ios device
                                   .addData("title", "???????????? ???????????? ?????? ????????? ??????????????????.")
                                   .addData("content", mGiverBean.studentId+"???????????? ???????????? ?????? ?????? ????????? ??????????????????.")
                                   .addData("notiType", "A")
                                   .build();
                           fcm.send(message);
                           upload();

                           //??????????????? ??? ?????????
                           Utils.hideProgress(Activity5.this);
                       }
                   }); // setPositive

                   builder.setNegativeButton("?????????", new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialogInterface, int i) {
                           Toast.makeText(Activity5.this, "????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                       }
                   }); // setNegative
                   builder.create().show();
               }
           }
       });

        // ?????? ?????? ??? ???????????? ??????
        Button btnGoMain = findViewById(R.id.btnGoMain);
        btnGoMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Activity5.this, Activity3.class);
                startActivity(i);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                finish();
            }
        });

        // ?????? ?????? ??? ?????? ????????? ??????
        Button btnNoti = findViewById(R.id.btnNoti);
        btnNoti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Activity5.this, Activity16.class);
                startActivity(i);
                //i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //finish();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    } // end onCreate()

    private void upload() {

        DatabaseReference firebaseRef = mDatabase.getReference();

        //Database??? ????????????.
        NotiBean bean= new NotiBean();

        bean.requestID = Activity1.mLoginedInfoBean.num;
        bean.studentID = mGiverBean.studentId;
        bean.kakaoID= Activity1.mLoginedInfoBean.kakaoID;
        bean.notiId = mGiverBean.name;
        bean.category = "1";

        firebaseRef.child("noti").child(bean.studentID).child(bean.notiId).setValue(bean);
        Toast.makeText(Activity5.this, "??????????????? ??????!!",Toast.LENGTH_SHORT).show();

        finish();

    }//end Upload

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;
        geocoder = new Geocoder(this);

        // ??? ?????? ????????? ?????? //
        /*mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){
            @Override
            public void onMapClick(LatLng point) {
                MarkerOptions mOptions = new MarkerOptions();
                // ?????? ?????????
                mOptions.title("?????? ??????");
                Double latitude = point.latitude; // ??????
                Double longitude = point.longitude; // ??????
                // ????????? ?????????(????????? ?????????) ??????
                mOptions.snippet(latitude.toString() + ", " + longitude.toString());
                // LatLng: ?????? ?????? ?????? ?????????
                mOptions.position(new LatLng(latitude, longitude));
                // ??????(???) ??????
                googleMap.addMarker(mOptions);
            }
        });*/
        ////////////////////

        String str=edtAdr.getText().toString();
        List<Address> addressList = null;
        try {
            // editText??? ????????? ?????????(??????, ??????, ?????? ???)??? ?????? ????????? ????????? ??????
            addressList = geocoder.getFromLocationName(
                    str, // ??????
                    10); // ?????? ?????? ?????? ??????
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(addressList.get(0).toString());
        // ????????? ???????????? split
        String []splitStr = addressList.get(0).toString().split(",");
        String address = splitStr[0].substring(splitStr[0].indexOf("\"") + 1,splitStr[0].length() - 2); // ??????
        System.out.println(address);

        String latitude = splitStr[10].substring(splitStr[10].indexOf("=") + 1); // ??????
        String longitude = splitStr[12].substring(splitStr[12].indexOf("=") + 1); // ??????
        System.out.println(latitude);
        System.out.println(longitude);

        // ??????(??????, ??????) ??????
        LatLng point = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
        // ?????? ??????
        MarkerOptions mOptions2 = new MarkerOptions();
        mOptions2.title(str);
        mOptions2.snippet(address);
        mOptions2.position(point);
        // ?????? ??????
        mMap.addMarker(mOptions2);
        // ?????? ????????? ?????? ???
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point,17));

        // ?????? ?????????
        /*btnAdr.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v){
                String str=edtAdr.getText().toString();
                List<Address> addressList = null;
                try {
                    // editText??? ????????? ?????????(??????, ??????, ?????? ???)??? ?????? ????????? ????????? ??????
                    addressList = geocoder.getFromLocationName(
                            str, // ??????
                            10); // ?????? ?????? ?????? ??????
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

                System.out.println(addressList.get(0).toString());
                // ????????? ???????????? split
                String []splitStr = addressList.get(0).toString().split(",");
                String address = splitStr[0].substring(splitStr[0].indexOf("\"") + 1,splitStr[0].length() - 2); // ??????
                System.out.println(address);

                String latitude = splitStr[10].substring(splitStr[10].indexOf("=") + 1); // ??????
                String longitude = splitStr[12].substring(splitStr[12].indexOf("=") + 1); // ??????
                System.out.println(latitude);
                System.out.println(longitude);

                // ??????(??????, ??????) ??????
                LatLng point = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                // ?????? ??????
                MarkerOptions mOptions2 = new MarkerOptions();
                mOptions2.title("search result");
                mOptions2.snippet(address);
                mOptions2.position(point);
                // ?????? ??????
                mMap.addMarker(mOptions2);
                // ?????? ????????? ?????? ???
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point,17));
            }
        });*/
        ////////////////////

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(37.6281894, 127.0897268);
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
    }


}