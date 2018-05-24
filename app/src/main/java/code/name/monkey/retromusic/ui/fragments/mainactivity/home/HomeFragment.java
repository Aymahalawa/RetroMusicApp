package code.name.monkey.retromusic.ui.fragments.mainactivity.home;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import code.name.monkey.appthemehelper.ThemeStore;
import code.name.monkey.appthemehelper.util.ATHUtil;
import code.name.monkey.appthemehelper.util.TintHelper;
import code.name.monkey.appthemehelper.util.ToolbarContentTintHelper;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.dialogs.SleepTimerDialog;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.interfaces.MainActivityFragmentCallbacks;
import code.name.monkey.retromusic.loaders.SongLoader;
import code.name.monkey.retromusic.misc.AppBarStateChangeListener;
import code.name.monkey.retromusic.model.Album;
import code.name.monkey.retromusic.model.Artist;
import code.name.monkey.retromusic.model.Genre;
import code.name.monkey.retromusic.model.Playlist;
import code.name.monkey.retromusic.model.smartplaylist.HistoryPlaylist;
import code.name.monkey.retromusic.model.smartplaylist.LastAddedPlaylist;
import code.name.monkey.retromusic.model.smartplaylist.MyTopTracksPlaylist;
import code.name.monkey.retromusic.mvp.contract.HomeContract;
import code.name.monkey.retromusic.mvp.presenter.HomePresenter;
import code.name.monkey.retromusic.ui.adapter.GenreAdapter;
import code.name.monkey.retromusic.ui.adapter.album.AlbumFullWithAdapter;
import code.name.monkey.retromusic.ui.adapter.artist.ArtistAdapter;
import code.name.monkey.retromusic.ui.fragments.base.AbsMainActivityFragment;
import code.name.monkey.retromusic.ui.fragments.mainactivity.folders.FoldersFragment;
import code.name.monkey.retromusic.util.Compressor;
import code.name.monkey.retromusic.util.NavigationUtil;
import code.name.monkey.retromusic.util.PreferenceUtil;
import code.name.monkey.retromusic.views.CircularImageView;
import code.name.monkey.retromusic.views.MetalRecyclerViewPager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static code.name.monkey.retromusic.Constants.USER_BANNER;
import static code.name.monkey.retromusic.Constants.USER_PROFILE;

