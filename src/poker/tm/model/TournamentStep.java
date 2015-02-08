package poker.tm.model;

public abstract class TournamentStep {
	
	private int id;
	
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * @return the duration in seconds of this step, or -1 if the duration is unknown 
	 * (this means that the timer will pause at this step)
	 */
	public abstract int getDuration();
	
	public String getDescription() {
		return "";
	}

}
