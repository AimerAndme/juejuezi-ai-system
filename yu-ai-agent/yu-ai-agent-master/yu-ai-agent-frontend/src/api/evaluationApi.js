import request from './index'

export const runEvaluation = (testCases) => {
  return request.post('/evaluation/run', testCases)
}

export const runEvaluationWithLLM = (testCases) => {
  return request.post('/evaluation/run-with-llm', testCases)
}

export const runEvaluationFromDataset = (testCases) => {
  return request.post('/evaluation/run', testCases)
}

export const runFullEvaluation = (
  testCases,
  outputDir = './evaluation_reports'
) => {
  return request.post('/evaluation/run-full-data', testCases, {
    params: { outputDir },
  })
}

export const createSampleDataset = (outputPath = './sample_dataset.json') => {
  return request.post('/evaluation/create-sample-dataset', null, {
    params: { outputPath },
  })
}

export const getAvailableMetrics = () => {
  return request.get('/evaluation/metrics')
}

export const evaluateWithRAGPipeline = (testCases, topK = 5) => {
  return request.post(
    '/evaluation/evaluate-with-rag-pipeline-data',
    testCases,
    {
      params: { topK },
    }
  )
}

export const evaluateRetrievalOnly = (testCases, topK = 5) => {
  return request.post('/evaluation/evaluate-retrieval-only-data', testCases, {
    params: { topK },
  })
}
