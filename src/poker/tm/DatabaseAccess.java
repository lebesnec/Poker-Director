package poker.tm;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import poker.tm.model.AddonOrRebuy;
import poker.tm.model.Break;
import poker.tm.model.Player;
import poker.tm.model.Round;
import poker.tm.model.Tournament;
import poker.tm.model.TournamentStep;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;

public class DatabaseAccess {

    private static final String DATABASE_NAME = "tm.data";
    private static final int DATABASE_VERSION = 15;
    
    private static final String DATABASE_TABLE_TOURNAMENTS = "tournaments";
    private static final String KEY_TOURNAMENTS_ROWID 	   = "_id";
    private static final String KEY_TOURNAMENTS_NAME 	   = "name";
    private static final String KEY_TOURNAMENTS_NBTABLES   = "nbTables";
    private static final String KEY_TOURNAMENTS_NBSEATS    = "nbSeats";
    private static final String KEY_TOURNAMENTS_SHOWCHIPS  = "showChips";
    private static final String KEY_TOURNAMENTS_BUYINCHIPS = "buyinChips";
    private static final String KEY_TOURNAMENTS_REBUYCHIPS = "rebuyChips";
    private static final String KEY_TOURNAMENTS_ADDONCHIPS = "addonChips";
	
	private static final String DATABASE_TABLE_STEPS   = "steps";
	private static final String KEY_STEPS_ROWID 	   = "_id";
	private static final String KEY_STEPS_TOURNAMENTID = "tournamentId";
	private static final String KEY_STEPS_DURATION 	   = "duration";
	private static final String KEY_STEPS_TYPE 		   = "type";
	private static final String KEY_STEPS_SMALLBLIND   = "smallBlind";
	private static final String KEY_STEPS_BIGBLIND 	   = "bigBlind";
	private static final String KEY_STEPS_ANTES 	   = "antes";
	private static final String KEY_STEPS_ADDON 	   = "addon";
	private static final String KEY_STEPS_REBUY 	   = "rebuy";
	
	private static final String DATABASE_TABLE_CHIPS   = "chips";
	private static final String KEY_CHIPS_TOURNAMENTID = "tournamentId";
	private static final String KEY_CHIPS_COLOR 	   = "color";
	private static final String KEY_CHIPS_VALUE 	   = "value";
	private static final String KEY_CHIPS_ANDROIDCOLOR = "androidColor";
	
	private static final String DATABASE_TABLE_PLAYERS 	 = "players";
	private static final String KEY_PLAYERS_ROWID 		 = "_id";
	private static final String KEY_PLAYERS_TOURNAMENTID = "tournamentId";
	private static final String KEY_PLAYERS_PLAYERID 	 = "playerId";
	private static final String KEY_PLAYERS_PLAYERNAME 	 = "playerName";
	private static final String KEY_PLAYERS_TABLE 		 = "tabl";
	private static final String KEY_PLAYERS_SEAT 		 = "seat";
	
	private static final String DATABASE_TABLE_BUYIN   = "buyin"; 
	private static final String KEY_BUYIN_TOURNAMENTID = "tournamentId";
	private static final String KEY_BUYIN_BUYIN		   = "buyin";
	private static final String KEY_BUYIN_ADDON 	   = "addon";
	private static final String KEY_BUYIN_NBREBUY 	   = "rebuy";
	private static final String KEY_BUYIN_VALREBUY     = "valRebuy";

	private static final String DATABASE_TABLE_PRIZEPOOL   = "prizepool";
	private static final String KEY_PRIZEPOOL_TOURNAMENTID = "tournamentId";
	private static final String KEY_PRIZEPOOL_PLACE		   = "place";
	private static final String KEY_PRIZEPOOL_PERCENT	   = "percent";

    
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    private final Context mCtx;
    

