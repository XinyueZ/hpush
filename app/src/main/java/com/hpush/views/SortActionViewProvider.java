package com.hpush.views;

import android.content.Context;
import android.support.v4.view.ActionProvider;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnDismissListener;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.hpush.R;
import com.hpush.bus.SortAllEvent;
import com.hpush.utils.Prefs;

import de.greenrobot.event.EventBus;

/**
 * A popup-menu to sort views.
 *
 * @author Xinyue Zhao
 */
public final class SortActionViewProvider extends ActionProvider implements OnDismissListener, OnMenuItemClickListener {
	/**
	 * Layout Id for the provider.
	 */
	private static final int LAYOUT   = R.layout.action_view_provider_sort;
	/**
	 * Menu-resource of the popup.
	 */
	private static final int MENU_RES = R.menu.sort;
	/**
	 * A {@link android.view.View} for this provider.
	 */
	private View      mProviderV;
	/**
	 * A popup with list of all sort-types.
	 */
	private PopupMenu mPopupMenu;
	/**
	 * Show/Hidden status of menu.
	 */
	private boolean   mShow;

	public SortActionViewProvider( Context context ) {
		super( context );
		mProviderV = LayoutInflater.from( context )
								   .inflate(
										   LAYOUT,
										   null,
										   false
								   );
		mProviderV.setOnClickListener( new OnViewAnimatedClickedListener() {
			@Override
			public void onClick() {
				if( !mShow ) {
					mShow = true;
					mPopupMenu.show();
					updateMenuItems( mPopupMenu.getMenu() );
				} else {
					mPopupMenu.dismiss();
				}
			}
		} );
		mPopupMenu = new PopupMenu(
				context,
				mProviderV
		);
		mPopupMenu.inflate( MENU_RES );
		mPopupMenu.setOnDismissListener( this );
		mPopupMenu.setOnMenuItemClickListener( this );
		updateMenuItems( mPopupMenu.getMenu() );
	}

	@Override
	public View onCreateActionView() {
		return mProviderV;
	}


	@Override
	public void onDismiss( PopupMenu popupMenu ) {
		mShow = false;
	}

	/**
	 * Update check-status for menu.
	 *
	 * @param menu
	 * 		The host of all menu-items.
	 */
	private void updateMenuItems( Menu menu ) {
		menu.findItem( R.id.action_sort_scores )
			.setChecked( 0 == selectedSortTypeValue() );
		menu.findItem( R.id.action_sort_arrival )
			.setChecked( 1 == selectedSortTypeValue() );
		menu.findItem( R.id.action_sort_creation )
			.setChecked( 2 == selectedSortTypeValue() );
		menu.findItem( R.id.action_sort_comments )
			.setChecked( 3 == selectedSortTypeValue() );
	}

	/**
	 * @return Get current selected sort-type {@code int}
	 */
	private int selectedSortTypeValue() {
		String sortTypeValue = Prefs.getInstance( getContext().getApplicationContext() )
									.getSortTypeValue();
		return Integer.valueOf( sortTypeValue );
	}

	@Override
	public boolean onMenuItemClick( MenuItem menuItem ) {
		Prefs prefs = Prefs.getInstance( getContext().getApplicationContext() );
		switch( menuItem.getItemId() ) {
			case R.id.action_sort_scores:
				prefs.setSortTypeValue( "0" );
				break;
			case R.id.action_sort_arrival:
				prefs.setSortTypeValue( "1" );
				break;
			case R.id.action_sort_creation:
				prefs.setSortTypeValue( "2" );
				break;
			case R.id.action_sort_comments:
				prefs.setSortTypeValue( "3" );
				break;
		}
		updateMenuItems( mPopupMenu.getMenu() );
		EventBus.getDefault()
				.post( new SortAllEvent() );
		return true;
	}
}
