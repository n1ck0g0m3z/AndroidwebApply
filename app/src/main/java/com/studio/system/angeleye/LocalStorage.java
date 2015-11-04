package com.studio.system.angeleye;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.SQLException;

public class LocalStorage extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "AngelEye.db";
    private static final String ID = "_id";
    private static final String TABLE_NAME_MESSAGES = "angelEye_messages";
    public static final String MESSAGE_RECEIVER = "receiver";
    public static final String MESSAGE_SENDER = "sender";
    private static final String MESSAGE_MESSAGE = "message";
    private static final String TABLE_USER = "angelEye_user";
    private static final String USER = "user";
    private static final String USER_PASSWORD = "password";
    private static final String USER_NAME = "name";
    private static final String USER_EMAIL = "email";
    Context context;

    private static final String TABLE_MESSAGE_CREATE
            = "CREATE TABLE " + TABLE_NAME_MESSAGES
            + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + MESSAGE_RECEIVER + " VARCHAR(25), "
            + MESSAGE_SENDER + " VARCHAR(25), "
            +MESSAGE_MESSAGE + " VARCHAR(255));";

    private static final String TABLE_USER_CREATE
            = "CREATE TABLE " + TABLE_USER
            + " (" + ID + " INTEGER PRIMARY KEY, "
            + USER + " VARCHAR(25), "
            + USER_PASSWORD + " VARCHAR(25), "
            + USER_NAME + " VARCHAR(25),"
            + USER_EMAIL + " VARCHAR(255));";

    private static final String TABLE_MESSAGE_DROP =
            "DROP TABLE IF EXISTS "
                    + TABLE_NAME_MESSAGES;

    private static final String TABLE_USER_DROP =
            "DROP TABLE IF EXISTS "
                    + TABLE_USER;

    public LocalStorage(Context context){
        super(context,DATABASE_NAME,null,1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_MESSAGE_CREATE);
        db.execSQL(TABLE_USER_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(TABLE_MESSAGE_DROP);
        db.execSQL(TABLE_USER_DROP);
        onCreate(db);
    }

    public void insertUser(String user, String password, int id){
        long rowID = -1;
        try{
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(USER,user);
            values.put(USER_PASSWORD,password);
            values.put(ID,id);
            rowID=db.insert(TABLE_USER,null,values);
            db.close();
        } catch (SQLiteException e){
            Log.e ("error",e.toString());
        } finally {
            Log.d("LocalStorage ="," "+rowID);
        }
    }

    public void modifyUser(String user, String password){
        long rowID = -1;
        try{
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(USER,user);
            values.put(USER_PASSWORD,password);
            rowID=db.update(TABLE_USER,values,ID+"=1",null);
            db.close();
        } catch (SQLiteException e){
            Log.e ("error",e.toString());
        } finally {
            Log.d("LocalStorage ="," "+rowID);
        }
    }

    public void insertMessage(String sender, String receiver, String message){
        long rowId = -1;
        try{

            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(MESSAGE_RECEIVER, receiver);
            values.put(MESSAGE_SENDER, sender);
            values.put(MESSAGE_MESSAGE, message);
            rowId = db.insert(TABLE_NAME_MESSAGES, null, values);
            db.close();
        } catch (SQLiteException e){
            Log.e ("insert()", e.toString());
        } finally {
            Log.d("LocalStorage","insert(): rowId=" + rowId);
        }

    }

    public Cursor getMessage(String sender, String receiver) {

        SQLiteDatabase db = getWritableDatabase();
        String SELECT_QUERY = "SELECT * FROM " + TABLE_NAME_MESSAGES + " WHERE " + MESSAGE_SENDER + " LIKE '" + sender + "' AND " + MESSAGE_RECEIVER + " LIKE '" + receiver + "' OR " + MESSAGE_SENDER + " LIKE '" + receiver + "' AND " + MESSAGE_RECEIVER + " LIKE '" + sender + "' ORDER BY " + ID + " ASC";
        return db.rawQuery(SELECT_QUERY,null);
    }

    public Cursor getUsers(){
        SQLiteDatabase db = getReadableDatabase();
        String[] values = {USER,USER_PASSWORD};
        Cursor c = db.query(TABLE_USER,values,null,null,null,null,null);
        if(c != null) {
            c.moveToFirst();
        }
        db.close();
        c.close();
        return c;
    }

    public String getRoom(){
        SQLiteDatabase db = getReadableDatabase();
        String[] values = {USER,USER_PASSWORD};
        Cursor c = db.query(TABLE_USER,values,"_id=1",null,null,null,null);
        if(c != null){
            c.moveToFirst();
            return c.getString(0);
        }else return null;
    }

    public void deleteUser(){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_USER, "_id=1",null);
        db.close();
    }
}
