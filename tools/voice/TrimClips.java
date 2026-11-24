import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Cuts the quiet tail off recorded clips (see trim.sh). The voices end every clip with up to
 * 1.5 s of near-silence, so "A." followed by "A is for Apple!" had a long gap. Each clip keeps
 * everything up to its last audible frame plus [KEEP_AFTER] frames (about 100 ms) of decay.
 * MP3 frames are cut whole, without re-encoding; trimming a trimmed clip changes nothing.
 *
 * Usage: java -cp jlayer.jar TrimClips.java folder [folder…]
 */
public class TrimClips {
    /** A frame louder than this (RMS, dBFS) is speech. */
    static final double SPEECH_DB = -60;
    static final int KEEP_AFTER = 4;

    public static void main(String[] args) throws Exception {
        int trimmed = 0, files = 0;
        double saved = 0;
        for (String dir : args) {
            File[] clips = new File(dir).listFiles((d, n) -> n.endsWith(".mp3"));
            if (clips == null) continue;
            for (File f : clips) {
                files++;
                byte[] data = Files.readAllBytes(f.toPath());
                List<Integer> offsets = frameOffsets(data);
                double[] levels = levels(data);
                int n = Math.min(offsets.size(), levels.length);
                int last = -1;
                for (int i = 0; i < n; i++) if (levels[i] > SPEECH_DB) last = i;
                if (last < 0) continue; // nothing audible: leave it alone
                int keep = last + 1 + KEEP_AFTER;
                if (keep >= offsets.size()) continue;
                int end = offsets.get(keep);
                try (FileOutputStream out = new FileOutputStream(f)) {
                    out.write(data, 0, end);
                }
                trimmed++;
                saved += (offsets.size() - keep) * frameSeconds(data);
            }
        }
        System.out.printf("trimmed %d of %d clips, %.0f s of silence removed%n", trimmed, files, saved);
    }

    /** Loudness of each decoded frame, in dBFS. */
    static double[] levels(byte[] data) throws Exception {
        Bitstream bs = new Bitstream(new ByteArrayInputStream(data));
        Decoder dec = new Decoder();
        List<Double> out = new ArrayList<>();
        Header h;
        while ((h = bs.readFrame()) != null) {
            SampleBuffer buf = (SampleBuffer) dec.decodeFrame(h, bs);
            short[] s = buf.getBuffer();
            int len = buf.getBufferLength();
            double sum = 0;
            for (int i = 0; i < len; i++) sum += (double) s[i] * s[i];
            double rms = Math.sqrt(sum / Math.max(1, len)) / 32768.0;
            out.add(20 * Math.log10(Math.max(rms, 1e-6)));
            bs.closeFrame();
        }
        return out.stream().mapToDouble(Double::doubleValue).toArray();
    }

    private static final int[] RATES_V1 = {44100, 48000, 32000};
    private static final int[] KBPS_V1 = {0, 32, 40, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320};
    private static final int[] KBPS_V2 = {0, 8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 160};

    /** Byte offset of each MPEG audio Layer III frame (the file must start with a frame). */
    static List<Integer> frameOffsets(byte[] d) {
        List<Integer> offsets = new ArrayList<>();
        int p = 0;
        while (p + 4 <= d.length) {
            int h = ((d[p] & 0xFF) << 24) | ((d[p + 1] & 0xFF) << 16) | ((d[p + 2] & 0xFF) << 8) | (d[p + 3] & 0xFF);
            if ((h >>> 21) != 0x7FF) break;
            int version = (h >> 19) & 3; // 3 = MPEG-1, 2 = MPEG-2, 0 = MPEG-2.5
            int bitrateIndex = (h >> 12) & 15;
            int rateIndex = (h >> 10) & 3;
            int padding = (h >> 9) & 1;
            if (bitrateIndex == 0 || bitrateIndex == 15 || rateIndex == 3) break;
            int rate = RATES_V1[rateIndex] / (version == 3 ? 1 : version == 2 ? 2 : 4);
            int kbps = (version == 3 ? KBPS_V1 : KBPS_V2)[bitrateIndex];
            int len = (version == 3 ? 144 : 72) * kbps * 1000 / rate + padding;
            offsets.add(p);
            p += len;
        }
        return offsets;
    }

    static double frameSeconds(byte[] d) {
        int version = (d[1] >> 3) & 3;
        int rateIndex = (d[2] >> 2) & 3;
        int rate = RATES_V1[rateIndex] / (version == 3 ? 1 : version == 2 ? 2 : 4);
        return (version == 3 ? 1152.0 : 576.0) / rate;
    }
}
