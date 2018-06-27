package id.assel.caribengkel.activity.main;

import android.Manifest;
import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.GeoPoint;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import id.assel.caribengkel.BuildConfig;
import id.assel.caribengkel.R;
import id.assel.caribengkel.activity.auth.SplashActivity;
import id.assel.caribengkel.model.Order;
import id.assel.caribengkel.model.OrderByUserLiveData;
import id.assel.caribengkel.model.Workshop;
import id.assel.caribengkel.tools.LoginPref;
import id.assel.caribengkel.tools.Utils;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    MainViewModel viewModel;
    FirebaseUser user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        ((TextView) findViewById(R.id.tvVersion)).setText("Versi: " + BuildConfig.VERSION_NAME);

        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(MainActivity.this, SplashActivity.class);
            startActivity(intent);
            finish();
            return;
        }
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
                .withOnDrawerItemClickListener((view, position, drawerItem) -> {
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
                                .addOnCompleteListener(task -> {
                                    LoginPref.clearAll(view.getContext());
                                    Intent intent = new Intent(MainActivity.this, SplashActivity.class);
                                    startActivity(intent);
                                    finish();
                                });

                    } else {
                        System.out.println("item not registered");
                    }


                    return false;
                })
                .build();


        findViewById(R.id.buttonFindMechanic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                //get current location
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    boolean gps_enabled = false;
                    try {
                        gps_enabled = lm != null && lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    } catch (Exception ignored) {
                    }
                    boolean network_enabled = false;
                    try {
                        network_enabled = lm != null && lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                    } catch (Exception ignored) {
                    }
                    if (!gps_enabled && !network_enabled) {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                        dialog.setMessage(getResources().getString(R.string.gps_network_not_enabled));
                        dialog.setPositiveButton(getResources().getString(R.string.open_location_settings), (paramDialogInterface, paramInt) -> {
                            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(myIntent);
                        });
                        dialog.setNegativeButton(getString(R.string.Cancel), (paramDialogInterface, paramInt) -> {
                            // TODO Auto-generated method stub

                        });
                        dialog.show();
                        return;
                    }


                    view.setEnabled(false);
                    final OrderDialog orderDialog = new OrderDialog(MainActivity.this, () -> viewModel.cancelOrderRequest());
                    orderDialog.show();
                    orderDialog.progressMessage("mencari lokasi anda.");

                    LocationRequest locationRequest = new LocationRequest().setFastestInterval(2000L).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(4000L);
                    final FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
                    fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            if (locationResult != null) {
                                fusedLocationClient.removeLocationUpdates(this);
                                viewModel.postOrder(locationResult.getLastLocation(), new MainViewModel.OrderCallback() {

                                    @Override
                                    public void onOrderAccepted(@org.jetbrains.annotations.NotNull Order order) {
                                        view.setEnabled(true);
                                        orderDialog.dismiss();
                                    }

                                    @Override
                                    public void onProcessingOrder(@NonNull String progressMessage) {
                                        System.out.println("progress: " + progressMessage);
                                        orderDialog.progressMessage(progressMessage);
                                    }

                                    @Override
                                    public void onFailure(@NotNull Exception exception) {
                                        Toast.makeText(MainActivity.this, "Pesanan gagal\n" + exception.getMessage(), Toast.LENGTH_SHORT).show();
                                        view.setEnabled(true);
                                        orderDialog.dismiss();
                                    }

                                    @Override
                                    public void onCanceled() {
                                        view.setEnabled(true);
                                        //user cancel rquest, nothing todo
                                    }
                                });
                            } else {
                                System.out.println("no location updates");
                            }
                        }
                        @Override
                        public void onLocationAvailability(LocationAvailability locationAvailability) {
                            if (locationAvailability.isLocationAvailable()) {
                                System.out.println("location available");
                            } else {
                                fusedLocationClient.removeLocationUpdates(this);
                                Toast.makeText(MainActivity.this, "layanan lokasi tidak tersedia", Toast.LENGTH_SHORT).show();
                                view.setEnabled(true);
                            }
                            super.onLocationAvailability(locationAvailability);
                        }
                    }, Looper.myLooper());
                } else {
                    checkLocationPermission();
                }

            }
        });

    }


    GoogleMap mMap;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        //listen available order
        final OrderByUserLiveData listOrder = new OrderByUserLiveData(this, user.getUid());
        listOrder.observe(this, orders -> {

            //TODO show order history
        });

        Snackbar snackbar = Snackbar.make(findViewById(R.id.map), "Mekanik sedang munuju ke lokasi anda", Snackbar.LENGTH_INDEFINITE);

        AlertDialog dialog = new AlertDialog.Builder(this).setCancelable(false)
                .setMessage("Mekanik sedang munuju ke lokasi anda")
                .create();
        LiveData<Order> ongoingOrder = viewModel.onGoingOrder(listOrder);
        ongoingOrder.observe(this, order -> {
            if (order != null && order.getStatus().equals(Order.ORDER_ONGOING)) {
                mMap.clear();
                viewModel.getWorkshopLocation().removeObserver(listWorkshopObserver);
                findViewById(R.id.buttonFindMechanic).setVisibility(View.INVISIBLE);
                if (!snackbar.isShown()) snackbar.show();
//                dialog.show();
            } else {
                if (!viewModel.getWorkshopLocation().hasActiveObservers()) {
                    viewModel.getWorkshopLocation().observe(MainActivity.this, listWorkshopObserver);
                }
                findViewById(R.id.buttonFindMechanic).setVisibility(View.VISIBLE);
                if (snackbar.isShown()) snackbar.dismiss();
//                dialog.dismiss();
            }
        });
        LiveData<GeoPoint> mechanicPosition = Transformations.map(ongoingOrder, new Function<Order, GeoPoint>() {
            @Override
            public GeoPoint apply(Order input) {
                GeoPoint output = null;
                if (input != null) {
                    output = input.getMechanicPosition();
                }
                return output;

            }
        });


        mechanicPosition.observe(this, point -> {
            LatLngBounds.Builder builder = new LatLngBounds.Builder(); //for camera bound
            Order order = ongoingOrder.getValue();
            if (order != null) {
                GeoPoint clientPosition = order.getLocation();
                LatLng latLng = new LatLng(clientPosition.getLatitude(), clientPosition.getLongitude());
                mMap.addMarker(new MarkerOptions().position(latLng));
                builder.include(latLng);
            }
            if(point != null) {
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_toolbox_circled_round);
                int px = Utils.dpToPx(MainActivity.this, 50);
                bitmap = Bitmap.createScaledBitmap(bitmap, px, px, false);
                BitmapDescriptor markerIcon = BitmapDescriptorFactory.fromBitmap(bitmap);
                LatLng latLng= new LatLng(point.getLatitude(), point.getLongitude());
                MarkerOptions marker = new MarkerOptions().position(latLng).icon(markerIcon);
                mMap.addMarker(marker);

                builder.include(latLng);
                LatLngBounds bounds = builder.build();
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
            }
        });

        viewModel.getWorkshopLocation().observe(MainActivity.this, listWorkshopObserver);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkLocationPermission();
        } else {
            mMap.setMyLocationEnabled(true);
        }
    }

    Observer<List<Workshop>> listWorkshopObserver = new Observer<List<Workshop>>() {
        boolean firstInit = true;
        @Override
        public void onChanged(@Nullable List<Workshop> workshops) {
            mMap.clear();
            if (workshops != null) {

                //create marker icon

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (Workshop workshop : workshops) {
                    BitmapDescriptor markerIcon;
                    String iconSnippet;
                    if (workshop.getActive()) {
                        //larger icon
                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_toolbox_circled_round);
                        int px = Utils.dpToPx(MainActivity.this, 50);
                        bitmap = Bitmap.createScaledBitmap(bitmap, px, px, false);
                        markerIcon = BitmapDescriptorFactory.fromBitmap(bitmap);
                        iconSnippet = "Operasional";
                    } else {
                        //smaller greyscalled icon
                        Bitmap bitmap = Utils.getBitmapFromVectorDrawable(MainActivity.this, R.drawable.ic_toolbox);
                        int px = Utils.dpToPx(MainActivity.this, 20);
                        bitmap = Bitmap.createScaledBitmap(bitmap, px, px, false);
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
    };

    static int REQUEST_PERMISSIONS_LOCATION = 10;

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", (dialogInterface, i) -> {
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_LOCATION);
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_PERMISSIONS_LOCATION);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMISSIONS_LOCATION) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                checkLocationPermission();
            }
            mMap.setMyLocationEnabled(true);
        }
    }
}
