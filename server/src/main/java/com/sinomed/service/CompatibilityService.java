package com.sinomed.service;

import com.sinomed.vo.CompatibilityResultView;

import java.util.List;

/**
 * 配伍审方（十八反/十九畏）：静态经典规则比对，提示不拦截——开方是否照用由医师判断。
 */
public interface CompatibilityService {

    /**
     * 按经典十八反/十九畏规则比对药材名。药材名为自由文本，含别名包含关系即命中
     * （如「法半夏」命中「半夏」）。空白名忽略；全部为空抛「药材名单不能为空」。
     */
    CompatibilityResultView check(List<String> herbNames);
}
