package poker.tm.services;

import poker.tm.R;
import poker.tm.activities.RunningTournamentActivity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;

public class TimerService extends Service {
	
	// Binder given to clients
    private final IBinder mBinder = new TimerServiceBinder();
	private Timer timer;
    private CountDownTimer countDown = new CountDownTimer(Integer.MAX_VALUE, 1000) {
		@Override
		public void onTick(long millisUntilFinished) {
			timer.tick();
		}			
		@Override
		public void onFinish() {}
	};
    

    public void registerTimer(Timer timer) {
		this.timer = timer;    	
    }
    
    public void startTimer() {
    	if (timer != null) {    		
    		Notification note = new Notification(R.drawable.notify_poker, "Tournament started", System.currentTimeMillis());
    		Intent i = new Intent(this, RunningTournamentActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
			note.setLatestEventInfo(this, "Poker director", "tap to return to the tournament", pi);
			note.flags |= Notification.FLAG_NO_CLEAR;
			
			startService(new Intent(this, TimerService.class));
			startForeground(1486127, note);    		
    		
    		countDown.start();
    	}
    }
    
    public void stopTimer() {
    	countDown.cancel();
    	stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    
    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class TimerServiceBinder extends Binder {
    	public TimerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return TimerService.this;
        }
    }
    
    public interface Timer {
    	public void tick();
    }

}
