package poker.tm.activities;

import poker.tm.Helper;
import poker.tm.R;
import poker.tm.model.Tournament;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class TemplatesListActivity extends Activity {
	
	private static final int EDIT_ID   = 0;
	private static final int START_ID  = 1;
	private static final int DELETE_ID = 2;
	private static final int COPY_ID   = 3;
	
	private static final int CREATE_ID   = 4;
	private static final int SETTINGS_ID = 5;
	private static final int ABOUT_ID 	 = 6;
	private static final int GROUPS_ID 	 = 7;
	private static final int EXIT_ID 	 = 8;
	
	private static final int HEADER_ID   = 666;
	private static final int TITLE_ID    = 777;
	private static final int NEW_ID      = 888;
	private static final int FEEDBACK_ID = 999;
	
	
	private ScrollView globalScrollView;
	private LinearLayout scrollableLinearLayout;
	private int position;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Helper.emptyData();
        
        LinearLayout globalLinearLayout = new LinearLayout(this);
		globalLinearLayout.setOrientation(LinearLayout.VERTICAL);
        globalLinearLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        this.setContentView(globalLinearLayout);
        
        globalLinearLayout.addView(getHeader());
        
        globalScrollView = new ScrollView(this);
        //globalScrollView.setBackgroundDrawable(Helper.getBackground(this));
		LayoutParams layout = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		layout.weight = 1;
		globalScrollView.setLayoutParams(layout);
		globalLinearLayout.addView(globalScrollView);
        
        scrollableLinearLayout = new LinearLayout(this);
        scrollableLinearLayout.setOrientation(LinearLayout.VERTICAL);
        scrollableLinearLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        globalScrollView.addView(scrollableLinearLayout);
        
        globalLinearLayout.addView(getFooter());
        //globalLinearLayout.addView(getAds()); TODO
        
        this.refresh();
        
//        if (tournaments.size() > 0) {
//	        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
//		    if (pref.getBoolean("show.feedback.popup", true)) {
//		        AlertDialog.Builder builder = new AlertDialog.Builder(TemplatesListActivity.this);
//				builder.setMessage("Have a question? Notice something wrong ? Got a suggestion? Use the feedback button and tell us!");
//				builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int id) {
//						dialog.cancel();
//					}
//				});
//				builder.show();
//				pref.edit().putBoolean("show.feedback.popup", false).commit();
//	        }
//        }
    }
	
	private void refresh() {
		Helper.refreshTournaments();
		scrollableLinearLayout.removeAllViews();
		
		if (Helper.getTournaments(this).size() > 0) {
//			scrollableLinearLayout.addView(getFeedback());
			for (int i = 0; i < Helper.getTournaments(this).size(); i++) {
	        	scrollableLinearLayout.addView(getTournamentView(i));
			}
			
		} else {
			View emptyView = LayoutInflater.from(this).inflate(R.layout.empty_home, null);
			scrollableLinearLayout.addView(emptyView);
		}
	}
	
