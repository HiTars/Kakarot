package cn.org.tars.kakarot.data;

import cn.org.tars.kakarot.KakarotUtils;
import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import okhttp3.Cookie;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * CookieTerm
 *
 * @author zhumeng
 * @since 2016/12/14
 */
@AVClassName("Cookie")
public class CookieTerm extends AVObject {

    private static final Logger logger = LogManager.getLogger(CookieTerm.class);

    public static List<CookieTerm> getAllCookies() {
        AVQuery<CookieTerm> query = AVObject.getQuery(CookieTerm.class);
        List<CookieTerm> cookies = new ArrayList<>();
        try {
            cookies = query.find();
        } catch (AVException e) {
            logger.warn("AVException", e);
        }
        return cookies;
    }

    public Cookie getCookie() {
        return KakarotUtils.gson.fromJson(getString("raw"), Cookie.class);
    }

    public boolean updateValue(Cookie cookie) {
        if (!getString("value").equals(cookie.value())) {
            put("value", cookie.value());
            put("raw", KakarotUtils.gson.toJson(cookie));
            try {
                save();
            } catch (AVException e) {
                logger.warn("AVException", e);
            }
            return true;
        } else {
            return false;
        }
    }

}