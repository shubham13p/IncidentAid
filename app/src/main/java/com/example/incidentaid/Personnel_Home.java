package com.example.incidentaid;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.internal.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Personnel_Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback, LocationListener, OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private Timer myTimer;
    private Context myContext;
    private Method method;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference myRef, myRef1, myRef2, myRef3, myRef4;
    private TextView title, name, job;
    private String token, filladdress, fillpincode;
    private Button note, notification, status;
    public static String mypref = "mypref";
    public static SharedPreferences pref, pref1;
    public SharedPreferences.Editor edit;
    private Button all_clear_b, evacuate_b, mayday_b, par_b, rescue_b, utility_b, status_incident;
    public static String incident_id_for_personnel, note_for_personnel, notification_id_for_personnel;
    public static String captain_id_for_personnel, inc_date_for_personnel, inc_time_for_personnel;
    public static String inc_lat_for_personnel, inc_long_for_personnel, personnels_id_for_personnel;
    public static String inc_status_for_personnel, inc_address_for_personnel, fire_station_lat, fire_station_long;
    private String all_clear_button, evacuate_button, mayday_button, par_button, rescue_button, utility_button;
    private GoogleMap mMap;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    GoogleApiClient mGoogleApiClient;
    Marker mCurrLocationMarker;
    LatLng p1 = null; //new LatLng(37.350870, -121.933775);
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main1);

        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }

        myContext = Personnel_Home.this;
        method = new Method(myContext);
        pref = getSharedPreferences(mypref, MODE_PRIVATE);
        edit = pref.edit();

        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference("User");
        myRef1 = FirebaseDatabase.getInstance().getReference("Incident");
        myRef2 = FirebaseDatabase.getInstance().getReference("Fire_Station");
        myRef3 = FirebaseDatabase.getInstance().getReference("Alert");
        myRef4 = FirebaseDatabase.getInstance().getReference("Notification");


        FirebaseMessaging.getInstance().setAutoInitEnabled(true);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        Login.username = Login.pref.getString("username", null);
        Login.snapshot_parent = Login.pref.getString("user_id", null);
        Login.firestation = Login.pref.getString("firestation", null);
        Login.jobtitle = Login.pref.getString("jobtitle", null);
        Login.values_for_profile = Login.pref.getString("values_for_profile", null);
        Login.who_is_the_user = Login.pref.getString("user_role", null);
        Login.alert_token = Login.pref.getString("alert_token", null);

        TextView name = (TextView) headerView.findViewById(R.id.name);
        TextView job = (TextView) headerView.findViewById(R.id.job);

        name.setText(Login.username.toUpperCase());
        job.setText(Login.jobtitle.toUpperCase());

        all_clear_b = (Button) findViewById(R.id.allclearbut);
        evacuate_b = (Button) findViewById(R.id.evacuatebut);
        mayday_b = (Button) findViewById(R.id.maydaybut);
        par_b = (Button) findViewById(R.id.parbut);
        rescue_b = (Button) findViewById(R.id.rescuebut);
        utility_b = (Button) findViewById(R.id.utilitiesbut);
        status_incident = (Button) findViewById(R.id.status);

        status_incident.setText("OPEN");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.child("email").getValue(String.class).equals(Login.values_for_profile)) {
                        filladdress = snapshot.child("address").getValue(String.class);
                        fillpincode = snapshot.child("pincode").getValue(String.class);
                        Log.e("mapready33", filladdress + " " + fillpincode);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        myRef1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() != 0) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.child("personnel").getValue(String.class).contains(Login.snapshot_parent)) {
                            Log.e("personnel", snapshot.getKey());
                            incident_id_for_personnel = snapshot.getKey();
                            note_for_personnel = snapshot.child("note_reference").getValue(String.class);
                            notification_id_for_personnel = snapshot.child("notification").getValue(String.class);
                            captain_id_for_personnel = snapshot.child("captain").getValue(String.class);
                            inc_date_for_personnel = snapshot.child("date").getValue(String.class);
                            inc_time_for_personnel = snapshot.child("time").getValue(String.class);
                            inc_lat_for_personnel = snapshot.child("latitude").getValue(String.class);
                            inc_long_for_personnel = snapshot.child("longitude").getValue(String.class);
                            personnels_id_for_personnel = snapshot.child("personnel").getValue(String.class);
                            inc_status_for_personnel = snapshot.child("status").getValue(String.class);
                            inc_address_for_personnel = snapshot.child("address").getValue(String.class);

                            edit.putString("incident_id_for_personnel", incident_id_for_personnel);
                            edit.putString("note_for_personnel", note_for_personnel);
                            edit.putString("notification_id_for_personnel", notification_id_for_personnel);
                            edit.putString("captain_id_for_personnel", captain_id_for_personnel);
                            edit.putString("inc_date_for_personnel", inc_date_for_personnel);
                            edit.putString("inc_time_for_personnel", inc_time_for_personnel);
                            edit.putString("inc_lat_for_personnel", inc_lat_for_personnel);
                            edit.putString("inc_long_for_personnel", inc_long_for_personnel);
                            edit.putString("personnels_id_for_personnel", personnels_id_for_personnel);
                            edit.putString("inc_status_for_personnel", inc_status_for_personnel);
                            edit.putString("inc_address_for_personnel", inc_address_for_personnel);
                            edit.commit();
                            Log.e("mapready22", inc_lat_for_personnel + " " + inc_long_for_personnel);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        myRef2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() != 0) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.getKey().equals(Login.firestation)) {
                            Log.e("123", snapshot.child("Latitude").getValue(String.class));
                            Log.e("123", snapshot.child("Longitude").getValue(String.class));
                            fire_station_lat = snapshot.child("Latitude").getValue(String.class);
                            fire_station_long = snapshot.child("Longitude").getValue(String.class);

                            edit.putString("fire_station_lan", fire_station_lat);
                            edit.putString("fire_station_long", fire_station_long);
                            edit.commit();
                            Log.e("check_data2", fire_station_long + "");
                            Log.e("mapready11", fire_station_lat + " " + fire_station_long);

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        note = (Button) findViewById(R.id.note);
        notification = (Button) findViewById(R.id.notification);
        status = (Button) findViewById(R.id.status);

        title = (TextView) findViewById(R.id.title);
        title.setText("PERSONNEL: " + Login.username.toUpperCase());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
// Create channel to show notifications.
            String channelId = getString(R.string.default_notification_channel_id);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
        }

        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
            }
        }

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("qwe", "getInstanceId failed", task.getException());
                            return;
                        }
                        token = task.getResult().getToken();
                        String msg = getString(R.string.msg_token_fmt, token);
                        Log.d("qwe", msg);
                        Toast.makeText(Personnel_Home.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });


        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (user == null) {
                    startActivity(new Intent(Personnel_Home.this, Login.class));
                    finish();
                }
            }
        };


        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                check_pending_alert_for_ack();
            }
        }, 0, 5000);


        note.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.e("check_data5", inc_lat_for_personnel + "");

                AlertDialog.Builder builder = new AlertDialog.Builder(Personnel_Home.this);
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.activity_note, null);
                builder.setCancelable(false);
                builder.setView(dialogView);
                final EditText note = (EditText) dialogView.findViewById(R.id.note);
                final Button note_save = (Button) dialogView.findViewById(R.id.note_save);
                note_save.setEnabled(false);
                note.setFocusable(false);

                final AlertDialog dialog = builder.create();
                dialog.show();
                dialog.setCancelable(true);
                dialog.setCanceledOnTouchOutside(true);

                myRef1.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getChildrenCount() != 0) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                if (snapshot.getKey().equals(incident_id_for_personnel)) {
                                    note.setText(snapshot.child("note_reference").getValue(String.class));
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });


        notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Personnel_Home.this, Notification.class)
                        .putExtra("incident_id", incident_id_for_personnel)
                        .putExtra("notification_id", notification_id_for_personnel));
            }
        });
        SupportMapFragment mapFragment = (SupportMapFragment) this.getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void check_pending_alert_for_ack() {

        myRef3.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() != 0) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.getKey().equals(incident_id_for_personnel)) {

                            if (snapshot.child("par").child("status").getValue().toString().equals("true")) {
                                par_button = "true";
                                par_b.setBackgroundColor(Color.GREEN);
                            }
                            if (snapshot.child("par").child("status").getValue().toString().equals("false")) {
                                par_button = "false";
                                par_b.setBackgroundColor(Color.RED);
                            }

                            if (snapshot.child("all_clear").child("status").getValue().toString().equals("true")) {
                                all_clear_button = "true";
                                all_clear_b.setBackgroundColor(Color.GREEN);
                            }
                            if (snapshot.child("all_clear").child("status").getValue().toString().equals("false")) {
                                all_clear_button = "false";
                                all_clear_b.setBackgroundColor(Color.RED);
                            }

                            if (snapshot.child("evacuate").child("status").getValue().toString().equals("true")) {
                                evacuate_button = "true";
                                evacuate_b.setBackgroundColor(Color.GREEN);
                            }
                            if (snapshot.child("evacuate").child("status").getValue().toString().equals("false")) {
                                evacuate_button = "false";
                                evacuate_b.setBackgroundColor(Color.RED);
                            }

                            if (snapshot.child("utility").child("status").getValue().toString().equals("true")) {
                                utility_button = "true";
                                utility_b.setBackgroundColor(Color.GREEN);
                            }
                            if (snapshot.child("utility").child("status").getValue().toString().equals("false")) {
                                utility_button = "false";
                                utility_b.setBackgroundColor(Color.RED);
                            }

                            if (snapshot.child("rescue").child("status").getValue().toString().equals("true")) {
                                rescue_button = "true";
                                rescue_b.setBackgroundColor(Color.GREEN);
                            }
                            if (snapshot.child("rescue").child("status").getValue().toString().equals("false")) {
                                rescue_button = "false";
                                rescue_b.setBackgroundColor(Color.RED);
                            }

                            if (snapshot.child("mayday").child("status").getValue().toString().equals("true")) {
                                mayday_button = "true";
                                mayday_b.setBackgroundColor(Color.GREEN);
                            }
                            if (snapshot.child("mayday").child("status").getValue().toString().equals("false")) {
                                mayday_button = "false";
                                mayday_b.setBackgroundColor(Color.RED);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        myRef3.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() != 0) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.getKey().equals(incident_id_for_personnel)) {

// par
                            if (snapshot.child("par").child("status").getValue().toString().equals("false") && snapshot.child("par").child(Login.snapshot_parent).getValue().toString().equals("0")) {
                                method.sendFCMPush("Alert", "Confirm PAR OFF Alert", Login.alert_token);
                            }
                            if (snapshot.child("par").child("status").getValue().toString().equals("true") && snapshot.child("par").child(Login.snapshot_parent).getValue().toString().equals("0")) {
                                method.sendFCMPush("Alert", "Confirm PAR ON Alert", Login.alert_token);
                            }

// all clear
                            if (snapshot.child("all_clear").child("status").getValue().toString().equals("false") && snapshot.child("all_clear").child(Login.snapshot_parent).getValue().toString().equals("0")) {
                                method.sendFCMPush("Alert", "Confirm All_clear OFF Alert", Login.alert_token);
                            }
                            if (snapshot.child("all_clear").child("status").getValue().toString().equals("true") && snapshot.child("all_clear").child(Login.snapshot_parent).getValue().toString().equals("0")) {
                                method.sendFCMPush("Alert", "Confirm All_clear ON Alert", Login.alert_token);
                            }

// evacuate
                            if (snapshot.child("evacuate").child("status").getValue().toString().equals("false") && snapshot.child("evacuate").child(Login.snapshot_parent).getValue().toString().equals("0")) {
                                method.sendFCMPush("Alert", "Confirm Evacuate OFF Alert", Login.alert_token);
                            }
                            if (snapshot.child("evacuate").child("status").getValue().toString().equals("true") && snapshot.child("evacuate").child(Login.snapshot_parent).getValue().toString().equals("0")) {
                                method.sendFCMPush("Alert", "Confirm Evacuate ON Alert", Login.alert_token);
                            }

// utility
                            if (snapshot.child("utility").child("status").getValue().toString().equals("false") && snapshot.child("utility").child(Login.snapshot_parent).getValue().toString().equals("0")) {
                                method.sendFCMPush("Alert", "Confirm Utility OFF Alert", Login.alert_token);
                            }
                            if (snapshot.child("utility").child("status").getValue().toString().equals("true") && snapshot.child("utility").child(Login.snapshot_parent).getValue().toString().equals("0")) {
                                method.sendFCMPush("Alert", "Confirm Utility ON Alert", Login.alert_token);
                            }

// rescue
                            if (snapshot.child("rescue").child("status").getValue().toString().equals("false") && snapshot.child("rescue").child(Login.snapshot_parent).getValue().toString().equals("0")) {
                                method.sendFCMPush("Alert", "Confirm Rescue OFF Alert", Login.alert_token);
                            }
                            if (snapshot.child("rescue").child("status").getValue().toString().equals("true") && snapshot.child("rescue").child(Login.snapshot_parent).getValue().toString().equals("0")) {
                                method.sendFCMPush("Alert", "Confirm Rescue ON Alert", Login.alert_token);
                            }

// mayday
                            if (snapshot.child("mayday").child("status").getValue().toString().equals("false") && snapshot.child("mayday").child(Login.snapshot_parent).getValue().toString().equals("0")) {
                                method.sendFCMPush("Alert", "Confirm Mayday OFF Alert", Login.alert_token);
                            }
                            if (snapshot.child("mayday").child("status").getValue().toString().equals("true") && snapshot.child("mayday").child(Login.snapshot_parent).getValue().toString().equals("0")) {
                                method.sendFCMPush("Alert", "Confirm Mayday ON Alert", Login.alert_token);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        par_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(Personnel_Home.this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(Personnel_Home.this);
                }

                builder.setTitle("Confirmation")
                        .setMessage("Want to push?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                myRef3.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.getChildrenCount() != 0) {
                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                if (snapshot.getKey().equals(incident_id_for_personnel)) {
                                                    if (snapshot.child("par").child(Login.snapshot_parent).getValue(String.class).equals("0")) {

                                                        HashMap map = new HashMap();
                                                        map.put(Login.snapshot_parent, "1");
                                                        map.put("received", snapshot.child("par").child("received").getValue(Integer.class) + 1);
                                                        myRef3.child(incident_id_for_personnel).child("par").updateChildren(map);

                                                        if (par_button.equals("true")) {
                                                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
                                                            String DateandTime = sdf.format(new Date());
                                                            HashMap hm = new HashMap();
                                                            hm.put(DateandTime, Login.snapshot_parent + " " + Login.username + " " + "Send ACK For PAR On Alert");
                                                            myRef4.child(incident_id_for_personnel).updateChildren(hm);

                                                            FirebaseDatabase.getInstance().getReference("User").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                    if (dataSnapshot.getChildrenCount() != 0) {
                                                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                                            if (snapshot.getKey().equals(captain_id_for_personnel)) {
                                                                                Log.e("helper", snapshot.child("token").getValue(String.class));
                                                                                method.sendFCMPush("Alert Response", Login.snapshot_parent + " " + Login.username + " " + "Send ACK For PAR On Alert", snapshot.child("token").getValue(String.class));
                                                                            }
                                                                        }
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                }
                                                            });
                                                        } else {
                                                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
                                                            String DateandTime = sdf.format(new Date());
                                                            HashMap hm = new HashMap();
                                                            hm.put(DateandTime, Login.snapshot_parent + " " + Login.username + " " + "Send ACK For PAR Off Alert");
                                                            myRef4.child(incident_id_for_personnel).updateChildren(hm);

                                                            FirebaseDatabase.getInstance().getReference("User").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                    if (dataSnapshot.getChildrenCount() != 0) {
                                                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                                            if (snapshot.getKey().equals(captain_id_for_personnel)) {
                                                                                Log.e("helper", snapshot.child("token").getValue(String.class));
                                                                                method.sendFCMPush("Alert Response", Login.snapshot_parent + " " + Login.username + " " + "Send ACK For PAR Off Alert", snapshot.child("token").getValue(String.class));
                                                                            }
                                                                        }
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                }
                                                            });
                                                        }

                                                    }
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });


        all_clear_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(Personnel_Home.this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(Personnel_Home.this);
                }

                builder.setTitle("Confirmation")
                        .setMessage("Want to push?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                myRef3.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.getChildrenCount() != 0) {
                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                if (snapshot.getKey().equals(incident_id_for_personnel)) {
                                                    if (snapshot.child("all_clear").child(Login.snapshot_parent).getValue(String.class).equals("0")) {

                                                        HashMap map = new HashMap();
                                                        map.put(Login.snapshot_parent, "1");
                                                        map.put("received", snapshot.child("all_clear").child("received").getValue(Integer.class) + 1);
                                                        myRef3.child(incident_id_for_personnel).child("all_clear").updateChildren(map);

                                                        if (all_clear_button.equals("true")) {
                                                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
                                                            String DateandTime = sdf.format(new Date());
                                                            HashMap hm = new HashMap();
                                                            hm.put(DateandTime, Login.snapshot_parent + " " + Login.username + " " + "Send ACK For All_Clear On Alert");
                                                            myRef4.child(incident_id_for_personnel).updateChildren(hm);

                                                            FirebaseDatabase.getInstance().getReference("User").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                    if (dataSnapshot.getChildrenCount() != 0) {
                                                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                                            if (snapshot.getKey().equals(captain_id_for_personnel)) {
                                                                                Log.e("helper", snapshot.child("token").getValue(String.class));
                                                                                method.sendFCMPush("Alert Response", Login.snapshot_parent + " " + Login.username + " " + "Send ACK For All_Clear On Alert", snapshot.child("token").getValue(String.class));
                                                                            }
                                                                        }
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                }
                                                            });
                                                        } else {
                                                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
                                                            String DateandTime = sdf.format(new Date());
                                                            HashMap hm = new HashMap();
                                                            hm.put(DateandTime, Login.snapshot_parent + " " + Login.username + " " + "Send ACK For All_Clear Off Alert");
                                                            myRef4.child(incident_id_for_personnel).updateChildren(hm);

                                                            FirebaseDatabase.getInstance().getReference("User").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                    if (dataSnapshot.getChildrenCount() != 0) {
                                                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                                            if (snapshot.getKey().equals(captain_id_for_personnel)) {
                                                                                Log.e("helper", snapshot.child("token").getValue(String.class));
                                                                                method.sendFCMPush("Alert Response", Login.snapshot_parent + " " + Login.username + " " + "Send ACK For All_Clear Off Alert", snapshot.child("token").getValue(String.class));
                                                                            }
                                                                        }
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                }
                                                            });
                                                        }

                                                    }
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });


        evacuate_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(Personnel_Home.this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(Personnel_Home.this);
                }

                builder.setTitle("Confirmation")
                        .setMessage("Want to push?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                myRef3.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.getChildrenCount() != 0) {
                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                if (snapshot.getKey().equals(incident_id_for_personnel)) {
                                                    if (snapshot.child("evacuate").child(Login.snapshot_parent).getValue(String.class).equals("0")) {

                                                        HashMap map = new HashMap();
                                                        map.put(Login.snapshot_parent, "1");
                                                        map.put("received", snapshot.child("evacuate").child("received").getValue(Integer.class) + 1);
                                                        myRef3.child(incident_id_for_personnel).child("evacuate").updateChildren(map);

                                                        if (evacuate_button.equals("true")) {
                                                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
                                                            String DateandTime = sdf.format(new Date());
                                                            HashMap hm = new HashMap();
                                                            hm.put(DateandTime, Login.snapshot_parent + " " + Login.username + " " + "Send ACK For Evacuate On Alert");
                                                            myRef4.child(incident_id_for_personnel).updateChildren(hm);

                                                            FirebaseDatabase.getInstance().getReference("User").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                    if (dataSnapshot.getChildrenCount() != 0) {
                                                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                                            if (snapshot.getKey().equals(captain_id_for_personnel)) {
                                                                                Log.e("helper", snapshot.child("token").getValue(String.class));
                                                                                method.sendFCMPush("Alert Response", Login.snapshot_parent + " " + Login.username + " " + "Send ACK For Evacuate On Alert", snapshot.child("token").getValue(String.class));
                                                                            }
                                                                        }
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                }
                                                            });
                                                        } else {
                                                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
                                                            String DateandTime = sdf.format(new Date());
                                                            HashMap hm = new HashMap();
                                                            hm.put(DateandTime, Login.snapshot_parent + " " + Login.username + " " + "Send ACK For Evacuate Off Alert");
                                                            myRef4.child(incident_id_for_personnel).updateChildren(hm);

                                                            FirebaseDatabase.getInstance().getReference("User").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                    if (dataSnapshot.getChildrenCount() != 0) {
                                                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                                            if (snapshot.getKey().equals(captain_id_for_personnel)) {
                                                                                Log.e("helper", snapshot.child("token").getValue(String.class));
                                                                                method.sendFCMPush("Alert Response", Login.snapshot_parent + " " + Login.username + " " + "Send ACK For Evacuate Off Alert", snapshot.child("token").getValue(String.class));
                                                                            }
                                                                        }
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                }
                                                            });
                                                        }

                                                    }
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });


        utility_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(Personnel_Home.this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(Personnel_Home.this);
                }

                builder.setTitle("Confirmation")
                        .setMessage("Want to push?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                myRef3.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.getChildrenCount() != 0) {
                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                if (snapshot.getKey().equals(incident_id_for_personnel)) {
                                                    if (snapshot.child("utility").child(Login.snapshot_parent).getValue(String.class).equals("0")) {

                                                        HashMap map = new HashMap();
                                                        map.put(Login.snapshot_parent, "1");
                                                        map.put("received", snapshot.child("utility").child("received").getValue(Integer.class) + 1);
                                                        myRef3.child(incident_id_for_personnel).child("utility").updateChildren(map);

                                                        if (utility_button.equals("true")) {
                                                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
                                                            String DateandTime = sdf.format(new Date());
                                                            HashMap hm = new HashMap();
                                                            hm.put(DateandTime, Login.snapshot_parent + " " + Login.username + " " + "Send ACK For Utility On Alert");
                                                            myRef4.child(incident_id_for_personnel).updateChildren(hm);

                                                            FirebaseDatabase.getInstance().getReference("User").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                    if (dataSnapshot.getChildrenCount() != 0) {
                                                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                                            if (snapshot.getKey().equals(captain_id_for_personnel)) {
                                                                                Log.e("helper", snapshot.child("token").getValue(String.class));
                                                                                method.sendFCMPush("Alert Response", Login.snapshot_parent + " " + Login.username + " " + "Send ACK For Utility On Alert", snapshot.child("token").getValue(String.class));
                                                                            }
                                                                        }
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                }
                                                            });
                                                        } else {
                                                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
                                                            String DateandTime = sdf.format(new Date());
                                                            HashMap hm = new HashMap();
                                                            hm.put(DateandTime, Login.snapshot_parent + " " + Login.username + " " + "Send ACK For Utility Off Alert");
                                                            myRef4.child(incident_id_for_personnel).updateChildren(hm);

                                                            FirebaseDatabase.getInstance().getReference("User").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                    if (dataSnapshot.getChildrenCount() != 0) {
                                                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                                            if (snapshot.getKey().equals(captain_id_for_personnel)) {
                                                                                Log.e("helper", snapshot.child("token").getValue(String.class));
                                                                                method.sendFCMPush("Alert Response", Login.snapshot_parent + " " + Login.username + " " + "Send ACK For Utility Off Alert", snapshot.child("token").getValue(String.class));
                                                                            }
                                                                        }
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                }
                                                            });
                                                        }

                                                    }
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });


        rescue_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(Personnel_Home.this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(Personnel_Home.this);
                }

                builder.setTitle("Confirmation")
                        .setMessage("Want to push?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                myRef3.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.getChildrenCount() != 0) {
                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                if (snapshot.getKey().equals(incident_id_for_personnel)) {
                                                    if (snapshot.child("rescue").child(Login.snapshot_parent).getValue(String.class).equals("0")) {

                                                        HashMap map = new HashMap();
                                                        map.put(Login.snapshot_parent, "1");
                                                        map.put("received", snapshot.child("rescue").child("received").getValue(Integer.class) + 1);
                                                        myRef3.child(incident_id_for_personnel).child("rescue").updateChildren(map);

                                                        if (rescue_button.equals("true")) {
                                                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
                                                            String DateandTime = sdf.format(new Date());
                                                            HashMap hm = new HashMap();
                                                            hm.put(DateandTime, Login.snapshot_parent + " " + Login.username + " " + "Send ACK For Rescue On Alert");
                                                            myRef4.child(incident_id_for_personnel).updateChildren(hm);

                                                            FirebaseDatabase.getInstance().getReference("User").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                    if (dataSnapshot.getChildrenCount() != 0) {
                                                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                                            if (snapshot.getKey().equals(captain_id_for_personnel)) {
                                                                                Log.e("helper", snapshot.child("token").getValue(String.class));
                                                                                method.sendFCMPush("Alert Response", Login.snapshot_parent + " " + Login.username + " " + "Send ACK For Rescue On Alert", snapshot.child("token").getValue(String.class));
                                                                            }
                                                                        }
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                }
                                                            });
                                                        } else {
                                                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
                                                            String DateandTime = sdf.format(new Date());
                                                            HashMap hm = new HashMap();
                                                            hm.put(DateandTime, Login.snapshot_parent + " " + Login.username + " " + "Send ACK For Rescue Off Alert");
                                                            myRef4.child(incident_id_for_personnel).updateChildren(hm);

                                                            FirebaseDatabase.getInstance().getReference("User").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                    if (dataSnapshot.getChildrenCount() != 0) {
                                                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                                            if (snapshot.getKey().equals(captain_id_for_personnel)) {
                                                                                Log.e("helper", snapshot.child("token").getValue(String.class));
                                                                                method.sendFCMPush("Alert Response", Login.snapshot_parent + " " + Login.username + " " + "Send ACK For Rescue Off Alert", snapshot.child("token").getValue(String.class));
                                                                            }
                                                                        }
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                }
                                                            });
                                                        }

                                                    }
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });


        mayday_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(Personnel_Home.this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(Personnel_Home.this);
                }

                builder.setTitle("Confirmation")
                        .setMessage("Want to push?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                myRef3.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.getChildrenCount() != 0) {
                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                if (snapshot.getKey().equals(incident_id_for_personnel)) {
                                                    if (snapshot.child("mayday").child(Login.snapshot_parent).getValue(String.class).equals("0")) {

                                                        HashMap map = new HashMap();
                                                        map.put(Login.snapshot_parent, "1");
                                                        map.put("received", snapshot.child("mayday").child("received").getValue(Integer.class) + 1);
                                                        myRef3.child(incident_id_for_personnel).child("mayday").updateChildren(map);

                                                        if (mayday_button.equals("true")) {
                                                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
                                                            String DateandTime = sdf.format(new Date());
                                                            HashMap hm = new HashMap();
                                                            hm.put(DateandTime, Login.snapshot_parent + " " + Login.username + " " + "Send ACK For Mayday On Alert");
                                                            myRef4.child(incident_id_for_personnel).updateChildren(hm);

                                                            FirebaseDatabase.getInstance().getReference("User").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                    if (dataSnapshot.getChildrenCount() != 0) {
                                                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                                            if (snapshot.getKey().equals(captain_id_for_personnel)) {
                                                                                Log.e("helper", snapshot.child("token").getValue(String.class));
                                                                                method.sendFCMPush("Alert Response", Login.snapshot_parent + " " + Login.username + " " + "Send ACK For Mayday On Alert", snapshot.child("token").getValue(String.class));
                                                                            }
                                                                        }
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                }
                                                            });
                                                        } else {
                                                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
                                                            String DateandTime = sdf.format(new Date());
                                                            HashMap hm = new HashMap();
                                                            hm.put(DateandTime, Login.snapshot_parent + " " + Login.username + " " + "Send ACK For Mayday Off Alert");
                                                            myRef4.child(incident_id_for_personnel).updateChildren(hm);

                                                            FirebaseDatabase.getInstance().getReference("User").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                    if (dataSnapshot.getChildrenCount() != 0) {
                                                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                                            if (snapshot.getKey().equals(captain_id_for_personnel)) {
                                                                                Log.e("helper", snapshot.child("token").getValue(String.class));
                                                                                method.sendFCMPush("Alert Response", Login.snapshot_parent + " " + Login.username + " " + "Send ACK For Mayday Off Alert", snapshot.child("token").getValue(String.class));
                                                                            }
                                                                        }
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                }
                                                            });
                                                        }

                                                    }
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            } else {
//Request Location Permission
                checkLocationPermission();
            }
        } else {
            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {

            }
        });
    }


    private void loadmappath() {
        String url = getUrl();
        Log.d("onMapClick", url.toString());
        Personnel_Home.FetchUrl FetchUrl = new Personnel_Home.FetchUrl();
        FetchUrl.execute(url);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    private String getUrl() {

        String str_origin = "origin=" + fire_station_lat + "," + fire_station_long;
        String str_dest = "destination=" + inc_lat_for_personnel + "," + inc_long_for_personnel;
        String sensor = "sensor=true";
        String mode = "mode=driving";
        String alternatives = "alternatives=false";
        String key = "key=" + BuildConfig.Google_Map_Key;
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + alternatives + "&" + mode + "&" + key;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        Log.e("urlurl", url);
        return url;
    }


    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();
        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {
            String data = "";

            try {
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);

        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask", jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                routes = parser.parse(jObject);
                Log.d("ParserTask", "Executing routes");
                Log.d("ParserTask", routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask", e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {


            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                lineOptions.addAll(points);
                lineOptions.width(8);

                lineOptions.color(Color.BLUE);
                Log.d("onPostExecute", "onPostExecute lineoptions decoded");

            }
            if (lineOptions != null) {
                mMap.addPolyline(lineOptions);
            } else {
                Log.d("onPostExecute", "without Polylines drawn");
            }
        }

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) this)
                .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    public void onConnectionSuspended(int i) {
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

// Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

// Show an explanation to the user *asynchronously* -- don't block
// this thread waiting for the user's response! After the user
// sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
//Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(Personnel_Home.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
// No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(latLng.toString());
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);

