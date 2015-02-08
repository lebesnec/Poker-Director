package poker.tm.activities;

import poker.tm.Helper;
import poker.tm.R;
import poker.tm.model.Round;
import poker.tm.model.Tournament;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class NewTournamentActivity  extends Activity {
	
	private EditText sbView, bbView;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.new_tournament_layout);
		
		final EditText nameView = (EditText) this.findViewById(R.id.newTournamentName);
		String name = "tournament " + (Helper.getTournaments(this).size() + 1);
		nameView.setText(name);
		nameView.setOnFocusChangeListener(new EmptyFieldFocusListener());
		
		sbView = (EditText) this.findViewById(R.id.newTournamentSB);
		sbView.setOnFocusChangeListener(new BlindFormatFocusListener());
		
		bbView = (EditText) this.findViewById(R.id.newTournamentBB);
		bbView.setOnFocusChangeListener(new BlindFormatFocusListener());
		
		final EditText durationView = (EditText) this.findViewById(R.id.newTournamentDuration);
		durationView.setOnFocusChangeListener(new EmptyFieldFocusListener());
		
		Button createButton = (Button) this.findViewById(R.id.createTournamentButton);
		createButton.setOnClickListener(new OnClickListener() {					
			public void onClick(View v) {
				Tournament t = new Tournament();
				t.setName(nameView.getText().toString());
				int sb = Integer.parseInt(sbView.getText().toString().replaceFirst("K", "000"));
				int bb = Integer.parseInt(bbView.getText().toString().replaceFirst("K", "000"));
				String d = durationView.getText().toString().equals("") ? "10" : durationView.getText().toString();
				int duration = Integer.parseInt(d.toString());
				Round r = new Round(sb, bb, 0, duration * 60);
				t.addStep(r);
				for (int i = 0; i < 9; i++) {
					t.addRound();
				}
				
				long id = Helper.getDatabase(NewTournamentActivity.this).insertTournament(t);
				t.setId((int) id);
				boolean ok = Helper.setSelectedTournament(NewTournamentActivity.this, t);
				Helper.getTournaments(NewTournamentActivity.this).add(t);
				
				NewTournamentActivity.this.finish();				
				if (ok) {
					Intent intent = new Intent(NewTournamentActivity.this, EditTemplateActivity.class);
					startActivity(intent);
				} else {
					AlertDialog.Builder builder = new AlertDialog.Builder(NewTournamentActivity.this);
					builder.setMessage("There was an error while opening the tournament.");
					builder.setNeutralButton("close", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
					AlertDialog alert = builder.create();
					alert.show();
				}
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		this.findViewById(R.id.newTournamentMainlayout).setBackgroundDrawable(Helper.getBackground());
	}
	
	private class EmptyFieldFocusListener implements OnFocusChangeListener {
		
		public void onFocusChange(View v, boolean hasFocus) {
			if (!hasFocus) {
				TextView t = (EditText) v;
				String s = t.getText().toString();
				if (s.equals("")) {
					if (t.getId() == R.id.newTournamentDuration) {
						t.setText("10");
					} else {
						String name = "tournament " + (Helper.getTournaments(NewTournamentActivity.this).size() + 1);
						t.setText(name);
					}
				}
			}
		}
		
	}
	
	private class BlindFormatFocusListener implements OnFocusChangeListener {
		
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				TextView t = (EditText) v;
				String val = t.getText().toString();
				t.setText(val.replaceFirst("K", "000"));
			
			} else { 
				TextView t = (EditText) v;
				String s = t.getText().toString();
				s = (s.equals("") ? "0" : s);
				int val = Integer.parseInt(s);
				t.setText(Helper.formatBlind(val));
				
				int sb = Integer.parseInt(sbView.getText().toString().replaceFirst("K", "000"));
				int bb = Integer.parseInt(bbView.getText().toString().replaceFirst("K", "000"));
				if (v.getId() == R.id.newTournamentSB && sb >= bb) { 
					bbView.setText(Helper.formatBlind(sb * 2));
					
				} else if (v.getId() == R.id.newTournamentBB && bb < sb) {
					sbView.setText(Helper.formatBlind(bb / 2));
				}
			}
		}
		
	}
	
}
