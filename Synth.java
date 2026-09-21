import javax.sound.sampled.*;
import java.util.Arrays;

public class Synth {

    // ============================================================
    // YM2612-INSPIRED SOFTWARE FM SYNTH
    //
    // 6 channels
    // 4 operators
    // integer phase accumulators
    // integer envelopes
    // FM modulation
    // feedback
    // stereo
    //
    // 44.1kHz / 16-bit / stereo
    // ============================================================

    public static final int SAMPLE_RATE = 44100;
    public static final int CHANNELS = 6;

    private static final int TABLE_BITS = 12;
    private static final int TABLE_SIZE = 1 << TABLE_BITS;
    private static final int TABLE_MASK = TABLE_SIZE - 1;

    /*
     * Phase:
     *
     * 0x00000000 = 0 degrees
     * 0x40000000 = 90 degrees
     * 0x80000000 = 180 degrees
     * 0xC0000000 = 270 degrees
     *
     * Full 32-bit phase wraps naturally.
     */
    private static final int PHASE_SHIFT = 16;

    /*
     * Operator output is approximately:
     *
     * -32768 ... +32767
     */
    private static final short[] SINE = new short[TABLE_SIZE];

    // ============================================================
    // SINE TABLE
    // ============================================================

    static {
        for (int i = 0; i < TABLE_SIZE; i++) {
            SINE[i] =
                (short)(Math.sin(
                    Math.PI * 2.0 * i / TABLE_SIZE
                ) * 32767.0);
        }
    }

    // ============================================================
    // AUDIO
    // ============================================================

    private static SourceDataLine line;
    private static Thread audioThread;

    private static volatile boolean playing;
    private static volatile Track currentTrack;

    private static final int MASTER_VOLUME = 72;

    // ============================================================
    // NOTE
    // ============================================================

    public static class Note {

        public final int midi;
        public final int ticks;
        public final int velocity;

        public Note(
            int midi,
            int ticks,
            int velocity
        ) {
            this.midi = midi;
            this.ticks = ticks;
            this.velocity = velocity;
        }
    }

    // ============================================================
    // OPERATOR
    // ============================================================

    static class Operator {

        /*
         * Ratio is Q8.8
         *
         * 256 = 1.00
         * 512 = 2.00
         * 128 = 0.50
         */
        int ratio;

        /*
         * Detune is Q8.8 percentage.
         *
         * 256 = +100%
         * 1 = +0.39%
         *
         * Usually very small.
         */
        int detune;

        /*
         * Output level:
         *
         * 0 ... 1024
         */
        int level;

        /*
         * Envelope times in milliseconds.
         */
        int attack;
        int decay;
        int sustain;
        int release;

        Operator(
            int ratio,
            int detune,
            int level,
            int attack,
            int decay,
            int sustain,
            int release
        ) {
            this.ratio = ratio;
            this.detune = detune;
            this.level = level;
            this.attack = attack;
            this.decay = decay;
            this.sustain = sustain;
            this.release = release;
        }
    }

    // ============================================================
    // INSTRUMENT
    // ============================================================

    static class Instrument {

        Operator[] op = new Operator[4];

        /*
         * 0:
         *
         * 4 -> 3 -> 2 -> 1
         *
         * 1:
         *
         * 2 -> 1
         * 4 -> 3
         *
         * 2:
         *
         * 2 -> 1
         * 4 -> 3
         *
         * 3:
         *
         * additive
         */
        int algorithm;

        /*
         * 0 ... 1024
         */
        int feedback;

        /*
         * LFO depth in Q8.8.
         */
        int lfoDepth;

        /*
         * LFO speed in Hz.
         */
        int lfoSpeed;

        /*
         * -256 = hard left
         * 0 = center
         * +256 = hard right
         */
        int pan;

        Instrument(
            int algorithm,
            int feedback,
            int lfoDepth,
            int lfoSpeed,
            int pan
        ) {
            this.algorithm = algorithm;
            this.feedback = feedback;
            this.lfoDepth = lfoDepth;
            this.lfoSpeed = lfoSpeed;
            this.pan = pan;
        }
    }

    // ============================================================
    // TRACK
    // ============================================================

    public static class Track {

        int bpm;
        Note[][] voices;
        Instrument[] instruments;
        boolean loop;

        Track(
            int bpm,
            Note[][] voices,
            Instrument[] instruments,
            boolean loop
        ) {
            this.bpm = bpm;
            this.voices = voices;
            this.instruments = instruments;
            this.loop = loop;
        }
    }

    // ============================================================
    // INSTRUMENTS
    // ============================================================

    /*
     * DEEP BASS
     *
     * 4 -> 3 -> 2 -> 1
     *
     * OP1 is the actual bass carrier.
     *
     * OP2/3/4 progressively shape the harmonic content.
     */
    static final Instrument BASS =
        new Instrument(
            0,
            180,
            4,
            5,
            -30
        );

