package com.yupi.yuaiagent.service.visualization;

import java.util.List;

public interface CalculationStrategy<R,T> {
    R useDataApplicationCalculation(List<T> dataList);

    R useHybridCalculation(List<T> dataList);

//    R handleCalculation(List<T> dataList, int dataLength);
}
