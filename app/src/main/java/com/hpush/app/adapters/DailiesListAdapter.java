package com.hpush.app.adapters;

import java.util.List;

import com.hpush.R;
import com.hpush.data.DailyListItem;

/**
 * The adapter for list of all dailies.
 *
 * @author Xinyue Zhao
 */
public final class DailiesListAdapter extends MessagesListAdapter<DailyListItem> {
	/**
	 * Constructor of {@link com.hpush.app.adapters.DailiesListAdapter}.
	 */
	public DailiesListAdapter(List<DailyListItem> messages) {
		super(messages, R.menu.item);
	}

	@Override
	public void onBindViewHolder(MessagesListAdapter.ViewHolder viewHolder, int i) {
		super.onBindViewHolder(viewHolder, i);
		final DailyListItem dailyListItem = getMessages().get(i);
		viewHolder.mToolbar.getMenu().findItem(R.id.action_item_bookmark).setVisible(!dailyListItem.isBookmarked());
	}
}
