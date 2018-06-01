package id.assel.caribengkel.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import id.assel.caribengkel.R;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.drawer_item_home);
        SecondaryDrawerItem item2 = new SecondaryDrawerItem().withIdentifier(2).withName(R.string.drawer_item_settings);

        new DrawerBuilder().withActivity(this).withTranslucentStatusBar(false).withDisplayBelowStatusBar(true)
                .withToolbar(toolbar).withActionBarDrawerToggle(true)
                .addDrawerItems(
                    item1,
                    new DividerDrawerItem(),
                    item2,
                    new SecondaryDrawerItem().withName(R.string.drawer_item_settings))
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        Toast.makeText(MainActivity.this, "position: "+position+"\ndrawerItem: "+drawerItem.toString(), Toast.LENGTH_SHORT).show();
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
