package com.yupi.yuaiagent.mapper;

import com.yupi.yuaiagent.domin.entity.FileUpload;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FileUploadMapper {

    int insert(FileUpload fileUpload);

    FileUpload selectByFileMd5(@Param("fileMd5") String fileMd5);

    FileUpload selectByFileMd5AndUserId(@Param("fileMd5") String fileMd5, @Param("userId") String userId);

    int updateStatus(@Param("fileMd5") String fileMd5, @Param("status") Integer status);

    int updateMergedAt(@Param("fileMd5") String fileMd5);
}
