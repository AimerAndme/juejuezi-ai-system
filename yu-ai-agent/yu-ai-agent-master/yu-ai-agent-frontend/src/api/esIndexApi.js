import request from './index'

/**
 * 根据文件MD5查询ES索引文档（分页）
 */
export const queryEsByFileMd5 = (fileMd5, from = 0, size = 100) => {
  return request.get('/es/index/query/fileMd5', {
    params: { fileMd5, from, size },
  })
}

/**
 * 根据文档ID查询ES索引单个文档
 */
export const getEsDocById = (docId) => {
  return request.get(`/es/index/query/${docId}`)
}

/**
 * 统计fileMd5对应的ES文档数量
 */
export const countEsByFileMd5 = (fileMd5) => {
  return request.get('/es/index/count/fileMd5', {
    params: { fileMd5 },
  })
}

/**
 * 根据fileMd5删除ES索引文档
 */
export const deleteEsByFileMd5 = (fileMd5) => {
  return request.delete('/es/index/delete/fileMd5', {
    params: { fileMd5 },
  })
}

/**
 * 根据文档ID删除ES索引单个文档
 */
export const deleteEsDocById = (docId) => {
  return request.delete(`/es/index/delete/${docId}`)
}
