package com.crmushi.mitapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
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

public class Dashboard extends AppCompatActivity {
    RecyclerView recyclerView;
    CoursesAdapter adapter;
    AlertDialog course_description_dialog;

    private List<CoursesSetGet> coursesSetGetList=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

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
                        hashMap.put("Student Name","name");
                        hashMap.put("StudentID","name");
                        hashMap.put("Course Name",itemSetGet.getCourseName());
                        hashMap.put("Request Date",currentdate+" Hrs");
                        hashMap.put("Request Status","Unread");
                        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference()
                                .child("Requests").push();
                        databaseReference.setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                DatabaseReference stdStatus=FirebaseDatabase.getInstance().getReference().child("Students")
                                        .child("userId ");
                                stdStatus.child("Status").setValue("Applied").addOnSuccessListener(new OnSuccessListener<Void>() {
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
        });
    }
}