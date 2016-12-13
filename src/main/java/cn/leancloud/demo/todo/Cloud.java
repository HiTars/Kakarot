package cn.leancloud.demo.todo;

import cn.leancloud.EngineFunction;
import cn.leancloud.EngineFunctionParam;
import com.avos.avoscloud.*;
import com.google.common.collect.ArrayListMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Cloud {

  private static final Logger logger = LogManager.getLogger(Cloud.class);

  private static final Gson gson = newGson();

  private static final HashMap<String, AVObject> stakeMap = new HashMap<>();

  private static final ArrayListMultimap<String, AVObject> cookieMap = ArrayListMultimap.create();

  static {
    // Load StakeMap
    AVQuery<AVObject> termQuery = new AVQuery<>("StakeTerm");
    termQuery.whereNotEqualTo("type", 0);
    List<AVObject> terms = new ArrayList<>();
    try {
      terms = termQuery.find();
    } catch (AVException e) {
      logger.warn("AVException", e);
    }
    for (AVObject term : terms) {
      stakeMap.put(term.getString("name"), term);
    }

    // Load cookies map
    AVQuery<AVObject> cookieQuery = new AVQuery<>("Cookie");
    cookieQuery.whereNotEqualTo("domain", "");
    List<AVObject> cookies = new ArrayList<>();
    try {
      cookies = cookieQuery.find();
    } catch (AVException e) {
      logger.warn("AVException", e);
    }
    for (AVObject cookie : cookies) {
      cookieMap.put(cookie.getString("domain"), cookie);
    }
  }

  @EngineFunction("hello")
  public static String hello() {
    logger.info("Hello world");
    return "Hello world!";
  }

  @EngineFunction("list")
  public static List<Todo> getNotes(@EngineFunctionParam("offset") int offset) throws AVException {
    AVQuery<Todo> query = AVObject.getQuery(Todo.class);
    query.orderByDescending("createdAt");
    query.include("createdAt");
    query.skip(offset);
    try {
      return query.find();
    } catch (AVException e) {
      if (e.getCode() == 101) {
        // 该错误的信息为：{ code: 101, message: 'Class or object doesn\'t exists.' }，说明 Todo 数据表还未创建，所以返回空的
        // Todo 列表。
        // 具体的错误代码详见：https://leancloud.cn/docs/error_code.html
        return new ArrayList<>();
      }
      throw e;
    }
  }

  @EngineFunction("stakeInfo")
  public static void getStakeInfo() throws Exception {
    OkHttpClient client = new OkHttpClient().newBuilder()
            .cookieJar(new CookieJar() {

              @Override
              public void saveFromResponse(HttpUrl httpUrl, List<Cookie> list) {
                List<AVObject> cookies = cookieMap.get(httpUrl.host());
                for (Cookie newCookie : list) {
                  boolean found = false;
                  for (int i = 0; i < cookies.size(); i++) {
                    AVObject oldCookie = cookies.get(i);
                    if (oldCookie.getString("name").equals(newCookie.name())) {
                      if (!oldCookie.getString("value").equals(newCookie.value())) {
                        oldCookie.put("value", newCookie.value());
                        oldCookie.put("raw", gson.toJson(newCookie));
                        try {
                          oldCookie.save();
                        } catch (AVException e) {
                          logger.warn("AVException", e);
                        }
                      }
                      found = true;
                      break;
                    }
                  }
                  if (!found) {
                    AVObject cookie = new AVObject("Cookie");
                    cookie.put("domain", newCookie.domain());
                    cookie.put("name", newCookie.name());
                    cookie.put("value", newCookie.value());
                    cookie.put("raw", gson.toJson(newCookie));
                    cookieMap.put(httpUrl.host(), cookie);
                    try {
                      cookie.save();
                    } catch (AVException e) {
                      logger.warn("AVException", e);
                    }
                  }
                }
                logger.info("save: " + list);
              }

              @Override
              public List<Cookie> loadForRequest(HttpUrl httpUrl) {
                List<AVObject> cookies = cookieMap.get(httpUrl.host());
                List<Cookie> ret = new ArrayList<>();
                for (AVObject cookie : cookies) {
                  ret.add(gson.fromJson(cookie.getString("raw"), Cookie.class));
                }
                logger.info("load: " + ret);
                return ret;
              }
            })
            .build();

    login(client);

    RequestBody formBody = new FormBody.Builder()
            .add("team_id", "94845d3f4db24f21971ef905c10ca46e")
            .add("Version", "V1.0.0")
            .build();

    Request request = new Request.Builder()
            .header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
            .url("http://114.215.85.38/appInterface/F16")
            .post(formBody)
            .build();

    Response response = client.newCall(request).execute();
    _StakeInfo stakeInfo = gson.fromJson(response.body().string(), _StakeInfo.class);
    logger.info(gson.toJson(stakeInfo));
    if ("SUCCESS".equals(stakeInfo.getStatus())) {
      String pushMsg = "";
      for (_TeamInfo teamInfo: stakeInfo.getResult().get(0).getTeam_info()) {
        String name = teamInfo.getTerm_name();
        AVObject term;
        if (stakeMap.containsKey(name)) {
          term = stakeMap.get(name);
          if (!term.getString("status").equals(teamInfo.getTerm_status())) {
            term.put("status", teamInfo.getTerm_status());
            pushMsg += (StringUtils.isBlank(pushMsg) ? "" : "\n") + teamInfo.getStatusStr() + ": " + name;
          }
        } else {
          term = new AVObject("StakeTerm");
          term.put("id", teamInfo.getTerm_id());
          term.put("name", teamInfo.getTerm_name());
          term.put("status", teamInfo.getTerm_status());
          term.put("time", teamInfo.getOrder_time());
          term.put("type", teamInfo.getTerm_type());
          stakeMap.put(name, term);
        }
        try {
          term.save();
        } catch (AVException e) {
          logger.warn("AVException", e);
        }
      }
      if (!StringUtils.isBlank(pushMsg)) {
        logger.info(pushMsg);
        AVPush push = new AVPush();
        JSONObject object = new JSONObject();
        object.put("alert", pushMsg);
        object.put("sound", "default");
        push.setPushToIOS(true);
        push.setProductionMode(false);
        push.setData(object);
        push.sendInBackground(new SendCallback() {
          @Override
          public void done(AVException e) {
            if (e == null) {
              logger.info("Push Success");
            } else {
              // something wrong.
            }
          }
        });
      }
    } else {
      if ("11".equals(stakeInfo.getErrNum())) {
        logger.info(gson.toJson(stakeInfo));
      }
    }
  }

  private static void login(OkHttpClient client) throws Exception {
    RequestBody formBody = new FormBody.Builder()
            .add("Version", "V1.0.0")
            .add("ac_name", "18511760027")
            .add("password", "0127zhumeng1989")
            .build();

    Request request = new Request.Builder()
            .header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
            .header("User-Agent", "SmartSocket/2 CFNetwork/758.5.3 Darwin/15.6.0")
            .url("http://114.215.85.38/appInterface/F40")
            .post(formBody)
            .build();
    client.newCall(request).execute();
  }

  @Data
  private class _StakeInfo {
    String ErrNum;
    String Status;
    String EventStatus;
    String Version;
    List<_Result> result;
  }

  @Data
  private class _Result {
    String location;
    String time_elec_count;
    String team_info_count;
    List<_TeamInfo> team_info;
  }

  @Data
  private class _TeamInfo {
    Integer term_id;
    String term_name;
    String term_status;
    Long order_time;
    Integer term_type;
    //String ac_name;
    public String getStatusStr() {
      if ("02".equals(term_status)) {
        return "空闲";
      } else if ("04".equals(term_status)) {
        return "充电";
      }
      return term_status;
    }
  }

  private static Gson newGson() {
    GsonBuilder builder = new GsonBuilder();
    builder.setVersion(0.1);
    return builder.create();
  }

  public static void main(String[] argsf) throws Exception {
    getStakeInfo();
  }

}
