package com.silver.diary.upload;

import static org.junit.jupiter.api.Assertions.*;

import com.silver.diary.exception.BusinessException;
import com.silver.diary.support.TestData;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class UploadValidatorTest {
    @Test
    void empty() {
        assertThrows(BusinessException.class, () -> UploadValidator.validate(null, false));
        assertThrows(
                BusinessException.class,
                () -> UploadValidator.validate(new MockMultipartFile("file", new byte[0]), false));
    }

    @Test
    void fakeImage() {
        var file =
                new MockMultipartFile(
                        "file",
                        "fake.png",
                        "image/png",
                        "<html>fake</html>".getBytes(StandardCharsets.UTF_8));
        assertEquals(
                415,
                assertThrows(BusinessException.class, () -> UploadValidator.validate(file, false))
                        .getCode());
    }

    @Test
    void largeImage() {
        var file = new MockMultipartFile("file", new byte[5 * 1024 * 1024 + 1]);
        assertEquals(
                413,
                assertThrows(BusinessException.class, () -> UploadValidator.validate(file, false))
                        .getCode());
    }

    @Test
    void realPng() throws Exception {
        // 文件名和 MIME 都是假的，但字节是真实 PNG，应按实际内容识别。
        var content = UploadValidator.validate(TestData.png(), false);
        assertEquals("image/png", content.type());
        assertEquals(".png", content.extension());
        assertNotNull(ImageIO.read(new ByteArrayInputStream(content.bytes())));
    }

    @Test
    void wavHeader() throws Exception {
        byte[] bytes = new byte[44];
        System.arraycopy("RIFF".getBytes(StandardCharsets.US_ASCII), 0, bytes, 0, 4);
        System.arraycopy("WAVE".getBytes(StandardCharsets.US_ASCII), 0, bytes, 8, 4);
        assertEquals(
                "audio/wav",
                UploadValidator.validate(new MockMultipartFile("file", bytes), true).type());
    }

    @Test
    void badAudio() {
        var file = new MockMultipartFile("file", "bad.mp3", "audio/mpeg", new byte[44]);
        assertEquals(
                415,
                assertThrows(BusinessException.class, () -> UploadValidator.validate(file, true))
                        .getCode());
    }
}
