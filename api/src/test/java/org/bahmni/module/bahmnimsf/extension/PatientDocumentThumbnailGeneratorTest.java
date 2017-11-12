package org.bahmni.module.bahmnimsf.extension;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.awt.image.BufferedImage;
import java.io.File;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PatientDocumentThumbnailGenerator.class)
public class PatientDocumentThumbnailGeneratorTest {

    private PatientDocumentThumbnailGenerator patientDocumentThumbnailGenerator;

    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        patientDocumentThumbnailGenerator = new PatientDocumentThumbnailGenerator();
    }

    @Test
    public void shouldReturnTrueIfFileFormatIsSupported() throws Exception {
        String supportedFormat = "MKV";

        boolean isFileFormatSupported = patientDocumentThumbnailGenerator.isFormatSupported(supportedFormat);

        assertTrue(isFileFormatSupported);
    }

    @Test
    public void shouldReturnFalseIfFileFormatIsNotSupported() throws Exception {
        String unSupportedFormat = "randomFormat";

        boolean isFileFormatSupported = patientDocumentThumbnailGenerator.isFormatSupported(unSupportedFormat);

        assertFalse(isFileFormatSupported);
    }

    @Test
    public void shouldCreateThumbnailForVideo() throws Exception {
        File outputFile = mock(File.class);
        FFmpegFrameGrabber fFmpegFrameGrabber = mock(FFmpegFrameGrabber.class);
        PowerMockito.whenNew(FFmpegFrameGrabber.class).withArguments(outputFile).thenReturn(fFmpegFrameGrabber);
        Frame grabKeyFrame = mock(Frame.class);
        when(fFmpegFrameGrabber.grabKeyFrame()).thenReturn(grabKeyFrame);
        Java2DFrameConverter java2DFrameConverter = mock(Java2DFrameConverter.class);
        PowerMockito.whenNew(Java2DFrameConverter.class).withNoArguments().thenReturn(java2DFrameConverter);
        BufferedImage bufferedImage = mock(BufferedImage.class);
        when(java2DFrameConverter.convert(grabKeyFrame)).thenReturn(bufferedImage);

        BufferedImage generatedThumbnail = patientDocumentThumbnailGenerator.generateThumbnail(outputFile);

        assertNotNull(generatedThumbnail);
        assertEquals(bufferedImage, generatedThumbnail);
        verify(fFmpegFrameGrabber, times(1)).start();
        verify(fFmpegFrameGrabber, times(1)).grabKeyFrame();
        verify(java2DFrameConverter, times(1)).convert(grabKeyFrame);
        verify(fFmpegFrameGrabber, times(1)).stop();
    }

    @Test
    public void shouldThrowAnExceptionWhenUnableToGenerateThumbnailForVideo() throws Exception {
        File outputFile = mock(File.class);
        FFmpegFrameGrabber fFmpegFrameGrabber = mock(FFmpegFrameGrabber.class);
        PowerMockito.whenNew(FFmpegFrameGrabber.class).withArguments(outputFile).thenThrow(new Exception());
        expectedException.expect(RuntimeException.class);

        BufferedImage generatedThumbnail = patientDocumentThumbnailGenerator.generateThumbnail(outputFile);

        assertNull(generatedThumbnail);
    }
}