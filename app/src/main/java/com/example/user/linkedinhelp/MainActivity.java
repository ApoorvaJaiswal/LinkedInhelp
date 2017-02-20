package com.example.user.linkedinhelp;

import android.Manifest;
import android.app.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import opennlp.maxent.Main;

public class MainActivity extends Activity {

    Button helpButton, faqButton;
    Intent i, intent, intentService;
    public static int OVERLAY_PERMISSION_REQ_CODE_CHATHEAD = 1234;
    public static int OVERLAY_PERMISSION_REQ_CODE_CHATHEAD_MSG = 5678;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Utils.canDrawOverlays(MainActivity.this))
            startChatHead();
        else{
            requestPermission(OVERLAY_PERMISSION_REQ_CODE_CHATHEAD);
        }
        //intentService= new Intent(MainActivity.this,ChatHeadService.class);
        //startService(intentService);
        helpButton = (Button) findViewById(R.id.helpButton);
        faqButton = (Button) findViewById(R.id.FAQbutton);
        i = new Intent(this, Help.class);
        intent = new Intent(this, giveSolutionQA.class);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(i);
            }
        });

        faqButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(intent);
            }
        });
    }

    private void needPermissionDialog(final int requestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("You need to allow permission");
        builder.setPositiveButton("OK",
                new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        requestPermission(requestCode);
                    }
                });
        builder.setNegativeButton("Cancel", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void requestPermission(int requestCode) {
        if(Build.VERSION.SDK_INT>=23) {
            Toast.makeText(MainActivity.this,"23serviceNot",Toast.LENGTH_SHORT).show();

            if ((ContextCompat.checkSelfPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW)) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this,"serviceNot23",Toast.LENGTH_SHORT).show();

                //needPermissionDialog(requestCode);
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SYSTEM_ALERT_WINDOW}, 0);

            } else if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW)) == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this,"service",Toast.LENGTH_SHORT).show();
                Intent it = new Intent(MainActivity.this, ChatHeadService.class);
                //it.putExtra(Utils.EXTRA_MSG, str);
                startService(it);
            }
        }
        else {
            Toast.makeText(MainActivity.this,"serviceNot",Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
           // intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, requestCode);
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW)) == PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(MainActivity.this,"REq",Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
           // intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, requestCode);
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == OVERLAY_PERMISSION_REQ_CODE_CHATHEAD) {
            if (!Utils.canDrawOverlays(MainActivity.this)) {
                Toast.makeText(MainActivity.this,"utilsserviceNot",Toast.LENGTH_SHORT).show();
                needPermissionDialog(requestCode);
            } else {
                startChatHead();
            }

        } /*else if (requestCode == OVERLAY_PERMISSION_REQ_CODE_CHATHEAD_MSG) {
            if (!Utils.canDrawOverlays(MainActivity.this)) {
                needPermissionDialog(requestCode);
            } else {
                showChatHeadMsg();
            }

        }*/

    }
    private void startChatHead(){
        Toast.makeText(MainActivity.this,"service",Toast.LENGTH_SHORT).show();
        startService(new Intent(MainActivity.this, ChatHeadService.class));
    }
    private void showChatHeadMsg(){
        //java.util.Date now = new java.util.Date();
        //String str = "test by henry  " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now);

        Intent it = new Intent(MainActivity.this, ChatHeadService.class);
        //it.putExtra(Utils.EXTRA_MSG, str);
        startService(it);
    }
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        startService(new Intent(MainActivity.this, ChatHeadService.class));

    }



    @Override
    protected void onPause() {
        super.onPause();
        startService(new Intent(MainActivity.this, ChatHeadService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        startService(new Intent(MainActivity.this, ChatHeadService.class));
    }
}
