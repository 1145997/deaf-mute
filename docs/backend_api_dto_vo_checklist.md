# 智能手语翻译工具后端接口与 DTO/VO 清单

基于当前项目的 `Result` 包装、`/api` 上下文和前后台分离风格整理。

## 统一约定

分页接口统一返回：

```json
{
  "list": [],
  "total": 0,
  "pageNum": 1,
  "pageSize": 10
}
```

建议枚举：
- `PostStatusEnum`: `PENDING` `PUBLISHED` `REJECTED` `OFFLINE`
- `CategoryTypeEnum`: `POST` `LEARNING`
- `FlowTypeEnum`: `SINGLE` `SEQUENCE` `CONTROL`
- `OutputTypeEnum`: `TEXT` `PHRASE` `CONTROL` `NONE`
- `ControlActionEnum`: `DELETE_LAST` `CLEAR_ALL` `FINISH_INPUT` `CONFIRM` `CANCEL`

## Post 模块

客户端接口：

- `POST /api/post`
  - `PostAddDTO`
```java
private Long categoryId;
private String title;
private String content;
private String coverImage;
private List<String> imageList;
private String sourceType;
private Long sourceRecordId;
```

- `GET /api/post/list`
  - `PostQueryDTO`
```java
private Integer pageNum;
private Integer pageSize;
private Long categoryId;
private String keyword;
private String sortBy;
```
  - `PostListVO`
```java
private Long id;
private Long userId;
private String userNickname;
private String userAvatar;
private Long categoryId;
private String categoryName;
private String title;
private String contentPreview;
private String coverImage;
private Integer status;
private Integer viewCount;
private Integer commentCount;
private String sourceType;
private LocalDateTime createTime;
```

- `GET /api/post/{id}`
  - `PostDetailVO`
```java
private Long id;
private Long userId;
private String userNickname;
private String userAvatar;
private Long categoryId;
private String categoryName;
private String title;
private String content;
private String coverImage;
private List<String> imageList;
private Integer status;
private Integer viewCount;
private Integer commentCount;
private String sourceType;
private Long sourceRecordId;
private LocalDateTime createTime;
```

- `GET /api/post/my`
- `PUT /api/post/{id}`
  - `PostUpdateDTO`
```java
private Long categoryId;
private String title;
private String content;
private String coverImage;
private List<String> imageList;
```
- `DELETE /api/post/{id}`

管理端接口：

- `GET /api/admin/post/pending`
  - `AdminPostPendingQueryDTO`
- `GET /api/admin/post/list`
  - `AdminPostQueryDTO`
```java
private Integer pageNum;
private Integer pageSize;
private String keyword;
private Long categoryId;
private Integer status;
private Long userId;
private String sourceType;
```
  - `AdminPostListVO`
```java
private Long id;
private String title;
private String userNickname;
private String categoryName;
private Integer status;
private String sourceType;
private Integer viewCount;
private Integer commentCount;
private String auditReason;
private LocalDateTime createTime;
private LocalDateTime auditTime;
```
- `GET /api/admin/post/{id}`
- `PUT /api/admin/post/{id}/approve`
- `PUT /api/admin/post/{id}/reject`
  - `PostRejectDTO`
```java
private String auditReason;
```
- `PUT /api/admin/post/{id}/status`
  - `PostStatusDTO`
```java
private Integer status;
```

## Comment 模块

- `POST /api/comment`
  - `CommentAddDTO`
```java
private Long postId;
private Long parentId;
private String content;
```

- `GET /api/comment/list?postId=`
  - `CommentVO`
```java
private Long id;
private Long postId;
private Long userId;
private String userNickname;
private String userAvatar;
private Long parentId;
private String content;
private LocalDateTime createTime;
private List<CommentVO> children;
```

- `GET /api/admin/comment/list`
  - `AdminCommentQueryDTO`
```java
private Integer pageNum;
private Integer pageSize;
private Long postId;
private Long userId;
private Integer status;
private String keyword;
```

- `PUT /api/admin/comment/{id}/status`
  - `CommentStatusDTO`
```java
private Integer status;
```

