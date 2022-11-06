package com.alibaba.dataops.server.domain.core.core.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.dataops.server.domain.core.api.model.DataSourceDTO;
import com.alibaba.dataops.server.domain.core.api.param.ConsoleConnectParam;
import com.alibaba.dataops.server.domain.core.api.param.DataSourceExecuteParam;
import com.alibaba.dataops.server.domain.core.api.param.DataSourceManageCreateParam;
import com.alibaba.dataops.server.domain.core.api.param.DataSourcePageQueryParam;
import com.alibaba.dataops.server.domain.core.api.param.DataSourceSelector;
import com.alibaba.dataops.server.domain.core.api.param.DataSourceTestParam;
import com.alibaba.dataops.server.domain.core.api.param.DataSourceUpdateParam;
import com.alibaba.dataops.server.domain.core.api.service.DataSourceCoreService;
import com.alibaba.dataops.server.domain.core.core.converter.DataSourceCoreConverter;
import com.alibaba.dataops.server.domain.core.repository.entity.DataSourceDO;
import com.alibaba.dataops.server.domain.core.repository.mapper.DataSourceMapper;
import com.alibaba.dataops.server.domain.data.api.model.DataSourceConnectDTO;
import com.alibaba.dataops.server.domain.data.api.model.DatabaseDTO;
import com.alibaba.dataops.server.domain.data.api.model.ExecuteResultDTO;
import com.alibaba.dataops.server.domain.data.api.model.SqlDTO;
import com.alibaba.dataops.server.domain.data.api.model.TableDTO;
import com.alibaba.dataops.server.domain.data.api.param.console.ConsoleCloseParam;
import com.alibaba.dataops.server.domain.data.api.param.console.ConsoleCreateParam;
import com.alibaba.dataops.server.domain.data.api.param.database.DatabaseQueryAllParam;
import com.alibaba.dataops.server.domain.data.api.param.datasource.DataSourceCloseParam;
import com.alibaba.dataops.server.domain.data.api.param.datasource.DataSourceCreateParam;
import com.alibaba.dataops.server.domain.data.api.param.sql.SqlAnalyseParam;
import com.alibaba.dataops.server.domain.data.api.param.table.TablePageQueryParam;
import com.alibaba.dataops.server.domain.data.api.param.table.TableQueryParam;
import com.alibaba.dataops.server.domain.data.api.param.table.TableSelector;
import com.alibaba.dataops.server.domain.data.api.param.template.TemplateExecuteParam;
import com.alibaba.dataops.server.domain.data.api.service.ConsoleDataService;
import com.alibaba.dataops.server.domain.data.api.service.DataSourceDataService;
import com.alibaba.dataops.server.domain.data.api.service.DatabaseDataService;
import com.alibaba.dataops.server.domain.data.api.service.JdbcTemplateDataService;
import com.alibaba.dataops.server.domain.data.api.service.SqlDataService;
import com.alibaba.dataops.server.domain.data.api.service.TableDataService;
import com.alibaba.dataops.server.tools.base.excption.BusinessException;
import com.alibaba.dataops.server.tools.base.excption.DatasourceErrorEnum;
import com.alibaba.dataops.server.tools.base.wrapper.result.ActionResult;
import com.alibaba.dataops.server.tools.base.wrapper.result.DataResult;
import com.alibaba.dataops.server.tools.base.wrapper.result.ListResult;
import com.alibaba.dataops.server.tools.base.wrapper.result.PageResult;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author moji
 * @version DataSourceCoreServiceImpl.java, v 0.1 2022年09月23日 15:51 moji Exp $
 * @date 2022/09/23
 */
@Service
public class DataSourceCoreServiceImpl implements DataSourceCoreService {

    @Autowired
    private DataSourceMapper dataSourceMapper;

    @Autowired
    private DataSourceDataService dataSourceDataService;

    @Autowired
    private ConsoleDataService consoleDataService;

    @Autowired
    private JdbcTemplateDataService jdbcTemplateDataService;

    @Autowired
    private SqlDataService sqlDataService;

    @Autowired
    private DatabaseDataService databaseDataService;

    @Autowired
    private TableDataService tableDataService;

    @Autowired
    private DataSourceCoreConverter dataSourceCoreConverter;

    @Override
    public DataResult<Long> create(DataSourceManageCreateParam param) {
        DataSourceDO dataSourceDO = dataSourceCoreConverter.param2do(param);
        dataSourceDO.setGmtCreate(LocalDateTime.now());
        dataSourceDO.setGmtModified(LocalDateTime.now());
        dataSourceMapper.insert(dataSourceDO);
        return DataResult.of(dataSourceDO.getId());
    }

    @Override
    public ActionResult update(DataSourceUpdateParam param) {
        DataSourceDO dataSourceDO = dataSourceCoreConverter.param2do(param);
        dataSourceDO.setGmtModified(LocalDateTime.now());
        dataSourceMapper.updateById(dataSourceDO);
        return ActionResult.isSuccess();
    }

    @Override
    public ActionResult delete(Long id) {
        dataSourceMapper.deleteById(id);
        return ActionResult.isSuccess();
    }

