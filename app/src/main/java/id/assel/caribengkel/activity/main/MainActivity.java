package id.assel.caribengkel.activity.main;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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

import org.jetbrains.annotations.NotNull;

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
        viewModel.setUser(user);
        //drawer item
        System.out.println("photoUrl: " + user.getPhotoUrl());
        final AccountHeader header = new AccountHeaderBuilder()
                .withActivity(this)
                .addProfiles(
                        new ProfileDrawerItem().withName(user.getDisplayName()).withEmail(user.getEmail()).withIcon(user.getPhotoUrl()))
                .withCompactStyle(true)
                .withSelectionListEnabledForSingleProfile(false)
                .withTextColor(getResources().getColor(R.color.md_black_1000))
                .withProfileImagesClickable(false)
                .build();
        final PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.drawer_item_home).withSelectable(false);
        final PrimaryDrawerItem item2 = new PrimaryDrawerItem().withIdentifier(2).withName("Profile").withSelectable(false);
        final PrimaryDrawerItem item3 = new PrimaryDrawerItem().withIdentifier(3).withName("Order History").withSelectable(false);
        final SecondaryDrawerItem item4 = new SecondaryDrawerItem().withIdentifier(2).withName("Bantuan").withSelectable(false);
        final SecondaryDrawerItem item5 = new SecondaryDrawerItem().withName(R.string.drawer_item_signout).withSelectable(false);

        //drawer body
        new DrawerBuilder().withActivity(this).withTranslucentStatusBar(false).withDisplayBelowStatusBar(true).withSelectedItem(-1)
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
                        } else if (drawerItem == item2) {
                            System.out.println("item2 clicked");
                        } else if (drawerItem == item3) {
                            System.out.println("item3 clicked");
                        } else if (drawerItem == item4) {
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


        findViewById(R.id.buttonFindMechanic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get current location
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(view.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_CONTACTS},
                            REQUEST_PERMISSIONS_LOCATION);
                    return;
                }
                FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
                fusedLocationClient.getLastLocation().addOnCompleteListener(MainActivity.this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        //TODO result is null
                        viewModel.postOrder(task.getResult(), new MainViewModel.UserActivivityCallback() {
                            @Override
                            public void onOrderPosted() {
                                //TODO wait ui
                                    Toast.makeText(MainActivity.this, "TODO create waiting UI", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(@NotNull Exception exception) {
                                Toast.makeText(MainActivity.this, "Pesanan gagal\nreason: "+exception.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        final GoogleMap mMap = googleMap;


        viewModel.getWorkshopLocation().observe(this, new Observer<List<Workshop>>() {
            boolean firstInit = true;
            @Override
            public void onChanged(@Nullable List<Workshop> workshops) {
                mMap.clear();
                if (workshops != null) {

                    //create marker icon
                    Bitmap bitmap = Utils.getBitmapFromVectorDrawable(MainActivity.this, R.drawable.ic_toolbox);

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for (Workshop workshop : workshops) {
                        BitmapDescriptor markerIcon;
                        String iconSnippet;
                        if (workshop.getActive()) {
                            //larger icon
                            int px =  Utils.dpToPx(MainActivity.this, 40);
                            bitmap = Bitmap.createScaledBitmap(bitmap,px,px, false);
                            markerIcon = BitmapDescriptorFactory.fromBitmap(bitmap);
                            iconSnippet = "Operasional";
                        } else {
                            //smaller greyscalled icon
                            int px =  Utils.dpToPx(MainActivity.this, 20);
                            bitmap = Bitmap.createScaledBitmap(bitmap,px,px, false);
                            markerIcon = BitmapDescriptorFactory.fromBitmap(Utils.toGrayscale(bitmap));
                            iconSnippet = "Tutup";
                        }
                        LatLng latLng = new LatLng(workshop.getLatLng().getLatitude(), workshop.getLatLng().getLongitude());
                        mMap.addMarker(new MarkerOptions().position(latLng).title(workshop.getName()).snippet(iconSnippet).icon(markerIcon));

                        if (firstInit) builder.include(latLng);
                    }

                    if (firstInit) {
                        LatLngBounds bounds = builder.build();
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
                        firstInit = false;
                    }
                }
            }
        });
    }

    static int REQUEST_PERMISSIONS_LOCATION = 10;
}
