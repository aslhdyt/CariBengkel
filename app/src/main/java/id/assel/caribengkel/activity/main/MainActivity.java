package id.assel.caribengkel.activity.main;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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

import java.util.List;

import id.assel.caribengkel.R;
import id.assel.caribengkel.activity.auth.SplashActivity;
import id.assel.caribengkel.model.Workshop;
import id.assel.caribengkel.tools.LoginPref;
import id.assel.caribengkel.tools.Utils;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    MainViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);


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
        final PrimaryDrawerItem item2 = new PrimaryDrawerItem().withIdentifier(2).withName("Profile");
        final PrimaryDrawerItem item3= new PrimaryDrawerItem().withIdentifier(3).withName("Order History");
        final SecondaryDrawerItem item4 = new SecondaryDrawerItem().withIdentifier(2).withName("Bantuan");
        final SecondaryDrawerItem item5 = new SecondaryDrawerItem().withName(R.string.drawer_item_signout);

        //drawer body
        new DrawerBuilder().withActivity(this).withTranslucentStatusBar(false).withDisplayBelowStatusBar(true)
            .withToolbar(toolbar).withActionBarDrawerToggle(true)
                .withAccountHeader(header)
            .addDrawerItems(
                item1,
                item2,
                item3,
                new DividerDrawerItem(),
                item4,
                item5)
            .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                @Override
                public boolean onItemClick(final View view, int position, IDrawerItem drawerItem) {
                    if (drawerItem == item1) {
                        System.out.println("item1 clicked");
                    } else  if (drawerItem == item2) {
                        System.out.println("item2 clicked");
                    } else  if (drawerItem == item3) {
                        System.out.println("item3 clicked");
                    } else  if (drawerItem == item4) {
                        System.out.println("item4 clicked");
                    } else if (drawerItem == item5) {
                        AuthUI.getInstance()
                            .signOut(MainActivity.this)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                public void onComplete(@NonNull Task<Void> task) {
                                    LoginPref.clearAll(view.getContext());
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
        final GoogleMap mMap = googleMap;


        viewModel.getWorkshopLocation().observe(this, new Observer<List<Workshop>>() {
            boolean firstInit = true;
            @Override
            public void onChanged(@Nullable List<Workshop> workshops) {
                if (workshops != null) {

                    int px =  Utils.dpToPx(MainActivity.this, 40);
                    //create marker icon
                    Bitmap bitmap = Utils.getBitmapFromVectorDrawable(MainActivity.this, R.drawable.ic_toolbox);
                    bitmap = Bitmap.createScaledBitmap(bitmap,px,px, false);
                    BitmapDescriptor markerIcon = BitmapDescriptorFactory.fromBitmap(bitmap);

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for (Workshop workshop : workshops) {
                        LatLng latLng = new LatLng(workshop.getLatLng().getLatitude(), workshop.getLatLng().getLongitude());
                        mMap.addMarker(new MarkerOptions().position(latLng).title(workshop.getName()).icon(markerIcon));

                        if (firstInit) builder.include(latLng);
                    }

                    if (firstInit) {
                        LatLngBounds bounds = builder.build();
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
                        firstInit = false;
                    }
                } else {
                    mMap.clear();
                }
            }
        });
    }


}
