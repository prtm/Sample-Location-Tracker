package me.pritam.rltl.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.pritam.rltl.L;
import me.pritam.rltl.R;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String grpName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        if (getIntent() != null) {
            grpName = getIntent().getExtras().getString("infoName");
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        L.logD("map ready");
        mMap = googleMap;
        getData();
        // Add a marker in Sydney and move the camera

    }

    private void getData() {
        L.logD("getData called " + grpName);
        DatabaseReference drf = FirebaseDatabase.getInstance().getReference("groups").child(grpName).child("members");
        final List<String> membersList = new ArrayList<>();
        drf.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    GenericTypeIndicator<Map<String, String>> genericTypeIndicator = new GenericTypeIndicator<Map<String, String>>() {
                    };

                    Map<String, String> map = dataSnapshot.getValue(genericTypeIndicator);
                    membersList.addAll(map.values());
//                    L.shortToast(MapsActivity.this, membersList.toString());
//                    L.logD(membersList.toString());

                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
                    for (final String s : membersList) {
                        DatabaseReference drfs = databaseReference.child(s).child("location");
                        drfs.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    GenericTypeIndicator<Map<String, String>> genericTypeIndicator = new GenericTypeIndicator<Map<String, String>>() {
                                    };

                                    Map<String, String> map = dataSnapshot.getValue(genericTypeIndicator);
                                    String lat = map.get("lat");
                                    String lon = map.get("long");
                                    L.logD(lat + "," + lon);
                                    LatLng firstPerson = new LatLng(Double.parseDouble(lat), Double.parseDouble(lon));
                                    mMap.addMarker(new MarkerOptions().position(firstPerson)).setSnippet(s);
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstPerson, 14f));

                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
}
