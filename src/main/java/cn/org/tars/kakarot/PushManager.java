/**
 * @(#)PushManager.java, 2016/12/18.
 * Copyright (c) 2016 Yodao, Inc. All rights reserved.
 * YODAO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package cn.org.tars.kakarot;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * PushManager
 *
 * @author zhumeng
 * @since 2016/12/18
 */
@Slf4j
public class PushManager {

    public static boolean checkInstallationChannel(String channel) {
        AVQuery<AVObject> query = new AVQuery<>("_Installation");
        query.whereEqualTo("channels", channel);
        List<AVObject> insts = new ArrayList<>();
        try {
            insts = query.find();
        } catch (AVException e) {
            log.warn("AVException", e);
        }
        log.info("checkInstallationChannel: " + insts.size());
        return insts.size() > 0;
    }

}