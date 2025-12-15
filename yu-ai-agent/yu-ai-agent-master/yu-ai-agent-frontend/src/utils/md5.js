/**
 * 文件MD5计算工具
 * 使用原生 Web Crypto API (SHA-256)
 */

/**
 * 计算文件MD5（使用Web Crypto API的SHA-256前32位）
 */
export async function calculateFileMD5(file, onProgress) {
  console.log('[MD5计算] 开始计算文件hash:', file.name, '大小:', file.size)
  const chunkSize = 2097152 // 2MB
  const chunks = Math.ceil(file.size / chunkSize)
  console.log('[MD5计算] 总分片数:', chunks)

  // 使用流式计算，按顺序读取所有分片并拼接成完整文件再计算hash
  const fileBytes = []

  for (let i = 0; i < chunks; i++) {
    const start = i * chunkSize
    const end = Math.min(start + chunkSize, file.size)
    const chunk = file.slice(start, end)
    const buffer = await chunk.arrayBuffer()
    fileBytes.push(new Uint8Array(buffer))

    if (onProgress) {
      onProgress(Math.floor(((i + 1) / chunks) * 50)) // 前50%是读取进度
    }
  }

  // 合并所有字节
  const totalLength = fileBytes.reduce((acc, arr) => acc + arr.length, 0)
  console.log(
    '[MD5计算] 合并后总字节数:',
    totalLength,
    '原始文件大小:',
    file.size
  )
  const completeFile = new Uint8Array(totalLength)
  let offset = 0
  for (const bytes of fileBytes) {
    completeFile.set(bytes, offset)
    offset += bytes.length
  }

  // 对完整文件计算hash
  const finalHash = await crypto.subtle.digest('SHA-256', completeFile.buffer)
  const hashArray = Array.from(new Uint8Array(finalHash))
  const hashHex = hashArray.map((b) => b.toString(16).padStart(2, '0')).join('')
  const result = hashHex.substring(0, 32) // 取前32位模拟MD5

  console.log('[MD5计算] 完成, hash:', result)

  if (onProgress) {
    onProgress(100)
  }

  return result
}

/**
 * 计算分片MD5（使用SHA-256前32位）
 */
export async function calculateChunkMD5(blob) {
  const buffer = await blob.arrayBuffer()
  const hashBuffer = await crypto.subtle.digest('SHA-256', buffer)
  const hashArray = Array.from(new Uint8Array(hashBuffer))
  const hashHex = hashArray.map((b) => b.toString(16).padStart(2, '0')).join('')
  const result = hashHex.substring(0, 32)
  console.log('[分片MD5] size:', blob.size, 'hash:', result)
  return result
}
