package com.yupi.yuaiagent.mapper;

import com.yupi.yuaiagent.domin.entity.FileUpload;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FileUploadMapper {

    int insert(FileUpload fileUpload);

    FileUpload selectByFileMd5(@Param("fileMd5") String fileMd5);

    List<FileUpload> selectByFileMd5List(@Param("md5List") List<String> md5List);

    FileUpload selectByFileMd5AndUserId(@Param("fileMd5") String fileMd5, @Param("userId") String userId);

    int updateStatus(@Param("fileMd5") String fileMd5, @Param("status") Integer status);

    int updateMergedAt(@Param("fileMd5") String fileMd5);

    // 文件管理接口
    List<FileUpload> selectByUserId(@Param("userId") String userId);

    List<FileUpload> selectAll();

    int deleteByFileMd5(@Param("fileMd5") String fileMd5);

    int updateFileName(@Param("fileMd5") String fileMd5, @Param("fileName") String fileName);

    int updateIsPublic(@Param("fileMd5") String fileMd5, @Param("isPublic") Boolean isPublic);
}
