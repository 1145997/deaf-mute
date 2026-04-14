package dev.forint.deafmute.modules.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import dev.forint.deafmute.modules.admin.entity.Admin;
import dev.forint.deafmute.modules.admin.vo.AdminInfoVO;

import java.util.Map;

public interface AdminService extends IService<Admin> {

    Map<String, Object> login(String username, String password);

    AdminInfoVO getCurrentAdminInfo();
}