package com.example.todo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.Date;

public class HomePage extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;

    private DatabaseReference dbref;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private String id;
    private ProgressDialog progressDialog;



    private String key = "";
    private String task;
    private String description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        id = user.getUid();
        dbref = FirebaseDatabase.getInstance().getReference().child("tasks").child(id);

        recyclerView = findViewById(R.id.recycle);
        floatingActionButton = findViewById(R.id.floating);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        progressDialog = new ProgressDialog(this);


        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add();
            }
        });
    }

    private void add() {

        final AlertDialog.Builder mydialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.layout_onadd, null);
        mydialog.setView(view);

        final AlertDialog dialog = mydialog.create();
        dialog.setCancelable(false);


        final EditText task = view.findViewById(R.id.task);
        final EditText description = view.findViewById(R.id.task1);
        Button save = view.findViewById(R.id.button1);
        Button cancle = view.findViewById(R.id.button2);
        dialog.show();

        cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String task1 = task.getText().toString().trim();
                String description1 = description.getText().toString().trim();
                String id = dbref.push().getKey();
                String date = DateFormat.getDateInstance().format(new Date());

                if (TextUtils.isEmpty(task1)) {
                    task.setError("Task is empty");
                    return;
                }

                if (TextUtils.isEmpty(description1)) {
                    description.setError("Description is empty");
                    return;
                } else {
                    progressDialog.setMessage("Adding the task");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    Method method = new Method(task1, description1, id, date);
                    dbref.child(id).setValue(method).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                Toast.makeText(HomePage.this, "Added", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            } else {
                                String error = task.getException().toString();
                                Toast.makeText(HomePage.this, "Failed " + error, Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }

                        }
                    });

                }
                dialog.dismiss();

            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();

        FirebaseRecyclerOptions<Method> options=new FirebaseRecyclerOptions.Builder<Method>()
                .setQuery(dbref,Method.class)
                .build();

        FirebaseRecyclerAdapter<Method,Myviewholder> adapter=new FirebaseRecyclerAdapter<Method, Myviewholder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull Myviewholder holder, final int position, @NonNull final Method model) {

                holder.setDate(model.getDate());
                holder.setTask(model.getTask());
                holder.setDescription(model.getDescription());
                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        key=getRef(position).getKey();
                        task=model.getTask();
                        description=model.getDescription();

                        updateTask();

                    }
                });

            }

            @NonNull
            @Override
            public Myviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.retrieve,parent,false);
                return new Myviewholder(view);
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }
    public static class Myviewholder extends RecyclerView.ViewHolder{

        View view;

        public Myviewholder(@NonNull View itemView) {
            super(itemView);
            view=itemView;
        }
        public void setTask(String task){
            TextView textView=view.findViewById(R.id.tt);
            textView.setText(task);

        }
        public  void setDescription(String de){
            TextView description=view.findViewById(R.id.ttt);
            description.setText(de);
        }
        public void setDate(String date){
            TextView da=view.findViewById(R.id.t);
            da.setText(date);

        }
    }
    private void updateTask(){

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        LayoutInflater inflater=LayoutInflater.from(this);
        View view=inflater.inflate(R.layout.update,null);
        builder.setView(view);

        final AlertDialog alertDialog=builder.create();
        final EditText task2=view.findViewById(R.id.l1);
        final EditText description2=view.findViewById(R.id.l2);
        task2.setText(task);
        task2.setSelection(task.length());

        description2.setText(description);
        description2.setSelection(description.length());

        Button delete=view.findViewById(R.id.b1);
        Button update=view.findViewById(R.id.b2);

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                task=task2.getText().toString().trim();
                description=description2.getText().toString().trim();

                String date=DateFormat.getDateInstance().format(new Date());

                Method method=new Method(task,description,key,date);
                dbref.child(key).setValue(method).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){
                            Toast.makeText(HomePage.this,"Updated successfully",Toast.LENGTH_SHORT).show();
                        }
                        else {
                            String err=task.getException().toString();
                            Toast.makeText(HomePage.this,"Failed to update "+err,Toast.LENGTH_SHORT).show();

                        }

                    }
                });
                alertDialog.dismiss();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dbref.child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){
                            Toast.makeText(HomePage.this,"Task Deleted",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            String err=task.getException().toString();
                            Toast.makeText(HomePage.this,"Failed to delete "+err,Toast.LENGTH_SHORT).show();

                        }

                    }
                });
                alertDialog.dismiss();
            }
        });
        alertDialog.show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_layout,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                auth.signOut();
                Intent intent=new Intent(HomePage.this,Login.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}