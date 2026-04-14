package dev.forint.deafmute.modules.user.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.forint.deafmute.common.config.WechatMiniappProperties;
import dev.forint.deafmute.modules.user.dto.WechatCode2SessionResponse;
import dev.forint.deafmute.modules.user.service.WechatAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class WechatAuthServiceImpl implements WechatAuthService {

    private final WechatMiniappProperties wechatMiniappProperties;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final RestClient restClient = RestClient.builder().build();

    @Override
    public WechatCode2SessionResponse code2Session(String code) {
        try {
            String responseText = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("api.weixin.qq.com")
                            .path("/sns/jscode2session")
                            .queryParam("appid", wechatMiniappProperties.getAppid())
                            .queryParam("secret", wechatMiniappProperties.getSecret())
                            .queryParam("js_code", code)
                            .queryParam("grant_type", "authorization_code")
                            .build())
                    .retrieve()
                    .body(String.class);

            if (!StringUtils.hasText(responseText)) {
                System.out.println("微信 code2Session 返回: " + responseText);
                throw new RuntimeException("调用微信登录接口失败：返回为空");
            }

            WechatCode2SessionResponse response =
                    objectMapper.readValue(responseText, WechatCode2SessionResponse.class);

            if (response.getErrcode() != null && response.getErrcode() != 0) {
                throw new RuntimeException("微信登录失败：" + response.getErrmsg());
            }

            if (!StringUtils.hasText(response.getOpenid())) {
                throw new RuntimeException("微信登录失败：未获取到openid");
            }

            return response;
        } catch (Exception e) {
            throw new RuntimeException("微信登录接口调用异常：" + e.getMessage(), e);
        }
    }
}