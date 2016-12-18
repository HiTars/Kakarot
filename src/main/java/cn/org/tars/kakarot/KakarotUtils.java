package cn.org.tars.kakarot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;

/**
 * KakarotUtils
 *
 * @author zhumeng
 * @since 2016/12/14
 */
public class KakarotUtils {

    public static final Gson gson = newGson();

    public static final OkHttpClient client = newClient();

    private static Gson newGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.setVersion(0.1);
        return builder.create();
    }

    private static OkHttpClient newClient() {
        return new OkHttpClient().newBuilder()
                .cookieJar(new CloudCookieJar())
                .build();
    }

}