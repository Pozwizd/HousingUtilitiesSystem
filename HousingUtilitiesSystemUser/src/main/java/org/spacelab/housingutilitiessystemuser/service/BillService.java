package org.spacelab.housingutilitiessystemuser.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.spacelab.housingutilitiessystemuser.entity.Bill;
import org.spacelab.housingutilitiessystemuser.entity.User;
import org.spacelab.housingutilitiessystemuser.repository.BillRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillService {

    private final BillRepository billRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    
    public Page<Bill> findByUserWithFilters(User user, String billNumber, LocalDate startDate,
            LocalDate endDate, Pageable pageable) {
        if (user == null || user.getBills() == null || user.getBills().isEmpty()) {
            return Page.empty(pageable);
        }

        List<Bill> allBills = user.getBills();

        
        List<Bill> filteredBills = allBills.stream()
                .filter(bill -> {
                    
                    if (billNumber != null && !billNumber.trim().isEmpty()) {
                        if (bill.getBillNumber() == null ||
                                !bill.getBillNumber().toLowerCase().contains(billNumber.toLowerCase())) {
                            return false;
                        }
                    }
                    
                    if (startDate != null && bill.getDate() != null) {
                        if (bill.getDate().isBefore(startDate)) {
                            return false;
                        }
                    }
                    
                    if (endDate != null && bill.getDate() != null) {
                        if (bill.getDate().isAfter(endDate)) {
                            return false;
                        }
                    }
                    return true;
                })
                
                .sorted(Comparator.comparing(Bill::getDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredBills.size());

        if (start > filteredBills.size()) {
            return new PageImpl<>(List.of(), pageable, filteredBills.size());
        }

        List<Bill> pagedBills = filteredBills.subList(start, end);
        return new PageImpl<>(pagedBills, pageable, filteredBills.size());
    }

    
    public Optional<Bill> findById(String id) {
        return billRepository.findById(id);
    }

    
    public Bill save(Bill bill) {
        return billRepository.save(bill);
    }

    
    public List<Bill> saveAll(List<Bill> bills) {
        return billRepository.saveAll(bills);
    }

    
    public byte[] generateExcel(List<Bill> bills) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Счета");

            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            
            Row headerRow = sheet.createRow(0);
            String[] headers = { "№ п/п", "№ Квитанции", "Дата" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            
            int rowNum = 1;
            for (Bill bill : bills) {
                Row row = sheet.createRow(rowNum);

                Cell cell0 = row.createCell(0);
                cell0.setCellValue(rowNum);
                cell0.setCellStyle(dataStyle);

                Cell cell1 = row.createCell(1);
                cell1.setCellValue(bill.getBillNumber() != null ? bill.getBillNumber() : "");
                cell1.setCellStyle(dataStyle);

                Cell cell2 = row.createCell(2);
                cell2.setCellValue(bill.getDate() != null ? bill.getDate().format(DATE_FORMATTER) : "");
                cell2.setCellStyle(dataStyle);

                rowNum++;
            }

            
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    
    public List<Bill> getFilteredBills(User user, String billNumber, LocalDate startDate, LocalDate endDate) {
        if (user == null || user.getBills() == null || user.getBills().isEmpty()) {
            return List.of();
        }

        return user.getBills().stream()
                .filter(bill -> {
                    if (billNumber != null && !billNumber.trim().isEmpty()) {
                        if (bill.getBillNumber() == null ||
                                !bill.getBillNumber().toLowerCase().contains(billNumber.toLowerCase())) {
                            return false;
                        }
                    }
                    if (startDate != null && bill.getDate() != null) {
                        if (bill.getDate().isBefore(startDate)) {
                            return false;
                        }
                    }
                    if (endDate != null && bill.getDate() != null) {
                        if (bill.getDate().isAfter(endDate)) {
                            return false;
                        }
                    }
                    return true;
                })
                .sorted(Comparator.comparing(Bill::getDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }
}
