package com.example.sehirapi;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> sehirListe = new ArrayList<>();
    ListView sehirListView;
    TextView weatherTextView;
    EditText sehirAraText;
    ImageButton araButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        sehirListView = findViewById(R.id.sehirListView);
        weatherTextView = findViewById(R.id.weatherTextView);
        sehirAraText=findViewById(R.id.sehirAraText);
        araButton=findViewById(R.id.araButton);
        new SehirAPI().execute();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ListView öğelerine tıklama işlemi
        sehirListView.setOnItemClickListener((parent, view, position, id) -> {
            String secilenSehir = sehirListe.get(position);
            new HavaDurumuAPI().execute(secilenSehir);
        });

        araButton.setOnClickListener(v ->{
            String girilenSehir=sehirAraText.getText().toString().trim();
            if (!girilenSehir.isEmpty()) {
                new HavaDurumuAPI().execute(girilenSehir);
            }else {
                weatherTextView.setText("Lütfen bir şehir adı girin.");
            }
        } );


    }



    // Şehir listesini çeken API
    public class SehirAPI extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String gelen = "";
            HttpURLConnection httpURLConnection;

            try {
                URL url = new URL("https://gist.githubusercontent.com/serong/9b25594a7b9d85d3c7f7/raw/9904724fdf669ad68c07ab79af84d3a881ff8859/iller.json");
                httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                int data = inputStreamReader.read();
                while (data > 0) {
                    char c = (char) data;
                    gelen += c;
                    data = inputStreamReader.read();
                }
                return gelen;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return gelen;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                JSONObject jsonObject = new JSONObject(s);
                ArrayList<String> yeniSehirListe = new ArrayList<>();

                for (int i = 1; i <= jsonObject.length(); i++) {
                    yeniSehirListe.add(jsonObject.optString(String.valueOf(i)));
                }

                sehirListe.clear();
                sehirListe.addAll(yeniSehirListe);

                sehirListView.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, sehirListe));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Hava durumu bilgisini çeken API
    public class HavaDurumuAPI extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String gelen = "";
            HttpURLConnection httpURLConnection;

            try {
                String sehir = strings[0];  // Tıklanan şehir
                String encodedSehir = java.net.URLEncoder.encode(sehir, "UTF-8");
                String apiKey = "YOUR_API_KEY"; // weatherapi.com
                String urlStr = "https://api.weatherapi.com/v1/current.json?key=" + apiKey + "&q=" + sehir + "&aqi=no";

                URL url = new URL(urlStr);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                int data = inputStreamReader.read();
                while (data > 0) {
                    char c = (char) data;
                    gelen += c;
                    data = inputStreamReader.read();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return gelen;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                JSONObject jsonObject = new JSONObject(s);
                JSONObject location = jsonObject.getJSONObject("location"); // Şehir bilgisi
                JSONObject current = jsonObject.getJSONObject("current");   // Hava durumu bilgisi

                // Şehir adı ve sıcaklık bilgilerini aldık.
                String sehirAdi = location.getString("name");    // Şehir adı
                String sicaklik = current.getString("temp_c");   // Sıcaklık (°C)

                weatherTextView.setText("Şehir: " + sehirAdi + "\nSıcaklık: " + sicaklik + "°C");

            } catch (Exception e) {
                e.printStackTrace();
                weatherTextView.setText("Hava durumu bilgisi alınamadı!");
            }
        }
    }
}
