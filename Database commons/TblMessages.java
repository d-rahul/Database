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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TblMessages extends DatabaseHelper {
    public static final String TABLE_NAME = "tbl_messages";

    public static final String ID = "ID";
    public static final String CONTACT_ID = "CONTACT_ID";
    public static final String JID = "JID";
    public static final String IS_SENT = "IS_SENT";
    public static final String MESSAGE_ID = "MESSAGE_ID";
    public static final String MESSAGE = "MESSAGE";
    public static final String RECEIVED_TIME = "RECEIVED_TIME";
    public static final String STATUS = "STATUS";
    public static final String IS_READ = "IS_READ";
    /*new field for image sharing*/
    public static final String TYPE = "TYPE";
    public static final String BASE64 = "BASE64";
    public static final String MEDIA_URL = "MEDIA_URL";
    public static final String LOCAL_PATH = "LOCAL_PATH";
    public static final String IS_MEDIA = "IS_MEDIA";

    //Sender detail
    public static final String FROM_PLAIN_JID = "FROM_PLAIN_JID";//This is always message sender JID
    public static final String FROM_USER_ID = "FROM_USER_ID";
    public static final String FROM_USER_NAME = "FROM_USER_NAME";
    public static final String FROM_EMAIL = "FROM_EMAIL";
    public static final String FROM_PHONE_NUMBER = "FROM_PHONE_NUMBER";
    public static final String FROM_DISPLAY_NAME = "FROM_DISPLAY_NAME";
    public static final String FROM_PROFILE_PIC = "FROM_PROFILE_PIC";

    public static final String TABLE_MESSAGE = "CREATE TABLE " + TblMessages.TABLE_NAME + " ( " +
            " `" + TblMessages.ID + "` INTEGER PRIMARY KEY AUTOINCREMENT, " +
            " `" + TblMessages.CONTACT_ID + "` INTEGER, " +
            " `" + TblMessages.JID + "` TEXT, " +
            " `" + TblMessages.IS_SENT + "` INTEGER, " +
            " `" + TblMessages.MESSAGE_ID + "` TEXT, " +
            " `" + TblMessages.MESSAGE + "` TEXT, " +
            " `" + TblMessages.RECEIVED_TIME + "` INTEGER, " +
            " `" + TblMessages.STATUS + "` TEXT, " +
            " `" + TblMessages.IS_READ + "` INTEGER, " +
            " `" + TblMessages.TYPE + "` TEXT," +
            " `" + TblMessages.BASE64 + "` TEXT," +
            " `" + TblMessages.MEDIA_URL + "` TEXT," +
            " `" + TblMessages.LOCAL_PATH + "` TEXT, " +
            " `" + TblMessages.IS_MEDIA + "` TEXT, " +

            //Sender details
            " `" + TblMessages.FROM_PLAIN_JID + "` TEXT, " +
            " `" + TblMessages.FROM_USER_ID + "` TEXT, " +
            " `" + TblMessages.FROM_USER_NAME + "` TEXT, " +
            " `" + TblMessages.FROM_EMAIL + "` TEXT, " +
            " `" + TblMessages.FROM_PHONE_NUMBER + "` TEXT, " +
            " `" + TblMessages.FROM_DISPLAY_NAME + "` TEXT, " +
            " `" + TblMessages.FROM_PROFILE_PIC + "` TEXT " +
            ")";

    private Context mContext;
    //private Object lock = new Object();

    public TblMessages(Context context) {
        super(context);
        mContext = context;
        database = getWritableDatabase();
    }

    public long saveMessage(ChatMessage chatMessage) {
        synchronized (lock) {
            database = getWritableDatabase();
            if (isMessageAlreadyReceived(chatMessage.messageID)) {
                return -1;
            }
            ContentValues mContentValues = new ContentValues();
            mContentValues.put(TblMessages.CONTACT_ID, chatMessage.contactID);
            mContentValues.put(TblMessages.JID, chatMessage.JID);
            mContentValues.put(TblMessages.IS_SENT, chatMessage.isSent);
            mContentValues.put(TblMessages.MESSAGE_ID, chatMessage.messageID);
            mContentValues.put(TblMessages.MESSAGE, chatMessage.message);
            mContentValues.put(TblMessages.RECEIVED_TIME, chatMessage.receivedTime);
            mContentValues.put(TblMessages.STATUS, chatMessage.getReceivedStatusString());
            mContentValues.put(TblMessages.IS_READ, chatMessage.isRead);
            mContentValues.put(TblMessages.TYPE, chatMessage.type);
            mContentValues.put(TblMessages.BASE64, chatMessage.base64);
            mContentValues.put(TblMessages.MEDIA_URL, chatMessage.mediaUrl);
            mContentValues.put(TblMessages.LOCAL_PATH, chatMessage.localPath);
            mContentValues.put(TblMessages.IS_MEDIA, chatMessage.isMedia);

            //Sender details
            mContentValues.put(TblMessages.FROM_PLAIN_JID, chatMessage.fromPlainJID);
            mContentValues.put(TblMessages.FROM_USER_ID, chatMessage.fromUserID);
            mContentValues.put(TblMessages.FROM_USER_NAME, chatMessage.fromUserName);
            mContentValues.put(TblMessages.FROM_EMAIL, chatMessage.fromEmail);
            mContentValues.put(TblMessages.FROM_PHONE_NUMBER, chatMessage.fromPhoneNumber);
            mContentValues.put(TblMessages.FROM_DISPLAY_NAME, chatMessage.fromDisplayName);
            mContentValues.put(TblMessages.FROM_PROFILE_PIC, chatMessage.fromProfilePic);
            return database.insert(TblMessages.TABLE_NAME, null, mContentValues);
        }
    }

    public List<ChatMessage> getUnreadMessageByJID(String JID) {
        synchronized (lock) {
            database = getWritableDatabase();
            List<ChatMessage> chatMessages = new ArrayList<>();
            Cursor mCursor = null;
            try {
                String sql = "select * from " + TblMessages.TABLE_NAME + " where " + TblMessages.JID + "='" + JID + "' and " + TblMessages.IS_READ + "=0 order by " + TblMessages.RECEIVED_TIME + " desc";
                mCursor = database.rawQuery(sql, null);
                if (mCursor.getCount() > 0) {
                    mCursor.moveToFirst();
                    do {
                        ChatMessage chatMessage = getChatMessageFromCursor(mCursor);
                        if (chatMessage != null) {
                            chatMessages.add(chatMessage);
                        }

                    } while (mCursor.moveToNext());
                    mCursor.close();

                    // change read STATUS to 1
                    ContentValues mContentValues = new ContentValues();
                    mContentValues.put(TblMessages.IS_READ, 1);
                    database.update(TblMessages.TABLE_NAME, mContentValues, TblMessages.JID + "=? and " + TblMessages.IS_READ + "=?", new String[]{JID, "0"});

                    Collections.reverse(chatMessages);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mCursor != null && !mCursor.isClosed()) {
                    mCursor.close();
                }
            }
            return chatMessages;
        }
    }

    /**
     * We have xmpp chat with app user and sms with non-app user so we will use CONTACT_ID if working with non-app because JID null.
     *
     * @param contactID
     * @param JID
     * @param messageID
     * @return
     */
    public List<ChatMessage> getMessageByJIDGreaterThenGivenID(long contactID, String JID, long messageID) {
        synchronized (lock) {
            database = getWritableDatabase();
            List<ChatMessage> chatMessages = new ArrayList<>();
            Cursor mCursor = null;
            try {
                String sql;
                if (TextUtils.isEmpty(JID)) {
                    sql = "select * from " + TblMessages.TABLE_NAME
                            + " where " + TblMessages.CONTACT_ID + "=" + contactID + " and " + TblMessages.ID + ">" + messageID
                            /*+ " and " + TblMessages.is_inactive + "=0 "*/
                            + " order by " + TblMessages.RECEIVED_TIME + " desc";

                } else {
                    //Doing this all because TblMessage column JID is having all JID's with the SMACK RESOURCE value
                    /*if (JID != null && !JID.endsWith("/" + XMPPInfo.XMPP_RESOURCE)) {
                        JID = JID.concat("/" + XMPPInfo.XMPP_RESOURCE);
                    }*/
                    sql = "select * from " + TblMessages.TABLE_NAME
                            + " where " + TblMessages.JID + "='" + JID + "' and " + TblMessages.ID + ">" + messageID
                            //+ " and " + TblMessages.is_inactive + "=0 "
                            + " order by " + TblMessages.RECEIVED_TIME + " desc";
                }
                mCursor = database.rawQuery(sql, null);
                if (mCursor.getCount() > 0) {
                    mCursor.moveToFirst();
                    do {
                        ChatMessage chatMessage = getChatMessageFromCursor(mCursor);
                        if (chatMessage != null) {
                            chatMessages.add(chatMessage);
                        }

                    } while (mCursor.moveToNext());
                    mCursor.close();

                    // change read STATUS to 1
                    ContentValues mContentValues = new ContentValues();
                    mContentValues.put(TblMessages.IS_READ, 1);
                    database.update(TblMessages.TABLE_NAME, mContentValues, TblMessages.JID + "=? and " + TblMessages.ID + ">?", new String[]{JID, "" + messageID});

                    Collections.reverse(chatMessages);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mCursor != null && !mCursor.isClosed()) {
                    mCursor.close();
                }
            }
            return chatMessages;
        }
    }

    /**
     * We have xmpp chat with app user and sms with non-app user so we will use CONTACT_ID if working with non-app because JID null.
     *
     * @param contactID
     * @param JID
     * @param start
     * @param limit
     * @return
     */
    public List<ChatMessage> getMessageByJID(long contactID, String JID, int start, int limit) {
        synchronized (lock) {
            database = getWritableDatabase();
            List<ChatMessage> chatMessages = new ArrayList<>();
            Cursor mCursor = null;
            try {
                String sql = "";
                if (TextUtils.isEmpty(JID)) {
                    sql = "select * from " + TblMessages.TABLE_NAME + " where " /*+ TblMessages.is_inactive + "=0 and "*/ + TblMessages.CONTACT_ID + "=" + contactID + " order by " + TblMessages.RECEIVED_TIME + " desc ";
                } else {
                    sql = "select * from " + TblMessages.TABLE_NAME + " where " /*+ TblMessages.is_inactive + "=0 and " */ + TblMessages.JID + "='" + JID + "' order by " + TblMessages.RECEIVED_TIME + " desc ";
                }
                if (limit > 0) {
                    sql += " limit " + start + "," + limit;
                }
                mCursor = database.rawQuery(sql, null);
                if (mCursor.getCount() > 0) {
                    mCursor.moveToFirst();
                    do {
                        ChatMessage chatMessage = getChatMessageFromCursor(mCursor);
                        if (chatMessage != null) {
                            chatMessages.add(chatMessage);
                        }

                    } while (mCursor.moveToNext());
                    mCursor.close();

                    // change read STATUS to 1
                    ContentValues mContentValues = new ContentValues();
                    mContentValues.put(TblMessages.IS_READ, 1);
                    if (TextUtils.isEmpty(JID)) {
                        database.update(TblMessages.TABLE_NAME, mContentValues, TblMessages.CONTACT_ID + "=? and " + TblMessages.IS_READ + "=?", new String[]{contactID + "", "0"});
                    } else {
                        database.update(TblMessages.TABLE_NAME, mContentValues, TblMessages.JID + "=? and " + TblMessages.IS_READ + "=?", new String[]{JID, "0"});
                    }

                    Collections.reverse(chatMessages);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mCursor != null && !mCursor.isClosed()) {
                    mCursor.close();
                }
            }
            return chatMessages;
        }
    }

    public long isChatMessageAvailable(String JID) {
        synchronized (lock) {
            if (!TextUtils.isEmpty(JID)) {
                database = getWritableDatabase();
                List<ChatMessage> chatMessages = new ArrayList<>();
                String sql = "select * from " + TblMessages.TABLE_NAME + " where " + TblMessages.JID + "='" + JID + "' order by " + TblMessages.RECEIVED_TIME + " desc ";
                Cursor mCursor = database.rawQuery(sql, null);
                if (mCursor.getCount() > 0) {
                    return mCursor.getCount();
                }
                mCursor.close();
            }
            return -1;
        }
    }

    public long isChatMessageReceived(String JID) {
        synchronized (lock) {
            Cursor mCursor = null;
            try {
                if (!TextUtils.isEmpty(JID)) {
                    database = getWritableDatabase();
                    List<ChatMessage> chatMessages = new ArrayList<>();
                    String sql = "select * from " + TblMessages.TABLE_NAME + " where " + TblMessages.IS_SENT + "=0 and " + TblMessages.JID + "='" + JID + "' order by " + TblMessages.RECEIVED_TIME + " desc ";
                    mCursor = database.rawQuery(sql, null);
                    if (mCursor.getCount() > 0) {
                        return mCursor.getCount();
                    }
                    mCursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mCursor != null && !mCursor.isClosed()) {
                    mCursor.close();
                }
            }
            return -1;
        }
    }

    public long isChatMessageSent(String JID) {
        synchronized (lock) {
            Cursor mCursor = null;
            try {
                if (!TextUtils.isEmpty(JID)) {
                    database = getWritableDatabase();
                    List<ChatMessage> chatMessages = new ArrayList<>();
                    String sql = "select * from " + TblMessages.TABLE_NAME + " where " + TblMessages.IS_SENT + "=1 and " + TblMessages.JID + "='" + JID + "' order by " + TblMessages.RECEIVED_TIME + " desc ";
                    mCursor = database.rawQuery(sql, null);
                    if (mCursor.getCount() > 0) {
                        return mCursor.getCount();
                    }
                    mCursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mCursor != null && !mCursor.isClosed()) {
                    mCursor.close();
                }
            }
            return -1;
        }
    }

    /**
     * We have xmpp chat with app user and sms with non-app user so we will use CONTACT_ID if working with non-app because JID null.
     *
     * @param JID
     * @return
     */
    public List<Chat> getSentMessage(long userID, String JID) {
        synchronized (lock) {
            database = getWritableDatabase();
            ArrayList<Chat> mChats = new ArrayList<>();
            String sql = "";
            //select * from tbl_messages inner join tbl_contacts on tbl_contacts.MSG_ID=tbl_messages.ID where IS_MATCH=0 and IS_SENT=1 and JID!='chat50hyvwb' order by RECEIVED_TIME desc
            if (TextUtils.isEmpty(JID)) {
                sql = "select " + TblContacts.TABLE_NAME + ".*,"
                        + TblMessages.TABLE_NAME + "." + TblMessages.CONTACT_ID + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.IS_SENT + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.MESSAGE_ID + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.MESSAGE + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.RECEIVED_TIME + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.STATUS + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.TYPE + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.IS_READ
                        + " from " + TblMessages.TABLE_NAME
                        + " inner join " + TblContacts.TABLE_NAME + " on " + TblContacts.TABLE_NAME + "." + TblContacts.ID + "=" + TblMessages.TABLE_NAME + "." + TblMessages.CONTACT_ID
                        + " where " + TblContacts.TABLE_NAME + "." + TblContacts.IS_MATCH + "!=1 and " + TblMessages.TABLE_NAME + "." + TblMessages.IS_SENT + "=1 and " + TblMessages.TABLE_NAME + "." + TblMessages.FROM_USER_ID + "='" + userID + "' order by " + TblMessages.RECEIVED_TIME + " desc ";
            } else {
                sql = "select " + TblContacts.TABLE_NAME + ".*,"
                        + TblMessages.TABLE_NAME + "." + TblMessages.CONTACT_ID + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.IS_SENT + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.MESSAGE_ID + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.MESSAGE + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.RECEIVED_TIME + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.STATUS + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.TYPE + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.IS_READ
                        + " from " + TblMessages.TABLE_NAME
                        + " inner join " + TblContacts.TABLE_NAME + " on " + TblContacts.TABLE_NAME + "." + TblContacts.ID + "=" + TblMessages.TABLE_NAME + "." + TblMessages.CONTACT_ID
                        + " where " + TblContacts.TABLE_NAME + "." + TblContacts.IS_MATCH + "!=1 and " + TblMessages.TABLE_NAME + "." + TblMessages.IS_SENT + "=1 and " + TblMessages.TABLE_NAME + "." + TblMessages.FROM_PLAIN_JID + "='" + JID + "' order by " + TblMessages.RECEIVED_TIME + " desc ";
            }
            Cursor mCursor = database.rawQuery(sql, null);
            if (mCursor.getCount() > 0) {
                mCursor.moveToFirst();
                do {
                    int id = mCursor.getInt(mCursor.getColumnIndex(TblContacts.ID));
                    String jid = mCursor.getString(mCursor.getColumnIndex(TblContacts.JID));
                    String userId = mCursor.getString(mCursor.getColumnIndex(TblContacts.USER_ID));
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

                    Chat chat = new Chat();
                    chat.contact = new Contact(id, jid, userId, userName, email, phoneNumber, profilePic,
                            displayName, status, presence, isBlocked, lastMsgReceivedTime, isAppUser, msgID, isNotificationMute, chatBackground, 0, location, lastBuzzTime, isMatch);

                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.ID = id;
                    chatMessage.JID = jid;
                    chatMessage.isSent = mCursor.getInt(mCursor.getColumnIndex(TblMessages.IS_SENT));
                    chatMessage.contactID = mCursor.getLong(mCursor.getColumnIndex(TblMessages.CONTACT_ID));
                    chatMessage.messageID = mCursor.getString(mCursor.getColumnIndex(TblMessages.MESSAGE_ID));
                    chatMessage.message = mCursor.getString(mCursor.getColumnIndex(TblMessages.MESSAGE));
                    chatMessage.receivedTime = mCursor.getLong(mCursor.getColumnIndex(TblMessages.RECEIVED_TIME));
                    chatMessage.isRead = mCursor.getInt(mCursor.getColumnIndex(TblMessages.IS_READ));
                    chatMessage.type = mCursor.getString(mCursor.getColumnIndex(TblMessages.TYPE));

                    chat.chatMessage = chatMessage;

                    //to stop showing recent chat without any message
                    if (!TextUtils.isEmpty(chatMessage.messageID)) {
                        mChats.add(chat);
                    }
                } while (mCursor.moveToNext());
                mCursor.close();
                Collections.reverse(mChats);
            }
            return mChats;
        }
    }

    /**
     * We have xmpp chat with app user and sms with non-app user so we will use CONTACT_ID if working with non-app because JID null.
     *
     * @param
     * @return
     */
    public List<Chat> getSentMessageForEveryUser(/*long userID, String JID*/) {
        synchronized (lock) {
            database = getWritableDatabase();
            ArrayList<Chat> mChats = new ArrayList<>();
            Cursor mCursor = null;
            try {
                String sql = "";
                //select * from tbl_messages inner join tbl_contacts on tbl_contacts.MSG_ID=tbl_messages.ID where IS_MATCH=0 and IS_SENT=1 and JID!='chat50hyvwb' order by RECEIVED_TIME desc
            /*if (TextUtils.isEmpty(JID)) {
                sql = "select " + TblContacts.TABLE_NAME + ".*,"
                        + TblMessages.TABLE_NAME + "." + TblMessages.CONTACT_ID + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.IS_SENT + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.MESSAGE_ID + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.MESSAGE + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.RECEIVED_TIME + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.STATUS + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.TYPE + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.IS_READ
                        + " from " + TblMessages.TABLE_NAME
                        + " inner join " + TblContacts.TABLE_NAME + " on " + TblContacts.TABLE_NAME + "." + TblContacts.ID + "=" + TblMessages.TABLE_NAME + "." + TblMessages.CONTACT_ID
                        + " where " *//*+ TblContacts.TABLE_NAME + "." + TblContacts.IS_MATCH + "!=1 and "*//* + TblMessages.TABLE_NAME + "." + TblMessages.IS_SENT + "=1 and " + TblMessages.TABLE_NAME + "." + TblMessages.FROM_USER_ID + "='" + userID + "' group by " + TblMessages.TABLE_NAME + "." + TblMessages.JID + " order by " + TblMessages.RECEIVED_TIME + " desc ";
            } else {*/
                sql = "select " + TblContacts.TABLE_NAME + ".*,"
                        + TblMessages.TABLE_NAME + "." + TblMessages.CONTACT_ID + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.IS_SENT + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.MESSAGE_ID + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.MESSAGE + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.RECEIVED_TIME + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.STATUS + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.TYPE + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.IS_READ
                        + " from " + TblMessages.TABLE_NAME
                        + " inner join " + TblContacts.TABLE_NAME + " on " + TblContacts.TABLE_NAME + "." + TblContacts.ID + "=" + TblMessages.TABLE_NAME + "." + TblMessages.CONTACT_ID
                        + " where " /*+ TblContacts.TABLE_NAME + "." + TblContacts.IS_MATCH + "!=1 and "*/ + TblMessages.TABLE_NAME + "." + TblMessages.IS_SENT + "=1 " +/*"and " + TblMessages.TABLE_NAME + "." + TblMessages.FROM_PLAIN_JID + "='" + JID + "'*/" group by " + TblMessages.TABLE_NAME + "." + TblMessages.JID + " order by " + TblMessages.RECEIVED_TIME + " desc ";
                //}
                mCursor = database.rawQuery(sql, null);
                if (mCursor.getCount() > 0) {
                    mCursor.moveToFirst();
                    do {
                        int id = mCursor.getInt(mCursor.getColumnIndex(TblContacts.ID));
                        String jid = mCursor.getString(mCursor.getColumnIndex(TblContacts.JID));
                        String userId = mCursor.getString(mCursor.getColumnIndex(TblContacts.USER_ID));
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

                        Chat chat = new Chat();
                        chat.contact = new Contact(id, jid, userId, userName, email, phoneNumber, profilePic,
                                displayName, status, presence, isBlocked, lastMsgReceivedTime, isAppUser, msgID, isNotificationMute, chatBackground, 0, location, lastBuzzTime, isMatch);

                        ChatMessage chatMessage = new ChatMessage();
                        chatMessage.ID = id;
                        chatMessage.JID = jid;
                        chatMessage.isSent = mCursor.getInt(mCursor.getColumnIndex(TblMessages.IS_SENT));
                        chatMessage.contactID = mCursor.getLong(mCursor.getColumnIndex(TblMessages.CONTACT_ID));
                        chatMessage.messageID = mCursor.getString(mCursor.getColumnIndex(TblMessages.MESSAGE_ID));
                        chatMessage.message = mCursor.getString(mCursor.getColumnIndex(TblMessages.MESSAGE));
                        chatMessage.receivedTime = mCursor.getLong(mCursor.getColumnIndex(TblMessages.RECEIVED_TIME));
                        chatMessage.isRead = mCursor.getInt(mCursor.getColumnIndex(TblMessages.IS_READ));
                        chatMessage.type = mCursor.getString(mCursor.getColumnIndex(TblMessages.TYPE));

                        chat.chatMessage = chatMessage;

                        //to stop showing recent chat without any message
                        if (!TextUtils.isEmpty(chatMessage.messageID)) {
                            mChats.add(chat);
                        }
                    } while (mCursor.moveToNext());
                    mCursor.close();
                    //Collections.reverse(mChats);
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

    /**
     * We have xmpp chat with app user and sms with non-app user so we will use CONTACT_ID if working with non-app because JID null.
     *
     * @param JID
     * @return
     */
    public List<ChatMessage> getReceiveMessage(long userID, String JID) {
        synchronized (lock) {
            database = getWritableDatabase();
            List<ChatMessage> chatMessages = new ArrayList<>();
            Cursor mCursor = null;
            try {
                String sql = "";
                if (TextUtils.isEmpty(JID)) {
                    sql = "select * from " + TblMessages.TABLE_NAME
                            + " inner join " + TblContacts.TABLE_NAME + " on " + TblContacts.TABLE_NAME + "." + TblContacts.ID + "=" + TblMessages.TABLE_NAME + "." + TblMessages.CONTACT_ID
                            + " where " + TblContacts.TABLE_NAME + "." + TblContacts.IS_MATCH + "!=1 and " + TblMessages.TABLE_NAME + "." + TblMessages.IS_SENT + "!=1 and " + TblMessages.TABLE_NAME + "." + TblMessages.FROM_USER_ID + "!='" + userID + "' order by " + TblMessages.RECEIVED_TIME + " desc ";
                } else {
                    sql = "select * from " + TblMessages.TABLE_NAME
                            + " inner join " + TblContacts.TABLE_NAME + " on " + TblContacts.TABLE_NAME + "." + TblContacts.ID + "=" + TblMessages.TABLE_NAME + "." + TblMessages.CONTACT_ID
                            + " where " + TblContacts.TABLE_NAME + "." + TblContacts.IS_MATCH + "!=1 and " + TblMessages.TABLE_NAME + "." + TblMessages.IS_SENT + "!=1 and " + TblMessages.TABLE_NAME + "." + TblMessages.FROM_PLAIN_JID + "!='" + JID + "' order by " + TblMessages.RECEIVED_TIME + " desc ";
                }
                mCursor = database.rawQuery(sql, null);
                if (mCursor.getCount() > 0) {
                    mCursor.moveToFirst();
                    do {
                        ChatMessage chatMessage = getChatMessageFromCursor(mCursor);
                        if (chatMessage != null) {
                            chatMessages.add(chatMessage);
                        }
                    } while (mCursor.moveToNext());
                    mCursor.close();
                    Collections.reverse(chatMessages);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mCursor != null && !mCursor.isClosed()) {
                    mCursor.close();
                }
            }
            return chatMessages;
        }
    }

    /**
     * We have xmpp chat with app user and sms with non-app user so we will use CONTACT_ID if working with non-app because JID null.
     *
     * @param JID
     * @return
     */
    public List<ChatMessage> getReceiveMessageForEveryUser(long userID, String JID) {
        synchronized (lock) {
            database = getWritableDatabase();
            List<ChatMessage> chatMessages = new ArrayList<>();
            Cursor mCursor = null;
            try {
                String sql = "";
                if (TextUtils.isEmpty(JID)) {
                    sql = "select * from " + TblMessages.TABLE_NAME
                            + " inner join " + TblContacts.TABLE_NAME + " on " + TblContacts.TABLE_NAME + "." + TblContacts.ID + "=" + TblMessages.TABLE_NAME + "." + TblMessages.CONTACT_ID
                            + " where " /*+ TblContacts.TABLE_NAME + "." + TblContacts.IS_MATCH + "!=1 and "*/ + TblMessages.TABLE_NAME + "." + TblMessages.IS_SENT + "!=1 and " + TblMessages.TABLE_NAME + "." + TblMessages.FROM_USER_ID + "!='" + userID + "' group by " + TblMessages.TABLE_NAME + "." + TblMessages.JID + " order by " + TblMessages.RECEIVED_TIME + " desc";
                } else {
                    sql = "select * from " + TblMessages.TABLE_NAME
                            + " inner join " + TblContacts.TABLE_NAME + " on " + TblContacts.TABLE_NAME + "." + TblContacts.ID + "=" + TblMessages.TABLE_NAME + "." + TblMessages.CONTACT_ID
                            + " where " /*+ TblContacts.TABLE_NAME + "." + TblContacts.IS_MATCH + "!=1 and "*/ + TblMessages.TABLE_NAME + "." + TblMessages.IS_SENT + "!=1 and " + TblMessages.TABLE_NAME + "." + TblMessages.FROM_PLAIN_JID + "!='" + JID + "' group by " + TblMessages.TABLE_NAME + "." + TblMessages.JID + " order by " + TblMessages.RECEIVED_TIME + " desc";
                }
                mCursor = database.rawQuery(sql, null);
                if (mCursor.getCount() > 0) {
                    mCursor.moveToFirst();
                    do {
                        ChatMessage chatMessage = getChatMessageFromCursor(mCursor);
                        if (chatMessage != null) {
                            chatMessages.add(chatMessage);
                        }
                    } while (mCursor.moveToNext());
                    mCursor.close();
                    Collections.reverse(chatMessages);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mCursor != null && !mCursor.isClosed()) {
                    mCursor.close();
                }
            }
            return chatMessages;
        }
    }

    /**
     * We have xmpp chat with app user and sms with non-app user so we will use CONTACT_ID if working with non-app because JID null.
     *
     * @param
     * @return
     */
    public List<Chat> getReceiveChatForEveryUser(/*long userID, String JID*/) {
        synchronized (lock) {
            database = getWritableDatabase();
            ArrayList<Chat> mChats = null;
            Cursor mCursor = null;
            try {
                mChats = new ArrayList<>();
                String sql = "";
                //select * from tbl_messages inner join tbl_contacts on tbl_contacts.MSG_ID=tbl_messages.ID where IS_MATCH=0 and IS_SENT=1 and JID!='chat50hyvwb' order by RECEIVED_TIME desc
            /*if (TextUtils.isEmpty(JID)) {
                sql = "select " + TblContacts.TABLE_NAME + ".*,"
                        + TblMessages.TABLE_NAME + "." + TblMessages.CONTACT_ID + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.IS_SENT + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.MESSAGE_ID + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.MESSAGE + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.RECEIVED_TIME + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.STATUS + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.TYPE + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.IS_READ
                        //
                        + "," + "(select count(" + TblMessages.TABLE_NAME + "." + TblMessages.ID + ") from " + TblMessages.TABLE_NAME + " where "
                        + TblMessages.TABLE_NAME + "." + TblMessages.CONTACT_ID + "=" + TblContacts.TABLE_NAME + "." + TblContacts.ID +
                        //+" and " + TblMessages.TABLE_NAME + "." + TblMessages.is_inactive + "=0"
                        " and " + TblMessages.TABLE_NAME + "." + TblMessages.IS_READ + "=0)" + " as unread_count "
                        //
                        + " from " + TblMessages.TABLE_NAME
                        + " inner join " + TblContacts.TABLE_NAME + " on " + TblContacts.TABLE_NAME + "." + TblContacts.ID + "=" + TblMessages.TABLE_NAME + "." + TblMessages.CONTACT_ID
                        + " where " *//*+ TblContacts.TABLE_NAME + "." + TblContacts.IS_MATCH + "!=1 and "*//* + TblMessages.TABLE_NAME + "." + TblMessages.IS_SENT + "=0 " +*//*and " + TblMessages.TABLE_NAME + "." + TblMessages.FROM_USER_ID + "='" + userID + "'*//*" group by " + TblMessages.TABLE_NAME + "." + TblMessages.JID + " order by " + TblMessages.RECEIVED_TIME + " desc ";
            } else {*/
                sql = "select " + TblContacts.TABLE_NAME + ".*,"
                        + TblMessages.TABLE_NAME + "." + TblMessages.CONTACT_ID + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.IS_SENT + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.MESSAGE_ID + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.MESSAGE + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.RECEIVED_TIME + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.STATUS + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.TYPE + ","
                        + TblMessages.TABLE_NAME + "." + TblMessages.IS_READ
                        //
                        + "," + "(select count(" + TblMessages.TABLE_NAME + "." + TblMessages.ID + ") from " + TblMessages.TABLE_NAME + " where "
                        + TblMessages.TABLE_NAME + "." + TblMessages.CONTACT_ID + "=" + TblContacts.TABLE_NAME + "." + TblContacts.ID +
                        //+" and " + TblMessages.TABLE_NAME + "." + TblMessages.is_inactive + "=0"
                        " and " + TblMessages.TABLE_NAME + "." + TblMessages.IS_READ + "=0)" + " as unread_count "
                        //
                        + " from " + TblMessages.TABLE_NAME
                        + " inner join " + TblContacts.TABLE_NAME + " on " + TblContacts.TABLE_NAME + "." + TblContacts.ID + "=" + TblMessages.TABLE_NAME + "." + TblMessages.CONTACT_ID
                        + " where " /*+ TblContacts.TABLE_NAME + "." + TblContacts.IS_MATCH + "!=1 and "*/ + TblMessages.TABLE_NAME + "." + TblMessages.IS_SENT + "=0 " +/*and " + TblMessages.TABLE_NAME + "." + TblMessages.FROM_PLAIN_JID + "='" + JID + "'*/" group by " + TblMessages.TABLE_NAME + "." + TblMessages.JID + " order by " + TblMessages.RECEIVED_TIME + " desc ";
                //}
                mCursor = database.rawQuery(sql, null);
                if (mCursor.getCount() > 0) {
                    mCursor.moveToFirst();
                    do {
                        int id = mCursor.getInt(mCursor.getColumnIndex(TblContacts.ID));
                        String jid = mCursor.getString(mCursor.getColumnIndex(TblContacts.JID));
                        String userId = mCursor.getString(mCursor.getColumnIndex(TblContacts.USER_ID));
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
                        int unreadCount = mCursor.getInt(mCursor.getColumnIndex("unread_count"));

                        Chat chat = new Chat();
                        chat.contact = new Contact(id, jid, userId, userName, email, phoneNumber, profilePic,
                                displayName, status, presence, isBlocked, lastMsgReceivedTime, isAppUser, msgID, isNotificationMute, chatBackground, unreadCount, location, lastBuzzTime, isMatch);

                        ChatMessage chatMessage = new ChatMessage();
                        chatMessage.ID = id;
                        chatMessage.JID = jid;
                        chatMessage.isSent = mCursor.getInt(mCursor.getColumnIndex(TblMessages.IS_SENT));
                        chatMessage.contactID = mCursor.getLong(mCursor.getColumnIndex(TblMessages.CONTACT_ID));
                        chatMessage.messageID = mCursor.getString(mCursor.getColumnIndex(TblMessages.MESSAGE_ID));
                        chatMessage.message = mCursor.getString(mCursor.getColumnIndex(TblMessages.MESSAGE));
                        chatMessage.receivedTime = mCursor.getLong(mCursor.getColumnIndex(TblMessages.RECEIVED_TIME));
                        chatMessage.isRead = mCursor.getInt(mCursor.getColumnIndex(TblMessages.IS_READ));
                        chatMessage.type = mCursor.getString(mCursor.getColumnIndex(TblMessages.TYPE));

                        chat.chatMessage = chatMessage;

                        //to stop showing recent chat without any message
                        if (!TextUtils.isEmpty(chatMessage.messageID)) {
                            mChats.add(chat);
                        }
                    } while (mCursor.moveToNext());
                    mCursor.close();
                    //Collections.reverse(mChats);
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


    /**
     * We have xmpp chat with app user and sms with non-app user so we will use CONTACT_ID if working with non-app because JID null.
     *
     * @param contactID
     * @param JID
     * @param start
     * @param limit
     * @return
     */
    public List<ChatMessage> getMessageForJIDBySearch(long contactID, String JID, int start, int limit, String search) {
        synchronized (lock) {
            database = getWritableDatabase();
            List<ChatMessage> chatMessages = new ArrayList<>();
            String sql = "";
            if (TextUtils.isEmpty(JID)) {
                sql = "select * from " + TblMessages.TABLE_NAME + " where " + TblMessages.MESSAGE + "IN (" + search + ") and " /*+ TblMessages.is_inactive + "=0 and " */ + TblMessages.CONTACT_ID + "=" + contactID + " order by " + TblMessages.RECEIVED_TIME + " desc ";
            } else {
                sql = "select * from " + TblMessages.TABLE_NAME + " where " + TblMessages.MESSAGE + "IN (" + search + ") and " /*+ TblMessages.is_inactive + "=0 and "*/ + TblMessages.JID + "='" + JID + "' order by " + TblMessages.RECEIVED_TIME + " desc ";
            }
            if (limit > 0) {
                sql += " limit " + start + "," + limit;
            }
            Cursor mCursor = database.rawQuery(sql, null);
            if (mCursor.getCount() > 0) {
                mCursor.moveToFirst();
                do {
                    ChatMessage chatMessage = getChatMessageFromCursor(mCursor);
                    if (chatMessage != null) {
                        chatMessages.add(chatMessage);
                    }

                } while (mCursor.moveToNext());
                mCursor.close();

                // change read STATUS to 1
                ContentValues mContentValues = new ContentValues();
                mContentValues.put(TblMessages.IS_READ, 1);
                if (TextUtils.isEmpty(JID)) {
                    database.update(TblMessages.TABLE_NAME, mContentValues, TblMessages.CONTACT_ID + "=? and " + TblMessages.IS_READ + "=?", new String[]{contactID + "", "0"});
                } else {
                    database.update(TblMessages.TABLE_NAME, mContentValues, TblMessages.JID + "=? and " + TblMessages.IS_READ + "=?", new String[]{JID, "0"});
                }

                Collections.reverse(chatMessages);
            }
            return chatMessages;
        }
    }

    public boolean isMessageAlreadyReceived(String messageID) {
        synchronized (lock) {
            database = getWritableDatabase();
            Cursor mCursor = null;
            int count = 0;
            try {
                String query = "select * from " + TblMessages.TABLE_NAME + " where " + TblMessages.MESSAGE_ID + " = '" + messageID + "' LIMIT 1";
                mCursor = database.rawQuery(query, null);
                count = mCursor.getCount();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mCursor != null && !mCursor.isClosed()) {
                    mCursor.close();
                }
            }
            return (count > 0) ? true : false;
        }
    }

    private ChatMessage getChatMessageFromCursor(Cursor mCursor) {
        if (mCursor != null && !mCursor.isClosed()) {
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.ID = mCursor.getLong(mCursor.getColumnIndex(TblMessages.ID));
            chatMessage.contactID = mCursor.getLong(mCursor.getColumnIndex(TblMessages.CONTACT_ID));
            chatMessage.JID = mCursor.getString(mCursor.getColumnIndex(TblMessages.JID));
            chatMessage.isSent = mCursor.getInt(mCursor.getColumnIndex(TblMessages.IS_SENT));
            chatMessage.messageID = mCursor.getString(mCursor.getColumnIndex(TblMessages.MESSAGE_ID));
            chatMessage.message = mCursor.getString(mCursor.getColumnIndex(TblMessages.MESSAGE));
            chatMessage.receivedTime = mCursor.getLong(mCursor.getColumnIndex(TblMessages.RECEIVED_TIME));
            chatMessage.mReceivedStatus = chatMessage.getReceivedStatus(mCursor.getString(mCursor.getColumnIndex(TblMessages.STATUS)));
            chatMessage.isRead = mCursor.getInt(mCursor.getColumnIndex(TblMessages.IS_READ));
            chatMessage.type = mCursor.getString(mCursor.getColumnIndex(TblMessages.TYPE));
            chatMessage.mediaUrl = mCursor.getString(mCursor.getColumnIndex(TblMessages.MEDIA_URL));
            chatMessage.localPath = mCursor.getString(mCursor.getColumnIndex(TblMessages.LOCAL_PATH));
            chatMessage.base64 = mCursor.getString(mCursor.getColumnIndex(TblMessages.BASE64));
            chatMessage.isMedia = mCursor.getString(mCursor.getColumnIndex(TblMessages.IS_MEDIA));

            chatMessage.fromPlainJID = mCursor.getString(mCursor.getColumnIndex(TblMessages.FROM_PLAIN_JID));
            chatMessage.fromUserID = mCursor.getString(mCursor.getColumnIndex(TblMessages.FROM_USER_ID));
            chatMessage.fromUserName = mCursor.getString(mCursor.getColumnIndex(TblMessages.FROM_USER_NAME));
            chatMessage.fromEmail = mCursor.getString(mCursor.getColumnIndex(TblMessages.FROM_EMAIL));
            chatMessage.fromPhoneNumber = mCursor.getString(mCursor.getColumnIndex(TblMessages.FROM_PHONE_NUMBER));
            chatMessage.fromDisplayName = mCursor.getString(mCursor.getColumnIndex(TblMessages.FROM_DISPLAY_NAME));
            chatMessage.fromProfilePic = mCursor.getString(mCursor.getColumnIndex(TblMessages.FROM_PROFILE_PIC));
            return chatMessage;
        }
        return null;
    }

    public int updateDeliveryStatusMyMessageID(String messageID, String status) {
        synchronized (lock) {
            database = getWritableDatabase();
            Cursor mCursor = null;
            try {
                ChatMessage tempChatMessage = new ChatMessage();
                tempChatMessage.mReceivedStatus = ChatMessage.ReceivedStatus.received;
                String received = tempChatMessage.getReceivedStatusString();
                tempChatMessage.mReceivedStatus = ChatMessage.ReceivedStatus.read;
                String read = tempChatMessage.getReceivedStatusString();

                String statusNotEqual = "";
                if (status.equals(received)) {
                    statusNotEqual = " and lower(" + TblMessages.STATUS + ") != '" + read + "'";
                }

                ContentValues mContentValues = new ContentValues();
                mContentValues.put(TblMessages.STATUS, status);

                mCursor = database.rawQuery("select * from " + TblMessages.TABLE_NAME + " where " + TblMessages.MESSAGE_ID + "=? LIMIT 1", new String[]{messageID});
                if (mCursor.getCount() > 0) {
                    mCursor.moveToFirst();
                    long ID = mCursor.getLong(mCursor.getColumnIndex(TblMessages.ID));
                    String JID = mCursor.getString(mCursor.getColumnIndex(TblMessages.JID));
                    if (!TextUtils.isEmpty(JID)) {
                        return database.update(TblMessages.TABLE_NAME, mContentValues, TblMessages.ID + "<= ? and " + TblMessages.JID + "=? " + statusNotEqual, new String[]{ID + "", JID});
                    }
                }
                mCursor.close();

                //return database.update(TblMessages.TABLE_NAME, mContentValues, TblMessages.MESSAGE_ID + "=?" + statusNotEqual, new String[]{MESSAGE_ID});
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

    public ChatMessage getLastMessageByJID(String JID) {
        synchronized (lock) {
            database = getWritableDatabase();
            Cursor mCursor = null;
            try {
                List<ChatMessage> chatMessages = new ArrayList<>();
                String sql = "select * from " + TblMessages.TABLE_NAME + " where " + TblMessages.JID + "='" + JID + "' order by " + TblMessages.ID + " desc limit 1";
                mCursor = database.rawQuery(sql, null);
                if (mCursor.getCount() > 0) {
                    mCursor.moveToFirst();
                    ChatMessage chatMessage = getChatMessageFromCursor(mCursor);
                    mCursor.close();
                    return chatMessage;
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

    public int updateJabberID(String JID, long ContactID) {
        synchronized (lock) {
            ContentValues mContentValues = new ContentValues();
            mContentValues.put(TblMessages.JID, JID);
            return database.update(TblMessages.TABLE_NAME, mContentValues, TblMessages.CONTACT_ID + "=?", new String[]{"" + ContactID});
        }
    }

    public void clearChatByJID(/*long CONTACT_ID, */String JID) {
        try {
            Log.e("TblMessages", "clearChatByJID");
            synchronized (lock) {
                database = getWritableDatabase();
                if (TextUtils.isEmpty(JID)) {
                    return;
                    //database.delete(TblMessages.TABLE_NAME, TblMessages.CONTACT_ID + "=? and " + TblMessages.CONTACT_ID + ">0 ", new String[]{CONTACT_ID + ""});
                } else {
                    database.delete(TblMessages.TABLE_NAME, TblMessages.JID + "=?", new String[]{JID});
                }

                //Update message last received time and msg id for the user to 0
                ContentValues mContentValues = new ContentValues();
                mContentValues.put(TblContacts.LAST_MSG_RECEIVED_TIME, 0);
                mContentValues.put(TblContacts.MSG_ID, 0);
                database.update(TblContacts.TABLE_NAME, mContentValues, TblContacts.JID + "=?", new String[]{JID});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearChatForSelectedJID(List<String> selectedJID) {
        try {
            Log.e("TblMessages", "clearChatByJID");
            if (selectedJID != null && selectedJID.size() > 0) {
                synchronized (lock) {
                    database = getWritableDatabase();
                    database.delete(TblMessages.TABLE_NAME,
                            TblMessages.JID + " IN (" + TextUtils.join(",", Collections.nCopies(selectedJID.size(), "?")) + ")",
                            Arrays.toString(selectedJID.toArray()).split("[\\[\\]]")[1].split(", "));

                    //Update message last received time and msg id for the user to 0
                    ContentValues mContentValues = new ContentValues();
                    mContentValues.put(TblContacts.LAST_MSG_RECEIVED_TIME, 0);
                    mContentValues.put(TblContacts.MSG_ID, 0);
                    database.update(TblContacts.TABLE_NAME, mContentValues, TblMessages.JID + " IN (" + TextUtils.join(",", Collections.nCopies(selectedJID.size(), "?")) + ")",
                            Arrays.toString(selectedJID.toArray()).split("[\\[\\]]")[1].split(", "));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteMessages(ArrayList<Long> deleteMessageId, String JID) {
        synchronized (lock) {
            database = getWritableDatabase();
            Cursor mCursor = null;
            try {
                if (deleteMessageId != null && deleteMessageId.size() > 0) {

				/*database.delete(TblMessages.TABLE_NAME,
                        TblMessages.ID + " IN (" + TextUtils.join(",", Collections.nCopies(deleteMessageId.size(), "?")) + ")",
						Arrays.toString(deleteMessageId.toArray()).split("[\\[\\]]")[1].split(", "));*/
                    database.delete(TblMessages.TABLE_NAME,
                            TblMessages.ID + " IN (" + TextUtils.join(",", Collections.nCopies(deleteMessageId.size(), "?")) + ")",
                            Arrays.toString(deleteMessageId.toArray()).split("[\\[\\]]")[1].split(", "));
                    String sql = "select * from " + TblMessages.TABLE_NAME + " where " + TblMessages.JID + "=? order by " + TblMessages.ID + " desc limit 1";
                    mCursor = database.rawQuery(sql, new String[]{JID});
                    if (mCursor != null && mCursor.getCount() > 0) {
                        mCursor.moveToFirst();
                        ChatMessage chatMessage = getChatMessageFromCursor(mCursor);
                        if (chatMessage != null) {
                            TblContacts tblContacts = new TblContacts(mContext);
                            tblContacts.updateMsgIdAndTime(chatMessage);
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

    public void deleteMessageByStangaId(String messageStangaId, String JID) {
        synchronized (lock) {
            database = getWritableDatabase();
            Cursor mCursor = null;
            try {
                database.delete(TblMessages.TABLE_NAME, TblMessages.MESSAGE_ID + "=?", new String[]{messageStangaId});
                String sql = "select * from " + TblMessages.TABLE_NAME + " where " + TblMessages.JID + "='" + JID + "' order by " + TblMessages.ID + " desc limit 1";
                mCursor = database.rawQuery(sql, new String[]{JID});
                if (mCursor != null && mCursor.getCount() > 0) {
                    mCursor.moveToFirst();
                    ChatMessage chatMessage = getChatMessageFromCursor(mCursor);
                    if (chatMessage != null) {
                        TblContacts tblContacts = new TblContacts(mContext);
                        tblContacts.updateMsgIdAndTime(chatMessage);
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

    public int updateLocalPath(long ID, String local_path) {
        synchronized (lock) {
            database = getWritableDatabase();
            ContentValues mContentValues = new ContentValues();

            mContentValues.put(TblMessages.LOCAL_PATH, local_path);
            return database.update(TblMessages.TABLE_NAME, mContentValues, TblMessages.ID + "=?", new String[]{"" + ID});
        }
    }

    public int getTotalUnreadChatCount() {
        synchronized (lock) {
            database = getWritableDatabase();
            String sql = "select count(" + TblMessages.TABLE_NAME + "." + TblMessages.ID + ") as unread_count from "
                    + TblMessages.TABLE_NAME + " where " + TblMessages.TABLE_NAME + "." + TblMessages.IS_READ + "=0 "
                    /*+ " and " + TblMessages.TABLE_NAME + "." + TblMessages.is_inactive + "=0"*/;
            Cursor mCursor = database.rawQuery(sql, null);
            int unread_count = 0;
            if (mCursor.getCount() > 0) {
                mCursor.moveToFirst();
                unread_count = mCursor.getInt(mCursor.getColumnIndex("unread_count"));
            }
            return unread_count;
        }
    }

    public int getTotalUnreadChatUserCount() {
        synchronized (lock) {
            database = getWritableDatabase();
            int unread_chat_user_count = 0;
            Cursor mCursor = null;
            try {
                String sql = "select count(" + TblMessages.TABLE_NAME + "." + TblMessages.ID + ") as unread_count from "
                        + TblMessages.TABLE_NAME + " where " + TblMessages.TABLE_NAME + "." + TblMessages.IS_READ + "=0 " + " group by " + TblMessages.TABLE_NAME + "." + TblMessages.JID
                        /*+ " and " + TblMessages.TABLE_NAME + "." + TblMessages.is_inactive + "=0"*/;
                mCursor = database.rawQuery(sql, null);
                unread_chat_user_count = 0;
                if (mCursor != null) {
                    unread_chat_user_count = mCursor.getCount();
                }
                mCursor.close();
            /*if (mCursor.getCount() > 0) {
                mCursor.moveToFirst();
                unread_count = mCursor.getInt(mCursor.getColumnIndex("unread_count"));
            }*/
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mCursor != null && !mCursor.isClosed()) {
                    mCursor.close();
                }
            }
            return unread_chat_user_count;
        }
    }

    public int updateReadChatCountByJID(String jid) {
        synchronized (lock) {
            SQLiteDatabase database = getWritableDatabase();
            ContentValues mContentValues = new ContentValues();
            mContentValues.put(TblMessages.IS_READ, "1");
            return database.update(TblMessages.TABLE_NAME, mContentValues, TblMessages.JID + "=?", new String[]{"" + jid});
        }
    }

    /*public void markAllInactiveMessageToActive() {
        synchronized (lock) {
            TblContacts tblContacts = new TblContacts(mContext);
            SQLiteDatabase database = getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(TblMessages.is_inactive, 0);
            database.update(TblMessages.TABLE_NAME, contentValues, TblMessages.is_inactive + "=?", new String[]{"1"});

            String sql = "select * from " + TblMessages.TABLE_NAME + " group by " + TblMessages.JID + " order by " + TblMessages.ID + " desc";
            Cursor mCursor = database.rawQuery(sql, null);
            database.beginTransaction();
            if (mCursor.getCount() > 0) {
                mCursor.moveToFirst();
                do {
                    ChatMessage chatMessage = getChatMessageFromCursor(mCursor);
                    if (chatMessage != null) {
                        tblContacts.updateLastReceivedMessageById(chatMessage);

                    }

                } while (mCursor.moveToNext());
                mCursor.close();
            }
            database.setTransactionSuccessful();
            database.endTransaction();
        }
    }*/

    /*public List<ChatMessage> getNotSendMessageByJID(String JID) {
        synchronized (lock) {
            SQLiteDatabase db = getWritableDatabase();
            List<ChatMessage> chatMessages = new ArrayList<>();

            Cursor mCursor0 = database.rawQuery("select * from " + TblBlockUsers.TABLE_NAME + " where "
                    + TblBlockUsers.JID + "=?", new String[]{JID});
            long timeMillis = 0;
            if (mCursor0.getCount() > 0) {
                mCursor0.moveToFirst();
                timeMillis = mCursor0.getLong(mCursor0.getColumnIndex(TblBlockUsers.updatedTimeMillis));
            }

            String sql = "select * from " + TblMessages.TABLE_NAME + " where "
                    + TblMessages.JID + "=? and "
                    + TblMessages.STATUS + "!=? and " + TblMessages.STATUS + "!=? and " + TblMessages.STATUS + "!=? and "
                    + TblMessages.RECEIVED_TIME + ">? order by "
                    + TblMessages.RECEIVED_TIME + " desc ";

            Cursor mCursor = database.rawQuery(sql, new String[]{JID, "received", "read", "processed", String.valueOf(timeMillis)});
            if (mCursor.getCount() > 0) {
                mCursor.moveToFirst();
                do {
                    ChatMessage chatMessage = getChatMessageFromCursor(mCursor);
                    if (chatMessage != null) {
                        chatMessages.add(chatMessage);
                    }

                } while (mCursor.moveToNext());
                mCursor.close();

                ContentValues contentValues = new ContentValues();
                contentValues.put(TblContacts.STATUS, "processed");
                database.update(TblMessages.TABLE_NAME, contentValues, TblMessages.JID + "=? and "
                        + TblMessages.STATUS + "!=? and " + TblMessages.STATUS + "!=? and " + TblMessages.STATUS + "!=? and "
                        + TblMessages.RECEIVED_TIME + ">? ", new String[]{JID, "received", "read", "processed", String.valueOf(timeMillis)});

                Collections.reverse(chatMessages);


            }
            return chatMessages;
        }
    }*/
}
