package poker.tm.activities;

import java.util.Collections;
import java.util.List;

import poker.tm.Helper;
import poker.tm.R;
import poker.tm.model.Player;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract.QuickContact;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.QuickContactBadge;
import android.widget.TextView;

public class TournamentEndingActivity extends Activity {
	
	private LinearLayout list;
	
	private List<Player> players;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		this.setContentView(R.layout.ending_tournament);
		
		list = (LinearLayout) this.findViewById(R.id.playersListView);
		
		players = Helper.getSelectedTournament(this).getPlayers();
		Collections.sort(players);
		int place = 0;
		for (Player player : players) {
			addContact(player, place);
			place ++;
		}
		
		int total = Helper.getSelectedTournament(this).getTotalPrize();
		int money = 0;
		for (int i = list.getChildCount() - 1; i >= 0; i--) {
			TextView dollar = (TextView) list.getChildAt(i).findViewById(R.id.playerPrize);
			if (i > Helper.getSelectedTournament(this).getPrizepool().size() - 1) {
				dollar.setText("0 " + Helper.getCurrency(this));
			} else {
				int percent = Helper.getSelectedTournament(this).getPrizepool().get(i);
				money = money + (percent * total / 100);
				if (i > 0) {
					dollar.setText((percent * total / 100) + " " + Helper.getCurrency(this));
				} else {
					// fix pour les erreurs d'arrondi :
					dollar.setText((percent * total / 100 + (total - money)) + " " + Helper.getCurrency(this));
				}
				if (percent * total / 100 > 0) {
		        	dollar.setTextColor(Color.GREEN);
		        }
			}
		}
		
		Button doneButton = (Button) this.findViewById(R.id.endButton);
		doneButton.setOnClickListener(new OnClickListener() {					
			public void onClick(View v) {
				finish();
				Intent intent = new Intent(TournamentEndingActivity.this, TemplatesListActivity.class);
				startActivity(intent);
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		this.findViewById(R.id.playersMainlayout).setBackgroundDrawable(Helper.getBackground());
	}
    
    private void addContact(final Player contact, int place) {
    	final View v = LayoutInflater.from(this).inflate(R.layout.player_prize_item, null);
    	TextView p = (TextView) v.findViewById(R.id.playerPlace);
        p.setText((place + 1) + "");
    	
        TextView name = (TextView) v.findViewById(R.id.playerName);
        name.setText(contact.getDisplayName());
        
        TextView spend = (TextView) v.findViewById(R.id.playerSpend);
        spend.setText(contact.getMoneySpend(Helper.getSelectedTournament(this)) + " " + Helper.getCurrency(this));
        
        if (contact.getUri() != null) {
	        QuickContactBadge badge =  (QuickContactBadge) v.findViewById(R.id.playerBadge);
	        badge.assignContactUri(contact.getUri());
	        badge.setMode(QuickContact.MODE_MEDIUM);
	        if (contact.getPhoto() != null) {
	        	badge.setImageBitmap(contact.getPhoto());
	        } 
        }
        
        list.addView(v);
    }

}
