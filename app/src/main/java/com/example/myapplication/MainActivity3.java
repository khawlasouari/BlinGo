package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class MainActivity3 extends AppCompatActivity {

    private EditText userInput;
    private TextView chatOutput;
    private Button sendButton;
    private OkHttpClient client;

    // 🔒 Clé API (Remplacez par votre clé API OpenAI)
    private final String API_KEY = "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";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        userInput = findViewById(R.id.userInput);
        chatOutput = findViewById(R.id.chatOutput);
        sendButton = findViewById(R.id.sendButton);
        client = new OkHttpClient();

        sendButton.setOnClickListener(v -> {
            sendButton.setEnabled(false);  // Désactive le bouton temporairement
            sendMessage(userInput.getText().toString());

            // Réactiver le bouton après 5 secondes (5000 ms)
            new android.os.Handler().postDelayed(() -> sendButton.setEnabled(true), 5000);
        });

    }

    private void sendMessage(String message) {
        String url = "https://api.openai.com/v1/chat/completions";

        try {
            // 🔹 Création de la requête JSON
            JSONObject json = new JSONObject();
            json.put("model", "gpt-3.5-turbo");

            JSONArray messages = new JSONArray();
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", message);
            messages.put(userMessage);

            json.put("messages", messages);

            // 🔹 Création du corps de la requête
            RequestBody body = RequestBody.create(MediaType.parse("application/json"), json.toString());

            // 🔹 Création de la requête HTTP
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Authorization", "Bearer " + API_KEY)  // Sécurisation de la clé API
                    .addHeader("Content-Type", "application/json")
                    .build();

            // 🔹 Envoi de la requête
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("API Error", "Échec de la requête : " + e.getMessage());
                    runOnUiThread(() -> chatOutput.setText("Échec de la requête : " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        Log.e("API Error", "Erreur HTTP : " + response.code());
                        runOnUiThread(() -> chatOutput.setText("Erreur HTTP : " + response.code()));
                        return;
                    }

                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        String responseData = responseBody.string();
                        Log.d("API Response", responseData); // Debugging

                        try {
                            JSONObject jsonResponse = new JSONObject(responseData);
                            JSONArray choices = jsonResponse.getJSONArray("choices");
                            if (choices.length() > 0) {
                                String reply = choices.getJSONObject(0)
                                        .getJSONObject("message")
                                        .getString("content");

                                runOnUiThread(() -> chatOutput.setText(reply.trim()));
                            } else {
                                runOnUiThread(() -> chatOutput.setText("Aucune réponse reçue."));
                            }
                        } catch (JSONException e) {
                            Log.e("JSON Error", "Erreur de parsing JSON : " + e.getMessage());
                            runOnUiThread(() -> chatOutput.setText("Erreur JSON : " + e.getMessage()));
                        }
                    }
                }
            });

        } catch (JSONException e) {
            Log.e("JSON Error", "Erreur lors de la construction du JSON : " + e.getMessage());
            runOnUiThread(() -> chatOutput.setText("Erreur JSON : " + e.getMessage()));
        }
    }
}