    static {

        BASS.op[0] =
            new Operator(
                256,
                0,
                1024,
                3,
                90,
                720,
                70
            );

        BASS.op[1] =
            new Operator(
                512,
                -2,
                900,
                2,
                60,
                150,
                50
            );

        BASS.op[2] =
            new Operator(
                128,
                0,
                800,
                2,
                70,
                100,
                50
            );

        BASS.op[3] =
            new Operator(
                768,
                3,
                500,
                1,
                45,
                40,
                40
            );
    }

    // ============================================================
    // FM STAB
    // ============================================================

    static final Instrument STAB =
        new Instrument(
            1,
            100,
            2,
            5,
            -80
        );

    static {

        STAB.op[0] =
            new Operator(
                256,
                0,
                900,
                1,
                90,
                180,
                50
            );

        STAB.op[1] =
            new Operator(
                512,
                0,
                900,
                1,
                50,
                50,
                40
            );

        STAB.op[2] =
            new Operator(
                256,
                2,
                750,
                1,
                70,
                120,
                40
            );

        STAB.op[3] =
            new Operator(
                768,
                -3,
                800,
                1,
                45,
                40,
                40
            );
    }

    // ============================================================
    // DARK LEAD
    // ============================================================

    static final Instrument LEAD =
        new Instrument(
            0,
            130,
            4,
            6,
            70
        );

    static {

        LEAD.op[0] =
            new Operator(
                256,
                0,
                900,
                5,
                120,
                700,
                120
            );

        LEAD.op[1] =
            new Operator(
                256,
                2,
                700,
                2,
                100,
                180,
                80
            );

        LEAD.op[2] =
            new Operator(
                512,
                -2,
                500,
                2,
                80,
                120,
                70
            );

        LEAD.op[3] =
            new Operator(
                768,
                3,
                400,
                2,
                70,
                100,
                60
            );
    }

    // ============================================================
    // HIGH FM
    // ============================================================

    static final Instrument HIGH =
        new Instrument(
            0,
            150,
            3,
            7,
            100
        );

    static {

        HIGH.op[0] =
            new Operator(
                256,
                0,
                650,
                1,
                60,
                100,
                40
            );

        HIGH.op[1] =
            new Operator(
                1792,
                2,
                900,
                1,
                30,
                30,
                30
            );

        HIGH.op[2] =
            new Operator(
                512,
                -2,
                450,
                1,
                50,
                50,
                30
            );

        HIGH.op[3] =
            new Operator(
                1280,
                3,
                700,
                1,
                30,
                30,
                30
            );
    }

    // ============================================================
    // PAD
    // ============================================================

    static final Instrument PAD =
        new Instrument(
            2,
            30,
            1,
            3,
            0
        );

    static {

        PAD.op[0] =
            new Operator(
                256,
                0,
                500,
                180,
                350,
                550,
                400
            );

        PAD.op[1] =
            new Operator(
                256,
                1,
                300,
                150,
                300,
                350,
                300
            );

        PAD.op[2] =
            new Operator(
                512,
                0,
                350,
                150,
                300,
                300,
                300
            );

        PAD.op[3] =
            new Operator(
                768,
                0,
                250,
                150,
                250,
                200,
                300
            );
    }

    // ============================================================
    // TRACK
    // ============================================================

    public static final Track LEVEL2 =
        createLevel2();

    // ============================================================
    // TRACK CREATION
    // ============================================================