//	private View getFeedback() {
//		TextView link = new TextView(this);
//		link.setText("Send feedback");
//		link.setHeight((int) (40 * getResources().getDisplayMetrics().density));
//		link.setGravity(Gravity.CENTER);
//		link.setTextColor(Color.WHITE);
//		link.setTypeface(Typeface.create("droid", Typeface.NORMAL));
//		link.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
//		link.setBackgroundColor(Color.rgb(255, 106, 0));
//		link.setOnClickListener(new OnClickListener() {			
//			public void onClick(View v) {
//				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://groups.google.com/forum/m/#!forum/poker-director")); 
//				startActivity(i); 	
//			}
//		});
//		return link;
//	}
	
	private View getAds() {
		AdView adView = new AdView(this, AdSize.BANNER, "a14d8380b359657");
		adView.loadAd(new AdRequest());
		
		return adView;
	}
	
	private View getHeader() {	
		ImageView icon1 = new ImageView(this);
		icon1.setImageResource(R.drawable.logo1);
		
		TextView title = new TextView(this);
		title.setText(R.string.app_name);
		title.setGravity(Gravity.CENTER);
		LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lp.gravity = Gravity.CENTER;
		title.setLayoutParams(lp);
		//title.setPadding(0, (int) (5 * getResources().getDisplayMetrics().density), 0, 0);
		title.setTypeface(Typeface.create("droid", Typeface.NORMAL));
		title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);//TODO 24
		title.setTextColor(Color.WHITE); 
		
		ImageView icon2 = new ImageView(this);
		lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lp.gravity = Gravity.TOP;
		icon2.setLayoutParams(lp);
		icon2.setImageResource(R.drawable.logo2);
		
		LinearLayout header = new LinearLayout(this);
		header.setOrientation(LinearLayout.HORIZONTAL);
		header.setId(HEADER_ID);
		header.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
		BitmapDrawable bkg = (BitmapDrawable) this.getResources().getDrawable(R.drawable.title);
		bkg.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);	
		header.setBackgroundDrawable(bkg);
		header.setMinimumHeight((int) (50 * getResources().getDisplayMetrics().density));
		header.addView(icon1);
		header.addView(title);
		header.addView(icon2);
		
		return header;
	}
	
	private View getFooter() {
		LinearLayout toolbar = new LinearLayout(this);
		toolbar.setGravity(Gravity.CENTER_VERTICAL);
		toolbar.setBackgroundResource(R.drawable.toolbar);
		toolbar.setMinimumHeight((int) (45 * getResources().getDisplayMetrics().density));
		toolbar.setPadding((int) (5 * getResources().getDisplayMetrics().density), 0, (int) (5 * getResources().getDisplayMetrics().density), 0);
		
		Button create = new Button(this);
		create.setId(NEW_ID);
		create.setTextColor(Color.WHITE);
		create.setBackgroundResource(R.drawable.toolbar_button);
		create.setMinimumHeight((int) (35 * getResources().getDisplayMetrics().density));
		create.setText("Add tournament");
		toolbar.addView(create);
		create.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				addTournament();
			}
		});
		
		TextView title = new TextView(this);
		title.setSingleLine();
		title.setId(TITLE_ID);
		title.setText(R.string.app_name);
		title.setGravity(Gravity.CENTER);
		title.setTypeface(Typeface.create((String)null, Typeface.BOLD));
		title.setTextColor(Color.WHITE);
		LayoutParams layout = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		layout.weight = 1;
		title.setLayoutParams(layout);
		toolbar.addView(title);
		
		Button feedback = new Button(this);
		feedback.setId(FEEDBACK_ID);
		feedback.setTextColor(Color.WHITE);
		feedback.setBackgroundResource(R.drawable.toolbar_button);
		feedback.setMinimumHeight((int) (35 * getResources().getDisplayMetrics().density));
		layout = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		layout.setMargins(0, 0, (int) (5 * getResources().getDisplayMetrics().density), 0);
		feedback.setLayoutParams(layout);
		feedback.setText("Feedback");
		toolbar.addView(feedback);
		feedback.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				goToFeedback();
			}
		});
		
		Button settings = new Button(this);
		settings.setTextColor(Color.WHITE);
		settings.setBackgroundResource(R.drawable.toolbar_button);
		settings.setMinimumHeight((int) (35 * getResources().getDisplayMetrics().density));
		layout = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		layout.setMargins(0, 0, (int) (5 * getResources().getDisplayMetrics().density), 0);
		settings.setLayoutParams(layout);
		settings.setText("Settings");
		toolbar.addView(settings);
		settings.setOnClickListener(new OnClickListener() {					
			public void onClick(View v) {
				goToSettings();
			}
		});
		
