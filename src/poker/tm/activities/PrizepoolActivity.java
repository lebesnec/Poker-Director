package poker.tm.activities;

import poker.tm.Helper;
import poker.tm.R;
import poker.tm.model.Tournament;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PrizepoolActivity extends Activity implements TextWatcher {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Tournament t = Helper.getSelectedTournament(this);
		this.setContentView(R.layout.prizepool_layout);
		
		EditText b = (EditText) this.findViewById(R.id.buyin);
		b.setText(Helper.getSelectedTournament(this).getBuyin() + "");
		b.addTextChangedListener(this);
		
		EditText a = (EditText) this.findViewById(R.id.addon);
		a.setText(Helper.getSelectedTournament(this).getAddon() + "");
		a.addTextChangedListener(this);
		
		EditText r = (EditText) this.findViewById(R.id.valRebuy);
		r.setText(Helper.getSelectedTournament(this).getRebuy() + "");
		r.addTextChangedListener(this);
		
		EditText n = (EditText) this.findViewById(R.id.nbRebuy);
		n.setText(Helper.getSelectedTournament(this).getNbRebuy() + "");
		n.addTextChangedListener(this);
		
		TextView c = (TextView) this.findViewById(R.id.currencyTextView0);
		c.setText(Helper.getCurrency(this));
		c = (TextView) this.findViewById(R.id.currencyTextView1);
		c.setText(Helper.getCurrency(this));
		c = (TextView) this.findViewById(R.id.currencyTextView2);
		c.setText(Helper.getCurrency(this));
		
		for (Integer percent : t.getPrizepool()) {
			this.addPaidPlace(percent);
		}
		
		this.refreshPrizes();		
		
		Button doneButton = (Button) this.findViewById(R.id.prizepoolButton);
		doneButton.setOnClickListener(new OnClickListener() {					
			public void onClick(View v) {
				finish();
			}
		});
		
		Button addPaidPlace = (Button) this.findViewById(R.id.addPaidPlace);
		addPaidPlace.setOnClickListener(new OnClickListener() {					
			public void onClick(View v) {
				addPaidPlace(-1);
			}
		});
		
		EditText totalVal = (EditText) this.findViewById(R.id.total);
		totalVal.addTextChangedListener(new TextWatcher() {			
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				refreshPayouts();				
			}			
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}			
			public void afterTextChanged(Editable s) {}
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
		
		Tournament t = Helper.getSelectedTournament(this);
		Helper.getDatabase(this).setBuyInAndAddon(t.getId(), t.getBuyin(), t.getAddon(), t.getNbRebuy(), t.getRebuy());
		
		t.getPrizepool().clear();
		LinearLayout list = (LinearLayout) this.findViewById(R.id.paidPlacesList);
		for (int i = 0; i < list.getChildCount(); i++) {
			TextView val = (TextView) list.getChildAt(i).findViewById(R.id.percent);
			if (!val.getText().toString().equals("")) {
				int percent = Integer.parseInt(val.getText().toString());
				t.getPrizepool().add(percent);
			} else {
				t.getPrizepool().add(0);
			}
		}
		Helper.getDatabase(this).setPrizepool(t.getId(), t.getPrizepool()); 
	}
	
	private void addPaidPlace(int percent) {
		final LinearLayout list = (LinearLayout) this.findViewById(R.id.paidPlacesList);
		final View v = LayoutInflater.from(this).inflate(R.layout.prizepool_item, null);
		list.addView(v);
		
		final int pos = list.getChildCount();
		TextView num = (TextView) v.findViewById(R.id.number);
		num.setText(pos + "");
		
		if (pos > 1 && percent == -1) {
			percent = 0;
		} else if (pos == 1 && percent == -1) {
			percent = 100;
		}
		TextView val = (TextView) v.findViewById(R.id.percent);
		val.setText(percent + "");
		val.addTextChangedListener(new TextWatcher() {			
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				refreshPrizes();			
			}			
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}			
			public void afterTextChanged(Editable s) {}
		});
		
		TextView dollar = (TextView) v.findViewById(R.id.resultlabel);
		dollar.setText("% = " + (percent * Helper.getSelectedTournament(this).getEstimateTotalPrize() / 100) + " " + Helper.getCurrency(this));
		
		View delete = v.findViewById(R.id.list_button);
		delete.setOnClickListener(new OnClickListener() {					
			public void onClick(View button) {
				for (int i = pos; i < list.getChildCount(); i++) {
					TextView num = (TextView) list.getChildAt(i).findViewById(R.id.number);
					num.setText(i + "");
				}
				list.removeView(v);
				refreshPrizes();
			}
		});
		
		refreshPrizes();
	}
	
	private void refreshPrizes() {
		Tournament t = Helper.getSelectedTournament(this);
		
		EditText b = (EditText) this.findViewById(R.id.buyin);
		if (!b.getText().toString().equals("")) {
			int buyin = Integer.parseInt(b.getText().toString());
			t.setBuyin(buyin);
		}
		
		EditText a = (EditText) this.findViewById(R.id.addon);
		if (!a.getText().toString().equals("")) {
			int addon = Integer.parseInt(a.getText().toString());
			t.setAddon(addon);
		}
		
		EditText r = (EditText) this.findViewById(R.id.valRebuy);
		if (!r.getText().toString().equals("")) {
			int rebuy = Integer.parseInt(r.getText().toString());
			t.setRebuy(rebuy);
		}
		
		EditText n = (EditText) this.findViewById(R.id.nbRebuy);
		if (!n.getText().toString().equals("")) {
			int nb = Integer.parseInt(n.getText().toString());
			t.setNbRebuy(nb);
		}
		
		TextView total = (TextView) this.findViewById(R.id.totalLabel);
		total.setText("Total max. for " + t.getPlayers().size() + " players : ");
		
		EditText totalVal = (EditText) this.findViewById(R.id.total);
		totalVal.setText(t.getEstimateTotalPrize() + "");
		
		refreshPayouts();
	}
	
	private void refreshPayouts() {
		EditText totalVal = (EditText) this.findViewById(R.id.total);
		int total = 0;
		if (!totalVal.getText().toString().equals("")) {
			total = Integer.parseInt(totalVal.getText().toString());
		}
		
		int sum = 0;
		int money = 0;
		LinearLayout list = (LinearLayout) this.findViewById(R.id.paidPlacesList);
		for (int i = list.getChildCount() - 1; i >= 0; i--) {
			TextView val = (TextView) list.getChildAt(i).findViewById(R.id.percent);
			TextView dollar = (TextView) list.getChildAt(i).findViewById(R.id.resultlabel);
			if (!val.getText().toString().equals("")) {
				int percent = Integer.parseInt(val.getText().toString());
				sum = sum + percent;
				money = money + (percent * total / 100);
				if (i > 0) {
					dollar.setText("% = " + (percent * total / 100) + " " + Helper.getCurrency(this));
				} else {
					// fix pour les erreurs d'arrondi :
					dollar.setText("% = " + (percent * total / 100 + (total - money)) + " " + Helper.getCurrency(this));
				}
			} else {
				dollar.setText("% = 0 " + Helper.getCurrency(this));
			}
		}
		
		TextView sumPercent = (TextView) this.findViewById(R.id.sumLabel);
		sumPercent.setText("sum = " + sum + " %");
		if (sum == 100) {
			sumPercent.setTextColor(Color.GREEN);
		} else {
			sumPercent.setTextColor(Color.RED);
		}
	}

	public void afterTextChanged(Editable s) {}

	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

	public void onTextChanged(CharSequence s, int start, int before, int count) {
		refreshPrizes();		
	}
	
}
