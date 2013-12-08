package co.bouk.proxynow;

import android.content.Context;
import android.content.Intent;
import android.text.SpannedString;

import static de.robv.android.xposed.XposedHelpers.*;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class InterceptGoogleNow implements IXposedHookLoadPackage {

    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if(!lpparam.packageName.equals("com.google.android.googlequicksearchbox")) {
            return;
        }

        Class<?> Event = findClass("com.google.android.search.core.state.VelvetEventBus.Event", lpparam.classLoader);
        findAndHookMethod("com.google.android.velvet.presenter.VelvetPresenter", lpparam.classLoader, "onStateChanged", Event, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Context context = (Context)getObjectField(param.thisObject, "mAppContext");
                if ((Boolean)callMethod(param.args[0], "hasQueryChanged")) {
                    Object queryState = getObjectField(param.thisObject, "mQueryState");
                    if (queryState != null) {
                        Object query = callMethod(queryState, "takeNewlyCommittedWebQuery");
                        if (query != null) {
                            String queryString = (String)callMethod(query, "getQueryString");
                            queryStringReceived(context, queryString, (Boolean)callMethod(query, "isVoiceSearch"));
                        }
                    }
                }
            }
        });
    }

    private void queryStringReceived(Context context, String query, boolean isVoiceSearch) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.setAction(ProxyNow.INTENT_QUERY);
        intent.putExtra(ProxyNow.DATA_QUERY, query);
        intent.putExtra(ProxyNow.DATA_IS_VOICE_SEARCH, isVoiceSearch);

        context.sendBroadcast(intent);
    }
}
