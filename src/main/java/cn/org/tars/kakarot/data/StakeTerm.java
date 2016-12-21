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
 * StakeTerm
 *
 * @author zhumeng
 * @since 2016/12/14
 */
@AVClassName("StakeTerm")
public class StakeTerm extends AVObject {

    private static final Logger logger = LogManager.getLogger(StakeTerm.class);

    public static List<StakeTerm> getAllStakes() {
        AVQuery<StakeTerm> query = AVObject.getQuery(StakeTerm.class);
        List<StakeTerm> stakes = new ArrayList<>();
        try {
            stakes = query.find();
        } catch (AVException e) {
            logger.warn("AVException", e);
        }
        return stakes;
    }

}