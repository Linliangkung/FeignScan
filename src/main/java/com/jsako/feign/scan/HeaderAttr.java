package com.jsako.feign.scan;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Date 2019/5/28
 * @Author LLJ
 * @Description Header注解属性实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeaderAttr {
    private String key;

    private String value;
}
