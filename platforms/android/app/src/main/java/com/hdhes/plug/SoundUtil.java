package com.hdhes.plug;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;


import com.hdhe.scan.R;

import java.util.HashMap;
import java.util.Map;

public class SoundUtil {

    public static SoundPool sp;
    public static Map<Integer, Integer> suondMap;
    public static Context context;

    public static void initSoundPool(Context context) {
        SoundUtil.context = context;
        // 5.0 及 之后
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
			Log.d("Util", "5.0 及 之后: ");
            AudioAttributes audioAttributes = null;
            audioAttributes = new AudioAttributes.Builder()
					.setUsage(AudioAttributes.USAGE_MEDIA)
					.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
					.build();
            sp = new SoundPool.Builder()
                    .setMaxStreams(16)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 1);
        }
        suondMap = new HashMap<>();
        suondMap.put(1, sp.load(context, R.raw.scan, 1));

    }

    //
    public static void play(int sound, int number) {
        try {
            Log.d("Util", "play: ");
            AudioManager am = (AudioManager) SoundUtil.context.getSystemService(Context.AUDIO_SERVICE);
            float audioMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            float audioCurrentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
            float volumnRatio = audioCurrentVolume / audioMaxVolume;
            sp.play(
                    suondMap.get(sound), //
                    audioCurrentVolume, //
                    audioCurrentVolume, //
                    1, //
                    number, //
                    1);//
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void pasue() {
        sp.pause(0);
    }
}
