package com.hpush.app.adapters;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.MenuRes;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hpush.R;
import com.hpush.bus.BookmarkMessageEvent;
import com.hpush.bus.ClickMessageCommentsEvent;
import com.hpush.bus.ClickMessageEvent;
import com.hpush.bus.ClickMessageLinkEvent;
import com.hpush.bus.SelectMessageEvent;
import com.hpush.bus.ShareIntentEvent;
import com.hpush.bus.ShareMessageEvent;
import com.hpush.bus.ShareMessageEvent.Type;
import com.hpush.data.MessageListItem;
import com.hpush.utils.DynamicShareActionProvider;
import com.hpush.utils.Prefs;
import com.hpush.utils.Utils;
import com.hpush.views.OnViewAnimatedClickedListener;
import com.hpush.views.OnViewAnimatedClickedListener2;
import com.tinyurl4j.Api;
import com.tinyurl4j.data.Response;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;

/**
 * The adapter for list of messages.
 *
 * @author Xinyue Zhao
 */
public   class MessagesListAdapter<T extends MessageListItem> extends RecyclerView.Adapter<MessagesListAdapter.ViewHolder> {
	/**
	 * Main layout for this component.
	 */
	private static final int ITEM_LAYOUT = R.layout.item_messages_list;
	/**
	 * Menu resource for facebook and tweet.
	 */
	private static final int MENU_FB_TW = R.menu.item3;
	/**
	 * Data collection.
	 */
	private List<T> mMessages;
	/**
	 * The overflow for toolbar on items.
	 */
	private int menuResId;

	/**
	 * Constructor of {@link MessagesListAdapter}.
	 * @param messages Data source for the list.
	 *                 @param menuResId   The overflow for toolbar on items.
	 */
	public MessagesListAdapter(List<T> messages, @MenuRes int menuResId) {
		mMessages = messages;
		this.menuResId = menuResId;
	}

	/**
	 * Set data-source.
	 * @param messages Data source for the list.
	 */
	public void setMessages(List<T> messages) {
		mMessages = messages;
	}
	/**
	 * Get data-source.
	 * @return  Data source for the list.
	 */
	public List<T> getMessages() {
		return mMessages;
	}

