package com.sinomed.service;

import com.sinomed.vo.AllergyResultView;

import java.util.List;

/**
 * 过敏审方：按顾客过敏史（customer_histories type=0）比对药味，提示不拦截。
 */
public interface AllergyService {

    /**
     * 比对：过敏史原文包含药名（≥2 字）即命中；顾客不存在或药味名单为空报 400
     */
    AllergyResultView check(Long customerId, List<String> herbNames);
}
