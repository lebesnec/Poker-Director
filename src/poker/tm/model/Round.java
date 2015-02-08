package poker.tm.model;

import poker.tm.Helper;

public class Round extends TournamentStep implements Cloneable {
	
	private int smallBlind = 1;
	private int bigBlind = 2;
	private int antes = 0;
	private int duration = 60 * 10;
	
	
	public Round() {}
	
	public Round(int smallBlind, int bigBlind, int antes, int duration) {
		this.smallBlind = smallBlind;
		this.bigBlind = bigBlind;
		this.antes = antes;		
		this.duration = duration;
	}
	
	public Round(int smallBlind, int bigBlind, int antes) {
		this(smallBlind, bigBlind, antes, 60 * 10);	
	}
	
	public Round(int smallBlind, int bigBlind) {
		this(smallBlind, bigBlind, 0);
	}
	
	public Round(int smallBlind) {	
		this(smallBlind, 2 * smallBlind);
	}
	
	/**
	 * create a round by doubling the blind and keeping the same duration.
	 */
	public Round(Round r) { 
		if (this.bigBlind < 1000000000) { // on évite de dépasser la taille max des int
			this.bigBlind = r.getBigBlind() * 2;
		} else {
			this.bigBlind = r.getBigBlind();
		}
		
		if (this.bigBlind > 0) {
			// this will round blind like 40/80 to 50/100 :
			String st = Integer.toString(this.bigBlind);
			int length = st.length();
			String s = "1";
			for (int i = 0; i < length; i++) {
				s = s.concat("0");
			}
			int nextRoundNumber = Integer.parseInt(s);
			if (100 * this.bigBlind / nextRoundNumber >= 80) {
				this.bigBlind = nextRoundNumber; 
			}
			
			// this will round blind like 512/1024 to 500/1000 :
			s = st.charAt(0) + "";
			for (int i = 0; i < length - 1; i++) {
				s = s.concat("0");
			}
			int previousRoundNumber = Integer.parseInt(s);
			if (100 * previousRoundNumber / this.bigBlind >= 80) {
				this.bigBlind = previousRoundNumber; 
			}
			
			// rounding some special case :
			if (this.bigBlind == 128) {
				this.bigBlind = 120;
			}
		}
		
		this.smallBlind = this.bigBlind / 2;
		this.antes = r.getAntes() * 2; 
		this.duration = r.getDuration();
	}
	
	@Override
	public Round clone() {
		try {
			return (Round) super.clone();
			
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	public int getDuration() {
		return duration;
	}
	
	public void setDuration(int duration) {
		this.duration = duration;
	}

	public void setSmallBlind(int smallBlind) {
		this.smallBlind = smallBlind;
	}

	public int getSmallBlind() {
		return smallBlind;
	}

	public void setBigBlind(int bigBlind) {
		this.bigBlind = bigBlind;
	}

	public int getBigBlind() {
		return bigBlind;
	}

	public int getAntes() {
		return antes;
	}
	
	public void setAntes(int antes) {
		this.antes = antes;
	}
	
	@Override
	public String getDescription() {
		return Helper.formatBlindsShort(this);
	}
	
}
