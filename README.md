## 介绍

1. PatcherX是一款导出增量补丁文件的IDEA插件，因奇葩的增量部署而生，为开发者省去了很多繁琐操作。
2. 建议IDEA版本升级至2017或2017以上版本。

基于serical的代码改造而来，感谢：https://github.com/serical/patcher

**下载**：
在IDEA的插件管理中搜索 PatcherX
或者 https://github.com/fun90/patcher/raw/master/PatcherX.jar

主要功能：
1. 可以手动选择导出的修改过的编译文件或源码文件
2. 可以在Version Control中按修改日志导出的修改过的编译文件或源码文件
3. 可以手动选择文件或在Version Control中复制修改过的文件路径

Description:
1. you can manually select the exported modified compiled files or source code files.
2. you can export the modified compiled files or source code files in Version Control according to the revision log.
3. you can select the file manually or copy the path of the modified file in Version Control.


界面预览：

![img](doc/preview.png)

## 安装

![img](doc/2.png)

## 更新日志：

1. 2017-05-04 新增特性：如果工程只有一个Module则自动选中，无需再手动选择。
2. 2017-05-05 新增特性：自动编译后再导出
3. 2017-05-05 新增特性：完成导出后通知消息加入打开导出文件夹
4. 2017-05-07 新增Version Control面板中SVN Repository右键菜单
5. 2017-05-15 新增Version Control面板Local Changes右键菜单
6. 2017-05-22 新增删除已存在文件选项，勾选则在导出前删除到已存在的文件
7. 2019-08-23 支持Idea 2019.2，新增复制change list，优化自动选择module
8. 2020-01-17 新增导出时可选择是否是源文件
9. 2020-03-09 修复导出文件中包含空文件夹时的bug
10. 2020-09-21 改名为patcherX