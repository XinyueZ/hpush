package com.hpush.app.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.hpush.R;
import com.hpush.app.fragments.SearchFragment;

/**
 * Show searched result list.
 *
 * @author Xinyue Zhao
 */
public final class SearchActivity extends DailiesActivity {

	/**
	 * Show single instance of {@link SearchActivity}
	 *
	 * @param cxt
	 * 		{@link android.content.Context}.
	 */
	public static void showInstance(Context cxt) {
		Intent intent = new Intent(cxt, SearchActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		cxt.startActivity(intent);
	}

	/**
	 * Handle search intent.
	 * @param intent Contains query keyword.
	 */
	private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			getSupportFragmentManager().beginTransaction().replace(R.id.search_result_container,
					SearchFragment.newInstance(getApplication(), query)).commit();
			getSupportActionBar().setTitle(query);
			setTitle(query);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		handleIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		handleIntent(intent);
	}

	@Override
	protected int getLayoutResId() {
		return R.layout.activity_search;
	}
}
