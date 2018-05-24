package code.name.monkey.retromusic.mvp.contract;

import code.name.monkey.retromusic.model.Album;
import code.name.monkey.retromusic.model.Artist;
import code.name.monkey.retromusic.model.Genre;
import code.name.monkey.retromusic.model.Playlist;
import code.name.monkey.retromusic.model.smartplaylist.AbsSmartPlaylist;
import code.name.monkey.retromusic.mvp.BasePresenter;
import code.name.monkey.retromusic.mvp.BaseView;

import java.util.ArrayList;

public interface HomeContract {

    interface HomeView extends BaseView<ArrayList<Object>> {

        void recentArtist(ArrayList<Artist> artists);

        void recentAlbum(ArrayList<Album> albums);

        void topArtists(ArrayList<Artist> artists);

        void topAlbums(ArrayList<Album> albums);

        void suggestions(ArrayList<Playlist> songs);

        void geners(ArrayList<Genre> songs);
    }

    interface HomePresenter extends BasePresenter<HomeView> {

        void loadRecentAlbums();

        void loadTopAlbums();

        void loadRecentArtists();

        void loadTopArtists();

        void loadSuggestions();

        void loadGenres();
    }
}
