package com.devdroid.upipayment;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;



public class MainActivity extends AppCompatActivity {
LottieAnimationView lottieCheck;
    CardView cardBtnMakePayment;
    final int UPI_PAYMENT = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lottieCheck = findViewById(R.id.lottieCheck);
        cardBtnMakePayment = findViewById(R.id.cardBtnMakePayment);


        cardBtnMakePayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();
                startActivity(new Intent(MainActivity.this, MainActivity.class));
            }
        });


        openScan();

        /*btnPay = findViewById(R.id.btnPay);

        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                *//*String upiId,amount,name,desc;
                upiId = "raunak.purohit92@oksbi";
                amount = "1";
                name = "Raunak";
                desc = "Hello";
                UpiPayment(upiId,amount,name,desc);*//*

            }
        });*/
    }

    private void openScan() {

        IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setOrientationLocked(false);
        integrator.setPrompt("");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();


    }

    private void UpiPayment(String upiId, String amount, String name, String desc) {

        Uri uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa",upiId)
                .appendQueryParameter("pn",name)
                .appendQueryParameter("mc","")
                .appendQueryParameter("tr","25584584")
                .appendQueryParameter("tn",desc)
                .appendQueryParameter("am",amount)
                .appendQueryParameter("cu","INR")
                .build();

        Intent upiPayIntent = new Intent(Intent.ACTION_VIEW);
        upiPayIntent.setData(uri);

        Intent chooserIntent = Intent.createChooser(upiPayIntent,"Pay with");

        if(null != chooserIntent.resolveActivity(getPackageManager())){
            startActivityForResult(chooserIntent,UPI_PAYMENT);
        }
        else {
            Toast.makeText(this, "No UPI app found", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Log.e("Scan*******", "Cancelled scan");

            } else {
                Log.d("Scan", result.getContents());
                String str[] = result.getContents().split("&");
                String upi = str[0].split("=")[1];
                String name = str[1].split("=")[1];
                Log.d("UPI", upi);
                Log.d("Name", name);
                //Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                UpiPayment(upi,"",name,"");

            }
        } else {
            Log.d("Check", "Else Part");
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }

        Log.d("Check", "Back");

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UPI_PAYMENT) {
            if ((RESULT_OK == resultCode) || (resultCode == 11)) {
                if (data != null) {
                    String response = data.getStringExtra("response");
                    Log.d("UPI: ", "onActivityResult " + response);
                    ArrayList<String> dataList = new ArrayList<>();
                    dataList.add(response);
                    paymentOperation(dataList);
                } else {
                    Log.d("UPI: ", "onActivityResult " + "Return data is null");
                    ArrayList<String> dataList = new ArrayList<>();
                    dataList.add("nothing");
                    paymentOperation(dataList);
                }
            } else {
                Log.d("UPI: ", "onActivityResult " + "Return data is null");
                ArrayList<String> dataList = new ArrayList<>();
                dataList.add("nothing");
                paymentOperation(dataList);
            }
        }
    }

    private void paymentOperation(ArrayList<String> dataList) {


        if(isConnectionAvailable(MainActivity.this)){

            String str = dataList.get(0);
            Log.d("UPI: ","paymentOperation: " + str);
            String paymentCancel = "";
            if(str == null){
                str = "discard";
            }
            String status = "";
            String approvalRefNo = "";
            String response[] = str.split("&");

            for(int i = 0;i<response.length;i++){
                String equalString[] = response[i].split("=");
                if(equalString.length >= 2){
                    if(equalString[0].toLowerCase().equals("Status".toLowerCase())){
                        status = equalString[1].toLowerCase();
                    }
                    else if(equalString[0].toLowerCase().equals("ApprovalRefNo".toLowerCase()) || equalString[0].toLowerCase().equals("txnRef".toLowerCase())){
                        approvalRefNo = equalString[1];
                    }
                }
                else{
                    paymentCancel = "Payment cancelled by user";
                }
            }



            if(status.equals("success")){
                Toast.makeText(this, "Transaction Successfull", Toast.LENGTH_SHORT).show();
                Log.d("UPI: ","paymentOperation " + approvalRefNo);
                loadAnimation(R.raw.success_pay);
            }
            else if("Payment cancelled by user".equals(paymentCancel)){
                Toast.makeText(this, "Payment cancelled by user", Toast.LENGTH_SHORT).show();
                loadAnimation(R.raw.failed);
            }
            else{
                Toast.makeText(this, "Transaction failed.Please try again", Toast.LENGTH_SHORT).show();
                loadAnimation(R.raw.failed);
            }
        }
        else{
            Toast.makeText(this, "Internet Connection not available", Toast.LENGTH_SHORT).show();
            loadAnimation(R.raw.failed);

        }



    }

    private void loadAnimation(int lottiePath) {
        lottieCheck.setAnimation(lottiePath);
        lottieCheck.playAnimation();
    }

    private boolean isConnectionAvailable(Context context) {

        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager != null){
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if(networkInfo!=null &&
            networkInfo.isConnected() &&
            networkInfo.isConnectedOrConnecting() &&
            networkInfo.isAvailable()){
                return true;
            }
        }

        return false;
    }

}