    @Override
    public DataResult<DataSourceDTO> queryById(Long id) {
        DataSourceDO dataSourceDO = dataSourceMapper.selectById(id);
        return DataResult.of(dataSourceCoreConverter.do2dto(dataSourceDO));
    }

    @Override
    public DataResult<Long> copyById(Long id) {
        DataSourceDO dataSourceDO = dataSourceMapper.selectById(id);
        dataSourceDO.setId(null);
        String alias = dataSourceDO.getAlias() + "Copy";
        dataSourceDO.setAlias(alias);
        dataSourceDO.setGmtCreate(LocalDateTime.now());
        dataSourceDO.setGmtModified(LocalDateTime.now());
        dataSourceMapper.insert(dataSourceDO);
        return DataResult.of(dataSourceDO.getId());
    }

    @Override
    public PageResult<DataSourceDTO> queryPage(DataSourcePageQueryParam param, DataSourceSelector selector) {
        QueryWrapper<DataSourceDO> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(param.getSearchKey())) {
            queryWrapper.like("alias", param.getSearchKey());
        }
        Integer start = param.getPageNo();
        Integer offset = param.getPageSize();
        Page<DataSourceDO> page = new Page<>(start, offset);
        IPage<DataSourceDO> iPage = dataSourceMapper.selectPage(page, queryWrapper);
        List<DataSourceDTO> dataSourceDTOS = dataSourceCoreConverter.do2dto(iPage.getRecords());
        return PageResult.of(dataSourceDTOS, iPage.getTotal(), param);
    }

    @Override
    public ActionResult test(DataSourceTestParam param) {
        com.alibaba.dataops.server.domain.data.api.param.datasource.DataSourceTestParam dataSourceTestParam
            = dataSourceCoreConverter.param2param(param);
        DataSourceConnectDTO dataSourceConnect = dataSourceDataService.test(dataSourceTestParam).getData();
        if (BooleanUtils.isNotTrue(dataSourceConnect.getSuccess())) {
            throw new BusinessException(DatasourceErrorEnum.DATASOURCE_TEST_ERROR);
        }
        // TODO 关闭连接
        return ActionResult.isSuccess();
    }

    @Override
    public ListResult<DatabaseDTO> attach(Long id) {
        DataSourceDO dataSourceDO = dataSourceMapper.selectById(id);
        DataSourceCreateParam param = dataSourceCoreConverter.do2param(dataSourceDO);
        DataSourceConnectDTO dataSourceConnect = dataSourceDataService.create(param).getData();
        if (BooleanUtils.isNotTrue(dataSourceConnect.getSuccess())) {
            throw new BusinessException(DatasourceErrorEnum.DATASOURCE_CONNECT_ERROR);
        }

        // 查询database
        DatabaseQueryAllParam queryAllParam = new DatabaseQueryAllParam();
        queryAllParam.setDataSourceId(id);
        ListResult<DatabaseDTO> databaseDTOS = databaseDataService.queryAll(queryAllParam);
        return databaseDTOS;
    }

    @Override
    public ActionResult close(Long id) {
        DataSourceCloseParam closeParam = new DataSourceCloseParam();
        closeParam.setDataSourceId(id);
        return dataSourceDataService.close(closeParam);
    }

    @Override
    public ActionResult createConsole(ConsoleConnectParam param) {
        ConsoleCreateParam createParam = dataSourceCoreConverter.param2consoleParam(param);
        return consoleDataService.create(createParam);
    }

    @Override
    public ActionResult closeConsole(ConsoleCloseParam param) {
        return consoleDataService.close(param);
    }

    @Override
    public ListResult<ExecuteResultDTO> execute(DataSourceExecuteParam param) {
        if (StringUtils.isBlank(param.getSql())) {
            return ListResult.empty();
        }

        // 解析sql
        SqlAnalyseParam sqlAnalyseParam = new SqlAnalyseParam();
        sqlAnalyseParam.setDataSourceId(param.getDataSourceId());
        sqlAnalyseParam.setSql(param.getSql());
        List<SqlDTO> sqlList = sqlDataService.analyse(sqlAnalyseParam).getData();
        if (CollectionUtils.isEmpty(sqlList)) {
            throw new BusinessException(DatasourceErrorEnum.SQL_ANALYSIS_ERROR);
        }

        List<ExecuteResultDTO> result = new ArrayList<>();
        // 执行sql
        for (SqlDTO sqlDTO : sqlList) {
            TemplateExecuteParam templateQueryParam = new TemplateExecuteParam();
            templateQueryParam.setConsoleId(param.getConsoleId());
            templateQueryParam.setDataSourceId(param.getDataSourceId());
            templateQueryParam.setSql(sqlDTO.getSql());
            ExecuteResultDTO executeResult = jdbcTemplateDataService.execute(templateQueryParam).getData();
            result.add(executeResult);
        }

        return ListResult.of(result);
    }

    @Override
    public DataResult<TableDTO> query(TableQueryParam param, TableSelector selector) {
        return tableDataService.query(param, selector);
    }

    @Override
    public PageResult<TableDTO> pageQuery(TablePageQueryParam param, TableSelector selector) {
        return tableDataService.pageQuery(param, selector);
    }
}
