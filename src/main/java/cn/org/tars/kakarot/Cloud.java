package cn.org.tars.kakarot;

import cn.leancloud.EngineFunction;
import cn.leancloud.EngineFunctionParam;
import cn.org.tars.kakarot.data.Todo;
import com.avos.avoscloud.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Cloud {

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
        String channel = "stakeInfo";
        if (PushManager.checkInstallationChannel(channel)) {
            String pushMsg = StakeCrawler.crawl();
            if (!StringUtils.isBlank(pushMsg)) {
                log.info(pushMsg);
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
                            log.info("Push Success");
                        } else {
                            // something wrong.
                        }
                    }
                });
            }
        }
    }

}
