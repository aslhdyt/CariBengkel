package id.assel.caribengkel.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import id.assel.caribengkel.R;
import id.assel.caribengkel.activity.auth.SplashActivity;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Intent intent = new Intent(MainActivity.this, SplashActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        //drawer item
        System.out.println("photoUrl: "+user.getPhotoUrl());
        final AccountHeader header = new AccountHeaderBuilder()
                .withActivity(this)
                .addProfiles(
                        new ProfileDrawerItem().withName(user.getDisplayName()).withEmail(user.getEmail()).withIcon(user.getPhotoUrl()))
                .withCompactStyle(true)
                .withSelectionListEnabledForSingleProfile(false)
                .withTextColor(getResources().getColor(R.color.md_black_1000))
                .withProfileImagesClickable(false)
                .build();
        final PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.drawer_item_home);
        final SecondaryDrawerItem item2 = new SecondaryDrawerItem().withIdentifier(2).withName(R.string.drawer_item_settings);
        final SecondaryDrawerItem item3 = new SecondaryDrawerItem().withName(R.string.drawer_item_signout);

        //drawer body
        new DrawerBuilder().withActivity(this).withTranslucentStatusBar(false).withDisplayBelowStatusBar(true)
            .withToolbar(toolbar).withActionBarDrawerToggle(true)
                .withAccountHeader(header)
            .addDrawerItems(
                item1,
                new DividerDrawerItem(),
                item2,
                item3)
            .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                @Override
                public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                    if (drawerItem == item1) {
                        System.out.println("item1 clicked");
                    } else  if (drawerItem == item2) {
                        System.out.println("item2 clicked");
                    } else if (drawerItem == item3) {
                        AuthUI.getInstance()
                            .signOut(MainActivity.this)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                public void onComplete(@NonNull Task<Void> task) {
                                    Intent intent = new Intent(MainActivity.this, SplashActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                    } else {
                        System.out.println("item not registered");
                    }


                    return false;
                }
            })
            .build();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        GoogleMap mMap = googleMap;


        LatLng jakarta = new LatLng(-6.21462, 106.84513);
        mMap.addMarker(new MarkerOptions().position(jakarta).title("Marker in Jakarta"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(jakarta, 10f));
    }
}
