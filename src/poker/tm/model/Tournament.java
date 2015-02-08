package poker.tm.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import poker.tm.Helper;
import poker.tm.PlayersSeatComparator;

/**
 * a poker tournament, which is a list of round, break or addon/rebuy ("steps")
 */
public class Tournament {
	
	private List<Player> players = new ArrayList<Player>();
	private int nbTable = 0, nbSeat = 0;
	private int buyin = 10, addon = 10, rebuy = 0;
	private int nbRebuy = 0;
	private List<Integer> prizepool = new ArrayList<Integer>();
	private int totalPrize = 0;
	private int id;
	private String name;
	private ArrayList<TournamentStep> steps = new ArrayList<TournamentStep>();
	private int currentTimeInStep = 0;
	private int currentStepPosition = 0;
	private boolean showChips = true;
	private int chipsBuyin, chipsRebuy, chipsAddon;

	
	public void setSteps(ArrayList<TournamentStep> steps) {
		this.steps = steps;
	}

	public ArrayList<TournamentStep> getSteps() {
		return steps;
	}
	
	public void addStep(TournamentStep step) {
		this.steps.add(step);
	}
	
	public void setCurrentTimeInStep(int currentTimeInStep) {
		this.currentTimeInStep = currentTimeInStep;
	}
	
	/**
	 * @return number of seconds since the beginning of the current step
	 */
	public int getCurrentTimeInStep() {
		return currentTimeInStep;
	}
	
	/**
	 * @return true if we are during the first step of the tournament
	 */
	public boolean isAtFirstStep() {
		return currentStepPosition == 0;
	}
	
	public TournamentStep getCurrentStep() {
		if (currentStepPosition < steps.size()) {
			return steps.get(currentStepPosition);
		
		} else {		
			return this.addRound();
		}
	}
	
	/**
	 * @return the next round (which could be after a break or addon/rebuy)
	 */
	public Round getNextRound() {
		for (int i = currentStepPosition + 1; i < steps.size(); i++) {
			TournamentStep step = steps.get(i);
			if (step instanceof Round) {
				return (Round) step;
			}
		}
			
		return this.addRound();
	}
	
	/**
	 * create a new round by doubling the blind.
	 * @return the created round
	 */
	public Round addRound() {
		Round lastRound = getLastRound();
		Round newRound;
		
		if (lastRound != null) {
			newRound = new Round(lastRound);
		} else {
			newRound = new Round();
		}
		
		this.addStep(newRound);
		return newRound;
	}
	
	private Round getLastRound() {
		for (int i = steps.size() - 1; i >= 0; i--) {
			if (steps.get(i) instanceof Round) {
				return (Round) steps.get(i);
			}
		}
		
		return null;
	}
	
	/**
	 * @return number of seconds before the next step, or -1 if unable to determine
	 */
	public int getDurationBeforeNextStep() {
		if (getCurrentStep().getDuration() > 0) {
			return getCurrentStep().getDuration() - currentTimeInStep;
		}
		
		return -1;
	}
	
	/**
	 * @return number of seconds before the next break, or -1 if no break found.
	 * Please note that the steps with unknown duration (== -1) will not be added. 
	 */
	public int getDurationBeforeNextBreak() {		
		int total = getDurationBeforeNextStep();
		
		for (int i = currentStepPosition + 1; i < steps.size(); i++) {
			if (steps.get(i) instanceof Break) {
				return total;
				
			} else {
				if (steps.get(i).getDuration() > 0) {
					total = total + steps.get(i).getDuration();
				}
			}
		}
		
		return -1;
	}
	
	/**
	 * @return number of seconds before the next addon/rebuy, or -1 if no addon/rebuy found.
	 * Please note that the steps with unknown duration (== -1) will not be added. 
	 */
	public int getDurationBeforeNextAddonOrRebuy() {		
		int total = getDurationBeforeNextStep();
		
		for (int i = currentStepPosition + 1; i < steps.size(); i++) {
			if (steps.get(i) instanceof AddonOrRebuy) {
				return total;
				
			} else {
				if (steps.get(i).getDuration() > 0) {
					total = total + steps.get(i).getDuration();
				}
			}
		}
		
		return -1;
	}
	
