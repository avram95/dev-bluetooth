package com.note4me.arduinopad;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.note4me.arduinopad.MainActivity.SortType;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DevicesRowAdapter extends ArrayAdapter<DeviceData>
{
	private final Context context;
	private final ArrayList<DeviceData> devices;
	private final MainActivity.SortType sortType;
	
	public DevicesRowAdapter(Context context, ArrayList<DeviceData> devices, MainActivity.SortType sortBy)
	{
		super(context, R.layout.bt_device_row, devices);

		this.context = context;
	    this.devices = devices;
	    this.sortType = sortBy;

	    Collections.sort(devices, new Comparator<DeviceData>()
   		{
	        @Override
	        public int compare(DeviceData o1, DeviceData o2)
	        {
	        	if (sortType == SortType.SORT_BY_NAME)
	        	{
	        		return o1.getName().compareTo(o2.getName());
	        	}
	        	else if (sortType == SortType.SORT_BY_BONDED_STATE)
	        	{
	        		Integer i1 = o1.getBondState();
	        		Integer i2 = o2.getBondState();
	        		return i1.compareTo(i2) * (-1);
	        	}
	        	else //type
	        	{
	        		Integer i1 = o1.getDeviceClass();
	        		Integer i2 = o2.getDeviceClass();
	        		return i1.compareTo(i2);
	        	}
	        }
	    });
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
	    DeviceData device = devices.get(position);
	    
	    LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View rowView = inflater.inflate(R.layout.bt_device_row, parent, false);
	    
	    TextView deviceName = (TextView) rowView.findViewById(R.id.deviceName);
	    TextView deviceDesc = (TextView) rowView.findViewById(R.id.deviceDesc);
	    TextView deviceState = (TextView) rowView.findViewById(R.id.deviceState);
	    ImageView deviceIcon = (ImageView) rowView.findViewById(R.id.deviceIcon);
	    TextView deviceServices = (TextView) rowView.findViewById(R.id.deviceServices);

	    String bondedState = device.getBondState() == BluetoothDevice.BOND_BONDED ? "bonded" : "";

	    deviceName.setText(device.getName());
	    deviceState.setText(bondedState);
	    deviceServices.setText(device.getUuids().size() == 0 ? "no services" : "");
	    
	    deviceServices.setVisibility(deviceServices.getText().length() == 0 ? View.INVISIBLE : View.VISIBLE);
	    deviceState.setVisibility(deviceState.getText().length() == 0 ? View.INVISIBLE : View.VISIBLE);

	    if (deviceServices.getVisibility() == View.VISIBLE)
	    {
	    	deviceName.setTextColor(0xff666666);
	    	deviceDesc.setTextColor(0xff666666);
	    }
	    
	    int majorClass = device.getMajorDeviceClass();
	    int deviceClass = device.getDeviceClass();
	    
	    switch (majorClass)
	    {
	    	case BluetoothClass.Device.Major.UNCATEGORIZED:
	    		deviceDesc.setText("Uncategorized");
	    		deviceIcon.setImageResource(R.drawable.icomisc);
	    		break;
	    	case BluetoothClass.Device.Major.HEALTH:
	    		deviceDesc.setText("Health");
	    		deviceIcon.setImageResource(R.drawable.icohealth);
	    		break;
	    	case BluetoothClass.Device.Major.TOY:
	    		deviceDesc.setText("Toy");
	    		deviceIcon.setImageResource(R.drawable.icotoy);
	    		break;
	    	case BluetoothClass.Device.Major.IMAGING:
	    		deviceDesc.setText("Imaging");
	    		deviceIcon.setImageResource(R.drawable.icomisc);
	    		break;
	    	case BluetoothClass.Device.Major.NETWORKING:
	    		deviceDesc.setText("Networking");
	    		deviceIcon.setImageResource(R.drawable.iconetworking);
	    		break;
	    	case BluetoothClass.Device.Major.PERIPHERAL:
	    		deviceDesc.setText("Peripheral");
	    		deviceIcon.setImageResource(R.drawable.icoperipheral);
	    		break;
	    	case BluetoothClass.Device.Major.MISC:
	    		deviceDesc.setText("Mics");
	    		deviceIcon.setImageResource(R.drawable.icomisc);
	    		break;
	    	case BluetoothClass.Device.Major.WEARABLE:
	    		deviceDesc.setText("Wearable");
	    		deviceIcon.setImageResource(R.drawable.icomisc);
	    		break;

	    	default:
	    		switch (deviceClass)
	    		{
	    			case BluetoothClass.Device.PHONE_CELLULAR:
	    				deviceDesc.setText("Phone (cellular)");
	    				deviceIcon.setImageResource(R.drawable.icophone);
	    				break;
	    			case BluetoothClass.Device.PHONE_CORDLESS:
	    				deviceDesc.setText("Phone (cordless)");
	    				deviceIcon.setImageResource(R.drawable.icophone);
	    				break;
	    			case BluetoothClass.Device.PHONE_SMART:
	    				deviceDesc.setText("Phone (smart)");
	    				deviceIcon.setImageResource(R.drawable.icosmart);
	    				break;
	    			case BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE:
	    				deviceDesc.setText("AV (handsfree)");
	    				deviceIcon.setImageResource(R.drawable.icohandsfree);
	    				break;
	    			case BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET:
	    				deviceDesc.setText("AV (headset)");
	    				deviceIcon.setImageResource(R.drawable.icoheadset);
	    				break;
	    			case BluetoothClass.Device.COMPUTER_LAPTOP:
	    				deviceDesc.setText("Computer (laptop)");
	    				deviceIcon.setImageResource(R.drawable.icolaptop);
	    				break;
	    			case BluetoothClass.Device.COMPUTER_PALM_SIZE_PC_PDA:
	    				deviceDesc.setText("Computer (palm pda)");
	    				deviceIcon.setImageResource(R.drawable.icolaptop);
	    				break;
	    			case BluetoothClass.Device.COMPUTER_DESKTOP:
	    				deviceDesc.setText("Computer (desktop)");
	    				deviceIcon.setImageResource(R.drawable.icocomputer);
	    				break;
	    			case BluetoothClass.Device.COMPUTER_SERVER:
	    				deviceDesc.setText("Computer (server)");
	    				deviceIcon.setImageResource(R.drawable.icocomputer);
	    				break;
	    			case BluetoothClass.Device.AUDIO_VIDEO_PORTABLE_AUDIO:
	    				deviceDesc.setText("AV (portable)");
	    				deviceIcon.setImageResource(R.drawable.icoportable);
	    				break;
	    			case BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER:
	    				deviceDesc.setText("AV (loudspeaker)");
	    				deviceIcon.setImageResource(R.drawable.icoloudspeaker);
	    				break;
	    			case BluetoothClass.Device.AUDIO_VIDEO_UNCATEGORIZED:
	    				deviceDesc.setText("AV (Uncategorized)");
	    				deviceIcon.setImageResource(R.drawable.icomisc);
	    				break;
	    			case BluetoothClass.Device.AUDIO_VIDEO_VIDEO_DISPLAY_AND_LOUDSPEAKER:
	    				deviceDesc.setText("AV (display and loudspeaker)");
	    				deviceIcon.setImageResource(R.drawable.icoloudspeakerdisplay);
	    				break;
	    			default:
	    				deviceDesc.setText("Unknown");
	    				deviceIcon.setImageResource(R.drawable.icomisc);
	    				break;
	    		}
	    		break;
	    }
	    
	    return rowView;
	}
}
