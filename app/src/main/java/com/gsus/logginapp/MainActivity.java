package com.gsus.logginapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.content.ContentValues;
import android.util.Log;
import android.widget.Toast;

import com.gsus.logginapp.model.Token;
import com.gsus.logginapp.service.ErpClient;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity {

    public String CLIENT_ID = "11";
    public String SECRET = "u12k5tax8zOQR53eRZdglLG2gpg5EuYsQqxLcOud";
    public String SECRET_DEV = "VmSpjp0T2WCwZUEWsROs5pd0ZA8K3Yx0qgNM8i8G";
    public String REDIRECT_URI = "/auth://callback";
    public String MOVIL = "http://192.168.100.110:8000/api/movil?client_id=11&response_type=code&redirect_uri=/auth&usuario=jlopeza&clave=123456";
    private GetCode code = null;
    private JSONObject resp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        code = new GetCode();
        code.execute();


    }

    private class GetCode extends AsyncTask<Void, Void, Boolean> {
        protected Boolean doInBackground(Void... urls) {
            String body = " ";

            try {
                URL url = new URL(MOVIL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                String codigoRespuesta = Integer.toString(urlConnection.getResponseCode());
                if(codigoRespuesta.equals("200")){//Vemos si es 200 OK y leemos el cuerpo del mensaje.
                    body = readStream(urlConnection.getInputStream());
                    resp = new JSONObject(body);
                    String codec = resp.get("code").toString();

                    Retrofit.Builder builder = new Retrofit.Builder()
                            .baseUrl("http://192.168.100.110:8000/")
                            .addConverterFactory(GsonConverterFactory.create());
                    Retrofit retrofit = builder.build();

                    ErpClient client = retrofit.create(ErpClient.class);
                    Call<Token> getAccessToken =  client.getToken(
                            CLIENT_ID,
                            SECRET_DEV,
                            codec,
                            "authorization_code",
                            "/auth"
                    );
                    getAccessToken.enqueue(new Callback<Token>() {
                        @Override
                        public void onResponse(Call<Token> call, Response<Token> response) {
                            Toast.makeText(MainActivity.this, "Yes" + response.body().getAccessToken(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<Token> call, Throwable t) {
                            Toast.makeText(MainActivity.this, "Nop", Toast.LENGTH_SHORT).show();
                        }
                    });

                }else{
                    body = "{\"status_code\":\""+codigoRespuesta+"\"}";
                }
                urlConnection.disconnect();
            } catch (MalformedURLException e) {
                body = e.toString(); //Error URL incorrecta
            } catch (SocketTimeoutException e){
                body = e.toString(); //Error: Finalizado el timeout esperando la respuesta del servidor.
            } catch (Exception e) {
                body = e.toString();//Error diferente a los anteriores.
            }

            return false;
        }


        protected void onPostExecute(Long result) {
            Toast.makeText(MainActivity.this, "ok!!", Toast.LENGTH_SHORT).show();
        }
    }
    private static String readStream(InputStream in) throws IOException{

        BufferedReader r = null;
        r = new BufferedReader(new InputStreamReader(in));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line);
        }
        if(r != null){
            r.close();
        }
        in.close();
        return total.toString();
    }
}

