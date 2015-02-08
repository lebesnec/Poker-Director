package poker.tm.activities;

import poker.tm.Helper;
import poker.tm.R;
import poker.tm.model.Tournament;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class TemplateOptionsActivity extends Activity {	
	
	private static final int PLAYERS_ID	  = 0;
	private static final int TABLES_ID	  = 1;
	private static final int PRIZEPOOL_ID = 2;
	private static final int CHIPS_ID	  = 3;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.tournament_options_layout);
		
		EditText name = (EditText) this.findViewById(R.id.tournamentName);
		name.setText(Helper.getSelectedTournament(this).getName());
		
		Button playersButton = (Button) this.findViewById(R.id.PlayersButton);
		playersButton.setOnClickListener(new OnClickListener() {					
			public void onClick(View v) {
				Intent intent = new Intent(TemplateOptionsActivity.this, PlayersListActivity.class);
				startActivity(intent);
			}
		});
		
		Button tablesButton = (Button) this.findViewById(R.id.TablesButton);
		tablesButton.setOnClickListener(new OnClickListener() {					
			public void onClick(View v) {
				Intent intent = new Intent(TemplateOptionsActivity.this, TablesListActivity.class);
				startActivity(intent);
			}
		});
		
		Button prizepoolButton = (Button) this.findViewById(R.id.PrizepoolButton);
		prizepoolButton.setOnClickListener(new OnClickListener() {					
			public void onClick(View v) {
				Intent intent = new Intent(TemplateOptionsActivity.this, PrizepoolActivity.class);
				startActivity(intent);
			}
		});
		
		Button chipsButton = (Button) this.findViewById(R.id.ChipsButton);
		chipsButton.setOnClickListener(new OnClickListener() {					
			public void onClick(View v) {
				Intent intent = new Intent(TemplateOptionsActivity.this, EditChipsActivity.class);
				startActivity(intent);
			}
		});
		
		Button doneButton = (Button) this.findViewById(R.id.optionsButton);
		doneButton.setOnClickListener(new OnClickListener() {					
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		this.findViewById(R.id.chipsMainlayout).setBackgroundDrawable(Helper.getBackground());
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		Tournament tournament = Helper.getSelectedTournament(this);
		EditText name = (EditText) this.findViewById(R.id.tournamentName);
		tournament.setName(name.getText().toString());
		Helper.getDatabase(this).updateTournamentName(tournament.getId(), tournament.getName());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuItem item = menu.add(0, PLAYERS_ID, 0, "Players");
		item.setIcon(R.drawable.players);
		item = menu.add(0, TABLES_ID, 1, "Tables");
		item.setIcon(R.drawable.tables);
		item = menu.add(0, PRIZEPOOL_ID, 2, "Prizepool");
		item.setIcon(R.drawable.prizepool);
		item = menu.add(0, CHIPS_ID, 3, "Chips");
		item.setIcon(R.drawable.chips);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case PLAYERS_ID:				
				Intent intent = new Intent(this, PlayersListActivity.class);
				startActivity(intent);
				return true;
				
			case TABLES_ID:
				intent = new Intent(this, TablesListActivity.class);
				startActivity(intent);
				return true;
				
			case PRIZEPOOL_ID:
				intent = new Intent(this, PrizepoolActivity.class);
				startActivity(intent);
				return true;
				
			case CHIPS_ID:
				intent = new Intent(this, EditChipsActivity.class);
				startActivity(intent);
				return true;
				
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
}
