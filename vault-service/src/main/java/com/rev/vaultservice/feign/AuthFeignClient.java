package com.rev.vaultservice.feign;

import com.rev.vaultservice.dto.MasterPasswordDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service")
public interface AuthFeignClient {
    @PostMapping("/api/auth/verify-master")
    boolean verifyMasterPassword(@RequestBody MasterPasswordDto dto);
}
