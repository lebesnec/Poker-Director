package poker.tm.activities;

import poker.tm.Helper;
import poker.tm.R;
import poker.tm.colorpicker.ColorPickerDialog;
import poker.tm.model.Tournament;
import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class EditChipsActivity extends Activity {
	
	private int[] chipsIds = {
    		R.id.chips0, R.id.chips1, R.id.chips2, 
    		R.id.chips3, R.id.chips4, R.id.chips5, 
    		R.id.chips6, R.id.chips7, R.id.chips8, 
    		R.id.chips9, R.id.chips10, R.id.chips11
    };
	private int[] chipsValues;
	private int[] chipsColor;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.chips_layout);
		 
		Tournament tournament = Helper.getSelectedTournament(this);
		int tournamentId = tournament.getId();
        chipsValues = Helper.getDatabase(this).fetchAllChipsValue(tournamentId);
        chipsColor = Helper.getDatabase(this).fetchAllChipsColor(tournamentId);
        ChipsFormatFocusListener chipsfocusListener = new ChipsFormatFocusListener();
        
        for (int i = 0; i < 12; i++) {
        	final int fuckyoujava = i;
			EditText t = (EditText) this.findViewById(chipsIds[i]);
			if (t != null) {
				t.setText(Helper.formatBlind(chipsValues[i]));
				setChipsColor(t, chipsColor[i]);
				t.setOnFocusChangeListener(chipsfocusListener);
				t.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {					
					public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
						menu.add(0, chipsIds[fuckyoujava], Menu.FIRST, "Set color");
					}
				});
			}
		}
        
        CheckBox c = (CheckBox) this.findViewById(R.id.checkBoxShowChips);
        c.setChecked(tournament.isShowChips());
        
        EditText t = (EditText) this.findViewById(R.id.chipsBuyin);
        t.setText(tournament.getChipsBuyin() + "");
        t = (EditText) this.findViewById(R.id.chipsRebuy);
        t.setText(tournament.getChipsRebuy() + "");
        t = (EditText) this.findViewById(R.id.chipsAddon);
        t.setText(tournament.getChipsAddon() + "");
		
		Button doneButton = (Button) this.findViewById(R.id.chipsButton);
		doneButton.setOnClickListener(new OnClickListener() {					
			public void onClick(View v) {
				finish();
			}
		});
		
		this.findViewById(R.id.chipsMainlayout).requestFocus();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		this.findViewById(R.id.chipsMainlayout).setBackgroundDrawable(Helper.getBackground());
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		Tournament tournament =  Helper.getSelectedTournament(this);
		int tournamentId = tournament.getId();
		for (int i = 0; i < 12; i++) {
			EditText t = (EditText) this.findViewById(chipsIds[i]);
			String val = (t != null && !t.getText().toString().equals("")) ? t.getText().toString() : "0";
			val = val.replaceFirst("K", "000");
			Helper.getDatabase(this).updateChips(tournamentId, i, Integer.parseInt(val), chipsColor[i]);
		}
		
		CheckBox c = (CheckBox) this.findViewById(R.id.checkBoxShowChips);
		tournament.setShowChips(c.isChecked());
		EditText t = (EditText) this.findViewById(R.id.chipsBuyin);
		String val = t.getText().toString();
		tournament.setChipsBuyin(Integer.parseInt(val.equals("") ? "0" : val));
		t = (EditText) this.findViewById(R.id.chipsRebuy);
		val = t.getText().toString();
		tournament.setChipsRebuy(Integer.parseInt(val.equals("") ? "0" : val));
		t = (EditText) this.findViewById(R.id.chipsAddon);
		val = t.getText().toString();
		tournament.setChipsAddon(Integer.parseInt(val.equals("") ? "0" : val));
		Helper.getDatabase(this).updateTournamentChipsOptions(tournament);
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		for (int i = 0; i < 12; i++) { // fuck you java #1
			final int theChips = i; // fuck you java #2
			if (chipsIds[i] == item.getItemId()) {
				
				final ColorPickerDialog d = new ColorPickerDialog(this, chipsColor[i]);
				d.setButton("Ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {	
						chipsColor[theChips] = d.getColor();
						EditText chips = (EditText) EditChipsActivity.this.findViewById(chipsIds[theChips]);
						setChipsColor(chips, d.getColor());
					}
				});
				d.show();
				return true;					
			}
		}

		return super.onMenuItemSelected(featureId, item);
	}
	
	private void setChipsColor(EditText chips, int color) {
		String val = chips.getText().toString();
    	val = val.replaceFirst("K", "000");
		if (val.length() == 0 || Integer.parseInt(val) == 0) {
			color = Color.TRANSPARENT;
		}
		
		Bitmap filter;
		float[] hsv = {0, 0, 0};
		Color.colorToHSV(color, hsv);
		if (hsv[1] < 0.01f && hsv[2] > 0.99f && color != Color.TRANSPARENT) {
			filter = ((BitmapDrawable) getResources().getDrawable(R.drawable.chips_filter2)).getBitmap();
			chips.setTextColor(Color.BLUE);
		} else {
			filter = ((BitmapDrawable) getResources().getDrawable(R.drawable.chips_filter)).getBitmap();
			chips.setTextColor(Color.WHITE);
		}
    	Bitmap result = Bitmap.createBitmap(filter.getWidth(), filter.getHeight(), Config.ARGB_8888);
    	Canvas canvas = new Canvas(result);
    	Paint p = new Paint();    	
    	p.setColor(color);
    	canvas.drawCircle(1 + filter.getWidth() / 2, 1 + filter.getHeight() / 2, (filter.getWidth() / 2) - 4, p);
    	canvas.drawBitmap(filter, 0, 0, null);
    	BitmapDrawable bkg = new BitmapDrawable(getResources(), result);
    	chips.setBackgroundDrawable(bkg);
	}
	
	
	private class ChipsFormatFocusListener implements OnFocusChangeListener {
		
		public void onFocusChange(View v, boolean hasFocus) { 
			if (hasFocus) {
				TextView t = (EditText) v;
				String val = t.getText().toString();
				t.setText(val.replaceFirst("K", "000"));
			
			} else {
				EditText t = (EditText) v;
				String s = t.getText().toString();
				
				if (s.length() > 0) {
					int val = Integer.parseInt(s);
					t.setText(Helper.formatBlind(val));
				} else {
					t.setText("0");
				}
				
				for (int i = 0; i < 12; i++) { // fuck you java 
					if (chipsIds[i] == t.getId()) {
						setChipsColor(t, chipsColor[i]);
					}
				}
			}
		}
		
	}
	
}
