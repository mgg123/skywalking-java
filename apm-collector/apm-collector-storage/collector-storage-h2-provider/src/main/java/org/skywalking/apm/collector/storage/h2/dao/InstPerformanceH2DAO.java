/*
 * Copyright 2017, OpenSkywalking Organization All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Project repository: https://github.com/OpenSkywalking/skywalking
 */

package org.skywalking.apm.collector.storage.h2.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.skywalking.apm.collector.client.h2.H2Client;
import org.skywalking.apm.collector.client.h2.H2ClientException;
import org.skywalking.apm.collector.core.data.Data;
import org.skywalking.apm.collector.storage.base.dao.IPersistenceDAO;
import org.skywalking.apm.collector.storage.base.define.DataDefine;
import org.skywalking.apm.collector.storage.dao.IInstPerformanceDAO;
import org.skywalking.apm.collector.storage.h2.base.dao.H2DAO;
import org.skywalking.apm.collector.storage.h2.base.define.H2SqlEntity;
import org.skywalking.apm.collector.storage.sql.SqlBuilder;
import org.skywalking.apm.collector.storage.table.instance.InstPerformanceTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author peng-yongsheng, clevertension
 */
public class InstPerformanceH2DAO extends H2DAO implements IInstPerformanceDAO, IPersistenceDAO<H2SqlEntity, H2SqlEntity> {

    private final Logger logger = LoggerFactory.getLogger(InstPerformanceH2DAO.class);
    private static final String GET_SQL = "select * from {0} where {1} = ?";

    @Override public Data get(String id, DataDefine dataDefine) {
        H2Client client = getClient();
        String sql = SqlBuilder.buildSql(GET_SQL, InstPerformanceTable.TABLE, InstPerformanceTable.COLUMN_ID);
        Object[] params = new Object[] {id};
        try (ResultSet rs = client.executeQuery(sql, params)) {
            if (rs.next()) {
                Data data = dataDefine.build(id);
                data.setDataInteger(0, rs.getInt(InstPerformanceTable.COLUMN_APPLICATION_ID));
                data.setDataInteger(1, rs.getInt(InstPerformanceTable.COLUMN_INSTANCE_ID));
                data.setDataInteger(2, rs.getInt(InstPerformanceTable.COLUMN_CALLS));
                data.setDataLong(0, rs.getLong(InstPerformanceTable.COLUMN_COST_TOTAL));
                data.setDataLong(1, rs.getLong(InstPerformanceTable.COLUMN_TIME_BUCKET));
                return data;
            }
        } catch (SQLException | H2ClientException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    @Override public H2SqlEntity prepareBatchInsert(Data data) {
        Map<String, Object> source = new HashMap<>();
        H2SqlEntity entity = new H2SqlEntity();
        source.put(InstPerformanceTable.COLUMN_ID, data.getDataString(0));
        source.put(InstPerformanceTable.COLUMN_APPLICATION_ID, data.getDataInteger(0));
        source.put(InstPerformanceTable.COLUMN_INSTANCE_ID, data.getDataInteger(1));
        source.put(InstPerformanceTable.COLUMN_CALLS, data.getDataInteger(2));
        source.put(InstPerformanceTable.COLUMN_COST_TOTAL, data.getDataLong(0));
        source.put(InstPerformanceTable.COLUMN_TIME_BUCKET, data.getDataLong(1));
        String sql = SqlBuilder.buildBatchInsertSql(InstPerformanceTable.TABLE, source.keySet());
        entity.setSql(sql);
        entity.setParams(source.values().toArray(new Object[0]));
        return entity;
    }

    @Override public H2SqlEntity prepareBatchUpdate(Data data) {
        Map<String, Object> source = new HashMap<>();
        H2SqlEntity entity = new H2SqlEntity();
        source.put(InstPerformanceTable.COLUMN_APPLICATION_ID, data.getDataInteger(0));
        source.put(InstPerformanceTable.COLUMN_INSTANCE_ID, data.getDataInteger(1));
        source.put(InstPerformanceTable.COLUMN_CALLS, data.getDataInteger(2));
        source.put(InstPerformanceTable.COLUMN_COST_TOTAL, data.getDataLong(0));
        source.put(InstPerformanceTable.COLUMN_TIME_BUCKET, data.getDataLong(1));
        String id = data.getDataString(0);
        String sql = SqlBuilder.buildBatchUpdateSql(InstPerformanceTable.TABLE, source.keySet(), InstPerformanceTable.COLUMN_ID);
        entity.setSql(sql);
        List<Object> values = new ArrayList<>(source.values());
        values.add(id);
        entity.setParams(values.toArray(new Object[0]));
        return entity;
    }
}
