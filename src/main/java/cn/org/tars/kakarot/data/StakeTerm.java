package cn.org.tars.kakarot.data;

import com.avos.avoscloud.AVClassName;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * StakeTerm
 *
 * @author zhumeng
 * @since 2016/12/14
 */
@Slf4j
@AVClassName("StakeTerm")
public class StakeTerm extends AVObject {

    public static List<StakeTerm> getAllStakes() {
        AVQuery<StakeTerm> query = AVObject.getQuery(StakeTerm.class);
        List<StakeTerm> stakes = new ArrayList<>();
        try {
            stakes = query.find();
        } catch (AVException e) {
            log.warn("AVException", e);
        }
        return stakes;
    }

}