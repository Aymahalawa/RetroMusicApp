package code.name.monkey.retromusic.ui.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.view.WindowManager;

import com.bumptech.glide.Glide;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrListener;
import com.r0adkll.slidr.model.SlidrPosition;

import butterknife.ButterKnife;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.glide.RetroMusicColoredTarget;
import code.name.monkey.retromusic.glide.SongGlideRequest;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.model.Song;
import code.name.monkey.retromusic.ui.activities.base.AbsMusicServiceActivity;
import code.name.monkey.retromusic.ui.fragments.player.lockscreen.LockScreenPlayerControlsFragment;

public class LockScreenActivity extends AbsMusicServiceActivity {

    private LockScreenPlayerControlsFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        getWindow().getDecorView().setBackgroundColor(Color.TRANSPARENT);

        setDrawUnderStatusbar(true);
        setContentView(R.layout.activity_lock_screen);
        hideStatusBar();

        SlidrConfig config = new SlidrConfig.Builder()

                .position(SlidrPosition.BOTTOM)
                .sensitivity(1f)
                .scrimColor(Color.BLACK)
                .scrimStartAlpha(0.8f)
                .scrimEndAlpha(0f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .edge(true)
                .edgeSize(0.25f) 
                .build();

        Slidr.attach(this, config);

        setStatusbarColor(Color.TRANSPARENT);
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();
        setLightNavigationBar(true);

        ButterKnife.bind(this);
        mFragment = (LockScreenPlayerControlsFragment) getSupportFragmentManager().findFragmentById(R.id.playback_controls_fragment);

        findViewById(R.id.slide).setTranslationY(100f);
        findViewById(R.id.slide).setAlpha(0f);
        ViewCompat.animate(findViewById(R.id.slide))
                .translationY(0f)
                .alpha(1f)
                .setDuration(1500)
                .start();

    }

    @Override
    public void onPlayingMetaChanged() {
        super.onPlayingMetaChanged();
        updateSongs();
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        updateSongs();
    }

    private void updateSongs() {
        Song song = MusicPlayerRemote.getCurrentSong();
        SongGlideRequest.Builder.from(Glide.with(this), song)
                .checkIgnoreMediaStore(this)
                .generatePalette(this)
                .build().into(new RetroMusicColoredTarget(findViewById(R.id.image)) {
            @Override
            public void onColorReady(int color) {
                mFragment.setDark(color);
            }
        });
    }
}
