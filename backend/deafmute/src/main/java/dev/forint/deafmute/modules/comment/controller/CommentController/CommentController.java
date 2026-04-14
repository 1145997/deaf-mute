package dev.forint.deafmute.modules.comment.controller;

import dev.forint.deafmute.common.api.Result;
import dev.forint.deafmute.common.utils.UserTokenUtils;
import dev.forint.deafmute.modules.comment.dto.CommentAddDTO;
import dev.forint.deafmute.modules.comment.service.CommentService;
import dev.forint.deafmute.modules.comment.vo.CommentVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private final UserTokenUtils userTokenUtils;
    private final CommentService commentService;

    @PostMapping
    public Result<Void> add(@RequestBody @Valid CommentAddDTO dto) {
        userTokenUtils.checkUserLogin();

        commentService.add(dto);
        return Result.success("评论成功", null);
    }

    @GetMapping("/list")
    public Result<List<CommentVO>> list(@RequestParam Long infoId) {
        return Result.success(commentService.getListByInfoId(infoId));
    }
}