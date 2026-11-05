package com.quanxiaoha.xiaohashu.note.biz.rpc;

import com.quanxiaoha.xiaohashu.distributed.id.generator.api.DistributedIdGeneratorFeignApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DistributedIdGeneratorRpcService {

    private final DistributedIdGeneratorFeignApi distributedIdGeneratorFeignApi;

    public String getSnowflakeId(){
        return distributedIdGeneratorFeignApi.getSnowflakeId("test");
    }

}
