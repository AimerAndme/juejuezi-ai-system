package com.yupi.yuaiagent.mapper;

import com.yupi.yuaiagent.domin.entity.FileExtractedImages;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface FileExtractedImagesMapper {

    int insert(FileExtractedImages entity);
    int insertBatch(@Param("entities") List<FileExtractedImages> entities);
    FileExtractedImages selectById(@Param("id") Integer id);

    List<FileExtractedImages> selectByFileMd5(@Param("fileMd5") String fileMd5);

    List<FileExtractedImages> selectByUserId(@Param("userId") Integer userId);

    List<FileExtractedImages> selectAll();

    int update(FileExtractedImages entity);

    int deleteById(@Param("id") Integer id);

    int deleteByFileMd5(@Param("fileMd5") String fileMd5);
}