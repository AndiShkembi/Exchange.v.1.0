package com.example.andi.exchange;

import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.example.andi.exchange.R;
import com.example.andi.exchange.ServiceHandler;

public class MainActivity extends ListActivity {




    private ProgressDialog pDialog;

    // URL Nga te cilat na vine te dhenat ne JSON.
    private static String url = "http://societe.divi-tech.net/api.php";


    // Emrat e objekteve dhe vektoreve qe ndodhen ne filen JSON

    private static final String TAG_EXCHANGE     = "exchange";
    private static final String TAG_VALUES       = "values";
    private static final String TAG_ID           = "id";
    private static final String TAG_CURRENCY     = "currency";
    private static final String TAG_BUY          = "buy";
    private static final String TAG_SELL         = "sell";
    private static final String TAG_DATA         = "date";
    private static final String TAG_MESSAGE      = "message";
    private static final String TAG_COLOR        = "color";
    private static final String TAG_BACKGROUND   = "background";
    private static final String TAG_TEXTCOLOR    = "textColor";





    // EXHCHANGE JSONFile

    JSONObject exchange         = null;
    JSONArray  values           = null;
    JSONObject color            = null;
    String     lastChangeData   = null;
    String     tekstimarquee    = null;
    String     backgroundColor  = null;
    String     textColor        = null;

    // Hashmap for ListView
    ArrayList<HashMap<String, String>> valueList;



   // Mundeson qe aplikacioni te shfaqet ne fullScreen.
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        View decorView = getWindow().getDecorView();
          if (hasFocus) {
               decorView.setSystemUiVisibility(
                              View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
          }
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Animimi i tekstit(marquee).
        TextView tekstii = (TextView) findViewById(R.id.marquee);
        tekstii.setText(tekstimarquee);
        Animation marquee = AnimationUtils.loadAnimation(this, R.anim.marquee);
        tekstii.startAnimation(marquee);


        valueList = new ArrayList<HashMap<String, String>>();
        // Calling async task to get json
        new GetContacts().execute();













    }



    /**
     * Async task class to get json by making HTTP call
     * */
    private class GetContacts extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);

            Log.d("Response: ", "> " + jsonStr);


            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    exchange = jsonObj.getJSONObject(TAG_EXCHANGE);
                    values = exchange.getJSONArray(TAG_VALUES);
                    color = exchange.getJSONObject(TAG_COLOR);


                    //Marrja e teksit marquee nga file JSON
                    tekstimarquee = jsonObj.getString(TAG_MESSAGE);


                     //Marrja e colorit te tekstit
                    textColor = color.getString(TAG_TEXTCOLOR);





                    //Marrja e dates nga file JSON
                    lastChangeData = exchange.getString(TAG_DATA);
                    final TextView datasotme = (TextView)findViewById(R.id.data);
                    final TextView teksti = (TextView)findViewById(R.id.marquee);


                    //final TextView colorMonedha = (TextView)findViewById(R.id.name);
                    //final TextView colorBuy = (TextView)findViewById(R.id.buy);
                    //final TextView colorSell = (TextView)findViewById(R.id.sell);

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            datasotme.setText(lastChangeData);
                            teksti.setText(tekstimarquee);
                            //colorMonedha.setTextColor(Color.RED);
                            //colorBuy.setTextColor(Color.RED);
                            //colorSell.setTextColor(Color.RED);
                        }
                    });









                    // Ben kerkimin e te dhenave ne te gjithe vektorin Value.
                    for (int i = 0; i < 4; i++) {
                        JSONObject c = values.getJSONObject(i);

                        String id = c.getString(TAG_ID);
                        String name = c.getString(TAG_CURRENCY);
                        String buy= c.getString(TAG_BUY);
                        String sell = c.getString(TAG_SELL);


                        // tmp hashmap for single value
                        HashMap<String, String> informacioni = new HashMap<String, String>();

                        // Vendosim te dhenat.
                        informacioni.put(TAG_ID, id);
                        informacioni.put(TAG_CURRENCY, name);
                        informacioni.put(TAG_BUY, buy);
                        informacioni.put(TAG_SELL, sell);

                        // Shton informacionin ne valueList
                        valueList.add(informacioni);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Nuk u gjet ndonje data ne url e dhene !");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Ben Update listView me te dhenat e paresuar nga file JSON.
             * */



            ListAdapter adapter = new SimpleAdapter(
                    MainActivity.this, valueList,
                    R.layout.list_item, new String[] { TAG_CURRENCY, TAG_BUY,
                    TAG_SELL}, new int[] { R.id.name,
                    R.id.buy, R.id.sell});

            setListAdapter(adapter);
        }

    }

}