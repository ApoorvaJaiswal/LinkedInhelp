package com.example.user.linkedinhelp;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.linkedinhelp.database.BaseHelper;
import com.example.user.linkedinhelp.database.Schema;
import com.example.user.linkedinhelp.database.Wrapper;


import java.io.File;
import java.util.ArrayList;

public class giveSolutionQA extends Activity{
    TextView tv1;
    RecyclerView recyclerView;
    ChatAdapter chatAdapter;
    EditText et1, et2;
    Button b1;
    ArrayList<String> replies = new ArrayList<>();
    private SQLiteDatabase database;
    Context context;
    Wrapper cursor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unanswered);
        tv1 = (TextView)findViewById(R.id.tv1);
        et1 = (EditText)findViewById(R.id.questionEditText);
        et2 = (EditText)findViewById(R.id.answerEditText);
        b1 = (Button)findViewById(R.id.submitButton);

        context= getApplicationContext();
        //File db= context.getDatabasePath(Schema.Chat.NAME);
        database = new BaseHelper(getApplicationContext()).getWritableDatabase();
        cursor = queryUnansweredQues(null, null);

        try{
            if(cursor.getCount()!=0) {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        replies.add(cursor.getUnansweredQuestion());
                        cursor.moveToNext();
                    }
                }
            }
        }finally {
            cursor.close();
        }


        recyclerView = (RecyclerView)findViewById(R.id.unansweredRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new giveSolutionQA.ChatAdapter();
        recyclerView.setAdapter(chatAdapter);


        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String q=et1.getText().toString();
                String a=et2.getText().toString();
                if(q!="" && a!=""){
                    ContentValues contentValues= getQAContentValues(q,a);
                    database.insert(Schema.QA.NAME,null,contentValues);
                    ContentValues cv= getUnansweredQuestionsContentValues(q);
                    database.delete(Schema.UnansweredQuestions.NAME, Schema.UnansweredQuestions.cols.QUESTION+" = ?", new String[]{q});
                    for(int i=0; i<replies.size(); i++){
                        if(replies.get(i).equalsIgnoreCase(q)){
                            replies.remove(i);
                            chatAdapter.notifyItemRemoved(i);
                            break;

                        }
                    }

                    Toast.makeText(giveSolutionQA.this,"Your answer would be helpful ",Toast.LENGTH_SHORT).show();
                    et1.setText("");
                    et2.setText("");
                }
            }
        });

    }

    private ContentValues getQAContentValues(String ques, String solution){
        ContentValues contentValues = new ContentValues();
        contentValues.put(Schema.QA.cols.QUESTION, ques);
        contentValues.put(Schema.QA.cols.ANSWER, solution);
        return contentValues;
    }

    private ContentValues getUnansweredQuestionsContentValues(String ques){
        ContentValues contentValues = new ContentValues();
        contentValues.put(Schema.UnansweredQuestions.cols.QUESTION, ques);
        return contentValues;
    }

    private Wrapper queryQA(String whereClause, String[] whereArgs){
        Cursor cursor = database.query(
                Schema.QA.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );

        return new Wrapper(cursor);
    }
    private Wrapper queryUnansweredQues(String whereClause, String[] whereArgs){
        Cursor cursor = database.query(
                Schema.UnansweredQuestions.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );

        return new Wrapper(cursor);
    }



    private class ChatHolder extends RecyclerView.ViewHolder{
        private TextView tv;

        public ChatHolder(View itemView){
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.tv1);
        }
        public void bindTextBot(String reply){
            tv.setText(reply);
            tv.setBackground(getDrawable(R.drawable.out));
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!(tv.getText().toString().equals(""))) {
                        et1.setText(tv.getText().toString());
                    }
                }
            });
        }
    }

    private class ChatAdapter extends RecyclerView.Adapter<giveSolutionQA.ChatHolder>{

        @Override
        public giveSolutionQA.ChatHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(giveSolutionQA.this);
            View view = inflater.inflate(R.layout.unanswered, parent, false);
            return new giveSolutionQA.ChatHolder(view);
        }

        @Override
        public void onBindViewHolder(giveSolutionQA.ChatHolder holder, int position) {
            String reply = replies.get(position);
            holder.bindTextBot(reply);

        }

        @Override
        public int getItemCount() {
            return replies.size();
        }
    }
}
