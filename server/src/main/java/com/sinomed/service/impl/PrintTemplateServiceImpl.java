package com.sinomed.service.impl;

import com.sinomed.service.PrintTemplateService;
import com.sinomed.vo.PrintTemplateView;
import com.sinomed.vo.PrintTemplatesView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

/**
 * 打印模板出源实现：白名单模板逐个解析——先找覆盖目录（data/printtemplates/{name}.html），
 * 没有再回落 classpath 内置版式（printtemplates/{name}.html）；version 取全部内容的 SHA-256 前 16 位。
 */
@Service
public class PrintTemplateServiceImpl implements PrintTemplateService {

    /** 可下发的模板白名单：与桌面壳内置模板一一对应，杜绝目录被塞任意文件后外泄 */
    private static final List<String> TEMPLATE_NAMES = List.of("prescription", "receipt");

    private final Path overrideDir;

    public PrintTemplateServiceImpl(
            @Value("${sinomed.print.templates-dir:data/printtemplates}") String templatesDir) {
        this.overrideDir = Path.of(templatesDir);
    }

    @Override
    public PrintTemplatesView list() {
        List<PrintTemplateView> templates = new ArrayList<>();
        for (String name : TEMPLATE_NAMES) {
            templates.add(new PrintTemplateView(name, readTemplate(name)));
        }
        return new PrintTemplatesView(version(templates), templates);
    }

    /** 单个模板解析：覆盖目录优先，缺失或读失败回落内置（内置缺失属装配错误，直接抛出） */
    private String readTemplate(String name) {
        Path override = overrideDir.resolve(name + ".html");
        if (Files.isReadable(override)) {
            try {
                return FileCopyUtils.copyToString(Files.newBufferedReader(override));
            } catch (Exception e) {
                // 覆盖文件读不了（权限/编码问题）时按无覆盖处理，回落内置
            }
        }
        try {
            return FileCopyUtils.copyToString(new java.io.InputStreamReader(
                    getClass().getResourceAsStream("/printtemplates/" + name + ".html"),
                    java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("内置打印模板缺失：" + name, e);
        }
    }

    /** 内容摘要：SHA-256 前 16 位 hex，任何模板内容变化都会改变版本 */
    private String version(List<PrintTemplateView> templates) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (PrintTemplateView template : templates) {
                digest.update(template.getName().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                digest.update((byte) 0);
                digest.update(template.getContent().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }
            byte[] hash = digest.digest();
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                hex.append(String.format("%02x", hash[i]));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("计算模板版本失败", e);
        }
    }
}
