package org.bahmni.module.bahmnimsf.extension;

import org.bahmni.module.bahmnicore.service.ThumbnailGenerator;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Component
public class PatientDocumentThumbnailGenerator implements ThumbnailGenerator{

    public enum VideoFormats {

        OGG("OGG"), _3GP("3GPP"), MP4("MP4"), MPEG("MPEG"), WMV("WMV"), AVI("AVI"), MOV("MOV"), FLV("FLV"), WEBM("WEBM"), MKV("MKV");

        private final String value;

        VideoFormats(String value) {
            this.value = value;
        }
    }

    public boolean isFormatSupported(String givenFormat) {
        for (VideoFormats format : VideoFormats.values()) {
            if (givenFormat.toUpperCase().contains(format.value))
                return true;
        }
        return false;
    }


    public BufferedImage generateThumbnail(File outputVideoFile) throws IOException {
        BufferedImage bufferedImage = null;
        try {
            FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(outputVideoFile);
            frameGrabber.start();
            Frame grabKeyFrame = frameGrabber.grabKeyFrame();
            Java2DFrameConverter java2DFrameConverter = new Java2DFrameConverter();
            bufferedImage = java2DFrameConverter.convert(grabKeyFrame);
            frameGrabber.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return bufferedImage;
    }

}
