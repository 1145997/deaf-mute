package dev.forint.deafmute.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import dev.forint.deafmute.modules.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}