    public DatabaseAccess(Context ctx) {
        this.mCtx = ctx;
    }

    
    public DatabaseAccess open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        
        return this;
    }
    
    public void close() {
        mDbHelper.close();
    }
    
    public boolean isOpen() {
    	return mDb != null && mDb.isOpen();
    }

    public long insertTournament(Tournament tournament) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TOURNAMENTS_NAME, tournament.getName());
        long id = mDb.insert(DATABASE_TABLE_TOURNAMENTS, null, initialValues);
        
        if (id != -1) {
	        for (TournamentStep step : tournament.getSteps()) {
				this.insertStep(step, id);
			}
        }
        
        return id;
    }

    public boolean deleteTournament(long rowId) {
    	mDb.delete(DATABASE_TABLE_STEPS, KEY_STEPS_TOURNAMENTID + "=" + rowId, null);
    	
        return mDb.delete(DATABASE_TABLE_TOURNAMENTS, KEY_TOURNAMENTS_ROWID + "=" + rowId, null) > 0;
    }

    public ArrayList<Tournament> fetchAllTournaments(ContentResolver contentResolver) {
		Cursor c = mDb.query(DATABASE_TABLE_TOURNAMENTS, new String[] {
				KEY_TOURNAMENTS_ROWID, KEY_TOURNAMENTS_NAME,
				KEY_TOURNAMENTS_NBTABLES, KEY_TOURNAMENTS_NBSEATS,
				KEY_TOURNAMENTS_SHOWCHIPS, KEY_TOURNAMENTS_BUYINCHIPS,
				KEY_TOURNAMENTS_REBUYCHIPS, KEY_TOURNAMENTS_ADDONCHIPS }, null,
				null, null, null, KEY_TOURNAMENTS_ROWID);
        try {
	        ArrayList<Tournament> result = new ArrayList<Tournament>(c.getCount());
	        
	        if (c != null && c.moveToFirst()) {
	            int idColumn = c.getColumnIndex(KEY_TOURNAMENTS_ROWID); 
	            int nameColumn = c.getColumnIndex(KEY_TOURNAMENTS_NAME);
	            int nbTablesColumn = c.getColumnIndex(KEY_TOURNAMENTS_NBTABLES); 
	            int nbSeatsColumn = c.getColumnIndex(KEY_TOURNAMENTS_NBSEATS);
	            int showChipsColumn = c.getColumnIndex(KEY_TOURNAMENTS_SHOWCHIPS);
	            int buyinChipsColumn = c.getColumnIndex(KEY_TOURNAMENTS_BUYINCHIPS);
	            int rebuyChipsColumn = c.getColumnIndex(KEY_TOURNAMENTS_REBUYCHIPS);
	            int addonChipsColumn = c.getColumnIndex(KEY_TOURNAMENTS_ADDONCHIPS);
	        
	            do {
	            	Tournament tournament = new Tournament();
	            	tournament.setId(c.getInt(idColumn));
	                tournament.setName(c.getString(nameColumn));
	                tournament.setNbTable(c.getInt(nbTablesColumn));
	                tournament.setNbSeat(c.getInt(nbSeatsColumn));
	                tournament.setShowChips(c.getInt(showChipsColumn) == 1);
	                tournament.setChipsBuyin(c.getInt(buyinChipsColumn));
	                tournament.setChipsRebuy(c.getInt(rebuyChipsColumn));
	                tournament.setChipsAddon(c.getInt(addonChipsColumn));
	                tournament.setSteps(fetchAllSteps(tournament.getId()));
	                this.getBuyInAndAddon(tournament);
	                tournament.setPlayers(fetchAllPlayers(tournament.getId(), contentResolver));
	                tournament.setPrizepool(fetchPrizepool(tournament.getId()));
	                result.add(tournament);
	
	            } while (c.moveToNext());
	        }
	        
	        return result;
	        
        } finally {
        	c.close();
        }
    }
    
    private ArrayList<TournamentStep> fetchAllSteps(long tournamentRowId) {
        Cursor c = mDb.query(DATABASE_TABLE_STEPS, new String[] {KEY_STEPS_ROWID, KEY_STEPS_TOURNAMENTID, KEY_STEPS_DURATION, KEY_STEPS_TYPE, KEY_STEPS_SMALLBLIND, KEY_STEPS_BIGBLIND, KEY_STEPS_ANTES, KEY_STEPS_ADDON, KEY_STEPS_REBUY}, KEY_STEPS_TOURNAMENTID + "=" + tournamentRowId, null, null, null, null);
        
        try {
        	ArrayList<TournamentStep> result = new ArrayList<TournamentStep>(c.getCount());
        	if (c != null && c.moveToFirst()) {
        		int idColumn = c.getColumnIndex(KEY_TOURNAMENTS_ROWID); 
        		int durationColumn = c.getColumnIndex(KEY_STEPS_DURATION);
        		int typeColumn = c.getColumnIndex(KEY_STEPS_TYPE);
        		int smallBLindColumn = c.getColumnIndex(KEY_STEPS_SMALLBLIND);
        		int bigBlindColumn = c.getColumnIndex(KEY_STEPS_BIGBLIND);
        		int antesColumn = c.getColumnIndex(KEY_STEPS_ANTES);
        		int addonColumn = c.getColumnIndex(KEY_STEPS_ADDON);
        		int rebuyColumn = c.getColumnIndex(KEY_STEPS_REBUY);
	        
	            do {
	            	switch (c.getInt(typeColumn)) {
						case 0:
							Round round = new Round(c.getInt(smallBLindColumn), c.getInt(bigBlindColumn), c.getInt(antesColumn));
							round.setDuration(c.getInt(durationColumn));
							round.setId(c.getInt(idColumn));
							result.add(round);
							break;
							
						case 1:
							Break break1 = new Break();
							break1.setDuration(c.getInt(durationColumn));
							break1.setId(c.getInt(idColumn));
							result.add(break1);
							break;
							
						case 2:
							AddonOrRebuy aor = new AddonOrRebuy(c.getInt(addonColumn) == 1, c.getInt(rebuyColumn) == 1);
							aor.setId(c.getInt(idColumn));
							result.add(aor);
							break;
	
						default:
							break;
					}	
	            } while (c.moveToNext());
        	}
        	
        	return result;
        	
        } finally {
        	c.close();
        }
    }

