package com.sinomed.service;

import com.sinomed.vo.PrintTemplatesView;

/**
 * 打印模板出源（desktop.md D7）：内置版式兜底，可被 data/printtemplates/ 下同名文件覆盖；
 * 模板更新只需改服务端文件，桌面壳按 version 增量拉取，不发壳版本。
 */
public interface PrintTemplateService {

    /** 当前生效的全部模板与内容摘要版本 */
    PrintTemplatesView list();
}
