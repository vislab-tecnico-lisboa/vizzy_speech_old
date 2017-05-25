package com.github.woz_dialog.ros_woz_dialog_project;


// =================================================================

// Copyright (C) 2017 João Avelino (javelino@isr.ist.utl.pt)

// Copyright (C) 2011-2015 Pierre Lison (plison@ifi.uio.no)

// Permission is hereby granted, free of charge, to any person
// obtaining a copy of this software and associated documentation
// files (the "Software"), to deal in the Software without restriction,
// including without limitation the rights to use, copy, modify, merge,
// publish, distribute, sublicense, and/or sell copies of the Software,
// and to permit persons to whom the Software is furnished to do so,
// subject to the following conditions:

// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
// IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
// CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
// TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
// SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
// =================================================================


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.*;
import java.net.URI;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;




public class TTSHTTPClient {



    private String APP_ID = "app ID";
    private String APP_KEY = "128 bit";
    private String VOICE = "Joana";
    private String LANGUAGE = "por-PRT";

    private static short PORT = (short) 443;
    private static String HOSTNAME = "sslsandbox-nmdp.nuancemobility.net";
    private static String TTS = "/NMDPTTSCmdServlet/tts";

    AudioInputStream stream;
    AudioFormat format;
    byte[] data;

    int currentPos;


    /** HTTP client and URI for the speech recognition */
    CloseableHttpClient asrClient;
    URI asrURI;

    /** HTTP client and URI for the speech synthesis */
    CloseableHttpClient ttsClient;
    URI ttsURI;

    final static Logger log = Logger.getLogger("ros_woz_speaker");




    public TTSHTTPClient(String app_id, String app_key, String voice, String language)
    {
        APP_ID = app_id;
        APP_KEY = app_key;
        VOICE = voice;
        LANGUAGE = language;

        buildClients();

    }

    public void synthesise(String utterance) throws Exception {


        try {
            log.fine("calling Nuance server to synthesise utterance \"" + utterance
                    + "\"");

            HttpPost httppost = new HttpPost(ttsURI);
            httppost.addHeader("Content-Type", "text/plain");
            httppost.addHeader("Accept", "audio/x-wav;codec=pcm;bit=16;rate=16000");
            HttpEntity entity = new StringEntity(utterance);

            //HttpEntity entity = new ByteArrayEntity(utterance.getBytes("UTF-8"));

            httppost.setEntity(entity);

            HttpResponse response = ttsClient.execute(httppost);

            HttpEntity resEntity = response.getEntity();



            if (resEntity == null
                    || response.getStatusLine().getStatusCode() != 200) {
                System.out.println("Response status: " + response.getStatusLine());
                throw new Exception("Response status: " + response.getStatusLine());
            }

            format = new AudioFormat(16000, 16, 1, true, false);

            System.out.println(response.getStatusLine().getStatusCode());

            data = new byte[0];
            write(resEntity.getContent());
            httppost.releaseConnection();


            //Get the file path
            String basepath = System.getProperty("user.home");
            basepath = basepath + "/wav/" + LANGUAGE + "/" + VOICE;
            File dir = new File(basepath);


            if(!dir.exists())
            {
                // attempt to create the directory here
                boolean successful = dir.mkdirs();
                if (successful)
                {
                    // creating the directory succeeded
                    System.out.println("directory was created successfully");
                }
                else
                {
                    // creating the directory failed
                    log.severe("failed trying to create the directory");
                    throw new Exception("failed trying to create the directory");
                }

                return;

            }

            String fullpath = basepath + "/" + utterance.toLowerCase() + ".wav";

            //Record the sound
            generateFile(data, new File(fullpath));


            //Play the received sound

            SourceDataLine line =
                    AudioSystem.getSourceDataLine(format);

            line.open(format);
            line.start();

            rewind();

            int nBytesRead = 0;
            byte[] abData = new byte[512 * 16];

            while (nBytesRead != -1) {
                nBytesRead = read(abData, 0, abData.length);


                if (nBytesRead >= 0) {
                    line.write(abData, 0, nBytesRead);
                }
            }

            line.drain();
            if (line.isOpen()) {
                line.close();
            }


        }catch (LineUnavailableException e) {
            log.warning("Audio line is unavailable: " + e);
            throw e;
        }
        catch (Exception e) {
            throw e;
        }

    }



