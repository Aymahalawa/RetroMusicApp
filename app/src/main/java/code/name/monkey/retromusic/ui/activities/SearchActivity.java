package code.name.monkey.retromusic.ui.activities;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.transition.TransitionManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import code.name.monkey.appthemehelper.ThemeStore;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.mvp.contract.SearchContract;
import code.name.monkey.retromusic.mvp.presenter.SearchPresenter;
import code.name.monkey.retromusic.ui.activities.base.AbsMusicServiceActivity;
import code.name.monkey.retromusic.ui.adapter.SearchAdapter;
import code.name.monkey.retromusic.util.Util;
import code.name.monkey.retromusic.util.ViewUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class SearchActivity extends AbsMusicServiceActivity implements OnQueryTextListener,
    SearchContract.SearchView, TextWatcher {

  public static final String TAG = SearchActivity.class.getSimpleName();
  public static final String QUERY = "query";
  private static final int REQ_CODE_SPEECH_INPUT = 9002;
  @BindView(R.id.voice_search)
  View micIcon;
  @BindView(R.id.recycler_view)
  RecyclerView recyclerView;
  @BindView(R.id.toolbar)
  Toolbar toolbar;
  @BindView(android.R.id.empty)
  TextView empty;
  @BindView(R.id.search_view)
  EditText searchView;
  @BindView(R.id.root)
  CoordinatorLayout container;
  @BindView(R.id.appbar)
  AppBarLayout appbar;
  @BindView(R.id.status_bar)
  View statusBar;
  private SearchPresenter searchPresenter;
  private SearchAdapter adapter;
  private String query;

  private boolean isMicSearch = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setDrawUnderStatusbar(true);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_search);

    ButterKnife.bind(this);

    ViewUtil.setStatusBarHeight(this, statusBar);

    searchPresenter = new SearchPresenter(this);

    setStatusbarColorAuto();
    setNavigationbarColorAuto();
    setTaskDescriptionColorAuto();
    setLightNavigationBar(true);

    setupRecyclerview();
    setUpToolBar();
    setupSearchView();

    if (savedInstanceState != null) {
      query = savedInstanceState.getString(QUERY);
      searchPresenter.search(query);
    }

    if (getIntent().getBooleanExtra("mic_search", false)) {
      startMicSearch();
      isMicSearch = true;
    }
  }

  private void setupRecyclerview() {
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    adapter = new SearchAdapter(this, Collections.emptyList());
    adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
      @Override
      public void onChanged() {
        super.onChanged();
        empty.setVisibility(adapter.getItemCount() < 1 ? View.VISIBLE : View.GONE);
      }
    });
    recyclerView.setAdapter(adapter);
  }

  private void setupSearchView() {
    SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
    if (searchManager != null) {
      searchView.addTextChangedListener(this);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    //Log.i(TAG, "onResume: " + query);
    searchPresenter.subscribe();
    searchPresenter.search(query);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    searchPresenter.unsubscribe();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(QUERY, query);
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    searchPresenter.search(savedInstanceState.getString(QUERY, ""));
  }

  private void setUpToolBar() {
    int primaryColor = ThemeStore.primaryColor(this);
    toolbar.setBackgroundColor(primaryColor);
    appbar.setBackgroundColor(primaryColor);
    setSupportActionBar(toolbar);
    //noinspection ConstantConditions
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
    }
    return super.onOptionsItemSelected(item);
  }

  private void search(@NonNull String query) {
    this.query = query;
    TransitionManager.beginDelayedTransition(toolbar);
    micIcon.setVisibility(query.length() > 0 ? View.GONE : View.VISIBLE);
    searchPresenter.search(query);
  }

  @Override
  public void onMediaStoreChanged() {
    super.onMediaStoreChanged();
    searchPresenter.search(query);
  }

  @Override
  public boolean onQueryTextSubmit(String query) {
    hideSoftKeyboard();
    return false;
  }

  @Override
  public boolean onQueryTextChange(String newText) {
    search(newText);
    return false;
  }

  private void hideSoftKeyboard() {
    Util.hideSoftKeyboard(SearchActivity.this);
    if (searchView != null) {
      searchView.clearFocus();
    }
  }

  @Override
  public void loading() {

  }

  @Override
  public void showEmptyView() {
    adapter.swapDataSet(new ArrayList<>());
  }

  @Override
  public void completed() {

  }

  @Override
  public void showData(ArrayList<Object> list) {
    adapter.swapDataSet(list);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode) {
      case REQ_CODE_SPEECH_INPUT: {
        if (resultCode == RESULT_OK && null != data) {

          ArrayList<String> result = data
              .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
          query = result.get(0);
          searchView.setText(query, BufferType.EDITABLE);
          searchPresenter.search(query);
        }
        break;
      }
    }
  }

  @OnClick(R.id.voice_search)
  void searchImageView() {
    startMicSearch();
  }

  private void startMicSearch() {
    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    intent
        .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));
    try {
      startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
    } catch (ActivityNotFoundException e) {
      e.printStackTrace();
      Toast.makeText(this, getString(R.string.speech_not_supported), Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  public void beforeTextChanged(CharSequence s, int start, int count, int after) {

  }

  @Override
  public void onTextChanged(CharSequence newText, int start, int before, int count) {
    search(newText.toString());
  }

  @Override
  public void afterTextChanged(Editable s) {

  }
}
