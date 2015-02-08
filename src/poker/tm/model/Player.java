package poker.tm.model;

import android.graphics.Bitmap;
import android.net.Uri;

/**
 * A model object containing contact data.
 */
public class Player implements Comparable<Player> {

	private long id = -1;
	private long rowId = -1;
	private Uri mUri;
    private String mDisplayName;
    private String mPhoneNumber;
    private Bitmap mPhoto;
    private boolean buyinUsed = true;
    private int rebuyUsed = 0;
    private int addonUsed = 0;
    private boolean out = false;
    private int place = 0;
    private int table = -1;
    private int seat = -1;

    
    public void setDisplayName(String displayName) {
        this.mDisplayName = displayName;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.mPhoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }
    
    public void setUri(Uri uri) {
        this.mUri = uri;
    }

    public Uri getUri() {
        return mUri;
    }
    
    public Bitmap getPhoto() {
		return mPhoto;
	}
    
    public void setPhoto(Bitmap photo) {
		this.mPhoto = photo;
	}
    
    public long getId() {
		return id;
	}
    
    public void setId(long id) {
		this.id = id;
	}

	public void setOut(boolean out) {
		this.out = out; 
	}

	public boolean isOut() {
		return out;
	}
	
	public long getRowId() {
		return rowId;
	}
	
	public void setRowId(long rowId) {
		this.rowId = rowId;
	}
	
	public int getPlace() {
		return place;
	}
	
	public void setPlace(int place) {
		this.place = place;
	}
	
	public int getRebuyUsed() {
		return rebuyUsed;
	}
	
	public void setRebuyUsed(int rebuyUsed) {
		this.rebuyUsed = rebuyUsed;
	}
	
	public int getAddonUsed() {
		return addonUsed;
	}
	
	public void setAddonUsed(int addonUsed) {
		this.addonUsed = addonUsed;
	}
	
	public void setBuyinUsed(boolean buyinUsed) {
		this.buyinUsed = buyinUsed;
	}
	
	public boolean isBuyinUsed() {
		return buyinUsed;
	}
	
	public int getMoneySpend(Tournament t) {
		return ((isBuyinUsed() ? 1 : 0) * t.getBuyin()) + (getRebuyUsed() * t.getRebuy()) + (getAddonUsed() * t.getAddon());
	}

	public int compareTo(Player another) {
		return this.place - another.place;
	}
	
	public int getTable() {
		if (out) {
			return -1;
		} else {
			return table;
		}
	}
	
	public void setTable(int table) {
		this.table = table;
	}
	
	public int getSeat() {
		if (out) {
			return -1;
		} else {
			return seat;
		}
	}
	
	public void setSeat(int seat) {
		this.seat = seat;
	}
    
}