package com.example.qrgenerator.util;

import com.example.qrgenerator.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QRCodeGeneratorTest {

    private final QRCodeGenerator generator = new QRCodeGenerator();

    @Test
    void generateQRCode_validContent_producesPngImage() throws IOException {
        byte[] png = generator.generateQRCode("https://example.com", 300, 300);

        assertThat(png).isNotEmpty();
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
        assertThat(image).isNotNull();
    }

    @Test
    void generateQRCode_correctDimensions_matchesRequestedSize() throws IOException {
        byte[] png = generator.generateQRCode("Hello World", 200, 200);

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
        assertThat(image.getWidth()).isEqualTo(200);
        assertThat(image.getHeight()).isEqualTo(200);
    }

    @Test
    void generateQRCode_defaultSize_is300x300() throws IOException {
        byte[] png = generator.generateQRCode("default size test");

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
        assertThat(image.getWidth()).isEqualTo(300);
        assertThat(image.getHeight()).isEqualTo(300);
    }

    @Test
    void generateQRCode_emptyContent_throwsBadRequestException() {
        assertThatThrownBy(() -> generator.generateQRCode("", 300, 300))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void generateQRCode_nullContent_throwsBadRequestException() {
        assertThatThrownBy(() -> generator.generateQRCode(null, 300, 300))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void resolveValidSize_allowedSize_returnsSameSize() {
        assertThat(generator.resolveValidSize(500)).isEqualTo(500);
    }

    @Test
    void resolveValidSize_nullSize_returnsDefault() {
        assertThat(generator.resolveValidSize(null)).isEqualTo(QRCodeGenerator.DEFAULT_SIZE);
    }

    @Test
    void resolveValidSize_invalidSize_throwsBadRequestException() {
        assertThatThrownBy(() -> generator.resolveValidSize(999))
                .isInstanceOf(BadRequestException.class);
    }
}
