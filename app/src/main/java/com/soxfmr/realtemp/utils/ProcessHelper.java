package com.soxfmr.realtemp.utils;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

/**
 * Created by Soxfmr@gmail.com on 2016/4/9.
 */
public class ProcessHelper {

    /**
     * Avoid that the system create a new task for the activity
     * @param context
     * @return
     */
    public static boolean needStartApp(Context context) {
        final ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningTaskInfo> tasks = manager.getRunningTasks(1024);
        if(!tasks.isEmpty()) {
            final String packageName = context.getPackageName();
            for(ActivityManager.RunningTaskInfo taskInfo : tasks) {
                if(packageName.equals(taskInfo.baseActivity.getPackageName())) {
                    return taskInfo.numActivities==1;
                }
            }

        }
        return true;
    }

}
