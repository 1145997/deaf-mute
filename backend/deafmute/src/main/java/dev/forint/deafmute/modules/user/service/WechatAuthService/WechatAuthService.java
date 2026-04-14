package dev.forint.deafmute.modules.user.service;

import dev.forint.deafmute.modules.user.dto.WechatCode2SessionResponse;

public interface WechatAuthService {

    WechatCode2SessionResponse code2Session(String code);
}