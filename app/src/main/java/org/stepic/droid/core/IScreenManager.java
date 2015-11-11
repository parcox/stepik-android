package org.stepic.droid.core;

import android.app.Activity;
import android.content.Context;

import org.jetbrains.annotations.NotNull;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Unit;

public interface IScreenManager {
    void showLaunchScreen(Context context, boolean overrideAnimation);
    void showRegistration(Activity sourceActivity);
    void showLogin(Context sourceActivity);

    void showMainFeed(Context sourceActivity);

    void showCourseDescriptionForNotEnrolled(Context sourceActivity, @NotNull Course course);

    void showCourseDescriptionForEnrolled (Context sourceActivity, @NotNull Course course);

    void showUnitsForSection (Context sourceActivity, @NotNull Section section);

    void showSteps(Context sourceActivity, Unit unit, Lesson lesson);

    void openStepInWeb(Context context, Step step);

    void openSignUpInWeb(Context context);

}