//package com.yupi.yuaiagent.service.visualization;
//
//import com.yupi.yuaiagent.mapper.VisualizationMapper;
//import lombok.AllArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.stream.Stream;
//
//@Service
//@AllArgsConstructor
//public class DataLoadingService {
//    private final VisualizationMapper visualizationMapper;
//    // 小数据量：直接加载全部数据
//    public <T> List<T> loadAllData(String tableName, Stint estimatedSize) {
//        if (estimatedSize < 10000) {
//            return visualizationMapper.getValueRange(tableName);
//        }
//        return Collections.emptyList();
//    }
//
//    // 中等数据量：分批加载
//    public <T> List<T> loadBatchData(String tableName, int estimatedSize) {
//        if (estimatedSize >= 10000 && estimatedSize < 100000) {
//            int batchSize = 5000;
//            return loadInBatches(tableName, batchSize);
//        }
//        return Collections.emptyList();
//    }
//
//    // 大数据量：流式处理
//    public <T> Stream<T> loadStreamData(String tableName, int estimatedSize) {
//        if (estimatedSize >= 100000) {
//            return repository.streamAll();
//        }
//        return Stream.empty();
//    }
//}