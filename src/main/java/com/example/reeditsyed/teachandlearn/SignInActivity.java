package com.example.reeditsyed.teachandlearn;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.*;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.util.ArrayList;
import java.util.List;

public class SignInActivity extends AppCompatActivity
        implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener{

    private LinearLayout profSection;
    private DatabaseReference firebase;
    private Button signOut,tutor,student;
    private TextView name,email;
    private boolean isTutor;
    private String subject,user;
    private ImageView profPic;
    private GoogleApiClient googleApiClient;
    private FirebaseAuth mAuth;
    private static final int REQ_CODE = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.google_signin_screen);

        subject = "";

        mAuth = FirebaseAuth.getInstance();

        profSection = (LinearLayout) findViewById(R.id.prof_section);
        profSection.setVisibility(View.GONE);

        signOut = (Button) findViewById(R.id.bn_logout);
        signOut.setOnClickListener(this);

        tutor = (Button) findViewById(R.id.bn_tutor);
        tutor.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                openSubjectScreen(view);
                isTutor = true;
            }
        });

        student = (Button) findViewById(R.id.bn_student);
        student.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                openSubjectScreen(view);
                isTutor = false;
            }
        });

        name = (TextView) findViewById(R.id.name);
        email = (TextView) findViewById(R.id.email);
        profPic = (ImageView) findViewById(R.id.prof_pic);

        firebase = FirebaseDatabase.getInstance().getReference();

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        signIn();

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    public void onClick(View v){
        signOut();
        setContentView(R.layout.activity_main);
        finish();
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    public void openSubjectScreen(View v){
        setContentView(R.layout.subject_screen);
    }

    public void submitSubject(View view){
        if (((RadioButton)findViewById(R.id.math)).isChecked()) subject = "Math";
        else if (((RadioButton)findViewById(R.id.science)).isChecked()) subject = "Science";
        else if (((RadioButton)findViewById(R.id.social_studies)).isChecked()) subject = "Social Studies";
        else if (((RadioButton)findViewById(R.id.spanish)).isChecked()) subject = "Spanish";
        else if (((RadioButton)findViewById(R.id.french)).isChecked()) subject = "French";
        else if (((RadioButton)findViewById(R.id.english)).isChecked()) subject = "English";
        else Toast.makeText(this,"No option selected",Toast.LENGTH_LONG).show();

        if (isTutor) {
            firebase.child(subject).child(user).setValue("Tutor");
            setContentView(new SignInActivity.OfferView(this));
        }else{
            firebase.child(subject).child(user).setValue("Student");
            setContentView(new SignInActivity.OfferView(this));
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult e){
        Toast.makeText(this,"Internet connection failed",Toast.LENGTH_LONG).show();
    }

    public void signIn(){
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, REQ_CODE);
    }

    public void signOut(){
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                updateUI(false);
            }
        });
    }

    private void handleResult(GoogleSignInResult result){

        if (result.isSuccess()){

            GoogleSignInAccount account = result.getSignInAccount();
            String name = account.getDisplayName();
            user = name;
            String email = account.getEmail();
            String img_url = null;
            try {
                img_url = account.getPhotoUrl().toString();
            }catch (NullPointerException e){
                Toast.makeText(this,"Couldn't retrieve account information.",Toast.LENGTH_LONG).show();
            }

            String uid = account.getId();
            firebase.child("user_profiles").child(uid).child("name: ").setValue(name);
            firebase.child("user_profiles").child(uid).child("email: ").setValue(email);

            this.name.setText(name);
            this.email.setText(email);
            Glide.with(this).load(img_url).into(profPic);
            updateUI(true);
        } else {
            updateUI(false);
        }

    }

    private void updateUI(boolean isLogin){
        if (isLogin)
            profSection.setVisibility(View.VISIBLE);
        else
            profSection.setVisibility(View.GONE);
    }

    private void updateUI(FirebaseUser user){
        this.name.setText(user.getDisplayName());
        this.email.setText(user.getEmail());
        Glide.with(this).load(user.getPhotoUrl().toString()).into(profPic);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == REQ_CODE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleResult(result);
        }
    }

    class OfferView extends View{

        private List<String> users;
        private List<Boolean> tutors;

        public OfferView(final Context context){
            super(context);
            users = new ArrayList<String>();
            tutors = new ArrayList<Boolean>();

            // read all offers in the subject, print on screen w/ drawText()
            firebase.child(subject).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    for (DataSnapshot child:snapshot.getChildren()) {
                        users.add(child.getKey());
                        tutors.add(child.getValue().equals("Tutor"));
                    }
                }
                @Override
                public void onCancelled(DatabaseError firebaseError) {
                    Toast.makeText(context,"Internet connection failed",Toast.LENGTH_LONG).show();
                }
            });
        }

        public void onDraw(Canvas g){
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            /*paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setColor(Color.WHITE);
            g.drawRect(0,0,getWidth(),getHeight(),paint);*/

            paint.setTypeface(Typeface.create("Arial",Typeface.ITALIC)); // set font using paint
            paint.setTextSize(getHeight()/21);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLACK);

            int index = 0;

            // read all offers in the subject, print on screen w/ drawText()
            for (String user:users){
                if (tutors.get(index) != isTutor) g.drawText(user,50,getHeight()/7,paint);
                index++;
            }

        }

    }
}
