/**
 * @(#)GuahaoDate.java, 2017/04/09.
 * Copyright (c) 2016 Yodao, Inc. All rights reserved.
 * YODAO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package cn.org.tars.kakarot.data;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * GuahaoDate, NOTE: 需要在AppInitListener类中注册
 *
 * @author zhumeng
 * @since 2017/04/09
 */
@AVClassName("GuahaoDate")
public class GuahaoDate extends AVObject {

    private static final Logger logger = LogManager.getLogger(GuahaoDate.class);

    public static List<GuahaoDate> getGuahaoDates() {
        AVQuery<GuahaoDate> query = AVObject.getQuery(GuahaoDate.class);
        List<GuahaoDate> guahaoDates = new ArrayList<>();
        try {
            guahaoDates = query.find();
        } catch (AVException e) {
            logger.warn("AVException", e);
        }
        return guahaoDates;
    }

}