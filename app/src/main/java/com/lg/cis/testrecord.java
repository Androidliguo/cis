package com.lg.cis;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
public class testrecord extends Activity {
        /** Called when the activity is first created. */
        private EditText mResultText;
        Button btnRecord, btnStop, btnExit;
        //音量的调制
        SeekBar skbVolume;
        //是否还在采集音频数据
        boolean isRecording = false;
        //采样频率
        static final int frequency = 16000;
        //通道数。这里是单通道
        static final int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        //数据位宽。这里16位。
        static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

        int recBufSize,playBufSize;
        AudioRecord audioRecord;
        AudioTrack audioTrack;
        

        @Override
        public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main);
                setTitle("RecordModule");
                //这个是最难理解又最重要的一个参数，它配置的是 AudioRecord 内部的音频缓冲区的大小，
                // 该缓冲区的值不能低于一帧“音频帧”（Frame）的大小
                //int size = 采样率 x 位宽 x 采样时间 x 通道数
                recBufSize = AudioRecord.getMinBufferSize(frequency,
                                channelConfiguration, audioEncoding);

                playBufSize=AudioTrack.getMinBufferSize(frequency,
                                channelConfiguration, audioEncoding);
                //audioRecord的参数的配置。
                //MediaRecorder.AudioSource.MIC；用于手机麦克风的输入。
                audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency,
                                channelConfiguration, audioEncoding, recBufSize);

                audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency,
                                channelConfiguration, audioEncoding,
                                playBufSize, AudioTrack.MODE_STREAM);
                //------------------------------------------
                btnRecord = (Button) this.findViewById(R.id.btnRecord);
                btnRecord.setOnClickListener(new ClickEvent());
                btnStop = (Button) this.findViewById(R.id.btnStop);
                btnStop.setOnClickListener(new ClickEvent());
                btnExit = (Button) this.findViewById(R.id.btnExit);
                btnExit.setOnClickListener(new ClickEvent());
                //录入的文字的内容
                mResultText= (EditText) findViewById(R.id.result);
                //初始化
               // SpeechUtility.createUtility(this, SpeechConstant.APPID + "=58188e7d");
               // mlat = SpeechRecognizer.createRecognizer(this, null);
              //  initSpeechRecognizer();
                //音量大小的控制
                skbVolume=(SeekBar)this.findViewById(R.id.skbVolume);
                skbVolume.setMax(100);
                skbVolume.setProgress(40);
                audioTrack.setStereoVolume(0.7f, 0.7f);
                skbVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        
                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                                float vol=(float)(seekBar.getProgress())/(float)(seekBar.getMax());
                                audioTrack.setStereoVolume(vol, vol);
                        }
                        
                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                                // TODO Auto-generated method stub
                        }
                        
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress,
                                        boolean fromUser) {
                                // TODO Auto-generated method stub
                        }
                });
        }

        /*//初始化SpeechRecognizer
        private void initSpeechRecognizer(){
                mlat.setParameter(SpeechConstant.LANGUAGE, "en_us");
                mlat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
                mlat.setParameter(SpeechConstant.ACCENT, "mandarin");
                mlat.setParameter(SpeechConstant.ASR_PTT, "1");
                //设置是从音频流中读取数据.不是从麦克风中读取数据
                mlat.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");
        }*/

        @Override
        protected void onDestroy() {
                super.onDestroy();
                android.os.Process.killProcess(android.os.Process.myPid());
        }

        class ClickEvent implements View.OnClickListener {

                @Override
                public void onClick(View v) {
                        //开始采集音频数据
                        if (v == btnRecord) {
                                isRecording = true;
                                new RecordPlayThread().start();
                                //停止采集音频数据
                        } else if (v == btnStop) {
                                isRecording = false;
                                //退出采集音频数据
                        } else if (v == btnExit) {
                                isRecording = false;
                                //销毁activity
                                testrecord.this.finish();
                        }
                }
        }

        //采集音频数据的线程。需要开辟线程。
        class RecordPlayThread extends Thread {
                @SuppressLint("ShowToast")
				public void run() {
                        try {
                                //临时的byte类型的数据
                                byte[] datas = new byte[recBufSize / 2];
                                short[] buffer = new short[recBufSize / 2];
                                //开始录制声音
                                audioRecord.startRecording();
                                //开始播放声音
                                audioTrack.play();

                                while (isRecording) {

                                        int bufferReadResult = audioRecord.read(buffer, 0,
                                                recBufSize / 2);
                                        //byte类型的数据
                                        int dataReadResult = audioRecord.read(datas, 0, recBufSize / 2);

                                        short[] tmpBuf = new short[bufferReadResult];
                                        //完成数组的复制。
                                        System.arraycopy(buffer, 0, tmpBuf, 0, bufferReadResult);
//                                        for(int i=0;i<tmpBuf.length;i++){
//                                            System.out.println(tmpBuf[i]);
//                                        }

                                        //语音处理
                                        //***************************************************


                                        CisImplement mCisImplement = new CisImplement(tmpBuf, tmpBuf.length);
//                                        byte[] processed = mCisImplement.yuJiaZhong();
                                        short[] processed = mCisImplement.BeginCis();
                                        short max = processed[0];
                                        for (int i = 0; i < tmpBuf.length; i++) {
                                                if (processed[i] > max) {
                                                        max = processed[i];
                                                }
                                        }
                                        System.out.println("max:  " + max);

//                                         for(int i=0;i<tmpBuf.length;i++){
//                                             System.out.println(processed[i]);
//                                          }
                                        //语音识别
                                        //mlat.startListening(recognizerListener);
                                        //mlat.writeAudio(datas, 0, datas.length);
                                        //***************************************************

                                        audioTrack.write(processed, 0, processed.length);
                                }
                                audioTrack.stop();
                                audioRecord.stop();
                        } catch (Throwable t) {
                                //Toast.makeText(testrecord.this, t.getMessage(), Toast.LENGTH_SHORT);
                        }
                }
        };

        /*//SpeechRecognizer的监听器
        private RecognizerListener recognizerListener=new RecognizerListener() {
                @Override
                public void onVolumeChanged(int i, byte[] bytes) {
                        // Toast.makeText(MainActivity.this, "当前正在说话，音量大小:" + i, Toast.LENGTH_LONG).show();


                }

                @Override
                public void onBeginOfSpeech() {
                        Toast.makeText(testrecord.this, "开始说话了", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onEndOfSpeech() {
                        Toast.makeText(testrecord.this, "结束说话了", Toast.LENGTH_LONG).show();
                        //重新调用
                        mlat.startListening(recognizerListener);
                }

                @Override
                public void onResult(RecognizerResult results, boolean b) {
                        String text = JsonParser.parseIatResult(results.getResultString());
                        mResultText.append(text);
                        mResultText.setSelection(mResultText.length());
                        if (b) {
                                Toast.makeText(testrecord.this, "最后一句了", Toast.LENGTH_LONG).show();
                        }
                }

                @Override
                public void onError(SpeechError speechError) {
                        Toast.makeText(testrecord.this, speechError.getErrorDescription(), Toast.LENGTH_SHORT).show();
                        mlat.startListening(recognizerListener);
                }

                @Override
                public void onEvent(int i, int i1, int i2, Bundle bundle) {

                }
        };
*/
        /**
         * @功能 短整型与字节的转换
         * @param
         * @return 两位的字节数组
         */
        public static byte[] shortToByte(short number) {
                int temp = number;
                byte[] b = new byte[2];
                for (int i = 0; i < b.length; i++) {
                        b[i] = new Integer(temp & 0xff).byteValue();// 将最低位保存在最低位
                        temp = temp >> 8; // 向右移8位
                }
                return b;
        }

        /**
         * 短整型数组转化成字节数组
         * @param numbers
         * @return
         */
        public static byte[] shortArrayToByte(short[] numbers) {

                return null;
        }
}