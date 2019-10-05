package com.example.incidentaid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import static android.content.ContentValues.TAG;

public class Method {

    private Context mContext;
    private FirebaseAuth mAuth;

    private DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("User");
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Incident");
    private DatabaseReference myRef2 = FirebaseDatabase.getInstance().getReference("Notification");
    private DatabaseReference myRef3 = FirebaseDatabase.getInstance().getReference("Alert");

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String userID;
    Random rnd = new Random();


    public Method() {
    }

    public Method(Context context) {
        mAuth = FirebaseAuth.getInstance();
        mContext = context;
        if (mAuth.getCurrentUser() != null) {
            userID = mAuth.getCurrentUser().getUid();
        }
    }

    public void registerNewRecruit(final String name, final String email, final String qualification, final String job_title, final String address, final String pincode, final String firestation) {

        User insertuser = new User(name, email, qualification, job_title, address, pincode, firestation, "not_captain", "user_token", "false");
        int n = 10000 + rnd.nextInt(90000);
        myRef.child(n + "").setValue(insertuser);

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(mContext, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(mContext);
        }
        builder.setTitle("Recruit ID")
                .setMessage("Remember the ID: " + n + "")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
        Toast.makeText(mContext, "Registered. WellCome to Department", Toast.LENGTH_SHORT).show();
    }

    public void registerNewEmail(final String id, final String email, final String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(mContext, "Uploading Data", Toast.LENGTH_SHORT).show();
                            sendVerificationEmail(id, email);
                        } else {
                            Toast.makeText(mContext, "Authentication failed.", Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    public void sendVerificationEmail(final String zid, final String email) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        FirebaseAuth.getInstance().signOut();
                        Toast.makeText(mContext, "verification email send.", Toast.LENGTH_SHORT).show();
                        mContext.startActivity(new Intent(mContext, Login.class));
                    } else {
                        Toast.makeText(mContext, "couldn't send verification email.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public void setupFirebaseAuth() {
//        mAuthListener =
        new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // user is signed in
                    Log.d(TAG, "onAuthStateChanged: signed_in:" + user.getUid());
                } else {
                    //user is signed out
                    Log.d(TAG, "onAuthStateChanged: signed_out");
                    Log.d(TAG, "onAuthStateChanged: navigating back to login screen.");
                    Intent intent = new Intent(mContext, Login.class);
                    //clear the activity stack， in case when sign out, the back button will bring the user back to the previous activity
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    mContext.startActivity(intent);
                }
            }
        };
    }

    public void showalert(String title, String msg) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(mContext, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(mContext);
        }
        builder.setTitle(title.toUpperCase())
                .setMessage(msg)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    public void sendFCMPush(String noti_title, String noti_msg, String user_token) {

        final String Legacy_SERVER_KEY = "AIzaSyDgPMlQb7p_QDf50fl8q1PR1NlbgqZOuvc";
        String msg = noti_msg;
        String title = noti_title;
        String token = user_token;

        JSONObject obj = null;
        JSONObject objData = null;
        JSONObject dataobjData = null;

        try {
            obj = new JSONObject();
            objData = new JSONObject();

            objData.put("body", msg);
            objData.put("title", title);
            objData.put("sound", "default");
            objData.put("icon", "icon_name"); //   icon_name image must be there in drawable
            objData.put("tag", token);
            objData.put("priority", "high");

            dataobjData = new JSONObject();
            dataobjData.put("text", msg);
            dataobjData.put("title", title);

            obj.put("to", token);
            //obj.put("priority", "high");

            obj.put("notification", objData);
            obj.put("data", dataobjData);
            Log.e("!_@rj@_@@_PASS:>", obj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, "https://fcm.googleapis.com/fcm/send", obj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("!_@@_SUCESS", response + "");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("!_@@_Errors--", error + "");
                    }
                }) {
            @Override
            public HashMap<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "key=" + Legacy_SERVER_KEY);
                params.put("Content-Type", "application/json");
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        int socketTimeout = 1000 * 60;// 60 seconds
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsObjRequest.setRetryPolicy(policy);
        requestQueue.add(jsObjRequest);
    }

    public void create_incident(String address, String id_list, String lat, String longi, String note_reference) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
        SimpleDateFormat date = new SimpleDateFormat("yyyy_MM_dd");
        SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");

        String DateandTime = sdf.format(new Date());
        String currentDate = date.format(new Date());
        String currentTime = time.format(new Date());

        id_list = id_list.replaceAll("\\[", "");
        id_list = id_list.replaceAll("\\]", "");
        String temp[] = id_list.split(", ");

        String captain = temp[0];
        String[] temp1 = Arrays.copyOfRange(temp, 1, temp.length);
        String personnel = "";
        for (String str : temp1) {
            personnel += "," + str;
        }
        personnel = personnel.substring(1, personnel.length());


        User person = new User(address, captain, personnel, currentDate, currentTime, lat, longi, note_reference, "open", DateandTime, DateandTime);
        mDatabase.child(DateandTime).setValue(person);

        HashMap map1 = new HashMap();
        map1.put(DateandTime, "Incident Created And Notification Sent To All");
        myRef2.child(DateandTime).updateChildren(map1);

//        HashMap map = new HashMap();
//        map.put("par", "false");
//        map.put("all_clear", "false");
//        map.put("evacuate", "false");
//        map.put("utility", "false");
//        map.put("rescue", "false");
//        map.put("mayday", "false");
//        myRef3.child(DateandTime).updateChildren(map);

        int n = temp1.length;
        String alert[] = {"par", "all_clear", "evacuate", "utility","rescue", "mayday"};
        for (String str : alert) {
            HashMap map2 = new HashMap();
            map2.put("status", "false");
            map2.put("send", n);
            map2.put("received", n);
            for(String ss : temp1){
                map2.put(ss, "1");
            }
            myRef3.child(DateandTime).child(str).updateChildren(map2);
        }


    }

    public void save_note(final String captain_id, final String note_detail) {

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() != 0) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.child("captain").getValue(String.class).equals(captain_id)) {

                            HashMap map = new HashMap();
                            map.put("note_reference", note_detail);
                            FirebaseDatabase.getInstance().getReference("Incident").child(snapshot.getKey()).updateChildren(map);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void popup(View view) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window, null);
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
    }

}
