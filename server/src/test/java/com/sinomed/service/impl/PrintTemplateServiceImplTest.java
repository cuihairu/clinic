package com.sinomed.service.impl;

import com.sinomed.vo.PrintTemplatesView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 打印模板出源单测（desktop.md D7）：内置兜底、目录覆盖、版本随内容漂移。
 */
class PrintTemplateServiceImplTest {

    @TempDir
    Path tempDir;

    private PrintTemplateServiceImpl serviceAt(Path dir) {
        return new PrintTemplateServiceImpl(dir.toString());
    }

    @Test
    void servesBuiltinTemplatesWithStableVersion() {
        PrintTemplatesView first = serviceAt(tempDir).list();
        // 白名单两枚内置模板都有内容
        assertEquals(List.of("prescription", "receipt"),
                first.getTemplates().stream().map(t -> t.getName()).toList());
        first.getTemplates().forEach(t -> assertTrue(t.getContent().contains("BEGIN items")));
        // 无覆盖目录时版本稳定
        assertEquals(first.getVersion(), serviceAt(tempDir).list().getVersion());
    }

    @Test
    void overrideDirShadowsBuiltinAndShiftsVersion() throws Exception {
        String builtinVersion = serviceAt(tempDir).list().getVersion();
        Files.createDirectories(tempDir);
        Files.writeString(tempDir.resolve("receipt.html"),
                "<html><body>覆盖版小票 <!-- BEGIN items -->x<!-- END items --></body></html>");
        PrintTemplatesView overridden = serviceAt(tempDir).list();
        String receipt = overridden.getTemplates().stream()
                .filter(t -> t.getName().equals("receipt")).findFirst().orElseThrow().getContent();
        assertTrue(receipt.contains("覆盖版小票"));
        assertNotEquals(builtinVersion, overridden.getVersion());
        // 处方笺仍走内置
        String prescription = overridden.getTemplates().stream()
                .filter(t -> t.getName().equals("prescription")).findFirst().orElseThrow().getContent();
        assertFalse(prescription.contains("覆盖版小票"));
    }

    @Test
    void missingOverrideDirFallsBackToBuiltin() {
        // 覆盖目录不存在：全部回落内置
        PrintTemplatesView view = serviceAt(tempDir.resolve("empty-missing")).list();
        assertTrue(view.getTemplates().get(1).getContent().contains("BEGIN items"));
    }
}
