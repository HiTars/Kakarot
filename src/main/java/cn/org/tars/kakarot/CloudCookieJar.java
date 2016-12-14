package cn.org.tars.kakarot;

import cn.org.tars.kakarot.data.CookieTerm;
import com.avos.avoscloud.AVException;
import com.google.common.collect.ArrayListMultimap;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

import java.util.ArrayList;
import java.util.List;

/**
 * CloudCookieJar
 *
 * @author zhumeng
 * @since 2016/12/14
 */
@Slf4j
public class CloudCookieJar implements CookieJar {

    private static final ArrayListMultimap<String, CookieTerm> cookieMap = ArrayListMultimap.create();

    static {
        // Load CookieMap
        for (CookieTerm cookie : CookieTerm.getAllCookies()) {
            cookieMap.put(cookie.getString("domain"), cookie);
        }
    }

    @Override
    public void saveFromResponse(HttpUrl httpUrl, List<Cookie> list) {
        List<CookieTerm> cookies = cookieMap.get(httpUrl.host());
        for (Cookie newCookie : list) {
            boolean found = false;
            for (CookieTerm oldCookie : cookies) {
                if (oldCookie.getString("name").equals(newCookie.name())) {
                    oldCookie.updateValue(newCookie);
                    found = true;
                    break;
                }
            }
            if (!found) {
                CookieTerm cookie = new CookieTerm(newCookie);
                try {
                    cookie.save();
                    cookieMap.put(httpUrl.host(), cookie);
                } catch (AVException e) {
                    log.warn("AVException", e);
                }
            }
        }
        log.info("save: " + list);
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl httpUrl) {
        List<CookieTerm> cookies = cookieMap.get(httpUrl.host());
        List<Cookie> ret = new ArrayList<>();
        for (CookieTerm cookie : cookies) {
            ret.add(cookie.getCookie());
        }
        log.info("load: " + ret);
        return ret;
    }
}