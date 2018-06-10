package id.assel.caribengkel.activity.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

import id.assel.caribengkel.R;
import id.assel.caribengkel.activity.MainActivity;

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

        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
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
                    Intent i = new Intent(this, MainActivity.class);
                    startActivity(i);
                    finish();
                }
            } else {
                if (response != null) {
                    Toast.makeText(this, "sign in failed\nresponse:"+response, Toast.LENGTH_SHORT).show();
                    startActivityForResult(authUI, RC_SIGN_IN);
                } else {
                    finish();
                }
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }

}
