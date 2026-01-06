package com.yupi.yuaiagent.service.impl;

import com.yupi.yuaiagent.domin.entity.ColumnMetadata;
import com.yupi.yuaiagent.mapper.ColumnMetadataMapper;
import com.yupi.yuaiagent.service.IColumnMetadataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class ColumnMetadataService implements IColumnMetadataService {

    private final ColumnMetadataMapper mapper;

    public ColumnMetadataService(ColumnMetadataMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<ColumnMetadata> getColumnMetadata(String tableName, String schemaName) {
        return mapper.selectColumnMetadata(tableName, schemaName);
    }
}
