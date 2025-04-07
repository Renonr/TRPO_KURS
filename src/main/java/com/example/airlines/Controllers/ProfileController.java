package com.example.airlines.Controllers;

import com.example.airlines.DTO.TicketDTO;
import com.example.airlines.DTO.UserDTO;
import com.example.airlines.Models.User;
import com.example.airlines.Repositories.UserRepository;
import com.example.airlines.Services.FileStorageService;
import com.example.airlines.Services.TicketService;
import com.example.airlines.Services.UserService;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final FileStorageService fileStorageService;
    private final TicketService ticketService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<UserDTO> getProfile(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = (User) authentication.getPrincipal();
        UserDTO userDTO = userService.convertToDTO(user);
        if (user.getProfileImageUrl() != null) {
            // Добавляем базовый путь только при отдаче DTO
            userDTO.setProfileImageUrl("/api/profile/images/" + user.getProfileImageUrl());
        }
        return ResponseEntity.ok(userDTO);
    }

    @PostMapping("/upload-image")
    public ResponseEntity<UserDTO> uploadProfileImage(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            User user = (User) authentication.getPrincipal();
            String fileName = fileStorageService.storeFile(file);
            // Сохраняем только имя файла, без пути
            user.setProfileImageUrl(fileName);
            User savedUser = userRepository.save(user);

            UserDTO userDTO = userService.convertToDTO(savedUser);
            // Добавляем полный URL только в DTO
            userDTO.setProfileImageUrl("/api/profile/images/" + fileName);

            return ResponseEntity.ok(userDTO);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/images/{fileName:.+}")
    public ResponseEntity<Resource> getImage(
            @PathVariable String fileName) {

        Resource resource = fileStorageService.loadFile(fileName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE)
                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                .body(resource);
    }

    @GetMapping("/tickets/{ticketId}/pdf")
    public ResponseEntity<byte[]> generateTicketPdf(
            @PathVariable Long ticketId,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = (User) authentication.getPrincipal();

        try {
            TicketDTO ticket = ticketService.getTicketById(ticketId);

            if (!ticketService.isTicketBelongsToUser(ticketId, user.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            // Добавляем контент с русскими шрифтами
            addPdfContent(document, user, ticket);

            document.close();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=ticket_" + ticket.getTicketNumber() + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(out.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    private void addPdfContent(Document document, User user, TicketDTO ticket)
            throws DocumentException, IOException {

        // 1. Загружаем русский шрифт
        Font titleFont = getRussianFont(22, Font.BOLD, BaseColor.RED);
        Font headerFont = getRussianFont(12, Font.BOLD, BaseColor.DARK_GRAY);
        Font valueFont = getRussianFont(12, Font.NORMAL);
        Font bigValueFont = getRussianFont(14, Font.BOLD);

        // 2. Добавляем логотип
        Paragraph logo = new Paragraph("PRONINA AIRLINES", titleFont);
        logo.setAlignment(Element.ALIGN_CENTER);
        logo.setSpacingAfter(20);
        document.add(logo);

        // 3. Создаем таблицу с данными
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(90);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.setSpacingBefore(20);
        table.setSpacingAfter(30);

        // 4. Добавляем строки в таблицу
        addTableRow(table, "ПАССАЖИР:",
                user.getFirstName() + " " + user.getLastName(),
                headerFont, bigValueFont);

        addTableRow(table, "НОМЕР БИЛЕТА:",
                ticket.getTicketNumber(),
                headerFont, valueFont);

        addTableRow(table, "МАРШРУТ:",
                ticket.getDepartureCity() + " → " + ticket.getArrivalCity(),
                headerFont, bigValueFont);

        addTableRow(table, "НОМЕР РЕЙСА:",
                ticket.getFlightNumber(),
                headerFont, valueFont);

        addTableRow(table, "ВЫЛЕТ:",
                formatDate(String.valueOf(ticket.getDepartureTime())) + " (" + ticket.getDepartureCity() + ")",
                headerFont, valueFont);

        addTableRow(table, "ПРИЛЁТ:",
                formatDate(String.valueOf(ticket.getArrivalTime())) + " (" + ticket.getArrivalCity() + ")",
                headerFont, valueFont);

        addTableRow(table, "СТОИМОСТЬ:",
                String.format("%.2f ₽", ticket.getPrice()),
                headerFont, bigValueFont);

        document.add(table);

        // 5. Добавляем нижний колонтитул
        Paragraph footer = new Paragraph(
                "Спасибо, что выбрали Pronina Airlines!",
                getRussianFont(10, Font.ITALIC, BaseColor.GRAY));
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(20);
        document.add(footer);
    }

    /**
     * Создает шрифт с поддержкой кириллицы
     */
    private Font getRussianFont(float size, int style) throws IOException, DocumentException {
        return getRussianFont(size, style, BaseColor.BLACK);
    }

    private Font getRussianFont(float size, int style, BaseColor color)
            throws IOException, DocumentException {

        // 1. Пытаемся загрузить шрифт из ресурсов
        try (InputStream fontStream = getClass().getResourceAsStream("/static/fonts/arialmt.ttf")) {
            if (fontStream != null) {
                BaseFont baseFont = BaseFont.createFont(
                        "arialmt.ttf",
                        BaseFont.IDENTITY_H,
                        BaseFont.EMBEDDED,
                        false,
                        IOUtils.toByteArray(fontStream),
                        null
                );
                return new Font(baseFont, size, style, color);
            }
        } catch (Exception e) {
            System.err.println("Ошибка загрузки шрифта: " + e.getMessage());
        }

        String FONT = "/static/fonts/arialmt.ttf";

        BaseFont baseFont = BaseFont.createFont(FONT,
                BaseFont.IDENTITY_H,
                BaseFont.EMBEDDED
        );
        return new Font(baseFont, size, style, color);
    }

    private void addTableRow(PdfPTable table, String label, String value,
                             Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(5);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(5);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private String formatDate(String dateTime) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm", new Locale("ru"));
            Date date = inputFormat.parse(dateTime);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateTime;
        }
    }
}