public class HomeFragment extends AbsMainActivityFragment implements MainActivityFragmentCallbacks,
        HomeContract.HomeView {

    private static final String TAG = "HomeFragment";

    Unbinder unbinder;
    @BindView(R.id.home_toolbar)
    Toolbar toolbar;
    @BindView(R.id.appbar)
    AppBarLayout appbar;
    @BindView(R.id.image)
    ImageView imageView;
    @BindView(R.id.user_image)
    CircularImageView userImage;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout toolbarLayout;
    @BindView(R.id.recycler_view)
    RecyclerView recentArtistRV;
    @BindView(R.id.recent_album)
    RecyclerView recentAlbumRV;
    @BindView(R.id.top_artist)
    RecyclerView topArtistRV;
    @BindView(R.id.top_album)
    MetalRecyclerViewPager topAlbumRV;
    @BindView(R.id.recent_artist_container)
    View recentArtistContainer;
    @BindView(R.id.recent_albums_container)
    View recentAlbumsContainer;
    @BindView(R.id.top_artist_container)
    View topArtistContainer;
    @BindView(R.id.top_albums_container)
    View topAlbumContainer;
    @BindView(R.id.title_welcome)
    AppCompatTextView titleWelcome;
    @BindView(R.id.gradient_background)
    View background;
    @BindView(R.id.genres)
    RecyclerView genresRecyclerView;
    @BindView(R.id.genre_container)
    LinearLayout genreContainer;
    @BindView(R.id.container)
    View container;

    private HomePresenter homePresenter;
    private CompositeDisposable disposable;

    public static HomeFragment newInstance() {
        Bundle args = new Bundle();
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private String getTimeOfTheDay() {
        String message = getString(R.string.title_good_day);
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        String[] images = new String[]{};
        if (timeOfDay >= 0 && timeOfDay < 6) {
            message = getString(R.string.title_good_night);
            images = getResources().getStringArray(R.array.night);
        } else if (timeOfDay >= 6 && timeOfDay < 12) {
            message = getString(R.string.title_good_morning);
            images = getResources().getStringArray(R.array.morning);
        } else if (timeOfDay >= 12 && timeOfDay < 16) {
            message = getString(R.string.title_good_afternoon);
            images = getResources().getStringArray(R.array.after_noon);
        } else if (timeOfDay >= 16 && timeOfDay < 20) {
            message = getString(R.string.title_good_evening);
            images = getResources().getStringArray(R.array.evening);
        } else if (timeOfDay >= 20 && timeOfDay < 24) {
            message = getString(R.string.title_good_night);
            images = getResources().getStringArray(R.array.night);
        }
        String day = images[new Random().nextInt(images.length)];
        loadTimeImage(day);
        return message;
    }

    private void loadTimeImage(String day) {
        //noinspection ConstantConditions
        if (PreferenceUtil.getInstance(getActivity()).getBannerImage().isEmpty()) {
            if (imageView != null) {
                Glide.with(getActivity()).load(day)
                        .asBitmap()
                        .placeholder(R.drawable.material_design_default)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(imageView);
            }
        } else {
            loadBannerFromStorage();
        }
    }

    private void loadBannerFromStorage() {
        //noinspection ConstantConditions
        disposable.add(new Compressor(getContext())
                .setQuality(100)
                .setCompressFormat(Bitmap.CompressFormat.WEBP)
                .compressToBitmapAsFlowable(
                        new File(PreferenceUtil.getInstance(getContext()).getBannerImage(), USER_BANNER))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(imageView::setImageBitmap));
    }

    private void loadImageFromStorage() {
        //noinspection ConstantConditions
        disposable.add(new Compressor(getContext())
                .setMaxHeight(300)
                .setMaxWidth(300)
                .setQuality(75)
                .setCompressFormat(Bitmap.CompressFormat.WEBP)
                .compressToBitmapAsFlowable(
                        new File(PreferenceUtil.getInstance(getContext()).getProfileImage(), USER_PROFILE))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmap -> userImage.setImageBitmap(bitmap),
                        throwable -> userImage.setImageDrawable(ContextCompat
                                .getDrawable(getContext(), R.drawable.ic_person_flat))));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        disposable = new CompositeDisposable();
        //noinspection ConstantConditions
        homePresenter = new HomePresenter(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getMainActivity().getSlidingUpPanelLayout().setShadowHeight(8);
        setStatusbarColorAuto(view);
        getMainActivity().setBottomBarVisibility(View.VISIBLE);

        setupToolbar();
        loadImageFromStorage();
        homePresenter.subscribe();
        checkPadding();
    }

    @SuppressWarnings("ConstantConditions")
    private void setupToolbar() {
        if (!PreferenceUtil.getInstance(getContext()).getFullScreenMode()) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) toolbar
                    .getLayoutParams();
            params.topMargin = getResources().getDimensionPixelOffset(R.dimen.status_bar_padding);
            toolbar.setLayoutParams(params);
        }

        appbar.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                int color = Color.BLACK;
                switch (state) {
                    case COLLAPSED:
                        getMainActivity().setLightStatusbar(!ATHUtil.isWindowBackgroundDark(getContext()));
                        color = ATHUtil.resolveColor(getContext(), R.attr.iconColor);
                        break;
                    default:
                    case EXPANDED:
                    case IDLE:
                        getMainActivity().setLightStatusbar(false);
                        color = ContextCompat.getColor(getContext(), R.color.md_white_1000);
                        break;
                }
                toolbarLayout.setExpandedTitleColor(color);
                ToolbarContentTintHelper.colorizeToolbar(toolbar, color, getActivity());
            }
        });

        int primaryColor = ThemeStore.primaryColor(getContext());

        toolbarLayout.setStatusBarScrimColor(primaryColor);
        toolbarLayout.setContentScrimColor(primaryColor);
        TintHelper.setTintAuto(background, primaryColor, true);

        toolbar.setTitle(R.string.home);
        getMainActivity().setSupportActionBar(toolbar);

        titleWelcome.setText(String.format("%s, %s!", getTimeOfTheDay(), PreferenceUtil.getInstance(getContext()).getUserName()));
        container.setBackgroundColor(primaryColor);
    }


    @Override
    public boolean handleBackPress() {
        return false;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        disposable.clear();
        homePresenter.unsubscribe();
    }

    @Override
    public void loading() {

    }

    @Override
    public void showEmptyView() {

    }

    @Override
    public void completed() {

    }

    @Override
    public void showData(ArrayList<Object> homes) {
        //homeAdapter.swapDataSet(homes);
    }

    @Override
    public void onMediaStoreChanged() {
        super.onMediaStoreChanged();
        homePresenter.subscribe();
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        checkPadding();
    }

    @Override
    public void onQueueChanged() {
        super.onQueueChanged();
        checkPadding();
    }

    private void checkPadding() {
        int height = getResources().getDimensionPixelSize(R.dimen.mini_player_height);
        container.setPadding(0, 0, 0, MusicPlayerRemote.getPlayingQueue().isEmpty() ? height * 2 : 0);
    }

    @Override
    public void recentArtist(ArrayList<Artist> artists) {
        recentArtistContainer.setVisibility(View.VISIBLE);
        recentArtistRV.setLayoutManager(new GridLayoutManager(getMainActivity(),
                1, GridLayoutManager.HORIZONTAL, false));
        ArtistAdapter artistAdapter = new ArtistAdapter(getMainActivity(), artists,
                R.layout.item_artist, false, null);
        recentArtistRV.setAdapter(artistAdapter);
    }

    @Override
    public void recentAlbum(ArrayList<Album> albums) {
        recentAlbumsContainer.setVisibility(View.VISIBLE);
        AlbumFullWithAdapter artistAdapter = new AlbumFullWithAdapter(getMainActivity(),
                getDisplayMetrics());
        artistAdapter.swapData(albums);
        recentAlbumRV.setAdapter(artistAdapter);
    }

    @Override
    public void topArtists(ArrayList<Artist> artists) {
        topArtistContainer.setVisibility(View.VISIBLE);
        topArtistRV.setLayoutManager(new GridLayoutManager(getMainActivity(),
                1, GridLayoutManager.HORIZONTAL, false));
        ArtistAdapter artistAdapter = new ArtistAdapter(getMainActivity(), artists,
                R.layout.item_artist, false, null);
        topArtistRV.setAdapter(artistAdapter);

    }

    @Override
    public void topAlbums(ArrayList<Album> albums) {
        topAlbumContainer.setVisibility(View.VISIBLE);
        AlbumFullWithAdapter artistAdapter = new AlbumFullWithAdapter(getMainActivity(),
                getDisplayMetrics());
        artistAdapter.swapData(albums);
        topAlbumRV.setAdapter(artistAdapter);
    }

    private DisplayMetrics getDisplayMetrics() {
        Display display = getMainActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        return metrics;
    }

    @Override
    public void suggestions(ArrayList<Playlist> playlists) {

    }

    private int getRecyclerView() {
        return R.layout.recycler_view_sec;
    }

    @Override
    public void geners(ArrayList<Genre> genres) {
        genreContainer.setVisibility(View.VISIBLE);
        genresRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        //noinspection ConstantConditions
        GenreAdapter genreAdapter = new GenreAdapter(getActivity(), genres, R.layout.item_list);
        genresRecyclerView.setAdapter(genreAdapter);
    }


    @OnClick({R.id.last_added, R.id.top_played, R.id.action_shuffle, R.id.timer, R.id.history})
    public void onViewClicked(View view) {
        Activity activity = getActivity();
        if (activity != null) {
            switch (view.getId()) {
                case R.id.action_shuffle:
                    MusicPlayerRemote
                            .openAndShuffleQueue(SongLoader.getAllSongs(activity).blockingFirst(), true);
                    break;
                case R.id.last_added:
                    NavigationUtil.goToPlaylistNew(activity, new LastAddedPlaylist(activity));
                    break;
                case R.id.top_played:
                    NavigationUtil.goToPlaylistNew(activity, new MyTopTracksPlaylist(activity));
                    break;
                case R.id.history:
                    NavigationUtil.goToPlaylistNew(activity, new HistoryPlaylist(activity));
                    break;
                case R.id.timer:
                    if (getFragmentManager() != null) {
                        new SleepTimerDialog().show(getFragmentManager(), TAG);
                    }
                    break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_folder:
                getMainActivity().setCurrentFragment(FoldersFragment.newInstance(getContext()), true);
                break;
            case R.id.action_settings:
                //noinspection ConstantConditions
                NavigationUtil.goToSettings(getActivity());
                break;
            case R.id.action_search:
                NavigationUtil.goToSearch(getMainActivity());

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_home, menu);
    }

    @OnClick(R.id.user_info_container)
    void startUserInfo() {
        //noinspection ConstantConditions
        NavigationUtil.goToUserInfo(getActivity());
    }

    @Override
    public void onPlayingMetaChanged() {
        super.onPlayingMetaChanged();

        homePresenter.loadRecentArtists();
        homePresenter.loadRecentAlbums();

    }
}