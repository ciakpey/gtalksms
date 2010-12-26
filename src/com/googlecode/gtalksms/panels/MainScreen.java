package com.googlecode.gtalksms.panels;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.gtalksms.MainService;
import com.googlecode.gtalksms.R;
import com.googlecode.gtalksms.XmppManager;
import com.googlecode.gtalksms.receivers.XmppListener;
import com.googlecode.gtalksms.tools.StringFmt;
import com.googlecode.gtalksms.tools.Tools;

public class MainScreen extends Activity {

    private MainService mainService;
    
    private XmppListener xmppListener = new XmppListener() {
        @Override
        public void onMessageReceived(String message) {
        }
        
        @Override
        public void onConnectionStatusChanged(int oldStatus, int status) {
            updateStatus(status);
        }

        @Override
        public void onPresenceStatusChanged(String person, String status) {
//            TextView console = (TextView) findViewById(R.id.Console);
//            console.append("\n" + person + " : " + status);
        }
    };

    private ServiceConnection mainServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mainService = ((MainService.LocalBinder)service).getService();
            registerListener();
        }

        public void onServiceDisconnected(ComponentName className) {
            mainService = null;
        }
    };
    
    @Override
    public void onPause() {
        super.onPause();
        
        if (MainService.getInstance() != null) {
            MainService.getInstance().setXmppListener(null);
        }  
    }
   
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        TextView label = (TextView) findViewById(R.id.VersionLabel);
        label.setText(StringFmt.Style("GTalkSMS " + Tools.getVersionName(getBaseContext(), getClass()), Typeface.BOLD));

        mainService = MainService.getInstance();
        registerListener();
        
        Button prefBtn = (Button) findViewById(R.id.Preferences);
        prefBtn.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                openOptionsMenu();
            }
        });
        
        Button aboutBtn = (Button) findViewById(R.id.About);
        aboutBtn.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(), About.class));
            }
        });
        
        Button clipboardBtn = (Button) findViewById(R.id.Clipboard);
        clipboardBtn.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                if (mainService != null ) {
                    mainService.sendClipboard();
                }
            }
        });

        Button startStopButton = (Button) findViewById(R.id.StartStop);
        startStopButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(".GTalkSMS.ACTION");
                ImageView statusImg = (ImageView) findViewById(R.id.StatusImage);
                statusImg.setImageResource(R.drawable.led_orange);
                
                if (MainService.getInstance() == null) {
                    bindService(intent, mainServiceConnection, Context.BIND_AUTO_CREATE);
                    startService(intent);
                }
                else {
                    try {
                        unbindService(mainServiceConnection);
                    } catch(Exception e) {
                        Log.w("unbinding service error", e);
                        mainService = null;
                    }
                    stopService(intent);
                }
            }
        });
        
        updateConsole();
    }
    
    public void updateConsole() {
//      TextView console = (TextView) findViewById(R.id.Console);
//      console.setAutoLinkMask(Linkify.ALL);
//      console.append("\n" + MainService.getInstance().getContactsList());
//      console.setText("http://code.google.com/p/gtalksms");
//      console.append("\n\nDonors\n");
//      console.append(Web.DownloadFromUrl("http://gtalksms.googlecode.com/hg/Donors"));
//      console.append("\n\nChange log\n");
//      console.append(Web.DownloadFromUrl("http://gtalksms.googlecode.com/hg/Changelog"));
    }
  
    public void updateStatus(int status) {
        ImageView statusImg = (ImageView) findViewById(R.id.StatusImage);
        switch (status) {
            case XmppManager.CONNECTED:
                statusImg.setImageResource(R.drawable.led_green);
                break;
            case XmppManager.DISCONNECTED:
                statusImg.setImageResource(R.drawable.led_red);
                break;
            case XmppManager.CONNECTING:
            case XmppManager.DISCONNECTING:
                statusImg.setImageResource(R.drawable.led_orange);
                break;
            default:
                break;
        }
    }
  
    public void registerListener() {
        if (mainService != null) {
            mainService.setXmppListener(xmppListener);
            //updateConsole();
            updateStatus(mainService.getConnectionStatus());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.preferences_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        int prefs_id;
        switch (item.getItemId()) {
        case R.id.connection_settings:
            prefs_id = R.xml.prefs_connection;
            break;
        case R.id.notification_settings:
            prefs_id = R.xml.prefs_notifications;
            break;
        case R.id.application_settings:
            prefs_id = R.xml.prefs_application;
            break;
        default:
            return super.onOptionsItemSelected(item);
        }
        Intent intent = new Intent(MainScreen.this, Preferences.class);
        intent.putExtra("panel", prefs_id);
        startActivity(intent);
        return true;
    }

}