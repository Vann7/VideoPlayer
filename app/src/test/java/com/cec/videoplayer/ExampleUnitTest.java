package com.cec.videoplayer;

import com.cec.videoplayer.utils.PlayHitstUtil;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }


    @Test
    public void viewHits() {
        int hits = 142314;
        String result = PlayHitstUtil.getCount(hits);
        assertEquals("14万次播放", result);
    }

}