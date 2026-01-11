package com.yupi.yuaiagent.domin.constant;

/**
 * 文件常量
 */
public interface FileConstant {

    /**
     * 零时文件保存目录
     */
    String FILE_SAVE_DIR = System.getProperty("user.dir") + "/tmp";
    /**
     * 文件保存目录
     */
    String FILE_UPLOAD_SAVE_DIR_ = System.getProperty("user.dir") + "/upload/files";
    /**
     * 文件保存目录
     */
    String IMAGES_UPLOAD_SAVE_DIR_ = System.getProperty("user.dir") + "/upload/images";
    //文件图片的前缀
    String IMAGES_UPLOADS_PREFIX = "_extracted_image_page_";
}
