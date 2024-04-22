package com.jainsoham.lab11;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    List<String> savedQuotes;
    Button saveButton;
    Button viewSavedButton;
    Button refreshButton;
    Button backButton;
    TextView textView;
    TextView tagsText;
    TextView savedText;
    List<String> tags;
    String quote;
    String joinedTags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tags = new ArrayList<>();
        textView = (TextView) findViewById(R.id.text);
        tagsText = (TextView) findViewById(R.id.tagsText);
        saveButton = findViewById(R.id.saveButton);
        viewSavedButton = findViewById(R.id.viewSavedButton);
        refreshButton = findViewById(R.id.refreshButton);
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
//        sharedPreferences.edit().clear().commit();
        savedQuotes = getSavedQuotesFromPreferences();
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://api.quotable.io/random";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            JSONArray jsonArray = object.getJSONArray("tags");
                            for (int i = 0;  i < jsonArray.length(); i++) {
                                String tag = jsonArray.getString(i);
                                tags.add("#" + tag.replaceAll(" ", ""));
                            }
                            quote = object.getString("content");
                            textView.setText("\"" + quote + "\"");
                            joinedTags = String.join(" ", tags.toArray(new String[0]));
                            tagsText.setText(joinedTags);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                textView.setText("Error");
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savedQuotes.add("\"" + quote + "\"");
                showSnackbar("Quote saved!");
                saveQuotesToPreferences();
            }
        });

        viewSavedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.saved);
                backButton = findViewById(R.id.backButton);
                savedText = (TextView) findViewById(R.id.savedText);
                savedText.setText(String.join("\n\n", savedQuotes.toArray(new String[0])));
                if (backButton != null) {
                    backButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = getIntent();
                            finish();
                            startActivity(intent);
                        }
                    });
                }
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });

        queue.add(stringRequest);
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    private List<String> getSavedQuotesFromPreferences() {
        List<String> savedQuotes = new ArrayList<>();
        String savedQuotesJson = sharedPreferences.getString("savedQuotes", null);
        if (savedQuotesJson != null) {
            try {
                JSONArray jsonArray = new JSONArray(savedQuotesJson);
                for (int i = 0; i < jsonArray.length(); i++) {
                    savedQuotes.add(jsonArray.getString(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return savedQuotes;
    }

    private void saveQuotesToPreferences() {
        JSONArray jsonArray = new JSONArray(savedQuotes);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("savedQuotes", jsonArray.toString());
        editor.apply();
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveQuotesToPreferences();
    }
}