    /**
     * Builds the REST clients for speech recognition and synthesis.
     *
     * @
     */
    private void buildClients() {

        // Initialize the HTTP clients
        asrClient = HttpClientBuilder.create().build();
        ttsClient = HttpClientBuilder.create().build();

        try {

            URIBuilder builder = new URIBuilder();
            builder.setScheme("https");
            builder.setHost("dictation.nuancemobility.net");
            builder.setPort(443);
            builder.setPath("/NMDPAsrCmdServlet/dictation");
            builder.setParameter("appId", APP_ID);
            builder.setParameter("appKey", APP_KEY);
            builder.setParameter("id", "0000");
            asrURI = builder.build();
            builder.setHost("tts.nuancemobility.net");
            builder.setPath("/NMDPTTSCmdServlet/tts");
            builder.setParameter("ttsLang", LANGUAGE);
            builder.setParameter("voice", VOICE);
            ttsURI = builder.build();

        }
        catch (Exception e) {
            throw new RuntimeException("cannot build client: " + e);
        }
    }

    public void write(byte[] buffer) {

        byte[] newData = new byte[data.length + buffer.length];
        System.arraycopy(data, 0, newData, 0, data.length);
        System.arraycopy(buffer, 0, newData, data.length, buffer.length);
        data = newData;
    }

    public void write(InputStream stream) {
        try {
            int nRead;
            byte[] buffer = new byte[1024 * 16];
            while ((nRead = stream.read(buffer, 0, buffer.length)) != -1) {
                byte[] newData = new byte[data.length + nRead];
                System.arraycopy(data, 0, newData, 0, data.length);
                System.arraycopy(buffer, 0, newData, data.length, nRead);
                data = newData;
            }
        }
        catch (IOException e) {
            log.warning("Cannot write the stream to the speech data");
        }
    }

    public int read() {
        if (currentPos < data.length) {
            return data[currentPos++];
        }
        else {
            return -1;
        }
    }


    public int read(byte[] buffer, int offset, int length) {
        if (currentPos >= data.length) {
          return -1;
        }

        int i = 0;
        for (i = 0; i < length & (currentPos + i) < data.length; i++) {
            buffer[offset + i] = data[currentPos + i];
        }
        currentPos += i;
        return i;
    }

    public void rewind() {
        currentPos = 0;
    }

    public static void generateFile(byte[] data, File outputFile) {
        try {
            AudioInputStream audioStream = getAudioStream(data);
            if (outputFile.getName().endsWith("wav")) {
                int nb = AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE,
                        new FileOutputStream(outputFile));
                log.fine("WAV file written to " + outputFile.getCanonicalPath()
                        + " (" + (nb / 1000) + " kB)");
            }
            else {
                throw new RuntimeException("Unsupported encoding " + outputFile);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("could not generate file: " + e);
        }
    }


    public static AudioInputStream getAudioStream(byte[] byteArray) {
        try {
            try {
                ByteArrayInputStream byteStream =
                        new ByteArrayInputStream(byteArray);
                return AudioSystem.getAudioInputStream(byteStream);
            }
            catch (UnsupportedAudioFileException e) {
                byteArray = addWavHeader(byteArray);
                ByteArrayInputStream byteStream =
                        new ByteArrayInputStream(byteArray);
                return AudioSystem.getAudioInputStream(byteStream);
            }
        }
        catch (IOException | UnsupportedAudioFileException e) {
            throw new RuntimeException("cannot convert bytes to audio stream: " + e);
        }
    }

    private static byte[] addWavHeader(byte[] bytes) throws IOException {

        ByteBuffer bufferWithHeader = ByteBuffer.allocate(bytes.length + 44);
        bufferWithHeader.order(ByteOrder.LITTLE_ENDIAN);
        bufferWithHeader.put("RIFF".getBytes());
        bufferWithHeader.putInt(bytes.length + 36);
        bufferWithHeader.put("WAVE".getBytes());
        bufferWithHeader.put("fmt ".getBytes());
        bufferWithHeader.putInt(16);
        bufferWithHeader.putShort((short) 1);
        bufferWithHeader.putShort((short) 1);
        bufferWithHeader.putInt(16000);
        bufferWithHeader.putInt(32000);
        bufferWithHeader.putShort((short) 2);
        bufferWithHeader.putShort((short) 16);
        bufferWithHeader.put("data".getBytes());
        bufferWithHeader.putInt(bytes.length);
        bufferWithHeader.put(bytes);
        return bufferWithHeader.array();
    }


}
