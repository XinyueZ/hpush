package com.hpush.app.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.hpush.app.fragments.BookmarksListFragment;
import com.hpush.app.fragments.MessagesListFragment;

/**
 * Adapter for main viewpager.
 */
public final class MainViewPagerAdapter extends FragmentPagerAdapter {
	private Context mContext;

	public MainViewPagerAdapter(Context cxt, FragmentManager fm) {
		super(fm);
		mContext = cxt;
	}

	@Override
	public Fragment getItem(int position) {
		Fragment f = null;
		switch (position) {
		case 1:
			f = BookmarksListFragment.newInstance(mContext);
			break;
		case 0:
			f = MessagesListFragment.newInstance(mContext);
		default:
			break;
		}
		return f;
	}

	@Override
	public int getCount() {
		return 2;
	}
}
