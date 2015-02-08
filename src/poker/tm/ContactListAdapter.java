package poker.tm;

import java.io.InputStream;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.TextView;

public class ContactListAdapter extends CursorAdapter implements Filterable {
	
	public static final String[] PEOPLE_PROJECTION = new String[] {
		ContactsContract.Contacts._ID,
        ContactsContract.Contacts.DISPLAY_NAME
    };

	private ContentResolver mContent; 
	
	
    public ContactListAdapter(Context context, Cursor c) {
        super(context, c);
        mContent = context.getContentResolver();
    }
    

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
    	float density = context.getResources().getDisplayMetrics().density;
    	TextView name = new TextView(context);
    	name.setText(cursor.getString(1));
    	name.setTag(cursor.getLong(0));
    	name.setGravity(Gravity.CENTER_VERTICAL);
    	name.setTextColor(Color.BLACK);
    	name.setTextSize(19);
    	name.setHeight((int) (54 * density));
    	
    	Uri contactPhotoUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, cursor.getLong(0));
    	InputStream image_stream = Contacts.openContactPhotoInputStream(mContent, contactPhotoUri);
    	
    	Drawable avatar;
	    if (image_stream != null) {
	    	Bitmap b = BitmapFactory.decodeStream(image_stream);
	    	avatar = new BitmapDrawable(b);
	    } else {
	    	avatar = context.getResources().getDrawable(R.drawable.contact_picture);
	    }
	    
	    avatar.setBounds(0, 0, (int) (54 * density), (int) (54 * density));	    	
    	name.setCompoundDrawablePadding((int) (3 * density));
    	name.setCompoundDrawables(avatar, null, null, null);
	    return name;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
    	// already done in newView
    }
    
    @Override
    public String convertToString(Cursor cursor) {
        return cursor.getString(1);
    }

    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        if (getFilterQueryProvider() != null) {
            return getFilterQueryProvider().runQuery(constraint);
        }

        StringBuilder buffer = null;
        String[] args = null;
        if (constraint != null) {
            buffer = new StringBuilder();
            buffer.append("UPPER(");
            buffer.append(ContactsContract.Contacts.DISPLAY_NAME);
            buffer.append(") GLOB ?");
            args = new String[] { constraint.toString().toUpperCase() + "*" };
        }
        
        return mContent.query(ContactsContract.Contacts.CONTENT_URI, ContactListAdapter.PEOPLE_PROJECTION, buffer == null ? null : buffer.toString(), args, ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
    }
       
}
