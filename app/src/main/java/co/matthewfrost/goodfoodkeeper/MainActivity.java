package co.matthewfrost.goodfoodkeeper;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.internal.Excluder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AbsListView.OnScrollListener {

    EditText searchBox;
    ListView results;
    Button search;
    String baseUrl, baseCountUrl, searchUrl, searchTerm;
    RequestQueue mQueue;
    Cache cache;
    Network network;
    ArrayList<Recipie> recipies;
    Recipie recipie;
    Gson gson;
    ArrayAdapter adapter;
    int currentPage, recipeCount;
    int preLast;
    JsonArrayRequest jsonArrayRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchBox = (EditText) findViewById(R.id.searchTerm);
        search = (Button) findViewById(R.id.search);
        results = (ListView) findViewById(R.id.searchResults);
        baseUrl = "https://good-food-api.herokuapp.com/search?term=";
        baseCountUrl = "https://good-food-api.herokuapp.com/count?term=";

        cache = new DiskBasedCache(getCacheDir(), 2048 * 2048);
        network = new BasicNetwork(new HurlStack());
        mQueue = new RequestQueue(cache, network);
        gson = new Gson();
        mQueue.start();
        recipies = new ArrayList<>();
        adapter = new ArrayAdapter<Recipie>(this, R.layout.activity_listview, recipies);
        results.setAdapter(adapter);
        results.setOnScrollListener(this);

        results.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Recipie selected = recipies.get(position);
                Intent i = new Intent(getApplicationContext(), Details.class);
                String iUrl = selected.getUrl().replace("/recipes/", "");
                i.putExtra("url", iUrl);
                startActivity(i);
                Log.v("url", selected.getUrl());
                Log.v("name", selected.toString());
            }
        });
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String countUrl;

                JsonObjectRequest objRequest;
                currentPage = 0;
                searchTerm = searchBox.getText().toString().replace(' ', '+');
                countUrl = baseCountUrl + searchTerm;

                objRequest = new JsonObjectRequest(Request.Method.GET, countUrl, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                           // Log.v("count", response.getString("count"));
                            recipeCount = (int) response.getInt("count");
                        }
                        catch (Exception e){
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

                searchUrl = baseUrl + searchTerm + "#query=" + searchTerm + "&page=" + currentPage;
                jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, searchUrl, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (recipies.size() > 0) {
                            recipies.clear();
                        }

                        for (int i = 0; i < response.length(); i++) {
                            try {
                                recipies.add(gson.fromJson(response.get(i).toString(), Recipie.class));
                            } catch (Exception e) {
                                Log.e("error", e.getMessage());
                            }

                        }
                        Log.v("done", "done");
                        adapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.v("error", error.getMessage());
                    }
                });

                mQueue.add(jsonArrayRequest);
                currentPage++;

            }


        });

    }

    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar, menu);
        return  true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_favorite:
                Intent i = new Intent(getApplicationContext(), Favourite.class);
                startActivity(i);
                break;
            default:
                break;
        }

        return true;
    }

    @Override
    public void onScroll(AbsListView lw, final int firstVisibleItem,
                         final int visibleItemCount, final int totalItemCount)
    {

        switch(lw.getId())
        {
            case R.id.searchResults:

                // Make your calculation stuff here. You have all your
                // needed info from the parameters of this function.

                // Sample calculation to determine if the last
                // item is fully visible.
                final int lastItem = firstVisibleItem + visibleItemCount;

                if(lastItem == totalItemCount)
                {
                    if(preLast!=lastItem)
                    {
                        //to avoid multiple calls for last item
                        Log.d("Last", "Last");
                        preLast = lastItem;
                        if(recipies.size() < recipeCount){
                            searchUrl = baseUrl + searchTerm + "&page=" + currentPage;
                            jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, searchUrl, null, new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {

                                    for (int i = 0; i < response.length(); i++) {
                                        try {
                                            recipies.add(gson.fromJson(response.get(i).toString(), Recipie.class));
                                        } catch (Exception e) {
                                            Log.e("error", e.getMessage());
                                        }

                                    }
                                    Log.v("done", "done");
                                    adapter.notifyDataSetChanged();
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.v("error", error.getMessage());
                                }
                            });

                            mQueue.add(jsonArrayRequest);
                            currentPage++;
                        }
                    }
                }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }
}
