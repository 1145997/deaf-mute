package dev.forint.deafmute.modules.phrasetemplate.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("phrase_template")
public class PhraseTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String phraseCode;

    private String phraseText;

    private String ttsText;

    private String sceneType;

    private Integer status;

    private Integer sort;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
