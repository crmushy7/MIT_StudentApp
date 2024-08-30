package com.crmushi.mitapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Adapters.CoursesAdapter;
import Adapters.CoursesSetGet;

public class Dashboard extends AppCompatActivity {
    RecyclerView recyclerView;
    CoursesAdapter adapter;

    private List<CoursesSetGet> coursesSetGetList=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        recyclerView=(RecyclerView) findViewById(R.id.courses_recyclerview);
        recyclerView.setLayoutManager(new GridLayoutManager(this,3));
        adapter=new CoursesAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
        for (int x=0; x<20; x++){
            CoursesSetGet coursesSetGet=new CoursesSetGet("BSc. Computer science","short course","one");
            coursesSetGetList.add(coursesSetGet);
            adapter.updateData(coursesSetGetList);
            Collections.reverse(coursesSetGetList);
            adapter.notifyDataSetChanged();
        }
    }
}