//		Button exit = new Button(this);
//		exit.setTextColor(Color.WHITE);
//		exit.setBackgroundResource(R.drawable.toolbar_button);
//		exit.setMinimumHeight((int) (35 * getResources().getDisplayMetrics().density));
//		exit.setText("X");
//		toolbar.addView(exit);
//		exit.setOnClickListener(new OnClickListener() {					
//			public void onClick(View v) {
//				exitApp();
//			}
//		});
		
		return toolbar;
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			this.findViewById(HEADER_ID).setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, 0));
			this.findViewById(TITLE_ID).setVisibility(View.VISIBLE);
		} else {
			this.findViewById(HEADER_ID).setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			this.findViewById(TITLE_ID).setVisibility(View.INVISIBLE);
		}
		
		if (Helper.getBackground() == null) {
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
			Helper.setBackground(pref.getString("background", "black"), this);
		}
		
		globalScrollView.setBackgroundDrawable(Helper.getBackground());
		this.refresh();
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getTag() != null) {
			position = (Integer) v.getTag();
		}
		
		menu.setHeaderTitle(Helper.getTournaments(this).get(position).getName());		
		menu.add(0, EDIT_ID,   0, "Edit tournament");
		menu.add(0, START_ID,  0, "Start tournament");
		menu.add(0, COPY_ID,   0, "Copy tournament");
		menu.add(0, DELETE_ID, 0, "Delete tournament");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {		
		switch (item.getItemId()) {
			case EDIT_ID:
				editTournament(position);
				return true;
			
			case START_ID:
				startTournament(position);
				return true;
				
			case COPY_ID:
				copyTournament(position);
				return true;
				
			case DELETE_ID:
				deleteTournament(position);
				return true;
				
			default:
				return super.onContextItemSelected(item);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuItem item = menu.add(0, CREATE_ID, 0, "Add a new tournament");
		item.setIcon(android.R.drawable.ic_menu_add);
		item = menu.add(0, SETTINGS_ID, 1, "Settings");
		item.setIcon(android.R.drawable.ic_menu_preferences);
		item = menu.add(0, GROUPS_ID, 2, "Feedback");
		item.setIcon(R.drawable.feedback);
		item = menu.add(0, ABOUT_ID, 3, "About");
		item.setIcon(android.R.drawable.ic_menu_info_details);
//		item = menu.add(0, EXIT_ID, 4, "Exit");
//		item.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case CREATE_ID:				
				this.addTournament();
				return true;
				
			case SETTINGS_ID:
				this.goToSettings();
				return true;
				
			case GROUPS_ID:
				this.goToFeedback();
				return true;
				
			case ABOUT_ID:
				this.goToAbout();
				return true;
				
			case EXIT_ID:
				this.exitApp();
				return true;
				
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private void exitApp() {
		AlertDialog.Builder builder = new AlertDialog.Builder(TemplatesListActivity.this);
		builder.setMessage("Are you sure you want to quit ?");
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
				finish();
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		builder.show();
	}
	
	private void goToAbout() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String version = "?";
		try {
			version = this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			// fuck this shit
		}
		builder.setMessage(getResources().getString(R.string.app_name) + "\nversion " + version + "\n\n" + getResources().getString(R.string.legal_bullshit));
		builder.setNeutralButton("ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	private void goToSettings() {
		Intent intent = new Intent(this, SettingsActivity.class);
		this.startActivity(intent);
	}
	
	private void goToFeedback() {
		Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://groups.google.com/forum/m/#!forum/poker-director")); 
		startActivity(i); 
	}
	
	private void addTournament() {
		Intent intent = new Intent(this, NewTournamentActivity.class);
		this.startActivity(intent);
	}

	private void deleteTournament(final int pos) {
		AlertDialog.Builder builder = new AlertDialog.Builder(TemplatesListActivity.this);
		builder.setMessage("Do you really want to delete this tournament ?");
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Tournament t = Helper.getTournaments(TemplatesListActivity.this).get(pos);		
				if (Helper.getDatabase(TemplatesListActivity.this).deleteTournament(t.getId())) {
					refresh();
				}
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		builder.show();
	} 

	private void copyTournament(int pos) {
		Tournament selectedTournament = Helper.getTournaments(this).get(pos);
		int[] chipsValues = Helper.getDatabase(this).fetchAllChipsValue(selectedTournament.getId());
		int[] chipsColor = Helper.getDatabase(this).fetchAllChipsColor(selectedTournament.getId());
		
		selectedTournament.setName("copy of " + selectedTournament.getName());
		long id = Helper.getDatabase(this).insertTournament(selectedTournament);
		selectedTournament.setId((int) id);
		
		Helper.getDatabase(this).updateTournamentTablesAndSeats(selectedTournament);
		Helper.getDatabase(this).setPlayers(id, selectedTournament.getPlayers());
		Helper.getDatabase(this).setBuyInAndAddon(id, selectedTournament.getBuyin(), selectedTournament.getAddon(), selectedTournament.getNbRebuy(), selectedTournament.getRebuy());
		Helper.getDatabase(this).setPrizepool(id, selectedTournament.getPrizepool());
		Helper.getDatabase(this).updateTournamentChipsOptions(selectedTournament);
		for (int i = 0; i < 12; i++) {
			Helper.getDatabase(this).updateChips(id, i, chipsValues[i], chipsColor[i]);
		}
		
		refresh();		
		editTournament(Helper.getTournaments(this).size() - 1);
	}

	private void editTournament(int pos) {
		if (Helper.setSelectedTournament(this, pos)) {		
			Intent intent = new Intent(this, EditTemplateActivity.class);
			this.startActivity(intent);
			
		} else {
			 displayErrorOpeningTournament();
		 }
	}

	private void startTournament(int pos) {
		 if (Helper.setSelectedTournament(this, pos)) {	
			Intent intent = new Intent(this, RunningTournamentActivity.class);
			this.startActivity(intent);
			
		 } else {
			 displayErrorOpeningTournament();
		 }
	}
	
	private void displayErrorOpeningTournament() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("There was an error while opening the tournament.");
		builder.setNeutralButton("close", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	public View getTournamentView(final int position) {
		View view = LayoutInflater.from(this).inflate(R.layout.list_item, null);
		view.setTag(position);
		this.registerForContextMenu(view);
		view.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				editTournament(position);
			}
		});
		
		TextView label = (TextView) view.findViewById(R.id.list_item1);
		label.setText(Helper.getTournaments(this).get(position).getName()); 
		
		TextView tw = (TextView) view.findViewById(R.id.list_item2);
		tw.setText(Helper.getTournaments(this).get(position).getDescription());
		
		View button = view.findViewById(R.id.list_button);
		button.setOnClickListener(new OnClickListener() {					
			public void onClick(View v) { 
				TemplatesListActivity.this.position = position;
				startTournament(position);						
			}
		});
		
		return view;
	}
	
}
