package com.note4me.arduinopad;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements OnClickListener
{
    private static final String TAG = "MainActivity";
    private static final boolean D = true;
    
    public static final String PREFS_NAME = "com.note4me.arduinopad.txt";
    public static final String PREFS_KEY_COMMAND = "command";
    public static final String PREFS_KEY_SORTTYPE = "sorttype";
    public static final String PREFS_KEY_COLLECT_DEVICES = "collectdevices";
    public static final String PREFS_KEY_SHOW_NO_SERVICES = "noservices";
    public static final String PREFS_KEY_SHOW_AUDIOVIDEO = "audiovideo";
    public static final String PREFS_KEY_SHOW_COMPUTER = "computer";
    public static final String PREFS_KEY_SHOW_HEALTH = "health";
    public static final String PREFS_KEY_SHOW_MISC = "misc";
    public static final String PREFS_KEY_SHOW_IMAGING = "imaging";
    public static final String PREFS_KEY_SHOW_NETWORKING = "networking";
    public static final String PREFS_KEY_SHOW_PERIPHERAL = "peripheral";
    public static final String PREFS_KEY_SHOW_PHONE = "phone";
    public static final String PREFS_KEY_SHOW_TOY = "toy";
    public static final String PREFS_KEY_SHOW_WEARABLE = "wearable";
    public static final String PREFS_KEY_SHOW_UNCATEGORIZED = "uncategorized";
    private static final int REQUEST_CODE_LOC = 100;
    private static final int REQUEST = 200;

    final Context context = this;
	
    private ListView devicesView;
    private EditText searchBox;
	private Button btnSearchDevices;
	private ImageButton btnFilter;
	private BluetoothAdapter btAdapter;
	private SettingEntity settings;
	private BroadcastReceiver broadcastReceiver;
	private boolean isReceiverRegistered = false;
	private ProgressDialog loadingDialog;
	private String searchFilter = "";

	private boolean showNoServicesDevices = true;
	private boolean showAudioVideo = true;
	private boolean showComputer = true;
	private boolean showHealth = true;
	private boolean showMisc = true;
	private boolean showImaging = true;
	private boolean showNetworking = true;
	private boolean showPeripheral = true;
	private boolean showPhone = true;
	private boolean showToy = true;
	private boolean showWearable = true;
	private boolean showUncategorized = true;
	
	private boolean collectDevicesStat = false;
	
	public static int REQUEST_ENABLE_BT = 1;
	public static String DEVICE_ADDRESS = "DEVICE_ADDRESS";
	public static String DEVICE_NAME = "DEVICE_NAME";
	
	public enum SortType
	{
		SORT_BY_NAME,
		SORT_BY_TYPE,
		SORT_BY_BONDED_STATE
	}
	
	public static ArrayList<String> buttonCommands = new ArrayList<String>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "+++ ON CREATE +++");

		setContentView(R.layout.activity_main);

        hasPermissions();

		settings = new SettingEntity();
		restoreSettings();

		devicesView = (ListView)findViewById(R.id.devicesView);
		btnSearchDevices = (Button)findViewById(R.id.btnSearchDevices);
		
		btnFilter = (ImageButton)findViewById(R.id.btnFilter);
		registerForContextMenu(btnFilter);
		
		searchBox = (EditText)findViewById(R.id.searchBox);
		searchBox.addTextChangedListener(searchBoxTextChangedListener);

		devicesView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ListAdapter adapter = devicesView.getAdapter();
				DeviceData data = (DeviceData) adapter.getItem(position);
				connectToDevice(data);
			}
		});
		registerForContextMenu(devicesView);
		
		btnSearchDevices.setEnabled(false);

		if (collectDevicesStat)
			deserializeDevices();
		
        loadingDialog = new ProgressDialog(context);
        loadingDialog.setMessage("Searching...");
        loadingDialog.setCancelable(false);
        loadingDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Stop", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();

                if (btAdapter.isDiscovering())
                {
                	btAdapter.cancelDiscovery();
                }
            }
        });

		btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter == null)
		{
			showAlert(getResources().getString(R.string.no_bt_support));
			return;
		}
	}
	private void hasPermissions() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
					!= PackageManager.PERMISSION_GRANTED) {
				requestPermissions(new String[]{
				                Manifest.permission.READ_EXTERNAL_STORAGE,
				                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.BLUETOOTH_ADMIN,
                                Manifest.permission.BLUETOOTH,
								Manifest.permission.CAMERA},
                        REQUEST);
			}
		}
	}
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "READ_EXTERNAL_STORAGE permission granted", Toast.LENGTH_LONG).show();
                } else {
                    //Toast.makeText(this, "READ_EXTERNAL_STORAGE permission denied", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnSearchDevices: {
				scanForDevices();
			}
			break;
			case R.id.btnClearSearchBox: {
				searchFilter = "";
				searchBox.clearFocus();
				searchBox.setText("");

				InputMethodManager keyboard = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				keyboard.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);

				fillDevicesView();
			}
			break;
			case R.id.btnFilter: {
				openContextMenu(v);
			}
			break;
		}
	}
	private void restoreSettings()
	{
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		
		String defaultCmd = DeviceControlActivity.NOT_SET_TEXT;		
		for (int i = 0; i < 12; i++)
		{
			String cmd = settings.getString(PREFS_KEY_COMMAND + i, "");
			if (cmd.isEmpty())
				cmd = defaultCmd;

			buttonCommands.add(cmd);
		}
		
		String sortValue = settings.getString(PREFS_KEY_SORTTYPE, "SORT_BY_NAME");		

		if (sortValue.equals("SORT_BY_TYPE"))
			this.settings.setSortType(SortType.SORT_BY_TYPE);
		else if (sortValue.equals("SORT_BY_BONDED_STATE"))
			this.settings.setSortType(SortType.SORT_BY_BONDED_STATE);
		else
			this.settings.setSortType(SortType.SORT_BY_NAME);
		
		showNoServicesDevices = settings.getBoolean(PREFS_KEY_SHOW_NO_SERVICES, true);
		showAudioVideo = settings.getBoolean(PREFS_KEY_SHOW_AUDIOVIDEO, true);
		showComputer = settings.getBoolean(PREFS_KEY_SHOW_COMPUTER, true);
		showHealth = settings.getBoolean(PREFS_KEY_SHOW_HEALTH, true);
		showMisc = settings.getBoolean(PREFS_KEY_SHOW_MISC, true);
		showImaging = settings.getBoolean(PREFS_KEY_SHOW_IMAGING, true);
		showNetworking = settings.getBoolean(PREFS_KEY_SHOW_NETWORKING, true);
		showPeripheral = settings.getBoolean(PREFS_KEY_SHOW_PERIPHERAL, true);
		showPhone = settings.getBoolean(PREFS_KEY_SHOW_PHONE, true);
		showToy = settings.getBoolean(PREFS_KEY_SHOW_TOY, true);
		showWearable = settings.getBoolean(PREFS_KEY_SHOW_WEARABLE, true);
		showUncategorized = settings.getBoolean(PREFS_KEY_SHOW_UNCATEGORIZED, true);
		
		collectDevicesStat = settings.getBoolean(PREFS_KEY_COLLECT_DEVICES, false);
	}

	private void saveSettings()
	{
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();

		String sortValue = "SORT_BY_NAME";
		switch (this.settings.getSortType())
		{
			case SORT_BY_TYPE:
				sortValue = "SORT_BY_TYPE";
				break;
			case SORT_BY_BONDED_STATE:
				sortValue = "SORT_BY_BONDED_STATE";
				break;
			default: //name
				break;
		}
		editor.putString(PREFS_KEY_SORTTYPE, sortValue);
		
		editor.putBoolean(PREFS_KEY_SHOW_NO_SERVICES, showNoServicesDevices);
		editor.putBoolean(PREFS_KEY_SHOW_AUDIOVIDEO, showAudioVideo);
		editor.putBoolean(PREFS_KEY_SHOW_COMPUTER, showComputer);
		editor.putBoolean(PREFS_KEY_SHOW_HEALTH, showHealth);
		editor.putBoolean(PREFS_KEY_SHOW_MISC, showMisc);
		editor.putBoolean(PREFS_KEY_SHOW_IMAGING, showImaging);
		editor.putBoolean(PREFS_KEY_SHOW_NETWORKING, showNetworking);
		editor.putBoolean(PREFS_KEY_SHOW_PERIPHERAL, showPeripheral);
		editor.putBoolean(PREFS_KEY_SHOW_PHONE, showPhone);
		editor.putBoolean(PREFS_KEY_SHOW_TOY, showToy);
		editor.putBoolean(PREFS_KEY_SHOW_WEARABLE, showWearable);
		editor.putBoolean(PREFS_KEY_SHOW_UNCATEGORIZED, showUncategorized);
		
		editor.putBoolean(PREFS_KEY_COLLECT_DEVICES, collectDevicesStat);
		
		editor.commit();
	}
	
	private void searchDevices()
	{		
		getPairedDevices();
		
		// Create a BroadcastReceiver for ACTION_FOUND
		broadcastReceiver = new BroadcastReceiver()
		{
		    public void onReceive(Context context, Intent intent)
		    {
		        String action = intent.getAction();
		        // When discovery finds a device
		        if (BluetoothDevice.ACTION_FOUND.equals(action))
		        {
		            // Get the BluetoothDevice object from the Intent
		            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

		            if (device != null)
		            {
		            	addBluetoothDevice(device);
		            	fillDevicesView();
		            }
		        }
		        else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
		        {
		        	loadingDialog.dismiss();		        	
		        	Toast.makeText(context, R.string.search_finished, Toast.LENGTH_SHORT).show();					
		        	fillDevicesView();		        
		        }
		    }
		};

		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(broadcastReceiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(broadcastReceiver, filter);
        
        isReceiverRegistered = true;
	}
	
	private void getPairedDevices()
	{
		// Get a set of currently paired devices
		Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
		if (pairedDevices.size() > 0)
		{			
			for (BluetoothDevice device : pairedDevices)
			{
				addBluetoothDevice(device);
		    }

			fillDevicesView();
		}
	}
	
	private boolean needAddToFiltered(DeviceData item)
	{
		if (item.getMajorDeviceClass() == BluetoothClass.Device.Major.AUDIO_VIDEO && showAudioVideo)
			return true;
		if (item.getMajorDeviceClass() == BluetoothClass.Device.Major.COMPUTER && showComputer)
			return true;
		if (item.getMajorDeviceClass() == BluetoothClass.Device.Major.HEALTH && showHealth)
			return true;
		if (item.getMajorDeviceClass() == BluetoothClass.Device.Major.IMAGING && showImaging)
			return true;
		if (item.getMajorDeviceClass() == BluetoothClass.Device.Major.MISC && showMisc)
			return true;
		if (item.getMajorDeviceClass() == BluetoothClass.Device.Major.NETWORKING && showNetworking)
			return true;
		if (item.getMajorDeviceClass() == BluetoothClass.Device.Major.PERIPHERAL && showPeripheral)
			return true;
		if (item.getMajorDeviceClass() == BluetoothClass.Device.Major.PHONE && showPhone)
			return true;
		if (item.getMajorDeviceClass() == BluetoothClass.Device.Major.TOY && showToy)
			return true;
		if (item.getMajorDeviceClass() == BluetoothClass.Device.Major.UNCATEGORIZED && showUncategorized)
			return true;
		if (item.getMajorDeviceClass() == BluetoothClass.Device.Major.WEARABLE && showWearable)
			return true;
		
		return false;
	}
	
	private void fillDevicesView()
	{
		ArrayList<DeviceData> filtered = new ArrayList<DeviceData>();
        for (DeviceData item : settings.getDevices())
        {
        	if (!needAddToFiltered(item))
        		continue;
        	
        	String name = item.getName() + "";
        	if (searchFilter == null || searchFilter.isEmpty() || name.toLowerCase().contains(searchFilter.toLowerCase()))
        	{
        		if (showNoServicesDevices)
        		{
        			filtered.add(item);
        		}
        		else
        		{
        			boolean hasServices = item.getUuids().size() > 0;
        			if (hasServices)
        				filtered.add(item);
        		}
        	}
		}

		DevicesRowAdapter adapter = new DevicesRowAdapter(this, filtered, settings.getSortType());
		devicesView.setAdapter(adapter);

		saveSettings();
	}
	
	private void scanForDevices()
	{
        // If we're already discovering, stop it
        if (btAdapter.isDiscovering())
        {
        	btAdapter.cancelDiscovery();
        	loadingDialog.dismiss();
        	return;
        }

        btAdapter.startDiscovery();
        loadingDialog.show();
	}
	
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (D) Log.d(TAG, "onActivityResult " + resultCode);
		
		if (requestCode == REQUEST_ENABLE_BT)
		{
			if (resultCode == RESULT_OK)
			{
				btnSearchDevices.setEnabled(true);
				searchDevices();
			}
			else
			{
				showAlert(getResources().getString(R.string.bt_not_enabled_leaving));
			}
		}
	}

    @Override
    public void onStart()
    {
        super.onStart();
        if (D) Log.d(TAG, "++ ON START ++");

		if (!btAdapter.isEnabled())
		{
		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		    return;
		}

		btnSearchDevices.setEnabled(true);
		searchDevices();
    }

    @Override
    public synchronized void onResume()
    {
        super.onResume();
        if (D) Log.d(TAG, "+ ON RESUME +");
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (D) Log.d(TAG, "--- ON DESTROY ---");

        if (btAdapter != null && btAdapter.isDiscovering())
        {
            btAdapter.cancelDiscovery();
        }
        
        if (isReceiverRegistered)
        	unregisterReceiver(broadcastReceiver);
    }

    @Override
    public synchronized void onPause()
    {
        super.onPause();
        if (D) Log.d(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (D) Log.d(TAG, "-- ON STOP --");
    }
    
    private TextWatcher searchBoxTextChangedListener = new TextWatcher()
    {
    	public void afterTextChanged(Editable s)
    	{
    		searchFilter = searchBox.getText().toString();
    		fillDevicesView();
        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    };
    
  /*  private OnClickListener btnClearSearchBoxClick = new OnClickListener()
    {
    	@Override
    	public void onClick(View v)
    	{
    		searchFilter = "";
    		searchBox.clearFocus();
    		searchBox.setText("");

            InputMethodManager keyboard = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);

    		fillDevicesView();
    	}
    };
    
    private OnClickListener btnFilterClick = new OnClickListener()
    {
    	@Override
    	public void onClick(View v)
    	{
    		openContextMenu(v);
    	}
    };

    */
	private void showAlert(String message)
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		alertDialogBuilder.setTitle(message);
		
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getId() == R.id.devicesView)
		{
			MenuInflater inflater = getMenuInflater();
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			ListAdapter adapter = devicesView.getAdapter();
			DeviceData data = (DeviceData) adapter.getItem(info.position);
			
			menu.setHeaderTitle(data.getName());
		    inflater.inflate(R.menu.activity_main_context, menu);
		}
		else if (v.getId() == R.id.btnFilter)
		{			
			MenuInflater inflater = getMenuInflater();
		    inflater.inflate(R.menu.filter_popup, menu);
		    
		    menu.findItem(R.id.menu_show_without_services).setChecked(showNoServicesDevices);
		    menu.findItem(R.id.menu_filter_av).setChecked(showAudioVideo);
		    menu.findItem(R.id.menu_filter_computer).setChecked(showComputer);
		    menu.findItem(R.id.menu_filter_health).setChecked(showHealth);
		    menu.findItem(R.id.menu_filter_imaging).setChecked(showImaging);
		    menu.findItem(R.id.menu_filter_misc).setChecked(showMisc);
		    menu.findItem(R.id.menu_filter_networking).setChecked(showNetworking);
		    menu.findItem(R.id.menu_filter_peripheral).setChecked(showPeripheral);
		    menu.findItem(R.id.menu_filter_phone).setChecked(showPhone);
		    menu.findItem(R.id.menu_filter_toy).setChecked(showToy);
		    menu.findItem(R.id.menu_filter_uncategorized).setChecked(showUncategorized);
		    menu.findItem(R.id.menu_filter_wearable).setChecked(showWearable);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.menu_show_without_services)
		{
			showNoServicesDevices = !showNoServicesDevices;
			fillDevicesView();
			return true;
		}
		if (item.getItemId() == R.id.menu_filter_av)
		{
			showAudioVideo = !showAudioVideo;
			fillDevicesView();
			return true;
		}
		if (item.getItemId() == R.id.menu_filter_computer)
		{
			showComputer = !showComputer;
			fillDevicesView();
			return true;
		}
		if (item.getItemId() == R.id.menu_filter_health)
		{
			showHealth = !showHealth;
			fillDevicesView();
			return true;
		}
		if (item.getItemId() == R.id.menu_filter_imaging)
		{
			showImaging = !showImaging;
			fillDevicesView();
			return true;
		}
		if (item.getItemId() == R.id.menu_filter_misc)
		{
			showMisc = !showMisc;
			fillDevicesView();
			return true;
		}
		if (item.getItemId() == R.id.menu_filter_networking)
		{
			showNetworking = !showNetworking;
			fillDevicesView();
			return true;
		}
		if (item.getItemId() == R.id.menu_filter_peripheral)
		{
			showPeripheral = !showPeripheral;
			fillDevicesView();
			return true;
		}
		if (item.getItemId() == R.id.menu_filter_phone)
		{
			showPhone = !showPhone;
			fillDevicesView();
			return true;
		}
		if (item.getItemId() == R.id.menu_filter_toy)
		{
			showToy = !showToy;
			fillDevicesView();
			return true;
		}
		if (item.getItemId() == R.id.menu_filter_uncategorized)
		{
			showUncategorized = !showUncategorized;
			fillDevicesView();
			return true;
		}
		if (item.getItemId() == R.id.menu_filter_wearable)
		{
			showWearable = !showWearable;
			fillDevicesView();
			return true;
		}
		
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		ListAdapter adapter = devicesView.getAdapter();
		DeviceData data = (DeviceData) adapter.getItem(info.position);

		switch (item.getItemId())
		{
			case R.id.menu_connect:
				connectToDevice(data);
				return true;

			case R.id.menu_device_info:
				showDeviceInfo(data);
				return true;

			default:
				return super.onContextItemSelected(item);
		}
	}
	
	private void showDeviceInfo(DeviceData itemData)
	{
		Map<String, String> services = BluetoothUtils.getDeviceServicesMap(itemData.getUuids());

		ArrayList<InfoData> data = new ArrayList<InfoData>();

		for (Map.Entry<String, String> entry : services.entrySet())
		{
	    	String key = entry.getKey();
	        String value = entry.getValue();
	        
	        InfoData id = new InfoData(key, value, !value.startsWith("Unknown"));
	        data.add(id);
		}
		
		DeviceInfoDialog dialog = DeviceInfoDialog.newInstance(data);
		dialog.show(getFragmentManager(), "dialog");
	}
	
	private void connectToDevice(DeviceData itemData)
	{
		// If we're already discovering, stop it
        if (btAdapter.isDiscovering())
        {
        	btAdapter.cancelDiscovery();
        	loadingDialog.dismiss();
        }
        
        Intent intent = new Intent(context, DeviceControlActivity.class);
        intent.putExtra(DEVICE_NAME, itemData.getName());
        intent.putExtra(DEVICE_ADDRESS, itemData.getAddress());
        startActivity(intent);
	}

	@Override
	public boolean onPrepareOptionsMenu (Menu menu)
	{
        menu.findItem(R.id.menu_enable_bt).setEnabled(!btAdapter.isEnabled());

        if (settings.getSortType() == SortType.SORT_BY_NAME)
        	menu.findItem(R.id.menu_sort_by_name).setChecked(true);
        if (settings.getSortType() == SortType.SORT_BY_TYPE)
        	menu.findItem(R.id.menu_sort_by_type).setChecked(true);
        if (settings.getSortType() == SortType.SORT_BY_BONDED_STATE)
        	menu.findItem(R.id.menu_sort_by_bonded_state).setChecked(true);
        
        menu.findItem(R.id.menu_collect_devices).setChecked(collectDevicesStat);
        
        return true;
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        	case R.id.menu_collect_devices:
        		collectDevicesStat = !collectDevicesStat;
        		saveSettings();
        		return true;

        	case R.id.menu_sort_by_name:
        		settings.setSortType(SortType.SORT_BY_NAME);
        		fillDevicesView();
        		return true;
        	
        	case R.id.menu_sort_by_type:
        		settings.setSortType(SortType.SORT_BY_TYPE);
        		fillDevicesView();
        		return true;

        	case R.id.menu_sort_by_bonded_state:
        		settings.setSortType(SortType.SORT_BY_BONDED_STATE);
        		fillDevicesView();
        		return true;
        		
        	case R.id.menu_enable_bt:

        		if (!btAdapter.isEnabled())
        		{
        		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        		}
            	
            	return true;
            
        	default:
                return super.onOptionsItemSelected(item);
        }
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	private void addBluetoothDevice(BluetoothDevice device)
	{
        String deviceName = null;
        String deviceAddress = null;

        try
        {        	
            deviceName = device.getName();
            deviceAddress = device.getAddress();
        }
        catch (Exception e)
        {
        	if (D) Log.e(TAG, "addBluetoothDevice() failed", e);
        	Toast.makeText(context, "Error while adding device " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        if (deviceAddress == null || deviceAddress.isEmpty())
        	return;

        String emptyName = getResources().getString(R.string.empty_device_name);
        if (deviceName == null || deviceName.isEmpty())
        	deviceName = emptyName;
        
        for (DeviceData item : settings.getDevices())
        {
        	String name = item.getName() + "";
        	String addr = item.getAddress() + "";
			if (name.equals(deviceName) && addr.equals(deviceAddress))
			{
				item.setBondState(device.getBondState());
				return;
			}
		}
		
		DeviceData dataToAdd = new DeviceData(device, emptyName);
		settings.getDevices().add(dataToAdd);
		
		if (collectDevicesStat)
			serializeDevices();
	}

	private void deserializeDevices()
	{
		String jsonData = "";
		
		try
		{
			String fileName = Environment.getExternalStorageDirectory().getPath() + "/" + PREFS_NAME;
			File myFile = new File(fileName);
			FileInputStream fIn = new FileInputStream(myFile);
			BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
			String aDataRow = "";
			String aBuffer = "";
			while ((aDataRow = myReader.readLine()) != null)
			{
				aBuffer += aDataRow + "\n";
			}
			jsonData = aBuffer;
			myReader.close();
		}
		catch (Exception e)
		{
			if (D) Log.e(TAG, "deserializeDevices() failed", e);
		}
		
    	String emptyName = getResources().getString(R.string.empty_device_name);
		SettingEntity tmp = DeviceSerializer.deserialize(jsonData);

		SortType st = SortType.SORT_BY_NAME;
		if (settings != null)
		{
			st = settings.getSortType();
		}

		if (tmp != null)
		{
			settings = tmp;
			settings.setSortType(st);

	        for (DeviceData item : settings.getDevices())
	        {
	        	String deviceName = item.getName();
	            if (deviceName == null || deviceName.isEmpty())
	            	item.setName(emptyName);
			}
		}
		else
		{
			settings = new SettingEntity();
			settings.setSortType(st);
		}
	}

	private void serializeDevices()
	{
		String jsonData = DeviceSerializer.serialize(settings);

		try
		{
			String fileName = Environment.getExternalStorageDirectory().getPath() + "/" + PREFS_NAME;
			File myFile = new File(fileName);

			FileWriter filewriter = new FileWriter(myFile);
            BufferedWriter out = new BufferedWriter(filewriter);
            
            out.write(jsonData);
            
            out.close();
	    }
		catch (Exception e)
		{
			if (D) Log.e(TAG, "serializeDevices() failed", e);
		}
	}
	
	@Override
	public boolean onSearchRequested()
	{
		searchBox.requestFocus();
		searchBox.postDelayed(new Runnable()
		{
            @Override
            public void run()
            {
                InputMethodManager keyboard = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.showSoftInput(searchBox, 0);
            }
        }, 200);

		return false; 
	}
}
