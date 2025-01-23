package com.wafflestudio.snu4t.oldcategory.service

import com.wafflestudio.snu4t.oldcategory.repository.OldCategoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Service

@Service
class OldCategoryFetchService(
    private val oldCategoryRepository: OldCategoryRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun getOldCategories(): Map<String, String> {
        val oldCategoriesXlsx = oldCategoryRepository.fetchOldCategories()
        val workbook = SXSSFWorkbook(WorkbookFactory.create(oldCategoriesXlsx.asInputStream()) as XSSFWorkbook)

        return workbook.sheetIterator().asFlow().flatMapConcat {
            it.rowIterator().asFlow().map { row ->
                val currentCourseNumber = row.getCell(8).stringCellValue
                val oldCategory = row.getCell(1).stringCellValue
                currentCourseNumber to oldCategory
            }
        }.toList().toMap()
    }
}
