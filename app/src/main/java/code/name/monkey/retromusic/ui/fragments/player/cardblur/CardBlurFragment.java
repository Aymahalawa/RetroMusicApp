package code.name.monkey.retromusic.ui.fragments.player.cardblur;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import code.name.monkey.appthemehelper.util.ToolbarContentTintHelper;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.glide.RetroMusicColoredTarget;
import code.name.monkey.retromusic.glide.SongGlideRequest;
import code.name.monkey.retromusic.helper.MusicPlayerRemote;
import code.name.monkey.retromusic.model.Song;
import code.name.monkey.retromusic.ui.fragments.base.AbsPlayerFragment;
import code.name.monkey.retromusic.ui.fragments.player.PlayerAlbumCoverFragment;
import code.name.monkey.retromusic.ui.fragments.player.normal.PlayerFragment;
import jp.wasabeef.glide.transformations.BlurTransformation;

public class CardBlurFragment extends AbsPlayerFragment implements PlayerAlbumCoverFragment.Callbacks {
    @BindView(R.id.player_toolbar)
    Toolbar toolbar;
    @BindView(R.id.now_playing_container)
    ViewGroup viewGroup;
    @BindView(R.id.gradient_background)
    ImageView colorBackground;

    private int lastColor;
    private CardBlurPlaybackControlsFragment playbackControlsFragment;
    private Unbinder unbinder;

    public static PlayerFragment newInstance() {
        Bundle args = new Bundle();
        PlayerFragment fragment = new PlayerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @ColorInt
    public int getPaletteColor() {
        return lastColor;
    }

    @Override
    public void onShow() {
        playbackControlsFragment.show();
    }

    @Override
    public void onHide() {
        playbackControlsFragment.hide();
        onBackPressed();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    public int toolbarIconColor() {
        return Color.WHITE;
    }

    @Override
    public void onColorChanged(int color) {
        playbackControlsFragment.setDark(color);
        lastColor = color;
        getCallbacks().onPaletteColorChanged();

        ToolbarContentTintHelper.colorizeToolbar(toolbar, Color.WHITE, getActivity());

    }

    @Override
    protected void toggleFavorite(Song song) {
        super.toggleFavorite(song);
        if (song.id == MusicPlayerRemote.getCurrentSong().id) {
            updateIsFavorite();
        }
    }

    @Override
    public void onFavoriteToggled() {
        toggleFavorite(MusicPlayerRemote.getCurrentSong());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_card_blur_player, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toggleStatusBar(viewGroup);

        setUpSubFragments();
        setUpPlayerToolbar();
    }

    private void setUpSubFragments() {
        playbackControlsFragment = (CardBlurPlaybackControlsFragment) getChildFragmentManager()
                .findFragmentById(R.id.playback_controls_fragment);

        PlayerAlbumCoverFragment playerAlbumCoverFragment =
                (PlayerAlbumCoverFragment) getChildFragmentManager()
                        .findFragmentById(R.id.player_album_cover_fragment);
        playerAlbumCoverFragment.setCallbacks(this);
        playerAlbumCoverFragment.removeEffect();
    }

    private void setUpPlayerToolbar() {
        toolbar.inflateMenu(R.menu.menu_player);
        toolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());
        toolbar.setOnMenuItemClickListener(this);

       /* for (int i = 0; i < toolbar.getMenu().size(); i++) {
            MenuItem menuItem = toolbar.getMenu().getItem(i);
            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }*/
        ToolbarContentTintHelper.colorizeToolbar(toolbar, Color.WHITE, getActivity());
    }

    @Override
    public void onServiceConnected() {
        updateIsFavorite();
        updateBlur();
    }

    @Override
    public void onPlayingMetaChanged() {
        updateIsFavorite();
        updateBlur();
    }

    private void updateBlur() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        int blurAmount = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("blur_amount", 25);

        colorBackground.clearColorFilter();
        SongGlideRequest.Builder.from(Glide.with(activity), MusicPlayerRemote.getCurrentSong())
                .checkIgnoreMediaStore(activity)
                .generatePalette(activity)
                .build()
                .transform(new BlurTransformation(getActivity(), blurAmount))
                .into(new RetroMusicColoredTarget(colorBackground) {
                    @Override
                    public void onColorReady(int color) {
                        if (color == getDefaultFooterColor()) {
                            colorBackground.setColorFilter(color);
                        }
                    }
                });
    }
}
