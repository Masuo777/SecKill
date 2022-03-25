package com.marshio.redisdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.marshio.redisdemo.pojo.User;
import org.springframework.stereotype.Repository;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author masuo
 * @since 2022-03-11
 */
@Repository
public interface UserMapper extends BaseMapper<User> {

}
