package ouc.lm.coolweather.util;

/**
 * Created by Lavida on 2016-04-14.
 */
public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);
}
