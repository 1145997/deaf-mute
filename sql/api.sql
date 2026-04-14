INSERT INTO `API_Info` (`作用`, `请求方式`, `接口`, `提交示例`, `成功示例`, `备注`) VALUES
('删除当前登录用户自己的失物或招领信息', 'DELETE', '/api/lostfound/{id}',
 'N/A',
 '{\"code\":200,\"message\":\"删除成功\",\"data\":null}',
 '需在请求头携带Authorization: Bearer token；只能删除本人发布的信息；路径参数为id');

 INSERT INTO `API_Info` (`作用`, `请求方式`, `接口`, `提交示例`, `成功示例`, `备注`) VALUES
('修改当前登录用户自己的失物或招领信息，修改后重新进入待审核', 'PUT', '/api/lostfound/{id}',
 '{\"type\":1,\"title\":\"重新修改后的校园卡寻物启事\",\"itemName\":\"校园卡\",\"categoryId\":6,\"brand\":\"\",\"color\":\"蓝色\",\"description\":\"我在第二教学楼附近丢失校园卡，如有拾到请联系。\",\"image\":\"http://8.133.16.236:9001/campus-lost-found/lostfound/82a1d4ee-e41d-411c-aaab-a76e30c8f7da.png\",\"eventTime\":\"2026-04-02T08:30:00\",\"eventPlace\":\"第二教学楼门口\",\"contactName\":\"张三\",\"contactPhone\":\"13900000001\",\"contactWechat\":\"zhangsan123\"}',
 '{\"code\":200,\"message\":\"修改成功，已重新进入审核\",\"data\":null}',
 '需在请求头携带Authorization: Bearer token；只能修改本人发布的信息；路径参数为id；修改后status重置为0待审核');