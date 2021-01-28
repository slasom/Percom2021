package com.spilab.percom21;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.internal.util.Checks;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.regex.Matcher;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
    @LargeTest
    public class HelloWorldEspressoTest {




    @Rule public ActivityScenarioRule<MainActivity> activityScenarioRule
            = new ActivityScenarioRule<>(MainActivity.class);



    @Test
    public void checkStateTextView() {

        onView(withId((R.id.buttonSendRequest))).perform(click());
        try {
            Thread.sleep(500);
            onView(withId(R.id.textViewState)).check(ViewAssertions.matches(withText("Obtaining...")));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }



}