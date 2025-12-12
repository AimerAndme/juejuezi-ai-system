package com.yupi.yuaiagent.mapper;

import com.yupi.yuaiagent.domin.entity.ChunkInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ChunkInfoMapper {

    int insert(ChunkInfo chunkInfo);

    List<ChunkInfo> selectByFileMd5(@Param("fileMd5") String fileMd5);

    ChunkInfo selectByFileMd5AndChunkIndex(@Param("fileMd5") String fileMd5, @Param("chunkIndex") Integer chunkIndex);

    int countByFileMd5(@Param("fileMd5") String fileMd5);
}
