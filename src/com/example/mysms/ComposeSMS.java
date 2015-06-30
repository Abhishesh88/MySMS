package com.example.mysms;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ComposeSMS extends Activity implements OnClickListener {
	static final int PICK_CONTACT = 1;

	Button plusButton;
	Button sendButton;
	String cNumber;
	EditText contactEditText;
	EditText descriptionEditext;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.compose_sms);
		plusButton = (Button) findViewById(R.id.plusButton);
		contactEditText = (EditText) findViewById(R.id.editText);
		descriptionEditext = (EditText) findViewById(R.id.description);
		sendButton  = (Button)findViewById(R.id.sendButton);
		sendButton.setOnClickListener(this);
		plusButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.plusButton:
			Intent intent = new Intent(Intent.ACTION_PICK,
					ContactsContract.Contacts.CONTENT_URI);
			startActivityForResult(intent, PICK_CONTACT);
			break;
		case R.id.sendButton:
			if (!cNumber.equals("") && !descriptionEditext.getText().toString().equals("")) {
				SmsManager smsManager = SmsManager.getDefault();
				smsManager.sendTextMessage(cNumber, null, descriptionEditext.getText().toString(), null, null);
		         Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();

			}
			 
			break;
		default:
			break;
		}
		

	}

	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		switch (reqCode) {
		case (PICK_CONTACT):
			if (resultCode == Activity.RESULT_OK) {

				Uri contactData = data.getData();
				Cursor c = managedQuery(contactData, null, null, null, null);
				if (c.moveToFirst()) {

					String id = c
							.getString(c
									.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

					String hasPhone = c
							.getString(c
									.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

					if (hasPhone.equalsIgnoreCase("1")) {
						Cursor phones = getContentResolver()
								.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
										null,
										ContactsContract.CommonDataKinds.Phone.CONTACT_ID
												+ " = " + id, null, null);
						phones.moveToFirst();
						cNumber = phones.getString(phones
								.getColumnIndex("data1"));
						System.out.println("number is:" + cNumber);
					}
					String name = c
							.getString(c
									.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

					contactEditText.setText(name);
				}
			}
			break;
		}
	}
}
