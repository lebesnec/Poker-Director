package poker.tm.activities;

import poker.tm.Helper;
import poker.tm.R;
import poker.tm.model.AddonOrRebuy;
import poker.tm.model.Break;
import poker.tm.model.Round;
import poker.tm.model.Tournament;
import poker.tm.model.TournamentStep;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils.TruncateAt;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class EditTemplateActivity extends Activity {
	
	private static final int DELETE_ID  	 = 0;
	private static final int ADD_ROUND_ID	 = 1;
	private static final int ADD_BREAK_ID	 = 2;
	private static final int ADD_ADDON_ID	 = 3;
	private static final int SET_DURATION_ID = 7;
	
	private static final int DELETE_TOURNAMENT_ID  = 4;
	private static final int START_TOURNAMENT_ID   = 5;
	private static final int OPTIONS_ID   		   = 6;
	
//	private static final int FIRST = -1;
	private static final int LAST  = -2;
	private static final int BUYIN_REQUEST = 123457;


	private Tournament tournament;
	private int position;
	private ScrollView globalScrollView;
	private LinearLayout toolbar;
	private LinearLayout header;
	private LinearLayout listFooter;
	private LinearLayout scrollableLinearLayout;
	private TextView name;
	private BlindFormatFocusListener smallBlindfocusListener;
	private BlindFormatFocusListener bigBlindfocusListener;
	private BlindFormatFocusListener antesfocusListener;
	private DurationFormatFocusListener durationfocusListener;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tournament = Helper.getSelectedTournament(this);
		this.smallBlindfocusListener = new BlindFormatFocusListener();
		this.smallBlindfocusListener.smallBlind = true;
		this.bigBlindfocusListener = new BlindFormatFocusListener();
		this.bigBlindfocusListener.bigBlind = true;
		this.antesfocusListener = new BlindFormatFocusListener();
		this.antesfocusListener.antes = true;
		this.durationfocusListener = new DurationFormatFocusListener();
		
		LinearLayout globalLinearLayout = new LinearLayout(this);
		globalLinearLayout.setFocusable(true);
		globalLinearLayout.setFocusableInTouchMode(true);		
		globalLinearLayout.setOrientation(LinearLayout.VERTICAL);
        globalLinearLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        this.setContentView(globalLinearLayout);

        globalLinearLayout.addView(getHeader());
		
		globalScrollView = new ScrollView(this);
		LayoutParams layout = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		layout.weight = 1;
		globalScrollView.setLayoutParams(layout);
        globalLinearLayout.addView(globalScrollView);
        
        scrollableLinearLayout = new LinearLayout(this);
        scrollableLinearLayout.setOrientation(LinearLayout.VERTICAL);
        scrollableLinearLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        globalScrollView.addView(scrollableLinearLayout);
        
        globalLinearLayout.addView(getToolbar());
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		tournament = Helper.getSelectedTournament(this);
		name.setText(tournament.getName());
		globalScrollView.setBackgroundDrawable(Helper.getBackground());
		this.refresh();
	}
	
	@Override
	protected void onPause() {
		if (this.getCurrentFocus() != null) {
			View focus = this.getCurrentFocus().focusSearch(View.FOCUS_LEFT);
			if (focus != null) {
				focus.requestFocus();
			}
		}
		Helper.getDatabase(this).updateTournament(tournament);
		
		super.onPause();
	}
	
	private LinearLayout getHeader() { 
		if (header == null) {
			header = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.step_list_header, null);
		}
		
		return header;
	}
	
	private LinearLayout getListFooter() {
		if (listFooter == null) {
			listFooter = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.step_list_footer, null);
			registerForContextMenu(listFooter);
			
			TextView tw = (TextView) listFooter.findViewById(R.id.list_item_text);
			tw.setText("Add a new step");
	        
			listFooter.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View v) {
	            	position = LAST;
	            	listFooter.setTag(position);
					openContextMenu(listFooter);
	            }
	        });
		}
		
		return listFooter;
	}
	
	private LinearLayout getToolbar() {
		if (toolbar == null) {
			toolbar = new LinearLayout(this);
			toolbar.setGravity(Gravity.CENTER_VERTICAL);
			toolbar.setBackgroundResource(R.drawable.toolbar);
			toolbar.setMinimumHeight((int) (45 * getResources().getDisplayMetrics().density));
			
			TextView filler = new TextView(this);
			filler.setText("");
			filler.setMinWidth((int) (5 * getResources().getDisplayMetrics().density));
			toolbar.addView(filler);
			
			Button prev = new Button(this); 
			prev.setTextColor(Color.WHITE);
			prev.setBackgroundResource(R.drawable.toolbar_button);
			prev.setMinWidth((int) (55 * getResources().getDisplayMetrics().density));
			prev.setMinimumHeight((int) (35 * getResources().getDisplayMetrics().density));
			prev.setText("done");
			toolbar.addView(prev);
			prev.setOnClickListener(new OnClickListener() {					
				public void onClick(View v) {
					EditTemplateActivity.this.finish();
				}
			});
			
			name = new TextView(this);
			name.setGravity(Gravity.CENTER);
			name.setSingleLine();
			name.setPadding(5, 2, 0, 0);
			name.setBackgroundColor(Color.TRANSPARENT);
			name.setTextColor(Color.WHITE);
			name.setTextSize(13);
			name.setTypeface(Typeface.create((String)null, Typeface.BOLD));
			name.setText(Helper.getSelectedTournament(this).getName());
			name.setEllipsize(TruncateAt.END);
			LayoutParams layout = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			layout.weight = 1;
			name.setLayoutParams(layout);
			toolbar.addView(name);	
			
			Button settings = new Button(this);
			settings.setTextColor(Color.WHITE);
			settings.setBackgroundResource(R.drawable.toolbar_button);
			settings.setMinWidth((int) (55 * getResources().getDisplayMetrics().density));
			settings.setMinimumHeight((int) (35 * getResources().getDisplayMetrics().density));
			settings.setText("settings");
			toolbar.addView(settings);
			settings.setOnClickListener(new OnClickListener() {					
				public void onClick(View v) {
					Intent intent = new Intent(EditTemplateActivity.this, TemplateOptionsActivity.class);
					startActivity(intent);
				}
			});
			
			filler = new TextView(this);
			filler.setText("");
			filler.setMinWidth((int) (5 * getResources().getDisplayMetrics().density));
			toolbar.addView(filler);
			
			Button start = new Button(this);
			start.setTextColor(Color.WHITE);
			start.setBackgroundResource(R.drawable.toolbar_button);
			start.setMinWidth((int) (55 * getResources().getDisplayMetrics().density));
			start.setMinimumHeight((int) (35 * getResources().getDisplayMetrics().density));
			start.setText("start");
			toolbar.addView(start);
			start.setOnClickListener(new OnClickListener() {					
				public void onClick(View v) {
					startTournament();
				}
			});
			
			filler = new TextView(this);
			filler.setText("");
			filler.setMinWidth((int) (5 * getResources().getDisplayMetrics().density));
			toolbar.addView(filler);
		}
		
		return toolbar;
	}
	
	private void refresh() {
		scrollableLinearLayout.removeAllViews();
		
		for (int i = 0; i < tournament.getSteps().size(); i++) {
        	scrollableLinearLayout.addView(getStepView(i));
		}
		
		scrollableLinearLayout.addView(getListFooter());
	}
	
	private void startTournament() {
		finish();
		Intent intent = new Intent(EditTemplateActivity.this, RunningTournamentActivity.class);
		startActivity(intent);
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BUYIN_REQUEST && resultCode == RESULT_OK) {
        	Intent intent = new Intent(EditTemplateActivity.this, RunningTournamentActivity.class);
			startActivity(intent);
        }
    }
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getTag() != null) {
			position = (Integer) v.getTag();
		}
		
		if (position == LAST) {
			menu.setHeaderTitle("Step " + (tournament.getSteps().size() + 1));
			menu.add(0, ADD_ROUND_ID, 0, "Add a round");
			menu.add(0, ADD_BREAK_ID, 0, "Add a break");
			menu.add(0, ADD_ADDON_ID, 0, "Add an addon");
			
		} else {			
			TournamentStep step = tournament.getSteps().get(position);
			menu.setHeaderTitle("Step " + (position + 1) + " : " + step.getDescription());			
			menu.add(0, DELETE_ID,    0, "Delete");
			menu.add(0, ADD_ROUND_ID, 0, "Insert a round");
			menu.add(0, ADD_BREAK_ID, 0, "Insert a break");
			menu.add(0, ADD_ADDON_ID, 0, "Insert an addon");
			if (step.getDuration() >= 0) {
				menu.add(0, SET_DURATION_ID, 0, "Set all next durations to " + Helper.formatDurationMinutes(step.getDuration()));
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {		
		switch (item.getItemId()) {
			case DELETE_ID:
				this.deleteStep(position);
				return true;
			
			case ADD_ROUND_ID:
				this.addRound(position);
				return true;
				
			case ADD_BREAK_ID:
				this.addBreak(position);
				return true;
				
			case ADD_ADDON_ID:
				this.addAddon(position);
				return true;
				
			case SET_DURATION_ID:
				this.setDurations(position);
				return true;
				 
			default:
				return super.onContextItemSelected(item);
		}
	}
	
	private void setDurations(int pos) {
		TournamentStep step = tournament.getSteps().get(pos);
		for (int i = pos + 1; i < tournament.getSteps().size(); i++) {
			TournamentStep next = tournament.getSteps().get(i);
			if (next instanceof Round) {
				Round r = (Round) next;
				r.setDuration(step.getDuration());
			} else if (next instanceof Break) {
				Break b = (Break) next;
				b.setDuration(step.getDuration());
			}
		}
		refresh();
	}

	private void deleteStep(int pos) {
		TournamentStep step = tournament.getSteps().get(pos);		
		if (Helper.getDatabase(this).deleteStep(step.getId())) {
			tournament.getSteps().remove(pos);
		}
		this.refresh();
	}
	
	private void addRound(int pos) {
		if (pos == LAST) {
			tournament.addRound();
			
		} else {
			Round previous = null;
			for (int i = pos; i >= 0; i--) {
				TournamentStep step = tournament.getSteps().get(i);
				if (step instanceof Round) {
					previous = (Round) step;
					break;
				}
			}
			
			Round next = null;
			for (int i = pos + 1; i < tournament.getSteps().size(); i++) {
				TournamentStep step = tournament.getSteps().get(i);
				if (step instanceof Round) {
					next = (Round) step;
					break;
				}
			}
			
			Round r = null;
			if (previous == null) {
				r = new Round();
			} else if (next == null) {
				r = new Round(previous);
			} else {
				r = new Round(previous);
				if (r.getBigBlind() >= next.getBigBlind()) {
					r = previous.clone();
				}
			}
			tournament.getSteps().add(pos + 1, r);
		}
		
		Helper.getDatabase(this).updateTournament(tournament);
		this.refresh();
	}

	private void addBreak(int pos) {
		if (pos == LAST) {
			pos = tournament.getSteps().size() - 1;
		}
		Break breaks = new Break();
		tournament.getSteps().add(pos + 1, breaks);
		Helper.getDatabase(this).updateTournament(tournament);
		this.refresh();
	}

	private void addAddon(int pos) {
		if (pos == LAST) {
			pos = tournament.getSteps().size() - 1;
		}
		AddonOrRebuy addon = new AddonOrRebuy();
		tournament.getSteps().add(pos + 1, addon);
		Helper.getDatabase(this).updateTournament(tournament);
		this.refresh();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuItem item = menu.add(0, OPTIONS_ID, 0, "Settings");
		item.setIcon(android.R.drawable.ic_menu_preferences);
		item = menu.add(0, DELETE_TOURNAMENT_ID, 1, "Delete");
		item.setIcon(android.R.drawable.ic_menu_delete);
		item = menu.add(0, START_TOURNAMENT_ID, 2, "Start");
		item.setIcon(R.drawable.play_menu);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case DELETE_TOURNAMENT_ID:				
				deleteTournament();
				return true;
				
			case OPTIONS_ID:
				Intent intent = new Intent(this, TemplateOptionsActivity.class);
				startActivity(intent);
				return true;
				
			case START_TOURNAMENT_ID:
				startTournament();
				return true;
				
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private void deleteTournament() {
		AlertDialog.Builder builder = new AlertDialog.Builder(EditTemplateActivity.this);
		builder.setMessage("Do you really want to delete this tournament ?");
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				if (Helper.getDatabase(EditTemplateActivity.this).deleteTournament(tournament.getId())) {
					finish();
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

	public View getStepView(final int pos) {
		TournamentStep step = tournament.getSteps().get(pos);
		View convertView = null;

		if (step instanceof Round) {
			convertView = LayoutInflater.from(this).inflate(R.layout.step_list_item, null);
			convertView.setTag(pos);
			this.registerForContextMenu(convertView);
			Round round = (Round) step;
			EditText ed = (EditText) convertView.findViewById(R.id.list_item_sb);
			ed.setTag(round);
			ed.setText(Helper.formatBlind(round.getSmallBlind()));
			ed.setOnFocusChangeListener(smallBlindfocusListener);
			
			ed = (EditText) convertView.findViewById(R.id.list_item_bb);
			ed.setTag(round);
			ed.setText(Helper.formatBlind(round.getBigBlind()));
			ed.setOnFocusChangeListener(bigBlindfocusListener);
			
			ed = (EditText) convertView.findViewById(R.id.list_item_antes);
			ed.setTag(round);
			ed.setText(Helper.formatBlind(round.getAntes()));
			ed.setOnFocusChangeListener(antesfocusListener);
			
			ed = (EditText) convertView.findViewById(R.id.list_item_duration);
			ed.setTag(round);
			ed.setText(Helper.formatDurationMinutes(round.getDuration()));
			ed.setOnFocusChangeListener(durationfocusListener);

		} else if (step instanceof Break) {
			convertView = LayoutInflater.from(this).inflate(R.layout.step_list_item2, null);
			convertView.setTag(pos);
			this.registerForContextMenu(convertView);
			ImageView iw = (ImageView) convertView.findViewById(R.id.list_item_icon);
			iw.setImageResource(R.drawable.watch);
			
			TextView tw = (TextView) convertView.findViewById(R.id.list_item_text);
			tw.setText("Break");
			
			EditText ed = (EditText) convertView.findViewById(R.id.list_item_duration);
			ed.setTag(step);
			ed.setText(Helper.formatDurationMinutes(step.getDuration()));
			ed.setOnFocusChangeListener(durationfocusListener);

		} else if (step instanceof AddonOrRebuy) {
			convertView = LayoutInflater.from(this).inflate(R.layout.step_list_item2, null);
			convertView.setTag(pos);
			this.registerForContextMenu(convertView);
			ImageView iw = (ImageView) convertView.findViewById(R.id.list_item_icon);
			iw.setImageResource(R.drawable.bank);
			
			TextView tw = (TextView) convertView.findViewById(R.id.list_item_text);
			tw.setText("Addon");
			
			EditText ed = (EditText) convertView.findViewById(R.id.list_item_duration);
//			ed.setTag(step);
//			ed.setText(Helper.formatDurationMinutes(step.getDuration()));
//			ed.setOnFocusChangeListener(durationfocusListener);
			ed.setVisibility(View.INVISIBLE);
		}

		TextView tw = (TextView) convertView.findViewById(R.id.list_item_pos);
		tw.setText((pos + 1) + ""); 
		
		final View button = convertView.findViewById(R.id.list_step_button);
		button.setOnClickListener(new OnClickListener() {					
			public void onClick(View v) {
				EditTemplateActivity.this.position = pos;
				button.setTag(pos);
				openContextMenu(button);
			}
		});
		
		return convertView;
	}
	
	
	private class BlindFormatFocusListener implements OnFocusChangeListener {
		
		private boolean smallBlind = false;
		private boolean bigBlind = false;
		private boolean antes = false;
		
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				TextView t = (EditText) v;
				String val = t.getText().toString();
				t.setText(val.replaceFirst("K", "000"));
			
			} else { 
				TextView t = (EditText) v;
				String s = t.getText().toString();
				s = s.equals("") ? "0" : s;
				int val = Integer.parseInt(s);
				t.setText(Helper.formatBlind(val));
				
				Round r = (Round) v.getTag(); 
				if (smallBlind) {
					r.setSmallBlind(val);
				} else if (bigBlind) {
					r.setBigBlind(val);
				} else if (antes) {
					r.setAntes(val);
				}
				
				if (smallBlind && r.getSmallBlind() >= r.getBigBlind()) {
					r.setBigBlind(r.getSmallBlind() * 2);
					refresh();
					
				} else if (bigBlind && r.getBigBlind() < r.getSmallBlind()) {
					r.setSmallBlind(r.getBigBlind() / 2);
					refresh();
				}
			}
		}
		
	}
	
	private class DurationFormatFocusListener implements OnFocusChangeListener {
		
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				TextView t = (EditText) v;
				String val = t.getText().toString();
				if (!val.equals("||")) {
					t.setText(val.substring(0, val.length() - 1));
				} else {
					t.setText("");
				}
			
			} else {
				TextView t = (EditText) v;
				String val = t.getText().toString();
				int duration = 0;
				if (!val.equals("")) {
					t.setText(val + "m");
					duration = Integer.parseInt(val) * 60;
				} else {
					t.setText("||");
					duration = -1;
				}
				
				if (v.getTag() instanceof Round) {
					Round r = (Round) v.getTag();
					r.setDuration(duration);
				} else if (v.getTag() instanceof Break) {
					Break b = (Break) v.getTag();
					b.setDuration(duration);
				}
			}
		}
		
	}

}
