package com.bignerdranch.android.geoquiz;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class QuizActivity extends AppCompatActivity {

    private static final String TAG = "QuizActivity";

    //indexes for rotation problem
    private static final String KEY_INDEX = "index";
    private static final String CHEAT_INDEX = "cheat_index";
    //eventually figure out this
    //private static final String ANSWERBANK_INDEX = "answerbank_index";

    private static final int REQUEST_CODE_CHEAT = 0;

    private Button mTrueButton;
    private Button mFalseButton;
    //Button replaced by ImageButton
    private ImageButton mNextButton;
    private ImageButton mPrevButton;
    private Button mCheatButton;
    private TextView mQuestionTextView;

    //question array
    private Question[] mQuestionBank = new Question[]{
            new Question(R.string.question_australia, true),
            new Question(R.string.question_oceans, true),
            new Question(R.string.question_mideast, false),
            new Question(R.string.question_africa, false),
            new Question(R.string.question_americas, true),
            new Question(R.string.question_asia, true),
    };

    private Boolean[] mAnswerBank = new Boolean[6];
    private Boolean[] mCheaterBank = {false, false, false, false, false, false};

    private int mCurrentIndex = 0;
    private int questions_right = 0;
    private int cheatTokens = 3;
    private boolean mIsCheater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate(Bundle) called");
        setContentView(R.layout.activity_quiz);

        if(savedInstanceState != null){
            mCurrentIndex = savedInstanceState.getInt(KEY_INDEX, 0);
            mIsCheater = savedInstanceState.getBoolean(CHEAT_INDEX);
            if(mIsCheater == true && cheatTokens != 0) {
                cheatTokens--;
            }
            //in the future, add all arrays.
        }

        mQuestionTextView = (TextView) findViewById(R.id.question_text_view);

        //allows user to click on the question to go to the next question
        mQuestionTextView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
                updateQuestion();
            }
        });


        mTrueButton = (Button) findViewById(R.id.true_button);
        mTrueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(true);
            }
        });
        mFalseButton = (Button) findViewById(R.id.false_button);
        mFalseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(false);
            }
        });


        //Button cast replaced by ImageView
        mNextButton = (ImageButton) findViewById(R.id.next_button);
        mNextButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
                updateQuestion();
            }
        });

        mPrevButton = (ImageButton) findViewById(R.id.prev_button);
        mPrevButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(mCurrentIndex == 0){
                    mCurrentIndex = (mQuestionBank.length - 1);
                }else{
                    mCurrentIndex = (mCurrentIndex - 1) % mQuestionBank.length;
                }
                updateQuestion();
            }
        });

        mCheatButton = (Button) findViewById(R.id.cheat_button);
        mCheatButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //start CheatActivity
                boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();
                Intent intent = CheatActivity.newIntent(QuizActivity.this, answerIsTrue);
                startActivityForResult(intent, REQUEST_CODE_CHEAT);
            }
        });

        updateQuestion();
    }

    //retrieves variable that CheatActivity passes back
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode != Activity.RESULT_OK){
            return;
        }

        if(requestCode == REQUEST_CODE_CHEAT){
            if(data == null){
                return;
            }
            mIsCheater = CheatActivity.wasAnswerShown(data);
            mCheaterBank[mCurrentIndex] = mIsCheater;
        }
    }

    private void checkIfAnswered(){
        if(mAnswerBank[mCurrentIndex] == null){
            mTrueButton.setEnabled(true);
            mFalseButton.setEnabled(true);
            if(cheatTokens != 0) {
                mCheatButton.setEnabled(true);
            }
        }else{
            mTrueButton.setEnabled(false);
            mFalseButton.setEnabled(false);
            mCheatButton.setEnabled(false);
            Toast.makeText(QuizActivity.this,R.string.already_answered,Toast.LENGTH_SHORT).show();
        }
    }

    private void updateQuestion(){
        mIsCheater = mCheaterBank[mCurrentIndex];
        checkIfAnswered();
        int question = mQuestionBank[mCurrentIndex].getTextResId();
        mQuestionTextView.setText(question);
    }

    private void checkAnswer(boolean userPressedTrue){
        boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();

        int messageResId = 0;


        //sets answer
        mAnswerBank[mCurrentIndex] = userPressedTrue;

        //sets string to be displayed when button is pressed
        if(mIsCheater){
            messageResId = R.string.judgement_toast;
            //the user doesn't get points if they cheat.
        }else {
            if (userPressedTrue == answerIsTrue) {
                messageResId = R.string.correct_toast;
                //sets answer
                questions_right = questions_right + 1;
            } else {
                messageResId = R.string.incorrect_toast;
            }
        }

        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show();
        //stops from answering or cheating twice
        mTrueButton.setEnabled(false);
        mFalseButton.setEnabled(false);
        mCheatButton.setEnabled(false);
        //checks if all questions are answered
        isQuizDone();
    }

    private void isQuizDone(){
        boolean b = false;

        for(int i=0; i < mQuestionBank.length; i++){
            if(mAnswerBank[i] == null){
                b = true;
            }
        }

        if(b == false){
            quizIsDone();
        }
    }

    //if all answers are answered, this is initialized.
    //percentage is given.
    private void quizIsDone(){
        Toast.makeText(QuizActivity.this, R.string.quiz_complete,Toast.LENGTH_SHORT).show();
        mTrueButton.setEnabled(false);
        mFalseButton.setEnabled(false);
        mPrevButton.setEnabled(false);
        mNextButton.setEnabled(false);
        int grade = Math.round((questions_right * 100.0f)/ mQuestionBank.length);
        mQuestionTextView.setText("Your percentage for the quiz is: " + grade + "%");
    }

    //override methods for log
    @Override
    public void onStart(){
        super.onStart();
        Log.d(TAG, "onStart() called");
    }
    @Override
    public void onResume(){
        super.onResume();
        Log.d(TAG, "onResume() called");
    }
    @Override
    public void onPause(){
        super.onPause();
        Log.d(TAG, "onPause() called");
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        Log.i(TAG, "onSaveInstanceState");
        //puts index and cheater values into savedInstanceState
        savedInstanceState.putInt(KEY_INDEX, mCurrentIndex);
        savedInstanceState.putBoolean(CHEAT_INDEX, mIsCheater);
    }
    @Override
    public void onStop(){
        super.onStop();
        Log.d(TAG, "onStop() called");
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
    }
    //end override methods
}
