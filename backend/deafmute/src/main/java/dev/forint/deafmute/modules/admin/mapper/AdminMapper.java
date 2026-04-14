package dev.forint.deafmute.modules.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import dev.forint.deafmute.modules.admin.entity.Admin;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminMapper extends BaseMapper<Admin> {
}