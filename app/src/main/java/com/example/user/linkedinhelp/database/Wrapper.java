package com.example.user.linkedinhelp.database;

import android.database.Cursor;
import android.database.CursorWrapper;

/**
 * Created by user on 08-11-2016.
 */
public class Wrapper extends CursorWrapper {
    public Wrapper(Cursor cursor){
        super(cursor);
    }
    public String getQAQuestion(){
        return getString(getColumnIndex(Schema.QA.cols.QUESTION));
    }
    public String getQAAnswer(){
        return getString(getColumnIndex(Schema.QA.cols.ANSWER));
    }
    public String getUnansweredQuestion(){
        return getString(getColumnIndex(Schema.UnansweredQuestions.cols.QUESTION));
    }
}
