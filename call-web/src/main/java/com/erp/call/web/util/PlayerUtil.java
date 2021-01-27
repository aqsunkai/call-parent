package com.erp.call.web.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;

/**
 * @author sunkai
 * @description
 * @date 2021/1/27 18:59
 */
@Component
public class PlayerUtil {

    private static Logger logger = LoggerFactory.getLogger(HttpClientHelper.class);

    public void play() {
        InputStream in = null;
        AudioInputStream as = null;
        SourceDataLine sdl = null;
        try {
//            ClassPathResource cpr = new ClassPathResource("voice/player.wav");
//            in = new BufferedInputStream(cpr.getInputStream());
            in = new BufferedInputStream(this.getClass().getResourceAsStream("/voice/player.wav"));
            as = AudioSystem.getAudioInputStream(in);
            AudioFormat format = as.getFormat();
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            sdl = (SourceDataLine) AudioSystem.getLine(info);
            sdl.open(format);
            sdl.start();
            int nBytesRead = 0;
            byte[] abData = new byte[512];
            while (nBytesRead != -1) {
                nBytesRead = as.read(abData, 0, abData.length);
                if (nBytesRead >= 0) {
                    sdl.write(abData, 0, nBytesRead);
                }
            }
            sdl.drain();
        } catch (Exception e) {
            logger.error("voice drain error ", e);
        } finally {
            try {
                if (null != as) {
                    as.close();
                }
                if (null != in) {
                    in.close();
                }
                if (null != sdl) {
                    sdl.close();
                }
            } catch (Exception e) {
                logger.error("voice drain close error ", e);
            }
        }
    }
}
