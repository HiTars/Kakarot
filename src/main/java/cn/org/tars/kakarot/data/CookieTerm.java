package cn.org.tars.kakarot.data;

import cn.org.tars.kakarot.KakarotUtils;
import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Cookie;

import java.util.ArrayList;
import java.util.List;

/**
 * CookieTerm
 *
 * @author zhumeng
 * @since 2016/12/14
 */
@Slf4j
@AVClassName("Cookie")
public class CookieTerm extends AVObject {

    public static List<CookieTerm> getAllCookies() {
        AVQuery<CookieTerm> query = AVObject.getQuery(CookieTerm.class);
        List<CookieTerm> cookies = new ArrayList<>();
        try {
            cookies = query.find();
        } catch (AVException e) {
            log.warn("AVException", e);
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
                log.warn("AVException", e);
            }
            return true;
        } else {
            return false;
        }
    }

}