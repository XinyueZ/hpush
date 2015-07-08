package com.hpush.app.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chopping.application.BasicPrefs;
import com.chopping.bus.CloseDrawerEvent;
import com.chopping.fragments.BaseFragment;
import com.chopping.utils.Utils;
import com.hpush.R;
import com.hpush.app.App;
import com.hpush.app.activities.ConnectGoogleActivity;
import com.hpush.utils.Prefs;
import com.squareup.picasso.Picasso;

import de.greenrobot.event.EventBus;

/**
 * The fragment that controls user information of g+, logout etc.
 *
 * @author Xinyue Zhao
 */
public final class GPlusFragment extends BaseFragment {
	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.fragment_gplus;

	/**
	 * Photo.
	 */
	private ImageView mPhotoIv;
	/**
	 * Name.
	 */
	private TextView mNameTv;
	/**
	 * Logout.
	 */
	private View mLogoutV;


	/**
	 * New an instance of {@link com.hpush.app.fragments.GPlusFragment}.
	 *
	 * @param context
	 * 		{@link android.content.Context}.
	 *
	 * @return An instance of {@link com.hpush.app.fragments.GPlusFragment}.
	 */
	public static Fragment newInstance(Context context) {
		return GPlusFragment.instantiate(context, GPlusFragment.class.getName());
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(LAYOUT, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mPhotoIv = (ImageView) view.findViewById(R.id.people_photo_iv);
		mNameTv = (TextView) view.findViewById(R.id.people_name_tv);
		mLogoutV = view.findViewById(R.id.logout_btn);
		mLogoutV.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EventBus.getDefault().post(new CloseDrawerEvent());

				//Logout and delete all userdata.
				com.hpush.utils.Utils.logout();
				Activity activity = getActivity();
				if (activity != null) {
					ConnectGoogleActivity.showInstance(getActivity());
				}
			}
		});



	}

	@Override
	public void onResume() {
		Prefs prefs = Prefs.getInstance(App.Instance);
		Picasso picasso = Picasso.with(App.Instance);
		if (!TextUtils.isEmpty(prefs.getGoogleThumbUrl())) {
			picasso.load(Utils.uriStr2URI(prefs.getGoogleThumbUrl())
					.toASCIIString()).into(mPhotoIv);
		}
		mNameTv.setText(getString(R.string.lbl_hello, prefs.getGoogleDisplyName()));
		super.onResume();
	}

	/**
	 * App that use this Chopping should know the preference-storage.
	 *
	 * @return An instance of {@link com.chopping.application.BasicPrefs}.
	 */
	@Override
	protected BasicPrefs getPrefs() {
		return Prefs.getInstance(getActivity().getApplication());
	}
}
