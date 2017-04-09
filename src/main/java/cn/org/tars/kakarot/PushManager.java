/**
 * @(#)PushManager.java, 2016/12/18.
 * Copyright (c) 2016 Yodao, Inc. All rights reserved.
 * YODAO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package cn.org.tars.kakarot;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * PushManager
 *
 * @author zhumeng
 * @since 2016/12/18
 */
public class PushManager {

    private static final Logger logger = LogManager.getLogger(PushManager.class);

    public static boolean checkInstallationChannel(String channel) {
        AVQuery<AVObject> query = new AVQuery<>("_Installation");
        query.whereEqualTo("channels", channel);
        List<AVObject> insts = new ArrayList<>();
        try {
            insts = query.find();
        } catch (AVException e) {
            logger.warn("AVException", e);
        }
        logger.info("checkInstallationChannel: " + channel + ", " + insts.size());
        return insts.size() > 0;
    }

}