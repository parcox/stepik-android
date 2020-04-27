package org.stepik.android.data.user_courses.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepic.droid.util.doCompletableOnSuccess
import org.stepik.android.data.user_courses.source.UserCoursesCacheDataSource
import org.stepik.android.data.user_courses.source.UserCoursesRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.user_courses.repository.UserCoursesRepository
import org.stepik.android.domain.user_courses.model.UserCourse
import javax.inject.Inject

class UserCoursesRepositoryImpl
@Inject
constructor(
    private val userCoursesRemoteDataSource: UserCoursesRemoteDataSource,
    private val userCoursesCacheDataSource: UserCoursesCacheDataSource
) : UserCoursesRepository {
    override fun getUserCourses(page: Int, sourceType: DataSourceType): Single<PagedList<UserCourse>> {
        val remoteSource = userCoursesRemoteDataSource
            .getUserCourses(page)
            .doCompletableOnSuccess(userCoursesCacheDataSource::saveUserCourses)

        val cacheSource = userCoursesCacheDataSource
            .getUserCourses()
            .map { PagedList(it) }

        return when (sourceType) {
            DataSourceType.CACHE ->
                cacheSource

            DataSourceType.REMOTE ->
                if (page == 1) {
                    remoteSource.onErrorResumeNext(cacheSource)
                } else {
                    remoteSource
                }
        }
    }

    override fun toggleUserCourse(userCourse: UserCourse): Completable =
        userCoursesRemoteDataSource
            .toggleUserCourses(userCourse.id, userCourse)
            .doOnComplete { userCoursesCacheDataSource.saveUserCourses(listOf(userCourse)) }

    override fun getUserCourse(courseId: Long): Single<UserCourse> =
        userCoursesCacheDataSource.getUserCourse(courseId)

    override fun addUserCourse(userCourse: UserCourse): Completable =
        userCoursesCacheDataSource.saveUserCourses(listOf(userCourse))

    override fun removeUserCourse(courseId: Long): Completable =
        userCoursesCacheDataSource.removeUserCourse(courseId)
}