- `DELETE /api/admin/comment/{id}`

## Category 模块

- `GET /api/category/list?type=POST`
  - `CategoryVO`
```java
private Long id;
private String name;
private String type;
private String icon;
private String description;
private Integer sort;
private Integer status;
```

- `GET /api/admin/category/list`
  - `AdminCategoryQueryDTO`

- `POST /api/admin/category`
  - `CategoryAddDTO`
```java
private String name;
private String type;
private String icon;
private String description;
private Integer sort;
private Integer status;
```

- `PUT /api/admin/category/{id}`
- `DELETE /api/admin/category/{id}`
- `PUT /api/admin/category/{id}/status`
  - `CategoryStatusDTO`

## Learning 模块

- `GET /api/learning/list`
  - `LearningQueryDTO`
- `GET /api/learning/{id}`
  - `LearningDetailVO`
```java
private Long id;
private Long categoryId;
private String categoryName;
private String title;
private String summary;
private String content;
private String coverImage;
private Integer difficultyLevel;
private Integer viewCount;
private LocalDateTime createTime;
```

- `GET /api/admin/learning/list`
  - `AdminLearningQueryDTO`
- `POST /api/admin/learning`
  - `LearningAddDTO`
```java
private Long categoryId;
private String title;
private String summary;
private String content;
private String coverImage;
private Integer difficultyLevel;
private Integer status;
private Integer sort;
```
- `PUT /api/admin/learning/{id}`
- `DELETE /api/admin/learning/{id}`
- `PUT /api/admin/learning/{id}/status`

## PhraseTemplate 模块

- `GET /api/phrase-template/list`
  - `PhraseTemplateVO`
```java
private Long id;
private String phraseCode;
private String phraseText;
private String ttsText;
private String sceneType;
```

- `GET /api/admin/phrase-template/list`
- `POST /api/admin/phrase-template`
  - `PhraseTemplateAddDTO`
```java
private String phraseCode;
private String phraseText;
private String ttsText;
private String sceneType;
private Integer status;
private Integer sort;
```
- `PUT /api/admin/phrase-template/{id}`
- `DELETE /api/admin/phrase-template/{id}`

## GestureLibrary 模块

- `GET /api/gesture-library/enabled`
  - `GestureLibraryVO`
```java
private Long id;
private String gestureCode;
private String gestureName;
private String description;
private String previewImage;
private Integer sort;
private Integer isBuiltin;
```

- `GET /api/admin/gesture-library/list`
  - `AdminGestureLibraryQueryDTO`
- `GET /api/admin/gesture-library/{id}`
  - `GestureLibraryDetailVO`
- `POST /api/admin/gesture-library`
  - `GestureLibraryAddDTO`
```java
private String gestureCode;
private String gestureName;
private String description;
private String previewImage;
private Integer status;
private Integer sort;
private Integer isBuiltin;
private String detectionKey;
```
- `PUT /api/admin/gesture-library/{id}`
- `PUT /api/admin/gesture-library/{id}/status`

## RecognitionConfig 模块

- `GET /api/admin/recognition-config/active`
  - `RecognitionConfigVO`
```java
private Long id;
private String configName;
private BigDecimal confidenceMin;
private Integer holdMs;
private Integer debounceMs;
private Integer cooldownMs;
private Integer requiredHits;
private Integer maxIntervalMs;
private Integer lockTimeoutMs;
private Integer resetOnFail;
private Integer allowRepeat;
private List<String> gestureOrder;
private Integer activeFlag;
private String remark;
```

- `GET /api/admin/recognition-config/list`
- `POST /api/admin/recognition-config`
  - `RecognitionConfigAddDTO`
- `PUT /api/admin/recognition-config/{id}`
- `PUT /api/admin/recognition-config/{id}/activate`

## GestureFlow 模块

- `GET /api/admin/gesture-flow/list`
  - `AdminGestureFlowQueryDTO`
- `GET /api/admin/gesture-flow/{id}`
  - `GestureFlowDetailVO`
