package com.hpush.app.adapters;

import android.support.v4.util.LongSparseArray;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hpush.R;
import com.hpush.bus.ClickMessageEvent;
import com.hpush.data.Message;

import de.greenrobot.event.EventBus;

/**
 * The adapter for list of messages.
 *
 * @author Xinyue Zhao
 */
public final class MessagesListAdapter extends RecyclerView.Adapter<MessagesListAdapter.ViewHolder> {
	/**
	 * Main layout for this component.
	 */
	private static final int ITEM_LAYOUT = R.layout.item_messages_list;
	/**
	 * Data collection.
	 */
	private LongSparseArray<Message> mMessages;

	public MessagesListAdapter(LongSparseArray<Message> messages) {
		mMessages = messages;
	}

	public void setMessages(LongSparseArray<Message> messages) {
		mMessages = messages;
	}

	@Override
	public MessagesListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
		View convertView =
				LayoutInflater.from(viewGroup.getContext())
						.inflate(ITEM_LAYOUT, viewGroup, false);
		MessagesListAdapter.ViewHolder viewHolder = new MessagesListAdapter.ViewHolder(convertView);
		return viewHolder;
	}

	@Override
	public void onBindViewHolder(MessagesListAdapter.ViewHolder viewHolder, int i) {
		long id = mMessages.keyAt(i);
		final Message msg = mMessages.get(id);
		viewHolder.mHeadLineTv.setText(msg.getTitle());
		if (!TextUtils.isEmpty(msg.getText())) {
			viewHolder.mContentTv.setVisibility(View.VISIBLE);
			viewHolder.mContentTv.setText(msg.getText());
		} else {
			viewHolder.mContentTv.setVisibility(View.GONE);
			viewHolder.mContentTv.setText("Content");
		}
		viewHolder.itemView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EventBus.getDefault().post(new ClickMessageEvent(msg));
			}
		});
	}

	@Override
	public int getItemCount() {
		return mMessages == null ? 0 : mMessages.size();
	}

	static class ViewHolder extends RecyclerView.ViewHolder {
		TextView mHeadLineTv;
		TextView mContentTv;

		private ViewHolder(View convertView) {
			super(convertView);
			mHeadLineTv = (TextView) convertView.findViewById(R.id.headline_tv);
			mContentTv = (TextView) convertView.findViewById(R.id.content_tv);
		}
	}
}
