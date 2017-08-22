package com.gumbley.jonathon.findmeaplace;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ChangeRadiusActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_radius);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Display hte back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Change the title
        getSupportActionBar().setTitle(getString(R.string.change_radius_title));

        Button buttonGo = (Button) findViewById(R.id.buttonGo);
        buttonGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add the new radius to the SharedPreferences file
                int newRadius = Integer.parseInt(((EditText)findViewById(R.id.editTextRadius)).getText().toString());

                SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit();
                editor.putInt(
                        getString(R.string.preference_radius),
                        newRadius * 1000);
                editor.apply();

                Toast.makeText(getApplication(), getString(R.string.new_radius) + " " + newRadius + getString(R.string.unit_km), Toast.LENGTH_SHORT).show();
                onBackPressed();
            }
        });

        Button buttonDefault = (Button) findViewById(R.id.buttonSetDefault);
        buttonDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set the default radius in the SharedPreferences file
                SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit();
                editor.putInt(
                        getString(R.string.preference_radius),
                        getResources().getInteger(R.integer.default_radius));
                editor.apply();

                Toast.makeText(getApplication(), getString(R.string.new_radius) + " " + (getResources().getInteger(R.integer.default_radius) / 1000) + getString(R.string.unit_km), Toast.LENGTH_SHORT).show();
                onBackPressed();
            }
        });


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
