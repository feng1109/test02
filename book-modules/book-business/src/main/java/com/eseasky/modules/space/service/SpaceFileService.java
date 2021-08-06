package com.eseasky.modules.space.service;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartFile;

import com.eseasky.common.code.utils.R;

public interface SpaceFileService {

    R<String> imageUpload(MultipartFile file) throws IOException;

    void qrCode(String roomId, HttpServletResponse response);

    void qrCodeBatch(List<String> roomIdList, HttpServletResponse response);

    R<String> roomUpload(MultipartFile file);

    R<String> downloadRoomTemplate(HttpServletResponse response);

}
