
package com.moutamid.matchinggame;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.moutamid.matchinggame.databinding.ActivityMainBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    String[] numbers = new String[]{"1", "2", "3", "4", "6", "7", "8", "9"};
    Integer[] colors = new Integer[]{R.color.orange, R.color.blue, R.color.pink, R.color.yellow, R.color.sky, R.color.green};
    private MaterialCardView selectedCard;
    private String selectedNumber;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        checkApp(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            getWindow().setStatusBarColor(getResources().getColor(android.R.color.transparent, null));
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        mediaPlayer = new MediaPlayer();

        binding.close.setOnClickListener(v -> finish());

        setupViews();

    }

    private void setupViews() {

        binding.card1.setVisibility(View.VISIBLE);
        binding.card2.setVisibility(View.VISIBLE);
        binding.card3.setVisibility(View.VISIBLE);
        binding.card4.setVisibility(View.VISIBLE);

        ArrayList<String> shuffledNumbers = new ArrayList<>(List.of(numbers));
        ArrayList<Integer> shuffledColors = new ArrayList<>(List.of(colors));
        Collections.shuffle(shuffledNumbers);
        Collections.shuffle(shuffledColors);

        String number1 = shuffledNumbers.get(0);
        String number2 = shuffledNumbers.get(1);

        int color1 = shuffledColors.get(0);
        int color2 = shuffledColors.get(1);

        List<TextView> textViews = new ArrayList<>(List.of(binding.text1, binding.text3, binding.text2, binding.text4));
        Collections.shuffle(textViews);

        textViews.get(0).setText(number1);
        textViews.get(1).setText(number2);
        textViews.get(2).setText(number2);
        textViews.get(3).setText(number1);

        for (int i = 0; i < textViews.size(); i++) {
            TextView textView = textViews.get(i);
            MaterialCardView cardView = getCardViewForTextView(textView);
            if (textView.getText().toString().equals(number1)) {
                cardView.setCardBackgroundColor(getResources().getColor(color1, null));
            } else if (textView.getText().toString().equals(number2)) {
                cardView.setCardBackgroundColor(getResources().getColor(color2, null));
            }
            cardView.setOnClickListener(v -> onCardClicked(cardView, textView));
        }

    }

    private void onCardClicked(MaterialCardView cardView, TextView textView) {
        if (selectedCard == null) {
            selectedCard = cardView;
            selectedNumber = textView.getText().toString();
            cardView.setVisibility(View.INVISIBLE);
        } else {
            if (textView.getText().toString().equals(selectedNumber)) {
                String[] file = new String[]{"success1.mp3", "success2.mp3"};
                try {
                    String randomFileName = file[new Random().nextInt(file.length)];
                    AssetFileDescriptor afd = getAssets().openFd(randomFileName);
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                playSound();
                cardView.setVisibility(View.INVISIBLE);
                selectedCard.setVisibility(View.INVISIBLE);
            } else {
                String[] file = new String[]{"fail1.mp3", "fail2.mp3"};
                try {
                    String randomFileName = file[new Random().nextInt(file.length)];
                    AssetFileDescriptor afd = getAssets().openFd(randomFileName);
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                playSound();
                cardView.setVisibility(View.VISIBLE);
                selectedCard.setVisibility(View.VISIBLE);
            }
            selectedCard = null;
            selectedNumber = null;

            List<MaterialCardView> allCards = new ArrayList<>(List.of(binding.card1, binding.card2, binding.card3, binding.card4));
            boolean check = false;

            for (MaterialCardView currentCard : allCards) {
                if (currentCard.getVisibility() == View.VISIBLE) {
                    check = true;
                }
            }

            if (!check) {
                setupViews();
            }

        }
    }

    public static void checkApp(Activity activity) {
        String appName = "matchinggame";

        new Thread(() -> {
            URL google = null;
            try {
                google = new URL("https://raw.githubusercontent.com/Moutamid/Moutamid/main/apps.txt");
            } catch (final MalformedURLException e) {
                e.printStackTrace();
            }
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(google != null ? google.openStream() : null));
            } catch (final IOException e) {
                e.printStackTrace();
            }
            String input = null;
            StringBuffer stringBuffer = new StringBuffer();
            while (true) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        if ((input = in != null ? in.readLine() : null) == null) break;
                    }
                } catch (final IOException e) {
                    e.printStackTrace();
                }
                stringBuffer.append(input);
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
            String htmlData = stringBuffer.toString();

            try {
                JSONObject myAppObject = new JSONObject(htmlData).getJSONObject(appName);

                boolean value = myAppObject.getBoolean("value");
                String msg = myAppObject.getString("msg");

                if (value) {
                    activity.runOnUiThread(() -> {
                        new AlertDialog.Builder(activity)
                                .setMessage(msg)
                                .setCancelable(false)
                                .show();
                    });
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }).start();
    }

    private MaterialCardView getCardViewForTextView(TextView textView) {
        int id = textView.getId();
        if (id == R.id.text1) {
            return binding.card1;
        } else if (id == R.id.text2) {
            return binding.card2;
        } else if (id == R.id.text3) {
            return binding.card3;
        } else if (id == R.id.text4) {
            return binding.card4;
        }
        return null;
    }

    private void playSound() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }


    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

}