package com.example.mysms;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.Telephony.Sms.Inbox;
import android.support.v4.content.CursorLoader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

public class MainActivity extends ActionBarActivity implements
		android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor>,
		android.view.View.OnClickListener, OnItemClickListener {

	private Button composeSmsButton;
	private SimpleCursorAdapter mAdapter;

	private SMSObserver observer = new SMSObserver();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

	
		SwipeMenuListView listView = (SwipeMenuListView) findViewById(android.R.id.list);
		listView.setEmptyView(findViewById(android.R.id.empty));
		
		composeSmsButton = (Button) findViewById(R.id.composeSmsButton);
		composeSmsButton.setOnClickListener(this);

		// Create adapter and set it to our ListView
		final String[] fromFields = new String[] {
				SmsQuery.PROJECTION[SmsQuery.ADDRESS],
				SmsQuery.PROJECTION[SmsQuery.BODY],
				SmsQuery.PROJECTION[SmsQuery.THREAD_ID],
				SmsQuery.PROJECTION[SmsQuery.READ] };
		final int[] toViews = new int[] { android.R.id.text1,
				android.R.id.text2 };
		mAdapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_2, null, fromFields, toViews,
				0) {
			@Override
			public View getView(int arg0, View view, ViewGroup arg2) {
				View view1 = super.getView(arg0, view, arg2);
				TextView text1 = (TextView) view1
						.findViewById(android.R.id.text1);

				Cursor cursor = (Cursor) mAdapter.getItem(arg0);
				String flag = cursor.getString(cursor.getColumnIndex("READ"));
				if (flag.equals("0")) {
					text1.setTextColor(Color.RED);

				} else {
					text1.setTextColor(Color.DKGRAY);
				}
				String nubmer = cursor.getString(cursor
						.getColumnIndex("ADDRESS"));
				if (contactExists(mContext, nubmer)) {
					view1.setBackgroundColor(Color.WHITE);
				} else {
					view1.setBackgroundColor(Color.LTGRAY);
				}
				return view1;
			}
		};

		
		listView.setAdapter(mAdapter);
		// Placeholder to process incoming SEND/SENDTO intents
		String intentAction = getIntent() == null ? null : getIntent()
				.getAction();
		if (!TextUtils.isEmpty(intentAction)
				&& (Intent.ACTION_SENDTO.equals(intentAction) || Intent.ACTION_SEND
						.equals(intentAction))) {
			// TODO: Handle incoming SEND and SENDTO intents by pre-populating
			// UI components
			Toast.makeText(
					this,
					"Handle SEND and SENDTO intents: "
							+ getIntent().getDataString(), Toast.LENGTH_SHORT)
					.show();
		}
		// Simple query to show the most recent SMS messages in the inbox
		getSupportLoaderManager().initLoader(SmsQuery.TOKEN, null, this);
		listView.setOnItemClickListener(this);

		getContentResolver().registerContentObserver(SmsQuery.CONTENT_URI,
				true, observer);

		if (Utils.hasKitKat()) {
			if (!Utils.isDefaultSmsApp(this)) {
				promptSetDefaultDialog();
			}
		}

		// step 1. create a MenuCreator
		SwipeMenuCreator creator = new SwipeMenuCreator() {

			@Override
			public void create(SwipeMenu menu) {
				// create "delete" item
				SwipeMenuItem deleteItem = new SwipeMenuItem(
						getApplicationContext());
				// set item background
				deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
						0x3F, 0x25)));
				// set item width
				deleteItem.setWidth(dp2px(90));
				// set a icon
				deleteItem.setIcon(R.drawable.ic_delete);
				// add to menu
				menu.addMenuItem(deleteItem);

			}
		};
		// set creator
		listView.setMenuCreator(creator);

		// step 2. listener item click event
		listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(int position, SwipeMenu menu,
					int index) {
				switch (index) {
				case 0:
					// delete
					// delete(item);
					Cursor cursor = (Cursor) mAdapter.getItem(position);
					String nubmer = cursor.getString(cursor
							.getColumnIndex("ADDRESS"));
					String threadId = cursor.getString(cursor
							.getColumnIndex("THREAD_ID"));

					if (contactExists(MainActivity.this, nubmer)) {

						setReadStatus(position);

					} else {

						getContentResolver().delete(
								Uri.parse("content://sms/conversations/"
										+ threadId), null, null);

						//

					}
					mAdapter.notifyDataSetChanged();
					break;
				}
				return false;
			}
		});

		// set SwipeListener
		listView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {

			@Override
			public void onSwipeStart(int position) {
				// swipe start
				// Toast.makeText(MainActivity.this, "this is swipe",
				// Toast.LENGTH_LONG).show();

			}

			@Override
			public void onSwipeEnd(int position) {
				// swipe end
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Only do these checks/changes on KitKat+, the "mSetDefaultSmsLayout"
		// has its visibility
		// set to "gone" in the xml layout so it won't show at all on earlier
		// Android versions.
	
	}

	@Override
	protected void onStop() {
		super.onStop();
		getContentResolver().unregisterContentObserver(observer);
	}

	private void promptSetDefaultDialog() {
		new AlertDialog.Builder(this)
				.setMessage("Please set Applicatio as default")
				.setPositiveButton("Set",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Utils.setDefaultSmsApp(MainActivity.this);

							}
						}).setNegativeButton("Cancel", null).create().show();
	}

	/**
	 * Dummy sendSms method, would need the "to" address to actually send a
	 * message :)
	 */
	private void sendSms(String smsText) {
		if (!TextUtils.isEmpty(smsText)) {
			if (Utils.isDefaultSmsApp(this)) {
				// TODO: Use SmsManager to send SMS and then record the message
				// in the system SMS
				// ContentProvider
				Toast.makeText(this, "Sending text message: " + smsText,
						Toast.LENGTH_SHORT).show();
			} else {
				// TODO: Notify the user the app is not default and provide a
				// way to trigger
				// Utils.setDefaultSmsApp() so they can set it.
				Toast.makeText(this, "Not default", Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public android.support.v4.content.Loader<Cursor> onCreateLoader(int i,
			Bundle arg1) {
		// TODO Auto-generated method stub
		if (i == SmsQuery.TOKEN) {
			// This will fetch all SMS messages in the inbox, ordered by date
			// desc
			return new CursorLoader(this, SmsQuery.CONTENT_URI,
					SmsQuery.PROJECTION, null, null, SmsQuery.SORT_ORDER);
		}
		return null;
	}

	@Override
	public void onLoadFinished(
			android.support.v4.content.Loader<Cursor> cursorLoader,
			Cursor cursor) {
		if (cursorLoader.getId() == SmsQuery.TOKEN && cursor != null) {
			// Standard swap cursor in when load is done
			mAdapter.swapCursor(cursor);
		}

	}

	@Override
	public void onLoaderReset(android.support.v4.content.Loader<Cursor> arg0) {
		// Standard swap cursor to null when loader is reset
		mAdapter.swapCursor(null);

	}

	/**
	 * A basic SmsQuery on android.provider.Telephony.Sms.Inbox
	 */
	@TargetApi(Build.VERSION_CODES.KITKAT)
	private interface SmsQuery {
		int TOKEN = 1;
		static final Uri CONTENT_URI = Inbox.CONTENT_URI;
		static final String[] PROJECTION = { Inbox._ID, Inbox.ADDRESS,
				Inbox.BODY, Inbox.THREAD_ID, Inbox.READ };
		static final String SORT_ORDER = Inbox.DEFAULT_SORT_ORDER;
		int ID = 0;
		int ADDRESS = 1;
		int BODY = 2;
		int THREAD_ID = 3;
		int READ = 4;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.composeSmsButton:

			Intent intent = new Intent();
			intent.setClass(this, ComposeSMS.class);
			startActivity(intent);
			break;
		default:
			break;
		}

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

		Cursor cursor = (Cursor) mAdapter.getItem(arg2);
		String threadId = cursor.getString(cursor.getColumnIndex("THREAD_ID"));
		String address  = cursor.getString(cursor.getColumnIndex("ADDRESS"));
		
		setReadStatus(arg2);

		Intent intent = new Intent();
		intent.setClass(this, ThreadConversationActivity.class);
		intent.putExtra("threadId", threadId);
		intent.putExtra("ADDRESS", address);
		startActivity(intent);
	}

	private class SMSObserver extends ContentObserver {

		public SMSObserver() {
			super(new Handler());
		}

		public void onChange(boolean selfChange) {
			getSupportLoaderManager().restartLoader(SmsQuery.TOKEN, null,
					MainActivity.this);
		}
	}

	private int dp2px(int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				getResources().getDisplayMetrics());
	}

	public boolean contactExists(Context context, String number) {
		// / number is the phone number
		Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(number));
		String[] mPhoneNumberProjection = { PhoneLookup._ID,
				PhoneLookup.NUMBER, PhoneLookup.DISPLAY_NAME };
		Cursor cur = context.getContentResolver().query(lookupUri,
				mPhoneNumberProjection, null, null, null);
		try {
			if (cur.moveToFirst()) {
				return true;
			}
		} finally {
			if (cur != null)
				cur.close();
		}
		return false;
	}

	void setReadStatus(int position) {

		Cursor cursor = (Cursor) mAdapter.getItem(position);
		String threadId = cursor.getString(cursor.getColumnIndex("THREAD_ID"));
		String from = cursor.getString(cursor.getColumnIndex("ADDRESS"));
		String body = cursor.getString(cursor.getColumnIndex("BODY"));

		Uri uri = Uri.parse("content://sms/inbox");
		String selection = "address = ? AND body = ? AND read = ?";
		String[] selectionArgs = { from, body, "0" };

		ContentValues values = new ContentValues();
		values.put("read", true);

		int rowsUpdated = getContentResolver().update(uri, values, selection,
				selectionArgs);

	}
}
