package poker.tm.activities;

import poker.tm.Helper;
import poker.tm.R;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class SettingsActivity extends PreferenceActivity implements OnPreferenceChangeListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.preferences);
        
        Preference p = findPreference("timer_duration");
        p.setOnPreferenceChangeListener(this);
        int duration = Integer.parseInt((String) getPreferenceScreen().getSharedPreferences().getString("timer_duration", "30"));
        p.setSummary(Helper.formatDuration(duration));
        
        p = findPreference("background");
        p.setOnPreferenceChangeListener(this);
        p.setSummary(getPreferenceScreen().getSharedPreferences().getString("background", "black"));
        
        p = findPreference("currency");
        p.setOnPreferenceChangeListener(this);
        p.setSummary(getPreferenceScreen().getSharedPreferences().getString("currency", "$"));
        
        p = findPreference("notify_sound_volume");
        p.setOnPreferenceChangeListener(this);
        float volume = Float.parseFloat((String) getPreferenceScreen().getSharedPreferences().getString("notify_sound_volume", "0.5"));
        if (volume <= 0.0f) {
        	p.setSummary("none");
        } else if (volume <= 0.25f) {
			p.setSummary("low");
		} else if (volume >= 1.0f) {
			p.setSummary("loudest");
		} else if (volume >= 0.75f) {
			p.setSummary("louder");
		} else {
			p.setSummary("loud");
		}
        
        p = findPreference("ringtone_notify");
        p.setOnPreferenceChangeListener(this);
        String sound = getPreferenceScreen().getSharedPreferences().getString("ringtone_notify", Settings.System.DEFAULT_NOTIFICATION_URI.toString());
        if (sound.equals("")) {
			p.setSummary("none");
		} else {
			Uri ringtoneUri = Uri.parse(sound);
			Ringtone ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
			if (ringtone != null) {
				p.setSummary(ringtone.getTitle(this));
			}
		}
        
        LinearLayout footer = new LinearLayout(this);        
        footer.setGravity(Gravity.BOTTOM);
		footer.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		footer.addView(getToolbar());		
		LayoutParams layout = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		this.addContentView(footer, layout);
		
		// fuck you android :
		View filler = new View(this);
		filler.setMinimumHeight((int) (45 * getResources().getDisplayMetrics().density));
		filler.setClickable(false);
		filler.setFocusable(false);
		this.getListView().addFooterView(filler);
    }
	
	private View getToolbar() {
		LinearLayout toolbar = new LinearLayout(this);
		toolbar.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		toolbar.setGravity(Gravity.CENTER_VERTICAL);
		toolbar.setBackgroundResource(R.drawable.toolbar);
		toolbar.setMinimumHeight((int) (45 * getResources().getDisplayMetrics().density));
		
		TextView filler = new TextView(this);
		filler.setText("");
		filler.setMinWidth(5);
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
				finish();
			}
		});
		
		TextView title = new TextView(this);
		LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		layout.weight = 1;
		title.setLayoutParams(layout);
		title.setGravity(Gravity.CENTER);
		title.setText("Settings");
		title.setTypeface(Typeface.create((String)null, Typeface.BOLD));
		title.setTextColor(Color.WHITE);
		toolbar.addView(title);
		
		filler = new TextView(this);
		filler.setMinWidth(60);
		toolbar.addView(filler);
		
		return toolbar;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		this.getListView().setBackgroundDrawable(Helper.getBackground());
	}
	
	public boolean onPreferenceChange(Preference pref, Object objValue) {
		if (pref.getKey().equals("timer_duration")) {
			int duration = Integer.parseInt((String) objValue);
			pref.setSummary(Helper.formatDuration(duration));
		
		} else if (pref.getKey().equals("background")) {
			pref.setSummary((String) objValue);
			Helper.setBackground((String) objValue, this);
			this.getListView().setBackgroundDrawable(Helper.getBackground());
			
		} else if (pref.getKey().equals("currency")) {
			pref.setSummary((String) objValue);
			
		} else if (pref.getKey().equals("notify_sound_volume")) {
			float volume = Float.parseFloat((String) objValue);
			if (volume <= 0.0f) {
	        	pref.setSummary("none");
	        } else if (volume <= 0.25f) {
				pref.setSummary("low");
			} else if (volume >= 1.0f) {
				pref.setSummary("loudest");
			} else if (volume >= 0.75f) {
				pref.setSummary("louder");
			} else {
				pref.setSummary("loud");
			}
			
			if (volume > 0.0f) {
				String sound = getPreferenceScreen().getSharedPreferences().getString("ringtone_notify", Settings.System.DEFAULT_NOTIFICATION_URI.toString());
		        if (!sound.equals("")) {
		        	Uri uri = Uri.parse(sound);
					MediaPlayer mp = MediaPlayer.create(this, uri);
					if (mp != null) {
						mp.setVolume(volume, volume);
					    mp.start();
					}
				}
			}
			
		} else if (pref.getKey().equals("ringtone_notify")) {
			if (objValue.equals("")) {
				pref.setSummary("none");
			} else {
				Uri ringtoneUri = Uri.parse((String) objValue);
				Ringtone ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
				if (ringtone != null) {
					pref.setSummary(ringtone.getTitle(this));
				}
			}
		}
		
		return true;
	}
	
}