	/**
	 * @return the time in seconds since the beginning of the tournament, ignoring pause.
	 */
	public int getCurrentTime() {
		int total = 0;
		
		for (int i = 0; i < currentStepPosition - 1; i++) {
			if (steps.get(i).getDuration() > 0) {
				total = total + steps.get(i).getDuration();
			}
		}
		
		return total + currentTimeInStep;
	}
	
	public int getCurrentSmallBlind() {
		if (getCurrentStep() instanceof Round) {
			Round round = (Round) getCurrentStep();
			return round.getSmallBlind();
		
		} else {
			return getNextRound().getSmallBlind();
		}
	}
	
	public int getCurrentBigBlind() {
		if (getCurrentStep() instanceof Round) {
			Round round = (Round) getCurrentStep();
			return round.getBigBlind();
		
		} else {
			return getNextRound().getBigBlind();
		}
	}
	
	public int getCurrentAntes() {
		if (getCurrentStep() instanceof Round) {
			Round round = (Round) getCurrentStep();
			return round.getAntes();
		
		} else {
			return getNextRound().getAntes();
		}
	}
	
	public int getNextSmallBlind() {
		return getNextRound().getSmallBlind();
	}
	
	public int getNextBigBlind() {
		return getNextRound().getBigBlind();
	}
	
