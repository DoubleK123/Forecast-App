package com.example.android.androidassignments;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class WeatherForecast extends AppCompatActivity {
    private ProgressBar progressBar;
    private ImageView img_view;
    private TextView current_t;
    private TextView min_t;
    private TextView max_t;
    private TextView city_name;
    private List<String> cityList;


    public static String ACTIVITY_NAME = "WeatherForecast.java";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_forecast);
        current_t = findViewById(R.id.current_temp);
        min_t = findViewById(R.id.min_temp);
        max_t = findViewById(R.id.max_temp);
        img_view = findViewById(R.id.weather_img);
        city_name = findViewById(R.id.city_n);

        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        get_a_city();


    }

    public void get_a_city() {


        cityList = Arrays.asList(getResources().getStringArray(R.array.cities));
        final Spinner citySpinner = findViewById(R.id.citySpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.cities, android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setAdapter(adapter);

        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int i, long id) {
                new ForecastQuery(cityList.get(i)).execute("this will go to background");
                city_name.setText(cityList.get(i) + " Weather");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });
    }

    public class ForecastQuery extends AsyncTask<String, Integer, String> {
        private Bitmap picture;
        protected String city;
        private String current_temp;
        private String max_temp;
        private String min_temp;

        ForecastQuery(String city) {
            this.city = city;
        }


        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL("https://api.openweathermap.org/" +
                        "data/2.5/weather?q=" + this.city  +"," +
                        "ca&APPID=79cecf493cb6e52d25bb7b7050ff723c&" +"mode=xml&units=metric");
                HttpsURLConnection conn = (HttpsURLConnection)
                        url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                InputStream in = conn.getInputStream();
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setFeature(
                            XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    parser.setInput(in, null);
                    int type;
                    //While you're not at the end of the document:
                    while ((type = parser.getEventType()) !=
                            XmlPullParser.END_DOCUMENT) {
                        //Are you currently at a Start Tag?
                        if (parser.getEventType() == XmlPullParser.START_TAG) {
                            if (parser.getName().equals("temperature")) {
                                current_temp = parser.getAttributeValue(null, "value");
                                publishProgress(25);
                                min_temp = parser.getAttributeValue(null, "min");
                                publishProgress(50);
                                max_temp = parser.getAttributeValue(null, "max");
                                publishProgress(75);
                            } else if (parser.getName().equals("weather")) {
                                String iconName = parser.getAttributeValue(null, "icon");
                                String fileName = iconName + ".png";
                                Log.i(ACTIVITY_NAME, "Looking for file: " + fileName);
                                if (fileExistence(fileName)) {
                                    FileInputStream fis = null;
                                    try {
                                        fis = openFileInput(fileName);

                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                    Log.i(ACTIVITY_NAME, "Found the file locally");
                                    picture = BitmapFactory.decodeStream(fis);
                                } else {
                                    String iconUrl =
                                            "https://openweathermap.org/img/w/" + fileName;
                                    picture = getPicture(new URL(iconUrl));
                                    FileOutputStream outputStream =
                                            openFileOutput(fileName, Context.MODE_PRIVATE);
                                    picture.compress(Bitmap.CompressFormat.PNG, 80,
                                            outputStream);
                                    Log.i(ACTIVITY_NAME,
                                            "Downloaded the file from the Internet");
                                    outputStream.flush();
                                    outputStream.close();
                                }
                                publishProgress(100);
                            }
                        }
                        // Go to the next XML event
                        parser.next();
                    }
                } finally {
                    in.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return "";
        }

        private boolean fileExistence(String fileName) {
            File file = getBaseContext().getFileStreamPath(fileName);
            return file.exists();


        }

        public Bitmap getPicture(URL url) {
            HttpsURLConnection connection = null;
            try {
                connection = (HttpsURLConnection) url.openConnection();
                connection.connect();
                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    return BitmapFactory.decodeStream(connection.getInputStream());
                } else
                    return null;
            } catch (Exception e) {
                return null;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }


        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            progressBar.setProgress(values[0]);
        }
        @Override
        protected void onPostExecute(String a) {
            progressBar.setVisibility(View.INVISIBLE);
            img_view.setImageBitmap(picture);
            current_t.setText( "current temp: " + current_temp + "C" );
            min_t.setText("minimum temp: " + min_temp + "C ");
            max_t.setText("maximum temp: " +max_temp + "C ");

        }


    }
}



