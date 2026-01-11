import request from './index'

const API_BASE_URL = '/data/file-extracted-images'

// 获取指定文件的提取图片列表
export const getFileExtractedImagesByMd5 = async (fileMd5) => {
  try {
    const response = await request.get(`${API_BASE_URL}/by-file-md5/${fileMd5}`)
    return response
  } catch (error) {
    console.error('获取文件提取图片失败:', error)
    throw error
  }
}

// 删除指定ID的提取图片
export const deleteFileExtractedImageById = async (id) => {
  try {
    const response = await request.delete(`${API_BASE_URL}/${id}`)
    return response
  } catch (error) {
    console.error('删除提取图片失败:', error)
    throw error
  }
}

// 删除指定文件的所有提取图片
export const deleteFileExtractedImagesByMd5 = async (fileMd5) => {
  try {
    const response = await request.delete(
      `${API_BASE_URL}/by-file-md5/${fileMd5}`
    )
    return response
  } catch (error) {
    console.error('批量删除提取图片失败:', error)
    throw error
  }
}
