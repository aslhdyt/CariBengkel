package id.assel.caribengkel.activity.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import id.assel.caribengkel.R;
import id.assel.caribengkel.activity.main.MainActivity;

public class SplashActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;
    private final Intent authUI = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setLogo(R.drawable.toolbox)      // Set logo drawable
            .setAvailableProviders(Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build(),
                    new AuthUI.IdpConfig.GoogleBuilder().build()
            )).build();


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        only for populate data to firestore
//        id.assel.caribengkel.debug.Debug.debugSetLocation(this);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        final FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        } else {
            startActivityForResult(authUI, RC_SIGN_IN);
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {

                    Map<String, String> docData = new HashMap<>();
                    docData.put("displayName", user.getDisplayName());
                    docData.put("name", user.getEmail());
                    FirebaseFirestore.getInstance().document("users/"+user.getUid())
                        .set(docData)
                        .addOnCompleteListener(SplashActivity.this, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Exception e = task.getException();
                                if (e != null) {
                                    onLoginFailure(e);
                                    return;
                                }
                                //todo callback

                                if (task.isSuccessful()) {
                                    System.out.println("task success");
                                    Intent i = new Intent(SplashActivity.this, MainActivity.class);
                                    startActivity(i);
                                    finish();
                                } else {
                                    System.out.println("task failed");
                                    onLoginFailure(new Exception("no connection"));
                                }
                            }
                        });
                }
            } else {
                if (response != null) {
                    Toast.makeText(this, "sign in failed\n"+response, Toast.LENGTH_SHORT).show();
                    startActivityForResult(authUI, RC_SIGN_IN);
                } else {
                    finish();
                }
            }
        }
    }


    public void onLoginFailure(Exception e) {
        e.printStackTrace();
        Toast.makeText(this, "Sign in failed\n"+e.getMessage(), Toast.LENGTH_SHORT).show();
        AuthUI.getInstance().signOut(this).addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                startActivityForResult(authUI, RC_SIGN_IN);
            }
        });
    }




}