```java
private Long id;
private String flowCode;
private String flowName;
private String flowType;
private String triggerMode;
private Integer status;
private Integer priority;
private Integer versionNo;
private Long startNodeId;
private Integer isBuiltin;
private String description;
private List<GestureFlowNodeVO> nodeList;
private List<GestureFlowOutputVO> outputList;
```
- `POST /api/admin/gesture-flow`
  - `GestureFlowAddDTO`
```java
private String flowCode;
private String flowName;
private String flowType;
private String triggerMode;
private Integer status;
private Integer priority;
private Integer isBuiltin;
private String description;
private List<GestureFlowNodeDTO> nodeList;
private List<GestureFlowOutputDTO> outputList;
```
- `PUT /api/admin/gesture-flow/{id}`
- `DELETE /api/admin/gesture-flow/{id}`

节点 DTO：

```java
private Long id;
private String nodeCode;
private String nodeName;
private Long parentNodeId;
private Long gestureLibraryId;
private Integer isStart;
private Integer isEnd;
private Integer nodeOrder;
private BigDecimal confidenceMin;
private Integer holdMs;
private Integer debounceMs;
private Integer cooldownMs;
private Integer requiredHits;
private Integer maxIntervalMs;
private Integer resetOnFail;
private Integer allowRepeat;
private String successNextStrategy;
private String failStrategy;
private String remark;
```

输出 DTO：

```java
private Long id;
private Long endNodeId;
private String outputType;
private String outputText;
private Long phraseTemplateId;
private String controlAction;
private String ttsText;
private String displayText;
```

## Recognition 运行接口

- `POST /api/recognition/session/start`
  - `RecognitionSessionStartDTO`
```java
private String clientPlatform;
```
  - `RecognitionSessionVO`
```java
private String sessionId;
private String state;
private Long configId;
private String configName;
```

- `POST /api/recognition/session/reset`
  - `RecognitionSessionResetDTO`
```java
private String sessionId;
```

- `POST /api/recognition/predict`
  - `RecognitionPredictDTO`
```java
private String sessionId;
private BigDecimal handConfidence;
private String clientPlatform;
private Long timestamp;
private List<LandmarkDTO> landmarks;
```

`LandmarkDTO`：

```java
private BigDecimal x;
private BigDecimal y;
private BigDecimal z;
```

  - `RecognitionResultVO`
```java
private Boolean matched;
private String sessionId;
private String state;
private Boolean locked;
private String matchedGestureCode;
private String matchedGestureName;
private Long matchedFlowId;
private String matchedFlowName;
private List<String> matchedNodePath;
private String outputType;
private String outputText;
private String displayText;
private String ttsText;
private String controlAction;
private BigDecimal confidenceScore;
private Long recordId;
```

- `GET /api/recognition/config`
- `GET /api/recognition/record/my`
  - `RecognitionRecordVO`

## Statistics 模块

- `GET /api/admin/statistics/overview`
  - `DashboardOverviewVO`
```java
private Long totalUsers;
private Long totalPosts;
private Long pendingPosts;
private Long totalComments;
private Long totalLearningArticles;
private Long totalGestureLibraries;
private Long totalGestureFlows;
private Long totalRecognitionRecords;
```

- `GET /api/admin/statistics/content-trend`
- `GET /api/admin/statistics/recognition-trend`
  - `TrendVO`
```java
private String date;
private Long count;
```

- `GET /api/admin/statistics/category-distribution`
- `GET /api/admin/statistics/gesture-hit-ranking`

## 首批优先类

建议你先落地这些类：

- `PostAddDTO`
- `PostQueryDTO`
- `AdminPostQueryDTO`
- `PostListVO`
- `PostDetailVO`
- `PostRejectDTO`
- `CommentAddDTO`
- `CommentVO`
- `CategoryAddDTO`
- `LearningAddDTO`
- `GestureLibraryAddDTO`
- `RecognitionConfigAddDTO`
- `GestureFlowAddDTO`
- `GestureFlowNodeDTO`
- `GestureFlowOutputDTO`
- `RecognitionPredictDTO`
- `RecognitionResultVO`
- `DashboardOverviewVO`
- `TrendVO`
