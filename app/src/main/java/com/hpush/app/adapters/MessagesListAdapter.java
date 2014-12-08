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
import com.hpush.bus.ClickMessageCommentsEvent;
import com.hpush.bus.ClickMessageEvent;
import com.hpush.bus.ClickMessageLinkEvent;
import com.hpush.data.MessageListItem;
import com.hpush.utils.Utils;
import com.hpush.views.OnViewAnimatedClickedListener;

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
	private LongSparseArray<MessageListItem> mMessages;

	public MessagesListAdapter(LongSparseArray<MessageListItem> messages) {
		mMessages = messages;
	}

	public void setMessages(LongSparseArray<MessageListItem> messages) {
		mMessages = messages;
	}

	@Override
	public MessagesListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
		View convertView = LayoutInflater.from(viewGroup.getContext()).inflate(ITEM_LAYOUT, viewGroup, false);
		MessagesListAdapter.ViewHolder viewHolder = new MessagesListAdapter.ViewHolder(convertView);
		return viewHolder;
	}

	@Override
	public void onBindViewHolder(final MessagesListAdapter.ViewHolder viewHolder, int i) {
		long id = mMessages.keyAt(i);
		final MessageListItem msg = mMessages.get(id);
		viewHolder.mHeadLineTv.setText(msg.getTitle());
		if (!TextUtils.isEmpty(msg.getText())) {
			viewHolder.mContentTv.setVisibility(View.VISIBLE);
			viewHolder.mContentTv.setText(msg.getText());
		} else {
			viewHolder.mContentTv.setVisibility(View.GONE);
			viewHolder.mContentTv.setText("Content");
		}
		viewHolder.mScoresTv.setText(msg.getScore() + "");
		viewHolder.mEditorTv.setText(msg.getBy());
		viewHolder.mTimeTv.setText(Utils.convertTimestamps2DateString(viewHolder.itemView.getContext(),
				msg.getTime() * 1000));
		viewHolder.itemView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EventBus.getDefault().post(new ClickMessageEvent(msg.getMessage()));
			}
		});
		viewHolder.mCommentsV.setOnClickListener(new OnViewAnimatedClickedListener() {
			@Override
			public void onClick() {
				EventBus.getDefault().post(new ClickMessageCommentsEvent(msg.getMessage(), viewHolder.mCommentsV));
			}
		});
		viewHolder.mLinkV.setOnClickListener(new OnViewAnimatedClickedListener() {
			@Override
			public void onClick() {
				EventBus.getDefault().post(new ClickMessageLinkEvent(msg.getMessage(), viewHolder.mLinkV));
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
		TextView mScoresTv;
		TextView mEditorTv;
		TextView mTimeTv;
		View mCommentsV;
		View mLinkV;

		private ViewHolder(View convertView) {
			super(convertView);
			mHeadLineTv = (TextView) convertView.findViewById(R.id.headline_tv);
			mContentTv = (TextView) convertView.findViewById(R.id.content_tv);
			mScoresTv = (TextView) convertView.findViewById(R.id.scores_tv);
			mEditorTv = (TextView) convertView.findViewById(R.id.editor_tv);
			mTimeTv = (TextView) convertView.findViewById(R.id.time_tv);
			mCommentsV = convertView.findViewById(R.id.comments_btn);
			mLinkV = convertView.findViewById(R.id.link_btn);
		}
	}
}
