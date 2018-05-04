## 一、介绍

1. Patcher是一款导出增量补丁文件的IDEA插件，为开发者省去了很多繁琐操作。
2. 建议IDEA版本升级至2017或2017以上版本。
3. 插件只负责复制target下的文件，复制前不会自动Compile，请所以先手动Compile。



感谢：https://github.com/serical/patcher



更新日志：

2017.5.4 新增特性：如果工程只有一个Module则自动选中，无需再手动选择。



界面预览：

![img](doc/1.png)

## 二、安装

![img](doc/2.png)

## 二、使用

**有三处地方可以增量导出文件**

第1处：左侧Project文件栏选择文件或目录后右击鼠标（无需选Module）

![img](doc/3.png)

第2处：文本编辑器内右击鼠标（无需选Module）

![img](doc/4.png)

第3处：Version Control面板的Log（推荐使用，需要选Module，只支持IDEA 2017及以上版本）

![img](doc/5.png)