//move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

//stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (LocationListener) this);
        }

        show_map();
        loadmappath();

    }


    void show_map() {

        LatLng scu = new LatLng(Double.parseDouble(inc_lat_for_personnel), Double.parseDouble(inc_long_for_personnel));
        mMap.addMarker(new MarkerOptions().position(scu).title("Incident Place").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(scu));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        LatLng scu1 = new LatLng(Double.parseDouble(fire_station_lat), Double.parseDouble(fire_station_long));
        mMap.addMarker(new MarkerOptions().position(scu1).title(Login.firestation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(scu1));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));


        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
// If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

// permission was granted, yay! Do the
// location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

// permission denied, boo! Disable the
// functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

// other 'case' lines to check for other
// permissions this app might request
        }
    }


    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {


            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(Personnel_Home.this, android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(Personnel_Home.this);
            }
            builder.setTitle("Exit")
                    .setMessage("Want to exit the app?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
//                            Intent a = new Intent(Intent.ACTION_MAIN);
//                            a.addCategory(Intent.CATEGORY_HOME);
//                            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            startActivity(a);
                            finishAffinity();

//            startActivity(new Intent(MainActivity.this, Captain_Home.class));
//                            Captain_DashBoard.super.onBackPressed();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();


        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
// Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
// Handle action bar item clicks here. The action bar will
// automatically handle clicks on the Home/Up button, so long
// as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_logout) {
//            return true;
//        }
//
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
// Handle navigation view item clicks here.
        int id = item.getItemId();

//        Fragment fragment = null;
//        FragmentManager fragmentManager = getSupportFragmentManager();

        if (id == R.id.profile) {
            startActivity(new Intent(Personnel_Home.this, Profile_Personnel.class).putExtra("filladdress", filladdress).putExtra("fillpincode", fillpincode));
        } else if (id == R.id.history) {
            startActivity(new Intent(Personnel_Home.this, Incident_History.class).putExtra("username", Login.username));
        } else if (id == R.id.logout) {

            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(Personnel_Home.this, android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(Personnel_Home.this);
            }
            builder.setTitle("Logout")
                    .setMessage("Want to logout from your account?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            signOut();
                            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                            homeIntent.addCategory(Intent.CATEGORY_HOME);
                            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            finishAffinity();
                            startActivity(homeIntent);
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
//        fragmentManager.beginTransaction().replace(R.id.content, fragment).commit();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void signOut() {
        auth.signOut();
    }

    int i = 0;

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    i++;
                    Log.e("sms", i + "");
                    if (i % 3 == 0) {
                        FirebaseDatabase.getInstance().getReference("User").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getChildrenCount() != 0) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        if (snapshot.getKey().equals(captain_id_for_personnel)) {
                                            String token = snapshot.child("token").getValue(String.class);
                                            method.sendFCMPush("MAYDAY MAYDAY", Login.snapshot_parent + " " + Login.username.toUpperCase() + " " + "Need Help ASAP", token);

                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }
}
