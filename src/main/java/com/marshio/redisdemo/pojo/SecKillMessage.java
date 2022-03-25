package com.marshio.redisdemo.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author masuo
 * @data 24/3/2022 上午9:51
 * @Description TODO
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SecKillMessage {

    private User user;

    private Long goodsId;
}
