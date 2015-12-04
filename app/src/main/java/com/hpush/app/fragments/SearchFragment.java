package com.hpush.app.fragments;

import java.util.List;

import android.content.Context;
import android.os.Bundle;

import com.hpush.data.RecentListItem;

/**
 * Show all results of search.
 *
 * @author Xinyue Zhao
 */
public final class SearchFragment extends DailiesLstFragment {
	/**
	 * Storage of the argument of keyword.
	 */
	private static final String EXTRAS_KEYWORD = "com.hpush.app.fragments.EXTRAS.keyword";

	/**
	 * Create new instance of {@link com.hpush.app.fragments.SearchFragment}.
	 *
	 * @param context
	 * 		{@link android.content.Context}.
	 * @param keyword
	 * 		Word to search.
	 *
	 * @return An instance of {@link com.hpush.app.fragments.SearchFragment}.
	 */
	public static SearchFragment newInstance( Context context, String keyword ) {
		Bundle args = new Bundle();
		args.putString(
				EXTRAS_KEYWORD,
				keyword
		);
		return (SearchFragment) DailiesLstFragment.instantiate(
				context,
				SearchFragment.class.getName(),
				args
		);
	}

	/**
	 * @return The keyword to search.
	 */
	private String getKeyword() {
		return getArguments().getString( EXTRAS_KEYWORD );
	}

	@Override
	protected List<RecentListItem> doSearch() {
		return getDB().search( getKeyword() );
	}
}
