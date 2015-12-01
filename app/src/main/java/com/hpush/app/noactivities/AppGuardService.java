package com.hpush.app.noactivities;


import java.util.Calendar;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.hpush.app.App;

public final class AppGuardService extends GcmTaskService {
	private static final String TAG = "AppGuardService";

	@Override
	public int onRunTask( TaskParams taskParams ) {
		Calendar calendar = Calendar.getInstance();
		int      day      = calendar.get( Calendar.DAY_OF_WEEK );
		startService( initService( this, false ) );
		if( day == Calendar.SUNDAY ) {
			startService( initService( this, true ) );
		}
		App.startAppGuardService( getApplication(), 1 );
		return GcmNetworkManager.RESULT_SUCCESS;
	}


	@NonNull
	private Intent initService( Context context, boolean allToRemove ) {
		Intent service;
		service = new Intent( context, DeleteDataService.class );
		service.putExtra( DeleteDataService.EXTRAS_RMV_ALL, allToRemove );
		return service;
	}
}
