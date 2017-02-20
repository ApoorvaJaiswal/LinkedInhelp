package com.example.user.linkedinhelp.database;


public class Schema {


    public static final class QA{
        public static final String NAME="QA";

        public static final class cols{
            public static final String QUESTION="question";
            public static final String ANSWER="answer";
        }
    }
    public static final class UnansweredQuestions{
        public static final String NAME="UnansweredQuestions";

        public static final class cols{
            public static final String QUESTION="question";
        }
    }


}
