package id.assel.caribengkel.activity.auth;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import id.assel.caribengkel.R;
import id.assel.caribengkel.activity.MainActivity;
import id.assel.caribengkel.activity.mechanic.MechanicActivity;

public class SplashActivity extends AppCompatActivity implements SignInInterface {
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

//        Debug.debugSetLocation(this);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        final FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            firestore.document("users/"+user.getUid()).get().addOnCompleteListener(this, new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    Exception e = task.getException();
                    if (e != null) {
                        onFailure(e);
                        return;
                    }


                    if (task.isSuccessful()) {
                        System.out.println("task success");
                        String role = task.getResult().getString("role");
                        if (role != null && !role.equals("")) {
                            onGetRoleSuccess(role);
                        } else {
                            setRole(user, SplashActivity.this);
                        }


                    } else {
                        System.out.println("task failed");
                        onFailure(new Exception("no document"));
                    }
                }
            });
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
                    setRole(user, this);
                }
            } else {
                if (response != null) {
                    Toast.makeText(this, "sign in failed\n"+response, Toast.LENGTH_SHORT).show();
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



    void setRole(final FirebaseUser user, final SignInInterface callback) {
        final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        final int[] roleCode = {0};
        // custom dialog
        final AlertDialog dialog = new AlertDialog.Builder(SplashActivity.this)
                .setTitle("Anda sebagai?")
                .setCancelable(false)
                .setSingleChoiceItems(new String[]{"Pengguna", "Mekanik"}, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            roleCode[0] = i;
                        } else if (i == 1) {
                            roleCode[0] = i;
                        } else {
                            throw new NullPointerException();
                        }
                    }
                }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final String role;
                        if (roleCode[0] == 0) role = "user";
                        else if (roleCode[0] == 1) role = "mechanic";
                        else role = "";

                        Map<String, String> docData = new HashMap<>();
                        docData.put("displayName", user.getDisplayName());
                        docData.put("name", user.getEmail());
                        docData.put("role", role);

                        firestore.document("users/"+user.getUid())
                                .set(docData)
                                .addOnCompleteListener(SplashActivity.this, new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Exception e = task.getException();
                                        if (e != null) {
                                            callback.onFailure(e);
                                            return;
                                        }
                                        //todo callback

                                        if (task.isSuccessful()) {
                                            System.out.println("task success");
                                            callback.onGetRoleSuccess(role);
                                        } else {
                                            System.out.println("task failed");
                                            callback.onFailure(new Exception("no document"));
                                        }
                                    }
                                });
                    }
                }).create();
        dialog.show();
    }

    //SIGN IN INTERFACE
    @Override
    public void onGetRoleSuccess(String role) {
        System.out.println("role: "+role);
        Intent i;
        switch (role) {
            case "user":
                i = new Intent(this, MainActivity.class);
                break;
            case "mechanic":
                Toast.makeText(this, "TODO create mechanic activity", Toast.LENGTH_SHORT).show();
                i = new Intent(this, MechanicActivity.class);
                break;
            default:
                onFailure(new IllegalArgumentException("No role as: "+role));
                return;
        }
        startActivity(i);
        finish();
    }
    @Override
    public void onFailure(Exception e) {
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
