package poker.tm.model;

public class AddonOrRebuy extends TournamentStep {
	
	private boolean isAddon = true;
	private boolean isRebuy = false;

	
	public AddonOrRebuy() {}
	
	public AddonOrRebuy(boolean isAddon, boolean isRebuy) {
		this.isAddon = isAddon;
		this.isRebuy = isRebuy;
	}
	

	public int getDuration() {
		return -1;
	}

	public void setAddon(boolean isAddon) {
		this.isAddon = isAddon;
	}

	public boolean isAddon() {
		return isAddon;
	}

	public void setRebuy(boolean isRebuy) {
		this.isRebuy = isRebuy;
	}

	public boolean isRebuy() {
		return isRebuy;
	}
	
	public boolean isAddonAndRebuy() {
		return isAddon && isRebuy;
	}
	
	@Override
	public String getDescription() {
		return "addon";
	}

}
