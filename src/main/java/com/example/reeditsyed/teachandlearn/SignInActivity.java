package com.example.reeditsyed.teachandlearn;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
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
    private String subject,user,uid;
    private ImageView profPic;
    private GoogleApiClient googleApiClient;
    private FirebaseAuth mAuth;
    private static final int REQ_CODE = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.google_signin_screen);

        subject = "";
        user = "";
        uid = "";

        mAuth = FirebaseAuth.getInstance();

        profSection = (LinearLayout) findViewById(R.id.prof_section);

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
                .requestIdToken(getString(R.string.default_web_client_id))
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

    public void openTutorProfileInfoScreen(View view){
        isTutor = true;
        setContentView(R.layout.profile_info_screen);
    }

    public void openStudentProfileInfoScreen(View view){
        isTutor = false;
        setContentView(R.layout.profile_info_screen);
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
        else{
            Toast.makeText(this,"No option selected",Toast.LENGTH_LONG).show();
            return;
        }

        ScrollView sView = new ScrollView(this);
        sView.addView(new SignInActivity.OfferView(this));

        if (isTutor) {
            firebase.child(subject).child(user).push().setValue("Tutor");
            setContentView(sView);
        }else if (!isTutor){
            firebase.child(subject).child(user).push().setValue("Student");
            setContentView(sView);
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

    public void addInfoToProfile(View view){

        String address = ((EditText)findViewById(R.id.address)).getText().toString()+", "+
                ((EditText)findViewById(R.id.citystate)).getText().toString()+", "+
                ((EditText)findViewById(R.id.zipcode)).getText().toString();
        String phone = ((EditText)findViewById(R.id.phone)).getText().toString();

        firebase.child("user_profiles").child(uid).child("address: ").push().setValue(address);
        firebase.child("user_profiles").child(uid).child("phone: ").push().setValue(phone);

        openSubjectScreen(view);

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

            uid = account.getServerAuthCode();

            firebase.child("user_profiles").child(user).push().child("name: ").push().setValue(name);
            firebase.child("user_profiles").child(user).child("email: ").push().setValue(email);
            firebase.child("user_profiles").child(user).child("profPic: ").push().setValue(img_url);

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
        if (user != null) {
            this.name.setText(user.getDisplayName());
            this.email.setText(user.getEmail());
            Glide.with(this).load(user.getPhotoUrl().toString()).into(profPic);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == REQ_CODE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
                handleResult(result);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }

    class ProfileView extends View{

        private String name,img_url,email;

        public ProfileView(final Context context,String name){
            super(context);
            this.name = name;
        }

        @Override
        public void onDraw(Canvas canvas){



        }

    }

    class OfferView extends View{

        private List<String> users;
        private List<Boolean> tutors;

        public OfferView(final Context context){
            super(context);
            users = new ArrayList<>();
            tutors = new ArrayList<>();

            // read all offers in the subject, print on screen w/ drawText()
            firebase.child(subject).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Log.i("Info",snapshot.hasChildren()+"");
                    for (DataSnapshot child:snapshot.getChildren()) { // no values are being assigned to users
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

        @Override
        public void onDraw(Canvas canvas){
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawPaint(paint);

            paint.setColor(Color.BLACK);
            paint.setTextSize(100);
            canvas.drawText("Some Text", 10, 125, paint);

            int index = 0;

            // read all offers in the subject, print on screen w/ drawText()
            for (String user:users){
                if (tutors.get(index) != isTutor)
                    canvas.drawText(user,50,getHeight()/7,paint);
                index++;
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent e){
            if (e.getAction() == MotionEvent.ACTION_UP) return true;

            int optionChosen = (int)(e.getY()/7);
            List<String> offers = new ArrayList<>();
            int index = 0;

            for (String user:users){
                if (tutors.get(index) != isTutor)
                    offers.add(user);
                index++;
                if (offers.size() == 7)
                    break;
            }



            return true;
        }

    }
}
