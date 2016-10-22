package co.matthewfrost.goodfoodkeeper;

import android.content.Intent;
import android.os.Environment;
import android.renderscript.ScriptGroup;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Exchanger;

public class Details extends AppCompatActivity {
    ListView ingredients, method;
    ArrayList<String> ingredientList, methodList;
    JsonObjectRequest objRequest;
    RequestQueue mQueue;
    Cache cache;
    Network network;
    String recipeUrl;
    ArrayAdapter methoodAdapter, ingredientAdapter;
    CheckBox checkBox;
    JSONObject recipe;
    String fileName;
    Gson gson;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ingredients = (ListView) findViewById(R.id.ingredientView);
        method = (ListView) findViewById(R.id.methodView);
        checkBox = (CheckBox) findViewById(R.id.checkBox);
        gson = new Gson();

        ingredientList = new ArrayList<>();
        methodList = new ArrayList<>();
        methoodAdapter = new ArrayAdapter<String>(this, R.layout.activity_listview, methodList);
        method.setAdapter(methoodAdapter);
        ingredientAdapter = new ArrayAdapter<String>(this, R.layout.activity_listview, ingredientList);
        ingredients.setAdapter(ingredientAdapter);
        cache = new DiskBasedCache(getCacheDir(), 2048 * 2048);
        network = new BasicNetwork(new HurlStack());
        mQueue = new RequestQueue(cache, network);

        mQueue.start();

        Intent i = getIntent();
        String url = i.getStringExtra("url");
        if(url != null) {
            recipeUrl = "http://good-food-api.herokuapp.com/scrape?id=" + url;
            objRequest = new JsonObjectRequest(Request.Method.GET, recipeUrl, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        recipe = response;
                        fileName = recipe.getString("title");
                        setUpLists(response);
                        Log.v("done", "done");
                    } catch (Exception e) {
                        Log.e("error", e.getMessage());
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.v("error", error.getMessage());
                }
            });
            mQueue.add(objRequest);
        }
        else{
            String path = i.getStringExtra("filePath");
            String json = "";
            try{
                InputStream is = new FileInputStream(path);
                int size = is.available();

                byte[] buffer = new byte[size];

                is.read(buffer);

                is.close();
                json = new String(buffer, "UTF-8");

            }
            catch(Exception e){

            }

            JsonParser parser = new JsonParser();
            JsonObject obj = parser.parse(json).getAsJsonObject();
            //setUpLists(gson.toJSONObject(obj));
            try {
                JSONObject data = new JSONObject(obj.toString());
                setUpLists(data);
            }
            catch(Exception e){

            }

        }

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    try {

                        FileWriter file = new FileWriter("/data/data/" + getApplicationContext().getPackageName() + "/" + fileName + ".json");
                        file.write(recipe.toString());
                        file.flush();
                        file.close();
                    }
                    catch (Exception e){

                    }
                }
                else{
                    File f = new File("/data/data/" + getApplicationContext().getPackageName() + "/" + fileName + ".json");
                    if(f.exists()){
                        f.delete();
                    }
                }
            }
        });
    }

    public void setUpLists(JSONObject data){

        try {
            for (int i = 0; i < data.getJSONArray("method").length(); i++) {
                methodList.add(data.getJSONArray("method").get(i).toString());
            }
            methoodAdapter.notifyDataSetChanged();

            for (int i = 0; i < data.getJSONArray("ingredients").length(); i++) {
                ingredientList.add(data.getJSONArray("ingredients").get(i).toString());
            }
            ingredientAdapter.notifyDataSetChanged();
        }
        catch (Exception e){

        }
    }
}
