package com.readyandroid.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.readyandroid.Chat;
import com.readyandroid.ChatMessage;
import com.readyandroid.Contact;
import com.readyandroid.XmppAuthentication;
import com.readyandroid.XmppUtils;
import com.readyandroid.BlockUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TblContacts extends DatabaseHelper {
    public static final String TABLE_NAME = "tbl_contacts";

    public static final String ID = "ID";
    public static final String JID = "JID";
    public static final String USER_ID = "UserId";
    public static final String USER_NAME = "USER_NAME";
    public static final String EMAIL = "EMAIL";
    public static final String PHONE_NUMBER = "PHONE_NUMBER";
    public static final String PROFILE_PIC = "PROFILE_PIC";
    public static final String DISPLAY_NAME = "DISPLAY_NAME";
    public static final String STATUS = "STATUS";
    public static final String PRESENCE = "PRESENCE";
    public static final String IS_BLOCKED = "IS_BLOCKED";
    public static final String LAST_MSG_RECEIVED_TIME = "LAST_MSG_RECEIVED_TIME";
    public static final String IS_APP_USER = "IS_APP_USER";
    public static final String MSG_ID = "MSG_ID";
    public static final String IS_NOTIFICATION_MUTE = "IS_NOTIFICATION_MUTE";
    public static final String CHAT_BACKGROUND = "CHAT_BACKGROUND";
    public static final String LOCATION = "LOCATION";
    public static final String LAST_BUZZ_TIME = "LAST_BUZZ_TIME";
    public static final String IS_MATCH = "IS_MATCH";

    public static final String TABLE_CONTACTS = "CREATE TABLE " + TblContacts.TABLE_NAME + " ( " +
            " `" + TblContacts.ID + "` INTEGER PRIMARY KEY AUTOINCREMENT, " +
            " `" + TblContacts.JID + "` TEXT UNIQUE, " +
            " `" + TblContacts.USER_ID + "` TEXT, " +
            " `" + TblContacts.USER_NAME + "` TEXT, " +
            " `" + TblContacts.EMAIL + "` TEXT, " +
            " `" + TblContacts.PHONE_NUMBER + "` TEXT, " +
            " `" + TblContacts.PROFILE_PIC + "` TEXT, " +
            " `" + TblContacts.DISPLAY_NAME + "` TEXT, " +
            " `" + TblContacts.STATUS + "` TEXT, " +
            " `" + TblContacts.PRESENCE + "` TEXT, " +
            " `" + TblContacts.IS_BLOCKED + "` INTEGER, " +
            " `" + TblContacts.LAST_MSG_RECEIVED_TIME + "` INTEGER, " +
            " `" + TblContacts.IS_APP_USER + "` INTEGER, " +
            " `" + TblContacts.MSG_ID + "` INTEGER, " +
            " `" + TblContacts.IS_NOTIFICATION_MUTE + "` INTEGER, " +
            " `" + TblContacts.CHAT_BACKGROUND + "` TEXT, " +
            " `" + TblContacts.LOCATION + "` TEXT, " +
            " `" + TblContacts.LAST_BUZZ_TIME + "` TEXT, " +
            " `" + TblContacts.IS_MATCH + "` INTEGER " +
            ")";

    private Context mContext;
    //private Object lock = new Object();

    public TblContacts(Context context) {
        super(context);
        mContext = context;
    }

    public void insertContacts(List<Contact> contacts) {
        synchronized (lock) {
            database = getWritableDatabase();
            Cursor mCursor = null;
            try {
                if (contacts != null && contacts.size() > 0) {
                    Log.e("TblContacts", "insertContacts");
                    for (int i = 0; i < contacts.size(); i++) {
                        Contact mContact = contacts.get(i);
                        if (!TextUtils.isEmpty(mContact.JID)) {
                            //Check if contact is already exists or not
                            String sql = "select * from " + TblContacts.TABLE_NAME + " where " + TblContacts.JID + "='" + mContact.JID + "'";
                            Log.e("sql", sql);
                            mCursor = database.rawQuery(sql, null);
                            // If contact already exist then update it's information

                            //database.beginTransaction();
                            ContentValues mContentValues = new ContentValues();
                            mContentValues.put(TblContacts.EMAIL, mContact.email);
                            mContentValues.put(TblContacts.PHONE_NUMBER, mContact.phoneNumber);
                            mContentValues.put(TblContacts.PROFILE_PIC, mContact.profilePic);
                            mContentValues.put(TblContacts.DISPLAY_NAME, mContact.displayName);
                            mContentValues.put(TblContacts.IS_APP_USER, mContact.isAppUser);
                            mContentValues.put(TblContacts.LOCATION, mContact.location);
                            mContentValues.put(TblContacts.IS_MATCH, mContact.isMatch);
                            if (mCursor.getCount() > 0) {
                                //If user exist then update
                                long contactID = database.update(TblContacts.TABLE_NAME, mContentValues, TblContacts.JID + "=?", new String[]{mContact.JID});
                            } else {
                                //insert
                                mContentValues.put(TblContacts.JID, mContact.JID);
                                mContentValues.put(TblContacts.USER_ID, mContact.userID);
                                mContentValues.put(TblContacts.USER_NAME, mContact.userName);
                                mContentValues.put(TblContacts.STATUS, mContact.status);
                                mContentValues.put(TblContacts.PRESENCE, mContact.presence);
                                mContentValues.put(TblContacts.MSG_ID, mContact.msgID);
                                mContentValues.put(TblContacts.LAST_MSG_RECEIVED_TIME, mContact.lastMsgReceivedTime);
                                mContentValues.put(TblContacts.IS_NOTIFICATION_MUTE, mContact.isNotificationMute);
                                mContentValues.put(TblContacts.CHAT_BACKGROUND, mContact.chatBackground);
                                mContentValues.put(TblContacts.LAST_BUZZ_TIME, mContact.lastBuzzTime);
                                mContentValues.put(TblContacts.IS_BLOCKED, mContact.isBlocked);
                                long contactID = database.insertWithOnConflict(TblContacts.TABLE_NAME, null, mContentValues, SQLiteDatabase.CONFLICT_IGNORE);
                            }
                            try {
                                XmppAuthentication.getInstance(mContext).addToRosterInBackground(mContact.JID, mContact.displayName, null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            //database.setTransactionSuccessful();
                            //database.endTransaction();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mCursor != null && !mCursor.isClosed()) {
                    mCursor.close();
                }
            }
        }
    }

    public void insertContacts(Contact mContact) {
        synchronized (lock) {
            database = getWritableDatabase();
            Cursor mCursor = null;
            try {
                if (mContact != null) {
                    if (!TextUtils.isEmpty(mContact.JID)) {
                        //Check if contact is already exists or not
                        String sql = "select * from " + TblContacts.TABLE_NAME + " where " + TblContacts.JID + "='" + mContact.JID + "'";
                        Log.e("sql", sql);
                        mCursor = database.rawQuery(sql, null);
                        // If contact already exist then update it's information

                        //database.beginTransaction();
                        ContentValues mContentValues = new ContentValues();
                        mContentValues.put(TblContacts.EMAIL, mContact.email);
                        mContentValues.put(TblContacts.PHONE_NUMBER, mContact.phoneNumber);
                        mContentValues.put(TblContacts.PROFILE_PIC, mContact.profilePic);
                        mContentValues.put(TblContacts.DISPLAY_NAME, mContact.displayName);
                        mContentValues.put(TblContacts.IS_APP_USER, mContact.isAppUser);
                        mContentValues.put(TblContacts.LOCATION, mContact.location);
                        mContentValues.put(TblContacts.IS_MATCH, mContact.isMatch);
                        if (mCursor.getCount() > 0) {
                            //If user exist then update
                            long contactID = database.update(TblContacts.TABLE_NAME, mContentValues, TblContacts.JID + "=?", new String[]{mContact.JID});
                        } else {
                            //insert
                            mContentValues.put(TblContacts.JID, mContact.JID);
                            mContentValues.put(TblContacts.USER_ID, mContact.userID);
                            mContentValues.put(TblContacts.USER_NAME, mContact.userName);
                            mContentValues.put(TblContacts.STATUS, mContact.status);
                            mContentValues.put(TblContacts.PRESENCE, mContact.presence);
                            mContentValues.put(TblContacts.MSG_ID, mContact.msgID);
                            mContentValues.put(TblContacts.LAST_MSG_RECEIVED_TIME, mContact.lastMsgReceivedTime);
                            mContentValues.put(TblContacts.IS_NOTIFICATION_MUTE, mContact.isNotificationMute);
                            mContentValues.put(TblContacts.CHAT_BACKGROUND, mContact.chatBackground);
                            mContentValues.put(TblContacts.LAST_BUZZ_TIME, mContact.lastBuzzTime);
                            mContentValues.put(TblContacts.IS_BLOCKED, mContact.isBlocked);
                            long contactID = database.insertWithOnConflict(TblContacts.TABLE_NAME, null, mContentValues, SQLiteDatabase.CONFLICT_IGNORE);
                        }
                        try {
                            XmppAuthentication.getInstance(mContext).addToRosterInBackground(mContact.JID, mContact.displayName, null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        //database.setTransactionSuccessful();
                        //database.endTransaction();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mCursor != null && !mCursor.isClosed()) {
                    mCursor.close();
                }
            }
        }
    }

    public boolean isUserSaved(String JID) {
        synchronized (lock) {
            database = getWritableDatabase();
            String sql = "select * from " + TblContacts.TABLE_NAME + " where " + TblContacts.JID + "='" + JID + "' LIMIT 1";
            Cursor mCursor = database.rawQuery(sql, null);
            if (mCursor.getCount() > 0) {
                mCursor.close();
                return true;
            }
            return false;
        }
    }

    public Contact getContactByJID(String JID) {
        synchronized (lock) {
            database = getWritableDatabase();
            Cursor mCursor = null;
            try {
                JID = XmppUtils.getPlainJID(JID);
                String sql = "select * from " + TblContacts.TABLE_NAME + " where " + "UPPER(" + TblContacts.JID + ")" + "='" + JID.toUpperCase() + "' LIMIT 1";
                mCursor = database.rawQuery(sql, null);
                if (mCursor.getCount() > 0) {
                    ArrayList<Contact> Contacts = getContactsFromCursor(mCursor);
                    if (mCursor != null && !mCursor.isClosed()) {
                        mCursor.close();
                    }
                    if (Contacts.size() > 0) {
                        return Contacts.get(0);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mCursor != null && !mCursor.isClosed()) {
                    mCursor.close();
                }
            }
            return null;
        }
    }

    public Contact getContactByID(long ID) {
        synchronized (lock) {
            database = getWritableDatabase();
            Cursor mCursor = null;
            try {
                String sql = "select * from " + TblContacts.TABLE_NAME + " where " + TblContacts.ID + "=" + ID + " LIMIT 1";
                mCursor = database.rawQuery(sql, null);
                if (mCursor.getCount() > 0) {
                    ArrayList<Contact> Contacts = getContactsFromCursor(mCursor);
                    if (mCursor != null && !mCursor.isClosed()) {
                        mCursor.close();
                    }
                    if (Contacts.size() > 0) {
                        return Contacts.get(0);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mCursor != null && !mCursor.isClosed()) {
                    mCursor.close();
                }
            }
            return null;
        }
    }

    public int getBlockedStatusForJID(String JID) {
        synchronized (lock) {
            database = getWritableDatabase();
            Cursor mCursor = null;
            try {
                String sql = "select " + TblContacts.TABLE_NAME + "." + TblContacts.IS_BLOCKED + " from " + TblContacts.TABLE_NAME + " where " + TblContacts.JID + "='" + JID + "' LIMIT 1";
                mCursor = database.rawQuery(sql, null);
                if (mCursor.getCount() > 0) {
                    mCursor.moveToFirst();
                    int isBlocked = mCursor.getInt(mCursor.getColumnIndex(TblContacts.IS_BLOCKED));
                    mCursor.close();
                    return isBlocked;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mCursor != null && !mCursor.isClosed()) {
                    mCursor.close();
                }
            }
            return 0;
        }
    }

    public int getMatchStatusForJID(String JID) {
        synchronized (lock) {
            database = getWritableDatabase();
            Cursor mCursor = null;
            try {
                String sql = "select " + TblContacts.TABLE_NAME + "." + TblContacts.IS_MATCH + " from " + TblContacts.TABLE_NAME + " where " + TblContacts.JID + "='" + JID + "' LIMIT 1";
                mCursor = database.rawQuery(sql, null);
                if (mCursor.getCount() > 0) {
                    mCursor.moveToFirst();
                    int isMatch = mCursor.getInt(mCursor.getColumnIndex(TblContacts.IS_MATCH));
                    mCursor.close();
                    return isMatch;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mCursor != null && !mCursor.isClosed()) {
                    mCursor.close();
                }
            }
            return 0;
        }
    }

    public Contact getContactByUserID(String UserID) {
        synchronized (lock) {
            database = getWritableDatabase();
            Cursor mCursor = null;
            try {
                String sql = "select * from " + TblContacts.TABLE_NAME + " where " + TblContacts.USER_ID + "='" + UserID + "' LIMIT 1";
                mCursor = database.rawQuery(sql, null);
                if (mCursor.getCount() > 0) {
                    ArrayList<Contact> Contacts = getContactsFromCursor(mCursor);
                    if (mCursor != null && !mCursor.isClosed()) {
                        mCursor.close();
                    }
                    if (Contacts.size() > 0) {
                        return Contacts.get(0);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mCursor != null && !mCursor.isClosed()) {
                    mCursor.close();
                }
            }
            return null;
        }
    }

    public ArrayList<Contact> getAllContacts() {
        synchronized (lock) {
            database = getWritableDatabase();
            Cursor mCursor = null;
            try {
                String sql = "select * from " + TblContacts.TABLE_NAME;
                mCursor = database.rawQuery(sql, null);
                if (mCursor.getCount() > 0) {
                    ArrayList<Contact> Contacts = getContactsFromCursor(mCursor);
                    if (mCursor != null && !mCursor.isClosed()) {
                        mCursor.close();
                    }
                    return Contacts;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mCursor != null && !mCursor.isClosed()) {
                    mCursor.close();
                }
            }
            return null;
        }
    }

    public ArrayList<Contact> getContactsFromCursor(Cursor mCursor) {
        ArrayList<Contact> tblContacts = new ArrayList<>();
        if (mCursor != null && !mCursor.isClosed()) {
            try {
                mCursor.moveToFirst();
                do {
                    int ID = mCursor.getInt(mCursor.getColumnIndex(TblContacts.ID));
                    String JID = mCursor.getString(mCursor.getColumnIndex(TblContacts.JID));
                    String userID = mCursor.getString(mCursor.getColumnIndex(TblContacts.USER_ID));
                    String userName = mCursor.getString(mCursor.getColumnIndex(TblContacts.USER_NAME));
                    String email = mCursor.getString(mCursor.getColumnIndex(TblContacts.EMAIL));
                    String phoneNumber = mCursor.getString(mCursor.getColumnIndex(TblContacts.PHONE_NUMBER));
                    String profilePic = mCursor.getString(mCursor.getColumnIndex(TblContacts.PROFILE_PIC));
                    String displayName = mCursor.getString(mCursor.getColumnIndex(TblContacts.DISPLAY_NAME));
                    String status = mCursor.getString(mCursor.getColumnIndex(TblContacts.STATUS));
                    String presence = mCursor.getString(mCursor.getColumnIndex(TblContacts.PRESENCE));
                    int isBlocked = mCursor.getInt(mCursor.getColumnIndex(TblContacts.IS_BLOCKED));
                    long lastMsgReceivedTime = mCursor.getLong(mCursor.getColumnIndex(TblContacts.LAST_MSG_RECEIVED_TIME));
                    int isAppUser = mCursor.getInt(mCursor.getColumnIndex(TblContacts.IS_APP_USER));
                    long msgID = mCursor.getLong(mCursor.getColumnIndex(TblContacts.MSG_ID));
                    int isNotificationMute = mCursor.getInt(mCursor.getColumnIndex(TblContacts.IS_NOTIFICATION_MUTE));
                    String chatBackground = mCursor.getString(mCursor.getColumnIndex(TblContacts.CHAT_BACKGROUND));
                    String location = mCursor.getString(mCursor.getColumnIndex(TblContacts.LOCATION));
                    String lastBuzzTime = mCursor.getString(mCursor.getColumnIndex(TblContacts.LAST_BUZZ_TIME));
                    int isMatch = mCursor.getInt(mCursor.getColumnIndex(TblContacts.IS_MATCH));

                    Contact contact = new Contact(ID, JID, userID, userName, email, phoneNumber, profilePic, displayName,
                            status, presence, isBlocked, lastMsgReceivedTime, isAppUser, msgID, isNotificationMute, chatBackground, location, lastBuzzTime, isMatch);
                    tblContacts.add(contact);
                } while (mCursor.moveToNext());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (!mCursor.isClosed()) {
                    mCursor.close();
                }
            }
        }
        return tblContacts;
    }

    public ArrayList<Chat> getRecentChattedContacts() {
        synchronized (lock) {
            database = getWritableDatabase();
            Cursor mCursor = null;
            ArrayList<Chat> mChats = new ArrayList<>();
            try {
                String sql = "select " + TblContacts.TABLE_NAME + ".*,"
                        + TblMessages.TABLE_NAME + "." + TblMessages.CONTACT_ID + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.IS_SENT + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.MESSAGE_ID + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.MESSAGE + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.RECEIVED_TIME + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.STATUS + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.TYPE + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.IS_READ + ","
                        + "(select count(" + TblMessages.TABLE_NAME + "." + TblMessages.ID + ") from " + TblMessages.TABLE_NAME + " where "
                        + TblMessages.TABLE_NAME + "." + TblMessages.CONTACT_ID + "=" + TblContacts.TABLE_NAME + "." + TblContacts.ID +
                        /*+" and "+ TblMessages.TABLE_NAME + "." + TblMessages.is_inactive + "=0"*/
                        " and " + TblMessages.TABLE_NAME + "." + TblMessages.IS_READ + "=0)" + " as unread_count "
                        + " from " + TblContacts.TABLE_NAME
                        + " inner join " + TblMessages.TABLE_NAME + " on " + TblContacts.TABLE_NAME + "." + TblContacts.MSG_ID + "=" + TblMessages.TABLE_NAME + "." + TblMessages.ID
                        + " where " + TblContacts.TABLE_NAME + "." + TblContacts.MSG_ID + ">0"
                        + " and " + TblContacts.TABLE_NAME + "." + TblContacts.IS_MATCH + "=1"
                        + " order by " + TblContacts.LAST_MSG_RECEIVED_TIME + " desc ";
                Log.i("sql", "recent chated: " + sql);
                mCursor = database.rawQuery(sql, null);
                if (mCursor.getCount() > 0) {
                    mCursor.moveToFirst();
                    do {
                        int ID = mCursor.getInt(mCursor.getColumnIndex(TblContacts.ID));
                        String JID = mCursor.getString(mCursor.getColumnIndex(TblContacts.JID));
                        String userID = mCursor.getString(mCursor.getColumnIndex(TblContacts.USER_ID));
                        String userName = mCursor.getString(mCursor.getColumnIndex(TblContacts.USER_NAME));
                        String email = mCursor.getString(mCursor.getColumnIndex(TblContacts.EMAIL));
                        String phoneNumber = mCursor.getString(mCursor.getColumnIndex(TblContacts.PHONE_NUMBER));
                        String profilePic = mCursor.getString(mCursor.getColumnIndex(TblContacts.PROFILE_PIC));
                        String displayName = mCursor.getString(mCursor.getColumnIndex(TblContacts.DISPLAY_NAME));
                        String status = mCursor.getString(mCursor.getColumnIndex(TblContacts.STATUS));
                        String presence = mCursor.getString(mCursor.getColumnIndex(TblContacts.PRESENCE));
                        int isBlocked = mCursor.getInt(mCursor.getColumnIndex(TblContacts.IS_BLOCKED));
                        long lastMsgReceivedTime = mCursor.getLong(mCursor.getColumnIndex(TblContacts.LAST_MSG_RECEIVED_TIME));
                        int isAppUser = mCursor.getInt(mCursor.getColumnIndex(TblContacts.IS_APP_USER));
                        long msgID = mCursor.getLong(mCursor.getColumnIndex(TblContacts.MSG_ID));
                        int isNotificationMute = mCursor.getInt(mCursor.getColumnIndex(TblContacts.IS_NOTIFICATION_MUTE));
                        String chatBackground = mCursor.getString(mCursor.getColumnIndex(TblContacts.CHAT_BACKGROUND));
                        String location = mCursor.getString(mCursor.getColumnIndex(TblContacts.LOCATION));
                        int unreadCount = mCursor.getInt(mCursor.getColumnIndex("unread_count"));
                        String lastBuzzTime = mCursor.getString(mCursor.getColumnIndex(TblContacts.LAST_BUZZ_TIME));
                        int isMatch = mCursor.getInt(mCursor.getColumnIndex(TblContacts.IS_MATCH));

                        Chat chat = new Chat();
                        chat.contact = new Contact(ID, JID, userID, userName, email, phoneNumber, profilePic,
                                displayName, status, presence, isBlocked, lastMsgReceivedTime, isAppUser, msgID, isNotificationMute, chatBackground, unreadCount, location, lastBuzzTime, isMatch);

                        ChatMessage chatMessage = new ChatMessage();
                        chatMessage.ID = ID;
                        chatMessage.JID = JID;
                        chatMessage.isSent = mCursor.getInt(mCursor.getColumnIndex(TblMessages.IS_SENT));
                        chatMessage.contactID = mCursor.getLong(mCursor.getColumnIndex(TblMessages.CONTACT_ID));
                        chatMessage.messageID = mCursor.getString(mCursor.getColumnIndex(TblMessages.MESSAGE_ID));
                        chatMessage.message = mCursor.getString(mCursor.getColumnIndex(TblMessages.MESSAGE));
                        chatMessage.receivedTime = mCursor.getLong(mCursor.getColumnIndex(TblMessages.RECEIVED_TIME));
                        chatMessage.mReceivedStatus = chatMessage.getReceivedStatus(mCursor.getString(mCursor.getColumnIndex(TblMessages.STATUS)));
                        chatMessage.isRead = mCursor.getInt(mCursor.getColumnIndex(TblMessages.IS_READ));
                        chatMessage.type = mCursor.getString(mCursor.getColumnIndex(TblMessages.TYPE));

                        chat.chatMessage = chatMessage;

                        //to stop showing recent chat without any message
                        if (!TextUtils.isEmpty(chatMessage.messageID)) {
                            mChats.add(chat);
                        }
                    } while (mCursor.moveToNext());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mCursor != null && !mCursor.isClosed()) {
                    mCursor.close();
                }
            }
            return mChats;
        }
    }


    public ArrayList<Chat> getRecentReceivedChatted() {
        synchronized (lock) {
            database = getWritableDatabase();
            ArrayList<Chat> mChats = new ArrayList<>();
            String sql = "select " + TblContacts.TABLE_NAME + ".*,"
                    + TblMessages.TABLE_NAME + "." + TblMessages.CONTACT_ID + ","
                    + TblMessages.TABLE_NAME + "." + TblMessages.IS_SENT + ","
                    + TblMessages.TABLE_NAME + "." + TblMessages.MESSAGE_ID + ","
                    + TblMessages.TABLE_NAME + "." + TblMessages.MESSAGE + ","
                    + TblMessages.TABLE_NAME + "." + TblMessages.RECEIVED_TIME + ","
                    + TblMessages.TABLE_NAME + "." + TblMessages.STATUS + ","
                    + TblMessages.TABLE_NAME + "." + TblMessages.TYPE + ","
                    + TblMessages.TABLE_NAME + "." + TblMessages.IS_READ + ","
                    + "(select count(" + TblMessages.TABLE_NAME + "." + TblMessages.ID + ") from " + TblMessages.TABLE_NAME + " where "
                    + TblMessages.TABLE_NAME + "." + TblMessages.CONTACT_ID + "=" + TblContacts.TABLE_NAME + "." + TblContacts.ID +
                    /*+" and "+ TblMessages.TABLE_NAME + "." + TblMessages.is_inactive + "=0"*/
                    " and " + TblMessages.TABLE_NAME + "." + TblMessages.IS_READ + "=0)" + " as unread_count "
                    + " from " + TblContacts.TABLE_NAME
                    + " inner join " + TblMessages.TABLE_NAME + " on " + TblContacts.TABLE_NAME + "." + TblContacts.MSG_ID + "=" + TblMessages.TABLE_NAME + "." + TblMessages.ID
                    + " where " + TblContacts.TABLE_NAME + "." + TblContacts.MSG_ID + ">0"
                    //+ " and " + TblContacts.TABLE_NAME + "." + TblContacts.IS_MATCH + "=1"
                    + " and " + TblMessages.TABLE_NAME + "." + TblMessages.IS_SENT + "!=1"
                    + " order by " + TblContacts.LAST_MSG_RECEIVED_TIME + " desc ";
            Log.i("sql", "recent chated: " + sql);
            Cursor mCursor = database.rawQuery(sql, null);
            if (mCursor.getCount() > 0) {
                mCursor.moveToFirst();
                do {
                    int ID = mCursor.getInt(mCursor.getColumnIndex(TblContacts.ID));
                    String JID = mCursor.getString(mCursor.getColumnIndex(TblContacts.JID));
                    String userID = mCursor.getString(mCursor.getColumnIndex(TblContacts.USER_ID));
                    String userName = mCursor.getString(mCursor.getColumnIndex(TblContacts.USER_NAME));
                    String email = mCursor.getString(mCursor.getColumnIndex(TblContacts.EMAIL));
                    String phoneNumber = mCursor.getString(mCursor.getColumnIndex(TblContacts.PHONE_NUMBER));
                    String profilePic = mCursor.getString(mCursor.getColumnIndex(TblContacts.PROFILE_PIC));
                    String displayName = mCursor.getString(mCursor.getColumnIndex(TblContacts.DISPLAY_NAME));
                    String status = mCursor.getString(mCursor.getColumnIndex(TblContacts.STATUS));
                    String presence = mCursor.getString(mCursor.getColumnIndex(TblContacts.PRESENCE));
                    int isBlocked = mCursor.getInt(mCursor.getColumnIndex(TblContacts.IS_BLOCKED));
                    long lastMsgReceivedTime = mCursor.getLong(mCursor.getColumnIndex(TblContacts.LAST_MSG_RECEIVED_TIME));
                    int isAppUser = mCursor.getInt(mCursor.getColumnIndex(TblContacts.IS_APP_USER));
                    long msgID = mCursor.getLong(mCursor.getColumnIndex(TblContacts.MSG_ID));
                    int isNotificationMute = mCursor.getInt(mCursor.getColumnIndex(TblContacts.IS_NOTIFICATION_MUTE));
                    String chatBackground = mCursor.getString(mCursor.getColumnIndex(TblContacts.CHAT_BACKGROUND));
                    String location = mCursor.getString(mCursor.getColumnIndex(TblContacts.LOCATION));
                    int unreadCount = mCursor.getInt(mCursor.getColumnIndex("unread_count"));
                    String lastBuzzTime = mCursor.getString(mCursor.getColumnIndex(TblContacts.LAST_BUZZ_TIME));
                    int isMatch = mCursor.getInt(mCursor.getColumnIndex(TblContacts.IS_MATCH));

                    Chat chat = new Chat();
                    chat.contact = new Contact(ID, JID, userID, userName, email, phoneNumber, profilePic,
                            displayName, status, presence, isBlocked, lastMsgReceivedTime, isAppUser, msgID, isNotificationMute, chatBackground, unreadCount, location, lastBuzzTime, isMatch);

                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.ID = ID;
                    chatMessage.JID = JID;
                    chatMessage.isSent = mCursor.getInt(mCursor.getColumnIndex(TblMessages.IS_SENT));
                    chatMessage.contactID = mCursor.getLong(mCursor.getColumnIndex(TblMessages.CONTACT_ID));
                    chatMessage.messageID = mCursor.getString(mCursor.getColumnIndex(TblMessages.MESSAGE_ID));
                    chatMessage.message = mCursor.getString(mCursor.getColumnIndex(TblMessages.MESSAGE));
                    chatMessage.receivedTime = mCursor.getLong(mCursor.getColumnIndex(TblMessages.RECEIVED_TIME));
                    chatMessage.mReceivedStatus = chatMessage.getReceivedStatus(mCursor.getString(mCursor.getColumnIndex(TblMessages.STATUS)));
                    chatMessage.isRead = mCursor.getInt(mCursor.getColumnIndex(TblMessages.IS_READ));
                    chatMessage.type = mCursor.getString(mCursor.getColumnIndex(TblMessages.TYPE));

                    chat.chatMessage = chatMessage;

                    //to stop showing recent chat without any message
                    if (!TextUtils.isEmpty(chatMessage.messageID)) {
                        mChats.add(chat);
                    }
                } while (mCursor.moveToNext());
            }
            return mChats;
        }
    }

    public List<Contact> getBlockedContactsFromDB() {
        List<Contact> tblContacts = new ArrayList<>();
        synchronized (lock) {
            database = getWritableDatabase();
            try {
                String query = "SELECT * FROM " + TblContacts.TABLE_NAME + " WHERE " + TblContacts.IS_BLOCKED + ">= 1";
                Cursor mCursor = database.rawQuery(query, null);
                if (mCursor != null && mCursor.getCount() > 0) {
                    if (mCursor.moveToFirst()) {
                        do {
                            int ID = mCursor.getInt(mCursor.getColumnIndex(TblContacts.ID));
                            String JID = mCursor.getString(mCursor.getColumnIndex(TblContacts.JID));
                            String userID = mCursor.getString(mCursor.getColumnIndex(TblContacts.USER_ID));
                            String userName = mCursor.getString(mCursor.getColumnIndex(TblContacts.USER_NAME));
                            String email = mCursor.getString(mCursor.getColumnIndex(TblContacts.EMAIL));
                            String phoneNumber = mCursor.getString(mCursor.getColumnIndex(TblContacts.PHONE_NUMBER));
                            String profilePic = mCursor.getString(mCursor.getColumnIndex(TblContacts.PROFILE_PIC));
                            String displayName = mCursor.getString(mCursor.getColumnIndex(TblContacts.DISPLAY_NAME));
                            String status = mCursor.getString(mCursor.getColumnIndex(TblContacts.STATUS));
                            String presence = mCursor.getString(mCursor.getColumnIndex(TblContacts.PRESENCE));
                            int isBlocked = mCursor.getInt(mCursor.getColumnIndex(TblContacts.IS_BLOCKED));
                            long lastMsgReceivedTime = mCursor.getLong(mCursor.getColumnIndex(TblContacts.LAST_MSG_RECEIVED_TIME));
                            int isAppUser = mCursor.getInt(mCursor.getColumnIndex(TblContacts.IS_APP_USER));
                            long msgID = mCursor.getLong(mCursor.getColumnIndex(TblContacts.MSG_ID));
                            int isNotificationMute = mCursor.getInt(mCursor.getColumnIndex(TblContacts.IS_NOTIFICATION_MUTE));
                            String chatBackground = mCursor.getString(mCursor.getColumnIndex(TblContacts.CHAT_BACKGROUND));
                            String location = mCursor.getString(mCursor.getColumnIndex(TblContacts.LOCATION));
                            String lastBuzzTime = mCursor.getString(mCursor.getColumnIndex(TblContacts.LAST_BUZZ_TIME));
                            int isMatch = mCursor.getInt(mCursor.getColumnIndex(TblContacts.IS_MATCH));

                            Contact contact = new Contact(ID, JID, userID, userName, email, phoneNumber, profilePic, displayName,
                                    status, presence, isBlocked, lastMsgReceivedTime, isAppUser, msgID, isNotificationMute, chatBackground, location, lastBuzzTime, isMatch);
                            tblContacts.add(contact);
                        } while (mCursor.moveToNext());
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        List<Contact> sortedContacts = sortContactAscByName(tblContacts);
        return sortedContacts;
    }

    /**
     * use this method after delete chats
     *
     * @param chatMessage
     * @return
     */
    public long updateMsgIdAndTime(ChatMessage chatMessage) {
        Log.e("TblContacts", "updateMsgIdAndTime");
        synchronized (lock) {
            database = getWritableDatabase();
            if (chatMessage.ID > 0) {
                ContentValues mContentValues = new ContentValues();
                mContentValues.put(TblContacts.LAST_MSG_RECEIVED_TIME, chatMessage.receivedTime);
                mContentValues.put(TblContacts.MSG_ID, chatMessage.ID);
                if (TextUtils.isEmpty(chatMessage.JID)) {
                    return database.update(TblContacts.TABLE_NAME, mContentValues, TblContacts.ID + "=?", new String[]{chatMessage.contactID + ""});
                } else {
                    return database.update(TblContacts.TABLE_NAME, mContentValues, TblContacts.JID + "=?", new String[]{chatMessage.JID});
                }
            }
            return 0;
        }
    }

    public long updateLastReceivedMessageById(ChatMessage chatMessage) {
        Log.e("TblContacts", "updateLastReceivedMessageById");
        synchronized (lock) {
            database = getWritableDatabase();
            if (chatMessage.ID > 0) {
                ContentValues mContentValues = new ContentValues();
                mContentValues.put(TblContacts.LAST_MSG_RECEIVED_TIME, System.currentTimeMillis());
                mContentValues.put(TblContacts.MSG_ID, chatMessage.ID);
                if (TextUtils.isEmpty(chatMessage.JID)) {
                    return database.update(TblContacts.TABLE_NAME, mContentValues, TblContacts.ID + "=?", new String[]{chatMessage.contactID + ""});
                } else {
                    return database.update(TblContacts.TABLE_NAME, mContentValues, TblContacts.JID + "=?", new String[]{chatMessage.JID});
                }
            }
            return 0;
        }
    }

    public long updateChatBackground(String jid, String chat_background) {
        Log.e("TblContacts", "updateLastReceivedMessageById");
        synchronized (lock) {
            database = getWritableDatabase();
            if (!TextUtils.isEmpty(jid)) {
                ContentValues mContentValues = new ContentValues();
                mContentValues.put(TblContacts.CHAT_BACKGROUND, chat_background);
                return database.update(TblContacts.TABLE_NAME, mContentValues, TblContacts.JID + "=?", new String[]{jid});
            }
            return 0;
        }
    }

    public long updateNotificationStatus(String jid, int notificationMute) {
        Log.e("TblContacts", "updateLastReceivedMessageById");
        synchronized (lock) {
            database = getWritableDatabase();
            if (!TextUtils.isEmpty(jid)) {
                ContentValues mContentValues = new ContentValues();
                mContentValues.put(TblContacts.IS_NOTIFICATION_MUTE, notificationMute);
                return database.update(TblContacts.TABLE_NAME, mContentValues, TblContacts.JID + "=?", new String[]{jid});
            }
            return 0;
        }
    }

    public long updateLastBuzzTime(String jid, String lastBuzzTime) {
        Log.e("TblContacts", "updateLastBuzzTime");
        synchronized (lock) {
            database = getWritableDatabase();
            if (!TextUtils.isEmpty(jid)) {
                ContentValues mContentValues = new ContentValues();
                mContentValues.put(TblContacts.LAST_BUZZ_TIME, lastBuzzTime);
                return database.update(TblContacts.TABLE_NAME, mContentValues, TblContacts.JID + "=?", new String[]{jid});
            }
            return 0;
        }
    }

    /**
     * Call this only following case.
     * user A from contact is not jabber user.
     * Later on user A become jabber user and ping you .
     * so on MESSAGE receive we are sync only one contact. after that we should call this.
     *
     * @return
     */
    public long updateAfterSyncCompleteOnChatReceive(String JID, long msgID, long receivedTime, String presence, String status) {
        synchronized (lock) {
            database = getWritableDatabase();
            ContentValues mContentValues = new ContentValues();
            mContentValues.put(TblContacts.LAST_MSG_RECEIVED_TIME, receivedTime);
            mContentValues.put(TblContacts.MSG_ID, msgID);
            mContentValues.put(TblContacts.PRESENCE, presence);
            mContentValues.put(TblContacts.STATUS, status);
            return database.update(TblContacts.TABLE_NAME, mContentValues, TblContacts.JID + "=?", new String[]{JID});
        }
    }

    public int updatePresenceByJID(String JID, boolean isAvailable) {
        synchronized (lock) {
            database = getWritableDatabase();
            ContentValues mContentValues = new ContentValues();
            mContentValues.put(TblContacts.PRESENCE, isAvailable ? "1" : "0");
            int noOfUpdatedRows = database.update(TblContacts.TABLE_NAME, mContentValues, TblContacts.JID + "=?", new String[]{JID});
            return noOfUpdatedRows;
        }
    }

    public int updateBlockedUserByJID(String JID, int isBlocked) {
        synchronized (lock) {
            database = getWritableDatabase();
            ContentValues mContentValues = new ContentValues();
            mContentValues.put(TblContacts.IS_BLOCKED, isBlocked);
            int noOfUpdatedRows = database.update(TblContacts.TABLE_NAME, mContentValues, TblContacts.JID + "=?", new String[]{JID});
            return noOfUpdatedRows;
        }
    }

    public int updateBlockedUsers(List<BlockUser> blockUsers) {
        synchronized (lock) {
            if (blockUsers != null) {
                database = getWritableDatabase();
                for (BlockUser user : blockUsers) {
                    int noOfUpdatedRows = 0;
                    Cursor mCursor = null;
                    try {
                        int isBlocked = 1;
                        String sql = "select * from " + TblContacts.TABLE_NAME + " where " + TblContacts.JID + "='" + user.chatUserName + "' LIMIT 1";
                        mCursor = database.rawQuery(sql, null);
                        if (mCursor.getCount() <= 0) {
                            Contact contact = new Contact();
                            contact.userID = user.usersId;
                            contact.JID = user.chatUserName;
                            contact.userName = "";
                            contact.phoneNumber = "";
                            contact.profilePic = user.profileImage;
                            contact.displayName = user.name;
                            contact.status = "";
                            contact.presence = "0";
                            isBlocked = contact.isBlocked = 1;
                            contact.lastMsgReceivedTime = 0;
                            contact.msgID = 0;
                            contact.isNotificationMute = 0;
                            contact.chatBackground = "";
                            contact.location = user.location;
                            contact.isMatch = 0;
                            insertContacts(contact);
                        } else {
                            mCursor.moveToFirst();
                            isBlocked = mCursor.getInt(mCursor.getColumnIndex(TblContacts.IS_BLOCKED));
                            mCursor.close();
                        }

                        ContentValues mContentValues = new ContentValues();
                        mContentValues.put(TblContacts.IS_BLOCKED, isBlocked == 2 ? 3 : 1);
                        noOfUpdatedRows = database.update(TblContacts.TABLE_NAME, mContentValues, TblContacts.JID + "=?", new String[]{JID});
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (mCursor != null && !mCursor.isClosed()) {
                            mCursor.close();
                        }
                    }
                    return noOfUpdatedRows;
                }
            }
        }
        return -1;
    }

    public int updateMatchByJID(String JID, int isMatch) {
        synchronized (lock) {
            database = getWritableDatabase();
            ContentValues mContentValues = new ContentValues();
            mContentValues.put(TblContacts.IS_MATCH, isMatch);
            int noOfUpdatedRows = database.update(TblContacts.TABLE_NAME, mContentValues, TblContacts.JID + "=?", new String[]{JID});
            return noOfUpdatedRows;
        }
    }

    public int updateMatchByUserID(String userId, int isMatch) {
        synchronized (lock) {
            database = getWritableDatabase();
            ContentValues mContentValues = new ContentValues();
            mContentValues.put(TblContacts.IS_MATCH, isMatch);
            int noOfUpdatedRows = database.update(TblContacts.TABLE_NAME, mContentValues, TblContacts.USER_ID + "=?", new String[]{userId});
            return noOfUpdatedRows;
        }
    }

    public void deleteContactByJID(String j_id) {
        synchronized (lock) {
            database = getWritableDatabase();
            database.delete(TblContacts.TABLE_NAME, TblContacts.JID + "=?", new String[]{j_id});
        }
    }

    private List<Contact> sortContactAscByName(List<Contact> contacts) {
        List<Contact> contactAscList = new ArrayList<Contact>(contacts);
        Collections.sort(contactAscList, Contact.contactByAscName);
        return contactAscList;
    }
}