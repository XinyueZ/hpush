package com.hpush.app.adapters;

import java.util.List;

import com.hpush.R;
import com.hpush.data.RecentListItem;

/**
 * The adapter for list of all dailies.
 *
 * @author Xinyue Zhao
 */
public final class DailiesListAdapter extends MessagesListAdapter<RecentListItem> {
	/**
	 * Constructor of {@link com.hpush.app.adapters.DailiesListAdapter}.
	 */
	public DailiesListAdapter(List<RecentListItem> messages) {
		super(messages, R.menu.item);
	}

	@Override
	public void onBindViewHolder(MessagesListAdapter.ViewHolder viewHolder, int i) {
		super.onBindViewHolder(viewHolder, i);
		final RecentListItem recentListItem = getMessages().get(i);
		viewHolder.mToolbar.getMenu().findItem(R.id.action_item_bookmark).setVisible(!recentListItem.isBookmarked());
		viewHolder.mFloatTv.setOnClickListener(null);
	}
}
