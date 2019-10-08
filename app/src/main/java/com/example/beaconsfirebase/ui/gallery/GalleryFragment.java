package com.example.beaconsfirebase.ui.gallery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import com.example.beaconsfirebase.R;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.maps.SupportMapFragment;

import java.util.List;

public class GalleryFragment extends Fragment implements PermissionsListener, OnMapReadyCallback {

    private MapboxMap mapboxMap;
    private MapView mapView;
    private PermissionsManager permissionsManager;
    private GalleryViewModel galleryViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        galleryViewModel =
                ViewModelProviders.of(this).get(GalleryViewModel.class);

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(getContext(), getString(R.string.access_token));

        View root = inflater.inflate(R.layout.fragment_gallery, container, false);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = (MapView) getView().findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        ImageView tilt = getView().findViewById(R.id.tilt);
        ImageView noTilt = getView().findViewById(R.id.noTilt);
        noTilt.setVisibility(View.INVISIBLE);
        noTilt.setClickable(false);
        tilt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                noTilt.setVisibility(View.VISIBLE);
                noTilt.setClickable(true);
                tilt.setVisibility(View.INVISIBLE);
                tilt.setClickable(false);
                //tilt
                CameraPosition cameraPosition = new CameraPosition.Builder().tilt(60).build();
                mapboxMap.setCameraPosition(cameraPosition);
            }
        });
        noTilt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tilt.setVisibility(View.VISIBLE);
                tilt.setClickable(true);
                noTilt.setVisibility(View.INVISIBLE);
                noTilt.setClickable(false);
                //tilt
                CameraPosition cameraPosition = new CameraPosition.Builder().tilt(0).build();
                mapboxMap.setCameraPosition(cameraPosition);
            }
        });
        createMap(savedInstanceState);
    }

    private void createMap(@Nullable Bundle savedInstanceState){

// Create supportMapFragment
        SupportMapFragment mapFragment;
        if (savedInstanceState == null) {

// Create fragment
            final FragmentTransaction transaction = getFragmentManager().beginTransaction();

// Build a Mapbox map
            MapboxMapOptions options = MapboxMapOptions.createFromAttributes(getContext(), null);
            options.camera(new CameraPosition.Builder()
                    .target(new LatLng(19.283478, -99.135122))
                    .zoom(18).tilt(0)
                    .build());

// Create map fragment
            mapFragment = SupportMapFragment.newInstance(options);

// Add map fragment to parent container
            transaction.add(R.id.fragment_gallery, mapFragment, "com.mapbox.map");
            transaction.commit();
        } else {
            mapFragment = (SupportMapFragment) getFragmentManager().findFragmentByTag("com.mapbox.map");
        }

        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(@NonNull MapboxMap mapboxMap) {
                    GalleryFragment.this.mapboxMap = mapboxMap;
                    mapboxMap.setStyle(new Style.Builder().fromUrl("mapbox://styles/evacuacion/ck1er07dh3k3d1cldoetbwjfs"),
                            new Style.OnStyleLoaded() {
                                @Override
                                public void onStyleLoaded(@NonNull Style style) {
                                    enableLocationComponent(style);
                                }
                            });
                }
            });
        }
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(getContext())) {
            MapboxMapOptions options = MapboxMapOptions.createFromAttributes(getContext(), null);

            // Get an instance of the LocationComponent.
            LocationComponent locationComponent = mapboxMap.getLocationComponent();
            /*LocationComponentActivationOptions locationComponentActivationOptions =
                    LocationComponentActivationOptions.builder(getContext(), loadedMapStyle)
                            .useDefaultLocationEngine(false)
                            .build();*/

            // Activate the LocationComponent
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(getContext(), loadedMapStyle).useDefaultLocationEngine(true).build());



            // Enable the LocationComponent so that it's actually visible on the map
            locationComponent.setLocationComponentEnabled(true);

            // Set the LocationComponent's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);


            // Set the LocationComponent's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(getActivity());
        }
    }


    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            if (mapboxMap.getStyle() != null) {
                enableLocationComponent(mapboxMap.getStyle());
            }
        }else {
            //Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            getActivity().finish();
        }
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(new Style.Builder().fromUrl("mapbox://styles/evacuacion/ck1er07dh3k3d1cldoetbwjfs"),
                new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        enableLocationComponent(style);
                    }
                });

    }


    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


}