package com.wafflestudio.snu4t.oldcategory.service

import com.wafflestudio.snu4t.lectures.data.Lecture
import com.wafflestudio.snu4t.oldcategory.repository.OldCategoryRepository
import kotlinx.coroutines.reactive.awaitSingle
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service

@Service
class OldCategoryFetchService(
    private val oldCategoryRepository: OldCategoryRepository,
    private val reactiveMongoTemplate: ReactiveMongoTemplate,
) {
    suspend fun applyOldCategories() {
        val oldCategories = getOldCategories()
        oldCategories.forEach { (courseNumber, oldCategory) ->
            reactiveMongoTemplate.updateMulti(
                Query(Criteria.where("courseNumber").`is`(courseNumber)),
                Update().set("oldCategory", oldCategory),
                Lecture::class.java
            ).awaitSingle()
        }
    }

    suspend fun getOldCategories(): Map<String, String> {
        val oldCategoriesXlsx = oldCategoryRepository.fetchOldCategories()
        val workbook = WorkbookFactory.create(oldCategoriesXlsx.asInputStream())
        return workbook.sheetIterator().asSequence()
            .flatMap { sheet ->
                sheet.rowIterator().asSequence()
                    .drop(3)
                    .filter { row ->
                        row.getCell(8) != null && row.getCell(1) != null
                    }
                    .map { row ->
                        try {
                            val currentCourseNumber = row.getCell(7).stringCellValue
                            val oldCategory = row.getCell(1).stringCellValue
                            if (currentCourseNumber.isBlank() || oldCategory.isBlank()) {
                                return@map null
                            }
                            currentCourseNumber to oldCategory
                        } catch (e: Exception) {
                            null
                        }
                    }
                    .filterNotNull()
            }
            .toMap()
    }
}
