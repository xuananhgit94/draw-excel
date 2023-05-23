package com.xuananh.demoimport.service;

import com.xuananh.demoimport.model.modelTotal.CounterSavingsInterest;
import com.xuananh.demoimport.model.modelexcel.*;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class InterestRateService {

    private static final int CELL_WIDTH = 120; // Độ rộng của mỗi ô lưới
    private static final int CELL_HEIGHT = 30; // Độ cao của mỗi ô lưới
    private static final int HEADER_HEIGHT = 80; // Độ cao của phần header

    @Autowired
    private MinioClient minioClient;

    public String upload(MultipartFile multipartFile) throws Exception {
        List<CounterSavingsInterest> counterSavingsInterests;
        try (InputStream inputStream = multipartFile.getInputStream()) {
            counterSavingsInterests = new ArrayList<>();
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            int rows = sheet.getPhysicalNumberOfRows();
            for (int i = 2; i < rows; i++) {
                Row row = sheet.getRow(i);
                String depositTerm = getStringCellValue(row.getCell(0));
                DailyInterestAccount dailyInterestAccount = DailyInterestAccount
                        .builder()
                        .truongAnLocAccount(getStringCellValue(row.getCell(1)))
                        .tailocAccount(getStringCellValue(row.getCell(2)))
                        .regularAccount(getStringCellValue(row.getCell(3)))
                        .month6Rate12Account(getStringCellValue(row.getCell(4)))
                        .dacLocAccount(getStringCellValue(row.getCell(5)))
                        .build();
                InitialInterestSavings initialInterestSavings = InitialInterestSavings
                        .builder()
                        .baoLocAccount(getStringCellValue(row.getCell(6)))
                        .regularAccount(getStringCellValue(row.getCell(7)))
                        .build();
                RegularInterestSavings regularInterestSavings = RegularInterestSavings
                        .builder()
                        .monthly(getStringCellValue(row.getCell(8)))
                        .quarterly(getStringCellValue(row.getCell(9)))
                        .build();
                ContributoryAccount contributoryAccount = ContributoryAccount
                        .builder()
                        .futureSavings(getStringCellValue(row.getCell(10)))
                        .futureSavingsKids(getStringCellValue(row.getCell(11)))
                        .build();
                BaoAnLocDeposit baoAnLocDeposit = BaoAnLocDeposit
                        .builder()
                        .endOfTerm(getStringCellValue(row.getCell(12)))
                        .monthly(getStringCellValue(row.getCell(13)))
                        .quarterly(getStringCellValue(row.getCell(14)))
                        .build();
                CounterSavingsInterest counterSavingsInterest = CounterSavingsInterest
                        .builder()
                        .depositTerm(depositTerm)
                        .dailyInterestAccount(dailyInterestAccount)
                        .initialInterestSavings(initialInterestSavings)
                        .regularInterestSavings(regularInterestSavings)
                        .contributoryAccount(contributoryAccount)
                        .baoAnLocDeposit(baoAnLocDeposit)
                        .build();
                counterSavingsInterests.add(counterSavingsInterest);
            }
        }
        return drawImageFindUrl(counterSavingsInterests);
    }

    private String drawImageFindUrl(List<CounterSavingsInterest> counterSavingsInterests) throws Exception {
        BufferedImage image = createImageFromCounterSavingsInterests(counterSavingsInterests);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        baos.flush();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(baos.toByteArray());
        baos.close();

        // Tạo tên file duy nhất cho ảnh
        String filename = UUID.randomUUID().toString() + ".png";

        // Lưu ảnh vào MinIO server
        minioClient.putObject(PutObjectArgs
                .builder()
                .bucket("images")
                .object(filename)
                .stream(inputStream, inputStream.available(), -1)
                .contentType("image/png")
                .build()
        );
        // Trả về URL của ảnh đã lưu
        GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs
                .builder()
                .method(Method.GET)
                .bucket("images")
                .object(filename)
                .build();
        return minioClient.getPresignedObjectUrl(args);
    }

    //    private BufferedImage createImageFromCounterSavingsInterests(List<CounterSavingsInterest> counterSavingsInterests) {
//        int cellWidth = 100; // Độ rộng ô
//        int cellHeight = 20; // Độ cao ô
//
//        int numColumns = 15; // Số cột
//        int numRows = counterSavingsInterests.size(); // Số hàng
//
//        int imageWidth = cellWidth * numColumns + 2; // Kích thước chiều ngang của ảnh
//        int imageHeight = cellHeight * numRows + 2; // Kích thước chiều cao của ảnh
//
//        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
//        Graphics2D graphics = image.createGraphics();
//        graphics.setColor(Color.WHITE);
//        graphics.fillRect(0, 0, imageWidth, imageHeight);
//        graphics.setColor(Color.BLACK);
//
//        Font font = new Font("Arial", Font.PLAIN, 12);
//        graphics.setFont(font);
//
//        FontMetrics fontMetrics = graphics.getFontMetrics(font);
//
//        // Vẽ lưới
//        for (int i = 0; i <= numRows; i++) {
//            int y = i * cellHeight;
//            graphics.drawLine(0, y, imageWidth, y);
//        }
//        for (int i = 0; i <= numColumns; i++) {
//            int x = i * cellWidth;
//            graphics.drawLine(x, 0, x, imageHeight);
//        }
//
//        // Vẽ header
//        int headerY = cellHeight; // Tọa độ y của header
//
//        String[] hearders = {"Kỳ hạn", "TK Trường An Lộc", "TK Tài Lộc", "TK Thường", "TK 6th lãi 12", "TK Đắc Lộc", "TK Bảo Lộc", "TK Thường", "Hàng Tháng", "Hàng Quý", "Future Savings", "Future Savings Kids", "Cuối Kỳ", "Hàng Tháng", "Hàng Quý"};
//
//        for (int j = 0; j < numColumns; j++) {
//            int cellX = j * cellWidth; // Tọa độ x của ô
//            String fieldName = hearders[j]; // Hàm để lấy tên trường dữ liệu
//            int fieldNameX = cellX + (cellWidth / 2) - (fontMetrics.stringWidth(fieldName) / 2); // Tọa độ x của tên trường dữ liệu
//            int fieldNameY = headerY + (cellHeight / 2) - (fontMetrics.getAscent() / 2); // Tọa độ y của tên trường dữ liệ
//
//            graphics.drawString(fieldName, fieldNameX, fieldNameY);
//        }
//
//
//
//        for (int i = 0, counterSavingsInterestsSize = counterSavingsInterests.size(); i < counterSavingsInterestsSize; i++) {
//            CounterSavingsInterest interest = counterSavingsInterests.get(i);
//            String depositTerm = interest.getDepositTerm();
//            DailyInterestAccount dailyInterest = interest.getDailyInterestAccount();
//            InitialInterestSavings interestSavings = interest.getInitialInterestSavings();
//            RegularInterestSavings regularInterestSavings = interest.getRegularInterestSavings();
//            ContributoryAccount contributory = interest.getContributoryAccount();
//            BaoAnLocDeposit baoAnLocDeposit = interest.getBaoAnLocDeposit();
//
//            int cellY = (i * cellHeight) + (cellHeight / 2); // Tọa độ y của trung tâm ô
//
//            List<String> rowList = getRowText(depositTerm, dailyInterest, interestSavings, regularInterestSavings, contributory, baoAnLocDeposit);
//
//            // Vẽ từng ô dữ liệu
//            for (int j = 0; j < rowList.size(); j++) {
//                int cellX = (j * cellWidth) + (cellWidth / 2); // Tọa độ x của trung tâm ô
//
//                // Vẽ dữ liệu
//                String fieldText = rowList.get(j);
//                int fieldWidth = fontMetrics.stringWidth(fieldText);
//                int fieldX = cellX - (fieldWidth / 2); // Tọa độ x của trường dữ liệu
//                int fieldY = cellY + (cellHeight / 2) - (fontMetrics.getAscent() / 2); // Tọa độ y của trường dữ liệu
//                graphics.drawString(fieldText, fieldX, fieldY);
//            }
//        }
//
//        // Kết thúc vẽ
//        graphics.dispose();
//
//        return image;
//    }
    private BufferedImage createImageFromCounterSavingsInterests(List<CounterSavingsInterest> counterSavingsInterests) {
        String[] headers = {
                "Kỳ hạn", "TK\nTrường\nAn Lộc", "TK\nTài Lộc", "TK\nThường", "TK 6th\nlãi 12", "TK\nĐắc Lộc", "TK\nBảo Lộc",
                "TK\nthường", "Hàng\ntháng", "Hàng\nquý", "Future\nSavings", "Future\nSavings\nKids", "Cuối\nKỳ", "Hàng\nTháng", "Hàng\nQuý"
        };

        // Số lượng hàng và cột trong bảng
        int rowCount = counterSavingsInterests.size();
        int columnCount = headers.length;

        // Độ rộng và độ cao của hình ảnh
        int imageWidth = columnCount * CELL_WIDTH;
        int imageHeight = (rowCount + 1) * CELL_HEIGHT + HEADER_HEIGHT; // +1 cho hàng header

        // Tạo hình ảnh mới với kích thước đã tính toán
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Thiết lập font và màu sắc cho header
        Font headerFont = new Font("Arial", Font.BOLD, 14);
        g2d.setFont(headerFont);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, imageWidth, HEADER_HEIGHT);

        // Vẽ phần header
        FontMetrics fontMetrics = g2d.getFontMetrics(headerFont);
        int headerY = (HEADER_HEIGHT - fontMetrics.getHeight()) / 2 + fontMetrics.getAscent();
        for (int column = 0; column < columnCount; column++) {
            String header = headers[column];
            int headerX = column * CELL_WIDTH;
            drawMultilineText(g2d, header, headerX, headerY, CELL_WIDTH);
        }

        // Vẽ lưới
        g2d.setColor(Color.BLACK);
        for (int row = 0; row <= rowCount; row++) {
            int y = HEADER_HEIGHT + row * CELL_HEIGHT;
            g2d.drawLine(0, y, imageWidth, y);
        }
        for (int column = 0; column < columnCount; column++) {
            int x = column * CELL_WIDTH;
            g2d.drawLine(x, HEADER_HEIGHT, x, imageHeight);
        }

        // Vẽ phần data
        Font dataFont = new Font("Arial", Font.PLAIN, 12);
        g2d.setFont(dataFont);
        g2d.setColor(Color.BLACK);
        int dataY = HEADER_HEIGHT + CELL_HEIGHT;

        int dataX = 0;
        for (int row = 0; row < rowCount; row++) {
            CounterSavingsInterest interest = counterSavingsInterests.get(row);
            dataX = 0;
            dataY += CELL_HEIGHT;
            for (int column = 0; column < columnCount; column++) {
                String cellData = getDataForCell(interest, column);
                drawMultilineText(g2d, cellData, dataX, dataY, CELL_WIDTH);
                dataX += CELL_WIDTH;
            }
        }

        // Kết thúc việc vẽ đồ họa
        g2d.dispose();

        return image;
    }

    private void drawMultilineText(Graphics2D g2d, String text, int x, int y, int width) {
        FontMetrics fontMetrics = g2d.getFontMetrics();
        List<String> lines = getLinesToFitWidth(fontMetrics, text, width);
        int lineHeight = fontMetrics.getHeight();
        int lineHeightOffset = (CELL_HEIGHT - lineHeight) / 2;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int lineY = y + lineHeightOffset + (lineHeight * i);
            int lineX = x + (width - fontMetrics.stringWidth(line)) / 2;
            g2d.drawString(line, lineX, lineY);
        }
    }

    private List<String> getLinesToFitWidth(FontMetrics fontMetrics, String text, int width) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split("\\n");
        StringBuilder currentLine = new StringBuilder();
        for (String word : words) {
            if (fontMetrics.stringWidth(currentLine + " " + word) <= width) {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            } else {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            }
        }
        lines.add(currentLine.toString());
        return lines;
    }

    private String getDataForCell(CounterSavingsInterest interest, int column) {
        // Dựa vào chỉ số cột, trích xuất dữ liệu từ đối tượng CounterSavingsInterest tương ứng
        switch (column) {
            case 0:
                return interest.getDepositTerm();
            case 1:
                return interest.getDailyInterestAccount().getTruongAnLocAccount();
            case 2:
                return interest.getDailyInterestAccount().getTailocAccount();
            case 3:
                return interest.getDailyInterestAccount().getRegularAccount();
            case 4:
                return interest.getDailyInterestAccount().getMonth6Rate12Account();
            case 5:
                return interest.getDailyInterestAccount().getDacLocAccount();
            case 6:
                return interest.getInitialInterestSavings().getBaoLocAccount();
            case 7:
                return interest.getInitialInterestSavings().getRegularAccount();
            case 8:
                return interest.getRegularInterestSavings().getMonthly();
            case 9:
                return interest.getRegularInterestSavings().getQuarterly();
            case 10:
                return interest.getContributoryAccount().getFutureSavings();
            case 11:
                return interest.getContributoryAccount().getFutureSavingsKids();
            case 12:
                return interest.getBaoAnLocDeposit().getEndOfTerm();
            case 13:
                return interest.getBaoAnLocDeposit().getMonthly();
            case 14:
                return interest.getBaoAnLocDeposit().getQuarterly();
            default:
                return "";
        }
    }




    private List<String> getRowText(String depositTerm, DailyInterestAccount dailyInterest, InitialInterestSavings interestSavings, RegularInterestSavings regularInterestSavings, ContributoryAccount contributory, BaoAnLocDeposit baoAnLocDeposit) {
        return List.of(
                depositTerm,
                dailyInterest.getTruongAnLocAccount(),
                dailyInterest.getTailocAccount(),
                dailyInterest.getRegularAccount(),
                dailyInterest.getMonth6Rate12Account(),
                dailyInterest.getDacLocAccount(),
                interestSavings.getBaoLocAccount(),
                interestSavings.getBaoLocAccount(),
                regularInterestSavings.getMonthly(),
                regularInterestSavings.getQuarterly(),
                contributory.getFutureSavings(),
                contributory.getFutureSavingsKids(),
                baoAnLocDeposit.getEndOfTerm(),
                baoAnLocDeposit.getMonthly(),
                baoAnLocDeposit.getQuarterly()

        );
    }

    private String getStringCellValue(Cell cell) {
        if (Objects.isNull(cell)|| cell.getCellType() == CellType.BLANK) {
            return "";
        }
        CellType cellType = cell.getCellType();
        if (cellType == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cellType == CellType.NUMERIC) {
            return String.valueOf(cell.getNumericCellValue());
        } else if (cellType == CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        } else if (cellType == CellType.FORMULA) {
            return cell.getCellFormula();
        } else {
            return null;
        }
    }
}
