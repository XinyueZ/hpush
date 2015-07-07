/*
                   _ooOoo_
                  o8888888o
                  88" . "88
                  (| -_- |)
                  O\  =  /O
               ____/`---'\____
             .'  \\|     |//  `.
            /  \\|||  :  |||//  \
           /  _||||| -:- |||||-  \
           |   | \\\  -  /// |   |
           | \_|  ''\---/''  |   |
           \  .-\__  `-`  ___/-. /
         ___`. .'  /--.--\  `. . __
      ."" '<  `.___\_<|>_/___.'  >'"".
     | | :  `- \`.;`\ _ /`;.`/ - ` : | |
     \  \ `-.   \_ __\ /__ _/   .-` /  /
======`-.____`-.___\_____/___.-`____.-'======
                   `=---='
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
         佛祖保佑       永无BUG
*/
//          佛曰:
//                  写字楼里写字间，写字间里程序员；
//                  程序人员写程序，又拿程序换酒钱。
//                  酒醒只在网上坐，酒醉还来网下眠；
//                  酒醉酒醒日复日，网上网下年复年。
//                  但愿老死电脑间，不愿鞠躬老板前；
//                  奔驰宝马贵者趣，公交自行程序员。
//                  别人笑我忒疯癫，我笑自己命太贱。

package com.hpush.app;

import android.app.Application;

import com.chopping.net.TaskHelper;
import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp.StethoInterceptor;
import com.hpush.R;
import com.squareup.okhttp.OkHttpClient;


/**
 * The app-object of the project.
 *
 * @author Xinyue Zhao
 */
public final class App extends Application {
    /**
     * Singleton.
     */
    public static App Instance;
	@Override
	public void onCreate() {
		super.onCreate();
        Instance = this;
		TaskHelper.init(getApplicationContext());

		boolean isDev = getResources().getBoolean(R.bool.dev);
		if(isDev) {
			Stetho.initialize(Stetho.newInitializerBuilder(this).enableDumpapp(Stetho.defaultDumperPluginsProvider(this)).enableWebKitInspector(
					Stetho.defaultInspectorModulesProvider(this)).build());
			OkHttpClient client = new OkHttpClient();
			client.networkInterceptors().add(new StethoInterceptor());
		}
	}
}
