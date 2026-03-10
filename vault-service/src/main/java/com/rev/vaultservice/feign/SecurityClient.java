package com.rev.vaultservice.feign;

import com.rev.vaultservice.dto.PasswordRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "SECURITY-SERVICE", path = "/security")
public interface SecurityClient {

    @PostMapping("/check-leaked")
    boolean checkLeaked(@RequestBody PasswordRequest request);

    @PostMapping("/check-leaked-batch")
    List<Boolean> checkLeakedBatch(@RequestBody List<String> passwords);
}
