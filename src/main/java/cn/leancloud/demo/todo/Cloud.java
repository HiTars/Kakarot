package cn.leancloud.demo.todo;

import java.util.ArrayList;
import java.util.List;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;

import cn.leancloud.EngineFunction;
import cn.leancloud.EngineFunctionParam;
import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Cloud {

  private static final Logger logger = LogManager.getLogger(Cloud.class);

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
  public static String getStakeInfo() throws Exception {
    RequestBody formBody = new FormBody.Builder()
            .add("team_id", "94845d3f4db24f21971ef905c10ca46e")
            .add("Version", "V1.0.0")
            .build();

    OkHttpClient client = new OkHttpClient().newBuilder()
            .cookieJar(new CookieJar() {
              @Override
              public void saveFromResponse(HttpUrl httpUrl, List<Cookie> list) {}

              @Override
              public List<Cookie> loadForRequest(HttpUrl httpUrl) {
                final ArrayList<Cookie> oneCookie = new ArrayList<>(1);
                oneCookie.add(new Cookie.Builder()
                        .domain("114.215.85.38")
                        .path("/")
                        .name("JSESSIONID")
                        .value("842C1EA3DE86A1D20E49617E7FD8FA38")
                        .httpOnly()
                        .build());
                return oneCookie;
              }
            })
            .build();

    Request request = new Request.Builder()
            .header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
            .url("http://114.215.85.38/appInterface/F16")
            .post(formBody)
            .build();

    Response response = client.newCall(request).execute();
    return response.body().string();
  }

  public static void main(String[] argsf) throws Exception {
    System.out.println(getStakeInfo());
  }

}
