package com.hpush.app.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.hpush.R;
import com.hpush.app.fragments.BookmarksListFragment;
import com.hpush.app.fragments.MessagesListFragment;

/**
 * Adapter for main viewpager.
 */
public final class MainViewPagerAdapter extends FragmentPagerAdapter {
	private Context mContext;
	private final int[] TITLES = { R.string.lbl_last_messages , R.string.lbl_bookmark };
	private int mScrollY;

	public MainViewPagerAdapter( Context cxt, FragmentManager fm ) {
		super( fm );
		mContext = cxt;
	}

	@Override
	public Fragment getItem( int position ) {
		Fragment f = null;
		switch( position ) {
			case 1:
				f = BookmarksListFragment.newInstance( mContext );
				break;
			case 0:
				f = MessagesListFragment.newInstance( mContext );
			default:
				break;
		}
		return f;
	}

	@Override
	public int getCount() {
		return 2;
	}


	@Override
	public CharSequence getPageTitle( int position ) {
		return mContext.getString( TITLES[ position ] );
	}

	public void setScrollY( int scrollY ) {
		mScrollY = scrollY;
	}

	public Fragment getItemAt( int position ) {
		return getItem( position );
	}

}
