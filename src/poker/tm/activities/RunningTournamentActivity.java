package poker.tm.activities;

import java.util.Collections;
import java.util.List;

import poker.tm.Helper;
import poker.tm.R;
import poker.tm.model.AddonOrRebuy;
import poker.tm.model.Break;
import poker.tm.model.Player;
import poker.tm.model.Round;
import poker.tm.model.Tournament;
import poker.tm.model.TournamentStep;
import poker.tm.services.TimerService;
import poker.tm.services.TimerService.Timer;
import poker.tm.services.TimerService.TimerServiceBinder;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.QuickContact;
import android.provider.Settings;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.Toast;

public class RunningTournamentActivity extends Activity implements Timer {
	
	private static final int STOP_ID   = 0;
	private static final int START_ID  = 1;
	private static final int PAUSE_ID  = 2;
	private static final int TIMER_ID  = 3;
	private static final int PAYOUT_ID = 7;
	
	private static final int REBUY_ID  = 4;
	private static final int OUT_ID	   = 5;
	private static final int CANCEL_ID = 6;
	
	public static final int NOTIFICATION_ID   = 1;
	
	public static final int[] TABLES_COLORS = {Color.parseColor("#550000ff"), Color.parseColor("#5500ff00"), Color.parseColor("#55ff0000"), Color.parseColor("#55ffff00"), Color.parseColor("#55ff00ff"), Color.parseColor("#5500ffff"), Color.parseColor("#55ff5500")};
	
	private static int totalTime = 0;
	private static boolean isPaused = false;
	
	private int[] chipsIds = {
    		R.id.chips0, R.id.chips1, R.id.chips2, 
    		R.id.chips3, R.id.chips4, R.id.chips5, 
    		R.id.chips6, R.id.chips7, R.id.chips8, 
    		R.id.chips9, R.id.chips10, R.id.chips11
    };
	
	private Tournament tournament;
	private boolean checkSeat;
	private int totalMoney = 0, chipsTotal = 0;
	private PowerManager.WakeLock wakeLock;
	private Button playButton;
	private TextView timeText, nextBlindsText, blindsText, totalTimeText, breakText, antesText, pauseText, playerText, averageChipsText;
	private ImageView stepIcon;
	private View currentPlayerView;
	private long timerValue;
	
