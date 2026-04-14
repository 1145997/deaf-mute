package dev.forint.deafmute.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wechat.miniapp")
public class WechatMiniappProperties {

    private String appid;

    private String secret;
}