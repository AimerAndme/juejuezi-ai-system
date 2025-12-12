/**
 * 文件MD5计算工具
 * 使用原生 Web Crypto API (SHA-256)
 */

/**
 * 计算文件MD5（使用Web Crypto API的SHA-256前32位）
 */
export async function calculateFileMD5(file, onProgress) {
  const chunkSize = 2097152 // 2MB
  const chunks = Math.ceil(file.size / chunkSize)
  const hashParts = []
  
  for (let i = 0; i < chunks; i++) {
    const start = i * chunkSize
    const end = Math.min(start + chunkSize, file.size)
    const chunk = file.slice(start, end)
    const buffer = await chunk.arrayBuffer()
    const hashBuffer = await crypto.subtle.digest('SHA-256', buffer)
    const hashArray = Array.from(new Uint8Array(hashBuffer))
    hashParts.push(...hashArray)
    
    if (onProgress) {
      onProgress(Math.floor(((i + 1) / chunks) * 100))
    }
  }
  
  // 对所有分片的hash再进行一次hash，模拟完整文件MD5
  const finalBuffer = new Uint8Array(hashParts).buffer
  const finalHash = await crypto.subtle.digest('SHA-256', finalBuffer)
  const hashArray = Array.from(new Uint8Array(finalHash))
  const hashHex = hashArray.map(b => b.toString(16).padStart(2, '0')).join('')
  return hashHex.substring(0, 32) // 取前32位模拟MD5
}

/**
 * 计算分片MD5（使用SHA-256前32位）
 */
export async function calculateChunkMD5(blob) {
  const buffer = await blob.arrayBuffer()
  const hashBuffer = await crypto.subtle.digest('SHA-256', buffer)
  const hashArray = Array.from(new Uint8Array(hashBuffer))
  const hashHex = hashArray.map(b => b.toString(16).padStart(2, '0')).join('')
  return hashHex.substring(0, 32)
}
