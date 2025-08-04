package com.wafflestudio.snutt.pre2025category.service

import com.wafflestudio.snutt.pre2025category.repository.CategoryPre2025Repository
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.core.io.buffer.PooledDataBuffer
import org.springframework.stereotype.Service

@Service
class CategoryPre2025FetchService(
    private val categoryPre2025Repository: CategoryPre2025Repository,
) {
    suspend fun getCategoriesPre2025(): Map<String, String> {
        val oldCategoriesXlsx: PooledDataBuffer = categoryPre2025Repository.fetchCategoriesPre2025()

        try {
            val workbook = WorkbookFactory.create(oldCategoriesXlsx.asInputStream())
            return workbook
                .sheetIterator()
                .asSequence()
                .flatMap { sheet ->
                    sheet
                        .rowIterator()
                        .asSequence()
                        .drop(4)
                        .mapNotNull { row ->
                            runCatching {
                                val currentCourseNumber = row.getCell(7).stringCellValue
                                val oldCategory = row.getCell(1).stringCellValue
                                check(currentCourseNumber.isNotBlank() && oldCategory.isNotBlank())
                                currentCourseNumber to oldCategory
                            }.getOrNull()
                        }
                }.toMap()
        } finally {
            oldCategoriesXlsx.release()
        }
    }
}
