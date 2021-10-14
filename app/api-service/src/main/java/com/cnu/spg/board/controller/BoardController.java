package com.cnu.spg.board.controller;

import com.cnu.spg.board.domain.BoardType;
import com.cnu.spg.board.dto.condition.BoardSearchCondition;
import com.cnu.spg.board.dto.condition.ProjectBoardCondition;
import com.cnu.spg.board.dto.request.BoardTypeRequset;
import com.cnu.spg.board.dto.request.BoardsRequset;
import com.cnu.spg.board.dto.request.CommentRequest;
import com.cnu.spg.board.dto.response.BoardDetailResponse;
import com.cnu.spg.board.dto.response.BoardResponse;
import com.cnu.spg.board.exception.BoardTypeNotValidException;
import com.cnu.spg.board.service.BoardAllService;
import com.cnu.spg.board.service.CommentService;
import com.cnu.spg.board.service.ProjectService;
import com.cnu.spg.config.resolver.UserId;
import com.cnu.spg.user.domain.User;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BoardController {

    private final BoardAllService boardAllService;
    private final ProjectService projectService;
    private final CommentService commentService;

    @ApiOperation("[권한] 전체 게시판 정보를 제공")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, paramType = "header"),
            @ApiImplicitParam(name = "pageNum", value = "page number of pagination", required = true, paramType = "query"),
            @ApiImplicitParam(name = "elementSize", value = "each page element number", required = true, paramType = "query")
    })
    @GetMapping("/api/v1/boards")
    public ResponseEntity<Page<BoardResponse>> getBoards(@Valid BoardsRequset boardsRequset,
                                                         @PageableDefault(sort = "id", direction = Sort.Direction.DESC, size = 20) Pageable pageable) {
        BoardSearchCondition boardSearchCondition = new BoardSearchCondition(boardsRequset.getPartTitle(), boardsRequset.getWriterName(), boardsRequset.getPartOfContent());

        return ResponseEntity.ok().body(boardAllService.findBoardsOnePage(boardSearchCondition, pageable));
    }

    @ApiOperation("[권한] board type에 따른 정보 조회")
    @GetMapping("/api/v1/boards/{boardType}")
    public ResponseEntity<Page<BoardResponse>> findBoardByType(@PathVariable String boardType, @Valid BoardTypeRequset boardsRequset,
                                                               @PageableDefault(sort = "id", direction = Sort.Direction.DESC, size = 20) Pageable pageable) {
        BoardType boardTypeEnum = BoardType.findBoardTypeByKey(boardType)
                .orElseThrow(BoardTypeNotValidException::new);

        ProjectBoardCondition projectBoardCondition = new ProjectBoardCondition(boardsRequset.getPartOfContent(), boardsRequset.getWriterName(), boardsRequset.getPartOfContent());
        if (boardTypeEnum == BoardType.PROJECT) {
            return ResponseEntity.ok().body(projectService.findProjectBoardsOnePage(projectBoardCondition, pageable, boardsRequset.getCategoryId()));
        }

        throw new IllegalArgumentException();
    }


    @ApiOperation("[권한] board 정보 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true, paramType = "header"),
            @ApiImplicitParam(name = "id", value = "board id 정보", readOnly = true, paramType = "path")
    })
    @GetMapping("/api/v1/boards/{boardType}/{id}")
    public ResponseEntity<? extends BoardDetailResponse> getBoard(@PathVariable String boardType, @PathVariable("id") Long boardId) {
        BoardType boardTypeEnum = BoardType.findBoardTypeByKey(boardType)
                .orElseThrow(BoardTypeNotValidException::new);
        return ResponseEntity.ok().body(boardAllService.getBoard(boardTypeEnum, boardId));
    }


    @PostMapping("/api/v1/boards/comment")
    public ResponseEntity<URI> createComment(@Valid @RequestBody CommentRequest commentRequest, @UserId User user) {
        URI commentUri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(commentService.saveNewComment(commentRequest, user))
                .toUri();

        return ResponseEntity.created(commentUri).build();
    }
}
