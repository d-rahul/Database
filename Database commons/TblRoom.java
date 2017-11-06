package com.readyandroid.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.readyandroid.Room;
import com.readyandroid.XmppUtils;

import java.util.ArrayList;
import java.util.List;

public class TblRoom extends DatabaseHelper {
    public static final String TABLE_NAME = "tbl_room";
    public static final String ID = "ID";
    public static final String JID = "jid";
    public static final String CONTACT_ID = "ContactID";
    public static final String TYPE = "TYPE"; // 0= one one, 1= group

    public static final String GROUP_ID = "GROUP_ID";
    public static final String GROUP_NAME = "GROUP_NAME";
    public static final String GROUP_PIC = "GROUP_PIC";
    public static final String IS_NOTIFICATION_MUTE = "IS_NOTIFICATION_MUTE";
    public static final String CHAT_BACKGROUND = "CHAT_BACKGROUND";
    public static final String TABLE_ROOM = "CREATE TABLE " + TblRoom.TABLE_NAME + " ( " +
            " `" + TblRoom.ID + "` INTEGER PRIMARY KEY AUTOINCREMENT, " +
            " `" + TblRoom.JID + "` TEXT UNIQUE, " +
            " `" + TblRoom.CONTACT_ID + "` INTEGER, " +
            " `" + TblRoom.TYPE + "` TEXT, " +
            " `" + TblRoom.GROUP_ID + "` TEXT, " +
            " `" + TblRoom.GROUP_NAME + "` TEXT, " +
            " `" + TblRoom.GROUP_PIC + "` TEXT, " +
            " `" + TblRoom.IS_NOTIFICATION_MUTE + "` TEXT, " +
            " `" + TblRoom.CHAT_BACKGROUND + "` TEXT " +
            ")";
    //private Object lock = new Object();
    private Context mContext;

    public TblRoom(Context context) {
        super(context);
        mContext = context;
        database = getWritableDatabase();
    }

    /*public void insertIfNotExist(String jid, ArrayList<String> members, int type) {
        synchronized (lock) {
            try {
                SQLiteDatabase database = getWritableDatabase();
                jid = XmppUtils.getJIDWithoutResource(jid);
                String sql = "select * from " + TblRoom.TABLE_NAME
                        + " where " + TblRoom.JID + " = '" + jid + "'";
                Cursor existingContact = database.rawQuery(sql, null);
                if (existingContact.getCount() == 0) {
                    long contactID = 0;

                    //This is for one 2 one chat, will not execute for group chat
                    if (type == 0 && members.size() == 2) {
                        TblContacts tblContacts = new TblContacts(mContext);
                        Contact contact = tblContacts.getContactByJID(members.get(0));
                        if (contact == null) {
                            contact = tblContacts.getContactByJID(members.get(1));
                        }
                        if (contact != null) {
                            contactID = contact.ID;
                        }
                    }

                    ContentValues mContentValues = new ContentValues();
                    mContentValues.put(TblRoom.JID, jid);
                    mContentValues.put(TblRoom.CONTACT_ID, contactID);
                    mContentValues.put(TblRoom.TYPE, type);
                    mContentValues.put(TblRoom.GROUP_ID, type);
                    mContentValues.put(TblRoom.GROUP_NAME, type);
                    mContentValues.put(TblRoom.GROUP_PIC, type);
                    mContentValues.put(TblRoom.IS_NOTIFICATION_MUTE, type);
                    mContentValues.put(TblRoom.CHAT_BACKGROUND, type);
                    database.insert(TABLE_NAME, null, mContentValues);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }*/

