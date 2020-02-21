package com.Group17;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class TempoSetupActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.tempo_screen);


        findViewById(R.id.b_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText tempoInput = findViewById(R.id.text_tempo);
                int tempo = Integer.parseInt(tempoInput.getText().toString());
                EditText densityInput = findViewById(R.id.text_density);
                double density = Double.parseDouble(densityInput.getText().toString());

                RandomTempoGenerator myRTG = new RandomTempoGenerator(50, density); //no reason to get length from user
                //be sure to actually use the tempo variable later
                Intent intent = new Intent(TempoSetupActivity.this, GameActivity.class);
                intent.putExtra("tempo", tempo);
                intent.putExtra("density", density);
                intent.putExtra("songName", "tempo_game_reserved_name");
                intent.putExtra("repeatingGame", false);
                startActivity(intent);
                //check the settings, then pass the parameters and start an activity, RTG should probably be instantiated inside to be consistent, pass in the arguments

            }
        });

    }
}
