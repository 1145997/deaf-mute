package dev.forint.deafmute.modules.lostfound.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import dev.forint.deafmute.modules.lostfound.entity.LostFound;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LostFoundMapper extends BaseMapper<LostFound> {
}