	@Override
	public MessagesListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
		Context cxt = viewGroup.getContext();
		boolean landscape = cxt.getResources().getBoolean(R.bool.landscape);
		View convertView = LayoutInflater.from(cxt).inflate(ITEM_LAYOUT, viewGroup, false);
		MessagesListAdapter.ViewHolder viewHolder = new MessagesListAdapter.ViewHolder(convertView, menuResId, landscape);
		return viewHolder;
	}

	@Override
	public void onBindViewHolder(final MessagesListAdapter.ViewHolder viewHolder, int i) {
		final MessageListItem msg = mMessages.get(i);
		if (!TextUtils.isEmpty(msg.getTitle())) {
			viewHolder.mFloatTv.setText(msg.getTitle().charAt(0) + "");
			viewHolder.mHeadLineTv.setVisibility(View.VISIBLE);
			viewHolder.mHeadLineTv.setText(msg.getTitle());
		} else {
			viewHolder.mFloatTv.setVisibility(View.GONE);
			viewHolder.mHeadLineTv.setVisibility(View.GONE);
			viewHolder.mHeadLineTv.setText("Headline");
		}
		if (!TextUtils.isEmpty(msg.getText())) {
			viewHolder.mContentTv.setVisibility(View.VISIBLE);
			viewHolder.mContentTv.setText(msg.getText());
		} else {
			viewHolder.mContentTv.setVisibility(View.GONE);
			viewHolder.mContentTv.setText("Content");
		}
		viewHolder.mScoresTv.setText(msg.getScore() + "");
		viewHolder.mCommentsCountTv.setText(msg.getCommentCounts()+"");
		viewHolder.mEditorTv.setText(msg.getBy());
		viewHolder.mTimeTv.setText(Utils.convertTimestamps2DateString(viewHolder.itemView.getContext(),
				msg.getTime() * 1000));
		viewHolder.itemView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EventBus.getDefault().post(new ClickMessageEvent(msg.getMessage()));
			}
		});
		((View)viewHolder.mCommentsCountTv.getParent()).setOnClickListener(new OnViewAnimatedClickedListener() {
			@Override
			public void onClick() {
				EventBus.getDefault().post(new ClickMessageCommentsEvent(msg.getMessage(), viewHolder.itemView));
			}
		});

		Context cxt = viewHolder.mToolbar.getContext();
		final String subject = cxt.getString(R.string.lbl_share_item_title);
		final String content = cxt.getString(R.string.lbl_share_item_content);
		final String hackerNewsHomeUrl = Prefs.getInstance(cxt).getHackerNewsHomeUrl();
		MenuItem shareMi = viewHolder.mToolbar.getMenu().findItem(R.id.action_item_share);
		DynamicShareActionProvider shareLaterProvider = (DynamicShareActionProvider) MenuItemCompat.getActionProvider(
				shareMi);
		shareLaterProvider.setShareDataType("text/plain");
		shareLaterProvider.setOnShareLaterListener(new DynamicShareActionProvider.OnShareLaterListener() {
			@Override
			public void onShareClick(final Intent shareIntent) {
				Api.getTinyUrl(msg.getUrl(), new retrofit.Callback<Response>() {
					@Override
					public void success(Response response, retrofit.client.Response response2) {
						String text;
						String sharedLink;
						if (response != null) {
							sharedLink = TextUtils.isEmpty(response.getResult()) ?
									msg.getUrl() : response.getResult();
						} else {
							sharedLink = hackerNewsHomeUrl;
						}
						text = String.format(content, msg.getTitle(), sharedLink );

						shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
						shareIntent.putExtra(Intent.EXTRA_TEXT, text);
						EventBus.getDefault().post(new ShareIntentEvent(shareIntent));
					}

					@Override
					public void failure(RetrofitError error) {
						String text = String.format(content, msg.getTitle(), msg.getUrl());
						shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
						shareIntent.putExtra(Intent.EXTRA_TEXT, text);
						EventBus.getDefault().post(new ShareIntentEvent(shareIntent));
					}
				});
			}
		});

		viewHolder.mToolbar.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem menuItem) {
				switch (menuItem.getItemId()) {
				case R.id.action_item_comment:
					EventBus.getDefault().post(new ClickMessageCommentsEvent(msg.getMessage(), viewHolder.itemView));
					break;
				case R.id.action_item_bookmark:
					EventBus.getDefault().post(new BookmarkMessageEvent(msg));
					break;
				//Facebook and tweet are available for large screen on toolbar which binds menu of item(2).xml, see different menu resource of item(2).xml.
				case R.id.action_facebook:
					EventBus.getDefault().post(new ShareMessageEvent(msg, Type.Facebook));
					break;
				case R.id.action_tweet:
					break;
				}
				return true;
			}
		});

		//Facebook and tweet are available for small screen on toolbar-3 which binds menu of item3.xml, see different menu resource of item(2).xml.
		viewHolder.mToolbar3.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem menuItem) {
				switch (menuItem.getItemId()) {
				case R.id.action_facebook:
					EventBus.getDefault().post(new ShareMessageEvent(msg, Type.Facebook));
					break;
				case R.id.action_tweet:
					break;
				}
				return true;
			}
		});

		viewHolder.mFloatTv.setBackgroundResource(msg.isChecked() ? R.drawable.circle_grey : R.drawable.circle_orange);
		viewHolder.mFloatTv.setOnClickListener(new OnViewAnimatedClickedListener2() {
			@Override
			public void onClick() {
				msg.setChecked(!msg.isChecked());
				viewHolder.mFloatTv.setBackgroundResource(msg.isChecked() ? R.drawable.circle_grey : R.drawable.circle_orange);
				EventBus.getDefault().post(new SelectMessageEvent());
			}
		});

		viewHolder.mContentV.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EventBus.getDefault().post(new ClickMessageLinkEvent(msg.getMessage(), viewHolder.itemView));
			}
		});
	}

	@Override
	public int getItemCount() {
		return mMessages == null ? 0 : mMessages.size();
	}

	static class ViewHolder extends RecyclerView.ViewHolder {
		TextView mFloatTv;
		TextView mHeadLineTv;
		TextView mContentTv;
		TextView mScoresTv;
		TextView mCommentsCountTv;
		TextView mEditorTv;
		TextView mTimeTv;
		Toolbar mToolbar;
		Toolbar mToolbar3;
		View mContentV;

		private ViewHolder(View convertView, int menuResId, boolean landscape) {
			super(convertView);
			mFloatTv = (TextView) convertView.findViewById(R.id.float_tv);
			mHeadLineTv = (TextView) convertView.findViewById(R.id.headline_tv);
			mContentTv = (TextView) convertView.findViewById(R.id.content_tv);
			mScoresTv = (TextView) convertView.findViewById(R.id.scores_tv);
			mCommentsCountTv = (TextView) convertView.findViewById(R.id.comments_count_tv);
			mEditorTv = (TextView) convertView.findViewById(R.id.editor_tv);
			mTimeTv = (TextView) convertView.findViewById(R.id.time_tv);
			mContentV = convertView.findViewById(R.id.content_v);
			mToolbar = (Toolbar) convertView.findViewById(R.id.toolbar);
			mToolbar.inflateMenu(menuResId);
			mToolbar3 = (Toolbar) convertView.findViewById(R.id.toolbar_3);
			if(!landscape) {
				mToolbar3.inflateMenu(MENU_FB_TW);
				mToolbar3.setVisibility(View.VISIBLE);
			} else {
				mToolbar3.setVisibility(View.GONE);
			}
		}
	}
}
