package com.googlecode.gtalksms.cmd.smsCmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.googlecode.gtalksms.Log;
import com.googlecode.gtalksms.SettingsManager;
import com.googlecode.gtalksms.data.contacts.ContactsManager;
import com.googlecode.gtalksms.tools.Tools;

/**
 * Helper to manage MMS from the Android API
 * 
 * @source: http://stackoverflow.com/questions/3012287/how-to-read-mms-data-in-android
 * @author Florent L.
 */
public class MmsManager {

    private Context _context;
    private SettingsManager _settings;
    
    private static final Uri MMS_CONTENT_URI = Uri.parse("content://mms");
    private static final Uri MMS_INBOX_CONTENT_URI = Uri.withAppendedPath(MMS_CONTENT_URI, "inbox");
    private static final Uri MMS_PART_CONTENT_URI = Uri.withAppendedPath(MMS_CONTENT_URI, "part");
    
    private static final String SORT_ORDER = "date DESC";

    public MmsManager(Context baseContext) {
        _settings = SettingsManager.getSettingsManager(baseContext);
        _context = baseContext;
        Log.initialize(_settings);
    }
    
    /**
     * Retrieve the X last MMS
     * TODO: To be tested with older Android's SDK
     * 
     * @param nbMMS Number of MMS to retrieve
     * @return The list of MMS
     */
    public ArrayList<Mms> getLastReceivedMmsDetails(int nbMMS) {
        ArrayList<Mms> allMms = new ArrayList<Mms>();
        
        // Looking for the last unread MMS
        Cursor cursor = _context.getContentResolver().query(MMS_INBOX_CONTENT_URI, new String[] { "_id", "sub", "read", "date", "m_id" }, null, null, SORT_ORDER);
        if (cursor != null) {
            try {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    int count = 0;
                    do {
                    	Mms mms = new Mms(cursor.getString(1), Tools.getDateMilliSeconds(cursor, "date"), cursor.getString(4), getSender(cursor.getInt(0)));
                        
                        // Read the content of the MMS
                        Cursor cPart = _context.getContentResolver().query(MMS_PART_CONTENT_URI, null, "mid = " + cursor.getString(0), null, null);
                        if (cPart.moveToFirst()) {
                            do {
                                // Dump all fields into the logs
                                // Log.dump("MMS: ", cPart);
                                String partId = cPart.getString(cPart.getColumnIndex("_id"));
                                String type = cPart.getString(cPart.getColumnIndex("ct"));
                                if ("text/plain".equals(type)) {
                                    String data = cPart.getString(cPart.getColumnIndex("_data"));
                                    if (data != null) {
                                        mms.appendMessage(getMmsText(partId));
                                    } else {
                                        mms.appendMessage(cPart.getString(cPart.getColumnIndex("text")));
                                    }
                                } else if ("image/jpeg".equals(type) || "image/bmp".equals(type) || "image/gif".equals(type) || "image/jpg".equals(type) || "image/png".equals(type)) {
                                    Bitmap bitmap = getMmsImage(partId);
                                    mms.setMessage("Image Size: " + bitmap.getWidth() + "x" + bitmap.getHeight());
                                    mms.setBitmap(bitmap);
                                }
                            } while (cPart.moveToNext());
                        }
                        if (cPart != null) {
                            cPart.close();
                        }
                        
                        allMms.add(mms);
                    } while (cursor.moveToNext() && count++ < nbMMS);
                }
            } finally {
                cursor.close();
            }
        }
        
        return allMms;
    }
    
    private String getMmsText(String id) {
        InputStream is = null;
        StringBuilder sb = new StringBuilder();
        try {
            is = _context.getContentResolver().openInputStream(Uri.withAppendedPath(MMS_PART_CONTENT_URI, id));
            if (is != null) {
                InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                BufferedReader reader = new BufferedReader(isr);
                String temp = reader.readLine();
                while (temp != null) {
                    sb.append(temp);
                    temp = reader.readLine();
                }
            }
        } catch (IOException e) {
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {}
            }
        }
        return sb.toString();
    }

    private Bitmap getMmsImage( String _id) {
        Uri partURI = Uri.parse("content://mms/part/" + _id);
        InputStream is = null;
        Bitmap bitmap = null;
        try {
            is = _context.getContentResolver().openInputStream(partURI);
            bitmap = BitmapFactory.decodeStream(is);
        } catch (IOException e) {
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {}
            }
        }
        return bitmap;
    }
    
    private String getSender(int id) {
        String selectionAdd = new String("msg_id = " + id);
        String uriStr = MessageFormat.format("content://mms/{0}/addr", id);
        Uri uriAddress = Uri.parse(uriStr);
        Cursor cAdd = _context.getContentResolver().query(uriAddress, new String[] { "address" }, selectionAdd, null, null);
        String name = null;
        if (cAdd.moveToFirst()) {
            do {
            	name = ContactsManager.getContactName(_context, cAdd.getString(0));
            } while (cAdd.moveToNext());
        }
        if (cAdd != null) {
            cAdd.close();
        }
        return name;
    }
}