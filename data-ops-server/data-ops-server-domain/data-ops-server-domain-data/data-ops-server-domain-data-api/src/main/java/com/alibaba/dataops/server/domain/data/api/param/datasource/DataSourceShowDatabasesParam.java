package com.alibaba.dataops.server.domain.data.api.param.datasource;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 展示数据库信息
 * @author Jiaju Zhuang
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString
public class DataSourceShowDatabasesParam {
    /**
     * 对应数据库存储的来源id
     */
    @NotNull
    private Long dataSourceId;
}
