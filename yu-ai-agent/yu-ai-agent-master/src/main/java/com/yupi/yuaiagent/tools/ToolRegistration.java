package com.yupi.yuaiagent.tools;

import com.yupi.yuaiagent.tools.mineControllerTools.*;
import lombok.AllArgsConstructor;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 集中的工具注册类
 */
@Configuration
@AllArgsConstructor
public class ToolRegistration {
    private final MineAreaControllerTool mineAreaControllerTool;
    private final BoreholeControllerTool boreholeControllerTool;
    private final CoalSeamControllerTool coalSeamControllerTool;
    private final ColumnMetadataControllerTool columnMetadataControllerTool;
    private final LithologyLogControllerTool lithologyLogControllerTool;
    private final MiningFaceControllerTool miningFaceControllerTool;
    private final MonthlyProductionControllerTool monthlyProductionControllerTool;
    private final ReserveBlockControllerTool reserveBlockControllerTool;
    private final RoadwayControllerTool roadwayControllerTool;
    private final RoadwayPointControllerTool roadwayPointControllerTool;
    private final SeamInterceptControllerTool seamInterceptControllerTool;
    private final SubsidenceObservationControllerTool subsidenceObservationControllerTool;
    private final SurfaceStationControllerTool surfaceStationControllerTool;
    private final ThreeQuantitiesControllerTool threeQuantitiesControllerTool;
//    @Value("${search-api.api-key}")
//    private String searchApiKey;

    @Bean
    public ToolCallback[] allTools() {
        FileOperationTool fileOperationTool = new FileOperationTool();
//        WebSearchTool webSearchTool = new WebSearchTool(searchApiKey);
//        WebScrapingTool webScrapingTool = new WebScrapingTool();
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        TerminateTool terminateTool = new TerminateTool();
        //TimeTool timeTool = new TimeTool();
        return ToolCallbacks.from(
                fileOperationTool,
//                webSearchTool,
//                webScrapingTool,
                resourceDownloadTool,
                terminalOperationTool,
                pdfGenerationTool,
                terminateTool
                //timeTool
        );
    }

    @Bean
    public ToolCallback[] mineControllerTools() {
        return ToolCallbacks.from(
                mineAreaControllerTool,
                boreholeControllerTool,
                coalSeamControllerTool,
                columnMetadataControllerTool,
                lithologyLogControllerTool,
                miningFaceControllerTool,
                monthlyProductionControllerTool,
                reserveBlockControllerTool,
                roadwayControllerTool,
                roadwayPointControllerTool,
                seamInterceptControllerTool,
                subsidenceObservationControllerTool,
                surfaceStationControllerTool,
                threeQuantitiesControllerTool
        );
    }
}
