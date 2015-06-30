package com.example.mysms;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class ThreadConversationActivity extends ActionBarActivity implements OnClickListener {
	ListView listView;
    EditText smsEditText;
    Button composeButton;
    String address;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.thread_conversation_layout);
		Intent intent = getIntent();
		String threadId = intent.getStringExtra("threadId");
		 address  = intent.getStringExtra("ADDRESS");
		ArrayList<String> converstaion = getSmsForThread(threadId);

		listView = (ListView) findViewById(R.id.listView);
		smsEditText = (EditText)findViewById(R.id.edittext);
		composeButton  =(Button)findViewById(R.id.addcontact);
		composeButton.setOnClickListener(this);
		
		listView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, converstaion));
	}

	ArrayList<String> getSmsForThread(String threadId) {
		ArrayList<String> conversation = new ArrayList<String>();
		Uri uriSMSURI = Uri.parse(Uri.parse("content://sms/conversations/")
				+ threadId);
		Cursor cursor = getContentResolver().query(uriSMSURI, null,
				"thread_id=?", new String[] { threadId }, null);

		if (cursor.getCount() > 0) {

			while (cursor.moveToNext()) {
				String result = "";

				result = cursor.getString(cursor.getColumnIndex("BODY"));
				conversation.add(result);

			}
		}
		return conversation;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.addcontact:
			if (!address.equals("") && !smsEditText.getText().toString().equals("")) {
				SmsManager smsManager = SmsManager.getDefault();
				smsManager.sendTextMessage(address, null, smsEditText.getText().toString(), null, null);
		         Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();

			}
			break;

		default:
			break;
		}
	}

}