	private TimerService timerService;
    private boolean timerServiceIsBound = false;
    private ServiceConnection timerServiceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
        	TimerServiceBinder binder = (TimerServiceBinder) service;
            timerService = binder.getService();
            timerServiceIsBound = true;
            RunningTournamentActivity.this.onServiceConnected();
        }

        public void onServiceDisconnected(ComponentName arg0) {
        	timerServiceIsBound = false;
        }
    };
    

	protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	Intent intent = new Intent(this, TimerService.class);    	
        bindService(intent, timerServiceConnection, Context.BIND_AUTO_CREATE);
        
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);		
		if (pref.getBoolean("power_management_off", true)) {
	        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
	        wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "RunningTournamentActivity");
		}
        
        tournament = Helper.getSelectedTournament(this);
        totalTime = 0;
        isPaused = false;
        timerValue = 0;
        
        this.setContentView(R.layout.running_tournament_layout);
		
        View t = this.findViewById(R.id.tournamentLayout);
        t.setMinimumWidth(getResources().getDisplayMetrics().widthPixels * 92 / 100);
        
        if (tournament.isShowChips()) {
        	int[] chipsValues = Helper.getDatabase(this).fetchAllChipsValue(tournament.getId());        
        	int[] chipsColor = Helper.getDatabase(this).fetchAllChipsColor(tournament.getId());
	        for (int i = 0; i < 12; i++) {
				EditText chip = (EditText) this.findViewById(chipsIds[i]);
				chip.setEnabled(false);
				chip.setFocusable(false);
				if (chip != null) {
					if (chipsValues[i] > 0) {
						int color = chipsColor[i];
						chip.setText(Helper.formatBlind(chipsValues[i]));
						Bitmap filter;
						float[] hsv = {0, 0, 0};
						Color.colorToHSV(color, hsv);
						if (hsv[1] < 0.01f && hsv[2] > 0.99f && color != Color.TRANSPARENT) {
							filter = ((BitmapDrawable) getResources().getDrawable(R.drawable.chips_filter2)).getBitmap();
							chip.setTextColor(Color.BLUE);
						} else {
							filter = ((BitmapDrawable) getResources().getDrawable(R.drawable.chips_filter)).getBitmap();
							chip.setTextColor(Color.WHITE);
						}
						Bitmap result = Bitmap.createBitmap(filter.getWidth(), filter.getHeight(), Config.ARGB_8888);
				    	Canvas canvas = new Canvas(result);
				    	Paint p = new Paint();
				    	p.setColor(color);
				    	canvas.drawCircle(1 + filter.getWidth() / 2, 1 + filter.getHeight() / 2, (filter.getWidth() / 2) - 4, p);
				    	canvas.drawBitmap(filter, 0, 0, null);
				    	BitmapDrawable bkg = new BitmapDrawable(getResources(), result);
				    	chip.setBackgroundDrawable(bkg);
					} else {
						chip.setVisibility(EditText.INVISIBLE);
					}
				}
			}
        } else {
        	this.findViewById(R.id.chipsGrid).setVisibility(View.GONE);
        }

		timeText = (TextView) this.findViewById(R.id.timer);
		nextBlindsText = (TextView) this.findViewById(R.id.nextBlindsTextView);
		blindsText = (TextView) this.findViewById(R.id.currentBlindsTextView);
		antesText = (TextView) this.findViewById(R.id.currentAntesTextView);
		totalTimeText = (TextView) this.findViewById(R.id.totalDurationTextView);
		breakText = (TextView) this.findViewById(R.id.nextBreakTextView);
		pauseText = (TextView) this.findViewById(R.id.pauseTextView);
		playButton = (Button) this.findViewById(R.id.playButton);
		stepIcon = (ImageView) this.findViewById(R.id.stepImageView);
		playerText = (TextView) this.findViewById(R.id.playerTextView);
		averageChipsText = (TextView) this.findViewById(R.id.averageStackTextView);
		averageChipsText.setVisibility(View.VISIBLE);
		
		playButton.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				if (isPaused) {
					restartTournament();
				} else {
					pauseTournament();
				}
			}
		});
		
		this.findViewById(R.id.timerButton).setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				openTimerDialog();
			}
		});
		
		this.findViewById(R.id.skipButton).setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				skipToNextStep();
			}
		});
		
		this.findViewById(R.id.stopButton).setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				stopTournament();
			}
		});

        this.displayBlind();
    };
    
    private void onServiceConnected() {
    	timerService.registerTimer(RunningTournamentActivity.this);
    	this.startTournament();
    }
    
    private void showWarningPlayersSeat() {
    	if (!checkSeat) {
			AlertDialog.Builder builder = new AlertDialog.Builder(RunningTournamentActivity.this);
			builder.setCancelable(false);
			builder.setMessage("Some players don't have a seat.\n\nYou should choose a seat for each players before the tournament start.");
			builder.setNegativeButton("Choose seats", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					timerService.stopTimer();					
					NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					mNotificationManager.cancelAll();
					RunningTournamentActivity.this.finish();				
					Intent intent = new Intent(RunningTournamentActivity.this, TablesListActivity.class);
					startActivity(intent);
				}
			});
			builder.setPositiveButton("Continue anyway", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			builder.show();	    	
    	} 
    }
    
    private void refreshTablesList() {
    	if (Helper.getSelectedTournament(this).getPlayers().size() > 0 && checkSeat) {
        	LinearLayout p1 = (LinearLayout) this.findViewById(R.id.TableRow1);
			LinearLayout p2 = (LinearLayout) this.findViewById(R.id.TableRow2);
			LinearLayout p3 = (LinearLayout) this.findViewById(R.id.TableRow3);
			LinearLayout p4 = (LinearLayout) this.findViewById(R.id.TableRow4);
			LinearLayout p5 = (LinearLayout) this.findViewById(R.id.TableRow5);
			LinearLayout p6 = (LinearLayout) this.findViewById(R.id.TableRow6);
			LinearLayout p7 = (LinearLayout) this.findViewById(R.id.TableRow7);
			p1.removeAllViews();
			p2.removeAllViews();
			p3.removeAllViews();
			p4.removeAllViews();
			p5.removeAllViews();
			p6.removeAllViews();
			p7.removeAllViews();
			
			if (Helper.getSelectedTournament(this).getNbTable() * Helper.getSelectedTournament(this).getNbSeat() > 0) {
				for (int i = 0; i < Helper.getSelectedTournament(this).getNbTable(); i++) {
					switch ((i * (Helper.getSelectedTournament(this).getNbSeat() + 1)) % 7) {
						case 0:
							p1.addView(getTableHeaderView(i));
							break;
						case 1:
							p2.addView(getTableHeaderView(i));
							break;
						case 2:
							p3.addView(getTableHeaderView(i));
							break;
						case 3:
							p4.addView(getTableHeaderView(i));
							break;
						case 4:
							p5.addView(getTableHeaderView(i));
							break;
						case 5:
							p6.addView(getTableHeaderView(i));
							break;
						case 6:
							p7.addView(getTableHeaderView(i));
							break;
						default:
							break;
					}
					
					for (int j = 0; j < Helper.getSelectedTournament(this).getNbSeat(); j++) {
						Player player = Helper.getSelectedTournament(this).getPlayer(i, j);
						View p = getPlayerView0(player, i, j);
						switch ((i * (Helper.getSelectedTournament(this).getNbSeat() + 1) + j + 1) % 7) {
							case 0:
								p1.addView(p);
								break;
							case 1:
								p2.addView(p);
								break;
							case 2:
								p3.addView(p);
								break;
							case 3:
								p4.addView(p);
								break;
							case 4:
								p5.addView(p);
								break;
							case 5:
								p6.addView(p);
								break;
							case 6:
								p7.addView(p);
								break;
							default:
								break;
						}
					}
				}
				
				int i = Helper.getSelectedTournament(this).getNbTable() * (Helper.getSelectedTournament(this).getNbSeat() + 1);
				while (i % 7 != 0) {
					switch (i % 7) {
						case 1:
							p2.addView(getEmptyPlayerView0());
							break;
						case 2:
							p3.addView(getEmptyPlayerView0());
							break;
						case 3:
							p4.addView(getEmptyPlayerView0());
							break;
						case 4:
							p5.addView(getEmptyPlayerView0());
							break;
						case 5:
							p6.addView(getEmptyPlayerView0());
							break;
						case 6:
							p7.addView(getEmptyPlayerView0());
							break;
						default:
							break;
					}
					i ++;
				}
			}
        } else {
        	this.findViewById(R.id.playersGrid).setVisibility(View.GONE);
        }
	}

	private View getPlayerView0(Player player, int table, int seat) {
    	View v = LayoutInflater.from(this).inflate(R.layout.player_button, null);
    	TextView nameText = (TextView) v.findViewById(R.id.playerNameTextView);    	
    	TextView moneyText = (TextView) v.findViewById(R.id.playerMoneyTextView);
    	TextView color = (TextView) v.findViewById(R.id.tableColor);
    	color.setText((seat + 1) + "");    			
    	color.setBackgroundColor(TABLES_COLORS[table % TABLES_COLORS.length]);
    	
    	if (player != null) {
    		moneyText.setText(player.getMoneySpend(tournament) + " " + Helper.getCurrency(this));    		
	    	v.setTag(player);
	    	this.registerForContextMenu(v);
	    	nameText.setText(player.getDisplayName());
	    	if (player.getPhoto() != null) {
	    		View photoHolder = v.findViewById(R.id.photoPlayerView);
	    		BitmapDrawable photo = new BitmapDrawable(player.getPhoto());
	    		photo.setBounds(0, 0, (int) (48 * getResources().getDisplayMetrics().density), (int) (48 * getResources().getDisplayMetrics().density));
	    		photoHolder.setBackgroundDrawable(photo);
	    	} else {
	    		View photoHolder = v.findViewById(R.id.photoPlayerView);
	    		Drawable photo = getResources().getDrawable(R.drawable.contact_picture);
	    		photo.setBounds(0, 0, (int) (48 * getResources().getDisplayMetrics().density), (int) (48 * getResources().getDisplayMetrics().density));
	    		photoHolder.setBackgroundDrawable(photo);
	    	}
	    	v.setOnClickListener(new OnClickListener() {			
	 			public void onClick(View v) {
	 				openContextMenu(v);				
	 			}
	 		});
	    	
    	} else {
    		moneyText.setText("");
    		nameText.setText("empty");
    	}
    	
    	return v;
    }
	
	private View getTableHeaderView(int table) {
		View v = LayoutInflater.from(this).inflate(R.layout.table_header, null);
		TextView t = (TextView) v.findViewById(R.id.tableNumTextView);
		t.setText("Table " + (table + 1));
		t.setBackgroundColor(TABLES_COLORS[table % TABLES_COLORS.length]);
		
		return v;
	}
	
	private View getEmptyPlayerView0() {		
    	View v = LayoutInflater.from(this).inflate(R.layout.player_button, null);
    	v.setClickable(false);
    	v.setFocusable(false);
    	TextView nameText = (TextView) v.findViewById(R.id.playerNameTextView);    	
    	TextView moneyText = (TextView) v.findViewById(R.id.playerMoneyTextView);
    	TextView color = (TextView) v.findViewById(R.id.tableColor);
    	color.setText("");    			
    	color.setBackgroundColor(Color.TRANSPARENT);
		moneyText.setText("");    		
    	nameText.setText("");
    	
    	return v;
    }
    
    @Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getTag() != null) {
			currentPlayerView = v;
			Player p = (Player) v.getTag();
			menu.setHeaderTitle(p.getDisplayName());	
			if (tournament.getNbRebuy() > 0) {
				MenuItem i = menu.add(0, REBUY_ID,  0, "Rebuy (" + p.getRebuyUsed() + "/" + tournament.getNbRebuy() + ")");
				i.setEnabled(p.getRebuyUsed() < tournament.getNbRebuy());
			}
			menu.add(0, OUT_ID,    1, "Out");
			menu.add(0, CANCEL_ID, 2, "Cancel");
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {		
		switch (item.getItemId()) {
			case REBUY_ID:
				Player p = (Player) currentPlayerView.getTag();
				totalMoney = totalMoney + tournament.getRebuy();
				chipsTotal = chipsTotal + tournament.getChipsRebuy();
				p.setRebuyUsed(p.getRebuyUsed() + 1);
				this.displayPlayers();
				this.refreshTablesList();
				return true;
			
			case OUT_ID:
				p = (Player) currentPlayerView.getTag();
				int seat = p.getSeat();
				int table = p.getTable();
				p.setPlace(tournament.getRemainingPlayer());
				p.setOut(true);
				this.displayPlayers();
				this.reseatPlayers(seat, table);
				
				return true;
				
			default:
				return super.onContextItemSelected(item);
		}
	}
    
    private void reseatPlayers(int removedSeat, int removedSeatTable) {
    	List<Player> movedPlayers = tournament.optimizeSeat(removedSeat, removedSeatTable);
		if (movedPlayers.size() > 0) {
			String text = "";
			for (Player player : movedPlayers) {
				text = text + player.getDisplayName() + " move to table " + (player.getTable() + 1) + ", seat " + (player.getSeat() + 1) + ".\n";
			}
			final String fuckYouJavaAreYouHappyNow = text;
			this.runOnUiThread(new Runnable() {
				public void run() {
					AlertDialog.Builder builder = new AlertDialog.Builder(RunningTournamentActivity.this);
					builder.setMessage(fuckYouJavaAreYouHappyNow);
					builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
					builder.show();
					refreshTablesList();
				}
			});
		} else {
			refreshTablesList();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		if (this.wakeLock != null) {
			this.wakeLock.acquire();
		}
		
		// for the love of god WHY android WHY :
		final View scroll = this.findViewById(R.id.runningTournamentScrollView);
		scroll.post(new Runnable() {
		    public void run() {
		    	scroll.scrollTo(0, 0);
		    }
		}); 
		scroll.setBackgroundDrawable(Helper.getBackground());
	}
    
    @Override
    protected void onPause() {
    	super.onPause();
    	
    	if (this.wakeLock != null) {
    		this.wakeLock.release();
    	}
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	
    	this.timerService.stopTimer();
    	// Unbind from the service
        if (timerServiceIsBound && isFinishing()) {
            unbindService(timerServiceConnection);
            timerServiceIsBound = false;
        }
    }
    
    @Override
    public void onBackPressed() {
    	stopTournament();
    }
    
	private void startTournament() {
		if (tournament != null && !tournament.getSteps().isEmpty()) {
			totalMoney = 0;
			chipsTotal = 0;
			totalTime = 0;
			isPaused = false;
			tournament.reset();
			//this.timerService.stopTimer();
			checkSeat = tournament.getSeatedPlayer() == tournament.getPlayers().size();
			
			if (tournament.getPlayers().size() > 0) {
				openPlayerList(true);
			} else {
				this.findViewById(R.id.playersGrid).setVisibility(View.GONE);
				this.timerService.startTimer();
				if (tournament.getCurrentStep().getDuration() < 0) {
					pauseTournament();
				}
			}
			
			this.displayTime();
			this.displayStep();
			this.displayPlayers();
			this.showWarningPlayersSeat();
		}
	}
	
	private void pauseTournament() {
		this.runOnUiThread(new Runnable() {
			public void run() {
				pauseText.setVisibility(TextView.VISIBLE);
				playButton.setText("Play");
				playButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.play2, 0, 0, 0);
				isPaused = true;
			}
		});
	}
	
	private void restartTournament() {
		playButton.setText("Pause");
		playButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.pause2, 0, 0, 0);
		isPaused = false;
	}
	
	private void stopTournament() {		
		AlertDialog.Builder builder = new AlertDialog.Builder(RunningTournamentActivity.this);
		builder.setMessage("Do you really want to stop the tournament ?");
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
				
				tournament.setTotalPrize(totalMoney);
				chipsTotal = 0;
				totalMoney = 0;
				totalTime = 0;
				isPaused = false;
				//tournament.stop();
				timerService.stopTimer();
				
				NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				mNotificationManager.cancelAll();
				
				RunningTournamentActivity.this.finish();
				if (tournament.getPlayers().size() > 0) {
					Intent intent = new Intent(RunningTournamentActivity.this, TournamentEndingActivity.class);					
					startActivity(intent);
				} 
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		builder.show();
	}
	
	private void skipToNextStep() {
		AlertDialog.Builder builder = new AlertDialog.Builder(RunningTournamentActivity.this);
		builder.setMessage("Do you really want to skip this step ?");
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				tournament.skipToNextStep();
				displayTime();
				displayStep();
				if (tournament.getCurrentStep().getDuration() < 0) {
					pauseTournament();
				} 
				displayInfo();
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		builder.show();
	}
	
	public void tick() {
		totalTime ++;
		
		if (!isPaused) {
			boolean change = tournament.tick();
			displayTime();			
			if (change) {
				displayStep();
			}			
			if (tournament.getCurrentStep().getDuration() < 0) {
				pauseTournament();
			}  
		}
		
		displayInfo();
	}
	
	private void displayPlayers() {
		if (tournament.getPlayers().size() > 0) {
			int remaining = tournament.getRemainingPlayer();
			playerText.setText(remaining + "/" + tournament.getPlayers().size() + " players, prizepool : " + totalMoney + Helper.getCurrency(this));
			if (remaining > 0) {
				averageChipsText.setText("average stack : " + (chipsTotal / remaining));
			} else {
				averageChipsText.setText("");
			}
		} else {
			playerText.setText("");
			averageChipsText.setText("");
		}
	}
	
	private void displayStep() {
		if (tournament.getCurrentStep() instanceof Round) {
			if (!tournament.isAtFirstStep()) {
				notifyBlind();
			}
			displayBlind();
		
		} else if (tournament.getCurrentStep() instanceof Break) {
			if (!tournament.isAtFirstStep()) {
				notifyBreak();
			}
			displayBreak();
			
		} else if (tournament.getCurrentStep() instanceof AddonOrRebuy) {
			if (!tournament.isAtFirstStep()) {
				notifyAddonOrRebuy();
				
				if (tournament.getPlayers().size() > 0) {
					openPlayerList(false);
				}
			}
			displayAddonOrRebuy();
		}
	}
	
	private void displayAddonOrRebuy() {
		this.runOnUiThread(new Runnable() {
			public void run() {
				blindsText.setText("addon");
				antesText.setText("");
				nextBlindsText.setText("next : " + Helper.formatBlindsShort(tournament.getNextRound()));	
				stepIcon.setImageResource(R.drawable.bank);
			}
		});
	}
	
	private void notifyAddonOrRebuy() {
		notifyUser("Addon", "tap \"Play\" to continue");
	}
	
	private void displayBreak() {
		this.runOnUiThread(new Runnable() {			
			public void run() {
				blindsText.setText("break");
				antesText.setText("");
				nextBlindsText.setText("next : " + Helper.formatBlindsShort(tournament.getNextRound()));	
				stepIcon.setImageResource(R.drawable.watch);
			}
		});
	}
	
	private void notifyBreak() {
		if (tournament.getCurrentStep().getDuration() > 0) {
			notifyUser("Break", "for " + Helper.formatDuration(tournament.getCurrentStep().getDuration()));
			
		} else {
			notifyUser("Break", "tap \"Play\" to continue");
		}
	}
	
	private void displayInfo() {
		if (!isPaused) {
			pauseText.setVisibility(TextView.INVISIBLE);
		}
		totalTimeText.setText("Running since " + Helper.formatDurationShort(totalTime));
		int d = tournament.getDurationBeforeNextBreak();
		if (d > 0) {
			breakText.setText("Next break in " + Helper.formatDurationShort(d));
		} else {
			breakText.setText("");
		}
	}
	
	private void displayTime() {
		int duration = tournament.getDurationBeforeNextStep();
		if (duration >= 0) {
			timeText.setText(Helper.formatTime(duration));
			
		} else {
			timeText.setText("00:00");
		}
	}
	
	private void displayBlind() {
		this.runOnUiThread(new Runnable() {			
			public void run() {
				TournamentStep step = tournament.getCurrentStep();
				if (step instanceof Round) {
					Round r = (Round) step;
					if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
						blindsText.setText(r.getSmallBlind() + "/" + r.getBigBlind());
						if (r.getAntes() > 0) {
							antesText.setText("antes : " + r.getAntes());
						} else {
							antesText.setText("");
						}
					} else {
						String label = r.getSmallBlind() + "/" + r.getBigBlind();
						if (r.getAntes() > 0) {
							label = label + " (+" + r.getAntes() + ")";
						}
						blindsText.setText(label);
					}
					stepIcon.setImageResource(R.drawable.chips_white);
				}
				nextBlindsText.setText("next : " + Helper.formatBlindsShort(tournament.getNextRound()));
			}
		});
	}
	
	private void notifyBlind() {
		Round r = (Round) tournament.getCurrentStep();
		
		String title = "Blinds " + Helper.formatBlinds(r);
		String content = "during " + Helper.formatDuration(r.getDuration());
		notifyUser(title, content);
	}
	
	private void notifyUser(final String title, final String content) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
				
		if (pref.getBoolean("notify_screen", true)) {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "RunningTournamentActivity");
			wl.acquire(5000);
		}
		
		float volume = Float.parseFloat(pref.getString("notify_sound_volume", "0.5"));
		if (volume > 0.0) {
			String sound = pref.getString("ringtone_notify", Settings.System.DEFAULT_NOTIFICATION_URI.toString());
	        if (!sound.equals("")) {
	        	Uri uri = Uri.parse(sound);
				MediaPlayer mp = MediaPlayer.create(this, uri);
				if (mp != null) {
					mp.setVolume(volume, volume);
				    mp.start();
				}
			}
		}
		
		if (pref.getBoolean("notify_notification", true)) {
			Notification notification = new Notification(R.drawable.notify_chips, title, System.currentTimeMillis());
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			
			Intent notificationIntent = new Intent(this, RunningTournamentActivity.class);
			notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); 
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			notification.setLatestEventInfo(this, title, content, contentIntent);
			
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(NOTIFICATION_ID, notification);
		}
		
		if (pref.getBoolean("notify_toast", false)) {
			this.runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(RunningTournamentActivity.this, title + "\n" + content, Toast.LENGTH_SHORT).show();
				}
			});
		}
	}
	
	private void openPlayerList(final boolean buyIn) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final AlertDialog dialog = builder.create();
		dialog.setCancelable(false);
		View v = LayoutInflater.from(this).inflate(R.layout.players_select_layout, null);
		dialog.setView(v);
		if (buyIn) {
			dialog.setTitle("Buy-in");
		} else {
			dialog.setTitle("Addon");
		}		
		
		LinearLayout list = (LinearLayout) v.findViewById(R.id.playersListView);		
		for (Player player : tournament.getPlayers()) {
			if (buyIn || !player.isOut()) {
				list.addView(getPlayerView1(player, buyIn));
				if (buyIn) {
					totalMoney = totalMoney + tournament.getBuyin();
					chipsTotal = chipsTotal + tournament.getChipsBuyin();
				} else {
					player.setAddonUsed(player.getAddonUsed() + 1);
					totalMoney = totalMoney + tournament.getAddon();
					chipsTotal = chipsTotal + tournament.getChipsAddon();
				}
			}
		}
		
		v.findViewById(R.id.ButtonDone).setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				dialog.cancel();	
				displayPlayers();
				
				refreshTablesList();
				if (buyIn) {
					timerService.startTimer();
					if (tournament.getCurrentStep().getDuration() < 0) {
						pauseTournament();
					}
				}
			}
		});
		
		dialog.show();
	}
	
	private View getPlayerView1(final Player contact, final boolean buyIn) {
    	final View v = LayoutInflater.from(this).inflate(R.layout.player_check_item, null);
    	
    	TextView val = (TextView) v.findViewById(R.id.buyIn);
    	if (buyIn) {
    		val.setText(tournament.getBuyin() + " " + Helper.getCurrency(this) + " : ");    		
    	} else {
    		val.setText(tournament.getAddon() + " " + Helper.getCurrency(this) + " : ");
    	}
    	
        TextView name = (TextView) v.findViewById(R.id.playerName);
        name.setText(contact.getDisplayName());
        
        if (contact.getUri() != null) {
	        QuickContactBadge badge =  (QuickContactBadge) v.findViewById(R.id.playerBadge);
	        badge.assignContactUri(contact.getUri());
	        badge.setMode(QuickContact.MODE_MEDIUM);
	        if (contact.getPhoto() != null) {
	        	badge.setImageBitmap(contact.getPhoto());
	        } 
        }
        
        CheckBox checkbox = (CheckBox) v.findViewById(R.id.CheckBox);
        checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					if (buyIn) {
						contact.setPlace(0);
						contact.setOut(false);
						contact.setBuyinUsed(true);
						totalMoney = totalMoney + tournament.getBuyin();
						chipsTotal = chipsTotal + tournament.getChipsBuyin();
					} else {
						contact.setAddonUsed(contact.getAddonUsed() + 1);
						totalMoney = totalMoney + tournament.getAddon();
						chipsTotal = chipsTotal + tournament.getChipsAddon();
					}
				} else {
					if (buyIn) {
						contact.setPlace(tournament.getRemainingPlayer());
						contact.setOut(true);
						contact.setBuyinUsed(false);
						totalMoney = totalMoney - tournament.getBuyin();
						chipsTotal = chipsTotal - tournament.getChipsBuyin();
					} else {
						contact.setAddonUsed(contact.getAddonUsed() - 1);
						totalMoney = totalMoney - tournament.getAddon();
						chipsTotal = chipsTotal - tournament.getChipsAddon();
					}
				}
			}
		});
        
        v.setTag(contact);
        return v;
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuItem i = menu.add(0, STOP_ID,  0, "Stop");
		i.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		i = menu.add(0, TIMER_ID, 3, "Timer");
		i.setIcon(R.drawable.clock);
		i = menu.add(0, PAYOUT_ID, 4, "Payouts");
		i.setIcon(R.drawable.prizepool);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean result = super.onPrepareOptionsMenu(menu);
		
		menu.removeItem(PAUSE_ID);	
		menu.removeItem(START_ID);
		
		if (isPaused) {
			MenuItem i = menu.add(0, START_ID, 1, "Start");
			i.setIcon(R.drawable.play_menu);
		} else {
			MenuItem i = menu.add(0, PAUSE_ID, 2, "Pause");
			i.setIcon(R.drawable.pause);
		}
		
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case STOP_ID:
				stopTournament();
				return true;
				
			case START_ID:
				restartTournament();
				return true;
				
			case PAUSE_ID:
				pauseTournament();
				return true;
			
			case TIMER_ID:
				this.runOnUiThread(new Runnable() {
					public void run() {
						openTimerDialog();
					}
				});
				return true;
				
			case PAYOUT_ID:
				this.runOnUiThread(new Runnable() {
					public void run() {
						openPayoutsDialog();
					}
				});
				return true;
				
			default:
				return super.onOptionsItemSelected(item);
		}
	}
    
	private void openTimerDialog() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		int duration = Integer.parseInt(pref.getString("timer_duration", "60"));	
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final AlertDialog dialog = builder.create();
		View v = LayoutInflater.from(this).inflate(R.layout.timer, null);
		final TextView text = (TextView) v.findViewById(R.id.chrono);
		text.setText(Helper.formatTime(duration));
		dialog.setView(v);
		
		long startAt;
		if (timerValue > 0) {
			startAt = timerValue;
		} else {
			startAt = duration * 1000;
		}
		final CountDownTimer chrono = new CountDownTimer(startAt, 1000) {			
			@Override
			public void onTick(long millisUntilFinished) {
				timerValue = millisUntilFinished;
				text.setText(Helper.formatTime((int) (millisUntilFinished / 1000)));
			}			
			@Override
			public void onFinish() {
				timerValue = 0;
				text.setText("Time!");
			}
		};
		
		v.findViewById(R.id.timerButtonClose).setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				dialog.cancel();
			}
		});
		v.findViewById(R.id.timerButtonStart).setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				chrono.start();	
			}
		});
		
		dialog.setOnCancelListener(new OnCancelListener() {			
			public void onCancel(DialogInterface dialog) {
				chrono.cancel();
				timerValue = 0;			
			}
		});
		
		dialog.show();
		if (timerValue > 0) {
			chrono.start();	
		}
	}
	
	private void openPayoutsDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final AlertDialog dialog = builder.create();
		dialog.setTitle("Current payouts");
		View v = LayoutInflater.from(this).inflate(R.layout.payouts_popup, null);
		dialog.setView(v);			
		LinearLayout list = (LinearLayout) v.findViewById(R.id.playersListView);
		
		List<Player> players = Helper.getSelectedTournament(this).getPlayers();
		Collections.sort(players);
		int place = 0;
		for (Player player : players) {
			addContactPayout(player, place, list);
			place ++;
		}
		
		int money = 0;
		for (int i = list.getChildCount() - 1; i >= 0; i--) {
			TextView dollar = (TextView) list.getChildAt(i).findViewById(R.id.playerPrize);
			if (i > Helper.getSelectedTournament(this).getPrizepool().size() - 1) {
				dollar.setText("0 " + Helper.getCurrency(this));
			} else {
				int percent = Helper.getSelectedTournament(this).getPrizepool().get(i);
				money = money + (percent * totalMoney / 100);
				if (i > 0) {
					dollar.setText((percent * totalMoney / 100) + " " + Helper.getCurrency(this));
				} else {
					// fix pour les erreurs d'arrondi :
					dollar.setText((percent * totalMoney / 100 + (totalMoney - money)) + " " + Helper.getCurrency(this));
				}
				if (percent * totalMoney / 100 > 0) {
		        	dollar.setTextColor(Color.GREEN);
		        }
			}
		}
		
		v.findViewById(R.id.payoutButtonClose).setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				dialog.cancel();
			}
		});
		
		dialog.show();
	}
	
	private void addContactPayout(final Player contact, int place, LinearLayout list) {
    	final View v = LayoutInflater.from(this).inflate(R.layout.player_prize_item, null);
    	TextView p = (TextView) v.findViewById(R.id.playerPlace);
        p.setText((place + 1) + "");
    	
        TextView name = (TextView) v.findViewById(R.id.playerName);
        if (contact.isOut()) {
        	name.setText(contact.getDisplayName());
        } else {
        	name.setText("???");
        }
        
        if (contact.isOut() && contact.getUri() != null) {
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