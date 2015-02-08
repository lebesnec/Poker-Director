package poker.tm.activities;

import java.util.List;

import poker.tm.ContactAccessor;
import poker.tm.ContactListAdapter;
import poker.tm.Helper;
import poker.tm.R;
import poker.tm.model.Player;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.QuickContact;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.QuickContactBadge;
import android.widget.TextView;

public class PlayersListActivity extends Activity {
	
	private static final int DELETE_ALL   = 1;
	private static final int ANONYMOUS 	  = 2;
	private static final int CONTACT_LIST = 3;
	private static final int DELETE		  = 4;
	// Request code for the contact picker activity
    private static final int PICK_CONTACT_REQUEST = 1;

	private ContactAccessor contactAccessor = new ContactAccessor();
	private LinearLayout list;
	private TextView title;
	private View currentView;
	
	private List<Player> players;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		this.setContentView(R.layout.players_list);
		
		list = (LinearLayout) this.findViewById(R.id.playersListView);
		title = (TextView) this.findViewById(R.id.playersTitle);
		
		players = Helper.getSelectedTournament(this).getPlayers();
		for (Player player : players) {
			addContact(player);
		}
		
		Button doneButton = (Button) this.findViewById(R.id.playersButton);
		doneButton.setOnClickListener(new OnClickListener() {					
			public void onClick(View v) {
				finish();
			}
		});
		
		Button anonymous = (Button) this.findViewById(R.id.addAnonymous);
		anonymous.setOnClickListener(new OnClickListener() {					
			public void onClick(View v) {
				Player contact = new Player();
				contact.setDisplayName("Player " + (players.size() + 1));
				players.add(contact);
				addContact(contact);
			}
		});
		
        Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, ContactListAdapter.PEOPLE_PROJECTION, null, null, ContactsContract.Contacts.DISPLAY_NAME);
        ContactListAdapter adapter = new ContactListAdapter(this, cursor);
        final AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.AutoCompleteTextView);
        textView.setAdapter(adapter);
		textView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> av, View v, int i, long l) {
				if (v != null && v.getTag() != null) {
					Long id = (Long) v.getTag();
					Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, id);
					loadContactInfo(contactUri);
					textView.setText("");
				}
			}
		});
		
		View addButton = this.findViewById(R.id.addPlayer);
		addButton.setOnClickListener(new OnClickListener() {
			public void onClick(View button) {
				String val = textView.getText().toString();
				if (!val.equals("")) {
					Player contact = new Player();
					contact.setDisplayName(val);
					players.add(contact);
					addContact(contact);
					textView.setText("");
				}
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		this.findViewById(R.id.playersMainlayout).setBackgroundDrawable(Helper.getBackground());
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		int tournamentId = Helper.getSelectedTournament(this).getId();
		Helper.getDatabase(this).setPlayers(tournamentId, players);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		currentView = v;
		if (v.getTag() != null) {
			Player c = (Player) v.getTag();
			
			menu.setHeaderTitle(c.getDisplayName());
			menu.add(0, DELETE, 0, "Delete player");
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {		
		switch (item.getItemId()) {				
			case DELETE:
				list.removeView(currentView);
				players.remove(currentView.getTag());	
				refreshTitle();
				return true;
				
			default:
				return super.onContextItemSelected(item);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
//		MenuItem item = menu.add(0, CONTACT_LIST, 0, "Add from contacts list");
//		item.setIcon(R.drawable.ic_menu_friendslist);
		MenuItem item = menu.add(0, ANONYMOUS, 1, "Add anonymous");
		item.setIcon(R.drawable.ic_menu_invite);
		item = menu.add(0, DELETE_ALL, 1, "Delete all");
		item.setIcon(android.R.drawable.ic_menu_delete);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case CONTACT_LIST:				
				startActivityForResult(contactAccessor.getPickContactIntent(), PICK_CONTACT_REQUEST);
				return true;
				
			case ANONYMOUS:
				Player contact = new Player();
				contact.setDisplayName("Player " + (players.size() + 1));
				players.add(contact);
				addContact(contact);
				return true;
				
			case DELETE_ALL:
				list.removeAllViews();
				players.clear();
				refreshTitle();
				return true;
				
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	/**
     * Invoked when the contact picker activity is finished. The {@code contactUri} parameter
     * will contain a reference to the contact selected by the user. We will treat it as
     * an opaque URI and allow the SDK-specific ContactAccessor to handle the URI accordingly.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_CONTACT_REQUEST && resultCode == RESULT_OK) {
            loadContactInfo(data.getData());
        }
    }
    
    /**
     * Load contact information on a background thread.
     */
    private void loadContactInfo(Uri contactUri) {
        // We should always run database queries on a background thread. The database may be
        // locked by some process for a long time.  If we locked up the UI thread while waiting
        // for the query to come back, we might get an "Application Not Responding" dialog.
        AsyncTask<Uri, Void, Player> task = new AsyncTask<Uri, Void, Player>() {

            @Override
            protected Player doInBackground(Uri... uris) {
                return contactAccessor.loadContact(PlayersListActivity.this.getContentResolver(), uris[0]);
            }

            @Override
            protected void onPostExecute(Player result) {
            	addContact(result);
            	players.add(result);
            }
        };

        task.execute(contactUri);
    }
    
    private void addContact(final Player contact) {
    	final View v = LayoutInflater.from(this).inflate(R.layout.player_item, null);
    	this.registerForContextMenu(v);
        TextView name = (TextView) v.findViewById(R.id.playerName);
        name.setText(contact.getDisplayName());
        TextView table = (TextView) v.findViewById(R.id.tableTextView);
        if (contact.getTable() >= 0) {
        	table.setText("table " + (contact.getTable() + 1));
        } else {
        	table.setText("");
        }
        TextView seat = (TextView) v.findViewById(R.id.seatTextView);
        if (contact.getSeat() >= 0) {
        	seat.setText("seat " + (contact.getSeat() + 1));
        } else {
        	seat.setText("");
        }
        
        if (contact.getUri() != null) {
	        QuickContactBadge badge =  (QuickContactBadge) v.findViewById(R.id.playerBadge);
	        badge.assignContactUri(contact.getUri());
	        badge.setMode(QuickContact.MODE_MEDIUM);
	        if (contact.getPhoto() != null) {
	        	badge.setImageBitmap(contact.getPhoto());
	        } 
        }
        
        v.setTag(contact);
        list.addView(v);
        
        View remove = v.findViewById(R.id.list_button);
        remove.setOnClickListener(new OnClickListener() {					
			public void onClick(View theButton) {
				list.removeView(v);
				players.remove(contact);	
				refreshTitle();
			}
		});
        
        refreshTitle();
    }
    
    private void refreshTitle() {
    	if (players.size() <= 1) {
        	title.setText(players.size() + " player");
        } else {
        	title.setText(players.size() + " players");
        }
    }

}
