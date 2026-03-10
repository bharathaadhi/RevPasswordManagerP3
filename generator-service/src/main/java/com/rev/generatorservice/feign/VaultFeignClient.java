package com.rev.generatorservice.feign;

import com.rev.generatorservice.dto.VaultRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "vault-service", url = "http://localhost:8082")
public interface VaultFeignClient {

    @PostMapping("/vault/save")
    Object saveToVault(@RequestBody VaultRequestDto dto);
}