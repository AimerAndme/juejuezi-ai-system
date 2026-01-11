package com.yupi.yuaiagent.service;

import com.yupi.yuaiagent.domin.entity.FileExtractedImages;
import java.util.List;

/**
 * 文件提取图片服务接口
 */
public interface IFileExtractedImagesService {

    /**
     * 新增提取图片记录
     *
     * @param images 提取图片信息
     * @return 影响行数
     */
    int addFileExtractedImages(FileExtractedImages images);

    /**
     * 根据ID查询提取图片记录
     *
     * @param id 记录ID
     * @return 提取图片信息
     */
    FileExtractedImages getFileExtractedImagesById(Integer id);

    /**
     * 根据文件MD5查询提取图片记录
     *
     * @param fileMd5 文件MD5
     * @return 提取图片列表
     */
    List<FileExtractedImages> getFileExtractedImagesByFileMd5(String fileMd5);

    /**
     * 根据用户ID查询提取图片记录
     *
     * @param userId 用户ID
     * @return 提取图片列表
     */
    List<FileExtractedImages> getFileExtractedImagesByUserId(Integer userId);

    /**
     * 查询所有提取图片记录
     *
     * @return 提取图片列表
     */
    List<FileExtractedImages> getAllFileExtractedImages();

    /**
     * 更新提取图片记录
     *
     * @param images 提取图片信息
     * @return 影响行数
     */
    int updateFileExtractedImages(FileExtractedImages images);

    /**
     * 根据ID删除提取图片记录
     *
     * @param id 记录ID
     * @return 影响行数
     */
    int deleteFileExtractedImagesById(Integer id);

    /**
     * 根据文件MD5删除提取图片记录
     *
     * @param fileMd5 文件MD5
     * @return 影响行数
     */
    int deleteFileExtractedImagesByFileMd5(String fileMd5);
}