    static Track createLevel2() {

        final int S = 1;
        final int E = 2;
        final int Q = 4;
        final int H = 8;

        // ========================================================
        // BASS
        // ========================================================

        Note[] bass =
            concat(

                n(40,E,127),
                n(40,E,115),
                n(43,E,125),
                n(47,E,118),
                n(45,E,120),
                n(43,E,110),
                n(40,E,127),
                n(38,E,115),

                n(40,E,127),
                n(40,E,115),
                n(43,E,125),
                n(47,E,118),
                n(50,E,125),
                n(47,E,115),
                n(43,E,120),
                n(38,E,115),

                n(40,E,127),
                n(40,E,115),
                n(43,E,125),
                n(45,E,118),
                n(47,E,127),
                n(43,E,118),
                n(40,E,127),
                n(36,E,120),

                n(38,E,127),
                n(38,E,115),
                n(40,E,125),
                n(43,E,118),
                n(47,E,127),
                n(45,E,115),
                n(43,E,120),
                n(35,E,125),

                n(40,E,127),
                n(40,E,115),
                n(43,E,127),
                n(47,E,120),
                n(50,E,127),
                n(47,E,118),
                n(43,E,123),
                n(40,E,115),

                n(38,E,127),
                n(38,E,115),
                n(40,E,125),
                n(43,E,120),
                n(47,E,127),
                n(43,E,115),
                n(40,E,125),
                n(36,E,120),

                n(40,E,127),
                n(43,E,118),
                n(47,E,127),
                n(50,E,120),
                n(52,E,127),
                n(50,E,118),
                n(47,E,125),
                n(43,E,115),

                n(40,E,127),
                n(38,E,118),
                n(36,E,127),
                n(38,E,118),
                n(40,E,127),
                n(43,E,118),
                n(47,E,127),
                n(38,E,120),

                n(40,E,127),
                n(40,E,115),
                n(43,E,125),
                n(47,E,118),
                n(45,E,120),
                n(43,E,110),
                n(40,E,127),
                n(38,E,115),

                n(40,E,127),
                n(40,E,115),
                n(43,E,125),
                n(47,E,118),
                n(50,E,125),
                n(47,E,115),
                n(43,E,120),
                n(38,E,115),

                n(40,E,127),
                n(43,E,118),
                n(45,E,127),
                n(47,E,118),
                n(50,E,127),
                n(47,E,118),
                n(43,E,123),
                n(40,E,115),

                n(38,E,127),
                n(40,E,118),
                n(43,E,127),
                n(47,E,118),
                n(50,E,127),
                n(47,E,118),
                n(43,E,123),
                n(36,E,127)
            );

        // ========================================================
        // STABS
        // ========================================================

        Note[] stabs =
            concat(

                rest(Q),
                n(52,Q,110),
                rest(Q),
                n(55,Q,115),

                n(50,Q,105),
                rest(Q),
                n(53,Q,110),
                rest(Q),

                rest(Q),
                n(52,Q,110),
                rest(Q),
                n(55,Q,115),

                n(50,Q,105),
                rest(Q),
                n(47,Q,115),
                rest(Q),

                rest(Q),
                n(52,Q,110),
                n(55,Q,115),
                rest(Q),

                n(50,Q,105),
                rest(Q),
                n(53,Q,110),
                rest(Q),

                rest(Q),
                n(55,Q,115),
                rest(Q),
                n(59,Q,120),

                n(57,Q,110),
                rest(Q),
                n(50,Q,115),
                rest(Q),

                rest(Q),
                n(52,Q,110),
                rest(Q),
                n(55,Q,115),

                n(50,Q,105),
                rest(Q),
                n(53,Q,110),
                rest(Q),

                rest(Q),
                n(52,Q,110),
                rest(Q),
                n(55,Q,115),

                n(50,Q,105),
                rest(Q),
                n(47,Q,115),
                rest(Q),

                rest(Q),
                n(55,Q,115),
                n(57,Q,110),
                rest(Q),

                n(59,Q,115),
                rest(Q),
                n(55,Q,110),
                rest(Q),

                rest(Q),
                n(52,Q,115),
                rest(Q),
                n(47,Q,120),

                n(50,Q,110),
                rest(Q),
                n(47,Q,125),
                rest(Q)
            );

        // ========================================================
        // LEAD
        // ========================================================

        Note[] lead =
            concat(

                rest(H),
                n(64,E,105),
                n(67,E,110),
                n(69,E,115),
                n(67,E,105),

                rest(E),
                n(64,E,110),
                n(62,E,105),
                n(60,E,115),

                rest(Q),
                n(64,E,110),
                n(67,E,115),
                n(69,E,110),
                n(71,E,120),

                n(69,E,110),
                n(67,E,105),
                n(64,E,110),
                n(62,E,105),

                rest(H),
                n(64,E,110),
                n(67,E,115),
                n(72,E,120),

                n(71,E,110),
                n(69,E,105),
                n(67,E,110),
                n(64,E,105),

                rest(Q),
                n(62,E,105),
                n(64,E,110),
                n(67,E,115),
                n(69,E,120),

                n(67,E,110),
                n(64,E,105),
                n(62,E,110),
                n(60,E,105),

                n(64,E,110),
                n(67,E,115),
                n(69,E,120),
                n(72,E,125),

                n(74,E,120),
                n(72,E,110),
                n(69,E,115),
                n(67,E,105),

                n(69,E,110),
                n(71,E,115),
                n(74,E,120),
                n(76,E,125),

                n(74,E,115),
                n(71,E,110),
                n(69,E,115),
                n(67,E,105),

                n(64,E,110),
                n(67,E,115),
                n(69,E,120),
                n(71,E,115),

                n(69,E,110),
                n(67,E,105),
                n(64,E,110),
                n(62,E,105),

                n(60,E,110),
                n(62,E,115),
                n(64,E,120),
                n(67,E,125),

                n(64,E,110),
                n(62,E,105),
                n(60,H,120)
            );

        // ========================================================
        // HIGH
        // ========================================================

        Note[] high =
            concat(

                rest(Q),
                n(76,E,60),
                rest(E),
                n(79,E,65),
                rest(E),

                n(74,E,60),
                rest(E),
                n(77,E,65),
                rest(E),

                n(72,E,65),
                rest(E),
                n(76,E,60),
                rest(E),

                n(79,E,65),
                rest(E),
                n(83,E,70),
                rest(E),

                n(81,E,65),
                rest(E),
                n(79,E,60),
                rest(E),

                n(76,E,65),
                rest(E),
                n(74,E,60),
                rest(E),

                n(72,E,65),
                rest(E),
                n(76,E,70),
                rest(E),

                n(79,E,65),
                rest(E),
                n(83,E,70),
                rest(E),

                n(76,E,65),
                n(79,E,60),
                n(83,E,70),
                n(79,E,60),

                n(74,E,65),
                n(77,E,60),
                n(81,E,70),
                n(77,E,60),

                n(72,E,65),
                n(76,E,60),
                n(79,E,70),
                n(76,E,60),

                n(74,E,65),
                n(77,E,60),
                n(81,E,70),
                n(77,E,60),

                n(76,E,65),
                n(79,E,60),
                n(83,E,70),
                n(86,E,60),

                n(83,E,65),
                n(79,E,60),
                n(76,E,70),
                n(74,E,60),

                n(72,E,65),
                n(76,E,60),
                n(79,E,70),
                n(83,E,60),

                n(79,E,65),
                n(76,E,60),
                n(72,H,70)
            );

        // ========================================================
        // PAD
        // ========================================================

        Note[] pad =
            concat(

                n(52,H,45),
                n(55,H,45),

                n(50,H,45),
                n(53,H,45),

                n(48,H,45),
                n(52,H,45),

                n(47,H,45),
                n(50,H,45),

                n(52,H,45),
                n(55,H,45),

                n(50,H,45),
                n(53,H,45),

                n(48,H,45),
                n(52,H,45),

                n(47,H,45),
                n(50,H,45),

                n(52,H,45),
                n(55,H,45),

                n(50,H,45),
                n(53,H,45),

                n(48,H,45),
                n(52,H,45),

                n(47,H,45),
                n(50,H,45),

                n(52,H,45),
                n(55,H,45),

                n(50,H,45),
                n(53,H,45),

                n(48,H,45),
                n(52,H,45),

                n(47,H,45),
                n(50,H,45)
            );

        // ========================================================
        // DRUMS
        // ========================================================

        Note[] drums =
            concat(

                drumBar(),
                drumBar(),
                drumBar(),
                drumFill(),

                drumBar(),
                drumBar(),
                drumBar(),
                drumFill(),

                drumBar(),
                drumBar(),
                drumBar(),
                drumFill(),

                drumBar(),
                drumBar(),
                drumBar(),
                drumFill()
            );

        return new Track(
            128,

            new Note[][] {
                bass,
                stabs,
                lead,
                high,
                pad,
                drums
            },

            new Instrument[] {
                BASS,
                STAB,
                LEAD,
                HIGH,
                PAD,
                null
            },

            true
        );
    }

