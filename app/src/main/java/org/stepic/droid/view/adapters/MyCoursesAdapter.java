package org.stepic.droid.view.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.stepic.droid.R;
import org.stepic.droid.base.MainApplication;
import org.stepic.droid.configuration.IConfig;
import org.stepic.droid.core.IShell;
import org.stepic.droid.model.Course;
import org.stepic.droid.store.CleanManager;
import org.stepic.droid.store.IDownloadManager;
import org.stepic.droid.store.operations.DatabaseManager;
import org.stepic.droid.util.HtmlHelper;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindDrawable;
import butterknife.ButterKnife;

public class MyCoursesAdapter extends ArrayAdapter<Course> {

    @Inject
    IShell mShell;

    @Inject
    IConfig mConfig;

    @Inject
    DatabaseManager mDatabase;

    @Inject
    IDownloadManager mDownloadManager;

    @Inject
    CleanManager mCleaner;

    private Activity mActivity;
    private final DatabaseManager.Table type;
    private LayoutInflater mInflater;

    public MyCoursesAdapter(Activity context, List<Course> courses, DatabaseManager.Table type) {
        super(context, 0, courses);
        mActivity = context;
        this.type = type;
        mInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        MainApplication.component().inject(this);

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Course course = getItem(position);

        View view = convertView;
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolderItem viewHolderItem = null;
        if (view == null) {
            view = mInflater.inflate(R.layout.course_item, null);
            viewHolderItem = new ViewHolderItem(view);
            view.setTag(viewHolderItem);
        } else {
            viewHolderItem = (ViewHolderItem) convertView.getTag();
        }
        viewHolderItem.courseName.setText(course.getTitle());
        viewHolderItem.courseSummary.setText(HtmlHelper.fromHtml(course.getSummary()));
        Picasso.with(mActivity).load(mConfig.getBaseUrl() + course.getCover()).
                placeholder(viewHolderItem.placeholder).into(viewHolderItem.courseIcon);
        viewHolderItem.courseDateInterval.setText(course.getDateOfCourse());

        viewHolderItem.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (course.getEnrollment() != 0) {
                    mShell.getScreenProvider().showCourseDescriptionForEnrolled(mActivity, course);
                } else {
                    mShell.getScreenProvider().showCourseDescriptionForNotEnrolled(mActivity, course);
                }
            }
        });

        if (type == DatabaseManager.Table.enrolled) {
            viewHolderItem.loadButton.setVisibility(View.VISIBLE);
            //cached/loading
            //false/false = show sky with suggestion for cache
            //false/true = show progress
            //true/false = show can with suggestion for delete
            //true/true = impossible
            if (course.is_cached()) {
                //cached

                viewHolderItem.loadButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCleaner.removeCourse(course, type);
                        course.setIs_cached(false);
                        course.setIs_loading(false);
                        mDatabase.updateOnlyCachedLoadingCourse(course, type);
                        notifyDataSetChanged();
                    }
                });

                viewHolderItem.preLoadIV.setVisibility(View.GONE);
                viewHolderItem.whenLoad.setVisibility(View.INVISIBLE);
                viewHolderItem.afterLoad.setVisibility(View.VISIBLE); //can

            } else {
                if (course.is_loading()) {

                    viewHolderItem.preLoadIV.setVisibility(View.GONE);
                    viewHolderItem.whenLoad.setVisibility(View.VISIBLE);
                    viewHolderItem.afterLoad.setVisibility(View.GONE);

                    //todo: add cancel of downloading
                } else {
                    //not cached not loading
                    viewHolderItem.preLoadIV.setVisibility(View.VISIBLE);
                    viewHolderItem.whenLoad.setVisibility(View.INVISIBLE);
                    viewHolderItem.afterLoad.setVisibility(View.GONE);


                    viewHolderItem.loadButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // FIXME: 21.10.15 IMPLEMENTS IN BACKGROUND THREAD
                            // FIXME: 21.10.15 MAKE UI DISABLED IF COURSE IS LOADED.
                            mDownloadManager.addCourse(course, type);
                            course.setIs_loading(true);
                            course.setIs_cached(false);
                            mDatabase.updateOnlyCachedLoadingCourse(course, type);
                            notifyDataSetChanged();
                        }
                    });
                }
            }
        } else {
            viewHolderItem.preLoadIV.setVisibility(View.GONE);
            viewHolderItem.whenLoad.setVisibility(View.GONE);
            viewHolderItem.afterLoad.setVisibility(View.GONE);
            viewHolderItem.loadButton.setVisibility(View.GONE);
        }
        return view;
    }

    static class ViewHolderItem {


        @Bind(R.id.course_name)
        TextView courseName;

        @Bind(R.id.course_info)
        TextView courseSummary;

        @Bind(R.id.course_icon)
        ImageView courseIcon;

        @Bind(R.id.course_date_interval)
        TextView courseDateInterval;

        @Bind(R.id.cv)
        View cardView;

        @Bind(R.id.pre_load_iv)
        View preLoadIV;

        @Bind(R.id.when_load_view)
        View whenLoad;

        @Bind(R.id.after_load_iv)
        View afterLoad;

        @Bind(R.id.load_button)
        View loadButton;

        @BindDrawable(R.drawable.stepic_logo_black_and_white)
        Drawable placeholder;

        public ViewHolderItem(View view) {
            ButterKnife.bind(this, view);
        }
    }
}