	public int getNextAntes() {
		return getNextRound().getAntes();
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
	
	public String getDescription() {
		String result = "";
		
		if (players.size() > 1) {
			result = players.size() + " players - ";
		} else {
			result = players.size() + " player - ";
		}
		
		for (TournamentStep step : steps) {
			if (step instanceof Round) {
				Round round = (Round) step;
				result = result + Helper.formatBlindsShort(round) + " • ";				
			}
		}
		
		if (result.endsWith(" • ")) {
			result = result.substring(0, result.length() - 3);
		}
		
		return result;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getBuyin() {
		return buyin;
	}
	
	public void setBuyin(int buyin) {
		this.buyin = buyin;
	}
	
	public int getAddon() {
		return addon;
	}
	
	public void setAddon(int addon) {
		this.addon = addon;
	}
	
	public int getNbRebuy() {
		return nbRebuy;
	}
	
	public void setNbRebuy(int nbRebuy) {
		this.nbRebuy = nbRebuy;
	}
	
	public int getRebuy() {
		return rebuy;
	}
	
	public void setRebuy(int rebuy) {
		this.rebuy = rebuy;
	}
	
	public int getEstimateTotalPrize() {
		int nbPlayers = getPlayers().size();
		return (nbPlayers * getBuyin()) + (nbPlayers * getAddon()) + (nbPlayers * getNbRebuy() * getRebuy());
	}
	
	public List<Player> getPlayers() {
		return players;
	}
	
	public int getRemainingPlayer() {
		int i = 0;
		for (Player p : getPlayers()) {
			if (!p.isOut()) {
				i ++;
			}
		}
		
		return i;
	}
	
	public void setPlayers(List<Player> players) {
		this.players = players;
	}
	
	public int getSeatedPlayer() {
		int i = 0;
		for (Player p : getPlayers()) {
			if (p.getSeat() != -1) {
				i ++;
			}
		}
		
		return i;
	}
	
	public void unseatAllPlayers() {
		for (Player p : getPlayers()) {
			p.setTable(-1);
			p.setSeat(-1);
		}
	}
	
	private boolean canRemoveTable(int nbTableNotEmpty) {
		int nbPlayers = 0;
		for (Player p : getPlayers()) {
			if (!p.isOut()) {
				nbPlayers ++;
			}
		}
		
		return nbPlayers > 0 && (nbTableNotEmpty - 1) * nbSeat >= nbPlayers;
	}
	
	public List<Player> optimizeSeat(int removedSeat, int removedSeatTable) {
		int nbTableNotEmpty = 0;
		List<List<Player>> tables = new ArrayList<List<Player>>();		
		for (int i = 0; i < nbTable; i++) {
			List<Player> seats = new ArrayList<Player>();
			tables.add(seats);
			for (int j = 0; j < nbSeat; j++) {
				Player p = getPlayer(i, j);
				if (p != null) {
					seats.add(p);
				}
			}
			if (!seats.isEmpty()) {
				nbTableNotEmpty ++;
			}
		}
		
		if (!canRemoveTable(nbTableNotEmpty)) {
			// équilibrage du nombre de joueurs au tables :
			int nbPlayerAtTable = tables.get(removedSeatTable).size();
			if (nbPlayerAtTable > 0) {
				for (List<Player> seats : tables) {
					if (seats.size() - nbPlayerAtTable == 2) {
						Player playerToMove = seats.get(seats.size() - 1);
						playerToMove.setSeat(removedSeat);
						playerToMove.setTable(removedSeatTable);
						List<Player> result = new ArrayList<Player>(1);
						result.add(playerToMove);
						return result;
					}
				}
			}
			return Collections.emptyList();
		}
		
		// suppression d'une table et déplacement de ses joueurs :
		List<Player> playersToMove = null;
		for (List<Player> seats : tables) {
			if (!seats.isEmpty()) {
				if (playersToMove == null || playersToMove.size() >= seats.size()) {
					playersToMove = seats;
				}
			}
		}
		
		PlayersSeatComparator comp = new PlayersSeatComparator();
		for (Player player : playersToMove) {
			int nbTable = 0;
			List<Player> destination = null;	
			for (List<Player> seats : tables) {
				if (player.getTable() != nbTable && (destination == null || (destination.size() > seats.size() && !seats.isEmpty()))) {
					destination = seats;
				}
				nbTable ++;
			}
			
			Collections.sort(destination, comp);
			int nbSeat = 0;
			for (Player p : destination) {
				if (p.getSeat() > nbSeat) {
					player.setSeat(nbSeat);
					player.setTable(p.getTable());
					break;
				} else if (nbSeat == destination.size() - 1) {
					player.setSeat(p.getSeat() + 1);
					player.setTable(p.getTable());
					break;
				}
				nbSeat ++;
			}
			destination.add(player);
		}
		
		return playersToMove;
	}
	
	public Player getPlayer(int table, int seat) {
		for (Player p : getPlayers()) {
			if (!p.isOut() && p.getTable() == table && p.getSeat() == seat) {
				return p;
			}
		}
		
		return null;
	}
	
	public List<Integer> getPrizepool() {
		return prizepool;
	}
	
	public void setPrizepool(List<Integer> prizepool) {
		this.prizepool = prizepool;
	}
	
	public int getTotalPrize() {
		return totalPrize;
	}
	
	public void setTotalPrize(int totalPrize) {
		this.totalPrize = totalPrize;
	}
	
	public int getNbSeat() {
		return nbSeat;
	}
	
	public void setNbSeat(int nbSeat) {
		this.nbSeat = nbSeat;
	}
	
	public int getNbTable() {
		return nbTable;
	}
	
	public void setNbTable(int nbTable) {
		this.nbTable = nbTable;
	}
	
	public boolean isShowChips() {
		return showChips;
	}

	public void setShowChips(boolean showChips) {
		this.showChips = showChips;
	}

	public int getChipsBuyin() {
		return chipsBuyin;
	}

	public void setChipsBuyin(int chipsBuyin) {
		this.chipsBuyin = chipsBuyin;
	}

	public int getChipsRebuy() {
		return chipsRebuy;
	}

	public void setChipsRebuy(int chipsRebuy) {
		this.chipsRebuy = chipsRebuy;
	}

	public int getChipsAddon() {
		return chipsAddon;
	}

	public void setChipsAddon(int chipsAddon) {
		this.chipsAddon = chipsAddon;
	}

	/**
	 * Add one second to the timer, and change the step if necessary.
	 * @return true if the step has changed.
	 */
	public boolean tick() {
		currentTimeInStep ++;
		
		if (currentTimeInStep > getCurrentStep().getDuration()) {
			currentTimeInStep = 0;
			currentStepPosition ++;
			
			return true;
			
		} else {
			return false;
		}
	}
	
	public void skipToNextStep() {
		currentTimeInStep = 0;
		currentStepPosition ++;
	}
	
	public void reset() {
		currentTimeInStep = 0;
		currentStepPosition = 0;
		totalPrize = 0;
		for (Player p : getPlayers()) {
			p.setOut(false);
			p.setPlace(0);
			p.setRebuyUsed(0);
		}
	}

}
