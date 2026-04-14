package dev.forint.deafmute.modules.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import dev.forint.deafmute.modules.user.dto.AdminUserQueryDTO;
import dev.forint.deafmute.modules.user.dto.UserLoginDTO;
import dev.forint.deafmute.modules.user.dto.WxUserLoginDTO;
import dev.forint.deafmute.modules.user.entity.User;
import dev.forint.deafmute.modules.user.vo.UserListVO;

import java.util.Map;

public interface UserService extends IService<User> {

    Map<String, Object> login(UserLoginDTO dto);

    Map<String, Object> wxLogin(WxUserLoginDTO dto);

    Page<UserListVO> getAdminPage(AdminUserQueryDTO dto);

    User getDetailById(Long id);

    void updateStatus(Long id, Integer status);
}