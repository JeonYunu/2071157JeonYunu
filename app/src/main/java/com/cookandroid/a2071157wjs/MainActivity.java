package com.cookandroid.a2071157wjs;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText amountEditText;
    private Spinner fromCurrencySpinner, toCurrencySpinner;
    private Button convertButton;
    private TextView resultTextView;
    private RequestQueue requestQueue;
    private Map<String, String> currencyMap;

    private final String API_KEY = "526753f813cb825711131b72"; // 여기에 실제 API 키를 입력합니다.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        amountEditText = findViewById(R.id.amountEditText);
        fromCurrencySpinner = findViewById(R.id.fromCurrencySpinner);
        toCurrencySpinner = findViewById(R.id.toCurrencySpinner);
        convertButton = findViewById(R.id.convertButton);
        resultTextView = findViewById(R.id.resultTextView);

        requestQueue = Volley.newRequestQueue(this);
        initializeCurrencyMap();
        loadCurrencyData();

        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                convertCurrency();
            }
        });
    }

    private void initializeCurrencyMap() {
        currencyMap = new HashMap<>();
        currencyMap.put("USD", "미국 달러");
        currencyMap.put("KRW", "원");
        currencyMap.put("EUR", "유로");
        currencyMap.put("JPY", "일본 엔");
        Log.d("CurrencyMap", "Initialized currency map: " + currencyMap.toString());
    }

    private void loadCurrencyData() {
        String url = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/latest/USD";
        Log.d("APIRequest", "Request URL: " + url); // API 요청 URL 로그 추가

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("APIResponse", "Response: " + response.toString()); // API 응답 로그 추가
                        try {
                            JSONObject conversionRates = response.getJSONObject("conversion_rates");
                            List<String> allowedCurrencies = new ArrayList<>();
                            allowedCurrencies.add("USD");
                            allowedCurrencies.add("EUR");
                            allowedCurrencies.add("JPY");
                            allowedCurrencies.add("KRW");

                            List<String> currencyCodes = new ArrayList<>();
                            for (String code : allowedCurrencies) {
                                if (conversionRates.has(code)) {
                                    currencyCodes.add(code);
                                }
                            }
                            Log.d("Currencies", currencyCodes.toString()); // 데이터 확인용 로그 출력

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, currencyCodes);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            fromCurrencySpinner.setAdapter(adapter);
                            toCurrencySpinner.setAdapter(adapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("JSONError", "Error parsing JSON data.");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.e("VolleyError", "Error fetching data from API.");
            }
        });

        requestQueue.add(jsonObjectRequest);
    }

    private void convertCurrency() {
        String amountStr = amountEditText.getText().toString();
        if (amountStr.isEmpty()) {
            resultTextView.setText("금액을 입력하세요.");
            return;
        }

        double amount = Double.parseDouble(amountStr);
        String fromCurrency = fromCurrencySpinner.getSelectedItem().toString();
        String toCurrency = toCurrencySpinner.getSelectedItem().toString();
        Log.d("CurrencySelection", "From: " + fromCurrency + ", To: " + toCurrency); // 선택된 통화 로그 추가

        if (fromCurrency == null || toCurrency == null) {
            resultTextView.setText("유효하지 않은 통화입니다.");
            Log.e("CurrencyError", "Invalid currency detected. From: " + fromCurrency + ", To: " + toCurrency);
            return;
        }

        String url = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/pair/" + fromCurrency + "/" + toCurrency;
        Log.d("APIRequest", "Request URL: " + url); // API 요청 URL 로그 추가

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("APIResponse", "Response: " + response.toString()); // API 응답 로그 추가
                        try {
                            double conversionRate = response.getDouble("conversion_rate");
                            Log.d("ConversionRate", "Conversion Rate: " + conversionRate); // 변환율 로그 추가
                            double convertedAmount = amount * conversionRate;
                            resultTextView.setText(String.format("%.2f %s = %.2f %s", amount, fromCurrency, convertedAmount, toCurrency));
                            Log.d("ConvertedAmount", "Converted Amount: " + convertedAmount); // 변환된 금액 로그 추가
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("JSONError", "Error parsing JSON data.");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.e("VolleyError", "Error fetching data from API.");
            }
        });

        requestQueue.add(jsonObjectRequest);
        Log.d("APIRequest", "Request added to queue"); // API 요청 큐에 추가 로그
    }
}