    public void insertIfNotExist(List<Room> rooms) {
        synchronized (lock) {
            database = getWritableDatabase();
            try {
                if (rooms != null && rooms.size() > 0) {
                    for (Room room : rooms) {
                        String jid = XmppUtils.getJIDWithoutResource(room.jid);
                        String sql = "select * from " + TblRoom.TABLE_NAME + " where " + TblRoom.JID + " = '" + jid + "'";
                        Cursor existingContact = database.rawQuery(sql, null);
                        long contactID = 0;
                        ContentValues mContentValues = new ContentValues();

                        if (existingContact.getCount() == 0) {
                            mContentValues.put(TblRoom.JID, jid);
                            mContentValues.put(TblRoom.CONTACT_ID, contactID);
                            mContentValues.put(TblRoom.TYPE, 1);
                            mContentValues.put(TblRoom.GROUP_ID, room.groupId);
                            mContentValues.put(TblRoom.GROUP_NAME, room.groupName);
                            mContentValues.put(TblRoom.GROUP_PIC, room.groupImage);
                            mContentValues.put(TblRoom.IS_NOTIFICATION_MUTE, room.isNotificationMute);
                            mContentValues.put(TblRoom.CHAT_BACKGROUND, room.chatBackground);
                            database.insert(TABLE_NAME, null, mContentValues);
                        } else {
                            mContentValues.put(TblRoom.GROUP_NAME, room.groupName);
                            mContentValues.put(TblRoom.GROUP_PIC, room.groupImage);
                            database.update(TblRoom.TABLE_NAME, mContentValues, TblRoom.GROUP_ID + "=?", new String[]{room.groupId});
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void insertIfNotExist(Room room) {
        synchronized (lock) {
            database = getWritableDatabase();
            try {
                if (room != null) {
                    /*if (TextUtils.isEmpty(room.jid) && room.groupName.equalsIgnoreCase("456")) {
                        room.jid = "aflatoon@" + XMPPInfo.XMPP_GROUP_ENDPOINT;
                    } else {
                        room.jid = room.groupId + "@" + XMPPInfo.XMPP_GROUP_ENDPOINT;
                    }*/
                    String jid = XmppUtils.getJIDWithoutResource(room.jid);
                    String sql = "select * from " + TblRoom.TABLE_NAME + " where " + TblRoom.JID + " = '" + jid + "'";
                    Cursor existingContact = database.rawQuery(sql, null);
                    long contactID = 0;
                    ContentValues mContentValues = new ContentValues();

                    if (existingContact.getCount() == 0) {
                        mContentValues.put(TblRoom.JID, jid);
                        mContentValues.put(TblRoom.CONTACT_ID, contactID);
                        mContentValues.put(TblRoom.TYPE, 1);
                        mContentValues.put(TblRoom.GROUP_ID, room.groupId);
                        mContentValues.put(TblRoom.GROUP_NAME, room.groupName);
                        mContentValues.put(TblRoom.GROUP_PIC, room.groupImage);
                        mContentValues.put(TblRoom.IS_NOTIFICATION_MUTE, room.isNotificationMute);
                        mContentValues.put(TblRoom.CHAT_BACKGROUND, room.chatBackground);
                        database.insertWithOnConflict(TABLE_NAME, null, mContentValues, SQLiteDatabase.CONFLICT_REPLACE);
                    } else {
                        mContentValues.put(TblRoom.GROUP_NAME, room.groupName);
                        mContentValues.put(TblRoom.GROUP_PIC, room.groupImage);
                        database.update(TblRoom.TABLE_NAME, mContentValues, TblRoom.GROUP_ID + "=?", new String[]{room.groupId});
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public long updateNotificationStatus(String jid, int notificationMute) {
        Log.e("TblRoom", "updateNotificationStatus");
        synchronized (lock) {
            database = getWritableDatabase();
            if (!TextUtils.isEmpty(jid)) {
                ContentValues mContentValues = new ContentValues();
                mContentValues.put(TblRoom.IS_NOTIFICATION_MUTE, notificationMute);
                return database.update(TblRoom.TABLE_NAME, mContentValues, TblRoom.JID + "=?", new String[]{jid});
            }
            return 0;
        }
    }

    public long updateChatBackground(String jid, String chat_background) {
        Log.e("TblRoom", "updateChatBackground");
        synchronized (lock) {
            database = getWritableDatabase();
            if (!TextUtils.isEmpty(jid)) {
                ContentValues mContentValues = new ContentValues();
                mContentValues.put(TblRoom.CHAT_BACKGROUND, chat_background);
                return database.update(TblRoom.TABLE_NAME, mContentValues, TblRoom.JID + "=?", new String[]{jid});
            }
            return 0;
        }
    }


    public long deleteRoomByJID(String jid) {
        Log.e("TblRoom", "deleteRoomByJID");
        synchronized (lock) {
            database = getWritableDatabase();
            if (!TextUtils.isEmpty(jid)) {
                return database.delete(TblRoom.TABLE_NAME, TblRoom.JID + "=?", new String[]{jid});
            }
            return 0;
        }
    }


    public Room getRoomByGroupID(long groupID) {
        Log.e("TblRoom", "getRoomByGroupID");
        synchronized (lock) {
            database = getWritableDatabase();
            ArrayList<Room> rooms = new ArrayList<>();
            String sql = "select * from " + TblRoom.TABLE_NAME + " where " + TblRoom.GROUP_ID + "='" + groupID + "'";
            Cursor cursor = database.rawQuery(sql, null);

            if (cursor.getCount() > 0) {
                rooms = getRoomsFromCursor(cursor);
                cursor.close();
                if (rooms.size() > 0) {
                    return rooms.get(0);
                }
            }
            return null;
        }
    }

    public Room getRoomByJID(String JID) {
        Log.e("TblRoom", "getRoomByJID");
        synchronized (lock) {
            if (!TextUtils.isEmpty(JID)) {
                database = getWritableDatabase();
                JID = XmppUtils.getPlainJID(JID);
                ArrayList<Room> rooms = new ArrayList<>();
                String sql = "select * from " + TblRoom.TABLE_NAME + " where " + TblRoom.JID + "='" + JID + "'";
                Cursor cursor = database.rawQuery(sql, null);

                if (cursor.getCount() > 0) {
                    rooms = getRoomsFromCursor(cursor);
                    cursor.close();
                    if (rooms.size() > 0) {
                        return rooms.get(0);
                    }
                }
            }
            return null;
        }
    }

    public ArrayList<Room> getAllRoom() {
        Log.e("TblRoom", "getAllRoom");
        synchronized (lock) {
            ArrayList<Room> rooms = new ArrayList<>();
            String sql = "select * from " + TblRoom.TABLE_NAME;
            Cursor cursor = database.rawQuery(sql, null);

            if (cursor.getCount() > 0) {
                rooms = getRoomsFromCursor(cursor);
            }
            return rooms;
        }
    }

    public ArrayList<Room> getRoomsFromCursor(Cursor mCursor) {
        ArrayList<Room> rooms = new ArrayList<>();
        mCursor.moveToFirst();
        do {
            Room room = new Room();
            room.ID = mCursor.getLong(mCursor.getColumnIndex(TblRoom.ID));
            room.jid = mCursor.getString(mCursor.getColumnIndex(TblRoom.JID));
            room.type = mCursor.getInt(mCursor.getColumnIndex(TblRoom.TYPE));
            room.groupId = mCursor.getString(mCursor.getColumnIndex(TblRoom.GROUP_ID));
            room.groupName = mCursor.getString(mCursor.getColumnIndex(TblRoom.GROUP_NAME));
            room.groupImage = mCursor.getString(mCursor.getColumnIndex(TblRoom.GROUP_PIC));
            room.isNotificationMute = mCursor.getInt(mCursor.getColumnIndex(TblRoom.IS_NOTIFICATION_MUTE));
            room.chatBackground = mCursor.getString(mCursor.getColumnIndex(TblRoom.CHAT_BACKGROUND));
            rooms.add(room);
        } while (mCursor.moveToNext());

        return rooms;
    }
}
