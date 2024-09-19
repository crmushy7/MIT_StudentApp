package com.crmushi.mitapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import Adapters.CoursesAdapter;
import Adapters.CoursesSetGet;
import Adapters.RequestsAdapter;
import Adapters.RequestsSetGet;

public class Dashboard extends AppCompatActivity {
    RecyclerView recyclerView,request_recyclerview;
    RequestsAdapter request_adapter;
    CoursesAdapter adapter;
    AlertDialog course_description_dialog;
    public static String userID,studentname,studentemail;
    LinearLayout homebutton,requestbutton,coursesAvailable,requestAvalable,profle;
    ProgressBar request_progressbar;
    TextView no_request_text;

    private List<CoursesSetGet> coursesSetGetList=new ArrayList<>();
    private List<RequestsSetGet> requestsSetGetList=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        profle=findViewById(R.id.ll_profileBtn);
        request_progressbar=findViewById(R.id.request_progressbar);
        no_request_text=findViewById(R.id.no_request_text);
        request_recyclerview=findViewById(R.id.requests_recyclerview);
        homebutton=findViewById(R.id.ll_homeBtn);
        requestbutton=findViewById(R.id.ll_requestsBtn);
        coursesAvailable=findViewById(R.id.courses_layout);
        requestAvalable=findViewById(R.id.requests_layaout);

        DatabaseReference studentDetails=FirebaseDatabase.getInstance().getReference()
                .child("Students").child(FirebaseAuth.getInstance().getUid());
        studentDetails.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userID=FirebaseAuth.getInstance().getUid();
                studentname=snapshot.child("Fullname").getValue(String.class);
                studentemail=snapshot.child("Email").getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        profle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent=new Intent(Dashboard.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        request_recyclerview.setLayoutManager(new LinearLayoutManager(Dashboard.this));
        request_adapter=new RequestsAdapter(new ArrayList<>());
        request_recyclerview.setAdapter(request_adapter);
        recyclerView=(RecyclerView) findViewById(R.id.courses_recyclerview);
        recyclerView.setLayoutManager(new GridLayoutManager(this,3));
        adapter=new CoursesAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
        DatabaseReference fetch_courses= FirebaseDatabase.getInstance().getReference()
                .child("All Courses");
        fetch_courses.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                coursesSetGetList.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    String courseNm=dataSnapshot.child("Course Name").getValue(String.class);
                    String courseDur=dataSnapshot.child("Course Duration").getValue(String.class);
                    String courseDes=dataSnapshot.child("Course Description").getValue(String.class);
                    String courseID=dataSnapshot.getKey().toString();
                    CoursesSetGet coursesSetGet=new CoursesSetGet(courseNm+"",courseDur+"",courseID+"",courseDes+"");
                    coursesSetGetList.add(coursesSetGet);

                }
                adapter.updateData(coursesSetGetList);
                Collections.reverse(coursesSetGetList);
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        adapter.setOnItemClickListener(new CoursesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, CoursesSetGet itemSetGet) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Dashboard.this);
                View popupView = LayoutInflater.from(Dashboard.this).inflate(R.layout.course_description, null);
                builder.setView(popupView);
                course_description_dialog = builder.create();
                course_description_dialog.show();
                Button requestBtn=popupView.findViewById(R.id.button_request);
                TextView courseName=popupView.findViewById(R.id.coursename);
                TextView courseDuration=popupView.findViewById(R.id.courseduration);
                TextView courseDescription=popupView.findViewById(R.id.coursedescription);
                courseName.setText(itemSetGet.getCourseName());
                courseDuration.setText("Duration: "+itemSetGet.getCourseDuration());
                courseDescription.setText(itemSetGet.getCourseDescription());
                requestBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Calendar calendar = Calendar.getInstance();
                        String currentdate = DateFormat.getInstance().format(calendar.getTime());
                        HashMap<Object,String> hashMap=new HashMap<>();
                        hashMap.put("Student Name",studentname);
                        hashMap.put("StudentID",userID);
                        hashMap.put("Course Name",itemSetGet.getCourseName());
                        hashMap.put("Request Date",currentdate+" Hrs");
                        hashMap.put("Request Status","Unread");
                        DatabaseReference courseExist=studentDetails.child(itemSetGet.getCourseName());
                        courseExist.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    String applyStatus=snapshot.getValue(String.class);
                                    if (applyStatus.equals("Applied")){
                                        course_description_dialog.dismiss();
                                        Toast.makeText(Dashboard.this, "Already applied for this course!", Toast.LENGTH_SHORT).show();
                                    }else {
                                        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference()
                                                .child("Requests").push();
                                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                databaseReference.setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        courseExist.setValue("Applied").addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                DatabaseReference databaseReference1=FirebaseDatabase.getInstance().getReference()
                                                                        .child("Feedback").child(userID).child(snapshot.getKey().toString());
                                                                databaseReference1.setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {
                                                                        course_description_dialog.dismiss();
                                                                        Toast.makeText(Dashboard.this, "Success!", Toast.LENGTH_SHORT).show();

                                                                    }
                                                                });
                                                            }
                                                        });
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });


                                    }
                                }else{
                                    DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference()
                                            .child("Requests").push();
                                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            databaseReference.setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    courseExist.setValue("Applied").addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            DatabaseReference databaseReference1=FirebaseDatabase.getInstance().getReference()
                                                                    .child("Feedback").child(userID).child(snapshot.getKey().toString());
                                                            databaseReference1.setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    course_description_dialog.dismiss();
                                                                    Toast.makeText(Dashboard.this, "Success!", Toast.LENGTH_SHORT).show();

                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            });
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                });
            }
        });

        requestbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                coursesAvailable.setVisibility(View.GONE);
                requestAvalable.setVisibility(View.VISIBLE);
                no_request_text.setVisibility(View.GONE);
                request_progressbar.setVisibility(View.VISIBLE);

                DatabaseReference fetch_requests=FirebaseDatabase.getInstance().getReference()
                        .child("Feedback").child(FirebaseAuth.getInstance().getUid());
                fetch_requests.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        requestsSetGetList.clear();
                        if (snapshot.exists()){
                            if (snapshot.hasChildren()){
                                request_progressbar.setVisibility(View.GONE);
                                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                                    String studentName=dataSnapshot.child("Student Name").getValue(String.class);
                                    String studentID=dataSnapshot.child("StudentID").getValue(String.class);
                                    String courseName=dataSnapshot.child("Course Name").getValue(String.class);
                                    String requestDate=dataSnapshot.child("Request Date").getValue(String.class);
                                    String requestStatus=dataSnapshot.child("Request Status").getValue(String.class);
                                    String requestID=dataSnapshot.getKey().toString();
                                    RequestsSetGet requestsSetGet=new RequestsSetGet(requestID+"",studentName+"",studentID+"",courseName+"",requestDate+"",requestStatus+"");
                                    requestsSetGetList.add(requestsSetGet);

                                }
                                request_adapter.updateData(requestsSetGetList);
                                Collections.reverse(requestsSetGetList);
                                request_adapter.notifyDataSetChanged();
                            }else {
                                request_progressbar.setVisibility(View.GONE);
                                no_request_text.setVisibility(View.VISIBLE);
                            }
                        }else{
                            request_progressbar.setVisibility(View.GONE);
                            no_request_text.setVisibility(View.VISIBLE);
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        homebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                coursesAvailable.setVisibility(View.VISIBLE);
                requestAvalable.setVisibility(View.GONE);

            }
        });
    }
}