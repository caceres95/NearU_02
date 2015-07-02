package nearu.nearu_02;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;


public class DeviceList extends ActionBarActivity {

    public static String EXTRA_ADDRESS = "device_address";
    Button btnPaired;
    Button btStart;
    ListView devicelist; //hello
    ListView rssilist;
    boolean repeat = false;
    int rssi = 0;
    int cont = 0;

    ArrayList list2 = new ArrayList();

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                list2.add(name + " => " + rssi + "dBm\n");
                //rssi_msg.setText(rssi_msg.getText() + name + " => " + rssi + "dBm\n");
                if ((name.equals("CBCA") && (rssi < -90))) {
                    vibrar();
                    Toast myToast = new Toast(getApplicationContext());
                    myToast.setGravity(9, 7, 7);
                    Toast.makeText(getApplicationContext(), "¡Estas olvidando tu dispositivo!" + " " +
                            name, Toast.LENGTH_LONG).show();
                }
            }
            ArrayAdapter adapter2 = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, list2);
            rssilist.setAdapter(adapter2);
        }
    };
    private BluetoothAdapter myBluetooth = null;
    private OutputStream outStream = null;
    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Make an intent to start next activity.
            Intent i = new Intent(DeviceList.this, PairedDevices.class); //Change the activity.

            i.putExtra(EXTRA_ADDRESS, address); //this will be received at ledControl (class) Activity
            startActivity(i);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));

        btnPaired = (Button) findViewById(R.id.button);
        btStart = (Button) findViewById(R.id.btStart);
        devicelist = (ListView) findViewById(R.id.listView);
        rssilist = (ListView) findViewById(R.id.listView2);
        setTitle("Device List");

        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        if (myBluetooth == null)
        {
            //Show a mensag. that thedevice has no bluetooth adapter
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
            //finish apk
            finish();
        }
        else if (!myBluetooth.isEnabled()) {
            //Ask to the user turn the bluetooth on
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon, 1);
        }

        btnPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pairedDevicesList(); //method that will be called
            }
        });

        btStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                repeat = true;
                start();
            }
        });
    }

    private void start() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (myBluetooth.isDiscovering())
                                        myBluetooth.cancelDiscovery();
                                    if (repeat) {
                                        start();
                                    }
                                }
                            },
                20000);
        myBluetooth.startDiscovery();
        cont++;
    }

    private void vibrar() {
        Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        v.vibrate(500);
    }

    private void pairedDevicesList()
    {
        Set<BluetoothDevice> pairedDevices;
        pairedDevices = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice bt : pairedDevices) {
                list.add(bt.getName() + "\n" + bt.getAddress());
            }
        } else {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        devicelist.setAdapter(adapter);
        devicelist.setOnItemClickListener(myListClickListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_device_list, menu);
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
}
