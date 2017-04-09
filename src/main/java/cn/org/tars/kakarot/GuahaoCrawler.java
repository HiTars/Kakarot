/**
 * @(#)GuahaoCrawler.java, 2017/04/09.
 * Copyright (c) 2016 Yodao, Inc. All rights reserved.
 * YODAO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package cn.org.tars.kakarot;

import cn.org.tars.kakarot.data.GuahaoDate;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import lombok.Data;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;

import static cn.org.tars.kakarot.KakarotUtils.client;

/**
 * GuahaoCrawler
 *
 * @author zhumeng
 * @since 2017/04/09
 */
public class GuahaoCrawler {

    private static final Logger logger = LogManager.getLogger(GuahaoCrawler.class);

    private static final HashMap<String, AVObject> guahaoMap = new HashMap<>();

    static {
        // Load GuahaoMap
        for (AVObject term : GuahaoDate.getGuahaoDates()) {
            guahaoMap.put(term.getString("date"), term);
        }
    }

    public static String crawl() throws Exception {
        Request request = new Request.Builder()
                .url("http://wechat.benmu-health.com/mobile/wx/product/list?hosCode=H1136112&" +
                        "firstDeptCode=m_FCK_bd926ff4&secondDeptCode=1012&_=" + System.currentTimeMillis())
                .build();

        Response response = client.newCall(request).execute();
        _GuahaoInfo guahaoInfo = KakarotUtils.gson.fromJson(response.body().string(), _GuahaoInfo.class);
        logger.info(KakarotUtils.gson.toJson(guahaoInfo));
        String pushMsg = "";
        if (guahaoInfo.getResCode() == 0) {
            for (_Item item : guahaoInfo.getData().getDateList().get(0)) {
                String date = item.getDate();
                AVObject guahao;
                if (guahaoMap.containsKey(date)) {
                    guahao = guahaoMap.get(date);
                    if (!guahao.getString("status").equals(item.getStatus())) {
                        guahao.put("status", item.getStatus());
                        pushMsg += (StringUtils.isBlank(pushMsg) ? "" : "\n") + item.getStatus() + ": " + date;
                    }
                } else {
                    guahao = new AVObject("GuahaoDate");
                    guahao.put("date", item.getDate());
                    guahao.put("status", item.getStatus());
                    guahaoMap.put(date, guahao);
                    pushMsg += (StringUtils.isBlank(pushMsg) ? "" : "\n") + item.getStatus() + ": " + date;
                }
                try {
                    guahao.save();
                } catch (AVException e) {
                    logger.warn("AVException", e);
                }
            }
        }
        return pushMsg;
    }

    @Data
    private class _GuahaoInfo {
        int resCode;
        String msg;
        _Data data;
    }

    @Data
    private class _Data {
        String today;
        List<List<_Item>> dateList;
    }

    @Data
    private class _Item {
        String date;
        String status;
        long waitOpenTime;
        long openTimestamp;
    }

}