package com.silver.music.upload;

import static org.junit.jupiter.api.Assertions.*;

import com.silver.diary.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class UploadValidatorTest {
    @Test
    void fakeImage() {
        var error =
                assertThrows(
                        BusinessException.class,
                        () ->
                                UploadValidator.validate(
                                        new MockMultipartFile(
                                                "file",
                                                "image.png",
                                                "image/png",
                                                "<script>bad</script>".getBytes()),
                                        false));
        assertEquals(415, error.getCode());
    }

    @Test
    void realPng() throws Exception {
        var bytes = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(new java.awt.image.BufferedImage(2, 2, 1), "png", bytes);
        var result =
                UploadValidator.validate(
                        new MockMultipartFile(
                                "file", "../../script.exe", "text/plain", bytes.toByteArray()),
                        false);
        assertEquals(".png", result.extension());
        assertEquals("image/png", result.type());
    }

    @Test
    void size() {
        assertEquals(
                400,
                assertThrows(
                                BusinessException.class,
                                () ->
                                        UploadValidator.validate(
                                                new MockMultipartFile("file", new byte[0]), false))
                        .getCode());
        assertEquals(
                413,
                assertThrows(
                                BusinessException.class,
                                () ->
                                        UploadValidator.validate(
                                                new MockMultipartFile(
                                                        "file", new byte[5 * 1024 * 1024 + 1]),
                                                false))
                        .getCode());
    }

    @Test
    void dimensions() throws Exception {
        var bytes = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(new java.awt.image.BufferedImage(4097, 1, 1), "png", bytes);
        assertEquals(
                400,
                assertThrows(
                                BusinessException.class,
                                () ->
                                        UploadValidator.validate(
                                                new MockMultipartFile("file", bytes.toByteArray()),
                                                false))
                        .getCode());
    }

    @Test
    void fakeAudio() {
        assertEquals(
                415,
                assertThrows(
                                BusinessException.class,
                                () ->
                                        UploadValidator.validate(
                                                new MockMultipartFile(
                                                        "file",
                                                        "song.mp3",
                                                        "audio/mpeg",
                                                        "not audio".getBytes()),
                                                true))
                        .getCode());
    }
}
