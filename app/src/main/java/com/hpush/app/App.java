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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import android.app.Application;

import com.chopping.net.TaskHelper;
import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp.StethoInterceptor;
import com.hpush.R;
import com.squareup.okhttp.OkHttpClient;

import cn.bmob.v3.Bmob;


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


    /**
     * Times that the AdMob shown before, it under App-process domain. When process killed, it recounts
     */
    private int mAdsShownTimes;


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

        Properties prop = new Properties();
        InputStream input = null;
        try {
			/*From "resources".*/
            input = getClassLoader().getResourceAsStream("key.properties");
            if (input != null) {
                // load a properties file
                prop.load(input);
                Bmob.initialize(this, prop.getProperty("bmobkey"));
            }
        } catch (IOException ex) {
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
	}


    /**
     * @return How much times that the AdMob has shown before, it under App-process domain. When process killed, it
     * recounts.
     */
    public int getAdsShownTimes() {
        return mAdsShownTimes;
    }

    /**
     * Set how much times that the AdMob has shown before, it under App-process domain.
     *
     * @param adsShownTimes
     * 		Times that AdMob has shown.
     */
    public void setAdsShownTimes(int adsShownTimes) {
        mAdsShownTimes = adsShownTimes;
    }
}
