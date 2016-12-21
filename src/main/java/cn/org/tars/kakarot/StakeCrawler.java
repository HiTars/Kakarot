package cn.org.tars.kakarot;

import cn.org.tars.kakarot.data.StakeTerm;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import lombok.Data;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;

/**
 * StakeCrawler
 *
 * @author zhumeng
 * @since 2016/12/18
 */
public class StakeCrawler {

    private static final Logger logger = LogManager.getLogger(StakeCrawler.class);

    private static final HashMap<String, AVObject> stakeMap = new HashMap<>();

    static {
        // Load StakeMap
        for (AVObject term : StakeTerm.getAllStakes()) {
            stakeMap.put(term.getString("name"), term);
        }
    }

    public static String crawl() throws Exception {
        login(KakarotUtils.client);

        RequestBody formBody = new FormBody.Builder()
                .add("team_id", "94845d3f4db24f21971ef905c10ca46e")
                .add("Version", "V1.0.0")
                .build();

        Request request = new Request.Builder()
                .header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
                .url("http://114.215.85.38/appInterface/F16")
                .post(formBody)
                .build();

        Response response = KakarotUtils.client.newCall(request).execute();
        _StakeInfo stakeInfo = KakarotUtils.gson.fromJson(response.body().string(), _StakeInfo.class);
        logger.info(KakarotUtils.gson.toJson(stakeInfo));
        String pushMsg = "";
        if ("SUCCESS".equals(stakeInfo.getStatus())) {
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
                    logger.warn("AVException", e);
                }
            }
        }
        return pushMsg;
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
        client.newCall(request).execute().close();
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