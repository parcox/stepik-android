package org.stepic.droid.core;

import android.os.Handler;

import com.squareup.otto.Bus;

import org.stepic.droid.base.MainApplication;
import org.stepic.droid.events.units.UnitProgressUpdateEvent;
import org.stepic.droid.events.units.UnitScoreUpdateEvent;
import org.stepic.droid.model.Progress;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Unit;
import org.stepic.droid.store.operations.DatabaseManager;
import org.stepic.droid.util.StringUtil;
import org.stepic.droid.web.IApi;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

public class LocalProgressOfUnitManager implements ILocalProgressManager {
    private DatabaseManager mDatabaseManager;
    private Bus mBus;
    private IApi mApi;

    @Inject
    public LocalProgressOfUnitManager(DatabaseManager databaseManager, Bus bus, IApi api) {
        mDatabaseManager = databaseManager;
        mBus = bus;
        mApi = api;
    }


    @Override
    public void checkUnitAsPassed(final long stepId) {
        Step step = mDatabaseManager.getStepById(stepId);
        if (step == null) return;
        List<Step> stepList = mDatabaseManager.getStepsOfLesson(step.getLesson());
        for (Step stepItem : stepList) {
            if (!stepItem.is_custom_passed()) return;
        }

        Unit unit = mDatabaseManager.getUnitByLessonId(step.getLesson());
        if (unit == null) return;

//        unit.setIs_viewed_custom(true);
//        mDatabaseManager.addUnit(unit); //// TODO: 26.01.16 progress is not saved
        mDatabaseManager.markProgressAsPassedIfInDb(unit.getProgress());

        final long unitId = unit.getId();
        Handler mainHandler = new Handler(MainApplication.getAppContext().getMainLooper());
        //Say to ui that ui is cached now
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                mBus.post(new UnitProgressUpdateEvent(unitId));
            }
        };
        mainHandler.post(myRunnable);

    }

    @Override
    public void updateUnitProgress(final long unitId) {

        Unit unit = mDatabaseManager.getUnitById(unitId);
        if (unit == null) return;
        Progress updatedUnitProgress;
        try {
            updatedUnitProgress = mApi.getProgresses(new String[]{unit.getProgress()}).execute().body().getProgresses().get(0);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        if (updatedUnitProgress == null)
            return;
        mDatabaseManager.addProgress(updatedUnitProgress);

        final Double finalScoreInUnit = getScoreOfProgress(updatedUnitProgress);
        if (finalScoreInUnit == null) {
            return;
        }
        Handler mainHandler = new Handler(MainApplication.getAppContext().getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                mBus.post(new UnitScoreUpdateEvent(unitId, finalScoreInUnit));
            }
        };
        mainHandler.post(myRunnable);
    }

    private Double getScoreOfProgress(Progress progress) {

        if (progress == null) return null;
        String oldScore = progress.getScore();
        return StringUtil.safetyParseString(oldScore);
    }
}