package code.name.monkey.retromusic.ui.activities;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.transition.TransitionManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.jaudiotagger.tag.FieldKey;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import code.name.monkey.appthemehelper.ThemeStore;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.helper.MusicProgressViewUpdateHelper;
import code.name.monkey.retromusic.model.Song;
import code.name.monkey.retromusic.model.lyrics.Lyrics;
import code.name.monkey.retromusic.ui.activities.base.AbsMusicServiceActivity;
import code.name.monkey.retromusic.ui.activities.tageditor.WriteTagsAsyncTask;
import code.name.monkey.retromusic.util.LyricUtil;
import code.name.monkey.retromusic.util.MusicUtil;
import code.name.monkey.retromusic.util.Util;
import code.name.monkey.retromusic.views.LyricView;
import io.reactivex.disposables.CompositeDisposable;

public class LyricsActivity extends AbsMusicServiceActivity implements
        MusicProgressViewUpdateHelper.Callback {

    @BindView(R.id.title)
    TextView songTitle;
    @BindView(R.id.text)
    TextView songText;
    @BindView(R.id.lyrics)
    LyricView lyricView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.offline_lyrics)
    TextView offlineLyrics;
    @BindView(R.id.actions)
    RadioGroup actionsLayout;
    @BindView(R.id.actions_container)
    ViewGroup actionsLayoutContainer;
    @BindView(R.id.edit)
    View edit;
    @BindView(R.id.gradient_background)
    View background;

    private MusicProgressViewUpdateHelper updateHelper;
    private AsyncTask updateLyricsAsyncTask;
    private CompositeDisposable disposable;
    private Song song;
    private Lyrics lyrics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyrics);
        ButterKnife.bind(this);

        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();
        setLightNavigationBar(true);

        updateHelper = new MusicProgressViewUpdateHelper(this, 500, 1000);

        setupToolbar();
        setupLyricsView();
        setupWakelock();

        actionsLayout.setOnCheckedChangeListener((group, checkedId) -> selectLyricsTye(checkedId));
    }

    private void selectLyricsTye(int group) {
        switch (group) {
            case R.id.synced_lyrics:
                loadLRCLyrics();
                offlineLyrics.setVisibility(View.GONE);
                break;
            default:
            case R.id.normal_lyrics:
                loadSongLyrics();
                lyricView.setVisibility(View.GONE);
                offlineLyrics.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void loadLRCLyrics() {
        if (LyricUtil.isLrcFileExist(song.title, song.artistName)) {
            showLyricsLocal(LyricUtil.getLocalLyricFile(song.title, song.artistName));
        }
    }

    private void setupWakelock() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void setupLyricsView() {
        disposable = new CompositeDisposable();
        //lyricView.setLineSpace(15.0f);
        //lyricView.setTextSize(17.0f);
        //lyricView.setPlayable(true);
        //lyricView.setTranslationY(DensityUtil.getScreenWidth(this) + DensityUtil.dip2px(this, 120));
        lyricView.setOnPlayerClickListener((progress, content) -> MusicPlayerRemote.seekTo((int) progress));

        //lyricView.setHighLightTextColor(ThemeStore.accentColor(this));
        lyricView.setDefaultColor(ContextCompat.getColor(this, R.color.md_grey_400));
        //lyricView.setTouchable(false);
        lyricView.setHintColor(Color.WHITE);


    }

    private void setupToolbar() {
        toolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        toolbar.setTitle("");
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        setSupportActionBar(toolbar);
    }

    @Override
    public void onPlayingMetaChanged() {
        super.onPlayingMetaChanged();
        loadLrcFile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateHelper.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateHelper.stop();
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        loadLrcFile();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposable.clear();
        lyricView.setOnPlayerClickListener(null);

        if (updateLyricsAsyncTask != null && !updateLyricsAsyncTask.isCancelled()) {
            updateLyricsAsyncTask.cancel(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onUpdateProgressViews(int progress, int total) {
        lyricView.setCurrentTimeMillis(progress);
    }

    private void loadLrcFile() {
        song = MusicPlayerRemote.getCurrentSong();
        songTitle.setText(song.title);
        songText.setText(song.artistName);

        selectLyricsTye(actionsLayout.getCheckedRadioButtonId());
    }

    private void showLyricsLocal(File file) {
        if (file == null) {
            lyricView.reset();
            lyricView.setVisibility(View.GONE);
        } else {
            lyricView.setVisibility(View.VISIBLE);
            lyricView.setLyricFile(file, "UTF-8");
        }
    }

    @OnClick({R.id.edit, R.id.edit_lyrics})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.edit_lyrics:
                switch (actionsLayout.getCheckedRadioButtonId()) {
                    case R.id.synced_lyrics:
                        showSyncedLyrics();
                        break;
                    case R.id.normal_lyrics:
                        showLyricsSaveDialog();
                        break;
                }
                break;
            case R.id.edit:
                TransitionManager.beginDelayedTransition(findViewById(R.id.root));
                actionsLayoutContainer.setVisibility(actionsLayoutContainer.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                break;

        }
    }

    @SuppressLint("StaticFieldLeak")
    private void loadSongLyrics() {
        if (updateLyricsAsyncTask != null) updateLyricsAsyncTask.cancel(false);
        final Song song = MusicPlayerRemote.getCurrentSong();
        updateLyricsAsyncTask = new AsyncTask<Void, Void, Lyrics>() {
            @Override
            protected Lyrics doInBackground(Void... params) {
                String data = MusicUtil.getLyrics(song);
                if (TextUtils.isEmpty(data)) {
                    return null;
                }
                return Lyrics.parse(song, data);
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                lyrics = null;
            }

            @Override
            protected void onPostExecute(Lyrics l) {
                lyrics = l;
                offlineLyrics.setVisibility(View.VISIBLE);
                if (l == null) {
                    edit.setVisibility(View.VISIBLE);
                    offlineLyrics.setText(R.string.no_lyrics_found);
                    return;
                }
                offlineLyrics.setText(l.data);
            }

            @Override
            protected void onCancelled(Lyrics s) {
                onPostExecute(null);
            }
        }.execute();
    }

    private void showSyncedLyrics() {
        String content = "";
        try {
            content = LyricUtil.getStringFromFile(song.title, song.artistName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        new MaterialDialog.Builder(this)
                .title("Add lyrics")
                .neutralText("Search")
                .content("Add time frame lyrics")
                .negativeText("Delete")
                .onNegative((dialog, which) -> {
                    LyricUtil.deleteLrcFile(song.title, song.artistName);
                    loadLrcFile();
                })
                .onNeutral((dialog, which) -> Util.openUrl(LyricsActivity.this, getGoogleSearchLrcUrl()))
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                .input("Paste lyrics here", content, (dialog, input) -> {
                    LyricUtil.writeLrcToLoc(song.title, song.artistName, input.toString());
                    loadLrcFile();
                }).show();
    }

    private String getGoogleSearchLrcUrl() {
        String baseUrl = "http://www.google.com/search?";
        String query = song.title + "+" + song.artistName;
        query = "q=" + query.replace(" ", "+") + " .lrc";
        baseUrl += query;
        return baseUrl;
    }

    private void showLyricsSaveDialog() {
        String content = "";
        if (lyrics == null) {
            content = "";
        } else {
            content = lyrics.data;
        }
        new MaterialDialog.Builder(this)
                .title("Add lyrics")
                .neutralText("Search")
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Util.openUrl(LyricsActivity.this, getGoogleSearchUrl(song.title, song.artistName));
                    }
                })
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                .input("Paste lyrics here", content, (dialog, input) -> {
                    Map<FieldKey, String> fieldKeyValueMap = new EnumMap<>(FieldKey.class);
                    fieldKeyValueMap.put(FieldKey.LYRICS, input.toString());

                    new WriteTagsAsyncTask(LyricsActivity.this)
                            .execute(new WriteTagsAsyncTask.LoadingInfo(getSongPaths(song), fieldKeyValueMap, null));
                    loadLrcFile();
                })
                .show();
    }

    private ArrayList<String> getSongPaths(Song song) {
        ArrayList<String> paths = new ArrayList<>(1);
        paths.add(song.data);
        return paths;
    }

    private String getGoogleSearchUrl(String title, String text) {
        String baseUrl = "http://www.google.com/search?";
        String query = title + "+" + text;
        query = "q=" + query.replace(" ", "+") + " lyrics";
        baseUrl += query;
        return baseUrl;
    }

    /*
    private void loadLyricsWIki(String title, String artist) {
        offlineLyrics.setVisibility(View.GONE);
        if (lyricsWikiTask != null) {
            lyricsWikiTask.cancel(false);
        }
        lyricsWikiTask = new ParseLyrics(new ParseLyrics.LyricsCallback() {
            @Override
            public void onShowLyrics(String lyrics) {
                offlineLyrics.setVisibility(View.VISIBLE);
                offlineLyrics.setText(lyrics);
            }

            @Override
            public void onError() {
                loadSongLyrics();
            }
        }).execute(title, artist);
    }

    private void callAgain(final String title, final String artist) {
        disposable.clear();
        disposable.add(loadLyrics.downloadLrcFile(title, artist, MusicPlayerRemote.getSongDurationMillis())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(disposable -> {
                    refresh.startAnimation(rotateAnimation);
                })
                .subscribe(this::showLyricsLocal, throwable -> {
                    refresh.clearAnimation();
                    showLyricsLocal(null);
                    //loadLyricsWIki(songTitle, artist);
                    toggleSyncLyrics(View.GONE);
                }, () -> {
                    refresh.clearAnimation();
                    Toast.makeText(this, "Lyrics downloaded", Toast.LENGTH_SHORT).show();
                }));
    }
*/
}
