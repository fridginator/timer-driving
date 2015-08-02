package com.burke.kelv.timerdriving;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by kelv on 19/03/2015.
 */
public class DetailsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.empty_activity);
        if (savedInstanceState == null) {
            // MainFragment fragment = new MainFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.containerEmpty, new DetailsFragment(), "DetailsFragment")
                    .commit();
        }
    }

}
