package com.quanxiaoha.xiaohashu.oss.biz.service.impl;

import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.xiaohashu.oss.biz.service.FileService;
import com.quanxiaoha.xiaohashu.oss.biz.strategy.FileStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private static final String BUCKET_NAME = "xiaohashu";

    private final FileStrategy fileStrategy;
    @Override
    public Response<?> uploadFile(MultipartFile file) {
        String url = fileStrategy.uploadFile(file, BUCKET_NAME);
        return Response.success(url);
    }
}
