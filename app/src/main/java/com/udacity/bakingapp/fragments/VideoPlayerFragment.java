package com.udacity.bakingapp.fragments;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.udacity.bakingapp.R;
import com.udacity.bakingapp.models.Step;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class VideoPlayerFragment extends Fragment {

    public static final String STEP_URI = "step_uri";
    public static final String STEP_VIDEO_POSITION = "step_video_position";
    public static final String STEP_PLAY_WHEN_READY = "step_play_when_ready";
    public static final String STEP_PLAY_WINDOW_INDEX = "step_play_window_index";
    public static final String STEP_SINGLE = "step_single";

    @BindView(R.id.step_title_tv)
    TextView mStepTitle;

    @BindView(R.id.player_view_tablet)
    PlayerView mPlayerView;

    @BindView(R.id.step_description_tv)
    TextView mStepDescription;

    @BindView(R.id.placeholder_iv)
    ImageView mImageViewPlaceholder;

    Step mStep;
    Uri mVideoUri;
    String mVideoThumbnail;
    Bitmap mVideoThumbnailImage;
    boolean mShouldPlayWhenReady = true;
    long mPlayerPosition;
    int mWindowIndex;
    SimpleExoPlayer mSimpleExoPlayer;

    public VideoPlayerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_video_player, container, false);
        ButterKnife.bind(this, root);

        // Check if there is any state saved
        if (savedInstanceState != null) {
            mStep = savedInstanceState.getParcelable(STEP_SINGLE);
            mShouldPlayWhenReady = savedInstanceState.getBoolean(STEP_PLAY_WHEN_READY);
            mPlayerPosition = savedInstanceState.getLong(STEP_VIDEO_POSITION);
            mWindowIndex = savedInstanceState.getInt(STEP_PLAY_WINDOW_INDEX);
            mVideoUri = Uri.parse(savedInstanceState.getString(STEP_URI));
        }
        // If there is no saved state getArguments from CookingActivity
        else {
            if (getArguments() != null) {

                mImageViewPlaceholder.setVisibility(View.GONE);
                mPlayerView.setVisibility(View.VISIBLE);

                // Get arguments
                mStep = getArguments().getParcelable(STEP_SINGLE);

                // If has no video
                if (mStep.getVideoURL().equals("")) {
                    // Check thumbnail
                    if (mStep.getThumbnailURL().equals("")) {
                        // If no video or thumbnail, use placeholder image
                        mPlayerView.setUseArtwork(true);
                        mImageViewPlaceholder.setVisibility(View.VISIBLE);
                        mPlayerView.setUseController(false);
                    } else {
                        mImageViewPlaceholder.setVisibility(View.GONE);
                        mPlayerView.setVisibility(View.VISIBLE);
                        mVideoThumbnail = mStep.getThumbnailURL();
                        mVideoThumbnailImage = ThumbnailUtils.createVideoThumbnail(mVideoThumbnail, MediaStore.Video.Thumbnails.MICRO_KIND);
                        mPlayerView.setUseArtwork(true);
                        mPlayerView.setDefaultArtwork(mVideoThumbnailImage);
                    }
                } else {
                    mVideoUri = Uri.parse(mStep.getVideoURL());
                }
            }
        }
        return root;
    }

    public void initializeVideoPlayer(Uri videoUri) {

        mStepDescription.setText(mStep.getDescription());
        mStepTitle.setVisibility(View.VISIBLE);
        mStepTitle.setText(mStep.getShortDescription());

        if (mSimpleExoPlayer == null) {

            mSimpleExoPlayer = ExoPlayerFactory.newSimpleInstance(getActivity());

            // Bind the player to the view.
            mPlayerView.setPlayer(mSimpleExoPlayer);

            // Prepare the MediaSource.
            String userAgent = Util.getUserAgent(getActivity(), getString(R.string.app_name));
            MediaSource mediaSource = buildMediaSource(videoUri);

            if (mPlayerPosition != C.TIME_UNSET) {
                mSimpleExoPlayer.seekTo(mPlayerPosition);
            }
            // Prepare the player with the source.
            mSimpleExoPlayer.prepare(mediaSource, false, false);
            mSimpleExoPlayer.setPlayWhenReady(mShouldPlayWhenReady);
        }
    }

    // Release player
    private void releasePlayer() {
        if (mSimpleExoPlayer != null) {
            updateStartPosition();
            mSimpleExoPlayer.stop();
            mSimpleExoPlayer.release();
            mSimpleExoPlayer = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializeVideoPlayer(mVideoUri);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Util.SDK_INT <= 23 || mSimpleExoPlayer == null) {
            initializeVideoPlayer(mVideoUri);
        }
        if (mSimpleExoPlayer != null) {
            mSimpleExoPlayer.setPlayWhenReady(mShouldPlayWhenReady);
            mSimpleExoPlayer.seekTo(mPlayerPosition);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSimpleExoPlayer != null) {
            updateStartPosition();
            if (Util.SDK_INT <= 23) {
                releasePlayer();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mSimpleExoPlayer != null) {
            updateStartPosition();
            if (Util.SDK_INT > 23) {
                releasePlayer();
            }
        }
    }

    private void updateStartPosition() {
        if (mSimpleExoPlayer != null) {
            mShouldPlayWhenReady = mSimpleExoPlayer.getPlayWhenReady();
            mWindowIndex = mSimpleExoPlayer.getCurrentWindowIndex();
            mPlayerPosition = mSimpleExoPlayer.getCurrentPosition();
        }
    }

    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory =
                new DefaultDataSourceFactory(getActivity(), getString(R.string.app_name));
        return new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri);
    }
}
