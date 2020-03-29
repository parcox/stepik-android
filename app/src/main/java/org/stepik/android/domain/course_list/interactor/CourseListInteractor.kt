package org.stepik.android.domain.course_list.interactor

import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course.interactor.CourseStatsInteractor
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.model.Course
import javax.inject.Inject

class CourseListInteractor
@Inject
constructor(
    private val courseRepository: CourseRepository,
    private val courseStatsInteractor: CourseStatsInteractor
) {

    // TODO Remove this method
    fun getCourses(courseListQuery: CourseListQuery): Single<PagedList<Course>> =
        courseRepository
            .getCourses(courseListQuery)

    // TODO Remove this method
    fun getSavedCourses(courseIds: LongArray): Single<PagedList<Course>> =
        courseRepository
            .getCourses(*courseIds, primarySourceType = DataSourceType.CACHE)

    fun getCourseListItems(vararg courseId: Long): Single<PagedList<CourseListItem.Data>> =
        getCourseListItems(coursesSource = courseRepository.getCourses(*courseId))

    fun getCourseListItems(courseListQuery: CourseListQuery): Single<PagedList<CourseListItem.Data>> =
        getCourseListItems(coursesSource = courseRepository.getCourses(courseListQuery))

    private fun getCourseListItems(coursesSource: Single<PagedList<Course>>): Single<PagedList<CourseListItem.Data>> =
        coursesSource
            .flatMap(::obtainCourseListItem)

    private fun obtainCourseListItem(courses: PagedList<Course>): Single<PagedList<CourseListItem.Data>> =
        courseStatsInteractor
            .getCourseStats(courses)
            .map { courseStats ->
                val list = courses.mapIndexed { index, course ->
                    CourseListItem.Data(
                        course = course,
                        courseStats = courseStats[index]
                    )
                }
                PagedList(list = list,
                    page = courses.page,
                    hasNext = courses.hasNext,
                    hasPrev = courses.hasPrev
                )
            }
}