//    public Tournament fetchTournament(long rowId) throws SQLException {
//        Cursor c = mDb.query(true, DATABASE_TABLE_TOURNAMENTS, new String[] {KEY_TOURNAMENTS_ROWID, KEY_TOURNAMENTS_NAME}, KEY_TOURNAMENTS_ROWID + "=" + rowId, null, null, null, null, null);
//        try {
//	        if (c != null && c.moveToFirst()) {
//	        	int idColumn = c.getColumnIndex(KEY_TOURNAMENTS_ROWID); 
//	            int nameColumn = c.getColumnIndex(KEY_TOURNAMENTS_NAME);
//	        
//	        	Tournament tournament = new Tournament();
//	        	tournament.setId(c.getInt(idColumn));
//	            tournament.setName(c.getString(nameColumn));
//	            
//	            return tournament;
//	        }
//        
//        return null;
//        
//        } finally {
//        	c.close();
//        }
//    }

    public boolean updateTournamentName(long rowId, String name) {
        ContentValues args = new ContentValues();
        args.put(KEY_TOURNAMENTS_NAME, name);

        return mDb.update(DATABASE_TABLE_TOURNAMENTS, args, KEY_TOURNAMENTS_ROWID + "=" + rowId, null) > 0;
    }
    
    public boolean updateTournamentTablesAndSeats(Tournament t) {
        ContentValues args = new ContentValues();
        args.put(KEY_TOURNAMENTS_NBTABLES, t.getNbTable());
        args.put(KEY_TOURNAMENTS_NBSEATS, t.getNbSeat());

        return mDb.update(DATABASE_TABLE_TOURNAMENTS, args, KEY_TOURNAMENTS_ROWID + "=" + t.getId(), null) > 0;
    }
    
    public boolean updateTournament(Tournament t) {
        boolean result = this.updateTournamentName(t.getId(), t.getName());
        
        mDb.delete(DATABASE_TABLE_STEPS, KEY_STEPS_TOURNAMENTID + "=" + t.getId(), null);
        
        for (TournamentStep step : t.getSteps()) { 
			this.insertStep(step, t.getId());
		}
        
        t.setSteps(this.fetchAllSteps(t.getId()));
        
        return result;
    }
    
    private long insertStep(TournamentStep step, long tournamentRowId) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_STEPS_TOURNAMENTID, tournamentRowId);
        initialValues.put(KEY_STEPS_DURATION, step.getDuration());
        initialValues.put(KEY_STEPS_TYPE, getType(step));
        
        if (step instanceof Round) {
        	Round r = (Round) step;
	        initialValues.put(KEY_STEPS_SMALLBLIND, r.getSmallBlind());
	        initialValues.put(KEY_STEPS_BIGBLIND, r.getBigBlind());
	        initialValues.put(KEY_STEPS_ANTES, r.getAntes());
	        
        } else if (step instanceof AddonOrRebuy) {
        	AddonOrRebuy aor = (AddonOrRebuy) step;
	        initialValues.put(KEY_STEPS_ADDON, aor.isAddon());
	        initialValues.put(KEY_STEPS_REBUY, aor.isRebuy());
        }

        return mDb.insert(DATABASE_TABLE_STEPS, null, initialValues);
    }
    
    private int getType(TournamentStep step) {
    	if (step instanceof Round) {
			return 0;
			
		} else if (step instanceof Break) {
			return 1;
			
		} else if (step instanceof AddonOrRebuy) {
			return 2;
			
		} else {
			throw new IllegalArgumentException();
		}
    }

    public boolean deleteStep(long rowId) {
        return mDb.delete(DATABASE_TABLE_STEPS, KEY_STEPS_ROWID + "=" + rowId, null) > 0;
    }    

    public boolean updateStep(TournamentStep step) {
        ContentValues args = new ContentValues();
        args.put(KEY_STEPS_DURATION, step.getDuration());
        args.put(KEY_STEPS_TYPE, getType(step));
        
        if (step instanceof Round) {
        	Round r = (Round) step;
        	args.put(KEY_STEPS_SMALLBLIND, r.getSmallBlind());
        	args.put(KEY_STEPS_BIGBLIND, r.getBigBlind());
        	args.put(KEY_STEPS_ANTES, r.getAntes());
	        
        } else if (step instanceof AddonOrRebuy) {
        	AddonOrRebuy aor = (AddonOrRebuy) step;
        	args.put(KEY_STEPS_ADDON, aor.isAddon());
        	args.put(KEY_STEPS_REBUY, aor.isRebuy());
        }

        return mDb.update(DATABASE_TABLE_STEPS, args, KEY_STEPS_ROWID + "=" + step.getId(), null) > 0;
    }
    
    public void updateChips(long tournamentRowId, int color, int value, int androidColor) {
    	mDb.delete(DATABASE_TABLE_CHIPS, KEY_CHIPS_TOURNAMENTID + "=" + tournamentRowId + " AND " + KEY_CHIPS_COLOR + "=" + color, null);
    	
    	ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_CHIPS_TOURNAMENTID, tournamentRowId);
        initialValues.put(KEY_CHIPS_COLOR, color);
        initialValues.put(KEY_CHIPS_VALUE, value);
        initialValues.put(KEY_CHIPS_ANDROIDCOLOR, androidColor);
        mDb.insert(DATABASE_TABLE_CHIPS, null, initialValues);
    }
    
    public int[] fetchAllChipsValue(long tournamentRowId) {
        Cursor c = mDb.query(DATABASE_TABLE_CHIPS, new String[] {KEY_CHIPS_COLOR, KEY_CHIPS_VALUE}, KEY_CHIPS_TOURNAMENTID + "=" + tournamentRowId, null, null, null, null);
        
        try {
        	int[] result = {1, 2, 5, 10, 20, 50, 100, 200, 500, 1000, 0, 0};
        	if (c != null && c.moveToFirst()) {
        		int colorColumn = c.getColumnIndex(KEY_CHIPS_COLOR);
        		int valueColumn = c.getColumnIndex(KEY_CHIPS_VALUE);
        		
	        	do {	
	        		int color = c.getInt(colorColumn);
	        		if (color >= 0 && color < 12) {
	        			result[color] = c.getInt(valueColumn);
	        		}
	        	} while (c.moveToNext());	        	
	    	} 
        	return result;
	    	
	    } finally {
	    	c.close();
	    }
	}
    
    public int[] fetchAllChipsColor(long tournamentRowId) {
    	Cursor c = mDb.query(DATABASE_TABLE_CHIPS, new String[] {KEY_CHIPS_COLOR, KEY_CHIPS_ANDROIDCOLOR}, KEY_CHIPS_TOURNAMENTID + "=" + tournamentRowId, null, null, null, null);
        
        try {
        	int[] result = {Color.WHITE, Color.YELLOW, Color.RED, Color.BLUE, Color.GRAY, Color.GREEN, Color.rgb(255, 127, 0), Color.BLACK, Color.rgb(255, 127, 255), Color.MAGENTA, Color.WHITE, Color.WHITE};
        	if (c != null && c.moveToFirst()) {
        		int colorColumn = c.getColumnIndex(KEY_CHIPS_COLOR);
        		int androidColorColumn = c.getColumnIndex(KEY_CHIPS_ANDROIDCOLOR);
        		
	        	do {	
	        		int color = c.getInt(colorColumn);
	        		if (color >= 0 && color < 10) {
	        			result[color] = c.getInt(androidColorColumn);
	        		}
	        	} while (c.moveToNext());        	
	    	} 
        	return result;
	    	
	    } finally {
	    	c.close();
	    }
	}
    
    public ArrayList<Player> fetchAllPlayers(long tournamentRowId, ContentResolver contentResolver) {
        Cursor c = mDb.query(DATABASE_TABLE_PLAYERS, new String[] {KEY_PLAYERS_ROWID, KEY_PLAYERS_TOURNAMENTID, KEY_PLAYERS_PLAYERID, KEY_PLAYERS_PLAYERNAME, KEY_PLAYERS_TABLE, KEY_PLAYERS_SEAT}, KEY_PLAYERS_TOURNAMENTID + "=" + tournamentRowId, null, null, null, null);
        
        try {
        	ArrayList<Player> result = new ArrayList<Player>(c.getCount());
        	if (c != null && c.moveToFirst()) {
        		int playerIdColumn = c.getColumnIndex(KEY_PLAYERS_PLAYERID);
        		int playerRowIdColumn = c.getColumnIndex(KEY_PLAYERS_ROWID);
        		int playerNameColumn = c.getColumnIndex(KEY_PLAYERS_PLAYERNAME);
        		int playerTableColumn = c.getColumnIndex(KEY_PLAYERS_TABLE);
        		int playerSeatColumn = c.getColumnIndex(KEY_PLAYERS_SEAT);
	        
	            do {
					Player player = new Player();
					player.setId(c.getInt(playerIdColumn));
					player.setRowId(c.getInt(playerRowIdColumn));
					if (player.getId() != -1) {
						Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, player.getId());
						player.setUri(contactUri);
				        InputStream image_stream = Contacts.openContactPhotoInputStream(contentResolver, contactUri);
					    if (image_stream != null) {
					    	player.setPhoto(BitmapFactory.decodeStream(image_stream));
					    } 
					}
					player.setDisplayName(c.getString(playerNameColumn));
					player.setTable(c.getInt(playerTableColumn));
					player.setSeat(c.getInt(playerSeatColumn));
					
					result.add(player);
							
	            } while (c.moveToNext());
        	}
        	
        	return result;
        	
        } finally {
        	c.close();
        }
    }

    public void setPlayers(long tournamentId, List<Player> players) {
        mDb.delete(DATABASE_TABLE_PLAYERS, KEY_PLAYERS_TOURNAMENTID + "=" + tournamentId, null);
        
        for (Player player : players) {
        	ContentValues initialValues = new ContentValues();
            initialValues.put(KEY_PLAYERS_TOURNAMENTID, tournamentId);
            initialValues.put(KEY_PLAYERS_PLAYERID, player.getId());
            initialValues.put(KEY_PLAYERS_PLAYERNAME, player.getDisplayName());
            initialValues.put(KEY_PLAYERS_TABLE, player.getTable());
            initialValues.put(KEY_PLAYERS_SEAT, player.getSeat());
            long id = mDb.insert(DATABASE_TABLE_PLAYERS, null, initialValues);
            player.setRowId(id);
		}
    } 
    
    private void getBuyInAndAddon(Tournament t) {
    	Cursor c = mDb.query(true, DATABASE_TABLE_BUYIN, new String[] {KEY_BUYIN_TOURNAMENTID, KEY_BUYIN_BUYIN, KEY_BUYIN_ADDON, KEY_BUYIN_NBREBUY, KEY_BUYIN_VALREBUY}, KEY_BUYIN_TOURNAMENTID + "=" + t.getId(), null, null, null, null, null);
        try {
  	        if (c != null && c.moveToFirst()) {
  	        	int buyinColumn = c.getColumnIndex(KEY_BUYIN_BUYIN); 
  	            int addonColumn = c.getColumnIndex(KEY_BUYIN_ADDON);
  	            int nbRebuyColumn = c.getColumnIndex(KEY_BUYIN_NBREBUY);
  	            int valRebuyColumn = c.getColumnIndex(KEY_BUYIN_VALREBUY);
  	        
  	        	t.setBuyin(c.getInt(buyinColumn));
  	            t.setAddon(c.getInt(addonColumn));
  	            t.setNbRebuy(c.getInt(nbRebuyColumn));
  	            t.setRebuy(c.getInt(valRebuyColumn));
  	        }        
        } finally {
        	c.close();
        }
    }
    
    public void setBuyInAndAddon(long tournamentId, int buyIn, int addon, int nbRebuy, int rebuy) {
        mDb.delete(DATABASE_TABLE_BUYIN, KEY_PLAYERS_TOURNAMENTID + "=" + tournamentId, null);
        
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_BUYIN_TOURNAMENTID, tournamentId);
        initialValues.put(KEY_BUYIN_BUYIN, buyIn);
        initialValues.put(KEY_BUYIN_ADDON, addon);
        initialValues.put(KEY_BUYIN_NBREBUY, nbRebuy);
        initialValues.put(KEY_BUYIN_VALREBUY, rebuy);
   
        mDb.insert(DATABASE_TABLE_BUYIN, null, initialValues);
    } 
    
    public void setPrizepool(long tournamentRowId, List<Integer> prizepool) {
    	mDb.delete(DATABASE_TABLE_PRIZEPOOL, KEY_PRIZEPOOL_TOURNAMENTID + "=" + tournamentRowId, null);
    	
    	int place = 0;
    	for (Integer percent : prizepool) {
	    	ContentValues initialValues = new ContentValues();
	        initialValues.put(KEY_PRIZEPOOL_TOURNAMENTID, tournamentRowId);
	        initialValues.put(KEY_PRIZEPOOL_PLACE, place);
	        initialValues.put(KEY_PRIZEPOOL_PERCENT, percent);
	        mDb.insert(DATABASE_TABLE_PRIZEPOOL, null, initialValues);
	        place ++;
	    }
    }
    
    public List<Integer> fetchPrizepool(long tournamentRowId) {
        Cursor c = mDb.query(DATABASE_TABLE_PRIZEPOOL, new String[] {KEY_PRIZEPOOL_PLACE, KEY_PRIZEPOOL_PERCENT}, KEY_PRIZEPOOL_TOURNAMENTID + "=" + tournamentRowId, null, null, null, KEY_PRIZEPOOL_PLACE);
        
        try {
        	List<Integer> result = new ArrayList<Integer>();
        	if (c != null && c.moveToFirst()) {
        		int percentColumn = c.getColumnIndex(KEY_PRIZEPOOL_PERCENT);
        		
	        	do {
	        		int percent = c.getInt(percentColumn);
	        		result.add(percent);
	        	} while (c.moveToNext());    	
	    	} 
        	return result;
	    	
	    } finally {
	    	c.close();
	    }
	}
    
    public boolean updateTournamentChipsOptions(Tournament t) {
        ContentValues args = new ContentValues();
        args.put(KEY_TOURNAMENTS_SHOWCHIPS, t.isShowChips());
        args.put(KEY_TOURNAMENTS_BUYINCHIPS, t.getChipsBuyin());
        args.put(KEY_TOURNAMENTS_REBUYCHIPS, t.getChipsRebuy());
        args.put(KEY_TOURNAMENTS_ADDONCHIPS, t.getChipsAddon());

        return mDb.update(DATABASE_TABLE_TOURNAMENTS, args, KEY_TOURNAMENTS_ROWID + "=" + t.getId(), null) > 0;
    }
    
    
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        } 

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + DATABASE_TABLE_TOURNAMENTS 
            		+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL);");
            db.execSQL("ALTER TABLE " + DATABASE_TABLE_TOURNAMENTS + " ADD nbTables INTEGER NOT NULL DEFAULT 0;");
    		db.execSQL("ALTER TABLE " + DATABASE_TABLE_TOURNAMENTS + " ADD nbSeats INTEGER NOT NULL DEFAULT 0;");
    		db.execSQL("ALTER TABLE " + DATABASE_TABLE_TOURNAMENTS + " ADD showChips BOOLEAN NOT NULL DEFAULT 1;");
    		db.execSQL("ALTER TABLE " + DATABASE_TABLE_TOURNAMENTS + " ADD buyinChips INTEGER NOT NULL DEFAULT 1000;");
    		db.execSQL("ALTER TABLE " + DATABASE_TABLE_TOURNAMENTS + " ADD rebuyChips INTEGER NOT NULL DEFAULT 1000;");
    		db.execSQL("ALTER TABLE " + DATABASE_TABLE_TOURNAMENTS + " ADD addonChips INTEGER NOT NULL DEFAULT 1000;");
            
            db.execSQL("CREATE TABLE " + DATABASE_TABLE_STEPS 
            		+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, tournamentId INTEGER NOT NULL, duration INTEGER NOT NULL, type INTEGER NOT NULL, smallBlind INTEGER, bigBlind INTEGER, antes INTEGER, addon BOOLEAN, rebuy BOOLEAN, FOREIGN KEY(tournamentId) REFERENCES tournaments(_id) ON DELETE CASCADE);");
            
            db.execSQL("CREATE TABLE " + DATABASE_TABLE_CHIPS 
            		+ " (tournamentId INTEGER NOT NULL, color INTEGER NOT NULL, value INTEGER NOT NULL);");
            db.execSQL("ALTER TABLE " + DATABASE_TABLE_CHIPS + " ADD androidColor INTEGER NOT NULL DEFAULT -1;");
            
            db.execSQL("CREATE TABLE " + DATABASE_TABLE_PLAYERS 
            		+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, tournamentId INTEGER NOT NULL, playerId INTEGER NOT NULL, playerName TEXT NOT NULL);");
            db.execSQL("ALTER TABLE " + DATABASE_TABLE_PLAYERS + " ADD tabl INTEGER NOT NULL DEFAULT -1;");
            db.execSQL("ALTER TABLE " + DATABASE_TABLE_PLAYERS + " ADD seat INTEGER NOT NULL DEFAULT -1;");
            
            db.execSQL("CREATE TABLE " + DATABASE_TABLE_BUYIN 
            		+ " (tournamentId INTEGER NOT NULL, buyin INTEGER NOT NULL, addon TEXT NOT NULL);");
            db.execSQL("ALTER TABLE " + DATABASE_TABLE_BUYIN + " ADD rebuy INTEGER NOT NULL DEFAULT 0;");
            db.execSQL("ALTER TABLE " + DATABASE_TABLE_BUYIN + " ADD valRebuy INTEGER NOT NULL DEFAULT 0;");
            
            db.execSQL("CREATE TABLE " + DATABASE_TABLE_PRIZEPOOL 
            		+ " (tournamentId INTEGER NOT NULL, place INTEGER NOT NULL, percent INTEGER NOT NULL);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        	if (oldVersion < 15) {
        		db.execSQL("ALTER TABLE " + DATABASE_TABLE_BUYIN + " ADD valRebuy INTEGER NOT NULL DEFAULT 0;");
        		db.execSQL("UPDATE " + DATABASE_TABLE_BUYIN + " SET valRebuy = buyin");
        	}
        	if (oldVersion < 14) {
        		db.execSQL("ALTER TABLE " + DATABASE_TABLE_CHIPS + " ADD androidColor INTEGER NOT NULL DEFAULT -1;");
        		db.execSQL("UPDATE " + DATABASE_TABLE_CHIPS + " SET androidColor = " + Color.WHITE + " WHERE color = 0");
        		db.execSQL("UPDATE " + DATABASE_TABLE_CHIPS + " SET androidColor = " + Color.YELLOW + " WHERE color = 1");
        		db.execSQL("UPDATE " + DATABASE_TABLE_CHIPS + " SET androidColor = " + Color.RED + " WHERE color = 2");
        		db.execSQL("UPDATE " + DATABASE_TABLE_CHIPS + " SET androidColor = " + Color.BLUE + " WHERE color = 3");
        		db.execSQL("UPDATE " + DATABASE_TABLE_CHIPS + " SET androidColor = " + Color.GRAY + " WHERE color = 4");
        		db.execSQL("UPDATE " + DATABASE_TABLE_CHIPS + " SET androidColor = " + Color.GREEN + " WHERE color = 5");
        		db.execSQL("UPDATE " + DATABASE_TABLE_CHIPS + " SET androidColor = " + Color.rgb(255, 127, 0) + " WHERE color = 6");
        		db.execSQL("UPDATE " + DATABASE_TABLE_CHIPS + " SET androidColor = " + Color.BLACK + " WHERE color = 7");
        		db.execSQL("UPDATE " + DATABASE_TABLE_CHIPS + " SET androidColor = " + Color.rgb(255, 84, 164) + " WHERE color = 8");
        		db.execSQL("UPDATE " + DATABASE_TABLE_CHIPS + " SET androidColor = " + Color.rgb(178, 0, 255) + " WHERE color = 9");
        	}
        	if (oldVersion < 13) {
        		db.execSQL("ALTER TABLE " + DATABASE_TABLE_TOURNAMENTS + " ADD showChips BOOLEAN NOT NULL DEFAULT 1;");
        		db.execSQL("ALTER TABLE " + DATABASE_TABLE_TOURNAMENTS + " ADD buyinChips INTEGER NOT NULL DEFAULT 1000;");
        		db.execSQL("ALTER TABLE " + DATABASE_TABLE_TOURNAMENTS + " ADD rebuyChips INTEGER NOT NULL DEFAULT 1000;");
        		db.execSQL("ALTER TABLE " + DATABASE_TABLE_TOURNAMENTS + " ADD addonChips INTEGER NOT NULL DEFAULT 1000;");
        	}
        	if (oldVersion == 9 || oldVersion == 10 || oldVersion == 11) {
        		db.execSQL("ALTER TABLE " + DATABASE_TABLE_TOURNAMENTS + " ADD nbTables INTEGER NOT NULL DEFAULT 0;");
        		db.execSQL("ALTER TABLE " + DATABASE_TABLE_TOURNAMENTS + " ADD nbSeats INTEGER NOT NULL DEFAULT 0;");
        		db.execSQL("ALTER TABLE " + DATABASE_TABLE_PLAYERS + " ADD tabl INTEGER NOT NULL DEFAULT -1;");
        		db.execSQL("ALTER TABLE " + DATABASE_TABLE_PLAYERS + " ADD seat INTEGER NOT NULL DEFAULT -1;");
        		
        	} else if (oldVersion == 8) { 
        		db.execSQL("ALTER TABLE " + DATABASE_TABLE_BUYIN + " ADD rebuy INTEGER NOT NULL DEFAULT 0;");
        		db.execSQL("ALTER TABLE " + DATABASE_TABLE_PLAYERS + " ADD tabl INTEGER NOT NULL DEFAULT -1;");
        		db.execSQL("ALTER TABLE " + DATABASE_TABLE_PLAYERS + " ADD seat INTEGER NOT NULL DEFAULT -1;");
        		db.execSQL("ALTER TABLE " + DATABASE_TABLE_TOURNAMENTS + " ADD nbTables INTEGER NOT NULL DEFAULT 0;");
        		db.execSQL("ALTER TABLE " + DATABASE_TABLE_TOURNAMENTS + " ADD nbSeats INTEGER NOT NULL DEFAULT 0;");
        		
        	} 
