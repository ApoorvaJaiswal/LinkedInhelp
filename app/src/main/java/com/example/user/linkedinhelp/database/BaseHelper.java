package com.example.user.linkedinhelp.database;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "chat.db";

    public BaseHelper(Context context){
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table "+ Schema.QA.NAME+"("+
                "_id integer primary key autoincrement, "+
                Schema.QA.cols.QUESTION+", "+
                Schema.QA.cols.ANSWER+
                ")");
        db.execSQL("create table "+ Schema.UnansweredQuestions.NAME+"("+
                "_id integer primary key autoincrement, "+
                Schema.QA.cols.QUESTION+
                ")");
    }
}
