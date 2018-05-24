package code.name.monkey.retromusic.ui.fragments;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import code.name.monkey.retromusic.R;


public enum NowPlayingScreen {
    NORMAL(R.string.normal, R.drawable.np_normal, 0),
    FLAT(R.string.flat, R.drawable.np_flat, 1),
    FULL(R.string.full, R.drawable.np_full, 2),
    PLAIN(R.string.plain, R.drawable.np_plain, 3),
    BLUR(R.string.blur, R.drawable.np_blur, 4),
    COLOR(R.string.color, R.drawable.np_color, 5),
    CARD(R.string.card, R.drawable.np_card, 6),
    TINY(R.string.tiny, R.drawable.np_tiny, 7),
    SIMPLE(R.string.simple, R.drawable.np_simple, 8),
    HOLIDAY(R.string.holiday, R.drawable.np_normal, 9),
    BLUR_CARD(R.string.blur_card, R.drawable.np_blur_card, 10),
    ADAPTIVE(R.string.adaptive, R.drawable.np_blur_card, 11);

    @StringRes
    public final int titleRes;
    @DrawableRes
    public final int drawableResId;
    public final int id;

    NowPlayingScreen(@StringRes int titleRes, @DrawableRes int drawableResId, int id) {
        this.titleRes = titleRes;
        this.drawableResId = drawableResId;
        this.id = id;
    }
}