    // ============================================================
    // DRUM BAR
    // ============================================================

    static Note[] drumBar() {

        return concat(

            n(36,2,127),
            n(42,2,65),
            n(42,2,45),
            n(38,2,120),
            n(42,2,65),
            n(36,2,115),
            n(42,2,55),
            n(42,2,45),

            n(36,2,125),
            n(42,2,65),
            n(38,2,120),
            n(42,2,55),
            n(36,2,115),
            n(42,2,65),
            n(42,2,50),
            n(38,2,115)
        );
    }

    // ============================================================
    // DRUM FILL
    // ============================================================

    static Note[] drumFill() {

        return concat(

            n(36,2,127),
            n(42,2,65),
            n(38,2,120),
            n(42,2,55),

            n(45,2,90),
            n(42,2,55),
            n(47,2,100),
            n(42,2,55),

            n(48,2,105),
            n(42,2,60),
            n(50,2,110),
            n(42,2,60),

            n(36,2,127),
            n(38,2,120),
            n(36,2,127),
            n(38,2,127)
        );
    }

    // ============================================================
    // NOTE HELPERS
    // ============================================================

    static Note n(
        int midi,
        int ticks,
        int velocity
    ) {
        return new Note(
            midi,
            ticks,
            velocity
        );
    }

    static Note rest(int ticks) {
        return new Note(
            -1,
            ticks,
            0
        );
    }

    static Note[] concat(Note[]... arrays) {

        int length = 0;

        for (Note[] a : arrays)
            length += a.length;

        Note[] result =
            new Note[length];

        int p = 0;

        for (Note[] a : arrays) {

            System.arraycopy(
                a,
                0,
                result,
                p,
                a.length
            );

            p += a.length;
        }

        return result;
    }

    static Note[] concat(Note... notes) {
        return notes;
    }

    // ============================================================
    // PLAY
    // ============================================================

    public static synchronized void play(
        Track track
    ) {

        stop();

        currentTrack = track;
        playing = true;

        audioThread =
            new Thread(
                () -> audioLoop(),
                "Synth-Audio"
            );

        audioThread.setDaemon(true);
        audioThread.start();
    }

    // ============================================================
    // STOP
    // ============================================================

