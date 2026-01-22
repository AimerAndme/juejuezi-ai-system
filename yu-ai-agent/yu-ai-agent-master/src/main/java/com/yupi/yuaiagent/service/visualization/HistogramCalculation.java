package com.yupi.yuaiagent.service.visualization;

import com.yupi.yuaiagent.domin.entity.HistogramRange;
import com.yupi.yuaiagent.domin.entity.HistogramStatistics;
import com.yupi.yuaiagent.domin.vo.HistogramData;
import com.yupi.yuaiagent.service.HistogramStatisticsService;
import com.yupi.yuaiagent.service.StatisticsCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistogramCalculation implements CalculationStrategy<List<HistogramRange>, Double> {

    @Autowired
    private HistogramStatisticsService histogramStatisticsService;

    @Autowired
    private StatisticsCalculationService statisticsCalculationService;

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

    public HistogramData handleCalculation(List<Double> dataList, int dataLength, int interval, Double max, Double min) {
        if (dataList == null || dataList.isEmpty()) {
            return null;
        }

        List<HistogramRange> histogramRanges;
        if (dataLength >= 100000 && dataLength <= 500000) {
            histogramRanges = useDataApplicationCalculation(dataList);
        } else {
            histogramRanges = histogramStatisticsService.calculateHistogram(dataList, interval, max, min);
        }

        HistogramStatistics statistics = statisticsCalculationService.calculateAllStatistics(dataList);

        HistogramData histogramData = new HistogramData();
        histogramData.setInterval(histogramRanges);
        histogramData.setTotalCount(statistics.getTotalCount());
        histogramData.setInvalidCount(statistics.getInvalidCount());
        histogramData.setMean(statistics.getMean());
        histogramData.setStandardDeviation(statistics.getStandardDeviation());
        histogramData.setCoefficientOfVariation(statistics.getCoefficientOfVariation());
        histogramData.setMaxValue(statistics.getMaxValue());
        histogramData.setUpperQuartile(statistics.getUpperQuartile());
        histogramData.setLowerQuartile(statistics.getLowerQuartile());
        histogramData.setMedian(statistics.getMedian());
        histogramData.setMinValue(statistics.getMinValue());
        histogramData.setMeanMinus2Sigma(statistics.getMeanMinus2Sigma());
        histogramData.setMeanPlus2Sigma(statistics.getMeanPlus2Sigma());
        histogramData.setLogMean(statistics.getLogMean());
        histogramData.setLogVariance(statistics.getLogVariance());
        histogramData.setShapeParameter(statistics.getShapeParameter());
        histogramData.setScaleParameter(statistics.getScaleParameter());

        return histogramData;
    }

    private int calculateOptimalIntervalCount(int dataSize) {
        if (dataSize <= 0) {
            return 10;
        }

        int sturgesInterval = (int) Math.ceil(Math.log(dataSize) / Math.log(2)) + 1;
        return Math.max(5, Math.min(50, sturgesInterval));
    }
}
