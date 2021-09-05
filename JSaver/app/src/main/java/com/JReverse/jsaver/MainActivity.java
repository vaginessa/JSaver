package com.JReverse.jsaver;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.os.FileUtils;
import android.util.Pair;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.JReverse.jsaver.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;


    static public Pair<Integer, String> runCmd(boolean addCommandline, String... commands) {
        final int BUFSIZE = 1024;
        StringBuilder sb = new StringBuilder(BUFSIZE);

        if (addCommandline) {
            // add input string to stdout
            for (String c : commands) {
                sb.append(c);
                sb.append(" ");
            }
            sb.append("\n");
        }

        int exitVal = -1;
        ProcessBuilder builder = new ProcessBuilder(commands);
        builder.redirectErrorStream(true);
        try {
            Process p = builder.start();

            InputStream in = p.getInputStream();
            InputStreamReader isr = new InputStreamReader(in);
            char[] buf = new char[BUFSIZE];
            int len = -1;
            while (-1 != (len = isr.read(buf))) {
                sb.append(buf, 0, len);
            }
            exitVal = p.waitFor();

            p.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return new Pair<Integer, String>(exitVal, sb.toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Runtime runtime = Runtime.getRuntime();
                try {
                    String snapchatData = "/data/user/0/com.snapchat.android/files/file_manager/chat_snap";
                    Pair<Integer, String> ret = runCmd(true, "su", "-c", "ls " + snapchatData);
                    //Split up files
                    String[] files = ret.second.split("\n");

                    //Copy over files
                    int count = 0;
                    boolean first = true;
                    for (String file : files) {
                        if (!first) {
                            String newPath = "/storage/emulated/0/Snapchat/" + file.replaceAll(".chat_snap.0", "");
                            File checkFile = new File(newPath);
                            if (!checkFile.exists()) {
                                long fileSizeInBytes = checkFile.length();
                                long fileSizeInKB = fileSizeInBytes / 1024;
                                long fileSizeInMB = fileSizeInKB / 1024;
                                if (fileSizeInMB < 1) {
                                    String command = "cp " + snapchatData + "/" + file + " " + newPath + ".jpg";
                                    runCmd(true, "su", "-c", command);
                                    count++;
                                }
                                else if (fileSizeInMB > 1) {
                                    String command = "cp " + snapchatData + "/" + file + " " + newPath + ".mp4";
                                    runCmd(true, "su", "-c", command);
                                    count++;
                                }
                            }
                        } else {
                            first = !first;
                        }
                    }

                    makeToast(count);
                } catch (Exception e) {
                }
            }
        });
    }
    public void debugToast(String toast){
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast.makeText(context, toast, duration).show();
    }
    public void makeToast(int count){
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        if(count > 0){
            Toast.makeText(context, "Saved " + count + " snapchats", duration).show();
        }
        else{
            Toast.makeText(context, "No snapchats found", duration).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}