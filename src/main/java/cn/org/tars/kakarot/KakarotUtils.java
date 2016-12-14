package cn.org.tars.kakarot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * KakarotUtils
 *
 * @author zhumeng
 * @since 2016/12/14
 */
public class KakarotUtils {

    public static final Gson gson = newGson();

    private static Gson newGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.setVersion(0.1);
        return builder.create();
    }

}