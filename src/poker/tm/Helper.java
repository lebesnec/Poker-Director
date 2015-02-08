package poker.tm;

import java.util.ArrayList;

import poker.tm.model.Round;
import poker.tm.model.Tournament;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.preference.PreferenceManager;

public final class Helper {
	
	private static BitmapDrawable background;
	private static DatabaseAccess database;	
	private static ArrayList<Tournament> tournaments = new ArrayList<Tournament>();
	private static Tournament selectedTournament;

	private Helper() {}
	
	
	/**
	 * @param duration a duration in second
	 * @return (H hour(s)(, M minute(s) (and S second(s))))
	 */
	public static String formatDuration(int duration) {
		if (duration <= 0) {
			return "0 second";
		}
		
		int s = duration % 60;
		int m = ((duration - s) / 60) % 60;
		int h = (duration - (m * 60) - s) / 3600;
		
		String resultH = "";
		if (h == 1) {
			resultH = "1 hour ";
		
		} else if (h > 0) {
			resultH = h + " hours ";
		} 
		
		String resultM = "";
		if (m == 1) {
			resultM = "1 minute ";
		
		} else if (m > 0) {
			resultM = m + " minutes ";
		}
		
		String resultS = "";
		if (s == 1) {
			resultS = "1 second ";
		
		} else if (s > 0) {
			resultS = s + " seconds ";
		}
		
		String result = resultH;
		if (resultH != "" && resultM != "" && resultS != "") {
			result = result + ","; 
		
		} else if (resultH != "" && resultM != "" && resultS == "") {
			result = result + "and "; 
		}
		result = result + resultM;		
		if (result != "" && resultS != "") {
			result = result + "and ";
		}
		
		return result + resultS;
	}
	
	/**
	 * @param duration a duration in second
	 * @return (H h( M m ( S s)))
	 */
	public static String formatDurationShort(int duration) {
		if (duration > 0) {
			int s = duration % 60;
			int m = ((duration - s) / 60) % 60;
			int h = (duration - (m * 60) - s) / 3600;
			
			String result = "";
			if (h > 0) {
				result = h + "h "; 		
			} 	
			if (m > 0) {
				result = result + m + "m "; 		
			} 	
			if (s > 0) {
				result = result + s + "s";
			}
			
			return result;
		
		} else {
			return "||";
		}
	}
	
	/**
	 * @param duration a duration in second
	 * @return M m
	 */
	public static String formatDurationMinutes(int duration) {
		if (duration >= 0) {
			int s = duration % 60;
			int m = (duration - s) / 60;
			
			return m + "m";
			
		} else {
			return "||";
		}
	}
	
	/**
	 * @param duration a time in second
	 * @return MM:SS
	 */
	public static String formatTime(int time) {
		int s = time % 60;
		int m = (time - s) / 60;
		
		return (m < 10 ? "0" + m : m) + ":" + (s < 10 ? "0" + s : s);
	}
	
	/**
	 * @return SM/BB (antes : A)
	 */
	public static String formatBlinds(Round r) {
		String result = r.getSmallBlind() + "/" + r.getBigBlind();
		if (r.getAntes() > 0) {
			result = result + " (antes : " + r.getAntes() + ")";
		}
		
		return result;
	}
	
	/**
	 * @return SM K/BB K (+ A K)
	 */
	public static String formatBlindsShort(Round r) {
		String result = formatBlind(r.getSmallBlind()) + "/" + formatBlind(r.getBigBlind());
		if (r.getAntes() > 0) {
			result = result + " (+ " + formatBlind(r.getAntes()) + ")";
		}
		
		return result;
	}
	
	/**
	 * @return X K
	 */
	public static String formatBlind(int blind) {
		if (blind >= 1000 && blind % 1000 == 0) {
			return (blind / 1000) + "K";
		
		} else {		
			return blind + "";
		}
	}
	
	public static void setBackground(String color, Activity a) {
		if (color.equals("green"))
			Helper.background = (BitmapDrawable) a.getResources().getDrawable(R.drawable.background_green);
		else if (color.equals("blue"))
			Helper.background = (BitmapDrawable) a.getResources().getDrawable(R.drawable.background_blue);
		else if (color.equals("red"))
			Helper.background = (BitmapDrawable) a.getResources().getDrawable(R.drawable.background_red);
		else
			Helper.background = (BitmapDrawable) a.getResources().getDrawable(R.drawable.background_black);
			
		Helper.background.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);	
	}
	
	public static BitmapDrawable getBackground() { 
        return Helper.background;
	}
	
	public static void emptyData() {
		tournaments = null;
		selectedTournament = null;
		database = null;
	}
	
	public static DatabaseAccess getDatabase(Activity a) {
		if (database == null) {
			database = new DatabaseAccess(a);
			if (!database.isOpen()) {
				database.open();
			}
		}
		
		return database;
	}
	
	public static ArrayList<Tournament> getTournaments(Activity a) {
		if (tournaments == null) {
			tournaments = getDatabase(a).fetchAllTournaments(a.getContentResolver());
		}
		
		return tournaments;
	}
	
	public static void refreshTournaments() {
		tournaments = null;
	}
	
	public static Tournament getSelectedTournament(Activity a) {
		if (selectedTournament == null) {
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(a);
			int selectedId = pref.getInt("selected.tounament.id", -1);
			if (selectedId == -1) {
				throw new IllegalArgumentException("Tournament not found");
			}
	        for (Tournament t : getTournaments(a)) {
				if (t.getId() == selectedId) {
					selectedTournament = t;
					break;
				} 
			}
		}
		
		return selectedTournament;
	}
	
	public static boolean setSelectedTournament(Activity a, int position) {
		selectedTournament = Helper.getTournaments(a).get(position);
		
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(a);
		return pref.edit().putInt("selected.tounament.id", selectedTournament.getId()).commit();
	}
	
	public static boolean setSelectedTournament(Activity a, Tournament t) {
		selectedTournament = t;
		
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(a);
		return pref.edit().putInt("selected.tounament.id", t.getId()).commit();
	}
	
	public static String getCurrency(Activity a) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(a);
	    return pref.getString("currency", "$");
	}

}
