package dev.forint.deafmute.modules.recognition.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import dev.forint.deafmute.modules.recognition.entity.RecognitionRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RecognitionRecordMapper extends BaseMapper<RecognitionRecord> {
}
