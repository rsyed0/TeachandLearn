package com.example.reeditsyed.teachandlearn;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import android.content.Intent;

import com.google.android.gms.common.SignInButton;
import static android.R.attr.value;

public class MainActivity extends AppCompatActivity {

    private SignInButton signIn;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signIn = (SignInButton) findViewById(R.id.sign_in_button);
        signIn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                attemptSignIn(view);
            }
        });
    }

    public void attemptSignIn(View view){
        Intent myIntent = new Intent(MainActivity.this, SignInActivity.class);
        myIntent.putExtra("key", value); //Optional parameters
        MainActivity.this.startActivity(myIntent);
    }

}