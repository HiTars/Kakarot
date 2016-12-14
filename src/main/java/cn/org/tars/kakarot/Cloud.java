package cn.org.tars.kakarot;

import cn.leancloud.EngineFunction;
import cn.leancloud.EngineFunctionParam;
import cn.org.tars.kakarot.data.StakeTerm;
import cn.org.tars.kakarot.data.Todo;
import com.avos.avoscloud.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
public class Cloud {

    private static final CookieJar cookieJar = new CloudCookieJar();

    private static final HashMap<String, AVObject> stakeMap = new HashMap<>();

    static {
        // Load StakeMap
        for (AVObject term : StakeTerm.getAllStakes()) {
            stakeMap.put(term.getString("name"), term);
        }
    }

    @EngineFunction("hello")
    public static String hello() {
        log.info("Hello world");
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
                .cookieJar(cookieJar)
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
        _StakeInfo stakeInfo = KakarotUtils.gson.fromJson(response.body().string(), _StakeInfo.class);
        log.info(KakarotUtils.gson.toJson(stakeInfo));
        if ("SUCCESS".equals(stakeInfo.getStatus())) {
            String pushMsg = "";
            for (_TeamInfo teamInfo : stakeInfo.getResult().get(0).getTeam_info()) {
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
                    log.warn("AVException", e);
                }
            }
            if (!StringUtils.isBlank(pushMsg)) {
                log.info(pushMsg);
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
                            log.info("Push Success");
                        } else {
                            // something wrong.
                        }
                    }
                });
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

}
