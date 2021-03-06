package com.udacity.bakingapp.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.udacity.bakingapp.R;
import com.udacity.bakingapp.fragments.VideoPlayerFragment;
import com.udacity.bakingapp.models.Step;
import com.udacity.bakingapp.utils.ConstantsUtil;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StepActivity extends AppCompatActivity implements View.OnClickListener {

    VideoPlayerFragment videoPlayerFragment;
    FragmentManager fragmentManager;
    Bundle stepsBundle;
    int display_mode;
    ArrayList<Step> mStepArrayList = new ArrayList<>();

    @BindView(R.id.next_btn)
    Button mNextBtn;
    @BindView(R.id.previous_btn)
    Button mPreviousBtn;

    private int mVideoNumber = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        display_mode = getResources().getConfiguration().orientation;
        if (display_mode == Configuration.ORIENTATION_LANDSCAPE) {
            getSupportActionBar().hide(); //<< this
        }
        setContentView(R.layout.activity_step);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        if (intent == null) {
            closeOnError();
        }

        mVideoNumber = intent.getIntExtra(ConstantsUtil.COOKING_STEP_NUMBER_KEY, 0);
        mStepArrayList = intent.getParcelableArrayListExtra(ConstantsUtil.ALL_STEP_KEY);

        // If there is no saved state, instantiate fragment
        if (savedInstanceState == null) {
            playVideo(mStepArrayList.get(mVideoNumber));
        }

        mNextBtn.setOnClickListener(this);
        mPreviousBtn.setOnClickListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ConstantsUtil.COOKING_STEP_NUMBER_KEY_ONSAVEINSTANCE, mVideoNumber);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mVideoNumber = savedInstanceState.getInt(ConstantsUtil.COOKING_STEP_NUMBER_KEY_ONSAVEINSTANCE);
    }

    // Initialize fragment first time
    private void playVideo(Step step) {
        videoPlayerFragment = new VideoPlayerFragment();
        stepsBundle = new Bundle();
        stepsBundle.putParcelable(ConstantsUtil.SINGLE_STEP_KEY, step);
        videoPlayerFragment.setArguments(stepsBundle);

        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.player_container, videoPlayerFragment)
                .commit();
    }

    // Initialize fragment further
    public void playVideoReplace(Step step) {
        videoPlayerFragment = new VideoPlayerFragment();
        stepsBundle = new Bundle();
        stepsBundle.putParcelable(ConstantsUtil.SINGLE_STEP_KEY, step);
        videoPlayerFragment.setArguments(stepsBundle);

        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.player_container, videoPlayerFragment)
                .commit();
    }

    private void closeOnError() {
        finish();
        Toast.makeText(this,
                ConstantsUtil.ERROR_MESSAGE, Toast.LENGTH_SHORT).show();
    }

    // Handling the navigation buttons moving next and previous cooking steps
    @Override
    public void onClick(View v) {
        if (mVideoNumber < 0) {
            mVideoNumber = 0;
        }
        if (v.getId() == mPreviousBtn.getId()) {
            mVideoNumber--;
            if (mVideoNumber < 0) {
                Toast.makeText(this,
                        R.string.go_next_step, Toast.LENGTH_SHORT).show();
            } else
                playVideoReplace(mStepArrayList.get(mVideoNumber));
        } else if (v.getId() == mNextBtn.getId()) {
            if (mVideoNumber == mStepArrayList.size() - 1) {
                Toast.makeText(this, R.string.steps_over, Toast.LENGTH_SHORT).show();
            } else {
                mVideoNumber++;
                playVideoReplace(mStepArrayList.get(mVideoNumber));
            }
        }
    }
}
