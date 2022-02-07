package com.example.hikinghelperni;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

    public void onProfileItemClick(MenuItem item) {
        if (item.getItemId() == R.id.action_profile) {
            Toast.makeText(MainActivity.this, "you clicked on button1",
                    Toast.LENGTH_SHORT).show();
        }

        if (item.getItemId() == R.id.action_sign_out) {
            signOut();
        }

        if (item.getItemId() == R.id.action_sign_in) {
            Intent intent = new Intent(this, FirebaseUIActivity.class);
            startActivity(intent);
        }
    }

    public void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(task -> {
                    task.getResult();
                    this.invalidateOptionsMenu();
                    Toast.makeText(MainActivity.this, "Sign Out Successful",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void CustomiseProfileMenu(Boolean isAuth, Menu menu) {
        //Show these if user is authenticated
        menu.findItem(R.id.action_profile).setVisible(isAuth);
        menu.findItem(R.id.action_sign_out).setVisible(isAuth);
        //Show this if user is not
        menu.findItem(R.id.action_sign_in).setVisible(!isAuth);
    }
}