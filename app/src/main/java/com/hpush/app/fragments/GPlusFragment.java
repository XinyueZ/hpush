package com.hpush.app.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.chopping.application.BasicPrefs;
import com.chopping.bus.CloseDrawerEvent;
import com.chopping.fragments.BaseFragment;
import com.chopping.net.TaskHelper;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.hpush.R;
import com.hpush.bus.LoginedGPlusEvent;
import com.hpush.bus.LogoutGPlusEvent;
import com.hpush.utils.Prefs;
import com.hpush.views.OnViewAnimatedClickedListener2;

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
	private NetworkImageView mPhotoIv;
	/**
	 * Name.
	 */
	private TextView mNameTv;
	/**
	 * Logout.
	 */
	private View mLogoutV;
	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link LoginedGPlusEvent}.
	 *
	 * @param e
	 * 		Event {@link LoginedGPlusEvent}.
	 */
	public void onEvent(LoginedGPlusEvent e) {
		GoogleApiClient client = e.getPlusClient();
		Person person = Plus.PeopleApi.getCurrentPerson(client);
		mPhotoIv.setImageUrl(person.getImage().getUrl(), TaskHelper.getImageLoader());
		mNameTv.setText(person.getDisplayName() + "," + person.getDisplayName());
		mPhotoIv.setVisibility(View.VISIBLE);
		mNameTv.setVisibility(View.VISIBLE);
		mLogoutV.setVisibility(View.VISIBLE);
	}

	//------------------------------------------------

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
		mPhotoIv = (NetworkImageView) view.findViewById(R.id.people_photo_iv);
		mNameTv = (TextView) view.findViewById(R.id.people_name_tv);
		mLogoutV = view.findViewById(R.id.logout_btn);
		mLogoutV.setOnClickListener(new OnViewAnimatedClickedListener2() {
			@Override
			public void onClick() {
				mPhotoIv.setVisibility(View.INVISIBLE);
				mNameTv.setVisibility(View.INVISIBLE);
				mLogoutV.setVisibility(View.INVISIBLE);
				EventBus.getDefault().post(new LogoutGPlusEvent());
				EventBus.getDefault().post(new CloseDrawerEvent());
			}
		});
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