//        	else {
//	        	db.execSQL("CREATE TABLE " + DATABASE_TABLE_PLAYERS 
//	            		+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, tournamentId INTEGER NOT NULL, playerId INTEGER NOT NULL, playerName TEXT NOT NULL);");
//	        	db.execSQL("CREATE TABLE " + DATABASE_TABLE_BUYIN 
//	            		+ " (tournamentId INTEGER NOT NULL, buyin INTEGER NOT NULL, addon TEXT NOT NULL);");
//	        	db.execSQL("CREATE TABLE " + DATABASE_TABLE_PRIZEPOOL 
//	            		+ " (tournamentId INTEGER NOT NULL, place INTEGER NOT NULL, percent INTEGER NOT NULL);");
//	        	db.execSQL("ALTER TABLE " + DATABASE_TABLE_BUYIN + " ADD rebuy INTEGER NOT NULL DEFAULT 0;");
//	        	db.execSQL("ALTER TABLE " + DATABASE_TABLE_PLAYERS + " ADD tabl INTEGER NOT NULL DEFAULT -1;");
//	        	db.execSQL("ALTER TABLE " + DATABASE_TABLE_PLAYERS + " ADD seat INTEGER NOT NULL DEFAULT -1;");
//	        	db.execSQL("ALTER TABLE " + DATABASE_TABLE_TOURNAMENTS + " ADD nbTables INTEGER NOT NULL DEFAULT 0;");
//        		db.execSQL("ALTER TABLE " + DATABASE_TABLE_TOURNAMENTS + " ADD nbSeats INTEGER NOT NULL DEFAULT 0;");
//        	}
        	
//            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_TOURNAMENTS);
//            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_STEPS);
//            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_CHIPS);
//            onCreate(db);
        }
        
    }
    
}
