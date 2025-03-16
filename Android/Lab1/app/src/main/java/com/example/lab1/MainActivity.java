package com.example.lab1;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lab1.databinding.ActivityMainBinding;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements TransactionEvents {

    // Used to load the 'lab1' library on application startup.
    static {
        System.loadLibrary("lab1");
        System.loadLibrary("mbedcrypto");
    }

    private ActivityMainBinding binding;
    private String pin;
    ActivityResultLauncher activityResultLauncher;

    public static byte[] stringToHex(String s) {
        byte[] hex;
        try {
            hex = Hex.decodeHex(s.toCharArray());
        } catch (DecoderException ex) {
            hex = null;
        }
        return hex;
    }

    public void onButtonClick(View v) {
        // Intent it = new Intent(this, PinpadActivity.class);
        //   activityResultLauncher.launch(it);
        byte[] trd = stringToHex("9F0206000000000100");
        transaction(trd);
      //  testHttpClient();
    }

    @Override
    public String enterPin(int ptc, String amount) {
        pin = new String();
        Intent it = new Intent(MainActivity.this, PinpadActivity.class);
        it.putExtra("ptc", ptc);
        it.putExtra("amount", amount);
        synchronized (MainActivity.this) {
            activityResultLauncher.launch(it);
            try {
                MainActivity.this.wait();
            } catch (Exception ex) {
                //todo: log error
            }
        }
        return pin;
    }

    @Override
    public void transactionResult(boolean result) {
        runOnUiThread(() -> {
            Toast.makeText(MainActivity.this, result ? "ok" : "failed", Toast.LENGTH_SHORT).show();
        });
    }

    protected void testHttpClient() {
        new Thread(() -> {
            try {
                HttpURLConnection uc = (HttpURLConnection)
                        (new URL("http://10.0.2.2:8081/api/v1/title").openConnection());
                InputStream inputStream = uc.getInputStream();
                String html = IOUtils.toString(inputStream);
                String title = getPageTitle(html);
                runOnUiThread(() ->
                {
                    Toast.makeText(this, title, Toast.LENGTH_LONG).show();
                });

            } catch (Exception ex) {
                Log.e("fapptag", "Http client fails", ex);
            }
        }).start();
    }

    protected String getPageTitle(String html) {
        Pattern pattern = Pattern.compile("<title>(.+?)</title>", Pattern.DOTALL); //(.+?)  группа, которая соответствует любому символу (.) одному или более раз (+), но как можно меньшему количеству символов (?)
        Matcher matcher = pattern.matcher(html);
        String p;
        if (matcher.find())
            p = matcher.group(1);
        else
            p = "Not found";
        return p;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int res = initRng();
        Log.d("MY_DEBUG", "res" + Integer.toString(res));
        byte[] v = randomBytes(10);
        Log.d("MY_DEBUG", "v " + Arrays.toString(v));
//        TextView tv = findViewById(R.id.sample_text);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        testHttpClient();

        String keyString = "1516411215413156";
        byte[] key = keyString.getBytes(StandardCharsets.US_ASCII);
        String dataString = "DataString";
        byte[] data = dataString.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedData = encrypt(key, data);
        byte[] decryptedData = decrypt(key, encryptedData);

//        TextView tv = binding.sampleText;
//        String result = stringFromJNI() + "\n"
//                + "Исходные данные: " + Arrays.toString(data) + "\n"
//                + "Закодированные данные: " + Arrays.toString(encryptedData) + "\n"
//                + "Декодированные данные: " + Arrays.toString(decryptedData);
//        tv.setText(result);

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback() {
                    @Override
                    public void onActivityResult(Object o) {
                        ActivityResult result = ((ActivityResult) o);
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            // обработка результата
                            //  String pin = data.getStringExtra("pin");
                            // Toast.makeText(MainActivity.this, pin, Toast.LENGTH_SHORT).show();
                            pin = data.getStringExtra("pin");
                            synchronized (MainActivity.this) {
                                MainActivity.this.notifyAll();
                            }
                        }
                    }
                });

//        new Thread(()-> {
//            try {
//                byte[] trd = stringToHex("9F0206000000000100");
//                boolean ok = transaction(trd);
//                runOnUiThread(()-> {
//                    Toast.makeText(MainActivity.this, ok ? "ok" : "failed", Toast.LENGTH_SHORT).show();
//                });
//
//            } catch (Exception ex) {
//                // todo: log error
//            }
//        }).start();
    }


    /**
     * A native method that is implemented by the 'lab1' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    public static native int initRng();

    public static native byte[] randomBytes(int no);

    public static native byte[] encrypt(byte[] key, byte[] data);

    public static native byte[] decrypt(byte[] key, byte[] data);

    public native boolean transaction(byte[] trd);
}
