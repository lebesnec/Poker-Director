package poker.tm.model;

public class Break extends TournamentStep {
	
	private int duration = -1;

	
	public int getDuration() {
		return duration;
	}
	
	public void setDuration(int duration) {
		this.duration = duration;
	}
	
	@Override
	public String getDescription() {
		return "break";
	}

}
