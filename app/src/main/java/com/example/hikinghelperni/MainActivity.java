package com.example.hikinghelperni;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.hikinghelperni.ui.profile.ProfileFragment;
import com.firebase.ui.auth.AuthUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.hikinghelperni.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_trails, R.id.navigation_log_hikes, R.id.navigation_saved_lists)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        refreshFragmentView();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        mFirebaseAuth = FirebaseAuth.getInstance();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar, menu);

        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
        CustomiseProfileMenu(currentUser != null, menu);
        return false;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_profile) {
            ProfileFragment nextFragment = new ProfileFragment();
            FragmentManager navFragmentManager = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main).getChildFragmentManager();
            navFragmentManager.beginTransaction()
                           .replace(R.id.nav_host_fragment_activity_main, nextFragment)
                           .addToBackStack("ToProfileFragment")
                           .commit();
        }

        if (item.getItemId() == R.id.action_sign_out) {
            signOut();
        }

        if (item.getItemId() == R.id.action_sign_in) {
            Intent intent = new Intent(this, FirebaseUIActivity.class);
            startActivity(intent);
        }
        if (item.getItemId() == android.R.id.home) {
            FragmentManager navFragmentManager = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main).getChildFragmentManager();
            navFragmentManager.popBackStack();
        }
        return false;
    }

    public void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(task -> {
                    task.getResult();
                    this.invalidateOptionsMenu();
                    refreshFragmentView();
                    Toast.makeText(MainActivity.this, "Sign Out Successful",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void refreshFragmentView() {
        Fragment parentFragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        Fragment fragment = parentFragment.getChildFragmentManager().getFragments().get(0);
        FragmentTransaction transaction = parentFragment.getChildFragmentManager().beginTransaction();
        if (Build.VERSION.SDK_INT >= 26) {
            transaction.setReorderingAllowed(false);
        }
        transaction.detach(fragment).commit();
        transaction = parentFragment.getChildFragmentManager().beginTransaction();
        transaction.attach(fragment).commit();
    }

    private void CustomiseProfileMenu(Boolean isAuth, Menu menu) {
        //Show these if user is authenticated
        menu.findItem(R.id.action_profile).setVisible(isAuth);
        menu.findItem(R.id.action_sign_out).setVisible(isAuth);
        //Show this if user is not
        menu.findItem(R.id.action_sign_in).setVisible(!isAuth);
    }
}