    public static synchronized void stop() {

        playing = false;
        currentTrack = null;

        if (line != null) {

            try {

                line.stop();
                line.flush();
                line.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            line = null;
        }
    }

    // ============================================================
    // AUDIO LOOP
    // ============================================================

    static void audioLoop() {

        try {

            AudioFormat format =
                new AudioFormat(
                    SAMPLE_RATE,
                    16,
                    2,
                    true,
                    false
                );

            line =
                AudioSystem.getSourceDataLine(
                    format
                );

            line.open(format);
            line.start();

            Track track = currentTrack;

            if (track != null)
                render(track);

        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            if (line != null) {

                try {
                    line.stop();
                    line.close();
                } catch (Exception e) {
                }

                line = null;
            }
        }
    }

    // ============================================================
    // RENDER
    // ============================================================

    static void render(Track track) {

        /*
         * 1 tick = 1/16 note.
         *
         * Samples per tick:
         *
         * 44100 * 60 / BPM / 4
         *
         * Keep this integer.
         */

        int samplesPerTick =
            mathlib.Divider(
                SAMPLE_RATE * 60,
                track.bpm * 4
            );

        int totalTicks =
            getTrackLength(track);

        int totalSamples =
            totalTicks * samplesPerTick;

        byte[] buffer =
            new byte[4096 * 4];

        VoiceState[] state =
            new VoiceState[CHANNELS];

        for (int i = 0; i < CHANNELS; i++)
            state[i] = new VoiceState();

        while (playing) {

            int rendered = 0;

            while (
                rendered < totalSamples &&
                playing
            ) {

                int samples =
                    Math.min(
                        4096,
                        totalSamples - rendered
                    );

                int bytes =
                    samples * 4;

                Arrays.fill(
                    buffer,
                    0,
                    bytes,
                    (byte)0
                );

                for (
                    int i = 0;
                    i < samples;
                    i++
                ) {

                    int left = 0;
                    int right = 0;

                    for (
                        int ch = 0;
                        ch < CHANNELS;
                        ch++
                    ) {

                        VoiceState v =
                            state[ch];

                        updateVoice(
                            track,
                            ch,
                            samplesPerTick,
                            v
                        );

                        int sample =
                            renderVoice(
                                track,
                                ch,
                                v
                            );

                        if (ch == 5) {

                            left += sample;
                            right += sample;

                        } else {

                            Instrument inst =
                                track.instruments[ch];

                            int pan =
                                inst.pan;

                            left +=
                                mathlib.Divider(
                                    sample *
                                    (256 - pan),
                                    256
                                );

                            right +=
                                mathlib.Divider(
                                    sample *
                                    (256 + pan),
                                    256
                                );
                        }
                    }

                    /*
                     * Master gain.
                     */
                    left =
                        mathlib.Divider(
                            left *
                            MASTER_VOLUME,
                            100
                        );

                    right =
                        mathlib.Divider(
                            right *
                            MASTER_VOLUME,
                            100
                        );

                    /*
                     * Cheap soft saturation.
                     *
                     * Keeps the bass from hard-clipping.
                     */
                    left = saturate(left);
                    right = saturate(right);

                    short l =
                        clamp16(left);

                    short r =
                        clamp16(right);

                    int p =
                        i * 4;

                    buffer[p] =
                        (byte)(l & 0xFF);

                    buffer[p + 1] =
                        (byte)((l >> 8) & 0xFF);

                    buffer[p + 2] =
                        (byte)(r & 0xFF);

                    buffer[p + 3] =
                        (byte)((r >> 8) & 0xFF);
                }

                line.write(
                    buffer,
                    0,
                    bytes
                );

                rendered += samples;
            }

            if (!track.loop)
                break;
        }
    }

    // ============================================================
    // VOICE STATE
    // ============================================================

    static class VoiceState {

        int noteIndex;

        int currentMidi = -1;

        int noteSamples;

        int noteLength;

        int phase0;
        int phase1;
        int phase2;
        int phase3;

        int previousOutput;

        int env0;
        int env1;
        int env2;
        int env3;

        int envStage0;
        int envStage1;
        int envStage2;
        int envStage3;

        int attackStep0;
        int attackStep1;
        int attackStep2;
        int attackStep3;

        int decayStep0;
        int decayStep1;
        int decayStep2;
        int decayStep3;

        int releaseStep0;
        int releaseStep1;
        int releaseStep2;
        int releaseStep3;

        int phaseInc0;
        int phaseInc1;
        int phaseInc2;
        int phaseInc3;

        int tickCounter;
    }

 // ============================================================
    // UPDATE VOICE
    // ============================================================

    static void updateVoice(
        Track track,
        int channel,
        int samplesPerTick,
        VoiceState state
    ) {

        Note[] notes =
            track.voices[channel];

        if (notes.length == 0) {
            state.noteIndex = -1;
            state.currentMidi = -1;
            state.noteLength = 0;
            return;
        }

        if (state.noteIndex < 0) {
            if (track.loop) {
                state.noteIndex = 0;
                startNote(
                    track,
                    channel,
                    state,
                    samplesPerTick
                );
            } else {
                return;
            }
        }

        if (state.noteLength <= 0) {
            startNote(
                track,
                channel,
                state,
                samplesPerTick
            );
        }

        state.noteSamples++;

        if (channel != 5) {
            Instrument inst =
                track.instruments[channel];

            if (inst != null)
                updateEnvelope(
                    state,
                    inst
                );
        }

        if (state.noteSamples >= state.noteLength) {

            state.noteIndex++;

            if (state.noteIndex >= notes.length) {
                if (track.loop) {
                    state.noteIndex = 0;
                } else {
                    state.noteIndex = -1;
                    state.currentMidi = -1;
                    state.noteLength = 0;
                    return;
                }
            }

            startNote(
                track,
                channel,
                state,
                samplesPerTick
            );
        }
    }

    // ============================================================
    // START NOTE
    // ============================================================


    // ============================================================

    static void startNote(
        Track track,
        int channel,
        VoiceState state,
        int samplesPerTick
    ) {

        Note[] notes =
            track.voices[channel];

        if (
            state.noteIndex < 0 ||
            state.noteIndex >= notes.length
        )
            return;

        Note note =
            notes[state.noteIndex];

        state.noteSamples = 0;

        state.noteLength =
            note.ticks *
            samplesPerTick;

        state.currentMidi =
            note.midi;

        state.phase0 = 0;
        state.phase1 = 0;
        state.phase2 = 0;
        state.phase3 = 0;

        state.previousOutput = 0;

        if (note.midi < 0)
            return;

        if (channel == 5)
            return;

        Instrument inst =
            track.instruments[channel];

        state.env0 = 0;
        state.env1 = 0;
        state.env2 = 0;
        state.env3 = 0;

        state.envStage0 = 0;
        state.envStage1 = 0;
        state.envStage2 = 0;
        state.envStage3 = 0;

        state.attackStep0 =
            envelopeAttack(
                inst.op[0]
            );

        state.attackStep1 =
            envelopeAttack(
                inst.op[1]
            );

        state.attackStep2 =
            envelopeAttack(
                inst.op[2]
            );

        state.attackStep3 =
            envelopeAttack(
                inst.op[3]
            );

        state.decayStep0 =
            envelopeDecay(
                inst.op[0]
            );

        state.decayStep1 =
            envelopeDecay(
                inst.op[1]
            );

        state.decayStep2 =
            envelopeDecay(
                inst.op[2]
            );

        state.decayStep3 =
            envelopeDecay(
                inst.op[3]
            );

        state.releaseStep0 =
            envelopeRelease(
                inst.op[0]
            );

        state.releaseStep1 =
            envelopeRelease(
                inst.op[1]
            );

        state.releaseStep2 =
            envelopeRelease(
                inst.op[2]
            );

        state.releaseStep3 =
            envelopeRelease(
                inst.op[3]
            );

        int freq =
            midiFrequency(note.midi);

        state.phaseInc0 =
            operatorFrequency(
                freq,
                inst.op[0]
            );

        state.phaseInc1 =
            operatorFrequency(
                freq,
                inst.op[1]
            );

        state.phaseInc2 =
            operatorFrequency(
                freq,
                inst.op[2]
            );

        state.phaseInc3 =
            operatorFrequency(
                freq,
                inst.op[3]
            );
    }

    // ============================================================
    // RENDER VOICE
    // ============================================================

    static int renderVoice(
        Track track,
        int channel,
        VoiceState state
    ) {

        if (
            state.currentMidi < 0 ||
            state.noteLength <= 0
        )
            return 0;

        Note note =
            track.voices[channel]
                [state.noteIndex];

        if (note.midi < 0)
            return 0;

        if (channel == 5)
            return renderDrum(
                note.midi,
                state.noteSamples,
                note.velocity
            );

        Instrument inst =
            track.instruments[channel];

        state.phase0 +=
            state.phaseInc0;

        state.phase1 +=
            state.phaseInc1;

        state.phase2 +=
            state.phaseInc2;

        state.phase3 +=
            state.phaseInc3;

        int o3 =
            operatorOutput(
                state.phase3,
                state.env3,
                inst.op[3].level
            );

        /*
         * Feedback is applied directly
         * to operator 4.
         */
        int feedback =
            mathlib.Divider(
                state.previousOutput *
                inst.feedback,
                1024
            );

        o3 =
            sinePhase(
                state.phase3 +
                (feedback << 8)
            );

        o3 =
            mathlib.Divider(
                o3 *
                state.env3 *
                inst.op[3].level,
                65535 * 1024
            );

        int o2 =
            sinePhase(
                state.phase2 +
                (o3 << 7)
            );

        o2 =
            mathlib.Divider(
                o2 *
                state.env2 *
                inst.op[2].level,
                65535 * 1024
            );

        int o1 =
            sinePhase(
                state.phase1 +
                (o2 << 7)
            );

        o1 =
            mathlib.Divider(
                o1 *
                state.env1 *
                inst.op[1].level,
                65535 * 1024
            );

        int o0 =
            sinePhase(
                state.phase0 +
                (o1 << 7)
            );

        o0 =
            mathlib.Divider(
                o0 *
                state.env0 *
                inst.op[0].level,
                65535 * 1024
            );

        state.previousOutput =
            o3;

        /*
         * Algorithm.
         */
        int output;

        if (inst.algorithm == 0) {

            /*
             * 4 -> 3 -> 2 -> 1
             */
            output = o0;

        } else if (inst.algorithm == 1) {

            /*
             * 2 -> 1
             * 4 -> 3
             */
            output =
                (o0 + o2) >> 1;

        } else if (inst.algorithm == 2) {

            /*
             * Two carriers.
             */
            output =
                (o0 + o2) >> 1;

        } else {

            /*
             * Additive.
             */
            output =
                (o0 + o1 + o2 + o3) >> 2;
        }

        output =
            mathlib.Divider(
                output *
                note.velocity,
                127
            );

        /*
         * Bass gets more energy than
         * the other melodic channels.
         */
        if (channel == 0)
            output =
                mathlib.Divider(
                    output * 115,
                    100
                );

        return output;
    }

    // ============================================================
    // OPERATOR OUTPUT
    // ============================================================

    static int operatorOutput(
        int phase,
        int envelope,
        int level
    ) {

        int output =
            sinePhase(phase);

        output =
            mathlib.Divider(
                output *
                envelope *
                level,
                65535 * 1024
            );

        return output;
    }

    // ============================================================
    // SINE
    // ============================================================

    static int sinePhase(int phase) {

        int index =
            phase >>> (
                32 - TABLE_BITS
            );

        return SINE[
            index & TABLE_MASK
        ];
    }

    // ============================================================
    // MIDI FREQUENCY
    //
    // Frequency is stored as Hz * 100.
    //
    // This avoids Math.pow() while playing.
    // ============================================================

    static int midiFrequency(int midi) {

        /*
         * C-1 through G9-ish range.
         *
         * Generated from a fixed-point
         * semitone multiplier.
         *
         * A4 = 44000.
         */

        if (midi < 0)
            return 0;

        if (midi > 127)
            midi = 127;

        int frequency = 818;

        /*
         * MIDI 0 = approximately 8.18 Hz.
         *
         * 1.059463 = Q20:
         * approximately 1110958.
         */

        for (int i = 0; i < midi; i++) {

            frequency =
                (frequency * 1110958) >> 20;
        }

        return frequency;
    }

    // ============================================================
    // OPERATOR FREQUENCY
    // ============================================================

    static int operatorFrequency(
        int frequency,
        Operator op
    ) {

        /*
         * frequency is Hz * 100.
         *
         * ratio is Q8.8.
         */

        int result =
            (frequency * op.ratio) >> 8;

        /*
         * Detune.
         *
         * detune is small Q8.8.
         */
        result =
            result +
            ((result * op.detune) >> 16);

        /*
         * Convert Hz to
         * 32-bit phase increment.
         *
         * freq / 44100 * 2^32
         *
         * freq is /100.
         */

        long temp =
            ((long)result << 32) /
            (SAMPLE_RATE * 100L);

        return (int)temp;
    }

    // ============================================================
    // ENVELOPE
    // ============================================================

    static int envelopeAttack(
        Operator op
    ) {

        if (op.attack <= 0)
            return 65535;

        return mathlib.Divider(
            65535,
            op.attack *
            SAMPLE_RATE /
            1000
        );
    }

    static int envelopeDecay(
        Operator op
    ) {

        if (op.decay <= 0)
            return 65535;

        int target =
            op.sustain;

        return mathlib.Divider(
            65535 - target,
            op.decay *
            SAMPLE_RATE /
            1000
        );
    }

    static int envelopeRelease(
        Operator op
    ) {

        if (op.release <= 0)
            return 65535;

        return mathlib.Divider(
            65535,
            op.release *
            SAMPLE_RATE /
            1000
        );
    }

    // ============================================================
    // UPDATE ENVELOPE
    // ============================================================

    static void updateEnvelope(
        VoiceState state,
        Instrument inst
    ) {

        state.env0 =
            updateOneEnvelope(
                state.env0,
                state.envStage0,
                inst.op[0],
                state.attackStep0,
                state.decayStep0
            );

        state.env1 =
            updateOneEnvelope(
                state.env1,
                state.envStage1,
                inst.op[1],
                state.attackStep1,
                state.decayStep1
            );

        state.env2 =
            updateOneEnvelope(
                state.env2,
                state.envStage2,
                inst.op[2],
                state.attackStep2,
                state.decayStep2
            );

        state.env3 =
            updateOneEnvelope(
                state.env3,
                state.envStage3,
                inst.op[3],
                state.attackStep3,
                state.decayStep3
            );
    }

    // ============================================================
    // SINGLE ENVELOPE
    // ============================================================

    static int updateOneEnvelope(
        int value,
        int stage,
        Operator op,
        int attackStep,
        int decayStep
    ) {

        if (value < 65535) {

            value += attackStep;

            if (value >= 65535)
                value = 65535;

            return value;
        }

        int sustain =
            op.sustain;

        if (value > sustain) {

            value -= decayStep;

            if (value < sustain)
                value = sustain;
        }

        return value;
    }

    // ============================================================
    // DRUMS
    // ============================================================

    static int renderDrum(
        int midi,
        int sample,
        int velocity
    ) {

        int v =
            mathlib.Divider(
                velocity,
                127
            );

        /*
         * KICK
         */
        if (midi == 36) {

            int phase =
                kickPhase(sample);

            int env =
                decayEnvelope(
                    sample,
                    160
                );

            return
                mathlib.Divider(
                    sinePhase(phase) *
                    env *
                    v,
                    65535
                );
        }

        /*
         * SNARE
         */
        if (midi == 38) {

            int env =
                decayEnvelope(
                    sample,
                    120
                );

            int noise =
                noiseSample();

            int body =
                sinePhase(
                    sample *
                    17895697
                );

            return
                mathlib.Divider(
                    (
                        noise * 3 +
                        body
                    ) *
                    env *
                    v,
                    65535 * 4
                );
        }

        /*
         * CLOSED HAT
         */
        if (midi == 42) {

            int env =
                decayEnvelope(
                    sample,
                    55
                );

            int a =
                sinePhase(
                    sample *
                    337064000
                );

            int b =
                sinePhase(
                    sample *
                    487709000
                );

            int c =
                sinePhase(
                    sample *
                    601295000
                );

            return
                mathlib.Divider(
                    (
                        a + b + c
                    ) *
                    env *
                    v,
                    65535 * 6
                );
        }

        /*
         * OPEN HAT
         */
        if (midi == 46) {

            int env =
                decayEnvelope(
                    sample,
                    180
                );

            int a =
                sinePhase(
                    sample *
                    337064000
                );

            int b =
                sinePhase(
                    sample *
                    487709000
                );

            int c =
                sinePhase(
                    sample *
                    601295000
                );

            return
                mathlib.Divider(
                    (
                        a + b + c
                    ) *
                    env *
                    v,
                    65535 * 6
                );
        }

        /*
         * TOMS
         */
        if (
            midi == 45 ||
            midi == 47 ||
            midi == 50
        ) {

            int frequency;

            if (midi == 45)
                frequency = 11500;
            else if (midi == 47)
                frequency = 15500;
            else
                frequency = 20500;

            int phase =
                (int)(
                    ((long)
                        frequency *
                        sample *
                        4294967296L
                    ) /
                    (SAMPLE_RATE * 100L)
                );

            int env =
                decayEnvelope(
                    sample,
                    150
                );

            return
                mathlib.Divider(
                    sinePhase(phase) *
                    env *
                    v,
                    65535
                );
        }

        return 0;
    }

    // ============================================================
    // KICK
    // ============================================================

    static int kickPhase(
        int sample
    ) {

        /*
         * Falling pitch:
         *
         * ~150 Hz
         * down to
         * ~45 Hz
         */

        int freq;

        if (sample < 3000) {

            int drop =
                mathlib.Divider(
                    sample * 10500,
                    3000
                );

            freq =
                15000 -
                drop;

        } else {

            freq = 4500;
        }

        return (int)(
            (
                (long)
                freq *
                sample *
                4294967296L
            ) /
            (SAMPLE_RATE * 100L)
        );
    }

    // ============================================================
    // DECAY
    // ============================================================

    static int decayEnvelope(
        int sample,
        int milliseconds
    ) {

        int length =
            milliseconds *
            SAMPLE_RATE /
            1000;

        if (sample >= length)
            return 0;

        return
            65535 -
            mathlib.Divider(
                sample * 65535,
                length
            );
    }

    // ============================================================
    // NOISE
    // ============================================================

    static int noiseSample() {

        return
            (mathlib.byteRNG() - 128)
            << 8;
    }

    // ============================================================
    // SATURATION
    // ============================================================

    static int saturate(
        int value
    ) {

        /*
         * Hard limit only after
         * a cheap soft-knee.
         */

        if (value > 32767)
            return 32767;

        if (value < -32768)
            return -32768;

        return value;
    }

    // ============================================================
    // 16-BIT CLAMP
    // ============================================================

    static short clamp16(
        int value
    ) {

        if (value > 32767)
            return 32767;

        if (value < -32768)
            return -32768;

        return (short)value;
    }

    // ============================================================
    // TRACK LENGTH
    // ============================================================

    static int getTrackLength(
        Track track
    ) {

        int longest = 0;

        for (
            Note[] voice :
            track.voices
        ) {

            int length = 0;

            for (Note n : voice)
                length += n.ticks;

            if (length > longest)
                longest = length;
        }

        return longest;
    }
}