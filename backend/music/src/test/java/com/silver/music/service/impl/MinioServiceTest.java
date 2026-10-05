package com.silver.music.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

class MinioServiceTest {
    private MinioClient client;
    private MinioServiceImpl service;

    @BeforeEach
    void setUp() {
        client = mock(MinioClient.class);
        service = new MinioServiceImpl(client);
        ReflectionTestUtils.setField(service, "bucketName", "test-music");
        ReflectionTestUtils.setField(service, "endpoint", "http://storage.invalid");
    }

    @Test
    void empty() {
        assertThrows(
                com.silver.diary.exception.BusinessException.class,
                () -> service.uploadFile(new MockMultipartFile("file", new byte[0]), "songs"));
        verifyNoInteractions(client);
    }

    @Test
    void upload() throws Exception {
        MockMultipartFile file = new MockMultipartFile("songs", "tone.wav", "audio/wav", wav());
        String url = service.uploadFile(file, "songs");
        ArgumentCaptor<PutObjectArgs> request = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(client).putObject(request.capture());
        assertEquals("test-music", request.getValue().bucket());
        assertTrue(request.getValue().object().startsWith("songs/"));
        assertTrue(request.getValue().object().endsWith(".wav"));
        assertEquals("http://storage.invalid/test-music/" + request.getValue().object(), url);
    }

    @Test
    void offline() throws Exception {
        when(client.putObject(any(PutObjectArgs.class)))
                .thenThrow(new IllegalStateException("storage-offline"));
        MockMultipartFile file = new MockMultipartFile("songs", "tone.wav", "audio/wav", wav());
        RuntimeException error =
                assertThrows(RuntimeException.class, () -> service.uploadFile(file, "songs"));
        assertEquals("文件上传失败", error.getMessage());
        assertEquals("storage-offline", error.getCause().getMessage());
    }

    private byte[] wav() {
        byte[] bytes = new byte[44];
        System.arraycopy(
                "RIFF".getBytes(java.nio.charset.StandardCharsets.US_ASCII), 0, bytes, 0, 4);
        System.arraycopy(
                "WAVE".getBytes(java.nio.charset.StandardCharsets.US_ASCII), 0, bytes, 8, 4);
        return bytes;
    }
}
