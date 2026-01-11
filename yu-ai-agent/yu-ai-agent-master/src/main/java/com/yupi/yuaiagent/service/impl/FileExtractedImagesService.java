package com.yupi.yuaiagent.service.impl;

import com.yupi.yuaiagent.domin.entity.FileExtractedImages;
import com.yupi.yuaiagent.mapper.FileExtractedImagesMapper;
import com.yupi.yuaiagent.service.IFileExtractedImagesService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class FileExtractedImagesService implements IFileExtractedImagesService {

    private final FileExtractedImagesMapper mapper;

    public FileExtractedImagesService(FileExtractedImagesMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public int addFileExtractedImages(FileExtractedImages images) {
        return mapper.insert(images);
    }

    @Override
    public FileExtractedImages getFileExtractedImagesById(Integer id) {
        return mapper.selectById(id);
    }

    @Override
    public List<FileExtractedImages> getFileExtractedImagesByFileMd5(String fileMd5) {
        return mapper.selectByFileMd5(fileMd5);
    }

    @Override
    public List<FileExtractedImages> getFileExtractedImagesByUserId(Integer userId) {
        return mapper.selectByUserId(userId);
    }

    @Override
    public List<FileExtractedImages> getAllFileExtractedImages() {
        return mapper.selectAll();
    }

    @Override
    public int updateFileExtractedImages(FileExtractedImages images) {
        return mapper.update(images);
    }

    @Override
    public int deleteFileExtractedImagesById(Integer id) {
        return mapper.deleteById(id);
    }

    @Override
    public int deleteFileExtractedImagesByFileMd5(String fileMd5) {
        return mapper.deleteByFileMd5(fileMd5);
    }
}