package com.yupi.yuaiagent.service.visualization;

import com.yupi.yuaiagent.domin.entity.HistogramRange;
import com.yupi.yuaiagent.service.HistogramStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistogramCalculation implements CalculationStrategy<List<HistogramRange>, Double> {

    @Autowired
    private HistogramStatisticsService histogramStatisticsService;

    /**
     * 10万-50万数据处理方案
     *
     * @param dataList
     * @return
     */
    @Override
    public List<HistogramRange> useDataApplicationCalculation(List<Double> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return null;
        }

        List<HistogramRange> result = histogramStatisticsService.calculateHistogramAutoInterval(dataList);
        return result;
    }

    @Override
    public List<HistogramRange> useHybridCalculation(List<Double> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return null;
        }

        List<HistogramRange> result = histogramStatisticsService.calculateHistogramAutoInterval(dataList);
        return result;
    }


    public List<HistogramRange> handleCalculation(List<Double> dataList, int dataLength, int interval) {
        if (dataList == null || dataList.isEmpty()) {
            return null;
        }
        if (dataLength >= 100000 && dataLength <= 500000) {
            return useDataApplicationCalculation(dataList);
        }
//        if (interval)
//        int intervalCount = calculateOptimalIntervalCount(dataLength);
        List<HistogramRange> result = histogramStatisticsService.calculateHistogram(dataList, interval);
        return result;
    }


    private int calculateOptimalIntervalCount(int dataSize) {
        if (dataSize <= 0) {
            return 10;
        }

        int sturgesInterval = (int) Math.ceil(Math.log(dataSize) / Math.log(2)) + 1;
        return Math.max(5, Math.min(50, sturgesInterval));
    }

}
