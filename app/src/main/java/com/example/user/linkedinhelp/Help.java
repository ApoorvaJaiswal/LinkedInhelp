package com.example.user.linkedinhelp;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;


import com.example.user.linkedinhelp.database.BaseHelper;
import com.example.user.linkedinhelp.database.Schema;
import com.example.user.linkedinhelp.database.Wrapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.zip.ZipInputStream;

public class Help extends AppCompatActivity implements Runnable{

    String userReply="",botReply="",b="",reply="";
    EditText ed;
    Button send;
    RecyclerView rv;
    SQLiteDatabase database;
    ChatAdapter chatAdapter;
    ArrayList<String> replies,udc;
    Intent checkIntent;
    Context context;
    boolean ready=false;
    Random random;
    InputStream is = null, ts = null, ps= null;
    SentenceModel sm = null;
    TokenizerModel tm = null;
    POSModel pos = null;
    AssetManager assetManager;
    TextToSpeech t;

    ImageView im;
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_screen);

        Thread t= new Thread();
        t.start();
        context= getApplicationContext();
        database=new BaseHelper(getApplicationContext()).getWritableDatabase();
            insert();
            //Toast.makeText(this, "Database created", Toast.LENGTH_SHORT).show();


        checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        //openNLP stuff
        assetManager = getApplicationContext().getAssets();


        try {
            is = assetManager.open("en-sent.bin");
            ts = assetManager.open("en-token.bin");
            ps = assetManager.open("en-pos-maxent.bin");


            //is = new FileInputStream("C:\\Users\\user\\AndroidStudioProjects\\LinkedInhelp\\en-sent.zip.bin");
            //ts = new FileInputStream("C:\\Users\\user\\AndroidStudioProjects\\LinkedInhelp\\en-token.zip.bin");
            //ps = new FileInputStream("C:\\Users\\user\\AndroidStudioProjects\\LinkedInhelp\\en-pos-maxent.zip.bin");

            sm = new SentenceModel(is);
            tm = new TokenizerModel(ts);
            pos = new POSModel(ps);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        random = new Random();

        send= (Button)findViewById(R.id.send);
        ed= (EditText)findViewById(R.id.reply);
       /* t= new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t.setLanguage(Locale.UK);
                    ready=true;
                }
            }
        });*/
        rv= (RecyclerView)findViewById(R.id.help_recycler_view);

        rv.setLayoutManager(new LinearLayoutManager(this));

        //im = (ImageView)findViewById(R.id.image);
        //im.setBackgroundResource(R.drawable.arrow);
        chatAdapter= new ChatAdapter();
        rv.setAdapter(chatAdapter);

        //UDCs
        udc = new ArrayList<>();
        udc.add("Sorry, I don't have the answer right now.\n I will try getting the answers from the experts.\n Thankyou");
        //udc.add("Can you please be specific?");
        //udc.add("I didn't understand it");



        //replies
        replies = new ArrayList<String>();
       load();
        rv.smoothScrollToPosition(chatAdapter.getItemCount());
      //  load();
       // rv.smoothScrollToPosition(chatAdapter.getItemCount());

        //send button onClickListener

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                reply = ed.getText().toString();
                //send the reply to openNLP and get the keywords. Then find out those keywords in the database and reply
                //If the keywords match and the answer is image, look into the assets folder , iterate through it and display all images in the folder.
                //If the keywords give a valid reply, display it on screen.
                //If not found, push the question to the database to find its answer.

                //save();

               // Toast.makeText(Help.this,"on click"+reply,Toast.LENGTH_SHORT).show();
               String reply1 = getReply(reply);
                userReply ="u"+reply;
                replies.add(userReply);
                chatAdapter.notifyItemInserted(replies.size() - 1);
                ed.setText("");

                    //Got the reply now,open the database and get the answer
                    Wrapper cursor = queryChat(Schema.QA.cols.QUESTION+" = ?", new String[]{reply1});
                    try{
                        if(cursor.getCount()==0){
                            botReply="";
                        }
                        else {
                            cursor.moveToFirst();
                            botReply = cursor.getQAAnswer();
                        }
                    }finally {
                        cursor.close();
                    }


                if(botReply.equals("")) {
                    b="";
                    b=udc.get(random.nextInt(udc.size()));
                    ContentValues cv= getUnanswredQuestionsContentValues(reply);
                    database.insert(Schema.UnansweredQuestions.NAME,null,cv);
                    //Intent checkIntent = new Intent();
                    //checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
                    startActivityForResult(checkIntent, 1);
                    botReply = "b" + b;
                    replies.add(botReply);
                    chatAdapter.notifyItemInserted(replies.size() - 1);

                }
                else {
                    if (botReply.charAt(0) == 'i') {
                        //fetch images
                        //botReply is the folder name.. retrieve the file path
                        b="Here are the images to help you out";
                      //  Intent checkIntent = new Intent();
                        //checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
                        startActivityForResult(checkIntent, 1);
                        AssetManager am =getAssets();
                        try {
                            String folder=botReply.substring(1, botReply.length());
                            String fileList[] = am.list(folder);

                            for(int i=0;i<fileList.length;i++)
                            {
                                //Toast.makeText(Help.this,fileList[i]+" ",Toast.LENGTH_SHORT).show();
                                String filename = "i"+folder+"/"+ fileList[i];
                                String tag="getReply";

                                Log.d(tag,filename);

                                replies.add(filename);
                                chatAdapter.notifyItemInserted(replies.size()-1);
                            }

                        }
                        catch(IOException e)
                        {
                            e.printStackTrace();
                        }


                    } else {
                        b="";
                        b= botReply.substring(1, botReply.length());
                        //Intent checkIntent = new Intent();
                        //checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
                        startActivityForResult(checkIntent, 1);
                        botReply ="b"+b;
                        replies.add(botReply);
                        chatAdapter.notifyItemInserted(replies.size() - 1);

                    }
                }

                save();
            }
        });
    }

    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success, create the TTS instance
                t = new TextToSpeech(Help.this, new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status != TextToSpeech.ERROR) {
                            t.setLanguage(Locale.UK);
                            t.speak(b, TextToSpeech.QUEUE_FLUSH, null);

                        }
                    }
                });
            } else {
                // missing data, install it
                Intent installIntent = new Intent();
                installIntent.setAction(
                        TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }
    }

    String getImageReply(String r)
    {
        String reply="";

        return reply;
    }
    String getReply(String a){
        String reply="";
        String out="";
        //send the string "a" to openNLP for POS tagger

        //Toast.makeText(this,"inside function",Toast.LENGTH_SHORT).show();
       try{
            SentenceDetectorME sme = new SentenceDetectorME(sm);
            String sentence [] = sme.sentDetect(a);

            is.close();
            TokenizerME tk = new TokenizerME(tm);
            for (int i=0;i<sentence.length; i++) {
                String tokenizer[]= tk.tokenize(sentence[i]);
                POSTaggerME pe = new POSTaggerME(pos);
                String tags[] = pe.tag(tokenizer);
                //alphabetical order
                int n=tokenizer.length;
                for(int k=0;k<n;k++)
                {
                    for(int j=0;j<n-k-1;j++)
                    {
                        if(tokenizer[j].compareTo(tokenizer[j+1]) > 0)
                        {
                            String t = tokenizer[j];
                            tokenizer[j]=tokenizer[j+1];
                            tokenizer[j+1]=t;

                            t=tags[j];
                            tags[j]=tags[j+1];
                            tags[j+1]=t;
                        }
                    }
                }

                for(int l=0;l<n;l++)
                    out+=tokenizer[l]+" "+tags[l]+"\n";

                for(int j=0; j<tags.length; j++)
                {
                    //Toast.makeText(this,"The sentence: "+i+"\n and tokenizer: "+ tokenizer[i]+"\n tags: "+tags[j],Toast.LENGTH_LONG).show();
                    if(tags[j].equalsIgnoreCase("NNP")|| tags[j].equalsIgnoreCase("NN")|| tags[j].equalsIgnoreCase("VB")|| tags[j].equalsIgnoreCase("WP") || tags[j].equalsIgnoreCase("WRB"))
                    reply=reply+tokenizer[j]+"_";
                }
                reply = reply.substring(0,reply.length()-1);
            }
            ts.close();
            ps.close();

        }
        catch(Exception i)
        {
            i.printStackTrace();
        }


        String tag="getReply";

        Log.d(tag,out);
        Toast.makeText(this,reply,Toast.LENGTH_LONG).show();
        return reply;
    }



    void insert(){
        //insert the keywords with answers(Checkout FAQs)
        ContentValues contentValues= getChatContentValues("block_how_member","iblock_member");
        database.insert(Schema.QA.NAME,null,contentValues);

        contentValues= getChatContentValues("account_create_how","iaccount_create_how");
        database.insert(Schema.QA.NAME,null,contentValues);

        contentValues= getChatContentValues("company_create_how__page","icompanypage_make");
        database.insert(Schema.QA.NAME,null,contentValues);

        contentValues= getChatContentValues("account_close_how","iaccount_close_how");
        database.insert(Schema.QA.NAME,null,contentValues);

        contentValues= getChatContentValues("linkedin_what","ilinkedin_what");
        database.insert(Schema.QA.NAME,null,contentValues);

        contentValues= getChatContentValues("help_how_linkedin","aWe can help you:\n" +
                "1 Establish your professional profile and control one of the top search results for your name.\n" +
                "2 Build and maintain your professional network.\n" +
                "3 Find and reconnect with colleagues and classmates.\n" +
                "4 Learn about other companies, and get industry insights.\n" +
                "5 Find other professionals in the same industry using groups.\n" +
                "6 Share your thoughts and insights through LinkedIn's long-form publishing platform.\n");
        database.insert(Schema.QA.NAME,null,contentValues);

       contentValues= getChatContentValues("join_linkedin_why","a1 Personal branding.\n" +
                "2   Storing your resume online.\n" +
                "3  Managing and growing your professional network.\n" +
                "4 Building an expert status.\n" +
                "5 Spying on others.\n" +
                "6 Getting yourself headhunted.\n");
        database.insert(Schema.QA.NAME,null,contentValues);

        contentValues= getChatContentValues("carrer_oppurtunities","aapplying for job,recruiting\n" +
                "\n" +
                "\nOur culture is our competitive advantage" +
                "");
        database.insert(Schema.QA.NAME,null,contentValues);

        contentValues= getChatContentValues("content_how_share","icontents_share");
        database.insert(Schema.QA.NAME,null,contentValues);

        contentValues= getChatContentValues("college_help_how_students","a1 Recommends jobs based on your education and interests. You’ll be able to get email alerts and notifications on your LinkedIn home page.\n" +
                "\n" +
                "\n" +
                "2 Helps LinkedIn’s network help you. The company suggests looking first at alumni , friends, and family members. \n" +
                "3 Helps you research companies via LinkedIn’s company pages. Find out what they do, types of people they hire, and what people say about them.\n" +
                "\n" +
                "\n" +
                "4 Allows companies to find you. Just as more experienced hires may be recruited to apply \n" +
                "for jobs, LinkedIn helps you job search even when you’re not actively looking for opportunities by making you part of database professionals in a variety of types of organizations are looking for you! \n" +
                "\n" +
                "\n" +
                "5 Helps you connect to other students to see how they landed their jobs.\n" +
                "\n" +
                "\n" +
                "6 Opens a worldwide network. The portal is available in all of the languages LinkedIn supports. It’s possible to find jobs from around the world.\n");
        database.insert(Schema.QA.NAME,null,contentValues);

        contentValues= getChatContentValues("feed_how_improve","ifeed_improve");
        database.insert(Schema.QA.NAME,null,contentValues);

        contentValues= getChatContentValues("change_how_password","ichange_how_password");
        database.insert(Schema.QA.NAME,null,contentValues);

        contentValues= getChatContentValues("how_someone_unfollow","ihow_someone_unfollow");
        database.insert(Schema.QA.NAME,null,contentValues);

        contentValues= getChatContentValues("course_python_what","ahttps://www.codecademy.com/learn/python" );
        database.insert(Schema.QA.NAME,null,contentValues);

        contentValues= getChatContentValues("course_java_what","ahttps://www.coursera.org/courses/?query=JAVA");
        database.insert(Schema.QA.NAME,null,contentValues);
        //repeat this line to insert in db
    }

    private ContentValues getChatContentValues(String ques, String ans){
        ContentValues contentValues = new ContentValues();
        contentValues.put(Schema.QA.cols.QUESTION, ques);
        contentValues.put(Schema.QA.cols.ANSWER, ans);
        return contentValues;
    }

    private ContentValues getUnanswredQuestionsContentValues(String ques){
        ContentValues contentValues = new ContentValues();
        contentValues.put(Schema.UnansweredQuestions.cols.QUESTION, ques);
        return contentValues;
    }

    private Wrapper queryChat(String whereClause, String[] whereArgs){
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
    public void load() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        replies.clear();
        int size = sharedPreferences.getInt("Status_size", 0);

        for(int i=0;i<size;i++)
        {
            replies.add(sharedPreferences.getString("Status_" + i, null));
        }
    }

    public void save() {
        rv.smoothScrollToPosition(chatAdapter.getItemCount());
        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("Status_size", replies.size());
        for(int i=0;i<replies.size();i++)
        {
            editor.remove("Status_" + i);
            editor.putString("Status_" + i, replies.get(i));
        }
        editor.commit();
    }

    @Override
    public void run() {

    }


    private class ChatHolder extends RecyclerView.ViewHolder{
        private TextView userReplyTextView;
        private TextView botReplyTextView;

        private ImageView im;
        TextToSpeech t1;

        public ChatHolder(View itemView){
            super(itemView);
            userReplyTextView = (TextView) itemView.findViewById(R.id.user_reply_text_view);
            botReplyTextView = (TextView) itemView.findViewById(R.id.bot_reply_text_view);
           im= (ImageView)itemView.findViewById(R.id.image);

        }
        public void bindTextBot(String reply){
          //  botReplyTextView.setAutoLinkMask(0);

            botReplyTextView.setText(reply);
            if(reply.length()>4 && (reply.substring(0,4).equals("http"))){
               // botReplyTextView.setMovementMethod(LinkMovementMethod.getInstance());
                Linkify.addLinks(botReplyTextView,Linkify.ALL);
            }

            botReplyTextView.setBackground(getDrawable(R.drawable.out_message_bg));
        }
        public void bindTextUser(String reply){
            userReplyTextView.setText(reply);
            //t1.speak(reply, TextToSpeech.QUEUE_FLUSH, null);

            userReplyTextView.setBackground(getDrawable(R.drawable.in_message_bg));
        }
        public void bindImageBot(String path, int i){
            //asset to bitmap image
            //path has path of each image to be displayed
            //im.setBackground(getDrawable(R.drawable.arrow));
            AssetManager assetManager = getApplicationContext().getAssets();
            InputStream istr = null;
            try {
                istr = assetManager.open(path);
                String s="error";
                Log.d(s,"rrr");
                Drawable d= Drawable.createFromStream(istr,null);
                // Bitmap bitmap = BitmapFactory.decodeStream(istr);
                //  SpannableStringBuilder sb= new SpannableStringBuilder();
                //sb.append(Integer.toString(i)+"\n");
                //sb.setSpan(new ImageSpan(getApplicationContext(),bitmap),sb.length()-1,sb.length(), 0);
                botReplyTextView.setText(i+"");
                im.setImageDrawable(d);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private class ChatAdapter extends RecyclerView.Adapter<ChatHolder>{

        @Override
        public ChatHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(Help.this);
            View view = inflater.inflate(R.layout.chat, parent, false);
            return new ChatHolder(view);
        }
        @Override
        public int getItemViewType(int position) {

            return position;
        }


        @Override
        public void onBindViewHolder(ChatHolder holder, int position) {
            String reply = replies.get(position);
            char turn = reply.charAt(0);
            reply = reply.substring(1,reply.length());//stores path of images for assets to access
            if(turn == 'b'){
                holder.bindTextBot(reply);
            }
            else if(turn == 'u'){
                holder.bindTextUser(reply);
            }
            else {
                holder.bindImageBot(reply,Integer.parseInt(reply.charAt(reply.length()-5)+""));//make sure to set last digit as the number of image as 1.png
            }
        }

        @Override
        public int getItemCount() {
            return replies.size();
        }
    }
}
