package cn.org.tars.kakarot;

import cn.leancloud.EngineFunction;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVPush;
import com.avos.avoscloud.SendCallback;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class Cloud {

    private static final Logger logger = LogManager.getLogger(Cloud.class);

    @EngineFunction("crawlInfo")
    public static void crawlInfo() throws Exception {
        String stakeChannel = "stakeInfo";
        String guahaoChannel = "guahaoInfo";
        if (PushManager.checkInstallationChannel(stakeChannel)) {
            String pushMsg = StakeCrawler.crawl();
            sendMessage(pushMsg, stakeChannel);
        } else if (PushManager.checkInstallationChannel(guahaoChannel)) {
            String pushMsg = GuahaoCrawler.crawl();
            sendMessage(pushMsg, guahaoChannel);
        }
    }

    private static void sendMessage(String pushMsg, String channel) {
        if (!StringUtils.isBlank(pushMsg)) {
            logger.info(pushMsg);
            AVPush push = new AVPush();
            JSONObject object = new JSONObject();
            object.put("alert", pushMsg);
            object.put("sound", "default");
            push.setChannel(channel);
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
    }

}
