package poker.tm.activities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import poker.tm.Helper;
import poker.tm.R;
import poker.tm.model.Player;
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.QuickContactBadge;
import android.widget.TextView;

public class TablesListActivity extends Activity implements TextWatcher {	
	
	private static final int CLEAR_ALL = 1;
	private static final int RANDOMIZE = 2;
	
	
	private List<List<Player>> tables = new ArrayList<List<Player>>();
	private LinearLayout list;
	private EditText nbTablesText;
	private EditText nbSeatsText;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);		
		this.setContentView(R.layout.tables_list);		
		list = (LinearLayout) this.findViewById(R.id.tablesListView);
		
		nbTablesText = (EditText) this.findViewById(R.id.nbTablesText);
		nbTablesText.setText(Helper.getSelectedTournament(this).getNbTable() + "");
		nbTablesText.addTextChangedListener(this);
		
		nbSeatsText = (EditText) this.findViewById(R.id.nbPlayersText);
		nbSeatsText.setText(Helper.getSelectedTournament(this).getNbSeat() + "");
		nbSeatsText.addTextChangedListener(this);
		
		Button randomizeButton = (Button) this.findViewById(R.id.randomize);
		randomizeButton.setOnClickListener(new OnClickListener() {					
			public void onClick(View v) {
				seatAllPlayers(); 
			}
		});
		
		Button doneButton = (Button) this.findViewById(R.id.tablesButton);
		doneButton.setOnClickListener(new OnClickListener() {					
			public void onClick(View v) {
				finish();
			}
		});
		
		reload();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		this.findViewById(R.id.tablesMainlayout).setBackgroundDrawable(Helper.getBackground());
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		Helper.getDatabase(this).updateTournamentTablesAndSeats(Helper.getSelectedTournament(this));
		int tournamentId = Helper.getSelectedTournament(this).getId();
		Helper.getDatabase(this).setPlayers(tournamentId, Helper.getSelectedTournament(this).getPlayers());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuItem item = menu.add(0, RANDOMIZE, 0, "Randomize");
		item.setIcon(R.drawable.dice);
		item = menu.add(0, CLEAR_ALL, 1, "Clear all");
		item.setIcon(R.drawable.delete);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case RANDOMIZE:				
				seatAllPlayers(); 
				return true;
				
			case CLEAR_ALL:
				Helper.getSelectedTournament(this).unseatAllPlayers();
				reload();
				return true;
				
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private void reload() {
		int nbTables = Helper.getSelectedTournament(this).getNbTable();
		int nbSeats = Helper.getSelectedTournament(this).getNbSeat();
		
		tables.clear();
		list.removeAllViews();
		
		if (nbTables * nbSeats < 100) {
			for (int i = 0; i < nbTables; i++) {
				List<Player> seats = new ArrayList<Player>(nbSeats);
				tables.add(seats);
				for (int j = 0; j < nbSeats; j++) {
					seats.add(Helper.getSelectedTournament(this).getPlayer(i, j));
				}
			}
		}
		
		refreshSummary();
		
		if (nbTables * nbSeats < 100 && nbTables * nbSeats > 0) {
			int i = 0;
			for (List<Player> table : tables) {
				this.addTable(i, table);
				i ++;
			}
		}
	}
	
	private void seatAllPlayers() {
		if (!nbTablesText.getText().toString().equals("") && !nbSeatsText.getText().toString().equals("")) {
			List<Player> players = new ArrayList<Player>(Helper.getSelectedTournament(this).getPlayers());
			Collections.shuffle(players);
			int nbTables = Integer.parseInt(nbTablesText.getText().toString());
			int nbSeats = Integer.parseInt(nbSeatsText.getText().toString());
			Helper.getSelectedTournament(this).setNbTable(nbTables);
			Helper.getSelectedTournament(this).setNbSeat(nbSeats);
			
			Helper.getSelectedTournament(this).unseatAllPlayers();
			tables.clear();
			list.removeAllViews();
			
			if (nbTables * nbSeats < 100) {
				for (int i = 0; i < nbTables; i++) {
					List<Player> seats = new ArrayList<Player>(nbSeats);
					tables.add(seats);
				}
				int i = 0, j = 0;
				for (Player player : players) {
					tables.get(i).add(player);
					player.setTable(i);
					player.setSeat(j);
					i ++;
					if (i >= nbTables) {
						i = 0;
						j ++;
						if (j >= nbSeats) {
							break;
						}
					}
				}
				for (i = 0; i < nbTables; i++) {
					List<Player> seats = tables.get(i);
					for (j = seats.size(); j < nbSeats; j++) {
						seats.add(null);
					}
				}
			}
			
			refreshSummary();
			
			if (nbTables * nbSeats < 100 && nbTables * nbSeats > 0) {
				int i = 0;
				for (List<Player> table : tables) {
					this.addTable(i, table);
					i ++;
				}
			}
		}
	}
	
	private void refreshSummary() {
		if (!nbTablesText.getText().toString().equals("") && !nbSeatsText.getText().toString().equals("")) {
			List<Player> players = Helper.getSelectedTournament(this).getPlayers();
			int nbTables = Integer.parseInt(nbTablesText.getText().toString());
			int nbSeats = Integer.parseInt(nbSeatsText.getText().toString());			
			int total = nbTables * nbSeats;
			int nbSeatedPlayers = Helper.getSelectedTournament(this).getSeatedPlayer();
			
			TextView summary = (TextView) findViewById(R.id.summaryText);
			
			if (total > 100) {
				summary.setText("Please try with less than 100 players thx.");
				summary.setTextColor(Color.RED);
				
			} else if (total > 0) {
				summary.setText(total + " seats, " + (total - nbSeatedPlayers) + " empty, " + nbSeatedPlayers + "/" + players.size() + " players seated");
				if (nbSeatedPlayers == players.size()) {
					summary.setTextColor(Color.GREEN);
				} else {
					summary.setTextColor(Color.RED);
				}
				
			} else {
				summary.setText("0 seats");
				summary.setTextColor(Color.RED);
			}
		}
	}
	
	private void addTable(int table, List<Player> seats) {
		TextView title = new TextView(this);
		title.setTypeface(Typeface.create("droid", Typeface.NORMAL));
		title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 19);
		title.setTextColor(Color.WHITE); 
		title.setText("Table " + (table + 1));
		title.setPadding(0, 3, 0, 3);
		title.setGravity(Gravity.CENTER);
		BitmapDrawable bkg = (BitmapDrawable) this.getResources().getDrawable(R.drawable.table_header);
		bkg.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);	
		title.setBackgroundDrawable(bkg);
		list.addView(title);
		
		int seat = 0;
		for (Player player : seats) {
			this.addSeat(table, seat, player);
			seat ++;
		}
	}
    
    private void addSeat(final int table, final int seat, final Player player) {
    	final View v = LayoutInflater.from(this).inflate(R.layout.table_player_item, null);
    	this.displayPlayer(v, table, seat, player);
    	list.addView(v);    	
    }
    
    private void displayPlayer(final View v, final int table, final int seat, final Player player) {
    	final TextView name = (TextView) v.findViewById(R.id.playerName);
        TextView place = (TextView) v.findViewById(R.id.playerPlace);
        place.setText((seat + 1) + "");
        final QuickContactBadge badge =  (QuickContactBadge) v.findViewById(R.id.playerBadge);
        final View remove = v.findViewById(R.id.list_button);
        final View divider = v.findViewById(R.id.divider);
        
        if (player != null) {
	        name.setText(player.getDisplayName());
	        
        	badge.setVisibility(View.VISIBLE);
	        if (player.getPhoto() != null) {
	        	badge.setImageBitmap(player.getPhoto());
	        } else {
	        	badge.setImageResource(R.drawable.contact_picture);
	        }
	        
	        divider.setVisibility(View.VISIBLE);
	        remove.setVisibility(View.VISIBLE);
	        remove.setOnClickListener(new OnClickListener() {					
				public void onClick(View theButton) {
					player.setTable(-1);
					player.setSeat(-1);
					refreshSummary();
					name.setText("empty");
		        	remove.setVisibility(View.INVISIBLE);
		        	badge.setVisibility(View.INVISIBLE);
		        	divider.setVisibility(View.INVISIBLE);
		        	v.setOnClickListener(new OnClickListener() {					
		    			public void onClick(View view) {
		    				openPlayerList(v, table, seat, null);
		    			}
		    		});
				}
			});
	        
        } else {
        	name.setText("empty");
        	remove.setVisibility(View.INVISIBLE);
        	badge.setVisibility(View.INVISIBLE);
        	divider.setVisibility(View.INVISIBLE);
        }
        
        v.setOnClickListener(new OnClickListener() {					
			public void onClick(View view) {
				openPlayerList(v, table, seat, player);
			}
		});
    }
    
    private void openPlayerList(final View currentView, final int table, final int seat, final Player currentPlayer) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final AlertDialog dialog = builder.create();		
		View v = LayoutInflater.from(this).inflate(R.layout.players_seat_select_layout, null);
		dialog.setView(v);
		dialog.setTitle("table " + (table + 1) + " seat " + (seat + 1));		
		
		boolean noRemainingPlayers = true;
		LinearLayout list = (LinearLayout) v.findViewById(R.id.playersSeatListView);		
		for (final Player player : Helper.getSelectedTournament(this).getPlayers()) {
			if (player.getTable() == -1) {
				noRemainingPlayers = false;
				final View item = getPlayerView(player);
				list.addView(item);
				item.setOnClickListener(new OnClickListener() {			
					public void onClick(View v) {
						player.setTable(table);
						player.setSeat(seat);
						refreshSummary();
						displayPlayer(currentView, table, seat, player);
						if (currentPlayer != null) {
							currentPlayer.setTable(-1);
							currentPlayer.setSeat(-1);
						}
						dialog.cancel();
					}
				});
			}
		}
		
		if (noRemainingPlayers || Helper.getSelectedTournament(this).getPlayers().isEmpty()) {
			View panel = LayoutInflater.from(this).inflate(R.layout.empty_tables_list, null);
			dialog.setView(panel);
			Button ok = (Button) panel.findViewById(R.id.ButtonOK);
			ok.setOnClickListener(new OnClickListener() {				
				public void onClick(View v) {
					dialog.cancel();					
				}
			});
			if (!Helper.getSelectedTournament(this).getPlayers().isEmpty()) {
				TextView msg = (TextView) panel.findViewById(R.id.msgTextView);
				msg.setText("All the players already have a seat !");
			} 
		}
		
		dialog.show();
	}
	
	private View getPlayerView(Player player) {
    	final View v = LayoutInflater.from(this).inflate(R.layout.player_simple_item, null);
    	
        TextView name = (TextView) v.findViewById(R.id.playerName);
        name.setText(player.getDisplayName());
        
        QuickContactBadge badge =  (QuickContactBadge) v.findViewById(R.id.playerBadge);
        if (player.getPhoto() != null) {
        	badge.setImageBitmap(player.getPhoto());
        }
        
        v.setTag(player);
        return v;
    }

	public void afterTextChanged(Editable s) {}

	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

	public void onTextChanged(CharSequence s, int start, int before, int count) {
		seatAllPlayers();		
	}

}
