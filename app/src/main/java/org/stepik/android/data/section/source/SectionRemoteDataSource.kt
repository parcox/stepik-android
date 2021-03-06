package org.stepik.android.data.section.source

import io.reactivex.Maybe
import io.reactivex.Single
import ru.nobird.android.domain.rx.maybeFirst
import org.stepik.android.model.Section

interface SectionRemoteDataSource {
    fun getSection(sectionId: Long): Maybe<Section> =
        getSections(sectionId).maybeFirst()

    fun getSections(vararg sectionIds: Long): Single<List<Section>>
}