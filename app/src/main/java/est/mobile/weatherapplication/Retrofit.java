package est.mobile.weatherapplication;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class Retrofit extends AppCompatActivity {
    interface RequestData {
        @GET("v1/forecast")
        Call<Response> getWeatherData(
                @Query("latitude") double latitude,
                @Query("longitude") double longitude,
                @Query("daily") String daily,
                @Query("current_weather") boolean currentWeather,
                @Query("timezone") String timezone
        );
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.retrofit);
        TextView txtcoordinat = findViewById(R.id.coordinat);
        TextView txtDate = findViewById(R.id.date);
        TextView txtTemperature = findViewById(R.id.temperature);
        TextView txtCondition = findViewById(R.id.condition);
        TextView txtWind = findViewById(R.id.wind);
        ImageView imgCondition = findViewById(R.id.imgCondition);

        retrofit2.Retrofit retrofit = new retrofit2.Retrofit.Builder()
                .baseUrl("https://api.open-meteo.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RequestData requestUser = retrofit.create(RequestData.class);

        requestUser.getWeatherData(-7.98, 112.63, "weathercode", true, "auto").enqueue(new Callback<Response>() {
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                Response weatherData = response.body();
                txtcoordinat.setText(String.valueOf(weatherData.getLatitude() + ", " + weatherData.getLongitude()));

                Response.CurrentWeather currentWeather = weatherData.getCurrentWeather();

                String date = currentWeather.getTime();
                txtDate.setText(String.valueOf(date));
                String[] dateSplit = date.split("-", 3);
                String day = dateSplit[2].substring(0,2);
                String month = getMonthName(Integer.parseInt(dateSplit[1]));
                String year = dateSplit[0];
                txtDate.setText(day + " " + month + " " + year);

                Float temperature = currentWeather.getTemperature();
                txtTemperature.setText(String.valueOf(temperature + "°"));

                int is_day = currentWeather.getIs_day();
                String condition = Integer.toString(is_day);
                txtCondition.setText(DetailCondition(condition));

                imgCondition.setImageDrawable(setImage(condition));

                float wind = currentWeather.getWindspeed();
                txtWind.setText(String.valueOf(wind + "m/s"));

                Response.DailyData dailyData = weatherData.getDailyData();
                String[] timeArray = dailyData.getTime();

                int[] weatherCodes = dailyData.getWeatherCode();
                ShowDataDaily(timeArray, weatherCodes);
            }

            @Override
            public void onFailure(Call<Response> call, Throwable t) {
                Toast.makeText(Retrofit.this, "API call failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void ShowDataDaily(String[] timeArray, int[] weatherCodes) {
        String[] items_daily =  new String[7];
        for(int i=0; i < items_daily.length ; i++) {
            String iterate = timeArray[i];
            items_daily[i] = iterate;
        }
        String[] items_weathercode =  new String[7];
        for(int i=0; i < items_weathercode.length ; i++) {
            int iterate = weatherCodes[i];
            String condition = Integer.toString(iterate);
            items_weathercode[i] = DetailCondition(condition);
        }

        Drawable[] items_images = new Drawable[7];
        for(int i=0; i < items_images.length ; i++) {
            String iterate = String.valueOf(weatherCodes[i]);
            items_images[i] = setImage(iterate);
        }

        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.list_view, items_daily);
        ListView listView1 = (ListView) findViewById(R.id.daily);
        listView1.setAdapter(adapter);

        ArrayAdapter adapter2 = new ArrayAdapter<String>(this, R.layout.list_view, items_weathercode);
        ListView listView2 = (ListView) findViewById(R.id.dailyweather);
        listView2.setAdapter(adapter2);

        ListView listView3 = (ListView) findViewById(R.id.imgWeather);
        ArrayAdapter<Drawable> adapter3 = new ArrayAdapter<Drawable>(this, R.layout.image_list, items_images) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.image_list, parent, false);
                }
                ImageView imageView = convertView.findViewById(R.id.imgWeather);
                imageView.setImageDrawable(getItem(position));
                return convertView;
            }
        };
        listView3.setAdapter(adapter3);
    }

    private Drawable setImage(String cond) {
        Drawable drawable;
        switch (cond){
            case "0":
                drawable = getResources().getDrawable(R.drawable.ic_sunny);
                break;
            case "1":
                drawable =  getResources().getDrawable(R.drawable.ic_daycloudy);
                break;
            case "2":
                drawable =  getResources().getDrawable(R.drawable.ic_cloudynight);
                break;
            case "3":
                drawable =  getResources().getDrawable(R.drawable.ic_cloudy);
                break;
            case "45":
            case "48":
                drawable =  getResources().getDrawable(R.drawable.ic_fog);
                break;
            case "51":
            case "53":
            case "55":
                drawable =  getResources().getDrawable(R.drawable.ic_sleet);
                break;
            case "61":
            case "63":
                drawable =  getResources().getDrawable(R.drawable.ic_hail);
                break;
            case "65":
                drawable =  getResources().getDrawable(R.drawable.ic_rainy);
                break;
            default:
                drawable = getResources().getDrawable(R.drawable.ic_storm);
                break;
        }
        return drawable;
    }
    private String DetailCondition(String cond) {
        switch (cond){
            case "0":
                cond = "Clear Sky";
                break;
            case "1":
                cond = "Mainly Clear";
                break;
            case "2":
                cond = "Partly Cloudy";
                break;
            case "3":
                cond = "Overcast";
                break;
            case "45":
            case "48":
                cond = "Fog";
                break;
            case "51":
            case "53":
            case "55":
                cond = "Drizzle";
                break;
            case "61":
            case "63":
                cond = "Rain Slight";
                break;
            case "65":
                cond = "Rain Heavy";
                break;
            default:
                cond = "Thunderstorm";
                break;
        }
        return cond;
    }
    public static String getMonthName(int number) {
        String[] monthNames = {
                "January", "February", "March", "April",
                "May", "June", "July", "August",
                "September", "October", "November", "December"
        };
        return monthNames[number - 1];
    }

}